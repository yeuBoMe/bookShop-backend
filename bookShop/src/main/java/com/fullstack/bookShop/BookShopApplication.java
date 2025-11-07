package com.fullstack.bookShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@SpringBootApplication(
//        exclude = {
//                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
//                org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
//        }
//)
public class BookShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookShopApplication.class, args);
	}

}
