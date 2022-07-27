package com.example.neteasecloudmusictext.view.myself;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;

import com.example.neteasecloudmusictext.R;

import com.example.neteasecloudmusictext.adapter.SongListAdapter;
import com.example.neteasecloudmusictext.databinding.ActivityMyselfSongListBinding;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.neteasecloudmusictext.vm.MyselfSongListViewModel;
import com.example.player.entity.Song;

import java.util.ArrayList;
import java.util.List;

public class MyselfSongListActivity extends AppCompatActivity {
    private ActivityMyselfSongListBinding binding;
    private MyselfSongListViewModel myselfSongListViewModel;



    int loadCount;
    private  SongListAdapter.ILoadCallback callback;
    int curPage = 1;
    List<Song> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMyselfSongListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myselfSongListViewModel = new ViewModelProvider(this).get(MyselfSongListViewModel.class);

        //修改状态栏
        Window window = this.getWindow();
        window.setStatusBarColor(Color.GRAY);

        //标题栏初始化
        initToolBar();

        RecyclerView recyclerView = binding.rvSongList;
        SongListAdapter adapter = new SongListAdapter(() -> {
            myselfSongListViewModel.requestSongList();
            curPage++;
        }, (songList,position) -> {

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myselfSongListViewModel.songList.observe(this,songList->{
//            adapter.appendData(songList,curPage);
            callback.onSuccess();
            if(loadCount++== ViewConstants.PAGE_MAX){
                callback.onFailure();
            }
        });
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.tb_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return);
        }
    }
}