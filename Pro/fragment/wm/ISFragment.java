package com.helloants.helloants.fragment.wm;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.adapters.IsRecyclerAdapter;
import com.helloants.helloants.data.type.ISType;
import com.helloants.helloants.db.bs.BsDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ISFragment extends Fragment {
    View v;
    String[] tabNames;
    TabLayout tabLayout;
    NestedScrollView scrollView;
    int mCount;
    int tabNamesSize;
    int mDaySum;
    int mMonthIncomeSum;
    int mMonthSum;
    public Toolbar toolbar;
    Map<String, ArrayList<ISType>> mDateMap;
    ArrayList<String> mDaySumList;
    ArrayList allList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_is, container, false);
        scrollView = (NestedScrollView) v.findViewById(R.id.scroll_isfrag);

        initData();
        initTap();

        final RecyclerView mView = (RecyclerView) v.findViewById(R.id.rv_is_monthfrag);
        mView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        mView.setNestedScrollingEnabled(false);
        mView.setHasFixedSize(true);
        mView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        final View header = inflater.inflate(R.layout.header_islist, null, false);
        ((LinearLayout) v.findViewById(R.id.llay_recycler_is)).addView(header);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String position = "";
                try {
                    position = tabNames[tabLayout.getSelectedTabPosition()];
                } catch (ArrayIndexOutOfBoundsException e) {
                }

                ArrayList<ISType> list = (mDateMap.get(position) == null) ?
                        new ArrayList<ISType>() :
                        mDateMap.get(position);

                final Comparator<ISType> comparator = new Comparator<ISType>() {
                    @Override
                    public int compare(ISType lhs, ISType rhs) {
                        Date d1;
                        Date d2;

                        d1 = lhs.mDate;
                        d2 = rhs.mDate;

                        return (d1.getTime() > d2.getTime() ? -1 : 1);
                    }
                };
                Collections.sort(list, comparator);

                mDaySumList = new ArrayList<String>();
                mDaySum = mMonthSum = mMonthIncomeSum = 0;
                int size = list.size();
                for (int j = 0; j < size; j++) {
                    String type1 = list.get(j).mType;
                    if (type1.equals("credit") || type1.equals("check") || type1.equals("cashExpend")) {
                        mMonthSum += Integer.parseInt(list.get(j).mPrice);

                        try {
                            if (j == 0 || list.get(j).mDate.getDate() == list.get(j - 1).mDate.getDate()) {
                                mDaySum += Integer.parseInt(list.get(j).mPrice);
                            } else {
                                mDaySumList.add(String.valueOf(mDaySum));
                                mDaySum = 0;
                                mDaySum += Integer.parseInt(list.get(j).mPrice);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            mDaySum += Integer.parseInt(list.get(j).mPrice);
                        }
                    } else if (type1.equals("income")) {
                        mMonthIncomeSum += Integer.parseInt(list.get(j).mPrice);
                    }
                }
                mDaySumList.add(String.valueOf(mDaySum));

                int count = 0;
                allList = new ArrayList<ISType>();
                for (int i = 0; i < size; ++i) {
                    String type1 = list.get(i).mType;

                    if (type1.equals("credit") || type1.equals("check") || type1.equals("cashExpend")) {
                        if (i == 0 || list.get(i).mDate.getDate() != list.get(i - 1).mDate.getDate()) {
                            ISType type = new ISType();
                            type.mPrice = mDaySumList.get(count++);
                            type.mWhere = String.valueOf(list.get(i).mDate.getDate());
                            type.mType = "date";
                            allList.add(type);
                        }
                    } else if (type1.equals("income")) {
                        if (i == 0 || list.get(i).mDate.getDate() != list.get(i - 1).mDate.getDate()) {
                            ISType type = new ISType();
                            type.mPrice = mDaySumList.get(count++);
                            type.mWhere = String.valueOf(list.get(i).mDate.getDate());
                            type.mType = "date";
                            allList.add(type);
                        }
                    }
                    allList.add(list.get(i));
                }

                IsRecyclerAdapter IsAdapter = new IsRecyclerAdapter(getActivity(), allList, mView);
                mView.setAdapter(IsAdapter);
                IsAdapter.notifyDataSetChanged();

                TextView textviewTime = (TextView) header.findViewById(R.id.txv_time_monthisfrag);
                TextView textViewSum = (TextView) header.findViewById(R.id.txv_sum_monthisfrag);
                TextView textViewIncomeSum = (TextView) header.findViewById(R.id.txv_incomesum_monthisfrag);

                textviewTime.setText(position);
                textViewSum.setText("지출 : " + String.format("%,d", mMonthSum) + "원");
                textViewIncomeSum.setText("수입 : " + String.format("%,d", mMonthIncomeSum) + "원");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return v;
    }

    private void initTap() {
        tabLayout = (TabLayout) v.findViewById(R.id.tab_tablayout_is);
        tabNamesSize = tabNames.length;
        for (int i = 0; i < tabNamesSize; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabNames[i]));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tabLayout.getTabAt(tabNamesSize - 1).select();
            }
        }, 100);
    }

    private void initData() {
        mDateMap = BsDB.INSTANCE.monthDataFind(getActivity());
        mCount = mDateMap.size();

        Iterator<String> iter = mDateMap.keySet().iterator();
        List list = new ArrayList();

        while (iter.hasNext()) {
            String str = iter.next();

            if (str.length() == 8) {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.insert(6, 0);
                str = sb.toString();
            }
            list.add(str);
        }

        Collections.sort(list);
        iter = list.iterator();

        int i = 0;
        List listSort = new ArrayList();
        while (iter.hasNext()) {
            String str = iter.next();

            if (str.charAt(6) == '0') {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.deleteCharAt(6);
                str = sb.toString();
            }
            listSort.add(str);
        }

        int size = listSort.size();
        tabNames = new String[size];
        for (int count = 0; count < size; ++count) {
            tabNames[count] = listSort.get(count).toString();
        }
    }
}