package kr.co.bsmsoft.beple_shop;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.PhotoGridViewAdapter;
import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.mms.MessageManager;
import kr.co.bsmsoft.beple_shop.mms.MmsManager;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;
import kr.co.bsmsoft.beple_shop.model.ImageModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.MmsTask;

public class DirectMmsViewActivity extends AppCompatActivity implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener, MmsManager.Callbacks {

    private final static String TAG = "DirectMmsViewActivity";

    private Toolbar toolbar;
    private MainApp mainApp;
    private Setting mSetting;
    private GridView gridPhotoview;
    private PhotoGridViewAdapter adapter;
    private EditText editMessage;
    private TextView txtCount;
    private Switch swUseName;
    private Boolean useName = false;

    private Button btnSend, btnClose, btnAddPhone, btnAddName;
    private int selectedPhoto = 0;
    private ArrayList<String> images;
    private String msgBody;
    private SweetAlertDialog pDialog;
    private MmsManager messageManager;
    private ArrayList<CustomerModel> phoneList = new ArrayList<CustomerModel>();

    private ConnectivityManager mConnMgr;
    private PowerManager.WakeLock mWakeLock;
    private ConnectivityBroadcastReceiver mReceiver;

    private NetworkInfo mNetworkInfo;
    private NetworkInfo mOtherNetworkInfo;

    public enum State {
        UNKNOWN,
        CONNECTED,
        NOT_CONNECTED
    }

    private State mState;
    private boolean bReady = false;

    private final static int MSG_SEND_MESSAGE = 2;
    private final static int MSG_UPDATE_MMS_COUNT = 3;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_SEND_MESSAGE: {

                    // 메시지 내용
                    pDialog = new SweetAlertDialog(DirectMmsViewActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("전송 중입니다...");
                    pDialog.setContentText("전송이 완료될 때 까지 창을 닫지 마시기 바랍니다.");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    Log.i(TAG,"phoneList size is " + phoneList.size());
                    for(CustomerModel item : phoneList) {
                        Log.i(TAG, "customer : " + item.getPhone());
                    }
                    messageManager = new MessageManager(msgBody, phoneList, useName, images, DirectMmsViewActivity.this);
                    messageManager.mCallbacks = DirectMmsViewActivity.this;
                    messageManager.execute();
                    break;
                }
                case MSG_UPDATE_MMS_COUNT: {

                    MmsTask task = new MmsTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = MmsTask.responseCode(ret);
                                if (code == RESPONSE_OK) {


                                }else{

                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            MmsTask.responseMessage(ret),
                                            SweetAlertDialog.ERROR_TYPE,
                                            DirectMmsViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), DirectMmsViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    DirectMmsViewActivity.this);

                        }
                    };

                    int count = msg.arg1;
                    task.updateMMSCount(mainApp.getShopInfo().getId(), count);
                    break;
                }
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CODE_IMAGE_SELECT_ACTIVITY: {

                if (resultCode != RESULT_OK) return;

                String filePath = data.getStringExtra(KEY_IMAGE_PATH);
                String serverURL = data.getStringExtra(KEY_SERVER_ADDR);
                String fileURL = data.getStringExtra(KEY_FILE_URL);
                if (filePath != null) {
                    ImageModel image = adapter.getItem(selectedPhoto);
                    image.setLocalPath(filePath);
                    image.setServerAddress(serverURL);
                    image.setFileUrl(fileURL);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
            case REQUEST_CODE_PHONE_LIST_ACTIVITY: {

                if (resultCode != RESULT_OK) return;

                ArrayList<CustomerModel> ret = data.getParcelableArrayListExtra(KEY_CUSTOMER_LIST);
                if (ret != null) {
                    phoneList = ret;
                    txtCount.setText(String.format("%d 명", phoneList.size()));
                }
                break;
            }
        }
    }

    private NetworkRequest mNetworkRequest;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private Network mNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_mms_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainApp = globalVar.getInstance();
        mSetting = new Setting(this);

        gridPhotoview = (GridView)findViewById(R.id.gridPhotoView);
        editMessage = (EditText)findViewById(R.id.editMessage);
        btnSend = (Button)findViewById(R.id.btnSend);
        btnClose = (Button)findViewById(R.id.btnClose);
        btnAddPhone = (Button)findViewById(R.id.btnAddPhone);
        btnAddName = (Button)findViewById(R.id.btnAddName);
        txtCount = (TextView)findViewById(R.id.txtCount);
        swUseName = (Switch) findViewById(R.id.swUseName);

        btnSend.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnAddPhone.setOnClickListener(this);
        btnAddName.setOnClickListener(this);
        btnAddName.setVisibility(View.INVISIBLE);
        swUseName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useName = isChecked;
                if(isChecked) btnAddName.setVisibility(View.VISIBLE);
                else btnAddName.setVisibility(View.INVISIBLE);
                Log.i(TAG, "swUseName isChecked : " + isChecked);
            }
        });

        setGridAdapter();

        /*
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            beginMmsConnectivityForLollipop();

        }else{

            mReceiver = new ConnectivityBroadcastReceiver();

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mReceiver, filter);

            try {
                // Ask to start the connection to the APN. Pulled from Android source code.
                int result = beginMmsConnectivity();

                if (result != PhoneEx.APN_ALREADY_ACTIVE) {
                    Log.v(TAG, "Extending MMS connectivity returned " + result + " instead of APN_ALREADY_ACTIVE");
                    // Just wait for connectivity startup without
                    // any new request of APN switch.
                }else{
                    bReady = true;
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        */
    }

    private void beginMmsConnectivityForLollipop() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mNetworkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                    .setNetworkSpecifier(Integer.toString(1))
                    .build();
        } else {
            mNetworkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                    .build();
        }

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);

                bReady = true;

                Log.d(TAG, "NetworkCallbackListener.onAvailable: network=" + network);
                mNetwork = network;
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Log.d(TAG, "NetworkCallbackListener.onLost: network=" + network);
                bReady = false;
                releaseRequestLocked();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);

            }
