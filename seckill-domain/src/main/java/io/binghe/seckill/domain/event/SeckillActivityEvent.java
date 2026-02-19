package io.binghe.seckill.domain.event;

/**
 * 活动事件
 */
public class SeckillActivityEvent extends SeckillBaseEvent{
    public SeckillActivityEvent(Long id, Integer status) {
        super(id, status);
    }
}
