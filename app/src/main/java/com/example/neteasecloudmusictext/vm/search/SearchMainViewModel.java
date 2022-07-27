package com.example.neteasecloudmusictext.vm.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.neteasecloudmusictext.model.search.SearchMainModel;

import java.util.List;

public class SearchMainViewModel extends ViewModel {
    private final MutableLiveData<List<String >> _hotSongList = new MutableLiveData<>();
    public LiveData<List<String>> hotSongList = _hotSongList ;

    public void requestHotSongList(){
        SearchMainModel.requestHotSongList(_hotSongList::postValue);
    }
}
