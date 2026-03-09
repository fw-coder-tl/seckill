package io.binghe.seckill.order.application.builder;

import io.binghe.seckill.common.builder.SeckillCommonBuilder;
import io.binghe.seckill.common.utils.beans.BeanUtil;
import io.binghe.seckill.order.application.command.SeckillOrderCommand;
import io.binghe.seckill.order.domain.model.entity.SeckillOrder;
public class SeckillOrderBuilder extends SeckillCommonBuilder {

    public static SeckillOrder toSeckillOrder(SeckillOrderCommand seckillOrderCommand){
        if (seckillOrderCommand == null){
            return null;
        }
        SeckillOrder seckillOrder = new SeckillOrder();
        BeanUtil.copyProperties(seckillOrderCommand, seckillOrder);
        return seckillOrder;
    }
}
