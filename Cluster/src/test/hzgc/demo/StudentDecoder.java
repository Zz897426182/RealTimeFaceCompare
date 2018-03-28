package demo;


import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;

/**
 * Created by Administrator on 2017-7-25.
 */
public class StudentDecoder implements Decoder<Student> {
    public StudentDecoder(VerifiableProperties verifiableProperties){
    }
    @Override
    public Student fromBytes(byte[] bytes){
        return BeanUtils.BytesToObject(bytes);
    }
}