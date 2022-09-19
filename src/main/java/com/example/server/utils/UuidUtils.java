package com.example.server.utils;


import java.util.UUID;

public class UuidUtils {

    public static String uuid16(){
        return UUID.randomUUID().toString().replace("-","").substring(5,21);
    }
}
