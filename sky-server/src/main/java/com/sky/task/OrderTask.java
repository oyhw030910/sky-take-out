package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void dealTimeOut(){
        log.info("处理超时未支付订单,{}", LocalDateTime.now());

        LocalDateTime time=LocalDateTime.now().plusMinutes(-15);

        List<Orders> orders=orderMapper.getByStatusAndTime(Orders.PENDING_PAYMENT,time);

        if (orders!=null&&!orders.isEmpty()){
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("超时未支付");
                orderMapper.update(order);

            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")
    public  void dealInDelivery(){
        log.info("处理仍处于派送中的订单,{}",LocalDateTime.now());
        LocalDateTime time=LocalDateTime.now().plusHours(-1);

        List<Orders> orders=orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS,time);

        if (orders!=null&&!orders.isEmpty()){
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
