package com.example.server.processor;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.example.server.entity.Player;
import com.example.server.entity.ScenePlayer;
import com.example.server.enums.EventEnums;
import com.example.server.enums.SceneEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 大厅处理器，监听事件
 */
public class HallProcessor {

    /**
     * 日志
     */
    Logger logger = LoggerFactory.getLogger(HallProcessor.class);
    /**
     * socket客户端
     */
    private final SocketIOClient socketIOClient;
    /**
     * socket服务器
     */
    private final SocketIOServer server;
    /**
     * 全局在线玩家
     */
    private final ScenePlayer scenePlayer;
    /**
     * 当前场景名称
     */
    private final static String SCENE_NAME = SceneEnums.HALL.getSceneName();
    public HallProcessor(SocketIOClient socketIOClient,SocketIOServer server,ScenePlayer scenePlayer) {
        this.socketIOClient = socketIOClient;
        this.server = server;
        this.scenePlayer = scenePlayer;
    }

    /**
     * 加入房间
     */
    public void join(Player player){
        scenePlayer.getHallPlayer().put(player.getClientId(),player);
        List<Player> players = new ArrayList<>(scenePlayer.getHallPlayer().values());
        try {
            //告知客户端当前场景在线列表
            for (int i = 0; i < 20; i++) {
                socketIOClient.sendEvent(EventEnums.ONLINE_PLAYERS.getName() + "_" + SCENE_NAME ,players);
                Thread.sleep(50);
            }
            //向所有人广播新加入的用户
            server.getBroadcastOperations().sendEvent(EventEnums.NEW_CONNECT.getName() + "_" + SCENE_NAME,player.getClientId());
            //广播当前在线用户
            server.getBroadcastOperations().sendEvent(EventEnums.ONLINE_PLAYERS.getName() + "_" + SCENE_NAME,players);
            System.out.println(players.toString());
        }catch (Exception e){
            logger.error(String.valueOf(e));
        }
    }

    /**
     * 更新移动
     */
    public void moved(Player player){
        //更新服务器玩家坐标
        scenePlayer.getHallPlayer().put(player.getClientId(),player);
        //向所有客户端广播玩家移动的消息
        server.getBroadcastOperations().sendEvent(EventEnums.SOMEONE_MOVED.getName()+ "_" + SCENE_NAME,player);
    }


    /**
     * 有玩家离开
     */
    public void leveled(Player player){
        String clientId = player.getClientId();
        scenePlayer.getHallPlayer().remove(clientId);
        List<Player> players = new ArrayList<>(scenePlayer.getHallPlayer().values());
        if (player.getNowScene() != player.getNextScene()){
            //广播前往其他房间
            server.getBroadcastOperations().sendEvent(EventEnums.SOMEONE_LEVEL_ROOM.getName() + "_" + SCENE_NAME,clientId);
        }else {
            //广播客户端谁离开了
            server.getBroadcastOperations().sendEvent(EventEnums.SOMEONE_LEVELED.getName() + "_" + SCENE_NAME,clientId);
        }
        //广播客户端当前在线的玩家信息
        server.getBroadcastOperations().sendEvent(EventEnums.ONLINE_PLAYERS.getName() + "_" + SCENE_NAME,players);
    }
}
