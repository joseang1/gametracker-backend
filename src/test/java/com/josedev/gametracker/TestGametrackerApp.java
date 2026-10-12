package com.josedev.gametracker;

import org.springframework.boot.SpringApplication;

public class TestGametrackerApp {

	public static void main(String[] args) {
		SpringApplication.from(GametrackerApp::main).with(TestcontainersConfiguration.class).run(args);
	}

}
