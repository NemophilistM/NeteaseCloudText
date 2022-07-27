package com.example.player.view;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.player.PlayerService;
import com.example.player.ViewConstants;
import com.example.player.entity.Song;
import com.example.player.model.PlayerModel;

import java.util.List;

public class PlayerViewModel extends ViewModel {
    private final MutableLiveData<Integer> _duration = new MutableLiveData<>();
    public LiveData<Integer> duration =_duration ;
    private final MutableLiveData<Integer> _currentPosition = new MutableLiveData<>();
    public LiveData<Integer> currentPosition = _currentPosition;
    private final MutableLiveData<String> _musicUrl = new MutableLiveData<>();
    public LiveData<String> musicUrl =_musicUrl ;

    public void play(PlayerService playerService, List<Song> list){
        playerService.communicate(new PlayerService.CallBack() {
            @Override
            public void prepared() {
                playerService.addTimer();
            }

            @Override
            public void changeData(int duration, int currentPosition) {
                if(currentPosition == -1 && duration == -1){
                    _currentPosition.postValue(ViewConstants.SONG_END);
                    _duration.postValue(duration);
                }else  {
                    _duration.postValue(duration);
                    _currentPosition.postValue(currentPosition);
                }

            }
        },list);
    }

    public void getUrl (Long id){
        PlayerModel playerModel = new PlayerModel();
        playerModel.requestMusicUrl(id, new PlayerModel.Callback() {
            @Override
            public void getUrl(String url) {
                _musicUrl.postValue(url);
            }

            @Override
            public void dispatch() {

            }
        });
    }

}
