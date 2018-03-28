package com.hzgc.util.common;

import java.io.*;

public class ObjectUtil {

    public static byte[] objectToByte(Object obj) {
        byte[] buffer = null;
        ObjectOutputStream os = null;
        ByteArrayOutputStream baos = null;
        if (obj != null) {
            try {
                baos = new ByteArrayOutputStream();
                os = new ObjectOutputStream(baos);
                os.writeObject(obj);
                buffer = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeStream(os);
                IOUtil.closeStream(baos);
            }
            return buffer;
        }
        return buffer;
    }

    public static java.lang.Object byteToObject(byte[] bc) {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bc);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            bis.close();
            ois.close();
        }
        catch(Exception e) {
            System.out.println("translation"+e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(bis);
            IOUtil.closeStream(ois);
        }
        return obj;
    }
}
