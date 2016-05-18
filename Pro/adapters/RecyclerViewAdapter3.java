package com.helloants.helloants.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.content.ContentDetailActivity;
import com.helloants.helloants.adapters.viewholder.RecyclerViewHolder;
import com.helloants.helloants.data.ContentImage;
import com.helloants.helloants.data.DeviceSize;

import java.util.List;

/**
 * Created by kingherb on 2016-04-13.
 */
public class RecyclerViewAdapter3 extends RecyclerView.Adapter<RecyclerViewHolder> {
    private List<ContentImage> contentimage;
    private Context context;
    RecyclerView mRecyclerView;

    public RecyclerViewAdapter3(Context context, List<ContentImage> contentimage,RecyclerView mRecyclerView) {
        this.contentimage = contentimage;
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.content_image_list,parent,false);
        mainGroup.setLayoutParams(new ViewGroup.LayoutParams(DeviceSize.mWidth/2,DeviceSize.mWidth/2));
        RecyclerViewHolder listHolder = new RecyclerViewHolder(mainGroup);
        mainGroup.setOnClickListener(new MyOnClickListener());
        return listHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        final ContentImage content = contentimage.get(position);
        RecyclerViewHolder mainHolder = holder;

        mainHolder.title.setText(content.getmSubTitle());
        mainHolder.imageView.setImageBitmap(content.getmBitmap());
    }

    @Override
    public int getItemCount() {
        return (null != contentimage ? contentimage.size():0);
    }

    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int itemPosition = mRecyclerView.getChildPosition(v);

            Intent ContentActivity = new Intent(context, ContentDetailActivity.class);
            ContentActivity.putExtra("id", contentimage.get(itemPosition).mId);
            ContentActivity.putExtra("subTitle", contentimage.get(itemPosition).mSubTitle);
            ContentActivity.putExtra("filePath", contentimage.get(itemPosition).mFilePath);
            context.startActivity(ContentActivity);
        }
    }
}
