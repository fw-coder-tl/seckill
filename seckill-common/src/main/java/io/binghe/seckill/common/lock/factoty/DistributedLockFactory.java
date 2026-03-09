package io.binghe.seckill.common.lock.factoty;


import io.binghe.seckill.common.lock.DistributedLock;
public interface DistributedLockFactory {

    /**
     * 根据key获取分布式锁
     */
    DistributedLock getDistributedLock(String key);
}
