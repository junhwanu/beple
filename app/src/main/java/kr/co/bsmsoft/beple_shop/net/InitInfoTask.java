package kr.co.bsmsoft.beple_shop.net;

import android.content.Context;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.InitModel;


public class InitInfoTask extends AbServerTask implements NetDefine {
	
	@Override
	public void success(JSONObject ret) {
		// TODO Auto-generated method stub
		mCallbacks.onSuccess(this, ret);
	}

	@Override
	public void failed(int code, Header[] a, Throwable e, JSONObject ret) {
		mCallbacks.onFailed(this, code, e.getLocalizedMessage());
	}


    public void getInitInfo(Context context) {
		
		RequestParams params = new RequestParams();
		params.put(KEY_PLATFORM, ANDROID);

        get(MOBILE_URL, params);

	}
	
	/**
	 * 초기 접속 정보 파싱
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static InitModel parseInit(JSONObject json) throws JSONException {

        InitModel init = new InitModel();
		JSONObject tmp = (JSONObject) json.get("init_info");

        init.setId(tmp.optInt(KEY_ID, 0));
        init.setAppVersion(tmp.optString(KEY_APP_VERSION, ""));
        init.setUpdateMsg(tmp.optString(KEY_UPDATE_MSG, ""));
        init.setUpdateAction(tmp.optString(KEY_UPDATE_ACTION, ""));
        init.setSystemMsg(tmp.optString(KEY_SYSTEM_MSG, ""));
        init.setSystemAction(tmp.optString(KEY_SYSTEM_ACTION, ""));
        init.setPlatform(tmp.optString(KEY_PLATFORM, ""));
        init.setMarketUrl(tmp.optString(KEY_MARKET_URL, ""));
		init.setNoticeUrl(tmp.optString(KEY_NOTICE_URL, ""));
		init.setFaqUrl(tmp.optString(KEY_FAQ_URL, ""));
		init.setLottoUrl(tmp.optString(KEY_LOTTO_URL, ""));
		init.setLottoMsg(tmp.optString(KEY_LOTTO_MSG, ""));

		return init;
	}
}
