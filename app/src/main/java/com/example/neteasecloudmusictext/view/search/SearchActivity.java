package com.example.neteasecloudmusictext.view.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neteasecloudmusictext.R;

import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.neteasecloudmusictext.databinding.ActivitySearchBinding;
import com.example.neteasecloudmusictext.vm.search.SearchMainViewModel;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private String text;
    FragmentManager fragmentManager = getSupportFragmentManager();
    private SearchResultFragment searchResultFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 将状态栏修改为灰色
        Window window = this.getWindow();
        window.setStatusBarColor(Color.GRAY);

        initToolBar();

        //viewModel初始化
        SearchMainViewModel viewModel = new ViewModelProvider(this).get(SearchMainViewModel.class);
        viewModel.requestHotSongList();
        binding.flowLayoutHotSong.setVisibility(View.GONE);
        viewModel.hotSongList.observe(this, hotSongList -> {
            binding.flowLayoutHotSong.setVisibility(View.VISIBLE);
            for (int i = 0; i < hotSongList.size(); i++) {
                TextView textView = new TextView(this);
                textView.setText(hotSongList.get(i));
                textView.setBackgroundResource(R.drawable.ripple_onclick_hot_song);
                // 告知父布局要怎么布局
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                int padding = 10;
                textView.setPadding(padding, padding, padding, padding);
                String word = hotSongList.get(i);
                textView.setOnClickListener(v -> {
                    binding.etWidgetSearch.setText(word);
                    if (searchResultFragment == null) {
                        searchResultFragment = SearchResultFragment.newInstance(word);
                        replaceFragment();
                    }
                });
                binding.flowLayoutHotSong.addView(textView);
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
            text = binding.etWidgetSearch.getEditText();
            if (text.isEmpty()) {
                Toast.makeText(this, ViewConstants.INPUT_NULL_TIPS, Toast.LENGTH_SHORT).show();
            } else {
                if (searchResultFragment == null) {
                    searchResultFragment = SearchResultFragment.newInstance(text);
                    replaceFragment();
                } else {
                    removeFragment();
                    searchResultFragment = SearchResultFragment.newInstance(text);
                    replaceFragment();
                    showFragment();
                }
            }
            InputMethodManager manager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = this.getCurrentFocus();
            if (view != null) {
                manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
        return super.dispatchKeyEvent(event);
    }

    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.tb_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return);
        }
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
                text = binding.etWidgetSearch.getEditText();
                if (text.isEmpty()) {
                    Toast.makeText(this, ViewConstants.INPUT_NULL_TIPS, Toast.LENGTH_SHORT).show();
                } else {
                    if (searchResultFragment == null) {
                        searchResultFragment = SearchResultFragment.newInstance(text);
                        replaceFragment();
                    } else {
                        removeFragment();
                        searchResultFragment = SearchResultFragment.newInstance(text);
                        replaceFragment();
                    }

                }
                break;
            case android.R.id.home:
                if(searchResultFragment!=null){
                    removeFragment();
                }else {
                    finish();
                }
                break;
            default:
        }
        return true;
    }


    private void replaceFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.layout_search, searchResultFragment);
        transaction.commit();
    }


    private void showFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(searchResultFragment);
        transaction.commit();
    }

    private void removeFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(searchResultFragment);
        transaction.commit();
        searchResultFragment = null;
    }


}