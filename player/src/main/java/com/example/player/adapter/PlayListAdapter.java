package com.example.player.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.player.PlayerService;
import com.example.player.R;
import com.example.player.entity.Artist;
import com.example.player.entity.Song;

import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {
    private final List<Song> playList;
    private Callback callback;

    public PlayListAdapter(List<Song> playList,Callback callback) {
        this.playList = playList;
        this.callback = callback;

    }

    @NonNull
    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull PlayListAdapter.ViewHolder holder, int position) {
        holder.songName.setText(playList.get(position).getName());
        if(position == PlayerService.position){
            holder.songName.setTextColor(Color.RED);
        }else {
            holder.songName.setTextColor(Color.GRAY);
        }
        StringBuffer artistName = new StringBuffer(" ");
        for (int i = 0; i < playList.get(position).getArtists().size(); i++) {
            Artist artist = playList.get(position).getArtists().get(i);
            String artName = artist.getName();
            artistName.append(artName).append(" ");
        }
        holder.authorName.setText(artistName);
        holder.remove.setOnClickListener(v->{
            callback.remove(position);
        });
        holder.songName.setOnClickListener(v->{
            PlayerService.position = position;
            callback.change();
        });
    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        TextView authorName;
        Button remove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.tv_song_name_play_list);
            authorName = itemView.findViewById(R.id.tv_author_name_play_list);
            remove = itemView.findViewById(R.id.bt_remove_play_list);
        }
    }

    public interface Callback{
        void remove(int position);
        void change();
    }

    public interface CallbackPosition{
        void getPosition(int position);
    }
}
