package io.binghe.seckill.application.cache.service.activity.impl;

import com.alibaba.fastjson.JSON;
import io.binghe.seckill.application.builder.SeckillActivityBuilder;
import io.binghe.seckill.application.cache.model.SeckillBusinessCache;
import io.binghe.seckill.application.cache.service.activity.SeckillActivityCacheService;
import io.binghe.seckill.domain.constants.SeckillConstants;
import io.binghe.seckill.domain.model.entity.SeckillActivity;
import io.binghe.seckill.domain.repository.SeckillActivityRepository;
import io.binghe.seckill.infrastructure.cache.distribute.DistributedCacheService;
import io.binghe.seckill.infrastructure.cache.local.LocalCacheService;
import io.binghe.seckill.infrastructure.lock.DistributedLock;
import io.binghe.seckill.infrastructure.lock.factory.DistributedLockFactory;
import io.binghe.seckill.infrastructure.utils.string.StringUtil;
import io.binghe.seckill.infrastructure.utils.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillActivityCacheServiceImpl implements SeckillActivityCacheService {

    private final static Logger logger = LoggerFactory.getLogger(SeckillActivityCacheServiceImpl.class);

    @Autowired
    private LocalCacheService<Long, SeckillBusinessCache<SeckillActivity>> localCacheService;

    // 分布式锁前缀
    private static final String SECKILL_ACTIVITY_UPDATE_CACHE_LOCK_KEY = "SECKILL_ACTIVITY_UPDATE_CACHE_LOCK_KEY_";
    // 本地锁
    private final Lock localCacheUpdatelock = new ReentrantLock();

    @Autowired
    private DistributedCacheService distributedCacheService;

    @Autowired
    private SeckillActivityRepository seckillActivityRepository;

    @Autowired
    private DistributedLockFactory distributedLockFactory;

    @Override
    public SeckillBusinessCache<SeckillActivity> getCachedSeckillActivity(Long activityId, Long version) {
        // 先从本地缓存获取数据
        SeckillBusinessCache<SeckillActivity> seckillActivityCache = localCacheService.getIfPresent(activityId);
        if (seckillActivityCache != null) {
            // 判断版本号是否为空
            if (version == null) {
                logger.info("SeckillActivityCache|命中本地缓存|{}", activityId);
                return seckillActivityCache;
            }
            if (version.compareTo(seckillActivityCache.getVersion()) <= 0) {
                logger.info("SeckillActivityCache|命中本地缓存|{}", activityId);
                return seckillActivityCache;
            }
            //传递的版本号大于缓存中的版本号，说明缓存中的数据比较落后，从分布式缓存获取数据并更新到本地缓存
            if (version.compareTo(seckillActivityCache.getVersion()) > 0) {
                return getDistributedCache(activityId);
            }
        }
        // 获取分布式缓存，并设置到本地缓存中
        return getDistributedCache(activityId);
    }

    /**
     * 利用分布式锁保证只有一个线程去更新分布式缓存中的数据
     */
    private SeckillBusinessCache<SeckillActivity> getDistributedCache(Long activityId) {
        logger.info("SeckillActivityCache|读取分布式缓存|{}", activityId);
        // 从分布式缓存获取数据
        SeckillBusinessCache<SeckillActivity> seckillActivityCache = SeckillActivityBuilder.getSeckillBusinessCache(distributedCacheService.getObject(buildCacheKey(activityId)), SeckillActivity.class);
        // 分布式缓存没有数据
        if (seckillActivityCache == null) {
            // 尝试更新分布式缓存中数据
            seckillActivityCache = tryUpdateSeckillActivityCacheByLock(activityId);
        }
        // 获取的数据不为空，并且不需要重试
        if (seckillActivityCache !=null && !seckillActivityCache.isRetryLater()) {
            // 获取分布式锁
            if (localCacheUpdatelock.tryLock()) {
                try {
                    localCacheService.put(activityId, seckillActivityCache);
                    logger.info("SeckillActivityCache|本地缓存已经更新|{}", activityId);
                }finally {
                    localCacheUpdatelock.unlock();
                }
            }
        }
        return seckillActivityCache;
    }

    @Override
    public SeckillBusinessCache<SeckillActivity> tryUpdateSeckillActivityCacheByLock(Long activityId) {
        logger.info("SeckillActivityCache|读取分布式缓存|{}", activityId);
        // 获取分布式锁
        DistributedLock lock = distributedLockFactory.getDistributedLock(SECKILL_ACTIVITY_UPDATE_CACHE_LOCK_KEY.concat(String.valueOf(activityId)));
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            // 没有获取到分布式锁就立马返回缓存资源
            if (!isLockSuccess) {
                return new SeckillBusinessCache<SeckillActivity>().retryLater();
            }
            // 获取锁成功后，再次从缓存中获取数据，防止高并发下多个线程强锁的过程中，后续的线程在等待1秒的时候,前面的线程释放掉了锁，后续的线程获取到锁之后再次更新分布式缓存数据
            SeckillBusinessCache<SeckillActivity> seckillActivityCache = SeckillActivityBuilder.getSeckillBusinessCache(distributedCacheService.getObject(buildCacheKey(activityId)), SeckillActivity.class);
            if (seckillActivityCache != null){
                return seckillActivityCache;
            }
            // 从数据库获取数据
            SeckillActivity seckillActivity = seckillActivityRepository.getSeckillActivityById(activityId);
            if (seckillActivity == null) {
                seckillActivityCache = new SeckillBusinessCache<SeckillActivity>().notExist();
            } else {
                seckillActivityCache = new SeckillBusinessCache<SeckillActivity>().with(seckillActivity).withVersion(SystemClock.millisClock().now());
            }
            // 将数据保存到分布式缓存
            distributedCacheService.put(buildCacheKey(activityId), JSON.toJSONString(seckillActivityCache), SeckillConstants.FIVE_MINUTES);
            logger.info("SeckillActivityCache|分布式缓存已经更新|{}", activityId);
            return seckillActivityCache;
            //将数据放入分布式缓存
        } catch (InterruptedException e) {
            logger.error("SeckillActivityCache|更新分布式缓存失败|{}", activityId);
            return new SeckillBusinessCache<SeckillActivity>().retryLater();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String buildCacheKey(Object key) {
        return StringUtil.append(SeckillConstants.SECKILL_ACTIVITY_CACHE_KEY,key);
    }
}
