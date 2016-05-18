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

public class EmailSearch extends AppCompatActivity {
    private EditText mNameEdit;
    private EditText mBirthEdit;
    private Button mSendBtn;
    private Toast mNameToast;
    private Toast mBirthToast;
    private String mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_search);
        mSendBtn = (Button) findViewById(R.id.btn_send_eamilsearch);
        mNameEdit = (EditText) findViewById(R.id.edit_name_emailsearch);
        mBirthEdit = (EditText) findViewById(R.id.edit_birth_emailsearch);
        mNameToast = Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT);
        mBirthToast = Toast.makeText(this, "생년월일 입력해 주세요.", Toast.LENGTH_SHORT);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNameEdit.getText().toString().equals("")) {
                    mNameToast.show();
                } else if (mBirthEdit.getText().toString().equals("")) {
                    mBirthToast.show();
                } else {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            BasicDBObject user = new BasicDBObject("name", mNameEdit.getText().toString())
                                    .append("birth", mBirthEdit.getText().toString());
                            mResult = MemberDB.INSTANCE.emailSearch(user);
                        }
                    };

                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    new AlertDialog.Builder(EmailSearch.this)
                            .setTitle("E-MAIL은")
                            .setMessage(mResult + "입니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent LoginActivity = new Intent(EmailSearch.this, LoginActivity.class);
                                    startActivity(LoginActivity);
                                }
                            }).show();
                }
            }
        });
    }
}
