package com.example.neteasecloudmusictext.view.follow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.neteasecloudmusictext.databinding.FragmentFollowBinding;

public class FollowFragment extends Fragment {
    private FragmentFollowBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}
