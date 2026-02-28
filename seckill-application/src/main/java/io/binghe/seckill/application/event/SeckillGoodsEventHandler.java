package io.binghe.seckill.application.event;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.event.EventHandler;
import com.alibaba.cola.event.EventHandlerI;
import com.alibaba.fastjson.JSON;
import io.binghe.seckill.application.cache.service.goods.SeckillGoodsCacheService;
import io.binghe.seckill.application.cache.service.goods.SeckillGoodsListCacheService;
import io.binghe.seckill.domain.event.SeckillActivityEvent;
import io.binghe.seckill.domain.event.SeckillGoodsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;

@EventHandler
public class SeckillGoodsEventHandler implements EventHandlerI<Response, SeckillGoodsEvent> {
    private final Logger logger = LoggerFactory.getLogger(SeckillGoodsEventHandler.class);

    @Autowired
    private SeckillGoodsCacheService seckillGoodsCacheService;
    @Autowired
    private SeckillGoodsListCacheService seckillGoodsListCacheService;

    @Override
    public Response execute(SeckillGoodsEvent seckillGoodsEvent) {
        logger.info("goodsEvent|接收秒杀品事件|{}", JSON.toJSON(seckillGoodsEvent));
        if (seckillGoodsEvent.getId() == null){
            logger.info("goodsEvent|接收秒杀品事件参数错误");
            return Response.buildSuccess();
        }
        seckillGoodsCacheService.tryUpdateSeckillGoodsCacheByLock(seckillGoodsEvent.getId(),false);
        seckillGoodsListCacheService.tryUpdateSeckillGoodsCacheByLock(seckillGoodsEvent.getActivityId(),false);
        return Response.buildSuccess();
    }
}
