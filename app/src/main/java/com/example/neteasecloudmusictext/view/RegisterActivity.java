package com.example.neteasecloudmusictext.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.neteasecloudmusictext.R;
import com.example.neteasecloudmusictext.databinding.ActivityRegisterBinding;
import com.example.neteasecloudmusictext.vm.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding ;
    private RegisterViewModel registerViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        registerViewModel = new RegisterViewModel();

        //修改状态栏
        Window window = this.getWindow();
        window.setStatusBarColor(Color.GRAY);

        initToolbar();

        binding.tvSendCaptcha.setOnClickListener(v->{
            String pastPhone = binding.etRegisterPhone.getText().toString();
            if(pastPhone.isEmpty()){
                Toast.makeText(this,"上述内容不能为空",Toast.LENGTH_SHORT).show();
            }
            registerViewModel.pastPhone(pastPhone);
        });

        binding.etRegisterCaptcha.setOnClickListener(v->{
            String pastPhone = binding.etRegisterPhone.getText().toString();
            String captcha = binding.etRegisterCaptcha.getText().toString();
            String password = binding.etRegisterPassword.getText().toString();
            String userName = binding.etRegisterName.getText().toString();
            if(pastPhone.isEmpty()||captcha.isEmpty()||password.isEmpty()||userName.isEmpty()){
                Toast.makeText(this,"上述内容不能为空！",Toast.LENGTH_SHORT).show();
            }
            registerViewModel.pastCaptcha(pastPhone,captcha,password,userName);
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

    public interface PastInformation {
        public void pastPhone(String phone);
        public void pastCaptcha(String phone,String captcha,String password,String userName);

    }
}