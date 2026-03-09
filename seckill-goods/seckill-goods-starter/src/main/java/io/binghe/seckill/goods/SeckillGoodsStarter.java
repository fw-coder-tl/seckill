package io.binghe.seckill.goods;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableDubbo
@SpringBootApplication
public class SeckillGoodsStarter {

    public static void main(String[] args) {
        System.setProperty("user.home", "/home/binghe/goods");
        SpringApplication.run(SeckillGoodsStarter.class, args);
    }
}
