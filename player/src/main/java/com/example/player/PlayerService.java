package com.example.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.player.base.GetPositionAndDuration;
import com.example.player.entity.Song;
import com.example.player.model.PlayerModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlayerService extends Service implements GetPositionAndDuration {

    public PlayerService() {
    }

    // 播放器类(让它成为静态方便在广播中调用)
    public static MediaPlayer player;
    // 用于播放设置的线程
    private static final HandlerThread handlerThread = new HandlerThread("handlerThread");
    // 此回调方法用于告知view层播放器是否准备完毕，同时更新进度条
    private CallBack callBack;
    // 持有设置线程loop，用于进行设置，要在该服务销毁时进行释放
    private Handler handler;
    // 存储歌单的集合
    public static List<Song> list;
    // 通知管理器
    private  NotificationManager notificationManager;
    // 通知的自定义view
    private  RemoteViews remoteViews;
    // 通知
    private  Notification notification;
    // 存储歌单位置
    public static int position = 0;
    // 用于网络请求的model层
    private PlayerModel playerModel;
    //播放逻辑
    public static int play_logic = ViewConstants.PLAY_REPEAT;
    // 判断activity是否存活
    public static boolean isLive = false;

    //两个存储随机数的集合，用于保存记录
    public static List<Integer> previousRandomList = new ArrayList<>();
    public static List<Integer> nextRandomList = new ArrayList<>();
    private MusicReceive musicReceive;
    private Random random;

    // 存储随机数的集合
    public static   List<Integer> randomList = new ArrayList<>();

    public static int randomListPosition = 0;



    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        playerModel = new PlayerModel();

        //广播管理器和广播的设置
        musicReceive = new MusicReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ViewConstants.MAIN_ACTIVITY_ACTION);
        intentFilter.addAction(ViewConstants.PAUSE_SONG_NOTIFICATION_ACTION);
        intentFilter.addAction(ViewConstants.LAST_SONG_NOTIFICATION_ACTION);
        intentFilter.addAction(ViewConstants.NEXT_SONG_NOTIFICATION_ACTION);
        registerReceiver(musicReceive, intentFilter);

        // 播放器的监听方法，判断是否准备完毕，准备完毕会调用前面的回调方法对view层进行通知
        player.setOnPreparedListener(mp -> {
            player = mp;
            player.start();
            if(callBack !=null){
                callBack.prepared();
            }
        });
        // 开启前台服务
        musicControlNotification();
        // 启动线程
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ViewConstants.PLAYING:
                        removeMessages(ViewConstants.PLAYING);
                        //获取总时长
                        int duration = player.getDuration();
                        //获取播放进度
                        int currentPosition = player.getCurrentPosition();
                        callBack.changeData(duration, currentPosition);
                        if (player.isPlaying()) {
                            sendEmptyMessageDelayed(ViewConstants.PLAYING, 100L);
                        }
                        player.setOnErrorListener((mp, what, extra) -> {
                            return true;
                        });
                        player.setOnCompletionListener(mp ->{
                            callBack.changeData(-1, -1);
                        });
                        break;
                    case ViewConstants.PAUSE:
                        player.pause();
                        break;
                    case ViewConstants.SEEK_TO:
                        int progress = (int) msg.obj;
                        player.seekTo(progress);
                        break;
                    case ViewConstants.CONTINUE:
                        player.start();
                        handler.sendEmptyMessage(ViewConstants.PLAYING);
                        break;
                    case ViewConstants.RESTART_PLAYING:
                        break;
                    case ViewConstants.PREPARED:
                        String url = (String) msg.obj;
                        player.reset();
                        try {
                            player.setDataSource(url);
                            player.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ViewConstants.NEXT_SONG:
                        String nextSongUrl = (String) msg.obj;
                        if (player.isPlaying()) {
                            player.stop();
                        }
                        player.reset();
                        try {
                            player.setDataSource(nextSongUrl);
                            player.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }


            }
        };
    }

    /**
     * v层调用，完成回调方法的实例以及传递歌单数据
     *
     * @param callBack 实例化回调方法，用于后面通知v层准备完毕，通知更新进度条
     * @param list 将歌单传给service
     */
    public void communicate(CallBack callBack, List<Song> list) {
        this.callBack = callBack;
        PlayerService.list = list;
    }

    /**
     * 由vm层调用，启动进度条
     */
    public void addTimer() {
        Message message = Message.obtain();
        message.what = ViewConstants.PLAYING;
        handler.sendMessage(message);
    }

    /**
     * 创建通知以及通知渠道并开启前台服务
     */
    public void musicControlNotification() {
        // 创建通知渠道
        createNotificationChannel();

        remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setImageViewResource(R.id.iv_clear, R.drawable.ic_clear);
        remoteViews.setImageViewResource(R.id.iv_last_song_notification, R.drawable.ic_last_song);
        remoteViews.setImageViewResource(R.id.iv_next_song_notification, R.drawable.ic_next_song);
        remoteViews.setImageViewResource(R.id.iv_pause_notification, R.drawable.ic_stop);

        // 跳转活动
//        Intent intent = new Intent(this,PlayerActivity.class);
        Intent intent = new Intent(ViewConstants.MAIN_ACTIVITY_ACTION);
        // 记录不同PendingIntent的版本号
        int versionPendingIntent = 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, versionPendingIntent, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_pic_album,pendingIntent);
        // 暂停播放操作
        Intent intentPause = new Intent(ViewConstants.PAUSE_SONG_NOTIFICATION_ACTION);
        intentPause.putExtra(ViewConstants.SONG_NOTIFICATION_INTENT_KEY,ViewConstants.PAUSE_SONG_NOTIFICATION_VALUE);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, versionPendingIntent, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_pause_notification, pendingIntentPause);

        //下一首操作
        Intent intentLastSong = new Intent(ViewConstants.NEXT_SONG_NOTIFICATION_ACTION);
        intentLastSong.putExtra(ViewConstants.SONG_NOTIFICATION_INTENT_KEY,ViewConstants.NEXT_SONG_NOTIFICATION_VALUE);
        PendingIntent pendingIntentLastSong = PendingIntent.getBroadcast(this, versionPendingIntent, intentLastSong, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_next_song_notification, pendingIntentLastSong);

        //上一首操作
        Intent intentNextSong = new Intent(ViewConstants.LAST_SONG_NOTIFICATION_ACTION);
        intentNextSong.putExtra(ViewConstants.SONG_NOTIFICATION_INTENT_KEY,ViewConstants.LAST_SONG_NOTIFICATION_VALUE);
        PendingIntent pendingIntentNextSong = PendingIntent.getBroadcast(this, versionPendingIntent, intentNextSong, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_last_song_notification, pendingIntentNextSong);

        //关闭前台服务的操作
        Intent clearForeground= new Intent(ViewConstants.CLEAR_FOREGROUND_NOTIFICATION_ACTION);
        PendingIntent pendingIntentClear = PendingIntent.getBroadcast(this, versionPendingIntent, clearForeground, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_clear, pendingIntentClear);

        notification = new NotificationCompat.Builder(this, ViewConstants.ORDINARY_NOTIFICATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_small_icon_music)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContent(remoteViews)
                .build();
        startForeground(ViewConstants.ID_ORDINARY_NOTIFICATION, notification);
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
            CharSequence name = ViewConstants.ORDINARY_NAME_NOTIFICATION;
            String description = ViewConstants.ORDINARY_DESCRIPTION_NOTIFICATION;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ViewConstants.ORDINARY_NOTIFICATION, name, importance);
            channel.setDescription(description);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(ViewConstants.ORDINARY_NOTIFICATION) == null) {
                notificationManager.createNotificationChannel(channel);
            }

    }

    @Override
    public int getPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    /**
     * 服务器的接收类，在此进行前台操作
     */
     private class MusicReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case ViewConstants.PAUSE_SONG_NOTIFICATION_ACTION:
                    if(player!=null){
                        if(player.isPlaying()){
                            pausePlay();
                            remoteViews.setImageViewResource(R.id.iv_pause_notification,R.drawable.ic_stop);
                            if(callBack!=null){
                                callBack.changePlayingStatus(false);
                            }
                        }else {
                            continuePlay();
                            remoteViews.setImageViewResource(R.id.iv_pause_notification,R.drawable.ic_continue);
                            if(callBack!=null){
                                callBack.changePlayingStatus(true);
                            }
                        }
                        notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION,notification);
                    }
                    break;
                case ViewConstants.NEXT_SONG_NOTIFICATION_ACTION:

                    // 获取播放逻辑进行判断
                    int play_logic = PlayerService.play_logic;
                    // 播放逻辑：顺序，单曲循环(这二者点击下一首都会跳转到列表下一首)
                    if (play_logic == ViewConstants.PLAY_REPEAT || play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                        //判断是否为最后一首
                        if (PlayerService.position == (list.size() - 1)) {
                            position = 0;
                        }else {
                            // 将当前列表位置加一
                            position++;
                        }
                        nextSong();
                        if(callBack!=null){
                            callBack.changeActivity(position);
                        }
                    }
                    // 播放逻辑：随机播放
                    else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                        if (random == null) {
                            random = new Random();
                        }
                        if(randomList!=null){
                            if(randomListPosition == randomList.size()-1){
                                randomListPosition = 0;
                            }else {
                                randomListPosition++;
                            }
                            position = randomList.get(randomListPosition);
                            nextSong();
                        }
                        if(callBack!=null){
                            callBack.changeActivity(position);
                        }
                    }
                    break;
                case ViewConstants.LAST_SONG_NOTIFICATION_ACTION:

                    // 判断播放逻辑
                    int play_logic2 = PlayerService.play_logic;
                    // 播放逻辑：顺序，单曲循环
                    if (play_logic2 == ViewConstants.PLAY_REPEAT || play_logic2 == ViewConstants.PLAY_REPEAT_ONCE) {
                        if (PlayerService.position == 0) {
                            PlayerService.position = (list.size() - 1);
                        }else {
                            //让当前位置自减1
                            position--;
                        }
                        nextSong();
                        if(callBack!=null){
                            callBack.changeActivity(position);
                        }
                    }
                    // 播放逻辑：随机播放
                    else if (play_logic2 == ViewConstants.PLAY_SHUFFLE) {
                        if (random == null) {
                            random = new Random();
                        }
                        if(randomList!=null){
                            if(randomListPosition==0){
                                randomListPosition =randomList.size()-1;
                            }else {
                                randomListPosition--;
                            }
                            position = randomList.get(randomListPosition);
                            nextSong();
                        }
                        if(callBack!=null){
                            callBack.changeActivity(position);
                        }
                    }
                    break;
                case ViewConstants.CLEAR_FOREGROUND_NOTIFICATION_ACTION:
                    stopForeground(true);
                    break;
                case ViewConstants.MAIN_ACTIVITY_ACTION:
                    if(isLive){
                        Toast.makeText(PlayerService.this.getApplicationContext(),"该页面正在显示",Toast.LENGTH_SHORT).show();
                    }else {
                        Intent intent1 = new Intent(PlayerService.this,PlayerActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);
                    }

            }
        }
    }

    /**
     * 播放操作，作为第一首歌开始播放的
     */
    public void play() {
        upData(position);
        playerModel.requestMusicUrl(list.get(position).getId(), new PlayerModel.Callback() {
            @Override
            public void getUrl(String url) {
                if (url.equals(ViewConstants.NULL)) {
                    url = ViewConstants.ID_SEARCH_PREFIX + list.get(position).getId();
                    Log.e(ViewConstants.TAG, "onCreate: 该歌曲为null");
                    Log.d(ViewConstants.TAG, "getUrl: "+Thread.currentThread().getName());
                }
                Message message = Message.obtain();
                message.what = ViewConstants.PREPARED;
                message.obj = url;
                handler.sendMessage(message);
            }

            @Override
            public void dispatch() {

            }
        });

    }

    /**
     * 暂停
     */
    public void pausePlay() {
        remoteViews.setImageViewResource(R.id.iv_pause_notification, R.drawable.ic_stop);
        notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION, notification);
        handler.sendEmptyMessage(ViewConstants.PAUSE);
    }

    /**
     * 继续播放（需要先暂停）
     */
    public void continuePlay() {
        remoteViews.setImageViewResource(R.id.iv_pause_notification, R.drawable.ic_continue);
        notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION, notification);
        handler.sendEmptyMessage(ViewConstants.CONTINUE);
    }

    /**
     * 跳转操作
     *
     * @param progress 进度
     */
    public void seekTo(int progress) {
        Message message = Message.obtain();
        message.obj = progress;
        message.what = ViewConstants.SEEK_TO;
        handler.sendMessage(message);
    }

    /**
     * 下一首
     */
    public void nextSong() {
        upData(position);
        playerModel.requestMusicUrl(list.get(position).getId(), new PlayerModel.Callback() {
            @Override
            public void getUrl(String url) {
                Message message = Message.obtain();
                message.what = ViewConstants.NEXT_SONG;
                message.obj = url;
                handler.sendMessage(message);
            }

            @Override
            public void dispatch() {

            }
        });

    }
    /**
     * 服务类返回的binder，进行歌曲操作
     */
    class MusicControl extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }

    }

    /**
     * 更新通知栏
     *
     * @param position 歌单位置
     */
    private void upData(int position) {
        remoteViews.setTextViewText(R.id.tv_notification_song_name, list.get(position).getName());
        StringBuilder author = new StringBuilder(" ");
        for (int i = 0; i < list.get(position).getArtists().size(); i++) {
            author.append(list.get(position).getArtists().get(i).getName()).append(" ");
        }
        remoteViews.setTextViewText(R.id.tv_notification_song_author, author);
        remoteViews.setImageViewResource(R.id.iv_pause_notification, R.drawable.ic_continue);
        Picasso.with(getApplicationContext()).load(list.get(position).getAlbum().getPicUrl() + ViewConstants.PARAM).into(remoteViews, R.id.iv_pic_album, ViewConstants.ID_ORDINARY_NOTIFICATION, notification);
        notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放播放器
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
        player.reset();
        // 关闭播放器设置线程
        handlerThread.quitSafely();
        // 初始化操作逻辑为顺序
        play_logic = ViewConstants.PLAY_REPEAT;
        // 释放两个存储随机数的列表
        nextRandomList = null;
        previousRandomList = null;
        // 将列表初始化
        list = null;
        // 初始化列表位置
        position = 0;
        // 注销广播接收期
        unregisterReceiver(musicReceive);
        //初始化随机数表
        randomListPosition = 0;
        randomList = new ArrayList<>();

    }

    /**
     * 回调方法，包括通知v层准备完毕，以及传递进度条数据
     */
    public interface CallBack {
        void prepared();

        void changeData(int duration, int currentPosition);

        void changeActivity(int position );

        void changePlayingStatus(boolean isPlaying);
    }


}