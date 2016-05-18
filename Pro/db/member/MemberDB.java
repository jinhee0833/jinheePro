package com.helloants.helloants.db.member;

/**
 * Created by park on 2016-01-21.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.facebook.login.LoginManager;
import com.helloants.helloants.data.type.FBType;
import com.helloants.helloants.db.ConnectDB;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by park on 2016-01-13.
 */
public enum MemberDB {
    INSTANCE;

    private DBCollection mColc;
    private DB mDB;
    private String mDBName;
    private Context mContext;
    private SharedPreferences mPref;
    private BasicDBObject mUser;
    private boolean mIsEqual;
    private boolean mIsFbJoin;
    private boolean mIsKaJoin;
    private boolean mIsDuplicated;

    private MemberDB() {
        mDB = ConnectDB.INSTANCE.mDB;
        mDBName = "member";
    }

    public void init(Context context) {
        mContext = context;
        mPref = mContext.getSharedPreferences("helloants", mContext.MODE_PRIVATE);
        setLoginData();
    }

    public void join(BasicDBObject insert) {
        String[] strs = new String[0];
        mColc = mDB.getCollection("numberCount");
        mColc.findAndModify(new BasicDBObject("_id", "userid"),
                new BasicDBObject("$inc",
                        new BasicDBObject("seq", 1)));
        DBObject temp = mColc.findOne(new BasicDBObject("_id", "userid"));

        mColc = mDB.getCollection("member");
        BasicDBObject doc = new BasicDBObject("_id", temp.get("seq"))
                .append("email", insert.get("email"))
                .append("pw", insert.get("pw"))
                .append("name", insert.get("name"))
                .append("gender", insert.get("gender"))
                .append("birth", insert.get("birth"))
                .append("joinPath", insert.get("joinPath"))
                .append("joinDate", new Date())
                .append("contentLike", strs)
                .append("replyLike", strs)
                .append("boxLists", strs)
                .append("dcsReplyList", strs);
        new SendPwTask().execute(doc);

        encryptLoginData(doc);
    }

    public void deviceToken(BasicDBObject query, BasicDBObject update) {
        mColc = mDB.getCollection("member");
        BulkWriteOperation builder = mColc.initializeOrderedBulkOperation();
        builder.find(query).updateOne(new BasicDBObject("$set", update));
        builder.execute();
    }

    public void insert(BasicDBObject user) {
        mColc = mDB.getCollection("member");
        mColc.insert(user);
    }

