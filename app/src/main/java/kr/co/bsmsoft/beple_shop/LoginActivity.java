package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.model.ShopModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.LoginTask;


public class LoginActivity extends AppCompatActivity implements NetDefine, OnClickListener {

    private Setting mSetting;
    private Indicator mIndicator;
    private MainApp mainApp;
    private String userName;
    private String userPassword;
    private EditText editUserName, editPassword;
    private CheckBox chkAutoLogin;

    private final static int MSG_LOGIN = 1;
    private final static int MSG_LAUNCH_MAIN = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_LOGIN: {

                    doLogin(userName, userPassword);

                    break;
                }
                case MSG_LAUNCH_MAIN: {

                    String isExpired = mainApp.getShopInfo().getExpired();
                    if ("Y".equals(isExpired)) {
                        Helper.sweetAlert("사용승인이 되어있지 않거나 사용기간이 만료되었습니다.\n관리자에게 문의하시기 바랍니다.", getString(R.string.alert_title), SweetAlertDialog.WARNING_TYPE, LoginActivity.this);
                    }else{
                        startActivity(new Intent(LoginActivity.this,
                                MainActivity.class));
                        finish();
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSetting = new Setting(this);
        mIndicator = new Indicator(this, null);

        mainApp = globalVar.getInstance();
        mainApp.setAppVersion(Helper.getVersion(this));

        editUserName = (EditText) findViewById(R.id.editUserName);
        editPassword = (EditText) findViewById(R.id.editPassword);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        chkAutoLogin = (CheckBox) findViewById(R.id.chkAutoLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        boolean isAutoLogin = mSetting.getBoolean(KEY_AUTO_LOGIN, false);
        userName = mSetting.getString(KEY_USER_NAME, "");
        userPassword = mSetting.getString(KEY_USER_PASSWORD, "");

        editUserName.setText(userName);
        editPassword.setText(userPassword);

        if (CommonUtil.isStringNullOrEmptyCheck(userName) &&
                CommonUtil.isStringNullOrEmptyCheck(userPassword) &&
                    isAutoLogin) {

            mHandler.obtainMessage(MSG_LOGIN).sendToTarget();
        }

    }

    private void doLogin(final String userName, final String password) {

        LoginTask task = new LoginTask();
        task.mCallbacks = new AbServerTask.ServerCallbacks(){

            @Override
            public void onSuccess(AbServerTask sender, JSONObject ret) {

                if (mIndicator.isShowing())
                    mIndicator.hide();

                try {

                    int code = LoginTask.responseCode(ret);
                    if (code == RESPONSE_OK) {

                        // 로그인 성공
                        ShopModel info = LoginTask.parseLogin(ret);
                        mainApp.setShopInfo(info);

                        ArrayList<String> messages =  LoginTask.parseSingleLineMessage(ret);
                        mainApp.setSingleLineMessage(messages);

                        setResult(RESULT_OK);

                        // 자동 로그인 설정
                        if (chkAutoLogin.isChecked()) {
                            mSetting.input(KEY_AUTO_LOGIN, true);
                            mSetting.input(KEY_USER_NAME, userName);
                            mSetting.input(KEY_USER_PASSWORD, password);
                        }else{
                            mSetting.input(KEY_AUTO_LOGIN, false);
                            mSetting.input(KEY_USER_NAME, userName);
                        }

                        mHandler.obtainMessage(MSG_LAUNCH_MAIN).sendToTarget();

                    }else if (code == ERROR_INVALID_LOGIN_INFO) {
                        Helper.alert(LoginTask.responseMessage(ret), LoginActivity.this);

                    }else if (code == ERROR_NOT_EXIST_USERID) {
                        Helper.alert(LoginTask.responseMessage(ret), LoginActivity.this);

                    }else{
                        Helper.alert(LoginTask.responseMessage(ret), LoginActivity.this);
                    }

                } catch (JSONException e) {

                    Helper.alert(e.getLocalizedMessage(), LoginActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(AbServerTask sender, int code, String msg) {

                if (mIndicator.isShowing())
                    mIndicator.hide();

                Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", LoginActivity.this);
            }

        };

        if (!mIndicator.isShowing())
            mIndicator.show();

        task.doLogin (userName, password, LoginActivity.this);
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnLogin) {

            userName = editUserName.getText().toString();
            userPassword = editPassword.getText().toString();

            if (userName.length() < 1) {
                Toast toast = Toast.makeText(LoginActivity.this,"아이디를 입력해 주세요.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (userPassword.length() < 1) {
                Toast toast = Toast.makeText(LoginActivity.this,"비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            mHandler.obtainMessage(MSG_LOGIN).sendToTarget();

        }else if (v.getId() == R.id.btnCancel) {
            finish();
        }else if (v.getId() == R.id.btnRegister) {
            Intent i = new Intent(this, TermNConditionActivity.class);
            startActivity(i);
        }
    }

}



