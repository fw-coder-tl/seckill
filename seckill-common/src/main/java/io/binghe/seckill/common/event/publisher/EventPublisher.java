package io.binghe.seckill.common.event.publisher;


import com.alibaba.cola.event.DomainEventI;
public interface EventPublisher {
    /**
     * 发布事件
     */
    void publish(DomainEventI domainEvent);
}
