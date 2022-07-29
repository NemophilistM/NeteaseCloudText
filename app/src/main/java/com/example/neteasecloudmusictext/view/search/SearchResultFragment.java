package com.example.neteasecloudmusictext.view.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neteasecloudmusictext.adapter.SongListAdapter;
import com.example.neteasecloudmusictext.base.CallbackEnter;
import com.example.neteasecloudmusictext.databinding.FragmentSearchResultBinding;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.neteasecloudmusictext.vm.search.SearchResultViewModel;
import com.example.player.PlayerActivity;
import com.example.player.entity.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {
    private FragmentSearchResultBinding binding;
    private SearchResultViewModel viewModel;
    private int curPage = 1;
    public static String postWords;
    SongListAdapter adapter;

    public static SearchResultFragment newInstance(String keyWords) {
        Bundle args = new Bundle();
        args.putString(ViewConstants.POST_WORD, keyWords);
        SearchResultFragment fragment = new SearchResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

//    public static void postWords(String keyWords) {
//        postWords = keyWords;
//    }

    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchResultBinding.inflate(inflater);
        viewModel = new ViewModelProvider(this).get(SearchResultViewModel.class);
        Bundle args = getArguments();
        assert args != null;
        postWords = args.getString(ViewConstants.POST_WORD);
        RecyclerView recyclerView = binding.rvSongList;
        //初始化适配器
        adapter = new SongListAdapter(() -> {
            viewModel.requestResultSongList(postWords, curPage);
            curPage++;
        }, (songList, position) -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ViewConstants.SONG_LIST, (Serializable) songList);
            Intent intent = new Intent(getContext(), PlayerActivity.class);
            intent.putExtra(ViewConstants.POSITION, position);
            intent.putExtra(ViewConstants.SONG_LIST, bundle);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        viewModel.resultSongList.observe(this.getViewLifecycleOwner(), resultSongList -> {
            if (resultSongList.size()==(curPage-1)*ViewConstants.REQUEST_PAGE_MAX) {
                adapter.requestData(true);
            } else {
                adapter.requestData(false);
            }
            adapter.appendData(resultSongList);
        });
        return binding.getRoot();
    }

}
