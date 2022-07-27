package com.example.neteasecloudmusictext.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;


import androidx.appcompat.widget.Toolbar;

import com.example.neteasecloudmusictext.MainActivity;
import com.example.neteasecloudmusictext.R;
import com.example.neteasecloudmusictext.databinding.ActivityLoginBinding;
import com.example.neteasecloudmusictext.vm.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding activityLoginBinding;
    private LoginViewModel loginViewModel;

    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        //修改状态栏
        Window window = this.getWindow();
        window.setStatusBarColor(Color.GRAY);

        initToolbar();

        //发送验证码
        activityLoginBinding.tvSendCaptcha.setOnClickListener(v -> {
            String phone = activityLoginBinding.etLoginPhone.getText().toString();
            loginViewModel.pastPhone(phone);
        });
        //登录操作
        activityLoginBinding.btLogin.setOnClickListener(v -> {
            String phone = activityLoginBinding.etLoginPhone.getText().toString();
            String captcha = activityLoginBinding.etLoginCaptcha.getText().toString();
            loginViewModel.pastCaptcha(phone,captcha);
        });

        //注册操作
        activityLoginBinding.tvLoginRegister.setOnClickListener(v->{
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        loginViewModel.liveDataSendWhetherTrue.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(LoginActivity.this,"注册"+s,Toast.LENGTH_SHORT).show();

            }
        });
        loginViewModel.liveDataLoginWhetherTrue.observe(this, s->{
            Toast.makeText(LoginActivity.this,"登录"+s,Toast.LENGTH_SHORT).show();
            Intent intent  = new Intent(this, MainActivity.class);
            startActivity(intent);
        });




    }
    @SuppressLint("UseSupportActionBar")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.tb_login);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_return);
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    public interface pastPhone{
        public void pastPhone(String phone);
        public void pastCaptcha(String phone,String captcha);

    }

}