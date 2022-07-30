package com.example.player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.player.adapter.PlayListAdapter;
import com.example.player.databinding.ActivityPlayerBinding;
import com.example.player.entity.Song;
import com.example.player.util.HttpUtil;
import com.example.player.util.TimeUtil;
import com.example.player.view.PlayerViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    private List<Song> songList;
    //随机数，用于随机播放的逻辑
    private Random random;
    // 播放列表
    private RecyclerView recyclerView;

    // 播放列表适配器
    private  PlayListAdapter adapter;



    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 告知服务层该活动是否毁灭
        PlayerService.isLive = true;
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
        Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl()+ ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
        binding.tvSongName.setText(songList.get(PlayerService.position).getName());
        binding.ivLastSong.setImageResource(R.drawable.ic_last_song);
        binding.ivNextSong.setImageResource(R.drawable.ic_next_song);
        binding.ivStop.setImageResource(R.drawable.ic_stop);



        //播放列表的初始化
        recyclerView = binding.rvPlayList;
        adapter = new PlayListAdapter(songList, new PlayListAdapter.Callback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void remove(int position) {
                if(adapter!=null){
                    if(songList.size()==1){
                        Toast.makeText(PlayerActivity.this,"该曲目未列表的最后一首，无法移除",Toast.LENGTH_SHORT).show();
                    }else {
                        if(random == null){
                            random = new Random();
                        }
                        adapter.notifyItemRemoved(position);

                        songList.remove(position);
//                        PlayerService.list.remove(position);
                        PlayerService.list.get(position);
                        if(PlayerService.randomList.size()!=0){
                            PlayerService.randomList.add(PlayerService.position);
                            while (PlayerService.randomList.size() < songList.size()) {
                                int random1 = random.nextInt(songList.size());
                                if(!PlayerService.randomList.contains(random1)){
                                    PlayerService.randomList.add(random1);
                                }
                            }
                        }
                        if(position<PlayerService.position){
                            PlayerService.position--;
                        }else if(position==PlayerService.position){
                            Picasso.with(PlayerActivity.this).load(songList.get(position).getAlbum().getPicUrl()+ ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
                            binding.tvSongName.setText(songList.get(position).getName());
                            playerService.nextSong();
                        }
                        adapter.notifyItemRangeChanged(0,adapter.getItemCount());
                        adapter.notifyDataSetChanged();
                    }

                }

            }

            @Override
            public void change() {
                Picasso.with(PlayerActivity.this).load(songList.get(PlayerService.position).getAlbum().getPicUrl()+ ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
                binding.tvSongName.setText(songList.get(PlayerService.position).getName());
                recyclerView.setVisibility(View.GONE);
                playerService.nextSong();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.GONE);

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
                    if (PlayerService.position == (songList.size() - 1)) {
                        PlayerService.position = 0;
                    } else {
                        PlayerService.position++;
                    }
                    Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
                    binding.tvSongName.setText(songList.get(PlayerService.position).getName());
                    playerService.nextSong();
                }
                // 播放逻辑：单曲循环(此时的播放位置是没有发生改变的)
                else if (play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                    playerService.nextSong();
                }
                // 播放逻辑：随机播放
                else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                    if(PlayerService.randomList!=null){
                        if(PlayerService.randomListPosition == PlayerService.randomList.size()-1){
                            PlayerService.randomListPosition = 0;
                        }else {
                            PlayerService.randomListPosition++;
                        }
                        PlayerService.position = PlayerService.randomList.get(PlayerService.randomListPosition);
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

        // 通知栏点击上下首时如果activity还存在需要对其进行更新
        playerViewModel.notificationPositionChange.observe(this,position->{
            Picasso.with(this).load(songList.get(position).getAlbum().getPicUrl() + ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
            binding.tvSongName.setText(songList.get(position).getName());
        });

        // 暂停操作的ui更新
        playerViewModel.notificationPlayingChange.observe(this,isPlaying->{
            if(isPlaying){
                this.isPlaying = true;
                binding.ivStop.setImageResource(R.drawable.ic_continue);
            }else {
                this.isPlaying = false;
                binding.ivStop.setImageResource(R.drawable.ic_stop);
            }
        });


        //点击事件

        // 播放列表的显示逻辑
        binding.ivPlayList.setOnClickListener(v->{
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
        });

        //播放暂停键的点击逻辑
        binding.ivStop.setOnClickListener(v -> {
            if (!isFirstInside && !isPlaying) {
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

            // 获取播放逻辑进行判断
            int play_logic = PlayerService.play_logic;
            // 播放逻辑：顺序，单曲循环(这二者点击下一首都会跳转到列表下一首)
            if (play_logic == ViewConstants.PLAY_REPEAT || play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                //判断是否为最后一首
                if (PlayerService.position == (songList.size() - 1)) {
                    PlayerService.position = 0;
                }else {
                    // 将当前列表位置加一
                    PlayerService.position++;
                }
                judgeSongStatus();

            }
            // 播放逻辑：随机播放
            else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                if(PlayerService.randomList!=null){
                    if(PlayerService.randomListPosition == PlayerService.randomList.size()-1){
                        PlayerService.randomListPosition = 0;
                    }else {
                        PlayerService.randomListPosition++;
                    }
                    PlayerService.position = PlayerService.randomList.get(PlayerService.randomListPosition);
                    judgeSongStatus();
                }

            }

        });

        // 上一首的点击逻辑
        binding.ivLastSong.setOnClickListener(v -> {

            // 判断播放逻辑
            int play_logic = PlayerService.play_logic;
            // 播放逻辑：顺序，单曲循环
            if (play_logic == ViewConstants.PLAY_REPEAT || play_logic == ViewConstants.PLAY_REPEAT_ONCE) {
                if (PlayerService.position == 0) {
                    PlayerService.position = (songList.size() - 1);
                }else {
                    //让当前位置自减1
                    PlayerService.position--;
                }
                judgeSongStatus();

            }
            // 播放逻辑：随机播放
            else if (play_logic == ViewConstants.PLAY_SHUFFLE) {
                if(PlayerService.randomList!=null){
                    if(PlayerService.randomListPosition==0){
                        PlayerService.randomListPosition = PlayerService.randomList.size()-1;
                    }else {
                        PlayerService.randomListPosition--;
                    }
                    PlayerService.position = PlayerService.randomList.get(PlayerService.randomListPosition);
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
                if(random == null){
                    random = new Random();
                }
//                 新建列表存储数据
                PlayerService.randomList.add(PlayerService.position);
                while (PlayerService.randomList.size() < songList.size()) {
                    int random1 = random.nextInt(songList.size());
                    if(!PlayerService.randomList.contains(random1)){
                        PlayerService.randomList.add(random1);
                    }
                }
                binding.ivPlayLogic.setImageResource(R.drawable.ic_player_shuffle);
            } else if (PlayerService.play_logic == ViewConstants.PLAY_SHUFFLE) {
                PlayerService.play_logic = ViewConstants.PLAY_REPEAT;
                PlayerService.randomList.clear();
                PlayerService.randomListPosition = 0;
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
            Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
            binding.tvSongName.setText(songList.get(PlayerService.position).getName());
            playerService.nextSong();
        } else {
            //不是第一次进入没有播放的
            isPlaying = true;
            binding.ivStop.setImageResource(R.drawable.ic_continue);
            if (PlayerService.position == songList.size() - 1) {
                Toast.makeText(this, "当前歌曲为播放列表最后一首，请及时添加", Toast.LENGTH_SHORT).show();
            }
            Picasso.with(this).load(songList.get(PlayerService.position).getAlbum().getPicUrl() + ViewConstants.PARAM).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(binding.ivPicture);
            binding.tvSongName.setText(songList.get(PlayerService.position).getName());
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
                if(recyclerView.getVisibility() == View.VISIBLE){
                    recyclerView.setVisibility(View.GONE);
                }else {
                    isFirstInside = true;
                    unBind(isBind);
                    isBind = false;
                    finish();
                }
            }
        return true;
    }

    private void init() {
        Intent intent1 = new Intent(this, PlayerService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerService = ((PlayerService.MusicControl) service).getService();
                Log.d(ViewConstants.TAG, "onServiceConnected: "+Thread.currentThread().getName());
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
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {


            }
        };
        //将服务绑定
        isBind = true;
        bindService(intent1, serviceConnection, BIND_AUTO_CREATE);
    }

    private void unBind(Boolean isBind) {
        if (isBind) {
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlayerService.isLive  = false;
        songList = null;
    }
}