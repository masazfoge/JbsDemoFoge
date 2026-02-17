package com.dynademo;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.dynademo")
public class DynaWebConfig implements WebMvcConfigurer {

	@PostConstruct
	public void init() {
	  System.out.println("AppConfig loaded!");
	}	
}
