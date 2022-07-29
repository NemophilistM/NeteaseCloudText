package com.example.neteasecloudmusictext.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neteasecloudmusictext.R;
import com.example.neteasecloudmusictext.base.CallbackEnter;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.player.entity.Artist;
import com.example.player.entity.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolderAction> {

    private static final int mPageSize = ViewConstants.EACH_PAGE_LIMIT;
    private int mPagePosition = 0;
    private boolean hasMordData = true;
    private static OnLoad mOnLoad;
    public List<Song> list = new ArrayList<>();
    private static CallbackEnter mCallbackEnter;


    public SongListAdapter(OnLoad onLoad, CallbackEnter callbackEnter) {
        mOnLoad = onLoad;
        mCallbackEnter = callbackEnter;

    }


    @NonNull
    @Override
    public ViewHolderAction onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        ViewHolderAction viewHolderAction;
        if (viewType == R.layout.item_download_now) {
            viewHolderAction = new LoadingItemVH(view);
        } else if (viewType == R.layout.item_no_more) {
            viewHolderAction = new NoMoreItemVH(view);
        } else {
            viewHolderAction = new SongListItem(view);
        }
        return viewHolderAction;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderAction holder, int position) {
        holder.takeAction(list, position);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void requestData(Boolean hasMordData) {
        this.hasMordData = hasMordData;
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            if (hasMordData) {
                return R.layout.item_download_now;
            } else {
                return R.layout.item_no_more;
            }
        } else {
            return R.layout.item_song_show;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void appendData(List<Song> dataSet) {
        if (dataSet != null && !dataSet.isEmpty()) {
            list = dataSet;
            notifyDataSetChanged();
        }

    }

    static class LoadingItemVH extends ViewHolderAction {

        public LoadingItemVH(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void takeAction(List<Song> songList, int position) {
            mOnLoad.load();
        }

    }

    static class NoMoreItemVH extends ViewHolderAction {

        public NoMoreItemVH(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void takeAction(List<Song> songList, int position) {
        }
    }

    static class SongListItem extends ViewHolderAction {
        TextView tvTitle;
        TextView tvAlbumName;
        TextView tvAuthor;
        ImageView ivAlbumPic;

        public SongListItem(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_name);
            tvAlbumName = itemView.findViewById(R.id.tv_album);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            ivAlbumPic = itemView.findViewById(R.id.iv_pic_album);
        }

        @Override
        void takeAction(List<Song> songList, int position) {
            Song song = songList.get(position);
            Picasso.with(itemView.getContext()).load(song.getAlbum().getPicUrl()+ViewConstants.PARAM+ViewConstants.PARAM_LIMIT).placeholder(R.drawable.img_wait).error(R.drawable.img_404).into(ivAlbumPic);
            StringBuffer artistName = new StringBuffer(" ");
            for (int i = 0; i < song.getArtists().size(); i++) {
                Artist artist = song.getArtists().get(i);
                String artName = artist.getName();
                artistName.append(artName).append(" ");
            }
            tvAuthor.setText(artistName);
            tvTitle.setText(song.getName());
            tvAlbumName.setText(song.getAlbum().getName());
            itemView.setOnClickListener(v -> {
                mCallbackEnter.play(songList,position);
            });
        }

    }

    /**
     * adapter通知fragment发送网络请求
     */
    public interface OnLoad {
        void load();
    }

    /**
     * 用于fragment告知adapter请求是否成功
     */
    public interface ILoadCallback {
        void onSuccess();

        void onFailure();
    }

    /**
     * 抽象类，包含一个抽象方法，是每个vh类都需要实现的，具体操作不同
     */
    abstract static class ViewHolderAction extends RecyclerView.ViewHolder {
        public ViewHolderAction(@NonNull View itemView) {
            super(itemView);
        }

        abstract void takeAction(List<Song> songList, int position);
    }
}
