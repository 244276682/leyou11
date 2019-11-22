package com.leyou.sms.config.mq;


import com.leyou.common.utils.JsonToUtils;
import com.leyou.sms.config.sms.SmsProperties;
import com.leyou.sms.config.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties properties;

    /**
     * 发送短信验证码
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name="sms.verify.code.queue", durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange",type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"
    ))
    public void ListenInsertOrUpdate(Map<String,String> msg){
        if(CollectionUtils.isEmpty(msg)){
            return ;
        }
        String phone = msg.remove("phone");
        if(StringUtils.isEmpty(phone)){
            return;
        }
        //发送给短信验证码  try处理消息失败 会自动回滚 可能触发限流
        smsUtils.sendSms(phone,properties.getSignName(),properties.getVerifyCodeTemplate(),JsonToUtils.object2json(msg));
        log.info("【短信服务】，发送短信验证码{}，手机号{}",phone,msg);
    }


}
