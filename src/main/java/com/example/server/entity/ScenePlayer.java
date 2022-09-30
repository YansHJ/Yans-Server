package com.example.server.entity;

import lombok.Data;

import java.util.Map;

@Data
public class ScenePlayer {

    /**
     * 大厅在线玩家
     */
    private Map<String,Player> hallPlayer;

    /**
     * 球场在线玩家
     */
    private Map<String,Player> footballFieldPlayer;
}
