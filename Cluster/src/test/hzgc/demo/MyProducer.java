package demo;

/**
 * Producer往kafka写对象（在写的过程中使用StudentEncoder作为编码器）
 */

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class MyProducer {
    private static Properties props;
    private static Producer<String, Student> producer;
    private static String topic="kafka_hzgc";
    static {
        if(null==props)
        {
            Properties props = new Properties();
            props.put("metadata.broker.list", "172.18.18.100:21005,172.18.18.101:21005,172.18.18.102:21005");
            props.put("bootstrap.servers", "172.18.18.100:24002,172.18.18.101:24002,172.18.18.102:24002");
            //在kafka的参数配置中引用自定义的Encoder
            props.put("serializer.class", StudentEncoder.class.getName());
            producer = new Producer<String, Student>(new ProducerConfig(props));
        }
    }

   public static void main(String args[]) {
        Student s = new Student();
        s.setId(12);
        s.setName("liushanbin");
        ProducerRecord<String, Student> data = new ProducerRecord<String, Student>("kafka_hzgc", "student", s);
        for (int i = 0; i < 10; i++) {
            producer.send(new KeyedMessage<String, Student>(topic, s));
        }
        producer.close();
    }
}
