package com.kexon5.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableScheduling
@EnableWebFlux
@EnableMongoRepositories(basePackages = {"com.kexon5.bot.repositories", "com.kexon5.common.repositories"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DdBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdBotApplication.class, args);
    }

}
