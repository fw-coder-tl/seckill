package io.binghe.seckill.activity.domain.event;


import io.binghe.seckill.common.event.SeckillBaseEvent;
public class SeckillActivityEvent extends SeckillBaseEvent {

    public SeckillActivityEvent(Long id, Integer status) {
        super(id, status);
    }
}
