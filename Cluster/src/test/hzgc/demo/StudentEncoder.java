package demo;

import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

public class StudentEncoder implements Encoder<Student> {
    public StudentEncoder(VerifiableProperties verifiableProperties){
    }
    @Override
    public byte[] toBytes(Student s) { //填写你需要传输的对象
        return BeanUtils.ObjectToBytes(s);
    }
}
