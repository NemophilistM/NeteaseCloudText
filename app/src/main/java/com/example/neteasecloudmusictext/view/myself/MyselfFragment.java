package com.example.neteasecloudmusictext.view.myself;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



import com.example.neteasecloudmusictext.databinding.FragmentMyselfBinding;

import com.example.player.PlayerActivity;


public class MyselfFragment extends Fragment {
    private FragmentMyselfBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding= FragmentMyselfBinding.inflate(inflater,container,false);
        binding.tvGetSelfList.setOnClickListener(v->{
                Intent intent = new Intent(this.getContext(),MyselfSongListActivity.class);
                startActivity(intent);
        });
        return binding.getRoot();
    }
}
