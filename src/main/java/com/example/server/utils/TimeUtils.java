package com.example.server.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TimeUtils {

    public static String getNow(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-hh HH:mm:ss");
        Date date = new Date();
        return format.format(date);
    }

}
