package io.binghe.seckill.infrastructure.lock.factory;

import io.binghe.seckill.infrastructure.lock.DistributedLock;

/**
 * 分布式工厂
 */
public interface DistributedLockFactory {

    /**
     * 根据key获取分布式锁
     */
    DistributedLock getDistributedLock(String key);
}
