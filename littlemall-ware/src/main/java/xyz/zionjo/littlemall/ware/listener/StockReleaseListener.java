package xyz.zionjo.littlemall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.zionjo.common.to.mq.OrderTo;
import xyz.zionjo.common.to.mq.StockLockedTo;

import xyz.zionjo.littlemall.ware.service.WareSkuService;


import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {


    @Autowired
    WareSkuService wareSkuService;

    @RabbitListener
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

    @RabbitListener
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
