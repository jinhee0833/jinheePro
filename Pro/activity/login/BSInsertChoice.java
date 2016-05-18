package com.helloants.helloants.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.helloants.helloants.R;
import com.helloants.helloants.data.type.BSType;
import com.helloants.helloants.db.bs.BsDB;
import com.helloants.helloants.db.bs.BsItem;
import com.helloants.helloants.db.member.MemberDB;

import java.util.ArrayList;
import java.util.Set;

public class BSInsertChoice extends AppCompatActivity {
    private Button mSkipBtn;
    private Button mInsertBtn;
    private ArrayList<BSType> mList;
    private ArrayList<BSType> mList2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bsinsert_choice);

        //건너뛰면서 기본값(0)넣기 클릭시
        mSkipBtn = (Button) findViewById(R.id.btn_skip_bsinsertchoice);
        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //자산기본값
                mList = new ArrayList();
                mList.add(new BSType("집값 혹은 보증금+", "house"));
                mList.add(new BSType("예·적금+", "save"));
                mList.add(new BSType("현금+", "income"));
                mList.add(new BSType("보험+", "save"));
                mList.add(new BSType("펀드+", "save"));
                mList.add(new BSType("주식+", "save"));
                mList.add(new BSType("자동차+", "car"));
                Set myCardSet = MemberDB.INSTANCE.myCardFind();

                for (Object card : myCardSet) {
                    String cardName = String.valueOf(card);
                    String[] cardN = cardName.split("~");
                    if (cardName.contains("check")) {
                        mList.add(new BSType(cardN[0] + "은행 계좌+", "save"));
                    }
                }
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        BsDB.INSTANCE.assetInsert(mList, "asset");
                        BsItem.INSTANCE.insertAsset(mList);
                    }
                };
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {}

                //부채기본값
                mList2 = new ArrayList<BSType>();
                mList2.add(new BSType("부동산 대출+", "loan"));
                mList2.add(new BSType("신용 대출+", "loan"));
                mList2.add(new BSType("학자금 대출+", "loan"));

                for (Object card : myCardSet) {
                    String cardName = String.valueOf(card);
                    String[] cardN = cardName.split("~");
                    if (cardName.contains("credit")) {
                        mList2.add(new BSType(cardN[0] + "카드+", "loan"));
                    }
                }

                Thread thread3 = new Thread() {
                    @Override
                    public void run() {
                        BsDB.INSTANCE.assetInsert(mList2, "debt");
                        BsItem.INSTANCE.insertDebt(mList2);
                        BsItem.INSTANCE.insert();
                    }
                };

                thread3.start();
                try {
                    thread3.join();
                } catch (InterruptedException e) {}

                Intent MainActivity = new Intent(BSInsertChoice.this, com.helloants.helloants.activity.MainActivity.class);
                startActivity(MainActivity);
            }


        });

        //입력하기버튼 클릭시
        mInsertBtn = (Button) findViewById(R.id.btn_insert_bsinsertchoice);
        mInsertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AssetInsert = new Intent(BSInsertChoice.this, AssetInsert.class);
                startActivity(AssetInsert);
            }
        });
    }
}