package com.example.neteasecloudmusictext.base;

import com.example.player.entity.Song;

import java.util.List;

public interface CallbackEnter {
    void play(List<Song> songList,int position);
}
