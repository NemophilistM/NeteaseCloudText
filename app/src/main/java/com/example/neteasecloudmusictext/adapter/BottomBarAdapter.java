package com.example.neteasecloudmusictext.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import com.example.neteasecloudmusictext.R;

import java.util.List;

public class BottomBarAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList;
    private final List<String> titleList;
    private final List<Integer> iconList;
    private Context context;


    public BottomBarAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentList, List<String> titleList, List<Integer> iconList, Context context) {
        super(fm);
        this.context = context;
        this.fragmentList = fragmentList;
        this.titleList = titleList;
        this.iconList = iconList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public View getSelectTabView(int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bottem_bar, null);
        ImageView imageView = view.findViewById(R.id.iv_tab_bottom_bar);
        imageView.setImageResource(iconList.get(position));
        TextView textView = view.findViewById(R.id.tv_tab_bottom_bar);
        textView.setText(titleList.get(position));
        return view;
    }
}
