package com.helloants.helloants.activity.mypage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.MainActivity;
import com.helloants.helloants.db.member.MemberDB;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;
import com.mongodb.BasicDBObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EditSalary extends AppCompatActivity {

    private String[] splitData;
    private ArrayList<String> cardNameList;
    private ArrayList<DatePicker> pikerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_salary);
        LinearLayout root = (LinearLayout)findViewById(R.id.linear_cardoffset_editsalary);

        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView)findViewById(R.id.txv_title_editsalary);
        txvTitle.setText("월급날, 카드정산일 수정");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton)findViewById(R.id.img_btn_editsalary);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditSalary.this.onBackPressed();
            }
        });

        //월급날 입력
        int salaryDay = MemberDB.INSTANCE.salaryDayFind();
        final DatePicker datePicker1 = (DatePicker)findViewById(R.id.datepick_salarydate_editsalary);
        datePicker1.updateDate(2016,3,salaryDay);
        try {
            Field f[] = datePicker1.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equals("mYearPicker")  || field.getName().equals("mYearSpinner")|| field.getName().equals("mMonthPicker")|| field.getName().equals("mMonthSpinner")) {
                    field.setAccessible(true);
                    Object dayPicker = new Object();
                    dayPicker = field.get(datePicker1);
                    ((View) dayPicker).setVisibility(View.GONE);
                }
            }
        } catch (SecurityException e) {}
        catch (IllegalArgumentException e) {}
        catch (IllegalAccessException e) {}

        //카드 정산일 수정
        //이미 입력된 거 가져오기
        Set beforeCardOffset = MemberDB.INSTANCE.myCardOffsetFind();
        //새로 입력할 데이터 담을 셋
        final Set afterCardOffset = new HashSet();

        cardNameList = new ArrayList<>();
        pikerList = new ArrayList<>();

        for (Object set:beforeCardOffset){
            String data = String.valueOf(set);
            splitData = data.split("~");

            LinearLayout layout = new LinearLayout(this);

            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.card_offset_insert, layout);

            TextView tvxCardName = (TextView)v.findViewById(R.id.txv_card_name);
            tvxCardName.setText(splitData[0]+"카드");
            cardNameList.add(splitData[0]);

            DatePicker datePicker = (DatePicker)v.findViewById(R.id.datepick_carddate_cardoffset);
            datePicker.updateDate(2016,03, Integer.parseInt(splitData[1]));
            pikerList.add(datePicker);
            try {
                Field f[] = datePicker.getClass().getDeclaredFields();
                for (Field field : f) {
                    if (field.getName().equals("mYearPicker")  || field.getName().equals("mYearSpinner")|| field.getName().equals("mMonthPicker")|| field.getName().equals("mMonthSpinner")) {
                        field.setAccessible(true);
                        Object dayPicker = new Object();
                        dayPicker = field.get(datePicker);
                        ((View) dayPicker).setVisibility(View.GONE);
                    }
                }
            } catch (SecurityException e) {}
            catch (IllegalArgumentException e) {}
            catch (IllegalAccessException e) {}

            root.addView(layout);
        }

        //다음 버튼 누르면 디비에 입력
        Button modifyBtn = (Button)findViewById(R.id.btn_next_editsalary);
        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0 ; i < pikerList.size() ; i++){
                    afterCardOffset.add(cardNameList.get(i) + "~" + pikerList.get(i).getDayOfMonth());
                }

                MemberDB.INSTANCE.init(EditSalary.this);
                new Thread() {
                    @Override
                    public void run() {
                        //데이트 픽커에서 가지고온 날짜
                        BasicDBObject user = new BasicDBObject("cardOffsetDay", afterCardOffset).append("salaryDate",datePicker1.getDayOfMonth());

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

                Intent intent = new Intent(EditSalary.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
