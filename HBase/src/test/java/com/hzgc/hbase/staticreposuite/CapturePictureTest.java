package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.dynamicrepo.CapturedPicture;
import com.hzgc.dubbo.dynamicrepo.PictureType;
import com.hzgc.hbase.dynamicrepo.CapturePictureSearchServiceImpl;
import com.hzgc.hbase.dynamicrepo.DynamicPhotoServiceImpl;
import org.junit.Test;

/**
 * CapturePictureSearchServiceImpl test class
 */
public class CapturePictureTest {

    //private static final String rowkey = "17130NCY0HZ0004-0_00000000000000_170523160015_0000004075_01";
    //private static final String rowkey = "62513NCY0HZ3544151_0000000000000_170523160015_0000004075_01";
    private static final int type = 5;
    private static final String rowkey = "62513NCY0HZ3544151_0000000000000_170523160015_0000004075_51";
    private static final float[] Float = {1.175494351f, 0.125f};

    @Test
    public void getCaptureMessageTest() {
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        CapturedPicture capturedPicture = new CapturedPicture();
        capturedPicture = capturePictureSearchService.getCaptureMessage(rowkey, type);
        System.out.println(capturedPicture);
    }

    @Test
    public void insertePictureFeatureTest() {
        DynamicPhotoServiceImpl dynamicPhotoService = new DynamicPhotoServiceImpl();
        boolean b = dynamicPhotoService.insertePictureFeature(PictureType.CAR, rowkey, Float);
        System.out.println(b);
    }

}
