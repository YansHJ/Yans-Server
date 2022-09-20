package com.example.server;

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
     * 连接时间
     */
    private String connectTime;
}
