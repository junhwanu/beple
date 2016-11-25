package kr.co.bsmsoft.beple_shop.net;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.bsmsoft.beple_shop.common.NetDefine;


public class MmsTask extends AbServerTask implements NetDefine {

	private String accessToken;
	public MmsTask(){}
	public MmsTask(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public void success(JSONObject ret) {
		
		mCallbacks.onSuccess(this, ret);
	}

	@Override
	public void failed(int code, Header[] a, Throwable e, JSONObject ret) {
		mCallbacks.onFailed(this, code, e.getLocalizedMessage());
	}

    public void getMMSCount(int shopId) {

        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_SHOP_ID, String.valueOf(shopId));

        get(MMS_URL, params);
    }


	public void updateMMSCount(int shopId, int count) {

		RequestParams params = new RequestParams();
		params.put(KEY_ACCESS_TOKEN, this.accessToken);
		params.put(KEY_SHOP_ID, String.valueOf(shopId));
		params.put(KEY_COUNT, String.valueOf(count));

		post(MMS_URL, params);
	}

	public static int getTodayCount(JSONObject json) throws JSONException {

		JSONObject tmp = (JSONObject) json.get(KEY_MMS);
		return tmp.optInt(KEY_MMS_TODAY, 0);
	}

	public static int getMonthCount(JSONObject json) throws JSONException {

		JSONObject tmp = (JSONObject) json.get(KEY_MMS);
		return tmp.optInt(KEY_MMS_MONTH, 0);
	}

}
