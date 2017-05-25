/**
 * 친구목록 fragment
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.LoginActivity;
import kr.co.bsmsoft.beple_shop.MainActivity;
import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.adapter.FlipperAdapter;
import kr.co.bsmsoft.beple_shop.adapter.MainMenuGridViewAdapter;
import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;
import kr.co.bsmsoft.beple_shop.model.MenuModel;
import kr.co.bsmsoft.beple_shop.model.ShopModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.LoginTask;
import kr.co.bsmsoft.beple_shop.util.mVideoView;


/**
 * Created by brady on 15. 8. 24..
 */
public class MainFragment extends AbFragment implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener {
    private final static String TAG = "MainFragment";
    private MainMenuGridViewAdapter adapter;
    private GridView gridMenuView;
    private MainApp mainApp;
    private DisplayImageOptions options;
    private ImageView imageShop;
    private RelativeLayout videoLayout;
    private VideoView videoMain;
    private TextView txtTitle, txtNotice, txtSmsPoint, txtLottoPoint;
    private View layoutNotice;
    private updateShopInfoThread updateThread;
    private ImageButton btnSimpleMMS, btnSimpleEvent, btnEvent, btnHomepage, btnShoppage;
    private ArrayList<MenuModel> menuList;
    private AdapterViewFlipper mainFlipper;
    private List<Integer> imageId = new ArrayList<>();

    private final static int MSG_GET_SHOP_INFO = 1;
    private final static int MSG_UPDATE_SHOP_INFO = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_GET_SHOP_INFO: {
                    getShopInfo();
                    break;
                }
                case MSG_UPDATE_SHOP_INFO: {
                    try {
                        updateView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    };
    public static MainFragment newInstance() {

        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mainApp = globalVar.getInstance();

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new SimpleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.drawable.bg_default_photo)
                .build();

        mainFlipper = (AdapterViewFlipper)rootView.findViewById(R.id.imageShop);
        for(int i=1;i<=3;i++) {
            imageId.add(getResources().getIdentifier("main_" + i, "drawable", getActivity().getPackageName()));
        }
        mainFlipper.setAdapter(new FlipperAdapter(getContext(), imageId));
        //imageShop = (ImageView)rootView.findViewById(R.id.imageShop);
        //videoMain = (VideoView)rootView.findViewById(R.id.videoMain);
        //videoLayout = (RelativeLayout)rootView.findViewById(R.id.videoLayout);
        txtTitle = (TextView)rootView.findViewById(R.id.txtTitle);
        layoutNotice = rootView.findViewById(R.id.layout_notice);
        txtNotice = (TextView)rootView.findViewById(R.id.txtNotice);
        txtSmsPoint = (TextView)rootView.findViewById(R.id.txtSmsPoint);
        txtLottoPoint = (TextView)rootView.findViewById(R.id.txtLottoPoint);

        txtNotice.setSelected(true);

        btnEvent = (ImageButton) rootView.findViewById(R.id.btn_event);
        btnShoppage = (ImageButton) rootView.findViewById(R.id.btn_shop);
        btnHomepage = (ImageButton) rootView.findViewById(R.id.btn_home);
        btnSimpleEvent = (ImageButton) rootView.findViewById(R.id.btn_simple_event);
        btnSimpleMMS = (ImageButton) rootView.findViewById(R.id.btn_simple_mms);
        btnEvent.setOnClickListener(this);
        btnShoppage.setOnClickListener(this);
        btnHomepage.setOnClickListener(this);
        btnSimpleEvent.setOnClickListener(this);
        btnSimpleMMS.setOnClickListener(this);
        layoutNotice.setOnClickListener(this);

        //gridMenuView = (GridView)rootView.findViewById(R.id.gridMenuView);

        initMainMenu();

        updateView();

        setSingleLineMessage();

        updateThread = new updateShopInfoThread();
        updateThread.start();

        return rootView;
    }

    private void setSingleLineMessage() {

        ArrayList<String> messages =  mainApp.getSingleLineMessage();
        if (messages != null && messages.size() > 0) {
            txtNotice.setText(messages.get(0));
            txtNotice.setVisibility(View.VISIBLE);
        }else{
            txtNotice.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!mainFlipper.isFlipping()) mainFlipper.startFlipping();
    }

