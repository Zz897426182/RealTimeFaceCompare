import com.hzgc.ftpserver.captureSubscription.CaptureSubscriptionObject;
import com.hzgc.ftpserver.captureSubscription.MQSwitchImpl;
import com.hzgc.ftpserver.captureSubscription.MQSwitchInit;
import com.hzgc.ftpserver.captureSubscription.MQSwitchStart;

import java.util.ArrayList;
import java.util.List;

public class ZKClientTest {
    public static void main(String[] args) {

        //MQSwitchInit.create();

        /*MQSwitchStart mqSwitchStart = new MQSwitchStart();
        mqSwitchStart.start();*/
       // MQSwitchInit init = new MQSwitchInit();
        MQSwitchImpl mqSwitch = new MQSwitchImpl();
        /*String userId1 = "user44";
        long time1 = 1516334343;
        List<String> user1 = new ArrayList<>();
        user1.add("aaaa");
        user1.add("bbbb");
        user1.add("cccc");
        mqSwitch.openMQReception(userId1,time1,user1);

        String userId2 = "user55";
        long time2 = 1516334428;//2018-01-19 11:37:36
        List<String> user2 = new ArrayList<>();
        user2.add("sss");
        user2.add("552");
        user2.add("145");
        mqSwitch.openMQReception(userId2,time2,user2);

        String userId3 = "user66";
        long time3 = 1516334343;
        List<String> user3 = new ArrayList<>();
        user3.add("xiao");
        user3.add("qi");
        mqSwitch.openMQReception(userId3,time3,user3);*/

        mqSwitch.closeMQReception("user22");
    }
}
