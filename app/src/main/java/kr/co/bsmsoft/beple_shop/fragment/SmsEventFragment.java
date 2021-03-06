/**
 * 친구목록 fragment
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.klinker.android.send_message.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.SmsEventViewActivity;
import kr.co.bsmsoft.beple_shop.adapter.SmsEventListAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;
import kr.co.bsmsoft.beple_shop.model.EventModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.EventTask;


/**
 * Created by brady on 15. 8. 24..
 */
public class SmsEventFragment extends AbFragment implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener {

    private SmsEventListAdapter adapter;
    private ListView eventListView;
    private MainApp mainApp;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final static int MSG_LOAD_EVENT_LIST = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_LOAD_EVENT_LIST: {

                    EventTask task = new EventTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {
                            mSwipeRefreshLayout.setRefreshing(false);

                            try {
                                int code = EventTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    ArrayList<EventModel> userList = EventTask.parseSmsEventList(ret);
                                    updateAdapter(userList);

                                }else{

                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            EventTask.responseMessage(ret),
                                            SweetAlertDialog.ERROR_TYPE,
                                            getActivity());
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), getActivity());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    getActivity());
                        }


                    };

                    task.getSmsEventList(mainApp.getShopInfo().getId());
                    break;
                }

            }
        }
    };

    public static SmsEventFragment newInstance() {

        SmsEventFragment fragment = new SmsEventFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sms_event, container, false);

        mainApp = globalVar.getInstance();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                initDataSet();
            }
        });

        eventListView = (ListView)rootView.findViewById(R.id.eventListView);
        eventListView.setOnItemClickListener(this);
        adapter = new SmsEventListAdapter( getActivity(), new ArrayList<EventModel>());
        eventListView.setAdapter(adapter);

        View header = getActivity().getLayoutInflater().inflate(R.layout.list_header, null, false);

        TextView txtHeader =  (TextView)header.findViewById(R.id.txtDescription);
        TextView txtTitle =  (TextView)header.findViewById(R.id.txtTitle);
        ImageView imgHeader =  (ImageView)header.findViewById(R.id.imgHeader);

        txtHeader.setText(getString(R.string.sms_event_description));
        txtTitle.setText("문자 이벤트");
        imgHeader.setImageResource(R.drawable.ic_header_sms);

        eventListView.addHeaderView(header);
        return rootView;
    }


    private void updateAdapter(ArrayList<EventModel> events) {

        if (events != null) {
            adapter.addAll(events);
        }
    }

    private void initDataSet() {
        adapter.clear();
        adapter.notifyDataSetChanged();
        mHandler.obtainMessage(MSG_LOAD_EVENT_LIST).sendToTarget();
    }

    @Override
    public void onResume() {
        super.onResume();
        initDataSet();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!checkMMSEnv()) {
            return;
        }

        EventModel event =  adapter.getItem(position-1);
        Intent i = new Intent(getActivity(), SmsEventViewActivity.class);
        i.putExtra(KEY_ID, event.getId());
        startActivity(i);
    }

    private boolean checkMMSEnv() {

        boolean mobileDataEnabled = Utils.isMobileDataEnabled(getActivity());
        if (!mobileDataEnabled) {
            Helper.alert("모바일 데이터를 사용할수 없습니다.", getActivity());
            return false;
        }

        WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        //boolean isWifiEnabled = wifi.isWifiEnabled();

        if (!useWifi(getActivity())) {
            wifi.setWifiEnabled(false);
        }

        return true;
    }

    public boolean useWifi(Context context) {

        if (Utils.isMmsOverWifiEnabled(context)) {
            ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnMgr != null) {
                NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if ((niWF != null) && (niWF.isConnected())) {
                    return true;
                }
            }
        }
        return false;
    }
}
