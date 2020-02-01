package cn.changyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("cn.changyou.order.mapper")
public class ChangyouOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChangyouOrderApplication.class, args);
    }
}
