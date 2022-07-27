package com.example.neteasecloudmusictext.view.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.neteasecloudmusictext.R;
import com.example.neteasecloudmusictext.databinding.FragmentSearchMainBinding;
import com.example.neteasecloudmusictext.vm.search.SearchMainViewModel;
import com.example.neteasecloudmusictext.widget.FlowLayoutWidget;

public class SearchMainFragment extends Fragment {
    private FragmentSearchMainBinding binding;
    CallBackAutoFill callBackAutoFill;
    private SearchMainViewModel viewModel;
    public SearchMainFragment(CallBackAutoFill callBackAutoFill) {
        this.callBackAutoFill = callBackAutoFill;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchMainBinding.inflate(inflater);
        viewModel = new ViewModelProvider(requireActivity()).get(SearchMainViewModel.class);
        viewModel.requestHotSongList();
        viewModel.hotSongList.observe(getViewLifecycleOwner(),hotSongList->{
            for (int i = 0; i < hotSongList.size(); i++) {
                TextView textView = new TextView(requireActivity());
                textView.setText(hotSongList.get(i));
                textView.setBackgroundResource(R.drawable.ripple_onclick_hot_song);
                // 告知父布局要怎么布局
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                int padding = 10;
                textView.setPadding(padding, padding, padding, padding);
                String word = hotSongList.get(i);
                textView.setOnClickListener(v -> {
                    callBackAutoFill.autoFill(word);
                });
                binding.flowLayoutHotSong.addView(textView);
            }
        });
        return binding.getRoot();
    }
    public interface CallBackAutoFill {
        /**
         * 回调方法，暴露给activity给他填充到搜索栏进行搜索
         *
         * @param hotKet 热词
         */
        public void autoFill(String hotKet);
    }


}
