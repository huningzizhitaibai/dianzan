package com.huning.dianzan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huning.dianzan.mapper")
public class DianzanApplication {

    public static void main(String[] args) {
        SpringApplication.run(DianzanApplication.class, args);
    }

}