//            @Override
//            public void onUnavailable() {
//                super.onUnavailable();
//                Log.d(TAG, "NetworkCallbackListener.onUnavailable");
//                synchronized (MmsNetworkManager.this) {
//                    releaseRequestLocked(this);
//                    MmsNetworkManager.this.notifyAll();
//                }
//            }
        };

        try {
            mConnMgr.requestNetwork(
                    mNetworkRequest, mNetworkCallback);
        } catch (SecurityException e) {
            Log.e(TAG, "permission exception... skipping it for testing purposes", e);
            //permissionError = true;
        }

    }

    private int beginMmsConnectivity() throws IOException {

        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();

        int result = -1;
        result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE_MMS, PhoneEx.FEATURE_ENABLE_MMS);
        Log.v(TAG, "beginMmsConnectivity: result=" + result);
        switch (result)
        {
            case PhoneEx.APN_ALREADY_ACTIVE:
            case PhoneEx.APN_REQUEST_STARTED:
                acquireWakeLock();
                return result;
        }

        // None found
        throw new IOException("Cannot establish MMS connectivity");

    }

    private synchronized void createWakeLock() {
        // Create a new wake lock if we haven't made one yet.
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS Connectivity");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        // It's okay to double-acquire this because we are not using it
        // in reference-counted mode.
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        // Don't release the wake lock if it hasn't been created and acquired.
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.w(TAG, "onReceived() called with " + mState.toString() + " and " + intent);
                return;
            }

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                mState = State.NOT_CONNECTED;
            } else {
                mState = State.CONNECTED;
            }

            mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            mOtherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

