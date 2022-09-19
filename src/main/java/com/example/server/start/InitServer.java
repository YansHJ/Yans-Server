package com.example.server.start;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.server.utils.TimeUtils;
import com.example.server.utils.UuidUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitServer {
    int a = 0;
    public void Server(){
        try {
            //客户端列表
            Map<String,String> clientMap= new HashMap<>();

            Configuration config = new Configuration();
            config.setHostname("localhost");
            config.setPort(8080);
            SocketIOServer server = new SocketIOServer(config);

//            客户端进入
            server.addConnectListener(new ConnectListener() {
                @Override
                public void onConnect(SocketIOClient socketIOClient) {
                    try {
                        String ip = socketIOClient.getRemoteAddress().toString().split(":")[0];
                        String now = TimeUtils.getNow();
                        String clientId = UuidUtils.uuid16();
                        System.out.println("|| ==========  " + now + ":   来自 " + ip + "的客户端进入: " + a++  + "  =========== ||");
                        System.out.println("|| ==========  客户端id: " + clientId + "  =========== ||");
                        Thread.sleep(500);
                        socketIOClient.sendEvent("getClientId",clientId);
                        clientMap.put(clientId,ip);
                        server.getBroadcastOperations().sendEvent("newConnect",clientId);
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }
            });

            //客户端进入，获取客户端id
            server.addEventListener("newConnect", String.class, new DataListener<String>() {
                @Override
                public void onData(SocketIOClient socketIOClient, String id, AckRequest ackRequest) throws Exception {
                    String ip = socketIOClient.getRemoteAddress().toString().split(":")[0];
                    clientMap.put(id,ip);
                    List<String> clientList = new ArrayList<>();
                    for (Map.Entry<String, String> entry : clientMap.entrySet()) {
                        clientList.add(entry.getKey());
                    }
                    System.out.println("已收到客户端加入请求，正在发送 newConnect 请求，客户端id为：" + id);
                    Thread.sleep(1000);
                    socketIOClient.sendEvent("initMe" + id,clientList);
                    server.getBroadcastOperations().sendEvent("newConnect",id);
                }
            });
            server.addEventListener("newConnectCompleted", Boolean.class, new DataListener<Boolean>() {
                @Override
                public void onData(SocketIOClient socketIOClient, Boolean aBoolean, AckRequest ackRequest) throws Exception {
                    System.out.println("收到newConnectCompleted，客户端已成功创建角色");
                }
            });


            //添加客户端断开连接事件
            server.addDisconnectListener(new DisconnectListener(){
                public void onDisconnect(SocketIOClient client) {
                    String ip = client.getRemoteAddress().toString().split(":")[0];
                    String now = TimeUtils.getNow();
                    a--;
                    System.out.println(now + ":   来自 " + ip + "的客户端离开 : " + a);
                }
            });
            server.addEventListener("playerDisconnect", String.class, new DataListener<String>() {
                @Override
                public void onData(SocketIOClient socketIOClient, String clientId, AckRequest ackRequest) throws Exception {
                    clientMap.remove(clientId);
                }
            });

            server.start();

            while (true){
                try {
                    Thread.sleep(2000);
                    List<String> clientList = new ArrayList<>();
                    for (Map.Entry<String, String> entry : clientMap.entrySet()) {
                        clientList.add(entry.getKey());
                    }
                    //广播消息
                    server.getBroadcastOperations().sendEvent("onlinePlayers",clientList);
//                    System.out.println(clients.entrySet().size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
