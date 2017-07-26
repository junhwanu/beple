package kr.co.bsmsoft.beple_shop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.UserTask;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class RegisterShopActivity extends AppCompatActivity implements View.OnClickListener, NetDefine {

    private static final int USER_TYPE_SHOP = 1;        // 가맹점
    private static final int USER_TYPE_AGENCY = 2;      // 영업점

    private final static int MSG_REGISTER_USER = 1;
    private final static int MSG_GET_USER_ID = 2;
    private final static int MSG_GET_AGENT_ID = 3;

    private EditText editShopName, editOwner, editUserId, editPasswd, editMobile, editAddress, editEmail, editConfirmPasswd, editPhone2, editPhone3, editAgency;
    private Indicator mIndicator;
    private MainApp mainApp;
    private Toolbar toolbar;
    private Button btnPhone1;
    private String typeCode = "N";  // Y: 영리, N:비영리 (현재는 N: 가맹점, Y: 영업점)
    private boolean bCheckId = false;
    private boolean bCheckAgent = false;
    private int userType = USER_TYPE_SHOP;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_REGISTER_USER: {

                    UserTask task = new UserTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            if (mIndicator.isShowing())
                                mIndicator.hide();

                            try {
                                int code = UserTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    AlertDialog.Builder altBld = new AlertDialog.Builder(RegisterShopActivity.this);
                                    altBld.setCancelable(false).setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                    dialog.dismiss();
                                                    Intent i = new Intent(RegisterShopActivity.this, PaymentInfoActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            });

                                    AlertDialog alert = altBld.create();
                                    alert.setTitle(R.string.app_name);
                                    alert.setMessage("누리통 회원 되신걸 감사드리며 회원님의 가맹비(본인직접회사)납부를 완료해주시고 승인시까지 잠시만 기다려주십시요!.");
                                    alert.setIcon(R.mipmap.ic_launcher);
                                    alert.show();

                                }else{
                                    Helper.alert(UserTask.responseMessage(ret), RegisterShopActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), RegisterShopActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            if (mIndicator.isShowing())
                                mIndicator.hide();

                            Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", RegisterShopActivity.this);
                        }

                    };

                    if (!mIndicator.isShowing())
                        mIndicator.show();

                    task.registerUser((HashMap)msg.obj);

                    break;
                }
                case MSG_GET_USER_ID: {

                    UserTask task = new UserTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            if (mIndicator.isShowing())
                                mIndicator.hide();

                            try {
                                int code = UserTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    bCheckId = true;
                                    Helper.alert("사용가능합니다.", RegisterShopActivity.this);
                                }else{
                                    bCheckId = false;
                                    Helper.alert(UserTask.responseMessage(ret), RegisterShopActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), RegisterShopActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            if (mIndicator.isShowing())
                                mIndicator.hide();

                            Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", RegisterShopActivity.this);
                        }

                    };

                    if (!mIndicator.isShowing())
                        mIndicator.show();

                    String userId = (String)msg.obj;
                    task.getUserId(userId);

                    break;
                }
                case MSG_GET_AGENT_ID: {

                    UserTask task = new UserTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            if (mIndicator.isShowing())
                                mIndicator.hide();

                            try {
                                int code = UserTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    bCheckAgent = true;
                                    Helper.alert("지사명이 확인되었습니다.", RegisterShopActivity.this);
                                }else{
                                    bCheckAgent = false;
                                    Helper.alert(UserTask.responseMessage(ret), RegisterShopActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), RegisterShopActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            if (mIndicator.isShowing())
                                mIndicator.hide();

                            Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", RegisterShopActivity.this);
                        }

                    };

                    if (!mIndicator.isShowing())
                        mIndicator.show();

                    String agentName = (String)msg.obj;
                    task.getAgentName(agentName);

                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        Intent i = getIntent();
        userType = i.getIntExtra(KEY_USER_TYPE, USER_TYPE_SHOP);
        if (userType == USER_TYPE_SHOP) {
            setContentView(R.layout.activity_register_shop);
        }else{
            setContentView(R.layout.activity_register_agency);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnOK = (Button) findViewById(R.id.btnOK);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        Button btnSearchId = (Button) findViewById(R.id.btnSearchId);
        Button btnSearchAgent = (Button) findViewById(R.id.btnSearchAgency);
//        btnPhone1 = (Button) findViewById(R.id.btnPhone1);
        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSearchId.setOnClickListener(this);
//        btnPhone1.setOnClickListener(this);
        btnSearchAgent.setOnClickListener(this);

        editShopName = (EditText) findViewById(R.id.editShopName); // 가맹점명 (가맹점 일경우만 사용)
        editOwner = (EditText) findViewById(R.id.editOwner);        // 이름
        editUserId = (EditText) findViewById(R.id.editUserId);      // 로그인 아이디
        editPasswd = (EditText) findViewById(R.id.editPasswd);
        editMobile = (EditText) findViewById(R.id.editMobile);
        editAddress = (EditText) findViewById(R.id.editAddress);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editConfirmPasswd = (EditText) findViewById(R.id.editConfirmPasswd);
        editPhone2 = (EditText) findViewById(R.id.editPhone2);
        editPhone3 = (EditText) findViewById(R.id.editPhone3);
        editAgency = (EditText) findViewById(R.id.editAgencyName);      //추천인

        /*

        editUserPosition = (EditText) findViewById(R.id.editUserPosition);
        if (editUserPosition != null) {
            editUserPosition.setText("사원");
        }
        Button btnSelectPosition = (Button) findViewById(R.id.btnSelectPosition);

        if (btnSelectPosition != null) {
            btnSelectPosition.setOnClickListener(this);
        }
    */

/*
        String testName = "비에스엠테스트01";

        if (editShopName != null) {
            editShopName.setText(testName);
        }
        if (editOwner != null) {
            editOwner.setText(testName);
        }

        editUserId.setText(testName);
        editPasswd.setText("1111");
        editConfirmPasswd.setText("1111");
        editMobile.setText("01028966808");
        editAddress.setText("서울시 영등포구 양평동 5가");
        editEmail.setText("bradie@gmail.com");
        editPhone2.setText("793");
        editPhone3.setText("6808");
        editAgency.setText("01046825319");
*/
        mIndicator = new Indicator(this, null);
        mainApp = globalVar.getInstance();

        editUserId.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bCheckId = false;
            }
        });
        editAgency.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bCheckAgent = false;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnSearchId:

                String userId = editUserId.getText().toString();
                if (userId.length() < 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), "아이디를 입력해 주십시오.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                    Message msg = new Message();
                    msg.obj = userId;
                    msg.what = MSG_GET_USER_ID;
                    mHandler.sendMessage(msg);
                }
                break;
            case R.id.btnOK:
                registerUser();
                break;
            case R.id.btnCancel:
                finish();
                break;
            /*
            case R.id.btnPhone1:
                selectPhoneNumber();
                break;
                */
            case R.id.btnSearchAgency:
                String agentId = editAgency.getText().toString();
                if (agentId.length() < 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), "지사명을 입력해 주십시오.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                    Message msg = new Message();
                    msg.obj = agentId;
                    msg.what = MSG_GET_AGENT_ID;
                    mHandler.sendMessage(msg);
                }
                break;
            //case R.id.btnSelectPosition:
                //selectUserPosition();
            //    break;
        }
    }

    public static final Pattern VALID_HANDPHONE_REGEX =
            Pattern.compile("(01[016789])(\\d{3,4})(\\d{4})", Pattern.CASE_INSENSITIVE);

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean validateHandPhone(String phoneStr) {
        Matcher matcher = VALID_HANDPHONE_REGEX.matcher(phoneStr);
        return matcher.find();
    }

    private void registerUser() {

        if (!bCheckId) {
            Toast toast = Toast.makeText(getApplicationContext(), "아이디 중복확인을 해주시기 바랍니다.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!bCheckAgent) {
            Toast toast = Toast.makeText(getApplicationContext(), "소속 지사점명 확인을 해주시기 바랍니다.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (userType == USER_TYPE_SHOP) {
            if (!CommonUtil.isStringNullOrEmptyCheck(editShopName.getText().toString())) {
                Toast toast = Toast.makeText(getApplicationContext(), "상호를 입력해 주십시오.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
        }

        if (!CommonUtil.isStringNullOrEmptyCheck(editOwner.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "이름을 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!CommonUtil.isStringNullOrEmptyCheck(editUserId.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "아이디를 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!CommonUtil.isStringNullOrEmptyCheck(editPasswd.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "비밀번호를 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!CommonUtil.isStringNullOrEmptyCheck(editConfirmPasswd.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "비밀번호 확인을 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!CommonUtil.isStringNullOrEmptyCheck(editAddress.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "지역명을 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String cellPhone = editMobile.getText().toString();
        if (!CommonUtil.isStringNullOrEmptyCheck(cellPhone)) {
            Toast toast = Toast.makeText(getApplicationContext(), "휴대폰 번호를 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!validateHandPhone(cellPhone)) {
            Toast toast = Toast.makeText(getApplicationContext(), "올바른 휴대폰 번호를 입력해 주세요.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        /*
        if (!CommonUtil.isStringNullOrEmptyCheck(editPhone2.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "전화번호를 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!CommonUtil.isStringNullOrEmptyCheck(editPhone3.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "전화번호를 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String email = editEmail.getText().toString();
        if (!CommonUtil.isStringNullOrEmptyCheck(email)) {
            Toast toast = Toast.makeText(getApplicationContext(), "이메일을 입력해 주십시오.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        */
        String email = editEmail.getText().toString();

        if (CommonUtil.isStringNullOrEmptyCheck(email) && !validateEmail(email)) {
            Toast toast = Toast.makeText(getApplicationContext(), "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        String passwd = editPasswd.getText().toString();
        String passwdConfirm = editConfirmPasswd.getText().toString();
        if (!passwd.equals(passwdConfirm)) {
            Toast toast = Toast.makeText(getApplicationContext(), "비밀번호와 비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }



        final HashMap<String, String> params = new HashMap<String, String>();
        if (userType == USER_TYPE_SHOP) {
            params.put(KEY_SHOP_NAME, editShopName.getText().toString());
            // 엉업점은 kind 코드는 2
            params.put(KEY_KIND, "2");
        }else{
            // 영업점일 경우 가맹점 이름을 사용자 이름으로 사용한다.
            params.put(KEY_SHOP_NAME, editOwner.getText().toString());
            params.put(KEY_KIND, "3"); //String.valueOf(userPosition
        }

        params.put(KEY_OWNER_NAME, editOwner.getText().toString());
        params.put(KEY_ADMIN_ID, editUserId.getText().toString());
        params.put(KEY_ADMIN_PWD, editPasswd.getText().toString());
        params.put(KEY_MOBILE, editMobile.getText().toString());
        params.put(KEY_ADDRESS, editAddress.getText().toString());
        params.put(KEY_EMAIL, editEmail.getText().toString());
/*
        if (!CommonUtil.isStringNullOrEmptyCheck(editPhone2.getText().toString()) || !CommonUtil.isStringNullOrEmptyCheck(editPhone3.getText().toString())) {
            params.put(KEY_PHONE1, "");
            params.put(KEY_PHONE2, "");
            params.put(KEY_PHONE3, "");
        } else {
            params.put(KEY_PHONE1, phone1);
            params.put(KEY_PHONE2, editPhone2.getText().toString());
            params.put(KEY_PHONE3, editPhone3.getText().toString());
        } */
        params.put(KEY_PHONE1, "");
        params.put(KEY_PHONE2, "");
        params.put(KEY_PHONE3, "");


        params.put(KEY_AGENT_NAME, editAgency.getText().toString());
        params.put(KEY_TYPE_CODE, typeCode);


        AlertDialog.Builder altBld = new AlertDialog.Builder(this);
        altBld.setCancelable(false);
        altBld.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        new SendPost().execute(params);

                        dialog.dismiss();
                    }
                });
        altBld.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                    }
                });

        AlertDialog alert = altBld.create();
        alert.setTitle(R.string.app_name);
        alert.setMessage("등록하시겠습니까?");
        alert.setIcon(R.mipmap.ic_launcher);
        alert.show();

    }

    private String phone1 = "02";
    private int phone1Value = 0;

    private void selectPhoneNumber() {

        final String[] arrayLocation = getResources().getStringArray(R.array.array_location);
        final String[] arrayLocationValues = getResources().getStringArray(R.array.array_location_values);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("지역번호");
        builder.setSingleChoiceItems(arrayLocation, phone1Value, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (whichButton > -1) {
                    btnPhone1.setText(arrayLocation[whichButton]);
                    phone1 = arrayLocationValues[whichButton];
                    phone1Value = whichButton;
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        builder.create().show();

    }

/*
    private int userPosition = 3;
    private int userPositionValue = 0;
    private void selectUserPosition() {

        final String[] arrayPosition = getResources().getStringArray(R.array.array_user_position);
        final String[] arrayPositionValues = getResources().getStringArray(R.array.array_user_position_value);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("직책");
        builder.setSingleChoiceItems(arrayPosition, userPositionValue, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (whichButton > -1) {
                    editUserPosition.setText(arrayPosition[whichButton]);
                    userPosition = Integer.valueOf(arrayPositionValues[whichButton]);
                    userPositionValue = whichButton;
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        builder.create().show();
    }
*/

    private class SendPost extends AsyncTask<HashMap<String, String>, Void, String> {

        private HashMap<String, String> postParams;

        protected String doInBackground(HashMap<String, String>... params) {

            postParams = params[0];
            //String content = executeClient(postParams);
            String content = "0";
            return content;
        }

        protected void onPostExecute(String result) {

            if ("0".equals(result)) {
                Message msg = new Message();
                msg.obj = postParams;
                msg.what = MSG_REGISTER_USER;
                mHandler.sendMessage(msg);
            }else if ("umem".equals(result)) {
                Helper.alert("존재하지 않는 추천인 번호입니다.", RegisterShopActivity.this);
            }else {
                Helper.alert("입력값이 올바르지 않습니다. (" + result + ")", RegisterShopActivity.this);
            }
        }

        // 실제 전송하는 부분
        public String executeClient(HashMap<String, String> params) {

            String strResult = "";

            try {
                //--------------------------
                //   URL 설정하고 접속하기
                //--------------------------
                URL url = new URL("http://beple.rbiz.kr/tools/appmember.php");       // URL 설정
                HttpURLConnection http = (HttpURLConnection) url.openConnection();          // 접속
                //--------------------------
                //   전송 모드 설정 - 기본적인 설정이다
                //--------------------------
                http.setDefaultUseCaches(false);
                http.setDoInput(true);                           // 서버에서 읽기 모드 지정
                http.setDoOutput(true);                          // 서버로 쓰기 모드 지정
                http.setRequestMethod("POST");                   // 전송 방식은 POST

                // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                //--------------------------
                //   서버로 값 전송
                //--------------------------
                StringBuffer buffer = new StringBuffer();
                buffer.append("rcode").append("=").append("app").append("&");                           // php 변수에 값 대입
                buffer.append("kind").append("=").append(params.get(KEY_KIND)).append("&");            // 2. 가맹점 , 3. 사원 , 4. 팀장 , 5. 지부장 , 6. 본부장
                buffer.append("uhp").append("=").append(params.get(KEY_SALES_NAME)).append("&");       // 추천인휴대폰
                buffer.append("name").append("=").append(params.get(KEY_OWNER_NAME)).append("&");
                buffer.append("cname").append("=").append(params.get(KEY_SHOP_NAME)).append("&");
                buffer.append("id").append("=").append(params.get(KEY_ADMIN_ID)).append("&");
                buffer.append("pw").append("=").append(params.get(KEY_ADMIN_PWD)).append("&");
                buffer.append("hp").append("=").append(params.get(KEY_MOBILE)).append("&");
                buffer.append("mail").append("=").append(params.get(KEY_EMAIL));

                OutputStream os = http.getOutputStream();
                os.write( buffer.toString().getBytes("euc-kr") );
                os.flush();
                os.close();

                //OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                //PrintWriter writer = new PrintWriter(outStream);
                //writer.write(buffer.toString().getBytes("euc-kr"));
                //writer.flush();
                //--------------------------
                //   서버에서 전송받기
                //--------------------------
                InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "EUC-KR");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    if (str.length() != 0) {
                        strResult = str;
                        break;
                    }
                }

            } catch (MalformedURLException e) {
                //
            } catch (IOException e) {
                //
            } // try
            return strResult;
        }
    }

}
