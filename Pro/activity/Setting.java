package com.helloants.helloants.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.login.LoginActivity;
import com.helloants.helloants.db.member.MemberDB;
import com.helloants.helloants.loading.WaitDlg;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;


public class Setting extends AppCompatActivity {
    private TextView mExPw;
    private TextView mAgree;
    private TextView mPersonnel;
    private EditText mExchangePW;
    private EditText mExchangePWConfirm;
    private TextView mLogout;
    private WaitDlg mWaitDlg;
    private Switch mPush;
    private View layout;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView)findViewById(R.id.txv_title_setting);
        txvTitle.setText("설정");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton)findViewById(R.id.img_btn_setting);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Setting.this.onBackPressed();
            }
        });



        mExPw = (TextView) findViewById(R.id.txv_exchange_pw_setting);
        mAgree = (TextView)findViewById(R.id.txv_agreement_setting);
        mPersonnel = (TextView) findViewById(R.id.txv_personnel_setting);
        mLogout = (TextView) findViewById(R.id.btn_logout_setting);
        mPush = (Switch) findViewById(R.id.push_setting);

        initLogoutBtn();
        initPwBtn();

        mAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Setting.this)
                        .setTitle("이용약관")
                        .setMessage("웹페이지로 이동합니다")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.helloants.com/rule1#0"));
                                intent.setPackage("com.android.chrome");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        mPersonnel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Setting.this)
                        .setTitle("개인정보 취급방침")
                        .setMessage("웹페이지로 이동합니다")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.helloants.com/rule2#1"));
                                intent.setPackage("com.android.chrome");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        SharedPreferences pref = Setting.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Boolean push =  pref.getBoolean("push", true);
        mPush.setChecked(push);

        mPush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences pref = Setting.this.getSharedPreferences("pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("push", mPush.isChecked());
                editor.commit();
            }
        });
    }

    private void initLogoutBtn() {
        String joinPath = "";

        try {
            joinPath = Cryptogram.Decrypt(LoginData.mJoinPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String JOINPATH = joinPath;
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Setting.this)
                        .setTitle("로그아웃")
                        .setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (JOINPATH.equals("general")) MemberDB.INSTANCE.logout();
                                else if (JOINPATH.equals("facebook"))
                                    MemberDB.INSTANCE.fbLogout();
                                else MemberDB.INSTANCE.kakaoLogout();

                                startActivity(new Intent(Setting.this, LoginActivity.class));
                                Setting.this.finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .show();
            }
        });
    }

    private void initPwBtn() {
        final EditText PRESENT_PW = (EditText) findViewById(R.id.present_pw);
        mExchangePW = (EditText) findViewById(R.id.exchange_pw);
        mExchangePWConfirm = (EditText) findViewById(R.id.exchange_pw_confirm);
        Button modify = (Button) findViewById(R.id.modify_pw);

        String email = "";

        try {
            email = Cryptogram.Decrypt(LoginData.mEmail);
        } catch (Exception e) {
        }

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWaitDlg = new WaitDlg(Setting.this, "Please Wait", "Loading...");
                mWaitDlg.start();

                String pPw = PRESENT_PW.getText().toString();
                String ePw = mExchangePW.getText().toString();
                String ePwC = mExchangePWConfirm.getText().toString();

                if (pPw.equals("") || ePw.equals("") || ePwC.equals("")) {
                    WaitDlg.stop(mWaitDlg);
                    Snackbar.make(v, "내용을 입력해 주세요.", Snackbar.LENGTH_SHORT).show();
                } else {
                    if (MemberDB.INSTANCE.confirmPW(pPw)) {
                        if (ePw.equals(ePwC)) {
                            MemberDB.INSTANCE.modifyPW(ePw);
                            WaitDlg.stop(mWaitDlg);
                            Snackbar.make(v, "비밀번호를 변경하였습니다.", Snackbar.LENGTH_SHORT).show();
                            PRESENT_PW.setText("");
                            mExchangePW.setText("");
                            mExchangePWConfirm.setText("");
                        } else {
                            WaitDlg.stop(mWaitDlg);
                            Snackbar.make(v, "비밀번호가 일치하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        WaitDlg.stop(mWaitDlg);
                        Snackbar.make(v, "비밀번호가 옳바르지 않습니다.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}