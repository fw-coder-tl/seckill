package io.binghe.seckill.order.domain.event;

import io.binghe.seckill.common.event.SeckillBaseEvent;
public class SeckillOrderEvent extends SeckillBaseEvent {

    public SeckillOrderEvent(Long id, Integer status) {
        super(id, status);
    }
}
