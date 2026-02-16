package br.com.gustavopomponi;

import org.springframework.boot.SpringApplication;

public class TestGustavopomponiApplication {

	public static void main(String[] args) {
		SpringApplication.from(GustavopomponiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
