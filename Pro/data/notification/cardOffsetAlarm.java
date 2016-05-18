package com.helloants.helloants.data.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.helloants.helloants.db.bs.BsDB;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kingherb on 2016-04-20.
 */
public class cardOffsetAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String cardName = intent.getDataString();
        Log.v("알람1", cardName);

        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Boolean push =  pref.getBoolean("push", true);
        if(push == true) {
            NotificationFormat.NotificationPush(context,"헬로앤츠",cardName + "카드 정산일 입니다. 자동 처리됩니다.",cardName + "카드 정산일 입니다. 자동 처리됩니다.");
            NotificationFormat.NotificationPopup(context, cardName + "카드 정산일 입니다. 자동 처리됩니다.");
        }
        ArrayList cardOffsetPrice = BsDB.INSTANCE.cardOffsetPrice(cardName);
        Date date = (Date) cardOffsetPrice.get(0);
        int price = (int) cardOffsetPrice.get(1);
        Log.v("알람5",String.valueOf(date));
        Log.v("알람6",String.valueOf(price));

        if(price!=0)BsDB.INSTANCE.newIsInsert(String.valueOf(price),"카드값 정산",date,"repay","self",cardName+"카드-","현금-");

    }
}
