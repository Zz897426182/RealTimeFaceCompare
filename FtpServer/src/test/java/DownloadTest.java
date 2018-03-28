import com.hzgc.ftpserver.util.DownloadUtils;

import java.util.Arrays;

public class DownloadTest {
    public static void main(String[] args) {
        String ftpAddress = "192.168.1.28";
        String ftpUserName = "admin";
        String ftpPassword = "123456";
        int ftpPort = 2121;
        String ftpFilePath = "/3B0383FPAG00883/2017/05/23/16/00/";
        String ftpFileName = "2017_05_23_16_00_15_5704_0.jpg";
        String localPath = "F:\\data";
        String localFileName = "aaa.jpg";
        String localFileName22 = "20111.jpg";
        String ftpUrl = "ftp://172.18.18.109:2121/3B0000000000000/2017/05/23/16/00/2017_05_23_16_00_15_5704_1.jpg";
        //DownloadUtils.downloadFtpFile(ftpAddress, ftpUserName, ftpPassword, ftpPort, ftpFilePath, ftpFileName, localPath, localFileName);
        DownloadUtils.downloadFtpFile(ftpUrl, localPath, localFileName22);
        byte[] bytes = DownloadUtils.downloadftpFile2Bytes(ftpUrl);
        System.out.println(Arrays.toString(bytes));
        System.out.println("ok");
    }
}
