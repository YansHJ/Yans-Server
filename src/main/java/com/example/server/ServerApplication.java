package com.example.server;


import com.example.server.start.InitServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {
	public static void main(String[] args) {
		//部署在服务器上
		InitServer server = new InitServer("0.0.0.0",7286);
		//部署在本地
//		InitServer server = new InitServer("localhost",7286);
		server.Server();
	}

}
