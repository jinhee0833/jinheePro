package com.helloants.helloants;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.helloants.helloants.activity.MainActivity;
import com.helloants.helloants.activity.login.LoginActivity;
import com.helloants.helloants.data.DeviceSize;
import com.helloants.helloants.data.SMS.SMSReader;
import com.helloants.helloants.data.network.GetNetState;
import com.helloants.helloants.db.content.ContentDB;
import com.helloants.helloants.db.content.NoticeDB;
import com.helloants.helloants.db.member.MemberDB;
import com.helloants.helloants.db.mypage.RequestDB;
import com.helloants.helloants.db.mypage.ScrapDB;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;


public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        DeviceSize.init(LoadingActivity.this);
        MemberDB.INSTANCE.init(LoadingActivity.this);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    SMSReader.INSTANCE.init(LoadingActivity.this);
                    ContentDB.INSTANCE.onlyCall();
                    NoticeDB.INSTANCE.settingImg();
                    ScrapDB.INSTANCE.onlyCall();
                    RequestDB.INSTANCE.onlyCall();
                } catch (ExceptionInInitializerError e) {}
            }
        };
        thread.start();

        try {
            GetNetState.INSTANCE.checkNetwork(LoadingActivity.this);
        } catch(NullPointerException e) {
            new AlertDialog.Builder(LoadingActivity.this)
                    .setTitle("인터넷 연결 오류")
                    .setMessage("인터넷 연결 중 문제가 발생했습니다.\n" +
                            "연결을 확인하고 다시 실행해주세요")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoadingActivity.this.finish();
                        }
                    })
                    .show();
        }

        if(GetNetState.INSTANCE.mWifi) {
            Handler hd = new Handler();
            hd.postDelayed(new splashhandler(), 2000);
        }
        else if(GetNetState.INSTANCE.mMobile) {
            Handler hd = new Handler();
            hd.postDelayed(new splashhandler(), 2000);
        } else {
            new AlertDialog.Builder(LoadingActivity.this)
                    .setTitle("인터넷 연결 오류")
                    .setMessage("인터넷이 연결되어 있지 않습니다.\n" +
                            "연결을 확인하고 다시 실행해주세요")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoadingActivity.this.finish();
                        }
                    }).show();
        }
    }

    private class splashhandler implements Runnable {
        public void run() {
            String email = "";

            try {
                email = Cryptogram.Decrypt(LoginData.mEmail);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (email.equals("")) {
                startActivity(new Intent(getApplication(), LoginActivity.class));
                LoadingActivity.this.finish();
            } else {
                startActivity(new Intent(getApplication(), MainActivity.class));
                LoadingActivity.this.finish();
            }
        }
    }
}