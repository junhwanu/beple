package kr.co.bsmsoft.beple_shop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.PhotoGridViewAdapter;
import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.mms.MessageManager;
import kr.co.bsmsoft.beple_shop.mms.MmsManager;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;
import kr.co.bsmsoft.beple_shop.model.EventModel;
import kr.co.bsmsoft.beple_shop.model.GroupModel;
import kr.co.bsmsoft.beple_shop.model.ImageModel;
import kr.co.bsmsoft.beple_shop.model.LottoModel;
import kr.co.bsmsoft.beple_shop.model.MmsModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.CustomerTask;
import kr.co.bsmsoft.beple_shop.net.EventTask;
import kr.co.bsmsoft.beple_shop.net.GroupTask;
import kr.co.bsmsoft.beple_shop.net.HistoryTask;
import kr.co.bsmsoft.beple_shop.net.MmsTask;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class AutoLottoEventViewActivity extends AppCompatActivity implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener, MmsManager.Callbacks {

    private final static String TAG = "AutoLottoEventActivity";

    private Toolbar toolbar;
    private MainApp mainApp;
    private Setting mSetting;
    private GridView gridPhotoview;
    private PhotoGridViewAdapter adapter;
    private EditText editMessage, editTitle, editCount;
    private ImageButton btnPlus, btnMinus;
    private Spinner spTimes;
    private TextView txtCount;
    private Switch swUseName, swSign;
    private Boolean useName = false, useSign = true;

    private Button btnSend, btnClose, btnAddPhone, btnAddName;
    private int selectedPhoto = 0;
    private ArrayList<String> images;
    private String msgBody, eventTitle, eventTimes, eventCount;
    private SweetAlertDialog pDialog;
    private MmsManager messageManager;
    private ArrayList<CustomerModel> phoneList = new ArrayList<CustomerModel>();
    private ArrayList<Integer> customerIds = new ArrayList<>();
    private GroupModel group = new GroupModel();
    private EventModel event = new EventModel();
    private int mms_group_id = 0;

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
    private final static int MSG_GET_LOTTO_TIMES = 4;
    private final static int MSG_REGISTER_CUSTOMER = 5;
    private final static int MSG_CREATE_GROUP = 6;
    private final static int MSG_ADD_GROUP = 7;
    private final static int MSG_CREATE_EVENT = 8;
    private final static int MSG_PUBLISH_LOTTO = 9;
    private final static int MSG_LOAD_EVENT = 10;
    private final static int MSG_UPDATE_MMS_STATE = 11;
    private final static int MSG_CREATE_MESSAGE_GROUP = 12;

    final Handler updateDialogMessageHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(pDialog != null && pDialog.isShowing()) {
                pDialog.setContentText(String.valueOf(msg.obj));
            }
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_MMS_STATE: {

                    HistoryTask task = new HistoryTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = HistoryTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                }else{

                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            HistoryTask.responseMessage(ret),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);

                        }
                    };
                    task.updateMmsHistory(mms_group_id, msg.obj.toString());

                    break;
                }
                case MSG_CREATE_MESSAGE_GROUP: {

                    HistoryTask task = new HistoryTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = HistoryTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    mms_group_id = HistoryTask.getGroupId(ret);
                                    mHandler.obtainMessage(MSG_SEND_MESSAGE).sendToTarget();
                                }else{

                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            HistoryTask.responseMessage(ret),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);

                        }
                    };
                    MmsModel model = new MmsModel();
                    model.setShopId(mainApp.getShopInfo().getId());
                    if(adapter.getCount() > 0) {
                        model.setImage_url(adapter.getItem(0).getServerAddress() + "/" + adapter.getItem(0).getFileUrl());
                    } else {
                        model.setImage_url("");
                    }
                    model.setType("EVENT");
                    String message = msgBody;
                    if(useSign) {
                        message = message + "\n" + mainApp.getShopInfo().getSign();
                    }
                    model.setMessage(message);
                    ArrayList<String> _phoneList = new ArrayList<>();
                    for(int i=0;i<phoneList.size();i++) {
                        _phoneList.add(phoneList.get(i).getPhone());
                    }
                    model.setPhoneList(_phoneList);

                    task.createMmsHistory(model);
                    break;
                }

                case MSG_SEND_MESSAGE: {

                    pDialog = new SweetAlertDialog(AutoLottoEventViewActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("전송 중입니다...");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    String signMessage = "";
                    if(useSign)
                        signMessage = mainApp.getShopInfo().getSign();

                    messageManager = new MessageManager(msgBody, event.getCustomers(), event.getLottoSet(), false, signMessage, images, AutoLottoEventViewActivity.this);
                    messageManager.mCallbacks = AutoLottoEventViewActivity.this;
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
                                    Helper.sweetAlert(MmsTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);

                        }
                    };

                    int count = msg.arg1;
                    task.updateMMSCount(mainApp.getShopInfo().getId(), count);
                    break;
                }
                case MSG_GET_LOTTO_TIMES: {
                    EventTask task = new EventTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = EventTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    ArrayList<LottoModel> times = EventTask.parseLottoTimes(ret);
                                    updateTimes(times);

                                }else{
                                    Helper.sweetAlert(EventTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };

                    task.getLottoTimes();
                    break;
                }
                case MSG_REGISTER_CUSTOMER: {
                    CustomerTask task = new CustomerTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = CustomerTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    customerIds = CustomerTask.parseCustomerIds(ret);

                                    for(int i=0;i<customerIds.size();i++)
                                        Log.d(TAG, "customerId[" + i + "] is " + customerIds.get(i));

                                    mHandler.obtainMessage(MSG_CREATE_GROUP).sendToTarget();
                                }else{
                                    Helper.sweetAlert(CustomerTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };

                    task.registerCustomer(mainApp.getShopInfo().getId(), phoneList);
                    break;
                }
                case MSG_CREATE_GROUP: {
                    GroupTask task = new GroupTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = GroupTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    group = GroupTask.parseGroup(ret);
                                    Log.d(TAG, "MSG_CREATE_GROUP onSuccess, group id is " + group.getId());

                                    mHandler.obtainMessage(MSG_ADD_GROUP).sendToTarget();
                                }else{
                                    Helper.sweetAlert(GroupTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    String groupName = eventTitle + " [" + df.format(new Date()) + "]";

                    group.setGroupNm(groupName);
                    group.setGroupDesc("");
                    task.createGroup(mainApp.getShopInfo().getId(), group);
                    break;
                }
                case MSG_ADD_GROUP: {
                    GroupTask task = new GroupTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = GroupTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    mHandler.obtainMessage(MSG_CREATE_EVENT).sendToTarget();
                                }else{
                                    Helper.sweetAlert(GroupTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };

                    task.addGroup(group.getId(), customerIds);
                    break;
                }
                case MSG_CREATE_EVENT: {
                    final EventTask task = new EventTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = EventTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    event = task.parseEventDetail(ret);
                                    mHandler.obtainMessage(MSG_PUBLISH_LOTTO).sendToTarget();
                                }else{

                                    Helper.sweetAlert(EventTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };
                    event.setNumberOfLotto(Integer.parseInt(eventCount));
                    event.setEventNm(eventTitle);
                    event.setMessage(msgBody);
                    event.setTimes(Integer.parseInt(eventTimes));
                    event.setTargetGroup(group.getId());

                    task.createEvent(mainApp.getShopInfo().getId(), event);
                    break;
                }
                case MSG_PUBLISH_LOTTO: {
                    EventTask task = new EventTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = EventTask.responseCode(ret);
                                if (code == RESPONSE_OK) {
                                    mHandler.obtainMessage(MSG_LOAD_EVENT).sendToTarget();
                                }else{

                                    Helper.sweetAlert(EventTask.responseMessage(ret),
                                            getString(R.string.alert_title),
                                            SweetAlertDialog.ERROR_TYPE,
                                            AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };

                    task.publishEvent(mainApp.getShopInfo().getId(), event.getId());
                    break;
                }
                case MSG_LOAD_EVENT: {

                    EventTask task = new EventTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = EventTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    event  = EventTask.parseEventDetail(ret);

                                    if (event != null) {
                                        mHandler.obtainMessage(MSG_CREATE_MESSAGE_GROUP).sendToTarget();
                                    }else{
                                        Helper.finishAlert("이벤트가 존재하지 않습니다.", AutoLottoEventViewActivity.this);
                                    }
                                }else{
                                    Helper.finishAlert(EventTask.responseMessage(ret), AutoLottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.finishAlert(e.getLocalizedMessage(), AutoLottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    AutoLottoEventViewActivity.this);
                        }

                    };

                    task.getLottoEvent(event.getId());
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
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_auto_lotto_event_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainApp = globalVar.getInstance();
        mSetting = new Setting(this);

        gridPhotoview = (GridView)findViewById(R.id.gridPhotoView);
        editMessage = (EditText)findViewById(R.id.edt_lotto_event_message);
        editTitle = (EditText)findViewById(R.id.edt_lotto_event_name);
        editCount = (EditText)findViewById(R.id.edt_lotto_event_count);
        spTimes = (Spinner) findViewById(R.id.sp_lotto_event_times);
        btnSend = (Button)findViewById(R.id.btnSend);
        btnClose = (Button)findViewById(R.id.btnClose);
        btnAddPhone = (Button)findViewById(R.id.btnAddPhone);
        btnAddName = (Button)findViewById(R.id.btnAddName);
        txtCount = (TextView)findViewById(R.id.txtCount);
        swUseName = (Switch) findViewById(R.id.swUseName);
        swSign = (Switch) findViewById(R.id.sw_sign);
        btnPlus = (ImageButton) findViewById(R.id.btn_plus);
        btnMinus = (ImageButton) findViewById(R.id.btn_minus);

        btnSend.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnAddPhone.setOnClickListener(this);
        btnAddName.setOnClickListener(this);
        btnPlus.setOnClickListener(this);
        btnMinus.setOnClickListener(this);

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
        swSign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useSign = isChecked;
                if(useSign && mainApp.getShopInfo().getSign().equals("")) {
                    Helper.sweetAlert("광고 수신거부 문구가 설정되어 있지 않습니다. 해당 기능을 사용하시려면 가맹점 페이지에서 설정해주시기 바랍니다.", "알림", SweetAlertDialog.NORMAL_TYPE, AutoLottoEventViewActivity.this);
                }
            }
        });

        mHandler.obtainMessage(MSG_GET_LOTTO_TIMES).sendToTarget();

        setGridAdapter();

        Log.i(TAG, "광고 : " + mainApp.getShopInfo().getSign());
        if(mainApp.getShopInfo().getSign().equals("")) {
            Helper.sweetAlert("광고 수신거부 문구가 설정되어 있지 않습니다. 해당 기능을 사용하시려면 가맹점 페이지에서 설정해주시기 바랍니다.", "알림", SweetAlertDialog.NORMAL_TYPE, this);
        }
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

        adapter = new PhotoGridViewAdapter(AutoLottoEventViewActivity.this, imageList);
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

            eventTitle = editTitle.getText().toString();
            eventTimes = spTimes.getSelectedItem().toString().substring(0, 3);
            eventCount = editCount.getText().toString();
            msgBody = editMessage.getText().toString();

            if (!CommonUtil.isStringNullOrEmptyCheck(eventTitle)) {
                Helper.sweetAlert("이벤트명이 없습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

            if (!CommonUtil.isStringNullOrEmptyCheck(eventTimes)) {
                Helper.sweetAlert("회차가 선택되지 않았습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

            if (!CommonUtil.isStringNullOrEmptyCheck(eventCount)) {
                Helper.sweetAlert("1인당 발행수량이 선택되지 않았습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

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

            /*
            if (images == null || images.size() ==0) {
                Helper.sweetAlert("홍보 이미지가 없습니다. 이미지를 선택해 주세요.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }
            */

            new SweetAlertDialog(AutoLottoEventViewActivity.this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("알림")
                    .setContentText("로또 이벤트 생성 및 메시지 전송을 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                            sweetAlertDialog.dismissWithAnimation();
                            mHandler.obtainMessage(MSG_REGISTER_CUSTOMER).sendToTarget();
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
        }else if (view.getId() == R.id.btn_plus) {
            try {
                Log.i(TAG, "click btn_plus " + editCount.getText().toString());
                String strCount = editCount.getText().toString();
                int count = Integer.parseInt(strCount) + 1;
                if(count > 10) {
                    Helper.sweetAlert("발행 수량은 최대 10개까지 가능합니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                    return;
                }
                editCount.setText(String.valueOf(count));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (view.getId() == R.id.btn_minus) {
            try {
                String strCount = editCount.getText().toString();
                int count = Integer.parseInt(strCount) - 1;
                if(count < 1) {
                    Helper.sweetAlert("발행 수량은 최소 1개 이상이어야 합니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                    return;
                }
                editCount.setText(String.valueOf(count));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onProgress(String contentMessage) {
        Message msg = updateDialogMessageHandler.obtainMessage();
        msg.obj = contentMessage;

        updateDialogMessageHandler.sendMessage(msg);
    }

    @Override
    public void onUpdate(String phone) {
        Message msg = mHandler.obtainMessage(MSG_UPDATE_MMS_STATE);
        msg.obj = phone;

        long todayDt = (new Date()).getTime();
        String strTodayDt = new SimpleDateFormat("yyyy/MM/dd").format(new Date(todayDt));
        int sentCount = mSetting.getInt(KEY_SENT_COUNT + "_" + strTodayDt, 0);
        mSetting.input(KEY_SENT_COUNT + "_" + strTodayDt, sentCount + 1);

        mHandler.sendMessage(msg);
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

    public void updateTimes(ArrayList<LottoModel> times) {
        List<String> items = new ArrayList<>();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String str_today = df.format(new Date());

        for(int i=0;i<times.size();i++) {
            String str_sort_dt = times.get(i).getSort_dt() + " 18:00:00";
            int compare = str_today.compareTo(str_sort_dt);

            if(compare <= 0)
                items.add( times.get(i).getTimes() + "회차 (" + times.get(i).getSort_dt() + ")");
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTimes.setAdapter(dataAdapter);
        spTimes.setSelection(dataAdapter.getCount() - 1);
    }
}
