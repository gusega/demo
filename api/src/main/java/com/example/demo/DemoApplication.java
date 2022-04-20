package com.example.demo;

import com.example.demo.DemoApplication.DriverProperties;
import com.example.demo.resources.Products;
import com.example.demo.resources.Sell;
import org.glassfish.jersey.server.ResourceConfig;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(DriverProperties.class)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public ResourceConfig jerseyResourceConfig(){
		ResourceConfig config = new ResourceConfig();
		config.register(Products.class);
		config.register(Sell.class);
		return config;
	}

	@Bean
	public Driver neo4jDriver(DriverProperties driverProperties) {
		return GraphDatabase.driver( driverProperties.url(), AuthTokens.basic( driverProperties.user(),
				driverProperties.password() ) );
	}

	@ConstructorBinding
	@ConfigurationProperties("neo4j")
	record DriverProperties(String url, String user, String password){}
}