    public boolean confirmPW(final String pw) {
        ConfirmPW cp = new ConfirmPW(pw);
        cp.start();
        try {
            cp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mIsEqual;
    }

    public void modifyPW(final String pw) {
        ModifyPW mp = new ModifyPW(pw);
        mp.start();
        try {
            mp.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkLogin(BasicDBObject user, boolean isAutoLogin) {
        mColc = mDB.getCollection(mDBName);
        mUser = user;
        Thread pw = new OnlyGetPW();
        pw.start();
        try {
            pw.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DBCursor cursor = mColc.find(mUser);

        if (cursor.hasNext()) {
            encryptLoginData(cursor.next());
            if (isAutoLogin) autoLogin();
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    private void encryptLoginData(DBObject user) {
        String email = user.get("email").toString();
        String name = user.get("name").toString();
        String joinPath = user.get("joinPath").toString();
        try {
            email = Cryptogram.INSTANCE.Encrypt(email);
            name = Cryptogram.INSTANCE.Encrypt(name);
            joinPath = Cryptogram.INSTANCE.Encrypt(joinPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LoginData.setLoginData(email, name, joinPath);
        autoLogin();
    }

    private void encryptLoginData(FBType user) {
        String email = user.mEmail;
        String name = user.mName;
        String joinPath = user.mJoinPath;

        try {
            email = Cryptogram.INSTANCE.Encrypt(email);
            name = Cryptogram.INSTANCE.Encrypt(name);
            joinPath = Cryptogram.INSTANCE.Encrypt(joinPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LoginData.setLoginData(email, name, joinPath);
        autoLogin();
    }

    public void setLoginData() {
        String email = mPref.getString("email", "");
        String name = mPref.getString("name", "");
        String joinPath = mPref.getString("joinPath", "");

        LoginData.setLoginData(email, name, joinPath);
    }

    public void autoLogin() {
        SharedPreferences.Editor edit = mPref.edit();

        edit.putString("email", LoginData.mEmail);
        edit.putString("name", LoginData.mName);
        edit.putString("joinPath", LoginData.mJoinPath);

        edit.commit();
    }

    public boolean isKakaoJoin(long id) {
        final String JOINPATH = String.valueOf(id + "@kakao.com");
        Thread thread = new Thread() {
            @Override
            public void run() {
                mColc = mDB.getCollection("member");
                DBCursor cursor = mColc.find(new BasicDBObject("joinPath", JOINPATH));
                if (cursor.hasNext()) mIsKaJoin = true;
                else mIsKaJoin = false;
            }
        };

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mIsKaJoin;
    }

    public void kakaoLogin(long id) {

        final String JOINPATH = String.valueOf(id + "@kakao.com");
        Thread thread = new Thread() {
            @Override
            public void run() {
                mColc = mDB.getCollection("member");
                DBCursor cursor = mColc.find(new BasicDBObject("joinPath", JOINPATH));
                encryptLoginData(cursor.next());
                autoLogin();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isFbJoin(final String EMAIL) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                mColc = mDB.getCollection("member");
                DBCursor cursor = mColc.find(new BasicDBObject("email", EMAIL));

                if (cursor.hasNext()) mIsFbJoin = true;
                else mIsFbJoin = false;
                cursor.close();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mIsFbJoin;
    }

    public void fbLogin(FBType user) {
        encryptLoginData(user);
        autoLogin();
    }

    public void logout() {
        LoginData.clear();

        SharedPreferences.Editor edit = mPref.edit();

        edit.putString("email", "");
        edit.putString("name", "");
        edit.putString("joinPath", "");

        edit.commit();
    }

    public void kakaoLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                logout();
            }
        });
    }

    public void fbLogout() {
        LoginManager.getInstance().logOut();
        logout();
    }

    public long getCount() {
        return mDB.getCollection(mDBName).count();
    }

    public DBCursor find() {
        BasicDBObject oderby = new BasicDBObject("date", -1);
        mColc = mDB.getCollection(mDBName);
        return mColc.find().sort(oderby);
    }

    public DBCursor find(DBObject query) {
        BasicDBObject oderby = new BasicDBObject("date", -1);
        mColc = mDB.getCollection(mDBName);
        return mColc.find(query).sort(oderby);
    }

    public void update(final DBObject query, final DBObject update) {
        mColc = mDB.getCollection(mDBName);
        mColc.update(query, update);
    }

    public void delete(DBObject query) {
        mColc = mDB.getCollection(mDBName);
        BulkWriteOperation builder = mColc.initializeOrderedBulkOperation();
        builder.find(query).removeOne();
        builder.execute();
    }

    public String emailSearch(BasicDBObject query) {
        mColc = mDB.getCollection(mDBName);
        DBCursor cursor = mColc.find(query);
        StringBuilder sb = new StringBuilder();
        while (cursor.hasNext()) {
            sb.append(cursor.next().get("email").toString());
        }
        cursor.close();
        return sb.toString();
    }

    public void pwSearch(BasicDBObject user) {
        SearchPW search = new SearchPW(user);
        search.start();
    }

    public boolean isDuplicate(final String EMAIL) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                mColc = mDB.getCollection("member");
                DBCursor cursor = mColc.find(new BasicDBObject("email", EMAIL));
                if (cursor.hasNext()) mIsDuplicated = true;
                else mIsDuplicated = false;
            }
        };

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mIsDuplicated;
    }

    private class SendPwTask extends AsyncTask<BasicDBObject, Void, Object> {
        private HttpClient mHttpClient = new DefaultHttpClient();
        private String mUrl = "http://www.helloants.com/getAndSendPW";

        @Override
        protected Object doInBackground(BasicDBObject... params) {
            executeClient(params[0]);
            return null;
        }

        public void executeClient(BasicDBObject user) {
            HttpPost httpPost = new HttpPost(mUrl + "/" + user.getString("pw"));
            HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);
            HttpConnectionParams.setSoTimeout(params, 3000);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");

            try {
                HttpResponse response = mHttpClient.execute(httpPost);
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "utf-8")
                );

                String line = null;
                String result = "";

                while ((line = bufReader.readLine()) != null) {
                    result += line;
                }
                user.removeField("pw");
                user.append("pw", result);
                INSTANCE.insert(user);
                synchronized (mContext) {
                    INSTANCE.mContext.notify();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class OnlyGetPW extends Thread {
        private HttpClient mHttpClient = new DefaultHttpClient();
        private String mUrl = "http://www.helloants.com/getAndSendPW";

        @Override
        public void run() {
            HttpPost httpPost = new HttpPost(mUrl + "/" + mUser.getString("pw"));
            HttpParams hparams = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(hparams, 3000);
            HttpConnectionParams.setSoTimeout(hparams, 3000);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            try {
                HttpResponse response = mHttpClient.execute(httpPost);
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "utf-8")
                );
                String line = null;
                String result = "";
                while ((line = bufReader.readLine()) != null) {
                    result += line;
                }
                mUser.removeField("pw");
                mUser.append("pw", result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SearchPW extends Thread {
        private HttpClient mHttpClient = new DefaultHttpClient();
        private String mUrl = "http://www.helloants.com/sendPw";
        private BasicDBObject mUser;

        public SearchPW(BasicDBObject user) {
            mUser = user;
        }

        @Override
        public void run() {
            HttpPost httpPost = new HttpPost(mUrl + "/" + mUser.getString("email"));
            HttpParams hparams = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(hparams, 3000);
            HttpConnectionParams.setSoTimeout(hparams, 3000);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            try {
                HttpResponse response = mHttpClient.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ModifyPW extends Thread {
        private HttpClient mHttpClient = new DefaultHttpClient();
        private String mUrl = "http://www.helloants.com/getAndSendPW";
        private String mPW;

        public ModifyPW(String pw) {
            mPW = pw;
        }

        @Override
        public void run() {
            HttpPost httpPost = new HttpPost(mUrl + "/" + mPW);
            HttpParams hparams = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(hparams, 3000);
            HttpConnectionParams.setSoTimeout(hparams, 3000);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            try {
                HttpResponse response = mHttpClient.execute(httpPost);
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "utf-8")
                );
                String line = null;
                String result = "";
                while ((line = bufReader.readLine()) != null) {
                    result += line;
                }

                String email = "";

                try {
                    email = Cryptogram.Decrypt(LoginData.mEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mColc = mDB.getCollection("member");
                mColc.findAndModify(new BasicDBObject("email", email),
                        new BasicDBObject("$set",
                                new BasicDBObject("pw", result)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConfirmPW extends Thread {
        private HttpClient mHttpClient = new DefaultHttpClient();
        private String mUrl = "http://www.helloants.com/getAndSendPW";
        private String mPW;

        public ConfirmPW(String pw) {
            mPW = pw;
        }

        @Override
        public void run() {
            HttpPost httpPost = new HttpPost(mUrl + "/" + mPW);
            HttpParams hparams = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(hparams, 3000);
            HttpConnectionParams.setSoTimeout(hparams, 3000);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            try {
                HttpResponse response = mHttpClient.execute(httpPost);
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "utf-8")
                );
                String line = null;
                String result = "";
                while ((line = bufReader.readLine()) != null) {
                    result += line;
                }

                String email = "";

                try {
                    email = Cryptogram.Decrypt(LoginData.mEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mColc = mDB.getCollection("member");
                String test = ((BasicDBObject) mColc.find(new BasicDBObject("email", email)).next()).getString("pw");

                if (test.equals(result)) mIsEqual = true;
                else mIsEqual = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Set myCardFind(){
        final Set set = new HashSet();
        Thread thread = new Thread() {
            public void run() {
                mColc = mDB.getCollection("member");
                String email = "";
                try {
                    email = Cryptogram.Decrypt(LoginData.mEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    DBCursor cursor = mColc.find(new BasicDBObject("email", email));
                    if (cursor.hasNext()) {
                        BasicDBObject obj = (BasicDBObject) cursor.next();
                        List list = (List) obj.get("myCard");
                        Iterator iter = list.iterator();
                        while (iter.hasNext()) {
                            set.add(iter.next());
                        }
                    }
                } catch(IllegalStateException e) {}
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return set;
    }

    public Set myCardOffsetFind(){
        final Set set = new HashSet();
        Thread thread = new Thread() {
            public void run() {
                mColc = mDB.getCollection("member");
                String email = "";
                try {
                    email = Cryptogram.Decrypt(LoginData.mEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    DBCursor cursor = mColc.find(new BasicDBObject("email", email));
                    if (cursor.hasNext()) {
                        BasicDBObject obj = (BasicDBObject) cursor.next();
                        List list = (List) obj.get("cardOffsetDay");
                        Iterator iter = list.iterator();
                        while (iter.hasNext()) {
                            set.add(iter.next());
                        }
                    }
                } catch(IllegalStateException e) {}
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return set;
    }

    public int salaryDayFind(){
        final int[] salaryDay = {0};

        Thread thread = new Thread(){
            @Override
            public void run(){
                mColc = mDB.getCollection("member");
                String email = "";
                try {
                    email = Cryptogram.Decrypt(LoginData.mEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DBCursor cursor = mColc.find(new BasicDBObject("email",email));
                if (cursor.hasNext()){
                    BasicDBObject object = (BasicDBObject) cursor.next();
                    salaryDay[0] = (int) object.get("salaryDate");
                }

            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return salaryDay[0];
    }
}