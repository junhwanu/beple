package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
import kr.co.bsmsoft.beple_shop.model.ImageModel;
import kr.co.bsmsoft.beple_shop.model.LottoModel;
import kr.co.bsmsoft.beple_shop.model.LottoSetModel;
import kr.co.bsmsoft.beple_shop.model.MmsModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.EventTask;
import kr.co.bsmsoft.beple_shop.net.HistoryTask;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class LottoEventViewActivity extends AppCompatActivity implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener, MessageManager.Callbacks {

    private Toolbar toolbar;
    private MainApp mainApp;
    private Setting mSetting;
    private GridView gridPhotoview;
    private PhotoGridViewAdapter adapter;
    private int eventId = 0;
    private TextView txtTitle, txtMessage, txtCount, txtTimes, txtNumOfLotto;
    private Button btnSend, btnClose, btnCustomerList;
    private EventModel currentEvent;
    private int selectedPhoto = 0, mms_group_id = 0;

    private ArrayList<String> images;
    private String msgBody;
    private Switch swSign;
    private Boolean useSign = true;
    private SweetAlertDialog pDialog;
    private MmsManager messageManager;
    ArrayList<CustomerModel> checked_customer;

    private final static int MSG_LOAD_EVENT = 1;
    private final static int MSG_SEND_MESSAGE = 2;
    private final static int MSG_UPDATE_RESULT = 3;
    private final static int MSG_UPDATE_MMS_STATE = 4;
    private final static int MSG_CREATE_MESSAGE_GROUP = 5;

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
                                            LottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), LottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    LottoEventViewActivity.this);

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
                                            LottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), LottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    LottoEventViewActivity.this);

                        }
                    };
                    MmsModel model = new MmsModel();
                    model.setShopId(mainApp.getShopInfo().getId());
                    if(adapter.getItem(0).getServerAddress().length() > 0 && adapter.getItem(0).getFileUrl().length() > 0) {
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
                    for(int i=0;i<currentEvent.getCustomers().size();i++) {
                        _phoneList.add(currentEvent.getCustomers().get(i).getPhone());
                    }
                    model.setPhoneList(_phoneList);

                    task.createMmsHistory(model);
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

                                    EventModel event  = EventTask.parseEventDetail(ret);

                                    if (event != null) {
                                        updateView(event);
                                    }else{
                                        Helper.finishAlert("이벤트가 존재하지 않습니다.", LottoEventViewActivity.this);
                                    }
                                }else{
                                    Helper.finishAlert(EventTask.responseMessage(ret), LottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.finishAlert(e.getLocalizedMessage(), LottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    LottoEventViewActivity.this);
                        }

                    };

                    task.getLottoEvent(eventId);
                    break;
                }
                case MSG_SEND_MESSAGE: {

                    // 메시지 내용
                    pDialog = new SweetAlertDialog(LottoEventViewActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("전송 중입니다...");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    String msgSign = "";
                    if(useSign) {
                        msgSign = mainApp.getShopInfo().getSign();
                    }
                    //messageManager = new MessageManager(msgBody, address, images, LottoEventViewActivity.this);
                    messageManager = new MessageManager(msgBody, currentEvent.getCustomers(), currentEvent.getLottoSet(), false, msgSign, images, LottoEventViewActivity.this);
                    messageManager.mCallbacks = LottoEventViewActivity.this;
                    messageManager.execute();
                    break;
                }
                case MSG_UPDATE_RESULT: {

                    EventTask task = new EventTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks() {

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = EventTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                } else {
                                    Helper.finishAlert(EventTask.responseMessage(ret), LottoEventViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.finishAlert(e.getLocalizedMessage(), LottoEventViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    LottoEventViewActivity.this);
                        }

                    };

                    task.updateLottoEventResult(eventId);
                    break;
                }
            }
        }
    };

    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        Log.d("", "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {

            case REQUEST_CODE_IMAGE_SELECT_ACTIVITY: {

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
            case REQUEST_CODE_CUSTOMER_LIST_ACTIVITY: {

                ArrayList<CustomerModel> ret = data.getParcelableArrayListExtra(KEY_CUSTOMER_LIST);
                if (ret != null) {
                    currentEvent.setCustomers(ret);

                    int count = 0;
                    for (CustomerModel customer : currentEvent.getCustomers()) {
                        if (customer.isSelected() == 1) {
                            count++;
                        }
                    }
                    txtCount.setText(String.format("%d 명", count));
                }
                break;
            }
        }
    }

    private void updateView(EventModel event) {

        currentEvent = event;

        txtTitle.setText(event.getEventNm());
        txtMessage.setText(event.getMessage());
        txtCount.setText(String.format("%d 명", event.getcCount()));
        txtTimes.setText(String.format("%d 회", event.getTimes()));
        txtNumOfLotto.setText(String.format("%d 개", event.getNumberOfLotto()));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_lotto_event_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        eventId = i.getIntExtra(KEY_ID, 0);

        mainApp = globalVar.getInstance();
        mSetting = new Setting(this);

        gridPhotoview = (GridView)findViewById(R.id.gridPhotoView);
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        txtCount = (TextView)findViewById(R.id.txtCount);
        txtTimes = (TextView)findViewById(R.id.txtTimes);
        txtNumOfLotto = (TextView)findViewById(R.id.txtNumOfLotto);

        btnSend = (Button)findViewById(R.id.btnSend);
        btnClose = (Button)findViewById(R.id.btnClose);
        btnCustomerList = (Button)findViewById(R.id.btnCustomerList);

        swSign = (Switch)findViewById(R.id.sw_sign);
        swSign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useSign = isChecked;
                if(useSign && mainApp.getShopInfo().getSign().equals("")) {
                    Helper.sweetAlert("광고 수신거부 문구가 설정되어 있지 않습니다. 해당 기능을 사용하시려면 가맹점 페이지에서 설정해주시기 바랍니다.", "알림", SweetAlertDialog.NORMAL_TYPE, LottoEventViewActivity.this);
                }
            }
        });

        btnSend.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnCustomerList.setOnClickListener(this);

        setGridAdapter();

        mHandler.obtainMessage(MSG_LOAD_EVENT).sendToTarget();

        if(mainApp.getShopInfo().getSign().equals("")) {
            Helper.sweetAlert("광고 수신거부 문구가 설정되어 있지 않습니다. 해당 기능을 사용하시려면 가맹점 페이지에서 설정해주시기 바랍니다.", "알림", SweetAlertDialog.NORMAL_TYPE, this);
        }
    }

    private final static int MAX_IMAGE_COUNT = 1;
    private void setGridAdapter() {

        ArrayList<ImageModel> imageList = new ArrayList<ImageModel>();
        for (int i=0; i< MAX_IMAGE_COUNT; i++) {
            ImageModel image = new ImageModel();
            imageList.add(image);
        }

        adapter = new PhotoGridViewAdapter(LottoEventViewActivity.this, imageList);
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

            msgBody = txtMessage.getText().toString();

            if (!CommonUtil.isStringNullOrEmptyCheck(msgBody)) {
                Helper.sweetAlert("메세지 내용이 없습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                return;
            }

            // 메시지 대상
            checked_customer = new ArrayList<>();

            for (CustomerModel customer : currentEvent.getCustomers()) {
                if (customer.isSelected() == 1) {
                    checked_customer.add(customer);
                }
            }

            if (checked_customer == null || checked_customer.size() == 0) {
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

            new SweetAlertDialog(LottoEventViewActivity.this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("알림")
                    .setContentText("메시지를 전송 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                            sweetAlertDialog.dismissWithAnimation();
                            mHandler.obtainMessage(MSG_CREATE_MESSAGE_GROUP).sendToTarget();
                        }
                    })
                    .show();

        }else if (view.getId() == R.id.btnCustomerList) {

            Intent i = new Intent(this, CustomerListActivity.class);
            if (currentEvent.getCustomers() == null || currentEvent.getCustomers().size() == 0) {
                i.putExtra(KEY_CUSTOMER_LIST, new ArrayList<CustomerModel>());
            }else{
                i.putExtra(KEY_CUSTOMER_LIST, currentEvent.getCustomers());
            }
            startActivityForResult(i, REQUEST_CODE_CUSTOMER_LIST_ACTIVITY);
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

        mHandler.obtainMessage(MSG_UPDATE_RESULT).sendToTarget();

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
