package com.example.neteasecloudmusictext.vm.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.neteasecloudmusictext.model.search.SearchResultModel;
import com.example.player.entity.Song;

import java.util.List;

public class SearchResultViewModel extends ViewModel {
    private final MutableLiveData<List<Song>> _resultSongList = new MutableLiveData<>();
    public LiveData<List<Song>> resultSongList = _resultSongList ;

    public void requestResultSongList(String  postWords,int curPage,boolean isRefresh){
        SearchResultModel.requestResultSongList(postWords,curPage,songs -> {
            if(songs!= null ){
                if(isRefresh){
                    _resultSongList.postValue(songs);
                }else {
                    List<Song> list = _resultSongList.getValue();
                    if(list!=null){
                        list.addAll(songs);
                        _resultSongList.postValue(list);
                    }else {
                        _resultSongList.postValue(songs);
                    }
                }
                
            }else {

            }



        });
    }


}
