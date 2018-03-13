/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.hzgc.ftpserver.command.impl;

import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.ftpserver.captureSubscription.CaptureSubscriptionObject;
import com.hzgc.ftpserver.captureSubscription.FTPShow;
import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.ftpserver.producer.ProducerOverFtp;
import com.hzgc.ftpserver.producer.RocketMQProducer;
import com.hzgc.ftpserver.common.FtpUtil;
import com.hzgc.ftpserver.queue.BufferQueue;
import com.hzgc.jni.FaceFunction;
import com.hzgc.ftpserver.command.AbstractCommand;
import com.hzgc.ftpserver.ftplet.*;
import com.hzgc.ftpserver.impl.*;
import com.hzgc.ftpserver.util.IoUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * <strong>Internal class, do not use directly.</strong>
 * <p>
 * <code>STOR &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 * <p>
 * This command causes the server-DTP to accept the data transferred via the
 * data connection and to store the data as a file at the server site. If the
 * file specified in the pathname exists at the server site, then its contents
 * shall be replaced by the data being transferred. A new file is created at the
 * server site if the file specified in the pathname does not already exist.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class STOR extends AbstractCommand {

    private final Logger LOG = LoggerFactory.getLogger(STOR.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Execute command.
     */
    public void execute(final FtpIoSession session,
                        final FtpServerContext ftpServerContext, final FtpRequest request)
            throws IOException, FtpException {

        DefaultFtpServerContext context = null;
        if (ftpServerContext instanceof DefaultFtpServerContext) {
            context = (DefaultFtpServerContext) ftpServerContext;
        }

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
                        "STOR.invalid", fileName, file));
                return;
            }
            fileName = file.getAbsolutePath();

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

            IODataConnection dataConnection;
            try {
                IODataConnectionFactory customConnFactory = (IODataConnectionFactory) session.getDataConnection();
                dataConnection = new IODataConnection(customConnFactory.createDataSocket(), customConnFactory.getSession(), customConnFactory);
                //dataConnection = session.getDataConnection().openConnection();
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

                RocketMQProducer rocketMQProducer = context.getProducerRocketMQ();
                CaptureSubscriptionObject subscriptionObject = context.getSubscriptionObject();
                InputStream is = dataConnection.getDataInputStream();
                ByteArrayOutputStream baos = FtpUtil.inputStreamCacher(is);
                byte[] data = baos.toByteArray();

                int faceNum = FtpUtil.pickPicture(fileName);
                if (fileName.contains("unknown")) {
                    LOG.error(fileName + ": contain unknown ipcID, Not send to rocketMQ and Kafka!");
                } else {
                    //当FTP接收到小图
                    if (fileName.contains(".jpg") && faceNum > 0) {
                        Map<String, String> map = FtpUtil.getFtpPathMessage(fileName);
                        //若获取不到信息，则不发rocketMQ和Kafka
                        if (!map.isEmpty()) {
                            String ipcID = map.get("ipcID");
                            String timeStamp = map.get("time");

                            //TODO 抓拍订阅及演示功能
                            //List<String> ipcIdList = subscriptionObject.getIpcIdList();
                            List<String> ipcIdList = FTPShow.getIpcIdList();
                            if (FTPShow.isIsShow()){
                                if (!ipcIdList.isEmpty() && ipcIdList.contains(ipcID)) {
                                    //拼装ftpUrl (带主机名的ftpUrl)
                                    String ftpHostNameUrl = FtpUtil.filePath2FtpUrl(fileName);
                                    //获取ftpUrl (带IP地址的ftpUrl)
                                    String ftpIpUrl = FtpUtil.getFtpUrl(ftpHostNameUrl);
                                    //发送到rocketMQ
                                    rocketMQProducer.send(ipcID, timeStamp, ftpIpUrl.getBytes());
                                }
                            }else if (!FTPShow.isIsShow()){
                                //拼装ftpUrl (带主机名的ftpUrl)
                                String ftpHostNameUrl = FtpUtil.filePath2FtpUrl(fileName);
                                //获取ftpUrl (带IP地址的ftpUrl)
                                String ftpIpUrl = FtpUtil.getFtpUrl(ftpHostNameUrl);
                                //发送到rocketMQ
                                rocketMQProducer.send(ipcID, timeStamp, ftpIpUrl.getBytes());
                            }
                        }
                    }
                }

                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                transSz = dataConnection.transferFromClient(session.getFtpletSession(), new BufferedInputStream(bais), outStream);
                //transSz = dataConnection.transferFromClient(session.getFtpletSession(), outStream);

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
                IoUtils.close(outStream);
                // Put filePath to queue and send kafka
                int faceNum = FtpUtil.pickPicture(fileName);
                if (fileName.contains("unknown")) {
                    LOG.error(fileName + ": contain unknown ipcID, Not send to rocketMQ and Kafka!");
                } else {
                    if (fileName.contains(".jpg") && faceNum > 0) {
                        Map<String, String> map = FtpUtil.getFtpPathMessage(fileName);
                        //若获取不到信息，则不发rocketMQ和Kafka
                        if (!map.isEmpty()) {
                            String ipcID = map.get("ipcID");
                            List<String> ipcIdList = FTPShow.getIpcIdList();
                            if (FTPShow.isIsShow()){
                                if (!ipcIdList.isEmpty() && ipcIdList.contains(ipcID)) {
                                    BufferQueue bufferQueue = context.getBufferQueue();
                                    BlockingQueue<String> queue = bufferQueue.getQueue();
                                    try {
                                        queue.put(fileName);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    LOG.info("Push to queue success,queue size : " + queue.size());
                                }
                            }else if (!FTPShow.isIsShow()){
                                BufferQueue bufferQueue = context.getBufferQueue();
                                BlockingQueue<String> queue = bufferQueue.getQueue();
                                try {
                                    queue.put(fileName);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                LOG.info("Push to queue success,queue size : " + queue.size());
                            }
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
}
