package com.sukhyna_mykola.musicvk;

import java.net.URL;
import java.net.URLConnection;

/**
 * Created by mikola on 25.10.2016.
 */

public class Constants {
    public static final String PERFORMER_ONLY = "performer_only";
    public final static String DB_NAME= "db_sounds_help";
    public final static String TABLE_NAME= "table_sounds";
    public interface ACTION {
        
        public static String PREV_ACTION = "com.sukhyna_mykola.vkmusic.prev";
        public static String PLAY_ACTION = "com.sukhyna_mykola.vkmusic.play";
        public static String NEXT_ACTION = "com.sukhyna_mykola.vkmusic.next";
        public static String STARTFOREGROUND_ACTION = "com.sukhyna_mykola.vkmusic.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.sukhyna_mykola.vkmusic.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
       if(hours!=0)
        buf     .append(String.format("%02d", hours))
                .append(":");
        buf
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }
    public static String getSizeFileMB(double lenght) {
        double fileSizeInMB = lenght / (1024.0 * 1024.0);
        if (String.valueOf(fileSizeInMB).length() >= 6)
            return String.valueOf(fileSizeInMB).substring(0, 4);
        else return String.valueOf(fileSizeInMB);
    }

    public static String SizeFile(String urlS){

        URL url;
        URLConnection conetion;

        try {
            url = new URL(urlS);
            conetion = url.openConnection();
            conetion.connect();
            return Constants.getSizeFileMB(conetion.getContentLength());
        } catch (Exception e) {
            return " ";
        }

    }

}