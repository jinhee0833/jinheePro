package com.helloants.helloants.activity.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.helloants.helloants.R;
import com.helloants.helloants.db.member.MemberDB;
import com.mongodb.BasicDBObject;

public class PwSearch extends AppCompatActivity {
    private Button mSendBtn;
    private EditText mEmailEdit;
    private Toast mEmailToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pw_search);
        mSendBtn = (Button)findViewById(R.id.btn_send_pwsearch);
        mEmailEdit = (EditText)findViewById(R.id.edit_email_pwsearch);
        mEmailToast = Toast.makeText(this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT);

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmailEdit.getText().toString().equals("")) {
                    mEmailToast.show();
                } else {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            BasicDBObject user = new BasicDBObject("email", mEmailEdit.getText().toString());
                            MemberDB.INSTANCE.pwSearch(user);

                        }
                    };
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    new AlertDialog.Builder(PwSearch.this)
                            .setTitle("알림")
                            .setMessage("새로운 비밀번호가 이메일로 전송되었습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent LoginActivity = new Intent(PwSearch.this, LoginActivity.class);
                                    startActivity(LoginActivity);
                                }
                            }).show();
                }
            }
        });
    }
}
