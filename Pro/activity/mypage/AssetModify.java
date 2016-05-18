package com.helloants.helloants.activity.mypage;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.login.AssetInsert;
import com.helloants.helloants.adapters.AssetModifyAdapter;
import com.helloants.helloants.data.type.BSType;
import com.helloants.helloants.db.bs.BsDB;
import com.helloants.helloants.db.bs.BsItem;

import java.util.ArrayList;


public class AssetModify extends AppCompatActivity {
    private Button mAddassetBtn;
    private ArrayList<BSType> mList;
    private boolean mIsBs;
    private Button mAssetInsertBtn;
    BackPressCloseHandler backPressCloseHandler;
    AssetModifyAdapter assetModifyAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_modify);
        Thread ethread = new Thread() {
            @Override
            public void run() {
                mIsBs = BsItem.INSTANCE.checkAsset();
            }
        };

        ethread.start();
        try {
            ethread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 가계부 입력을 한 사람
        if (mIsBs) {
            setContentView(R.layout.activity_asset_modify);

            //툴바 타이틀 텍스트뷰
            TextView txvTitle = (TextView) findViewById(R.id.txv_title_aif);
            txvTitle.setText("자산 초기값 입력");

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_aif);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AssetModify.this.onBackPressed();
                }
            });

            final ListView list = (ListView) findViewById(R.id.list_asset_assetinsertfrag);
            assetModifyAdapter = new AssetModifyAdapter();

            //디비에 있는 자산항목 가져와서 뿌리기
            mList = new ArrayList<BSType>();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    BsDB.INSTANCE.firstAssetFind(mList);
                }
            };

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assetModifyAdapter.setList(mList);
            assetModifyAdapter.setTag("asset");
            list.setAdapter(assetModifyAdapter);


            //자산항목 추가 버튼 눌렀을때 얼럿다이얼로그
            LayoutInflater inflater = getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.asset_modify_dialog, null);
            mAddassetBtn = (Button) findViewById(R.id.btn_addasset_assetinsertfrag);
            mAddassetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AssetModify.this)
                            .setTitle("자산항목 추가")
                            .setView(dialogView)
                            .setPositiveButton("추가", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //추가 버튼 눌렀을때
                                    EditText editText = (EditText) dialogView.findViewById(R.id.dialog_edit);
                                    String name = editText.getText().toString();

                                    EditText editTextPrice = (EditText) dialogView.findViewById(R.id.dialog_edit_price);
                                    String price = editTextPrice.getText().toString();

                                    RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.dialog_rg);
                                    int checkedId = radioGroup.getCheckedRadioButtonId();
                                    String type = "";
                                    switch (checkedId) {
                                        case R.id.dialog_rb_save:
                                            type = "save";
                                            break;
                                        case R.id.dialog_rb_house:
                                            type = "house";
                                            break;
                                        case R.id.dialog_rb_income:
                                            type = "income";
                                            break;
                                        case R.id.dialog_rb_car:
                                            type = "car";
                                            break;
                                        case R.id.dialog_rb_etc:
                                            type = "etc";
                                            break;
                                    }

                                    String firstPlot = "asset";

                                    if (editText.equals("") || editTextPrice.equals("") || checkedId == 0) {
                                        Snackbar.make(v, "내용을 입력해 주세요.", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        mList.add(new BSType(name + "+", Long.parseLong(price)));
                                        //디비에 추가
                                        Log.v("자산항목추가", name + " " + price + " " + type + " " + firstPlot);
                                        BsDB.INSTANCE.newFirstAssetDebt(price, type, name + "+", firstPlot);
                                        assetModifyAdapter.setList(mList);
                                        list.setAdapter(assetModifyAdapter);
                                        ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                                    }
                                }
                            });
                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                        }
                    });
                    alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                        }
                    });
                    AlertDialog dialog = alertDialog.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            });

        } else {
            // 가계부 입력이 안 된 사람
            setContentView(R.layout.activity_asset_modify2);

            //툴바 타이틀 텍스트뷰
            TextView txvTitle = (TextView) findViewById(R.id.txv_title_aif2);
            txvTitle.setText("자산 초기값 입력");

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_aif2);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AssetModify.this.onBackPressed();
                }
            });

            mAssetInsertBtn = (Button) findViewById(R.id.btn_insertasset_assetinsertfrag);
            mAssetInsertBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent a = new Intent(AssetModify.this, AssetInsert.class);
                    startActivity(a);
                }
            });
        }

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    private class BackPressCloseHandler {
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            assetModifyAdapter.refresh();
            activity.finish();
        }
    }
}