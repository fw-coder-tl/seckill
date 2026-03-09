package io.binghe.seckill.user.infrastructure.mapper;

import io.binghe.seckill.user.domain.model.entity.SeckillUser;
import org.apache.ibatis.annotations.Param;
public interface SeckillUserMapper {

    /**
     * 根据用户名获取用户信息
     */
    SeckillUser getSeckillUserByUserName(@Param("userName") String userName);
}
