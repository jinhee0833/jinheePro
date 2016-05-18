package com.helloants.helloants.activity.mypage;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.data.constant.Icon;
import com.helloants.helloants.db.mypage.ProfileDB;


public class Profile extends AppCompatActivity {
    private EditText mPresentPW;
    private EditText mExchangePW;
    private EditText mExchangePWConfirm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView)findViewById(R.id.txv_title_pro);
        txvTitle.setText("프로필");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton)findViewById(R.id.img_btn_pro);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile.this.onBackPressed();
            }
        });

        Typeface fontFamily = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/fontawesome.ttf");
        TextView name = (TextView) findViewById(R.id.profile_name);
        TextView email = (TextView) findViewById(R.id.profile_email);
        TextView gender = (TextView) findViewById(R.id.profile_gender);
        TextView birth = (TextView) findViewById(R.id.profile_birth);
        TextView nameIcon = (TextView) findViewById(R.id.profile_name_icon);
        TextView emailIcon = (TextView) findViewById(R.id.profile_email_icon);
        TextView genderIcon = (TextView)findViewById(R.id.profile_gender_icon);
        TextView birthIcon = (TextView) findViewById(R.id.profile_birth_icon);
        TextView nameAngleIcon = (TextView) findViewById(R.id.profile_name_icon_angle);
        TextView emailAngleIcon = (TextView) findViewById(R.id.profile_email_icon_angle);
        TextView genderAngleIcon = (TextView) findViewById(R.id.profile_gender_icon_angle);
        TextView birthAngleIcon = (TextView) findViewById(R.id.profile_birth_icon_angle);

        nameIcon.setTypeface(fontFamily);
        emailIcon.setTypeface(fontFamily);
        genderIcon.setTypeface(fontFamily);
        birthIcon.setTypeface(fontFamily);
        nameAngleIcon.setTypeface(fontFamily);
        emailAngleIcon.setTypeface(fontFamily);
        genderAngleIcon.setTypeface(fontFamily);
        birthAngleIcon.setTypeface(fontFamily);

        nameIcon.setText(Icon.BARCODE);
        emailIcon.setText(Icon.ENVELOPE_O);
        genderIcon.setText(Icon.VENUS_MARS);
        birthIcon.setText(Icon.BIRTHDAY_CAKE);
        nameAngleIcon.setText(Icon.ANGLE_DOUBLE_RIGHT);
        emailAngleIcon.setText(Icon.ANGLE_DOUBLE_RIGHT);
        genderAngleIcon.setText(Icon.ANGLE_DOUBLE_RIGHT);
        birthAngleIcon.setText(Icon.ANGLE_DOUBLE_RIGHT);

        ProfileDB.INSTANCE.settingData();
        name.setText(" " + ProfileDB.INSTANCE.getUserDate().mName);
        email.setText(" " + ProfileDB.INSTANCE.getUserDate().mEmail);
        gender.setText(" " + ProfileDB.INSTANCE.getUserDate().mGender);
        birth.setText(" " + ProfileDB.INSTANCE.getUserDate().mBirth);


    }


}
