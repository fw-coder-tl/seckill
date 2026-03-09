package io.binghe.seckill.order.application.service;


import io.binghe.seckill.order.application.command.SeckillOrderCommand;
import io.binghe.seckill.order.domain.model.entity.SeckillOrder;

import java.util.List;
public interface SeckillOrderService {

    /**
     * 保存订单
     */
    Long saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand);

    /**
     * 根据用户id获取订单列表
     */
    List<SeckillOrder> getSeckillOrderByUserId(Long userId);

    /**
     * 根据活动id获取订单列表
     */
    List<SeckillOrder> getSeckillOrderByActivityId(Long activityId);
}
