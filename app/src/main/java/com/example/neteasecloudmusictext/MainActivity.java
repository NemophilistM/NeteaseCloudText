package com.example.neteasecloudmusictext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.neteasecloudmusictext.adapter.BottomBarAdapter;
import com.example.neteasecloudmusictext.databinding.ActivityMainBinding;
import com.example.neteasecloudmusictext.util.HttpUtil;
import com.example.neteasecloudmusictext.view.LoginActivity;
import com.example.neteasecloudmusictext.view.search.SearchActivity;
import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.neteasecloudmusictext.view.find.FindFragment;
import com.example.neteasecloudmusictext.view.follow.FollowFragment;
import com.example.neteasecloudmusictext.view.myself.MyselfFragment;
import com.example.player.PlayerService;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView ivHeader;
    private ActivityMainBinding activityMainBinding;
    private List<Fragment> fragmentList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        //修改状态栏
        Window window = this.getWindow();
        window.setStatusBarColor(Color.GRAY);

//        先开启前台服务，歌曲可以后面再放进去
        Intent intent = new Intent(this, PlayerService.class);
        startService(intent);

        //创建线程池
        HttpUtil.buildThreadPool();

        //标题栏初始化
        initToolBar();

        //侧边栏初始化
        initSidebar();

        //底边栏实现
        initBottomBar();
    }



    private void initBottomBar() {
        TabLayout tabLayout = activityMainBinding.tabBottomBar;
        ViewPager viewPager = activityMainBinding.vpBottomBar;

        fragmentList.add(new FindFragment());
        fragmentList.add(new FollowFragment());
        fragmentList.add(new MyselfFragment());

        List<String> titleList = new ArrayList<>();
        titleList.add(ViewConstants.TITLE_FIND);
        titleList.add(ViewConstants.TITLE_FOLLOW);
        titleList.add(ViewConstants.TITLE_MYSELF);

        List<Integer> iconList = new ArrayList<>();
        iconList.add(R.drawable.ic_find);
        iconList.add(R.drawable.ic_follow);
        iconList.add(R.drawable.ic_myself);

        BottomBarAdapter adapter = new BottomBarAdapter(getSupportFragmentManager(), fragmentList, titleList, iconList, this);
        viewPager.setAdapter(adapter);

        //设置第一页为首页

        viewPager.setCurrentItem(0);
        tabLayout.setupWithViewPager(viewPager);

        //自定义tab
        for (int index = 0; index < tabLayout.getTabCount(); index++) {
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (tab != null) {
                tab.setCustomView(adapter.getSelectTabView(index));
            }
        }

        View view = Objects.requireNonNull(tabLayout.getTabAt(0)).getCustomView();
        assert view != null;
        TextView textView = view.findViewById(R.id.tv_tab_bottom_bar);
        textView.setTextColor(Color.RED);
        ImageView imageView = view.findViewById(R.id.iv_tab_bottom_bar);
        imageView.setImageResource(R.drawable.ic_check);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                assert view != null;
                TextView textView = view.findViewById(R.id.tv_tab_bottom_bar);
                textView.setTextColor(Color.RED);
                ImageView imageView1 = view.findViewById(R.id.iv_tab_bottom_bar);
                imageView1.setImageResource(R.drawable.ic_check);
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    View view = tab.getCustomView();
                    assert view != null;
                    TextView textView = view.findViewById(R.id.tv_tab_bottom_bar);
                    textView.setTextColor(Color.BLACK);
                    ImageView imageView1 = view.findViewById(R.id.iv_tab_bottom_bar);
                    imageView1.setImageResource(R.drawable.ic_find);
                } else if (position == 1) {
                    View view = tab.getCustomView();
                    assert view != null;
                    TextView textView = view.findViewById(R.id.tv_tab_bottom_bar);
                    textView.setTextColor(Color.BLACK);
                    ImageView imageView1 = view.findViewById(R.id.iv_tab_bottom_bar);
                    imageView1.setImageResource(R.drawable.ic_follow);
                } else {
                    View view = tab.getCustomView();
                    assert view != null;
                    TextView textView = view.findViewById(R.id.tv_tab_bottom_bar);
                    textView.setTextColor(Color.BLACK);
                    ImageView imageView1 = view.findViewById(R.id.iv_tab_bottom_bar);
                    imageView1.setImageResource(R.drawable.ic_myself);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    private void initSidebar() {
        ivHeader = findViewById(R.id.iv_header);
        Picasso.with(this).load(R.drawable.img_test_header).resize(200, 200).placeholder(R.drawable.img_header).error(R.drawable.img_error).into(ivHeader);
        ivHeader.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.tb_search);
        drawerLayout = findViewById(R.id.drawer_layout_base_activity);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_hamburger_button);
        }
    }

    private void removeFragment(List<Fragment> fragmentList) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentList.size(); i++) {
            transaction.remove(fragmentList.get(i));
        }
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base_activity_toolbar, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_toolbar:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fragmentList!=null){
            removeFragment(fragmentList);
        }
    }
}