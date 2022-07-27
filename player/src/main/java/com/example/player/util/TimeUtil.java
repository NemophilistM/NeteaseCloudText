package com.example.player.util;

public class TimeUtil {
    public static String time(int duration){
        int minute = duration/1000/60;
        int second= duration/1000%60;
        String stringMinute = null;
        String stringSecond = null;
        if(minute<10){
            stringMinute = "0"+minute;
        }else {
            stringMinute=minute+"";
        }
        if(second<10){
            stringSecond ="0"+second;
        }else {
            stringSecond = second+"";
        }
        return stringMinute+":"+stringSecond;
    }
}
