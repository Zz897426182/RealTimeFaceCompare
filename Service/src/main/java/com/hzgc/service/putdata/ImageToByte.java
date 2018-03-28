package com.hzgc.service.putdata;

import org.apache.log4j.Logger;

import javax.imageio.stream.FileImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageToByte {
    private static Logger LOG = Logger.getLogger(ImageToByte.class);
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
}
