package com.example.player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.player.databinding.ActivityPlayerBinding;
import com.example.player.entity.Song;
import com.example.player.util.HttpUtil;
import com.example.player.util.TimeUtil;
import com.example.player.view.PlayerViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    //判断是否绑定服务，默认为非绑定
    private boolean isBind = false;
    //判断是否为第一次进入，默认为是
    private boolean isFirstInside = true;
    //判断是否正在播放，默认为否
    public boolean isPlaying = false;
    //服务链接类
    private ServiceConnection serviceConnection;
    //vm层
    private PlayerViewModel playerViewModel;
    //服务类
    private PlayerService playerService;
    //    //静态类，用于记录页数
//    private   int PlayerService.position = 0;
    //歌曲列表，从跳转activity获取
    public List<Song> songList;
    //随机数，用于随机播放的逻辑
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //建立用于申请url的线程池
        HttpUtil.buildThreadPool();
        //初始化顶部栏
        initToolbar();

        //获取传入的歌单列表和歌曲位置
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(ViewConstants.SONG_LIST);
        if (bundle == null) {
            songList = PlayerService.list;
        } else {
            songList = (List<Song>) bundle.getSerializable(ViewConstants.SONG_LIST);
            PlayerService.position = intent.getIntExtra(ViewConstants.POSITION, 0);
        }


        //建立与vm层的通信
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        //完成服务绑定
        init();
        //播放逻辑的判断显示(由于逻辑的版本号是存储在服务层的静态类，因此在进入播放时需要判断)
        if (PlayerService.play_logic == ViewConstants.PLAY_REPEAT) {
            binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeat);
        } else if (PlayerService.play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
            binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeatonce);
        } else if (PlayerService.play_logic == ViewConstants.PLAY_SHUFFLE) {
            binding.ivPlayLogic.setImageResource(R.drawable.ic_player_shuffle);
        }

        //完成进入播放界面的ui更新
        Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl()).into(binding.ivPicture);
        binding.tvSongName.setText(songList.get(PlayerService.position).getName());
        binding.ivLastSong.setImageResource(R.drawable.ic_last_song);
        binding.ivNextSong.setImageResource(R.drawable.ic_next_song);
        binding.ivStop.setImageResource(R.drawable.ic_stop);


        // 对进度条位置的参数观察
        playerViewModel.currentPosition.observe(this, currentPosition -> {
            // 判断是否播放完毕
            // 未播放完毕
            if (currentPosition != ViewConstants.SONG_END) {
                binding.sbProgress.setProgress(currentPosition);
                String time = TimeUtil.time(currentPosition);
                // 显示当前歌曲已经播放的时间
                binding.tvProgress.setText(time);
            }
            // 播放完毕
            else {
                // 获取播放逻辑进行判断
                int play_logic = PlayerService.play_logic;
                // 播放逻辑：顺序播放
                if (play_logic == ViewConstants.PLAY_REPEAT) {
                    PlayerService.position++;
                    if (PlayerService.position <= (songList.size() - 1)) {
                        Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
                        binding.tvSongName.setText(songList.get(PlayerService.position).getName());
                        playerService.nextSong();
                    } else {
                        PlayerService.position = 0;
                        Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
                        binding.tvSongName.setText(songList.get(PlayerService.position).getName());
                        playerService.nextSong();
                    }
                }
                // 播放逻辑：单曲循环(此时的播放位置是没有发生改变的)
                else if (play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                    playerService.nextSong();
                }
                // 播放逻辑：随机播放
                else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                    if (random == null) {
                        random = new Random();
                    }
                    // 判断此时的下一首随机播放存储列表是否为空(为空就代表不是先点击了上一首随机播放)
                    if (PlayerService.nextRandomList.size() <= 1) {
                        PlayerService.position = random.nextInt(songList.size());
                        PlayerService.previousRandomList.add(PlayerService.position);
                        judgeSongStatus();
                    } else {
                        PlayerService.position = PlayerService.nextRandomList.get(PlayerService.nextRandomList.size() - 1);
                        PlayerService.nextRandomList.remove(PlayerService.nextRandomList.size() - 1);
                        judgeSongStatus();
                    }
                }
            }
        });
        //对进度条总计的参数观察
        playerViewModel.duration.observe(this, duration -> {
            binding.sbProgress.setMax(duration);
            String time = TimeUtil.time(duration);
            binding.tvTotal.setText(time);
        });
        //点击事件
        //播放暂停键的点击逻辑
        binding.ivStop.setOnClickListener(v -> {
            //第一次进入，未播放
            if (isFirstInside && !isPlaying) {
                isFirstInside = false;
                isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
                //完成三层之间的通信
                playerViewModel.play(playerService, songList);
                //准备播放器
                playerService.play();
            }
            // 不是第一次进入，未播放
            else if (!isFirstInside && !isPlaying) {
                isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
                playerService.continuePlay();
            }
            // 不是第一次进入，播放
            else if (!isFirstInside && isPlaying) {
                isPlaying = false;
                binding.ivStop.setImageResource(R.drawable.ic_stop);
                playerService.pausePlay();
            }
        });

        //下一首的点击逻辑
        binding.ivNextSong.setOnClickListener(v -> {
            // 首先将当前列表位置加一
            PlayerService.position++;

            // 获取播放逻辑进行判断
            int play_logic = PlayerService.play_logic;
            // 播放逻辑：顺序，单曲循环(这二者点击下一首都会跳转到列表下一首)
            if (play_logic == ViewConstants.PLAY_REPEAT || play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                //判断是否为最后一首
                if (PlayerService.position <= (songList.size() - 1)) {
                    judgeSongStatus();
                } else {
                    PlayerService.position = 0;
                    judgeSongStatus();
                }
            }
            // 播放逻辑：随机播放
            else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                if (random == null) {
                    random = new Random();
                }
                // 判断此时的下一首随机播放存储列表是否为空(为空就代表不是先点击了上一首随机播放)
                if (PlayerService.nextRandomList.size() <= 1) {
                    PlayerService.position = random.nextInt(songList.size());
                    PlayerService.previousRandomList.add(PlayerService.position);
                    judgeSongStatus();
                } else {
                    PlayerService.position = PlayerService.nextRandomList.get(PlayerService.nextRandomList.size() - 1);
                    PlayerService.nextRandomList.remove(PlayerService.nextRandomList.size() - 1);
                    judgeSongStatus();
                }

            }

        });

        // 上一首的点击逻辑
        binding.ivLastSong.setOnClickListener(v -> {
            //先让当前位置自减1
            PlayerService.position--;
            // 判断播放逻辑
            int play_logic = PlayerService.play_logic;
            // 播放逻辑：顺序，单曲循环
            if (play_logic == ViewConstants.PLAY_REPEAT || play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                if (PlayerService.position >= 0) {
                    judgeSongStatus();
                } else {
                    PlayerService.position = (songList.size() - 1);
                    judgeSongStatus();
                }
            }
            // 播放逻辑：随机播放
            else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                if (random == null) {
                    random = new Random();
                }
                // 判断此时的上一首随机播放存储列表是否为空
                if (PlayerService.previousRandomList.size() <= 1) {
                    PlayerService.position = random.nextInt(songList.size());
                    judgeSongStatus();
                    PlayerService.nextRandomList.add(PlayerService.position);
                } else {
                    PlayerService.position = PlayerService.previousRandomList.get(PlayerService.previousRandomList.size() - 1);
                    PlayerService.previousRandomList.remove(PlayerService.previousRandomList.size() - 1);
                    judgeSongStatus();
                }
            }

        });

        // 播放逻辑的点击事件
        binding.ivPlayLogic.setOnClickListener(v -> {
            if (PlayerService.play_logic == ViewConstants.PLAY_REPEAT) {
                PlayerService.play_logic = ViewConstants.PLAY_REPEAT_ONCE;
                binding.ivPlayLogic.setImageResource(R.drawable.ic_player_repeatonce);
            } else if (PlayerService.play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                PlayerService.play_logic = ViewConstants.PLAY_SHUFFLE;
                PlayerService.previousRandomList.add(PlayerService.position);
                PlayerService.nextRandomList.add(PlayerService.position);
                binding.ivPlayLogic.setImageResource(R.drawable.ic_player_shuffle);
            } else if (PlayerService.play_logic == ViewConstants.PLAY_SHUFFLE) {
                PlayerService.play_logic = ViewConstants.PLAY_REPEAT;
                PlayerService.previousRandomList.clear();
                PlayerService.nextRandomList.clear();
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
                playerService.seekTo(progress);
            }
        });


    }

    /**
     * 判断播放状态
     */
    private void judgeSongStatus() {
        if (!isPlaying && isFirstInside) {
            //第一次进入但没有播放
            isFirstInside = false;
            isPlaying = true;
            binding.ivStop.setImageResource(R.drawable.ic_continue);
            //完成三层之间的通信
            playerViewModel.play(playerService, songList);
            //准备播放器
            playerService.play();
        } else if (!isFirstInside && isPlaying) {
            //不是第一次进入正在播放
            Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
            binding.tvSongName.setText(songList.get(PlayerService.position).getName());
//                    playerViewModel.getUrl(songList.get(PlayerService.position).getId());
            playerService.nextSong();
        } else {
            //不是第一次进入没有播放的
            isPlaying = true;
            binding.ivStop.setImageResource(R.drawable.ic_continue);
            if (PlayerService.position == songList.size() - 1) {
                Toast.makeText(this, "当前歌曲为播放列表最后一首，请及时添加", Toast.LENGTH_SHORT).show();
            }
            Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).into(binding.ivPicture);
            binding.tvSongName.setText(songList.get(PlayerService.position).getName());
//                    playerViewModel.getUrl(songList.get(PlayerService.position).getId());
            playerService.nextSong();
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