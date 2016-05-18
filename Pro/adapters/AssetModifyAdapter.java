package com.helloants.helloants.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.helloants.helloants.R;
import com.helloants.helloants.activity.MainActivity;
import com.helloants.helloants.data.type.BSType;
import com.helloants.helloants.db.bs.BsDB;
import com.helloants.helloants.db.bs.BsItem;
import com.helloants.helloants.fragment.wm.BSFragment;

import java.util.ArrayList;
import java.util.Date;


public class AssetModifyAdapter extends BaseAdapter {
    // 문자열을 보관 할 ArrayList
    private ArrayList<BSType> m_List;
    private int tagIndex;
    private String tag;
    private String left;
    private String right;
    private String where;
    private Date date;
    private String part;
    private String type;
    private String price;

    // 생성자
    public AssetModifyAdapter() {
        m_List = new ArrayList<BSType>();
    }

    public void setList(ArrayList<BSType> list) {
        m_List = list;
    }

    public ArrayList<BSType> getList() {
        return m_List;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // 현재 아이템의 수를 리턴
    @Override
    public int getCount() {
        return m_List.size();
    }

    // 현재 아이템의 오브젝트를 리턴, Object를 상황에 맞게 변경하거나 리턴받은 오브젝트를 캐스팅해서 사용
    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    // 아이템 position의 ID 값 리턴
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 출력 될 아이템 관리
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        final ViewGroup aparent = parent;

        if (convertView == null) {
            // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_bs_list, parent, false);
        }
        TextView text = (TextView) convertView.findViewById(R.id.txv_name_list);
        String name = m_List.get(position).getName();
        text.setText(name.substring(0,name.length()-1));

        TextView values = (TextView) convertView.findViewById(R.id.txv_price_list);
        values.setText(String.format("%,d", m_List.get(position).getValue()));

        final View vv = convertView;

        //리스트 아이템을 터치 했을 때 이벤트 발생
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.alert_firstvalue_modify);
                dialog.setTitle("초기값 수정");

                final EditText AssetName = (EditText) dialog.findViewById(R.id.alert_firstvalue_title);
                String name = m_List.get(pos).getName();
                AssetName.setText(name.substring(0,name.length()-1));
                final EditText AssetPrice = (EditText) dialog.findViewById(R.id.alert_firstvalue_content);
                AssetPrice.setText(String.valueOf(m_List.get(pos).getValue()));
                AssetPrice.requestFocus();
                Button btnInsert = (Button) dialog.findViewById(R.id.alert_firstvaluet_insert);
                Button btnDelete = (Button) dialog.findViewById(R.id.alert_firstvalue_cancel);
                btnInsert.setText("수정");
                btnDelete.setText("삭제");

                final Dialog DIALOG = dialog;

                //삭제버튼 눌렀을때
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if(tag.equals("asset")){
                            tagIndex = 0;
                            left = "상계처리+";
                            String name = m_List.get(pos).getName();
                            right = name.substring(0,name.length()-1) + "-";
                        }else if(tag.equals("debt")){
                            tagIndex = 1;
                            String name = m_List.get(pos).getName();
                            left = name.substring(0,name.length()-1) + "-";
                            right = "상계처리+";
                        }
                        where = "초기값 삭제";
                        date = new Date((new Date().getTime() / 1000) * 1000);
                        part = "offset";
                        type = "offset";
                        price = String.valueOf(BSFragment.mPriceMap.get(m_List.get(pos).getName()));

                        //삭제확인
                        new AlertDialog.Builder(context)
                                .setTitle("삭제")
                                .setMessage("정말로 삭제 하시겠습니까?")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //확인 버튼 눌렀을때
                                        BsDB.INSTANCE.newIsInsert(price, where, date, type, part, left, right);
                                        BsDB.INSTANCE.modifyFirstTypeToOffset(m_List.get(pos).getID());
                                        BsItem.INSTANCE.removeOne(m_List.get(pos).getName(), tagIndex);
                                        Snackbar.make(v, "삭제했습니다.", Snackbar.LENGTH_SHORT).show();
                                        remove(pos);
                                        notifyDataSetChanged();
                                    }
                                }).show();
                        DIALOG.dismiss();
                    }
                });

                //수정버튼 눌렀을때
                btnInsert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strName = AssetName.getText().toString()+"+";
                        final String price = AssetPrice.getText().toString();

                        if (strName.equals("") || price == null) {
                            Snackbar.make(v, "내용을 입력해 주세요.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            //추가 버튼 눌렀을때
                            String name = strName;
                            Long value = Long.parseLong(price);
                            Thread thread = new Thread(){
                                @Override
                                public void run(){
                                    BsDB.INSTANCE.firstModify(m_List.get(pos).getName(), strName, price, m_List.get(pos).getID(),tag);
                                }
                            };
                            thread.start();
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            m_List.get(pos).setName(name);
                            m_List.get(pos).setValue(value);

                            TextView names = (TextView)vv.findViewById(R.id.txv_name_list);
                            TextView values = (TextView) vv.findViewById(R.id.txv_price_list);
                            names.setText(m_List.get(pos).getName());
                            values.setText(String.format("%,d", m_List.get(pos).getValue()));
                            DIALOG.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

//        //리스트 아이템을 길게 터치 했을 떄 이벤트 발생
//        convertView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View v) {
//
//                return true;
//            }
//        });

        return convertView;
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(String _msg) {
        m_List.add(new BSType(_msg));
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        m_List.remove(_position);
    }

    public void refresh() {
        try {
            if(tag.equals("asset")) {
                BSFragment.mAssetList = m_List;
                BSFragment.mAssetAdapter.setList(m_List);
//                BSFragment.mAssetAdapter.notifyDataSetChanged();
            } else {
                BSFragment.mDebtList = m_List;
                BSFragment.mDebtAdapter.setList(m_List);
//                BSFragment.mDebtAdapter.notifyDataSetChanged();
            }
            MainActivity.vpPager.getAdapter().notifyDataSetChanged();
        } catch(Exception e) {}
    }
}
