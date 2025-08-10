package com.listo.eskalink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EskalinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(EskalinkApplication.class, args);
	}

}
