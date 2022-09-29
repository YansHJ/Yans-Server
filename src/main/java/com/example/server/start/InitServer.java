package com.example.server.start;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.server.Player;
import com.example.server.entity.ScenePlayer;
import com.example.server.enums.EventEnums;
import com.example.server.enums.SceneEnums;
import com.example.server.processor.HallProcessor;
import com.example.server.utils.TimeUtils;
import com.example.server.utils.UuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 各房间在线玩家列表
     */
    private final ScenePlayer scenePlayer = new ScenePlayer();
    int a = 0;

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
    /**
     * 客户端列表
     */
    Map<String,Player> clientMap = new HashMap<>();


    public void Server(){
        initScenePlayers();

        try {

            Configuration config = new Configuration();
            config.setHostname(hostName);
            config.setPort(port);
            SocketIOServer server = new SocketIOServer(config);
           //客户端进入大厅
            server.addConnectListener(new ConnectListener() {
                @Override
                public void onConnect(SocketIOClient socketIOClient) {
                    try {
                        //来源ip
                        String ip = socketIOClient.getRemoteAddress().toString().split(":")[0];
                        String now = TimeUtils.getNow();
                        //客户端ID生成
                        String clientId = socketIOClient.getSessionId().toString().replace("-","");
                        logger.info("|| ==========  " + now + ":   Client from " + ip + " comes in: " + ++a  + "  =========== ||");
                        logger.info("|| ==========  ClientId : " + clientId + "  =========== ||");
                        //告知客户端分配的clientId
                        socketIOClient.sendEvent(EventEnums.GET_CLIENT_ID.getName(),clientId);
                        //初始化玩家并添加到在线列表
                        Player player = new Player();
                        player.setClientId(clientId);
                        player.setConnectTime(now);
                        player.setXx(1366/2);
                        player.setYy(768/2);
                        player.setScene(SceneEnums.HALL.getSceneNo());
                        //更新大厅玩家列表
                        Map<String, Player> hallPlayer = scenePlayer.getHallPlayer();
                        hallPlayer.put(clientId,player);
                        //更新玩家列表
                        clientMap.put(clientId,player);
                    }catch (Exception e){
                        logger.error(" |||||||||||||||||||||||||||||    " + e + "    |||||||||||||||||||||||||||||    ");
                    }
                }
            });

            //监听玩家进入房间
            server.addEventListener("joinScene", Player.class, new DataListener<Player>() {
                @Override
                public void onData(SocketIOClient socketIOClient, Player player, AckRequest ackRequest) throws Exception {
                    switch (player.getScene()){
                        case 1 :new HallProcessor(socketIOClient,server,scenePlayer).join(player);break;
                    }
                }
            });

            //监听客户端人物移动
            server.addEventListener(EventEnums.I_MOVED.getName(), Player.class, new DataListener<Player>() {
                @Override
                public void onData(SocketIOClient socketIOClient, Player player, AckRequest ackRequest) throws Exception {
                    switch (player.getScene()){
                        case 1 :new HallProcessor(socketIOClient,server,scenePlayer).moved(player);break;
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
                    Player player = clientMap.get(clientId);
                    clientMap.remove(clientId);
                    logger.info("|| ==========  " + now + ":   Client from " + ip + " login out: " + ++a  + "  =========== ||");
                    logger.info("|| ==========  ClientId : " + clientId + "  =========== ||");
                    switch (player.getScene()){
                        case 1 :new HallProcessor(null,server,scenePlayer).leveled(player);break;
                    }
                }
            });

            //启动服务
            server.start();

        }catch (Exception e){
            logger.error(" |||||||||||||||||||||||||||||    " + e + "    |||||||||||||||||||||||||||||    ");
        }
    }

    private void initScenePlayers(){
        scenePlayer.setHallPlayer(new HashMap<>());
    }
}
