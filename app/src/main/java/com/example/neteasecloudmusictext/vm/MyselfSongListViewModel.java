package com.example.neteasecloudmusictext.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.neteasecloudmusictext.model.MyselfSongListModel;
import com.example.player.entity.Song;

import java.util.List;

public class MyselfSongListViewModel extends ViewModel {
    private final MutableLiveData<List<Song>> _songList = new MutableLiveData<>();
    public LiveData<List<Song>> songList = _songList ;

    public void requestSongList(){
        MyselfSongListModel.requestSongList(_songList::postValue);
    }

}
