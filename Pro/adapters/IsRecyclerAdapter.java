package com.helloants.helloants.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.ModifyDelete;
import com.helloants.helloants.adapters.viewholder.IsRecyclerViewHolder;
import com.helloants.helloants.data.type.ISType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by park on 2016-04-21.
 */
public class IsRecyclerAdapter extends RecyclerView.Adapter<IsRecyclerViewHolder> {
    public ArrayList<ISType> mList;
    ViewGroup mViewGroup;
    RecyclerView mRecyclerView;
    Context mContext;
    int size;

    public IsRecyclerAdapter(Context context, ArrayList<ISType> list, RecyclerView view) {
        mContext = context;
        mList = list;
        mRecyclerView = view;
        size = mList.size();
    }

    @Override
    public IsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_is_list, parent, false);
        mViewGroup = mainGroup;
        mainGroup.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        IsRecyclerViewHolder viewHolder = new IsRecyclerViewHolder(mainGroup);
        mainGroup.setOnClickListener(new MyOnClickListener());

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(IsRecyclerViewHolder holder, int position) {
        ISType type = mList.get(position);
        if (type.mType.equals("date")) {
            TextView date = (TextView) mViewGroup.findViewById(R.id.txv_date_is_item_list);
            TextView sum = (TextView) mViewGroup.findViewById(R.id.txv_sum_is_item_list);
            date.setText(type.mWhere + "일");
            sum.setText("지출 : " + String.format("%,d", Integer.parseInt(type.mPrice)) + "원");

            ((RelativeLayout) mViewGroup.findViewById(R.id.rlay_img_and_frag_is_item_list)).setVisibility(View.GONE);
            ((RelativeLayout) mViewGroup.findViewById(R.id.rlay_left_and_right_is_item_list)).setVisibility(View.GONE);
        } else {
            ((RelativeLayout) mViewGroup.findViewById(R.id.rlay_date_and_sum_is_item_list)).setVisibility(View.GONE);

            holder.txtLeftCha.setText(type.mLeft);
            holder.txtWhere.setText(type.mWhere);
            holder.txtPrice.setText(String.format("%,d", Integer.parseInt(type.mPrice)) + "원");
            holder.txtTime.setText(new SimpleDateFormat("a hh:mm").format(type.mDate));
            holder.txtRightDea.setText(type.mRight);

            switch (type.mType) {
                case "credit":
                    holder.imgIcon.setImageResource(R.drawable.ic_credit);
                    break;
                case "check":
                    holder.imgIcon.setImageResource(R.drawable.ic_check);
                    break;
                case "income":
                    holder.imgIcon.setImageResource(R.drawable.ic_income);
                    break;
                case "cashExpend":
                    holder.imgIcon.setImageResource(R.drawable.ic_cash);
                    break;
                case "loan":
                    holder.imgIcon.setImageResource(R.drawable.ic_loan);
                    break;
                case "repay":
                    holder.imgIcon.setImageResource(R.drawable.ic_repay);
                    break;
                case "lend":
                    holder.imgIcon.setImageResource(R.drawable.ic_lend);
                    break;
                case "receiveLend":
                    holder.imgIcon.setImageResource(R.drawable.ic_receive);
                    break;
                case "house":
                    holder.imgIcon.setImageResource(R.drawable.ic_house);
                    break;
                case "save":
                    holder.imgIcon.setImageResource(R.drawable.ic_save);
                    break;
                case "sell":
                    holder.imgIcon.setImageResource(R.drawable.ic_sell);
                    break;
                case "car":
                    holder.imgIcon.setImageResource(R.drawable.ic_car);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mList == null) ? 0 : size;
    }

    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int itemPosition = mRecyclerView.getChildPosition(v);

            String part = mList.get(itemPosition).mPart;
            if(part == null) {

            } else if (part.equals("first")) {
                Snackbar.make(v, "초기값은 마이페이지에서 수정, 삭제해 주세요.", Snackbar.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(mContext, ModifyDelete.class);
                ISType type = mList.get(itemPosition);
                intent.putExtra("date", type.mDate);
                intent.putExtra("left", type.mLeft);
                intent.putExtra("right", type.mRight);
                intent.putExtra("part", type.mPart);
                intent.putExtra("type", type.mType);
                intent.putExtra("price", type.mPrice);
                intent.putExtra("where", type.mWhere);
                intent.putExtra("phoneNum", type.mPhoneNum);
                intent.putExtra("cardName", type.mCardName);

                mContext.startActivity(intent);
            }
        }
    }
}