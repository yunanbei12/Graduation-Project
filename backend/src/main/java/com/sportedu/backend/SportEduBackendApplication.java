package com.sportedu.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sportedu.backend.**.mapper")
public class SportEduBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportEduBackendApplication.class, args);
    }
}