    private void initMainMenu() {
        menuList = new ArrayList<MenuModel>();

        MenuModel menu = new MenuModel();
        menu.setId(R.id.item_lotto_event);
        menu.setTitle(getString(R.string.item_lotto_event));
        menu.setImageRes(R.drawable.btn_main_01);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_auto_event);
        menu.setTitle(getString(R.string.item_auto_event));
        menu.setImageRes(R.drawable.btn_main_02);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_send_mms);
        menu.setTitle(getString(R.string.item_send_message));
        menu.setImageRes(R.drawable.btn_main_03);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_image_manage);
        menu.setImageRes(R.drawable.btn_main_04);
        menu.setTitle(getString(R.string.item_image_manage));
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_notices);
        menu.setTitle(getString(R.string.item_notices));
        menu.setImageRes(R.drawable.btn_main_05);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_about);
        menu.setTitle(getString(R.string.item_about));
        menu.setImageRes(R.drawable.btn_main_06);
        menuList.add(menu);
    }
    /*
    private void initMainMenu() {

        ArrayList<MenuModel> menuList = new ArrayList<MenuModel>();

        MenuModel menu = new MenuModel();
        menu.setId(R.id.item_lotto_event);
        menu.setTitle(getString(R.string.item_lotto_event));
        menu.setImageRes(R.drawable.btn_main_01);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_auto_event);
        menu.setTitle(getString(R.string.item_auto_event));
        menu.setImageRes(R.drawable.btn_main_02);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_send_mms);
        menu.setTitle(getString(R.string.item_send_message));
        menu.setImageRes(R.drawable.btn_main_03);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_image_manage);
        menu.setImageRes(R.drawable.btn_main_04);
        menu.setTitle(getString(R.string.item_image_manage));
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_notices);
        menu.setTitle(getString(R.string.item_notices));
        menu.setImageRes(R.drawable.btn_main_05);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_about);
        menu.setTitle(getString(R.string.item_about));
        menu.setImageRes(R.drawable.btn_main_06);
        menuList.add(menu);

        adapter = new MainMenuGridViewAdapter(getActivity(), menuList);
        gridMenuView.setAdapter(adapter);
        gridMenuView.setOnItemClickListener(this);

    }*/

    private void updateView() {

        ShopModel shop = mainApp.getShopInfo();
        txtTitle.setText(shop.getShopName());
        txtLottoPoint.setText("로또 포인트 : " + mainApp.getShopInfo().getPointLotto());

        String expiredDt = mainApp.getShopInfo().getExpiredDt();
        if (expiredDt.length() == 0) {
            txtSmsPoint.setText("만료일 : 없음");
        }else{

            try {
                long remains =  diffOfDate(expiredDt);
                if (remains > 0) {
                    txtSmsPoint.setText("만료일 : " + remains + "일 남음");
                }else if (remains == 0) {
                    txtSmsPoint.setText("만료일 : 오늘 만료");
                }else{
                    txtSmsPoint.setText("만료일 : " + remains + "일");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        //txtLottoPoint.setText("SMS 포인트 : " + mainApp.getShopInfo().getPointLotto());

        String shopImage = shop.getImage();

        /*
        if (CommonUtil.isStringNullOrEmptyCheck(shopImage)) {

            String imageServerPath = String.format("%s/%s", SERVER_URL, shopImage);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
            imageLoader.displayImage(imageServerPath, imageShop, options);
        }else{
            imageShop.setImageResource(R.drawable.bg_default_photo);
        }

        imageShop.setVisibility(View.VISIBLE);
        */
        /*
        videoLayout.setVisibility(View.INVISIBLE);
        try {
            // Start the MediaController
            MediaController mediacontroller = new MediaController(getActivity());
            mediacontroller.setAnchorView(videoMain);
            // Get the URL from String VideoURL
            Uri video = Uri.parse(VIDEO_URL);
            videoMain.setMediaController(mediacontroller);
            videoMain.setVideoURI(video);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoMain.requestFocus();
        videoMain.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "videoMain onPreared.");
                mp.setLooping(true);
                mp.setVolume(0f, 0f);
                imageShop.setVisibility(View.INVISIBLE);
                videoLayout.setVisibility(View.VISIBLE);
                videoMain.start();
                Log.i(TAG, "videoMain start.");
            }
        });
        */
    }

    public static long diffOfDate(String begin) throws Exception
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date beginDate = formatter.parse(begin);
        Date endDate = new Date(); // formatter.parse(end);

        long diff = beginDate.getTime() - endDate.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        //Intent i = new Intent(getActivity(), SendMMSActivity.class);
        //startActivity(i);

        MenuModel menu = adapter.getItem(position);
        mCallbacks.onAction(this, menu.getId());
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
        if (view.getId() == R.id.layout_notice) {
            mCallbacks.onAction(this, R.id.item_notices);
        } else if (view.getId() == R.id.btn_simple_mms) {
            mCallbacks.onAction(this, R.id.item_send_mms);
        } else if (view.getId() == R.id.btn_event) {
            mCallbacks.onAction(this, R.id.item_lotto_event);
        } else if (view.getId() == R.id.btn_simple_event) {
            mCallbacks.onAction(this, R.id.item_auto_event);
        } else if (view.getId() == R.id.btn_home) {
            mCallbacks.onAction(this, R.id.item_notices);
        } else if (view.getId() == R.id.btn_shop) {
            mCallbacks.onAction(this, R.id.item_about);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt();
        }
    }

    private void getShopInfo() {

        LoginTask task = new LoginTask(mainApp.getToken());
        task.mCallbacks = new AbServerTask.ServerCallbacks(){

            @Override
            public void onSuccess(AbServerTask sender, JSONObject ret) {

                try {

                    int code = LoginTask.responseCode(ret);
                    if (code == RESPONSE_OK) {

                        // 가맹점 정보 조회 성공
                        ShopModel info = LoginTask.parseLogin(ret);
                        mainApp.setShopInfo(info);

                        mHandler.obtainMessage(MSG_UPDATE_SHOP_INFO).sendToTarget();
                    }else{
                        Helper.alert(LoginTask.responseMessage(ret), getActivity());
                    }

                } catch (JSONException e) {

                    Helper.alert(e.getLocalizedMessage(), getActivity());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(AbServerTask sender, int code, String msg) {
                Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", getActivity());
            }

        };

        try {
            Log.i(TAG, "Shop Id : " + String.valueOf(mainApp.getShopInfo().getId()));
            task.getShopInfo(mainApp.getShopInfo().getId());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    class updateShopInfoThread extends Thread {
        @Override
        public void run() {
            super.run();

            while(true) {
                try {
                    Log.i(TAG, "request ShopInfo in updateShopInfoThread.");
                    mHandler.obtainMessage(MSG_GET_SHOP_INFO).sendToTarget();
                    Thread.sleep(5000);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
