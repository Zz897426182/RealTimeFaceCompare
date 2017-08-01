package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.dynamicrepo.CapturedPicture;
import com.hzgc.hbase.dynamicrepo.CapturePictureSearchServiceImpl;
import com.hzgc.hbase.util.HBaseHelper;
import org.apache.hadoop.hbase.client.Table;

/**
 * CapturePictureSearchServiceImpl test class
 */
public class CapturePictureTest {

    //private static final String rowkey = "17130NCY0HZ0004-0_00000000000000_170523160015_0000004075_01";
    private static final String rowkey = "62513NCY0HZ3544151_0000000000000_170523160015_0000004075_01";
    private static final int type = 5;

    @org.junit.Test
    public void getCaptureMessageTest() {
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        CapturedPicture capturedPicture = new CapturedPicture();
        capturedPicture = capturePictureSearchService.getCaptureMessage(rowkey, type);
        if (null != capturedPicture) {
            if (null != capturedPicture.getBigImage()) {
                System.out.println(capturedPicture.getBigImage());
            }
            if (null != capturedPicture.getDescription()) {
                System.out.println(capturedPicture.getDescription());
            }
            if (null != capturedPicture.getExtend()) {

                System.out.println(capturedPicture.getExtend());
            }
            if (null != capturedPicture.getId()) {
                System.out.println(capturedPicture.getId());
            }

            System.out.println(capturedPicture.getSimilarity());

            if (null != capturedPicture.getSmallImage()) {
                System.out.println(capturedPicture.getSmallImage());
            }

            System.out.println(capturedPicture.getTimeStamp());
        }
    }


}
