package com.hzgc.jni;

import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.dubbo.feature.FaceExtract;
import org.apache.log4j.Logger;

public class FaceExtractImpl implements FaceExtract {
    private static Logger LOG = Logger.getLogger(FaceExtractImpl.class);
    private FaceExtractImpl() {
        try {
            LOG.info("Start NativeFunction init....");
            NativeFunction.init();
            LOG.info("Init NativeFunction successful!");
        } catch (Exception e) {
            LOG.error("Init NativeFunction failure!");
            e.printStackTrace();
        }
    }

    @Override
    public FaceAttribute featureExtract(byte[] imageBytes) {
        if (imageBytes != null && imageBytes.length > 0) {
            return FaceFunction.featureExtract(imageBytes);
        }
        return null;
    }
}
