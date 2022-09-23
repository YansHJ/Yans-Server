package com.example.server;


import com.example.server.start.InitServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {
	public static void main(String[] args) {
		InitServer server = new InitServer("localhost",7286);
		server.Server();
	}

}
