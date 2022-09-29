package com.example.server;

import com.example.server.enums.SceneEnums;
import lombok.Data;

@Data
public class Player {

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 坐标X
     */
    private int xx;

    /**
     * 坐标Y
     */
    private int yy;

    /**
     * 当前所在场景
     */
    private int scene;

    /**
     * 连接时间
     */
    private String connectTime;
}
