package com.helloants.helloants.activity.mypage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.content.ContentDetailActivity;
import com.helloants.helloants.adapters.ImgListAdapter;
import com.helloants.helloants.data.ContentImage;
import com.helloants.helloants.data.DeviceSize;
import com.helloants.helloants.data.type.ContentType;
import com.helloants.helloants.db.content.ContentDB;
import com.helloants.helloants.db.mypage.ScrapDB;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by park on 2016-02-05.
 */
public class Scrap extends AppCompatActivity implements AbsListView.OnScrollListener {
    GridView list;
    List<ContentImage> contentImage;
    ArrayList<Bitmap> bitmap;
    private ArrayList<ContentType> mScrapList;
    ImgListAdapter imgListAdapter;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrap);

        //툴바 타이틀 텍스트뷰
        TextView txvTitle = (TextView)findViewById(R.id.txv_title_scrap);
        txvTitle.setText("스크랩");

        //툴바 이미지 백 버튼
        ImageButton btnBack = (ImageButton)findViewById(R.id.img_btn_scrap);
        btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scrap.this.onBackPressed();
            }
        });

        mScrapList = ScrapDB.INSTANCE.mScrapList;
        try {
            final Thread iThread = new Thread() {

                @Override
                public void run() {
                    try {
                        bitmap = new ArrayList();
                        for (int i = 0; i < mScrapList.size(); ++i) {
                            URL url = new URL(mScrapList.get(i).mFilePath);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            Bitmap preBitmap = BitmapFactory.decodeStream(is);
                            Bitmap bit = Bitmap.createScaledBitmap(preBitmap,
                                    DeviceSize.mWidth / 2,
                                    DeviceSize.mWidth / 2,
                                    true);
                            bitmap.add(bit);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            };
            iThread.start();
            iThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        list = (GridView) findViewById(R.id.listView);
        contentImage = new ArrayList<ContentImage>();
        Iterator<ContentType> iter = mScrapList.iterator();
        int i = 0;
        while(iter.hasNext()) {
            contentImage.add(new ContentImage(iter.next().mSubTitle, bitmap.get(i++)));
        }
        imgListAdapter = new ImgListAdapter(Scrap.this, R.layout.content_image_list, contentImage,6,2);
        list.setAdapter(imgListAdapter);
        list.setOnScrollListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent ContentActivity = new Intent(Scrap.this, ContentDetailActivity.class);
                int contentID = mScrapList.get(position).mID;
                ContentActivity.putExtra("id", contentID);
                ContentActivity.putExtra("subTitle", ContentDB.INSTANCE.getSubTitle(contentID));
                ContentActivity.putExtra("filePath", mScrapList.get(position).mFilePath);
                startActivity(ContentActivity);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(firstVisibleItem + visibleItemCount == totalItemCount && !imgListAdapter.endReached()){
            boolean noMoreToShow = imgListAdapter.showMore();
        }
    }
}
