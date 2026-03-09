package io.binghe.seckill.common.event;

import com.alibaba.cola.event.DomainEventI;
public class SeckillBaseEvent implements DomainEventI {

    private Long id;
    private Integer status;

    public SeckillBaseEvent(Long id, Integer status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
