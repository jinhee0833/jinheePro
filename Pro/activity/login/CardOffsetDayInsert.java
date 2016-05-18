package com.helloants.helloants.activity.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.db.member.MemberDB;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;
import com.mongodb.BasicDBObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CardOffsetDayInsert extends AppCompatActivity {
    private ArrayList<DatePicker> pikerList;
    private String[] cardN;
    private ArrayList<String> cardNameList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_offset_day_insert);
        LinearLayout root = (LinearLayout)findViewById(R.id.linear_cardoffset);
        //디비서 카드가져오기
        final Set myCardSet = MemberDB.INSTANCE.myCardFind();

        if (myCardSet == null || myCardSet.isEmpty()){
            Intent intent = new Intent(CardOffsetDayInsert.this,BSInsertChoice.class);
            startActivity(intent);
        }else {
            //카드 정산일 집어넣을 셋
            final Set cardOffDateSet = new HashSet();

            cardNameList = new ArrayList<>();
            pikerList = new ArrayList<>();
            for(Object card : myCardSet){
                String cardName = String.valueOf(card);
                cardN = cardName.split("~");
                if(cardN[1].equals("credit")){
                    LinearLayout layout = new LinearLayout(this);

                    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.card_offset_insert, layout);


                    //카드이름만 추출해서 넣기
                    TextView tvxCardName = (TextView)v.findViewById(R.id.txv_card_name);
                    tvxCardName.setText(cardN[0]);
                    cardNameList.add(cardN[0]);


                    DatePicker datePicker = (DatePicker)v.findViewById(R.id.datepick_carddate_cardoffset);
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

            }


            //다음 버튼 누르면 디비에 입력
            Button nextBtn = (Button)findViewById(R.id.btn_next_cardoffset);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < pikerList.size(); i++) {
                        cardOffDateSet.add(cardNameList.get(i) + "~" + pikerList.get(i).getDayOfMonth());
                    }

                    MemberDB.INSTANCE.init(CardOffsetDayInsert.this);
                    new Thread() {
                        @Override
                        public void run() {
                            //데이트 픽커에서 가지고온 날짜
                            BasicDBObject user = new BasicDBObject("cardOffsetDay", cardOffDateSet);

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

                    Intent intent = new Intent(CardOffsetDayInsert.this, BSInsertChoice.class);
                    startActivity(intent);
                }
            });
        }
    }
}
