package io.binghe.seckill.domain.event.publisher;

import com.alibaba.cola.event.DomainEventI;

/**
 * 事件发布器接口
 */
public interface EventPublisher {

    /**
     * 发布事件
     */
    void publish(DomainEventI domainEvent);
}
