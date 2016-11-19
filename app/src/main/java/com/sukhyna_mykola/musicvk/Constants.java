package com.sukhyna_mykola.musicvk;

import android.util.Log;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import static com.sukhyna_mykola.musicvk.SoundInfoFragment.Tag;

/**
 * Created by mikola on 25.10.2016.
 */

public class Constants {
    public static final String PERFORMER_ONLY = "performer_only";
    public final static String DB_NAME = "db_sounds_help";
    public final static String TABLE_NAME = "table_sounds";
    public static final HashMap<Integer, String> genre = new HashMap<Integer, String>() {{
        put(1, "Rock");
        put(2, "Pop");
        put(3, "Rap & Hip-Hop");
        put(4, "Easy Listening");
        put(5, "House & DanceRock");

        put(6, "Instrumental");
        put(7, "Metal");
        put(8, "Dubstep");
        put(10, "Drum & Bass");

        put(11, "Trance");
        put(12, "Chanson");
        put(13, "Ethnic");
        put(14, "Acoustic & Vocal");
        put(15, " Reggae");

        put(16, "Classical");
        put(17, "Indie Pop");
        put(18, "Other");
        put(19, "Speech");

        put(21, "Alternative");
        put(22, "Electropop & Disco");

        put(1001, "Jazz & Blues");

    }};

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
        if (hours != 0)
            buf.append(String.format("%02d", hours))
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

    public static String SizeFile(String urlS) {
        String res;
        URL url;
        HttpURLConnection conetion;
        try {
            url = new URL(urlS);
            Log.d(Tag,"pre");
            conetion = (HttpURLConnection) url.openConnection();
            Log.d(Tag,"after");
            conetion.connect();
            Log.d(Tag,"connect");
            int size = conetion.getContentLength();
            Log.d(Tag,"size = "+size);
            res = Constants.getSizeFileMB(size);
            conetion.disconnect();
            Log.d(Tag,"disconnect");
        } catch (Exception e) {
            res = " ";
        } finally {

        }
        return res;
    }


}