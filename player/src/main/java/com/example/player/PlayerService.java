package com.example.player;

import android.annotation.SuppressLint;
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
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.player.base.GetPositionAndDuration;
import com.example.player.entity.Song;
import com.example.player.model.PlayerModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;


public class PlayerService extends Service implements GetPositionAndDuration {

    public PlayerService() {
    }

    public static MediaPlayer player;
    private static final HandlerThread handlerThread = new HandlerThread("handlerThread");
    private CallBack callBack;
    private static Handler handler;
    private List<Song> list;
     NotificationManager notificationManager;
     RemoteViews remoteViews;
     Notification notification;
     public static int position;
     private PlayerModel playerModel;
     private int versionPendingIntent = 0;
     //播放逻辑
    public  static int play_logic  = ViewConstants.PLAY_REPEAT;
     private LocalBroadcastManager localBroadcastManager;
     private MusicReceive musicReceive;
     private IntentFilter intentFilter;


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
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        musicReceive = new MusicReceive();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ViewConstants.ACTION_NOTIFICATION);
        localBroadcastManager.registerReceiver(musicReceive,intentFilter);
        player.setOnPreparedListener(mp -> {
            player = mp;
            player.start();
            callBack.prepared();
        });
        musicControlNotification();
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
                        player.setOnErrorListener((mp,what,extra)->{
                            return true;
                        });
                        player.setOnCompletionListener(mp->{
                            callBack.changeData(-1,-1);
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
                    case ViewConstants.NO_SONG:
                        player.stop();
                        break;
                }


            }
        };
    }
//    private Handler handlerMain = new Handler(Looper.getMainLooper()){
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//        }
//    };

    public void communicate(CallBack callBack, List<Song> list) {
        this.callBack = callBack;
        this.list = list;
    }

    public void addTimer() {
        Message message = Message.obtain();
        message.what = ViewConstants.PLAYING;
        handler.sendMessage(message);
    }

    public void musicControlNotification() {
        createNotificationChannel();
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, versionPendingIntent++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setImageViewResource(R.id.iv_clear, R.drawable.ic_clear);
        remoteViews.setImageViewResource(R.id.iv_last_song_notification, R.drawable.ic_last_song);
        remoteViews.setImageViewResource(R.id.iv_next_song_notification, R.drawable.ic_next_song);
        remoteViews.setImageViewResource(R.id.iv_pause_notification, R.drawable.ic_stop);


        // 暂停播放操作
        Intent intentPause = new Intent(ViewConstants.PAUSE_SONG_NOTIFICATION_ACTION);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, versionPendingIntent++, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_pause_notification, pendingIntentPause);

        //下一首操作
        Intent intentLastSong = new Intent(ViewConstants.LAST_SONG_NOTIFICATION_ACTION);
        PendingIntent pendingIntentLastSong = PendingIntent.getBroadcast(this, versionPendingIntent++, intentLastSong, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_pause_notification, pendingIntentLastSong);

        //上一首操作
        Intent intentNextSong = new Intent(ViewConstants.NEXT_SONG_NOTIFICATION_ACTION);
        PendingIntent pendingIntentNextSong = PendingIntent.getBroadcast(this, versionPendingIntent++, intentNextSong, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_pause_notification, pendingIntentNextSong);

        notification = new NotificationCompat.Builder(this, ViewConstants.ORDINARY_NOTIFICATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_small_icon_music)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContent(remoteViews)
                .build();
        startForeground(ViewConstants.ID_ORDINARY_NOTIFICATION,notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ViewConstants.ORDINARY_NAME_NOTIFICATION;
            String description = ViewConstants.ORDINARY_DESCRIPTION_NOTIFICATION;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ViewConstants.ORDINARY_NOTIFICATION, name, importance);
            channel.setDescription(description);
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager.getNotificationChannel(ViewConstants.ORDINARY_NOTIFICATION)==null){
                notificationManager.createNotificationChannel(channel);
            }
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
    static class MusicReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (handler != null) {
                if(player == null){
                    Log.e(ViewConstants.TAG, "onReceive: player没有实例" );
                }else {
                    if (ViewConstants.PAUSE_SONG_NOTIFICATION_ACTION.equals(action)) {
                        if(player.isPlaying()){
                            handler.sendEmptyMessage(ViewConstants.PAUSE);
                        }else {
                            handler.sendEmptyMessage(ViewConstants.CONTINUE);
                        }
                    } else if (ViewConstants.LAST_SONG_NOTIFICATION_ACTION.equals(action)) {

                    }
                }

            }
        }
    }

     class MusicControl extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }

        public  void play( int position) {
            PlayerService.position = position;
            upData(position);
            playerModel.requestMusicUrl(list.get(position).getId(), new PlayerModel.Callback() {
                @Override
                public void getUrl(String url) {
                    if(url.equals(ViewConstants.NULL)){
                       url = ViewConstants.ID_SEARCH_PREFIX+list.get(position).getId();
                        Log.e(ViewConstants.TAG, "onCreate: 该歌曲为null");
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


        public void pausePlay() {
            remoteViews.setImageViewResource(R.id.iv_pause_notification,R.drawable.ic_stop);
            notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION,notification);
            handler.sendEmptyMessage(ViewConstants.PAUSE);
        }

        public void continuePlay() {
            remoteViews.setImageViewResource(R.id.iv_pause_notification,R.drawable.ic_continue);
            notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION,notification);
            handler.sendEmptyMessage(ViewConstants.CONTINUE);
        }

//        public void stopPlay(int position) {
//
//        }

        public void seekTo(int progress) {
            Message message = Message.obtain();
            message.obj = progress;
            message.what = ViewConstants.SEEK_TO;
            handler.sendMessage(message);
        }

        public void nextSong( int position) {
            PlayerService.position = position;
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
        public void noSong() {
            handler.sendEmptyMessage(ViewConstants.NO_SONG);
        }
    }
    private void upData(int position){
        remoteViews.setTextViewText(R.id.tv_notification_song_name, list.get(position).getName());
        StringBuilder author = new StringBuilder(" ");
        for (int i = 0; i < list.get(position).getArtists().size(); i++) {
            author.append(list.get(position).getArtists().get(i).getName()).append(" ");
        }
        remoteViews.setTextViewText(R.id.tv_notification_song_author, author);
        remoteViews.setImageViewResource(R.id.iv_pause_notification,R.drawable.ic_continue);
        Picasso.with(getApplicationContext()).load(list.get(position).getAlbum().getPicUrl()+ ViewConstants.PARAM).into(remoteViews, R.id.iv_pic_album, ViewConstants.ID_ORDINARY_NOTIFICATION,notification);
        notificationManager.notify(ViewConstants.ID_ORDINARY_NOTIFICATION, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
        player.reset();
        handlerThread.quitSafely();
        player = null;
        play_logic = ViewConstants.PLAY_REPEAT;
    }

    public interface CallBack {
        void prepared();

        void changeData(int duration, int currentPosition);
    }


}