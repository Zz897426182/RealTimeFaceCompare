package com.hzgc.service.staticreposuite;


import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.*;

public class Image2Byte2Image {
    private static Logger LOG = Logger.getLogger(Image2Byte2Image.class);
    //图片到byte数组
    public static byte[] image2byte(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()){
            LOG.info("File does not exist!");
            return null;
        }
        FileImageInputStream in = new FileImageInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int readByte = 0;
        byte[] tmp = new byte[1024];
        while((readByte = in.read(tmp)) != -1){
            out.write(tmp, 0, readByte);
        }
        byte[] data = out.toByteArray();
        out.close();
        in.close();
        return data;
    }

    //Byte 数组到图片。
    public static byte[] byte2image(byte[] data, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()){
            LOG.info("File does not exist!");
            file.createNewFile();
        }
        FileImageOutputStream out = new FileImageOutputStream(file);
        out.write(data, 0 , data.length);
        out.close();
        return  data;
    }
}
