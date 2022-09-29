package com.example.server.entity;

import com.example.server.Player;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ScenePlayer {

    /**
     * 大厅在线玩家
     */
    private Map<String,Player> hallPlayer;
}
