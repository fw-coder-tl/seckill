package io.binghe.seckill.application.cache.service.activity.impl;

import com.alibaba.fastjson.JSON;
import io.binghe.seckill.application.builder.SeckillActivityBuilder;
import io.binghe.seckill.application.cache.model.SeckillBusinessCache;
import io.binghe.seckill.application.cache.service.activity.SeckillActivityListCacheService;
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillActivityListCacheServiceImpl implements SeckillActivityListCacheService {

    private final static Logger logger = LoggerFactory.getLogger(SeckillActivityListCacheServiceImpl.class);

    @Autowired
    private LocalCacheService<Long, SeckillBusinessCache<List<SeckillActivity>>> localCacheService;

    // 分布式锁key
    private static final String SECKILL_ACTIVITES_UPDATE_CACHE_LOCK_KEY = "SECKILL_ACTIVITIES_UPDATE_CACHE_LOCK_KEY_";
    //本地锁
    private final Lock localCacheUpdatelock = new ReentrantLock();

    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private SeckillActivityRepository seckillActivityRepository;
    @Autowired
    private DistributedLockFactory distributedLockFactory;

    @Override
    public SeckillBusinessCache<List<SeckillActivity>> getCachedActivities(Integer status, Long version) {
        // 获取本地缓存
        SeckillBusinessCache<List<SeckillActivity>> seckillActivitiyListCache = localCacheService.getIfPresent(status);
        if (seckillActivitiyListCache != null) {
            if (version == null) {
                logger.info("SeckillActivityCache|命中本地缓存|{}", status);
                return seckillActivitiyListCache;
            }
            // 传递过来的版本号小于或等于缓存中的版本号
            if (version.compareTo(seckillActivitiyListCache.getVersion()) <= 0) {
                logger.info("SeckillActivityCache|命中本地缓存|{}", status);
                return seckillActivitiyListCache;
            }
            // 返回本地缓存
            if (version.compareTo(seckillActivitiyListCache.getVersion()) > 0) {
                return getDistributedCache(status);
            }
        }
        return getDistributedCache(status);
    }

    /**
     * 获取分布式缓存里面的数据
     */
    private SeckillBusinessCache<List<SeckillActivity>> getDistributedCache(Integer status) {
        logger.info("SeckillActivitesCache|读取分布式缓存|{}", status);
        SeckillBusinessCache<List<SeckillActivity>> seckillActivitiyListCache = SeckillActivityBuilder.getSeckillBusinessCacheList(distributedCacheService.getObject(buildCacheKey(status)), SeckillActivity.class);
        if (seckillActivitiyListCache == null) {
            tryUpdateSeckillActivityCacheByLock(status);
        }
        if (seckillActivitiyListCache != null && !seckillActivitiyListCache.isRetryLater()) {
            if (localCacheUpdatelock.tryLock()) {
                try {
                    localCacheService.put(status.longValue(), seckillActivitiyListCache);
                    logger.info("SeckillActivitesCache|本地缓存已经更新|{}", status);
                } finally {
                    localCacheUpdatelock.unlock();
                }
            }
        }
        return seckillActivitiyListCache;
    }

    @Override
    public SeckillBusinessCache<List<SeckillActivity>> tryUpdateSeckillActivityCacheByLock(Integer status) {
        logger.info("SeckillActivitesCache|更新分布式缓存|{}", status);
        DistributedLock lock = distributedLockFactory.getDistributedLock(SECKILL_ACTIVITES_UPDATE_CACHE_LOCK_KEY.concat(String.valueOf(status)));
        try {
            boolean isLockSuccess = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLockSuccess) {
                return new SeckillBusinessCache<List<SeckillActivity>>().retryLater();
            }
            //获取锁成功后，再次从缓存中获取数据，防止高并发下多个线程争抢锁的过程中，后续的线程在等待1秒的过程中，前面的线程释放了锁，后续的线程获取锁成功后再次更新分布式缓存数据
            SeckillBusinessCache<List<SeckillActivity>> seckillActivitiyListCache = SeckillActivityBuilder.getSeckillBusinessCacheList(distributedCacheService.getObject(buildCacheKey(status)),  SeckillActivity.class);
            if (seckillActivitiyListCache != null){
                return seckillActivitiyListCache;
            }
            // 查询数据库
            List<SeckillActivity> seckillActivityList = seckillActivityRepository.getSeckillActivityList(status);
            if(seckillActivityList==null){
                seckillActivitiyListCache = new SeckillBusinessCache<List<SeckillActivity>>().notExist();
            } else {
                seckillActivitiyListCache = new SeckillBusinessCache<List<SeckillActivity>>().with(seckillActivityList).withVersion(SystemClock.millisClock().now());
            }
            distributedCacheService.put(buildCacheKey(status), JSON.toJSONString(seckillActivitiyListCache), SeckillConstants.FIVE_MINUTES);
            logger.info("SeckillActivitesCache|分布式缓存已经更新|{}", status);
            return seckillActivitiyListCache;
        } catch (InterruptedException e) {
            logger.info("SeckillActivitesCache|更新分布式缓存失败|{}", status);
            return new SeckillBusinessCache<List<SeckillActivity>>().retryLater();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String buildCacheKey(Object key) {
        return StringUtil.append(SECKILL_ACTIVITES_UPDATE_CACHE_LOCK_KEY, key);
    }
}
