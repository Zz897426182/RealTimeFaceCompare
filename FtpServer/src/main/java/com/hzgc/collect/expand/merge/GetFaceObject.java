package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.collect.expand.processer.FtpPathMessage;
import com.hzgc.collect.expand.util.JSONHelper;
import com.hzgc.collect.expand.util.FtpUtils;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.jni.FaceFunction;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class GetFaceObject {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static FaceObject getFaceObject(String row) {
        FaceObject faceObject = null;
        if (row != null && row.length() != 0) {
            LogEvent event = JSONHelper.toObject(row, LogEvent.class);
            // 路径中不包含/opt/ftpdata
            String path = event.getFtpPath();
            // 路径中包含/opt/ftpdata/
            String absolutePath = event.getAbsolutePath();

            byte[] photo = FaceFunction.getPictureBytes(absolutePath);
            if (photo != null) {
                FaceAttribute faceAttribute = FaceFunction.featureExtract(photo);
                FtpPathMessage ftpPathMessage = FtpUtils.getFtpPathMessage(path);
                String ipcId = ftpPathMessage.getIpcid();
                String timeStamp = ftpPathMessage.getTimeStamp();
                String timeSlot = ftpPathMessage.getTimeslot();
                String date = ftpPathMessage.getDate();
                SearchType type = SearchType.PERSON;
                String startTime = sdf.format(new Date());

                int sharpness = 1;
                try {
                    BufferedImage image = ImageIO.read(new FileInputStream(event.getAbsolutePath()));
                    int imageHeight = image.getHeight();
                    int imageWidth = image.getWidth();
                    if (imageHeight > 80 && imageWidth>80){
                        sharpness = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                faceObject = new FaceObject(ipcId, timeStamp, type, date, timeSlot, faceAttribute, startTime, sharpness);
            }
        }
        return faceObject;
    }
}
