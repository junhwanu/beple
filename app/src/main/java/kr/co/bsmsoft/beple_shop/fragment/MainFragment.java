/**
 * 친구목록 fragment
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.adapter.MainMenuGridViewAdapter;
import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;
import kr.co.bsmsoft.beple_shop.model.MenuModel;
import kr.co.bsmsoft.beple_shop.model.ShopModel;
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
    private VideoView videoMain;
    private TextView txtTitle, txtNotice, txtSmsPoint, txtLottoPoint;
    private View layoutNotice;

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

        imageShop = (ImageView)rootView.findViewById(R.id.imageShop);
        videoMain = (VideoView)rootView.findViewById(R.id.videoMain);
        //videoLayout = (LinearLayout) rootView.findViewById(R.id.videoLayout);
        txtTitle = (TextView)rootView.findViewById(R.id.txtTitle);
        layoutNotice = rootView.findViewById(R.id.layout_notice);
        txtNotice = (TextView)rootView.findViewById(R.id.txtNotice);
        txtSmsPoint = (TextView)rootView.findViewById(R.id.txtSmsPoint);
        txtLottoPoint = (TextView)rootView.findViewById(R.id.txtLottoPoint);

        txtNotice.setSelected(true);

        layoutNotice.setOnClickListener(this);

        gridMenuView = (GridView)rootView.findViewById(R.id.gridMenuView);

        initMainMenu();

        updateView();

        setSingleLineMessage();

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

    private void initMainMenu() {

        ArrayList<MenuModel> menuList = new ArrayList<MenuModel>();

        MenuModel menu = new MenuModel();
        menu.setId(R.id.item_lotto_event);
        menu.setTitle(getString(R.string.item_lotto_event));
        menu.setImageRes(R.drawable.btn_main_01);
        menuList.add(menu);

        menu = new MenuModel();
        menu.setId(R.id.item_sms_event);
        menu.setTitle(getString(R.string.item_sms_event));
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

    }

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


        if (CommonUtil.isStringNullOrEmptyCheck(shopImage)) {

            String imageServerPath = String.format("%s/%s", SERVER_URL, shopImage);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
            imageLoader.displayImage(imageServerPath, imageShop, options);
        }else{
            imageShop.setImageResource(R.drawable.bg_default_photo);
        }

        videoMain.setVideoURI(Uri.parse(VIDEO_URL));
        videoMain.setMediaController(new MediaController(getActivity()));
        videoMain.seekTo(0);
        videoMain.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        videoMain.start();

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

        if (view.getId() == R.id.layout_notice) {
            mCallbacks.onAction(this, R.id.item_notices);
        }
    }
}
