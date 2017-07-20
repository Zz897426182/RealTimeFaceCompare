package com.hzgc.ftpserver.kafka.ftp;

import com.hzgc.ftpserver.kafka.producer.ProducerOverFtp;
import com.hzgc.ftpserver.local.LocalIODataConnection;
import com.hzgc.ftpserver.util.Utils;
import org.apache.ftpserver.command.AbstractCommand;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.impl.*;
import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;

public class KafkaSTOR extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(KafkaSTOR.class);

    public void execute(final FtpIoSession session,
                        final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {
        KafkaFtpServerContext kafkaContext = null;
        if (context instanceof KafkaFtpServerContext) {
            kafkaContext = (KafkaFtpServerContext)context;
        }
        try {

            // get state variable
            long skipLen = session.getFileOffset();

            // argument check
            String fileName = request.getArgument();
            if (fileName == null) {
                session
                        .write(LocalizedFtpReply
                                .translate(
                                        session,
                                        request,
                                        kafkaContext,
                                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                                        "STOR", null));
                return;
            }

            DataConnectionFactory connFactory = null;
            try {
                connFactory = session.getDataConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (connFactory != null) {
                InetAddress address = ((ServerDataConnectionFactory) connFactory)
                        .getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(
                            FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                            "PORT or PASV must be issued first"));
                    return;
                }
            }

            // get filename
            FtpFile file = null;
            try {
                file = session.getFileSystemView().getFile(fileName);
            } catch (Exception ex) {
                LOG.info("Exception getting file object", ex);
            }
            if (file == null) {
                session.write(LocalizedFtpReply.translate(session, request, kafkaContext,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.invalid", fileName));
                return;
            }
            fileName = file.getAbsolutePath();

            // get permission
            if (!file.isWritable()) {
                session.write(LocalizedFtpReply.translate(session, request, kafkaContext,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.permission", fileName));
                return;
            }

            // get data connection
            session.write(
                    LocalizedFtpReply.translate(session, request, kafkaContext,
                            FtpReply.REPLY_150_FILE_STATUS_OKAY, "STOR",
                            fileName)).awaitUninterruptibly(10000);

            LocalIODataConnection dataConnection;
            try {
                IODataConnectionFactory customConnFactory = (IODataConnectionFactory)session.getDataConnection();
                dataConnection = new LocalIODataConnection(customConnFactory.createDataSocket(), customConnFactory.getSession(), customConnFactory);
            } catch (Exception e) {
                LOG.info("Exception getting the input data stream", e);
                session.write(LocalizedFtpReply.translate(session, request, kafkaContext,
                        FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOR",
                        fileName));
                return;
            }

            // transfer data
            boolean failure = false;
            OutputStream outStream = null;
            try {
                ByteArrayOutputStream value;
                InputStream is = dataConnection.getDataInputStream();
                value = Utils.inputStreamCacher(is);
                String key;
                ProducerOverFtp kafkaProducer = kafkaContext.getProducerOverFtp();
                long transSz;
                //parsing JSON files
                if (file.getName().contains(".json")) {
                    key = Utils.transformNameToKey(fileName);
                    LOG.info("Kafka Producer Send message[" + file.getName() + "] to Kafka");
                    kafkaProducer.sendKafkaMessage(kafkaProducer.getJson(), key, value.toByteArray());
                    transSz = value.toByteArray().length;
                } else if (fileName.contains(".jpg")) {
                    key = Utils.transformNameToKey(fileName);
                    //it is picture
                    if (Utils.pickPicture(fileName) == 0) {
                        LOG.info("Kafka Producer Send message[" + file.getName() + "] to Kafka");
                        kafkaProducer.sendKafkaMessage(kafkaProducer.getPicture(), key, value.toByteArray());
                        transSz = value.toByteArray().length;
                    } else if (Utils.pickPicture(fileName) > 0) {
                        LOG.info("Kafka Producer Send message[" + file.getName() + "] to Kafka");
                        kafkaProducer.sendKafkaMessage(kafkaProducer.getFace(), Utils.faceKey(Utils.pickPicture(fileName), key), value.toByteArray());
                        transSz = value.toByteArray().length;
                    } else {
                        LOG.info("Contains illegal file[" + file.getName()  + "], write to local default");
                        outStream = file.createOutputStream(skipLen);
                        ByteArrayInputStream bis = new ByteArrayInputStream(value.toByteArray());
                        transSz = dataConnection.transferFromClient(session.getFtpletSession(), bis, outStream);
                    }
                } else {
                    LOG.info("Contains illegal file[" + fileName  + "], write to local default");
                    outStream = file.createOutputStream(skipLen);
                    ByteArrayInputStream bis = new ByteArrayInputStream(value.toByteArray());
                    transSz = dataConnection.transferFromClient(session.getFtpletSession(), bis, outStream);
                }
                // attempt to close the output stream so that errors in
                // closing it will return an error to the client (FTPSERVER-119)
                if(outStream != null) {
                    outStream.close();
                }

                LOG.info("File uploaded {}", fileName);

                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics) kafkaContext
                        .getFtpStatistics();
                ftpStat.setUpload(session, file, transSz);

            } catch (SocketException ex) {
                LOG.info("Socket exception during data transfer", ex);
                failure = true;
                session.write(LocalizedFtpReply.translate(session, request, kafkaContext,
                        FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "STOR", fileName));
            } catch (IOException ex) {
                LOG.info("IOException during data transfer", ex);
                failure = true;
                session
                        .write(LocalizedFtpReply
                                .translate(
                                        session,
                                        request,
                                        kafkaContext,
                                        FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                                        "STOR", fileName));
            } finally {
                // make sure we really close the output stream
                IoUtils.close(outStream);
            }

            // if data transfer ok - send transfer complete message
            if (!failure) {
                session.write(LocalizedFtpReply.translate(session, request, kafkaContext,
                        FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "STOR",
                        fileName));

            }
        } finally {
            session.resetState();
            session.getDataConnection().closeDataConnection();
        }
    }
}
