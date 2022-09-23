package com.example.server.start;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.server.Player;
import com.example.server.utils.TimeUtils;
import com.example.server.utils.UuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Async("asyncServerExecutor")
public class InitServer {
    public InitServer(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    int a = 0;

    /**
     * 在线用户列表
     */
    public List<Player> onlinePlayers;
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(InitServer.class);
    /**
     * 服务器地址
     */

    private final String hostName;
    /**
     * 服务器端口
     */
    private final int port;


    public void Server(){
        try {
            //客户端列表
            Map<String,String> clientMap = new HashMap<>();
            Configuration config = new Configuration();
            config.setHostname(hostName);
            config.setPort(port);
            SocketIOServer server = new SocketIOServer(config);
            onlinePlayers = new ArrayList<>();
           //客户端进入
            server.addConnectListener(new ConnectListener() {
                @Override
                public void onConnect(SocketIOClient socketIOClient) {
                    try {
                        //告知客户端当前在线的玩家信息
                        socketIOClient.sendEvent("initOnlinePlayers",onlinePlayers);
                        //来源ip
                        String ip = socketIOClient.getRemoteAddress().toString().split(":")[0];
                        String now = TimeUtils.getNow();
                        //客户端ID生成
                        String clientId = socketIOClient.getSessionId().toString().replace("-","");
                        logger.info("|| ==========  " + now + ":   Client from " + ip + " comes in: " + ++a  + "  =========== ||");
                        logger.info("|| ==========  ClientId : " + clientId + "  =========== ||");
                        //告知客户端分配的clientId
                        socketIOClient.sendEvent("getClientId",clientId);
                        clientMap.put(clientId,ip);
                        //初始化玩家并添加到在线列表
                        Player player = new Player();
                        player.setClientId(clientId);
                        player.setConnectTime(now);
                        player.setXx(1366/2);
                        player.setYy(768/2);
                        onlinePlayers.add(player);
                        //向所有人广播新加入的用户
                        server.getBroadcastOperations().sendEvent("newConnect",clientId);
                        //广播当前在线用户
                        server.getBroadcastOperations().sendEvent("onlinePlayers",onlinePlayers);
                    }catch (Exception e){
                        logger.error(" |||||||||||||||||||||||||||||    " + e + "    |||||||||||||||||||||||||||||    ");
                    }
                }
            });

            //监听客户端人物移动
            server.addEventListener("IMoved", Player.class, new DataListener<Player>() {
                @Override
                public void onData(SocketIOClient socketIOClient, Player player, AckRequest ackRequest) throws Exception {
                    //向所有客户端广播玩家移动的消息
                    server.getBroadcastOperations().sendEvent("someoneMoved",player);
                    for (Player onlinePlayer : onlinePlayers) {
                        if (onlinePlayer.getClientId().equals(player.getClientId())){
                            onlinePlayer.setXx(player.getXx());
                            onlinePlayer.setYy(player.getYy());
                        }
                    }
                }
            });



            //添加客户端断开连接事件
            server.addDisconnectListener(new DisconnectListener(){
                @Override
                public void onDisconnect(SocketIOClient client) {
                    String ip = client.getRemoteAddress().toString().split(":")[0];
                    String now = TimeUtils.getNow();
                    String clientId = client.getSessionId().toString().replace("-","");
                    logger.info("|| ==========  " + now + ":   Client from " + ip + " login out: " + ++a  + "  =========== ||");
                    logger.info("|| ==========  ClientId : " + clientId + "  =========== ||");
                    for (int i = 0; i < onlinePlayers.size(); i++) {
                        if (onlinePlayers.get(i).getClientId().equals(clientId)){
                            onlinePlayers.remove(i);
                            logger.info("onlineSize : " + onlinePlayers.size());
                        }
                    }
                    //广播客户端谁离开了
                    server.getBroadcastOperations().sendEvent("someoneLeveled",clientId);
                    //广播客户端当前在线的玩家信息
                    server.getBroadcastOperations().sendEvent("onlinePlayers",onlinePlayers);
                }
            });

            //启动服务
            server.start();

        }catch (Exception e){
            logger.error(" |||||||||||||||||||||||||||||    " + e + "    |||||||||||||||||||||||||||||    ");
        }
    }
}
