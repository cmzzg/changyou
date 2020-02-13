package cn.changyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author GM
 * @create 2019-12-07 18:44
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ChangyouDealApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChangyouDealApplication.class);
    }
}
