package com.example.player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.player.databinding.ActivityPlayerBinding;
import com.example.player.entity.Song;
import com.example.player.util.HttpUtil;
import com.example.player.util.TimeUtil;
import com.example.player.view.PlayerViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    private boolean isBind = false;
    private  boolean isFirstInside = true;
    public  boolean isPlaying = false;
    private ServiceConnection serviceConnection;
    private PlayerService.MusicControl musicControl;
    private PlayerViewModel playerViewModel;
    private PlayerService playerService;
    public static int songPosition = 0;
    private Boolean isFirstSong = true;
    public static List<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        HttpUtil.buildThreadPool();
        initToolbar();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(ViewConstants.SONG_LIST);
        songList = (List<Song>) bundle.getSerializable(ViewConstants.SONG_LIST);
        songPosition = intent.getIntExtra(ViewConstants.POSITION,0);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        //完成服务绑定
        init();
        //播放逻辑的判断显示
        if(PlayerService.play_logic == ViewConstants.PLAY_REPEAT){
            binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeat);
        }else if(PlayerService.play_logic == ViewConstants.PLAY_REPEAT_ONCE){
            binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeatonce);
        }else if(PlayerService.play_logic == ViewConstants.PLAY_SHUFFLE){
            binding.ivPlayLogic.setImageResource(R.drawable.ic_player_shuffle);
        }
        Picasso.with(this).load(songList.get(songPosition).getAlbum().getPicUrl()).into(binding.ivPicture);
        binding.tvSongName.setText(songList.get(songPosition).getName());
        binding.ivLastSong.setImageResource(R.drawable.ic_last_song);
        binding.ivNextSong.setImageResource(R.drawable.ic_next_song);
        binding.ivStop.setImageResource(R.drawable.ic_stop);


        playerViewModel.currentPosition.observe(this, currentPosition -> {
            if(currentPosition!=ViewConstants.SONG_END){
                binding.sbProgress.setProgress(currentPosition);
                String time = TimeUtil.time(currentPosition);
                //显示当前歌曲已经播放的时间
                binding.tvProgress.setText(time);
            }else {
                int play_logic = PlayerService.play_logic;
                if(play_logic == ViewConstants.PLAY_REPEAT){
                    songPosition++;
                    if (songPosition <= (songList.size() - 1)) {
                        Picasso.with(this).load(songList.get(songPosition).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
                        binding.tvSongName.setText(songList.get(songPosition).getName());
                        musicControl.nextSong(songPosition);
                    } else {
                        songPosition = 0;
                        Picasso.with(this).load(songList.get(songPosition).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
                        binding.tvSongName.setText(songList.get(songPosition).getName());
                        musicControl.nextSong(songPosition);
                    }
                } else if(play_logic == ViewConstants.PLAY_REPEAT_ONCE){
                    musicControl.nextSong(songPosition);
                }else if(play_logic == ViewConstants.PLAY_SHUFFLE){

                }

            }

        });
        playerViewModel.duration.observe(this, duration -> {
            binding.sbProgress.setMax(duration);
            String time = TimeUtil.time(duration);
            binding.tvTotal.setText(time);
        });
//        playerViewModel.musicUrl.observe(this, url -> {
//            if(url.equals(ViewConstants.NULL)){
//                if (isFirstSong) {
//                    musicControl.play(ViewConstants.ID_SEARCH_PREFIX+songList.get(songPosition).getId(),songPosition);
//                    isFirstSong = false;
//                } else {
//                    musicControl.nextSong(ViewConstants.ID_SEARCH_PREFIX+songList.get(songPosition).getId()+ViewConstants.ID_SEARCH_SUFFIX,songPosition);
//                }
//                Log.e(ViewConstants.TAG, "onCreate: 该歌曲为null");
//            }else {
//                if (isFirstSong) {
//                    musicControl.play(url,songPosition);
//                    isFirstSong = false;
//                } else {
//                    musicControl.nextSong(url,songPosition);
//                }
//            }
//        });

        //点击事件
        binding.ivStop.setOnClickListener(v -> {
            if (isFirstInside && !isPlaying) {
                isFirstInside = false;
                isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
                //完成三层之间的通信
                playerViewModel.play(playerService,songList);
                //准备播放器
//                playerViewModel.getUrl(songList.get(songPosition).getId());
                musicControl.play(songPosition);
            } else if (!isFirstInside && !isPlaying) {
                isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
                musicControl.continuePlay();
            } else if (!isFirstInside  && isPlaying) {
                isPlaying = false;
                binding.ivStop.setImageResource(R.drawable.ic_stop);
                musicControl.pausePlay();
            }
        });
        binding.ivNextSong.setOnClickListener(v -> {
            songPosition++;
            int play_logic = PlayerService.play_logic;
            if(play_logic ==ViewConstants.PLAY_REPEAT || play_logic == ViewConstants.PLAY_REPEAT_ONCE){
                if (songPosition <= (songList.size() - 1)) {
                    judgeSongStatus(songPosition);
                }else {
                    songPosition = 0;
                    judgeSongStatus(songPosition);
                }
            } else if(play_logic == ViewConstants.PLAY_SHUFFLE){

            }

        });
        binding.ivLastSong.setOnClickListener(v->{
            songPosition--;
            int play_logic = PlayerService.play_logic;
            if(play_logic == ViewConstants.PLAY_REPEAT||play_logic == ViewConstants.PLAY_REPEAT_ONCE){
                if (songPosition >= 0){
                    judgeSongStatus(songPosition);
                } else {
                    songPosition  = (songList.size()-1);
                    judgeSongStatus(songPosition);
                }
            } else if(play_logic == ViewConstants.PLAY_SHUFFLE){

            }

        });
        binding.ivPlayLogic.setOnClickListener(v->{
            if(PlayerService.play_logic == ViewConstants.PLAY_REPEAT){
                PlayerService.play_logic = ViewConstants.PLAY_REPEAT_ONCE;
                binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeatonce);
            } else if(PlayerService.play_logic == ViewConstants.PLAY_REPEAT_ONCE){
                PlayerService.play_logic = ViewConstants.PLAY_SHUFFLE;
                binding.ivPlayLogic.setImageResource(R.drawable.ic_player_shuffle);
            } else if(PlayerService.play_logic == ViewConstants.PLAY_SHUFFLE){
                PlayerService.play_logic = ViewConstants.PLAY_REPEAT;
                binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeat);
            }
        });

        binding.sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                musicControl.seekTo(progress);
            }
        });



    }

    private void judgeSongStatus(int songPosition) {
            if (!isPlaying && isFirstInside) {
                //第一次进入但没有播放
                isFirstInside = false;
                isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
                //完成三层之间的通信
                playerViewModel.play(playerService, songList);
                //准备播放器
//                    playerViewModel.getUrl(songList.get(songPosition).getId());
                musicControl.play(songPosition);
            } else if (!isFirstInside && isPlaying) {
                //不是第一次进入正在播放
                Picasso.with(this).load(songList.get(songPosition).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
                binding.tvSongName.setText(songList.get(songPosition).getName());
//                    playerViewModel.getUrl(songList.get(songPosition).getId());
                musicControl.nextSong(songPosition);
            } else {
                //不是第一次进入没有播放的
                isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
                if (songPosition == songList.size() - 1) {
                    Toast.makeText(this, "当前歌曲为播放列表最后一首，请及时添加", Toast.LENGTH_SHORT).show();
                }
                Picasso.with(this).load(songList.get(songPosition).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
                binding.tvSongName.setText(songList.get(songPosition).getName());
//                    playerViewModel.getUrl(songList.get(songPosition).getId());
                musicControl.nextSong(songPosition);
            }

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.tb_player);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            isFirstInside = true;
            unBind(isBind);
            isBind = false;
            finish();
        }
        return true;
    }

    private void init() {
        Intent intent1 = new Intent(this, PlayerService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicControl = (PlayerService.MusicControl) service;
                playerService = ((PlayerService.MusicControl) service).getService();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {


            }
        };
        //将服务绑定
        isBind = true;
        bindService(intent1, serviceConnection, BIND_AUTO_CREATE);
    }
