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

import com.hzgc.ftpserver.command.AbstractCommand;
import com.hzgc.ftpserver.ftplet.FtpException;
import com.hzgc.ftpserver.ftplet.FtpReply;
import com.hzgc.ftpserver.ftplet.FtpRequest;
import com.hzgc.ftpserver.impl.FtpIoSession;
import com.hzgc.ftpserver.impl.FtpServerContext;
import com.hzgc.ftpserver.impl.LocalizedFtpReply;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * <code>NOOP &lt;CRLF&gt;</code><br>
 * 
 * This command does not affect any parameters or previously entered commands.
 * It specifies no action other than that the server send an OK reply.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class NOOP extends AbstractCommand {

    /**
     * Execute command
     */
    public void execute(final FtpIoSession session,
                        final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {

        session.resetState();
        session.write(LocalizedFtpReply.translate(session, request, context,
                FtpReply.REPLY_200_COMMAND_OKAY, "NOOP", null));
    }
}
