package cn.changyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author xgl
 * @create 2019-12-25 11:18
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ChangyouSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChangyouSearchApplication.class);
    }
}
