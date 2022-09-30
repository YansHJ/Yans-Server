package com.example.server.enums;

import lombok.Getter;

/**
 * 场景房间
 */
public enum SceneEnums {

    HALL("HALL",1),
    FOOTBALL_FIELD("FOOTBALLFIELD",2);

    @Getter
    private final String sceneName;
    @Getter
    private final int sceneNo;

    SceneEnums(String sceneName, int sceneNo){
        this.sceneName = sceneName;
        this.sceneNo = sceneNo;
    }


}
