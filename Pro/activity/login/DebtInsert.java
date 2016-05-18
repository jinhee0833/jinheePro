package com.helloants.helloants.activity.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.helloants.helloants.R;
import com.helloants.helloants.adapters.AssetInsertAdapter;
import com.helloants.helloants.data.type.BSType;
import com.helloants.helloants.db.bs.BsDB;
import com.helloants.helloants.db.bs.BsItem;
import com.helloants.helloants.db.member.MemberDB;

import java.util.ArrayList;
import java.util.Set;

public class DebtInsert extends AppCompatActivity {
    private Button mAddDebtBtn;
    private Button mFinishBtn;
    ArrayList<BSType> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_insert);

        final ListView list = (ListView) findViewById(R.id.list_debt_debtinsertactivity);
        final AssetInsertAdapter debtAdapter = new AssetInsertAdapter();

        mList = new ArrayList<BSType>();
        mList.add(new BSType("부동산 대출+","loan"));
        mList.add(new BSType("신용 대출+","loan"));
        mList.add(new BSType("학자금 대출+","loan"));

        Set myCardSet = MemberDB.INSTANCE.myCardFind();
        for(Object card : myCardSet){
            String cardName = String.valueOf(card);
            String[] cardN = cardName.split("~");
            if(cardName.contains("credit")){
                mList.add(new BSType(cardN[0]+"카드+","loan"));
            }
        }

        debtAdapter.setList(mList);

        list.setAdapter(debtAdapter);

        //빚항목 추가 눌렀을때 얼럿다이얼로그
        mAddDebtBtn = (Button) findViewById(R.id.btn_adddebt_debtinsertactivity);
        mAddDebtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText DebtName = new EditText(DebtInsert.this);
                new AlertDialog.Builder(DebtInsert.this)
                        .setTitle("부채 항목 추가")
                        .setMessage("새로운 부채를 추가해 주세요")
                        .setView(DebtName)
                        .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //추가 버튼 눌렀을때
                                String name = DebtName.getText().toString();
                                mList.add(new BSType(name+"+","loan"));
                                debtAdapter.setList(mList);
                                list.setAdapter(debtAdapter);
                            }
                        }).show();
            }
        });


        //완료버튼 클릭시
        mFinishBtn = (Button) findViewById(R.id.btn_send_debtinsertactivity);
        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //넘어가면서 DB입력
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        BsDB.INSTANCE.assetInsert(mList, "debt");
                        BsItem.INSTANCE.insertDebt(mList);
                        BsItem.INSTANCE.insert();
                    }
                };

                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //메인액티비티로 넘어가기
                Intent MainActivity = new Intent(DebtInsert.this, com.helloants.helloants.activity.MainActivity.class);
                startActivity(MainActivity);
            }
        });


    }
}
