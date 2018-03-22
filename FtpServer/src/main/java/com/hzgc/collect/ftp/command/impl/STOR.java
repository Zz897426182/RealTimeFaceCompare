package com.hzgc.collect.ftp.command.impl;

import com.hzgc.collect.expand.subscribe.FtpSwitchObject;
import com.hzgc.collect.expand.subscribe.ReceiveObject;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FtpPathMessage;
import com.hzgc.collect.expand.processer.RocketMQProducer;
import com.hzgc.collect.ftp.command.AbstractCommand;
import com.hzgc.collect.ftp.ftplet.*;
import com.hzgc.collect.ftp.impl.*;
import com.hzgc.collect.expand.util.FtpUtils;
import com.hzgc.util.common.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class STOR extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(STOR.class);

    public void execute(final FtpIoSession session,
                        final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {

        try {

            // get state variable
            long skipLen = session.getFileOffset();

            // argument check
            String fileName = request.getArgument();
            if (fileName == null) {
                session
                        .write(LocalizedDataTransferFtpReply
                                .translate(
                                        session,
                                        request,
                                        context,
                                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                                        "STOR", null, null));
                return;
            }

            // 24-10-2007 - added check if PORT or PASV is issued, see
            // https://issues.apache.org/jira/browse/FTPSERVER-110
            DataConnectionFactory connFactory = session.getDataConnection();
            if (connFactory instanceof IODataConnectionFactory) {
                InetAddress address = ((IODataConnectionFactory) connFactory)
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
                LOG.debug("Exception getting file object", ex);
            }
            if (file == null) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.invalid", fileName, null));
                return;
            }

            // get permission
            if (!file.isWritable()) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.permission", fileName, file));
                return;
            }

            // get data connection
            session.write(
                    LocalizedFtpReply.translate(session, request, context,
                            FtpReply.REPLY_150_FILE_STATUS_OKAY, "STOR",
                            fileName)).awaitUninterruptibly(10000);

            DataConnection dataConnection;
            try {
                dataConnection = session.getDataConnection().openConnection();
            } catch (Exception e) {
                LOG.debug("Exception getting the input data stream", e);
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                        FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOR",
                        fileName, file));
                return;
            }

            // transfer data
            boolean failure = false;
            OutputStream outStream = null;
            long transSz = 0L;
            try {
                outStream = file.createOutputStream(skipLen);
                transSz = dataConnection.transferFromClient(session.getFtpletSession(), outStream);

                // attempt to close the output stream so that errors in
                // closing it will return an error to the client (FTPSERVER-119)
                if (outStream != null) {
                    outStream.close();
                }

                LOG.info("File uploaded {}", fileName);

                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics) context
                        .getFtpStatistics();
                ftpStat.setUpload(session, file, transSz);

            } catch (SocketException ex) {
                LOG.debug("Socket exception during data transfer", ex);
                failure = true;
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                        FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "STOR", fileName, file));
            } catch (IOException ex) {
                LOG.debug("IOException during data transfer", ex);
                failure = true;
                session
                        .write(LocalizedDataTransferFtpReply
                                .translate(
                                        session,
                                        request,
                                        context,
                                        FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                                        "STOR", fileName, file));
            } finally {
                // make sure we really close the output stream
                IOUtil.closeStream(outStream);
                fileName = file.getAbsolutePath();

                //此处产生LogEvent对象用来进行异步处理
                if (fileName.contains("unknown")) {
                    LOG.error(fileName + ":contain unknown ipcID, Not send to rocketMQ and Kafka!");
                } else {
                    int faceNum = FtpUtils.pickPicture(fileName);
                    if (fileName.contains(".jpg") && faceNum > 0) {
                        FtpPathMessage message = FtpUtils.getFtpPathMessage(fileName);
                        if (FtpSwitchObject.isFtpSwitch()) {
                            List<String> showList = ReceiveObject.getInstance().getIpcIdList_show();
                            if (!showList.isEmpty()) {
                                if (showList.contains(message.getIpcid())) {
                                    sendMQAndWriteLogEvent(fileName, file, message, context);
                                }
                            } else {
                                List<String> subscriptionList = ReceiveObject.getInstance().getIpcIdList_subscription();
                                if (!subscriptionList.isEmpty()) {
                                    if (subscriptionList.contains(message.getIpcid())) {
                                        sendMQAndWriteLogEvent(fileName, file, message, context);
                                    }
                                } else {
                                    writeLogEvent(fileName, file, context);
                                }
                            }
                        } else {
                            sendMQAndWriteLogEvent(fileName, file, message, context);
                        }
                    }
                }
            }

            // if data transfer ok - send transfer complete message
            if (!failure) {
                session.write(LocalizedDataTransferFtpReply.translate(session, request, context,
                        FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "STOR",
                        fileName, file, transSz));

            }
        } finally {
            session.resetState();
            session.getDataConnection().closeDataConnection();
        }
    }

    private void sendMQAndWriteLogEvent(String fileName, FtpFile file, FtpPathMessage message, FtpServerContext context) {
        //拼装ftpUrl (带主机名的ftpUrl)
        String ftpHostNameUrl = FtpUtils.filePath2FtpUrl(fileName);
        //获取ftpUrl (带IP地址的ftpUrl)
        String ftpIpUrl = FtpUtils.getFtpUrl(ftpHostNameUrl);
        LogEvent event = new LogEvent();
        event.setTimeStamp(System.currentTimeMillis());
        event.setAbsolutePath(file.getFileAbsolutePa());
        event.setFtpPath(ftpHostNameUrl);
        event.setStatus("0");
        //发送到rocketMQ
        RocketMQProducer.getInstance().send(message.getIpcid(), message.getTimeStamp(), ftpIpUrl.getBytes());
        context.getScheduler().putData(event);
    }

    private void writeLogEvent(String fileName, FtpFile file, FtpServerContext context) {
        //拼装ftpUrl (带主机名的ftpUrl)
        String ftpHostNameUrl = FtpUtils.filePath2FtpUrl(fileName);
        LogEvent event = new LogEvent();
        event.setTimeStamp(System.currentTimeMillis());
        event.setAbsolutePath(file.getFileAbsolutePa());
        event.setFtpPath(ftpHostNameUrl);
        event.setStatus("0");
        context.getScheduler().putData(event);
    }
}