//    public     Handler handler = new Handler(Looper.getMainLooper()){
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            Bundle bundle  = msg.getData();
//            int duration =bundle.getInt(ViewConstants.DURATION);
//            int currentPosition = bundle.getInt(ViewConstants.CURRENT_POSITION);
//            binding.sbProgress.setMax(duration);
//            binding.sbProgress.setProgress(currentPosition);
//            int minute = duration/1000/60;
//            int second= duration/1000%60;
//            String stringMinute = null;
//            String stringSecond = null;
//            if(minute<10){
//                stringMinute = "0"+minute;
//            }else {
//                stringMinute=minute+"";
//            }
//            if(second<10){
//                stringSecond ="0"+second;
//            }else {
//                stringSecond = second+"";
//            }
//
//            binding.tvTotal.setText(stringMinute+":"+stringSecond);
//            minute=currentPosition/1000/60;
//            second=currentPosition/1000%60;
//            if(minute<10){//如果歌曲的时间中的分钟小于10
//                stringMinute="0"+minute;//在分钟的前面加一个0
//            }else{
//                stringMinute=minute+" ";
//            }
//            if (second<10){//如果歌曲中的秒钟小于10
//                stringSecond="0"+second;//在秒钟前面加一个0
//            }else{
//                stringSecond=second+" ";
//            }
//            //显示当前歌曲已经播放的时间
//            binding.tvProgress.setText(stringMinute+":"+stringSecond);
//
//        }
//    };
//
//    public static void startPlayer(int duration,int currentPosition){
//
//    }

//    public void addTimer(int status){
//        if(status == 0){
//            int duration = playerService.getDuration();
//            binding.sbProgress.setMax(duration);
//            String time = TimeUtil.time(duration);
//            binding.tvTotal.setText(time);
//            HandlerThread handlerThread = new HandlerThread("handlerThread");
//            handlerThread.start();
//            handler = new Handler(handlerThread.getLooper());
//            handler.postDelayed(() -> {
//                //获取播放进度
//                int currentPosition = playerService.getPosition();
//                binding.sbProgress.setProgress(currentPosition);
//                String position = TimeUtil.time(currentPosition);
//                //显示当前歌曲已经播放的时间
//                binding.tvProgress.setText(position);
//            }, 1000L);
//        }else if(status == 1){
//            try {
//                handler.wait();
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }
//        }else {
//            handler.notify();
//        }
//
//    }

    private void unBind(Boolean isBind) {
        if (isBind) {
            unbindService(serviceConnection);
        }
    }



}