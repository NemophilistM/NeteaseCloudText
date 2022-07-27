package com.example.player;

public class ViewConstants {

    public static String DURATION = "duration";
    public static String CURRENT_POSITION = "currentPosition";
    public static String NAME = "name";
    public static String URL = "url";
    public static String ID_SEARCH_PREFIX = " https://music.163.com/song/media/outer/url?id=";
    public static String ID_DOWN_LOAD = "https://netease-cloud-music-api-4eodv9lwk-tangan91314.vercel.app/song/download/url";
    public static String GET_SONG_URL = " https://netease-cloud-music-api-4eodv9lwk-tangan91314.vercel.app/song/url";
    public static String ID_SEARCH_SUFFIX = ".mp3";
    public static String SONG_LIST = "songList";
    public static String TIMESTAMP = "timestamp";
    public static String ID = "id";
    public static final int NOTIFICATION_DOWNLOAD_PROGRESS = 1;
    public static int SONG_END = 10;
    public static String  PARAM = "?param=100y100" ;
    public static  String POSITION = "position";
    public static  String URL_NULL = "URL_NULL";
    public static  String NULL = "null";
    public static  String TAG = "JM";
    public static  String ACTION_NOTIFICATION = "actionNotification";



    //播放状态
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final int RESTART_PLAYING = 3;
    public static final int CONTINUE = 2;
    public static final int PREPARED = 4;
    public static final int SEEK_TO = 5;
    public static final int STOP = 6;
    public static final int NEXT_SONG = 7;
    public static final int NO_SONG = 8;


    //json
    public static String DATA_JSON = "data";
    public static String ID_JSON = "id";
    public static String URL_JSON = "url";


    //线程名称
    public static String HANDLER_THREAD_NET = "netHandlerThread";

    //通知渠道
    public static final String  ORDINARY_NOTIFICATION = "110";
    public static final int   ID_ORDINARY_NOTIFICATION = 110;
    public static final CharSequence ORDINARY_NAME_NOTIFICATION = "ordinaryChannel";
    public static final String ORDINARY_DESCRIPTION_NOTIFICATION = "A Normal Channel";

    //通知操作按钮主题
    public static final String  LAST_SONG_NOTIFICATION_ACTION = "previous";
    public static final String  PAUSE_SONG_NOTIFICATION_ACTION = "pause";
    public static final String  NEXT_SONG_NOTIFICATION_ACTION = "next";
    public static final String  PLAY_SONG_NOTIFICATION_ACTION = "play";

    //播放逻辑
    public static final int  PLAY_REPEAT = 0;
    public static final int  PLAY_REPEAT_ONCE = 1;
    public static final int  PLAY_SHUFFLE = 2;






}