//			mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
//			mIsFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            // Check availability of the mobile network.
            if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
                Log.v(TAG, "type is not TYPE_MOBILE_MMS, bail");
                bReady = false;
                return;
            }

            if (!mNetworkInfo.isConnected()) {
                Log.v(TAG, "TYPE_MOBILE_MMS not connected, bail");
                bReady = false;
                return;

            }else{

                bReady = true;

            }
        }
    };

    public void onDestroy() {

        super.onDestroy();
   }

    private void releaseRequestLocked() {

        try {
            mConnMgr.unregisterNetworkCallback(mNetworkCallback);


//            mNetworkRequest = new NetworkRequest.Builder()
//
//                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//                    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
//                    .setNetworkSpecifier(Integer.toString(1))
//                    .build();
//
//            try {
//
//                mConnMgr.requestNetwork(
//                        mNetworkRequest, mNetworkCallback);
//
//            } catch (SecurityException e) {
//                Log.e(TAG, "permission exception... skipping it for testing purposes", e);
//                //permissionError = true;
//            }

        } catch (Exception e) {
            Log.e(TAG, "couldn't unregister", e);
        }
    }

    protected void endMmsConnectivity() {

        // End the connectivity
        try {
            Log.v(TAG, "endMmsConnectivity");
            if (mConnMgr != null) {
                mConnMgr.stopUsingNetworkFeature(
                        ConnectivityManager.TYPE_MOBILE,
                        PhoneEx.FEATURE_ENABLE_MMS);
            }
        } finally {
            releaseWakeLock();
        }
    }

    private final static int MAX_IMAGE_COUNT = 1;
    private void setGridAdapter() {

        ArrayList<ImageModel> imageList = new ArrayList<ImageModel>();
        for (int i=0; i< MAX_IMAGE_COUNT; i++) {
            ImageModel image = new ImageModel();
            imageList.add(image);
        }

        adapter = new PhotoGridViewAdapter(DirectMmsViewActivity.this, imageList);
        gridPhotoview.setAdapter(adapter);
        gridPhotoview.setOnItemClickListener(this);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        ImageModel image = adapter.getItem(position);
        if (image.getLocalPath() == null) {
            selectedPhoto = position;
            selectPhoto();
        }else{
            removePhoto(image);
        }
    }

    private void removePhoto(final ImageModel image) {

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("이미지 삭제")
                .setContentText("이미지를 삭제 할까요?")
                .setConfirmText("확인")
                .setCancelText("취소")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        setGridAdapter();
                        adapter.notifyDataSetChanged();
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    private void selectPhoto() {

        Intent intent = new Intent(this, ImageSelectViewActivity.class);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_SELECT_ACTIVITY);
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnClose) {

            finish();

        }else if (view.getId() == R.id.btnSend) {

            //if (!bReady) {
            //    Helper.alert("MMS 를 보낼수 있는 시스템 환경이 아닙니다. 화면을 닫았다가 다시 실행해 보시기 바랍니다.", this);
            //    return;
            //}

            msgBody = editMessage.getText().toString();

            if (!CommonUtil.isStringNullOrEmptyCheck(msgBody)) {
                Helper.sweetAlert("메세지 내용이 없습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

            if ( phoneList == null || phoneList.size() == 0) {
                Helper.sweetAlert("메세지를 받을 대상이 없습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

            // 첨부 이미지
            images = new ArrayList<String>();
            for (ImageModel image : adapter.getItems()) {
                if (image.getLocalPath() != null) {
                    images.add(image.getLocalPath());
                }
            }

            if (images == null || images.size() ==0) {
                Helper.sweetAlert("홍보 이미지가 없습니다. 이미지를 선택해 주세요.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

            new SweetAlertDialog(DirectMmsViewActivity.this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("알림")
                    .setContentText("메시지를 전송 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                            sweetAlertDialog.dismissWithAnimation();
                            mHandler.obtainMessage(MSG_SEND_MESSAGE).sendToTarget();
                        }
                    })
                    .show();

        }else if (view.getId() == R.id.btnAddPhone) {

            Intent i = new Intent(this, AddPhoneNumberActivity.class);
            i.putExtra(KEY_CUSTOMER_LIST, phoneList);
            startActivityForResult(i, REQUEST_CODE_PHONE_LIST_ACTIVITY);
        }else if (view.getId() == R.id.btnAddName) {
            try {
                int curStart = editMessage.getSelectionStart();
                int curEnd = editMessage.getSelectionEnd();
                editMessage.getText().replace(Math.min(curStart, curEnd), Math.max(curStart, curEnd), getString(R.string.str_name), 0, getString(R.string.str_name).length());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onComplete() {
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            releaseRequestLocked();

        }else{
            endMmsConnectivity();
            if (mReceiver != null) {
                unregisterReceiver(mReceiver);
            }
        }
        */


        int count = phoneList.size();
        Message msg = new Message();
        msg.what = MSG_UPDATE_MMS_COUNT;
        msg.arg1 = count;
        mHandler.sendMessage(msg);

        pDialog.setTitleText("알림")
                .setContentText("전송을 완료하였습니다.")
                .setConfirmText("확인")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        sweetAlertDialog.dismiss();
                        finish();
                    }
                })
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

    }

    @Override
    public void onFailed(String errMessage) {

        pDialog.setTitleText("알림")
                .setContentText(errMessage)
                .setConfirmText("확인")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        sweetAlertDialog.dismiss();
                    }
                })
                .changeAlertType(SweetAlertDialog.ERROR_TYPE);

    }
}
