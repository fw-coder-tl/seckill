package io.binghe.seckill.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableDubbo
@SpringBootApplication
public class SeckillOrderStarter {
    public static void main(String[] args) {
        System.setProperty("user.home", "/home/binghe/order");
        SpringApplication.run(SeckillOrderStarter.class, args);
    }
}
