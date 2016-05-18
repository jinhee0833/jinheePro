package com.helloants.helloants.activity.login;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.helloants.helloants.R;
import com.helloants.helloants.activity.MainActivity;
import com.helloants.helloants.data.constant.Icon;
import com.helloants.helloants.data.network.GetNetState;
import com.helloants.helloants.data.notification.QuickstartPreferences;
import com.helloants.helloants.data.notification.RegistrationIntentService;
import com.helloants.helloants.data.type.FBType;
import com.helloants.helloants.db.member.MemberDB;
import com.helloants.helloants.loading.WaitDlg;
import com.helloants.helloants.login.Cryptogram;
import com.helloants.helloants.login.LoginData;
import com.kakao.auth.AuthType;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.mongodb.BasicDBObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class LoginActivity extends AppCompatActivity {
    private EditText mEmailEdit;
    private EditText mPwEdit;
    private RelativeLayout mLogin;
    private RelativeLayout mJoin;
    private TextView mFindEmailTxv;
    private TextView mFindPwTxv;
    private Toast mLoginfailToast;
    private CallbackManager callbackManager;
    private RelativeLayout mFacebook;
    private RelativeLayout mKakao;
    private SessionCallback kakaoCallback;
    private WaitDlg mWaitDlg;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";
    private String token;
    private BackPressCloseHandler backPressCloseHandler;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
            registBroadcastReceiver();
            getInstanceIdToken();

            initKakao();
            initFacebook();
            MemberDB.INSTANCE.init(LoginActivity.this);
            initLayout();

            backPressCloseHandler = new BackPressCloseHandler(this);

            layout = (LinearLayout) findViewById(R.id.llay_login_login);
        } else {
            // 연결이 끊어졌을 때 어떻게 처리할 것인가
        }
    }

    private void initKakao() {
        kakaoCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(kakaoCallback);
        mKakao = (RelativeLayout) findViewById(R.id.rlay_kakaologin_login);
        mKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    Session.getCurrentSession().open(AuthType.KAKAO_TALK, LoginActivity.this);
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                FBType user = new FBType();
                                try {
                                    user = new FBType(object.getString("id"),
                                            object.getString("name"),
                                            object.getString("email"),
                                            object.getString("birthday"),
                                            object.getString("gender"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (MemberDB.INSTANCE.isFbJoin(user.mEmail)) {
                                    MemberDB.INSTANCE.fbLogin(user);

                                    final String EMAIL = user.mEmail;
                                    Thread thread = new Thread() {
                                        @Override
                                        public void run() {
                                            BasicDBObject device = new BasicDBObject("DeviceToken", token).append("DeviceType", "Android");
                                            MemberDB.INSTANCE.deviceToken(new BasicDBObject("email", EMAIL), device);
                                        }
                                    };

                                    thread.start();
                                    try {
                                        thread.join();
                                    } catch (InterruptedException e) {}

                                    startActivity(new Intent(getApplication(), MainActivity.class));
                                    LoginActivity.this.finish();
                                } else {
                                    Intent loginActivity = new Intent(LoginActivity.this, Join.class);
                                    loginActivity.putExtra("fbEmail", user.mEmail);
                                    loginActivity.putExtra("fbName", user.mName);
                                    loginActivity.putExtra("fbBirthday", user.mBirth);
                                    loginActivity.putExtra("fbGender", user.mGender);
                                    loginActivity.putExtra("joinPath", "facebook");
                                    startActivity(loginActivity);
                                    LoginActivity.this.finish();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {}
            @Override
            public void onError(FacebookException exception) {}
        });
    }

    private void initLayout() {
        mEmailEdit = (EditText) findViewById(R.id.edit_id_login);
        mPwEdit = (EditText) findViewById(R.id.edit_pw_login);
        mLoginfailToast = Toast.makeText(this, "이메일 혹은 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT);

        mLogin = (RelativeLayout) findViewById(R.id.rlay_login_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    mWaitDlg = new WaitDlg(LoginActivity.this, "Please Wait", "Loading...");
                    mWaitDlg.start();

                    new CheckLogin(mEmailEdit.getText().toString(), mPwEdit.getText().toString()).execute();
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mJoin = (RelativeLayout) findViewById(R.id.rlay_join_login);
        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent JoinActivity = new Intent(LoginActivity.this, Join.class);
                JoinActivity.putExtra("token", token);
                startActivity(JoinActivity);
            }
        });
        mFindEmailTxv = (TextView) findViewById(R.id.txv_findEmai_loginl);
        mFindEmailTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    Intent EmailSearch = new Intent(LoginActivity.this, EmailSearch.class);
                    startActivity(EmailSearch);
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFindPwTxv = (TextView) findViewById(R.id.txv_FindPw_login);
        mFindPwTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    Intent PwSearch = new Intent(LoginActivity.this, PwSearch.class);
                    startActivity(PwSearch);
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFacebook = (RelativeLayout) findViewById(R.id.rlay_fblogin_login);
        mFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetNetState.INSTANCE.mWifi || GetNetState.INSTANCE.mMobile) {
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                } else {
                    Toast.makeText(LoginActivity.this, "네트워크 연결이 불안정합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Typeface fontFamily = Typeface.createFromAsset(getAssets(), "fonts/fontawesome.ttf");
        TextView loginIcon = (TextView) findViewById(R.id.txv_login_icon);
        loginIcon.setTypeface(fontFamily);
        loginIcon.setText(Icon.LOGIN);

        TextView joinIcon = (TextView) findViewById(R.id.txv_join_icon);
        joinIcon.setTypeface(fontFamily);
        joinIcon.setText(Icon.JOIN);

        TextView fbIcon = (TextView) findViewById(R.id.txv_fblogin_icon);
        fbIcon.setTypeface(fontFamily);
        fbIcon.setText(Icon.FACEBOOK);

        TextView kaIcon = (TextView) findViewById(R.id.txv_kakaologin_icon);
        kaIcon.setTypeface(fontFamily);
        kaIcon.setText(Icon.KAKAOTALK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class CheckLogin extends AsyncTask<Void, Void, Void> {
        private String mEmail;
        private String mPw;

        public CheckLogin(String email, String pw) {
            mEmail = email;
            mPw = pw;
        }

        @Override
        protected Void doInBackground(Void... params) {
            BasicDBObject user = new BasicDBObject("email", mEmail).append("pw", mPw);
            if (MemberDB.INSTANCE.checkLogin(user, true)) {
                String email = "";
                try {
                    email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                } catch (Exception e) {}

                try {
                    WaitDlg.stop(mWaitDlg);
                    if (MemberDB.INSTANCE.find(new BasicDBObject("email", email)).next().get("salaryDate") == null) {
                        startActivity(new Intent(getApplication(), SalaryInsert.class));
                        LoginActivity.this.finish();
                    } else {
                        startActivity(new Intent(getApplication(), MainActivity.class));
                        LoginActivity.this.finish();
                    }
                } catch (NoSuchElementException e) {
                    startActivity(new Intent(getApplication(), LoginActivity.class));
                    LoginActivity.this.finish();
                }

                BasicDBObject device = new BasicDBObject("DeviceToken", token).append("DeviceType", "Android");
                MemberDB.INSTANCE.deviceToken(new BasicDBObject("email", email), device);
            } else {
                WaitDlg.stop(mWaitDlg);
                mLoginfailToast.show();
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(kakaoCallback);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                clearApplicationCache(null);
                activity.finish();
            }
        }

        private void showGuide() {
            Snackbar.make(layout, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT).show();
        }

        private void clearApplicationCache(java.io.File dir) {
            if (dir == null)
                dir = getCacheDir();
            if (dir == null)
                return;
            java.io.File[] children = dir.listFiles();
            try {
                for (int i = 0; i < children.length; i++)
                    if (children[i].isDirectory())
                        clearApplicationCache(children[i]);
                    else children[i].delete();
            } catch (Exception e) {
            }
        }
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            requestMe();
        }
        @Override
        public void onSessionOpenFailed(KakaoException exception) {}
    }

    protected void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                if (result == ErrorCode.CLIENT_ERROR_CODE) {
                    finish();
                } else {}
            }
            @Override
            public void onSessionClosed(ErrorResult errorResult) {}
            @Override
            public void onNotSignedUp() {}
            @Override
            public void onSuccess(UserProfile userProfile) {
                mWaitDlg = new WaitDlg(LoginActivity.this, "Please Wait", "Loading...");
                mWaitDlg.start();

                long id = userProfile.getId();
                if (MemberDB.INSTANCE.isKakaoJoin(id)) {
                    MemberDB.INSTANCE.kakaoLogin(id);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    final String KAKAOID = String.valueOf(id + "@kakao.com");
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            BasicDBObject device = new BasicDBObject("DeviceToken", token).append("DeviceType", "Android");
                            MemberDB.INSTANCE.deviceToken(new BasicDBObject("joinPath", KAKAOID), device);
                        }
                    };

                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    WaitDlg.stop(mWaitDlg);
                    startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, Join.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra("kaName", userProfile.getNickname());
                    intent.putExtra("joinPath", String.valueOf(id + "@kakao.com"));
                    WaitDlg.stop(mWaitDlg);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
            }
        });
    }

    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void registBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                token = intent.getStringExtra("token");
            }
        };
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }
}