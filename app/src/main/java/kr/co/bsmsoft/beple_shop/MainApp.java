package kr.co.bsmsoft.beple_shop;

import android.app.Application;


import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.model.InitModel;
import kr.co.bsmsoft.beple_shop.model.ShopModel;


public class MainApp extends Application implements NetDefine {

	public static volatile MainApp application;
    private String appVersion;
    private String marketUrl;
    private String updateMsg;
    private String updateAction;
    private String serverAppVerion;
    private Setting mSetting;
    private String noticeUrl;
    private String faqUrl;
    private ShopModel shopInfo;
    private String lottoUrl;
    private String lottoMsg;
    private ArrayList<String> singleLineMessage;

    /**
     * 서버 초기 접속 데이터
     * @param info
     */
    public void setInitData(InitModel info) {

        marketUrl = info.getMarketUrl();
        serverAppVerion = info.getAppVersion();
        updateMsg = info.getUpdateMsg();
        updateAction =info.getUpdateAction();
        noticeUrl = info.getNoticeUrl();
        faqUrl = info.getFaqUrl();
        lottoUrl = info.getLottoUrl();
        lottoMsg = info.getLottoMsg();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getMarketUrl() {
        return marketUrl;
    }

    public String getUpdateMsg() {
        return updateMsg;
    }

    public String getUpdateAction() {
        return updateAction;
    }

    public String getServerAppVerion() {
        return serverAppVerion;
    }

	@Override
	public void onCreate() {
		application = this;
		super.onCreate();

        mSetting = new Setting(this);

	}

    @Override
    public void onTerminate() {
        super.onTerminate();
        application = null;
    }

    public ShopModel getShopInfo() {
        return shopInfo;
    }

    public void setShopInfo(ShopModel shopInfo) {

        this.shopInfo = shopInfo;
    }

    public String getToken() {
        return "123456789";
    }

    public String getNoticeUrl() {
        return noticeUrl;
    }

    public void setNoticeUrl(String noticeUrl) {
        this.noticeUrl = noticeUrl;
    }

    public String getFaqUrl() {
        return faqUrl;
    }

    public void setFaqUrl(String faqUrl) {
        this.faqUrl = faqUrl;
    }

    public String getLottoUrl() {
        return lottoUrl;
    }

    public void setLottoUrl(String lottoUrl) {
        this.lottoUrl = lottoUrl;
    }

    public String getLottoMsg() {
        return lottoMsg;
    }

    public void setLottoMsg(String lottoMsg) {
        this.lottoMsg = lottoMsg;
    }

    public void logout() {

        mSetting = new Setting(this);
        mSetting.del(KEY_USER_PASSWORD);
        shopInfo = null;
    }

    public ArrayList<String> getSingleLineMessage() {
        return singleLineMessage;
    }

    public void setSingleLineMessage(ArrayList<String> singleLineMessage) {
        this.singleLineMessage = singleLineMessage;
    }
}
