/**
 * 친구목록 fragment
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;
import kr.co.bsmsoft.beple_shop.model.ShopModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.MmsTask;


/**
 * Created by brady on 15. 8. 24..
 */
public class ShopFragment extends AbFragment implements NetDefine {

    private MainApp mainApp;
    private DisplayImageOptions options;
    private ImageView imageShop;
    private TextView txtShopName, txtOwnerName, txtPhone, txtMobile, txtEmail, txtSmsPoint, txtLottoPoint, txtMmsCount, txtTotalMmsCount;

    private final static int MSG_LOAD_MMS_COUNT = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_LOAD_MMS_COUNT: {

                    MmsTask task = new MmsTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = MmsTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    txtMmsCount.setText(String.valueOf(MmsTask.getTodayCount(ret)));
                                    txtTotalMmsCount.setText(String.valueOf(MmsTask.getMonthCount(ret)));

                                }else{

                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            MmsTask.responseMessage(ret),
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

                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    getActivity());
                        }

                    };

                    task.getMMSCount(mainApp.getShopInfo().getId());
                    break;
                }

            }
        }
    };


    public static ShopFragment newInstance() {

        ShopFragment fragment = new ShopFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        mainApp = globalVar.getInstance();

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new SimpleBitmapDisplayer())
                //.displayer(new FadeInBitmapDisplayer(300))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.drawable.bg_default_photo)
                .build();

        imageShop = (ImageView)rootView.findViewById(R.id.imageShop);
        txtShopName = (TextView)rootView.findViewById(R.id.txtShopName);
        txtMobile = (TextView)rootView.findViewById(R.id.txtMobile);
        txtPhone = (TextView)rootView.findViewById(R.id.txtPhone);
        txtEmail = (TextView)rootView.findViewById(R.id.txtEmail);
        txtSmsPoint = (TextView)rootView.findViewById(R.id.txtSmsPoint);
        txtLottoPoint = (TextView)rootView.findViewById(R.id.txtLottoPoint);
        txtMmsCount = (TextView)rootView.findViewById(R.id.txtMmsCount);
        txtTotalMmsCount = (TextView)rootView.findViewById(R.id.txtTotalMmsCount);

        updateView();

        return rootView;
    }

    public void onResume() {
        super.onResume();

        mHandler.obtainMessage(MSG_LOAD_MMS_COUNT).sendToTarget();
    }

    private void updateView() {

        ShopModel shop = mainApp.getShopInfo();

        String shopImage = shop.getImage();

        if (CommonUtil.isStringNullOrEmptyCheck(shopImage)) {

            String imageServerPath = String.format("%s/%s", SERVER_URL, shopImage);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
            imageLoader.displayImage(imageServerPath, imageShop, options);
        }else{
            imageShop.setImageResource(R.drawable.bg_default_photo);
        }

        txtShopName.setText(shop.getShopName());
        txtEmail.setText(shop.getEmail());
        txtSmsPoint.setText(String.valueOf(shop.getPointSms()));
        txtLottoPoint.setText(String.valueOf(shop.getPointLotto()));

        String phone = String.format("(%s)%s-%s",
                shop.getPhone1(),
                shop.getPhone2(),
                shop.getPhone3()
        );

        txtPhone.setText(phone);
        txtMobile.setText(shop.getMobile());
    }

}
