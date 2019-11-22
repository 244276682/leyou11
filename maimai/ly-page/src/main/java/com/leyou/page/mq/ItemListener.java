package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    /**
     * 接收消息 添加或修改静态页
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name="page.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void ListenInsertOrUpdate(Long spuId){
        if(spuId==null){
            return;
        }
        //处理消息，对索引库进行新增和修改
        pageService.createHtml(spuId);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name="page.item.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void ListenDelete(Long spuId){
        if(spuId==null){
            return;
        }
        //处理消息，对索引库进行新增和修改
        pageService.deleteHtml(spuId);
    }
}
