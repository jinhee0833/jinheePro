package com.helloants.helloants.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.helloants.helloants.R;
import com.helloants.helloants.data.SMS.SMSReader;
import com.helloants.helloants.db.member.MemberDB;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;
import com.mongodb.BasicDBObject;

import java.lang.reflect.Field;

public class SalaryInsert extends AppCompatActivity {
    private Button mNextBtn;
    private DatePicker mSalarydateEdit;
    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_insert);
        mSalarydateEdit = (DatePicker) findViewById(R.id.datepick_salarydate_salaryinsert);
        try {
            Field f[] = mSalarydateEdit.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mYearPicker")  || field.getName().equals("mYearSpinner")|| field.getName().equals("mMonthPicker")|| field.getName().equals("mMonthSpinner")) {
                    field.setAccessible(true);
                    Object dayPicker = new Object();
                    dayPicker = field.get(mSalarydateEdit);
                    ((View) dayPicker).setVisibility(View.GONE);
                }
            }
        } catch (SecurityException e) {}
        catch (IllegalArgumentException e) {}
        catch (IllegalAccessException e) {}

        //기존문자 넣기
        Thread threadSMS = new Thread() {
            @Override
            public void run() {
                SMSReader.INSTANCE.SMSList(SalaryInsert.this);
            }
        };
        threadSMS.start();
        try {
            threadSMS.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mNextBtn = (Button)findViewById(R.id.btn_next_salaryinsert);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MemberDB.INSTANCE.init(SalaryInsert.this);
                new Thread() {
                    @Override
                    public void run() {
                        BasicDBObject user = new BasicDBObject("salaryDate", mSalarydateEdit.getDayOfMonth());

                        String email = "";
                        try {
                            email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                new BasicDBObject("$set", user));
                    }
                }.start();

                Intent intent = new Intent(SalaryInsert.this, CardOffsetDayInsert.class);
                startActivity(intent);
            }
        });

        backPressCloseHandler = new BackPressCloseHandler(this);
    }
    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
    private class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                toast.cancel();
            }
        }

        private void showGuide() {
            toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
