package com.quantumsyntax;

import org.springframework.boot.SpringApplication;

public class TestQuantumSyntaxApplication {

	public static void main(String[] args) {
		SpringApplication.from(QuantumSyntaxApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
