package com.leyou.sms.config.mq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsListenerTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public  void sendsms(){
        Map<String, String> msg = new HashMap<>();
        msg.put("phone","13012045150");
        msg.put("code","15897");
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
    }


}