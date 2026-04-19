package com.zero;

import com.zero.config.AppEnvProperties;
import com.zero.config.CorsProperties;
import com.zero.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.zero.mapper")
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class, AppEnvProperties.class})
public class ZeroApplication {

  public static void main(String[] args) {
    SpringApplication.run(ZeroApplication.class, args);
  }
}
