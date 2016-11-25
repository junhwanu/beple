package kr.co.bsmsoft.beple_shop.net;

import android.content.Context;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;

import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;

public class PushTask extends AbServerTask implements NetDefine {

	private String accessToken;

	public PushTask() {
	}

	public PushTask(String accessToken) {
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

    public void registerPush(int id, String pushId, Context context) {
		
		RequestParams params = new RequestParams();
		params.put(KEY_ACCESS_TOKEN, this.accessToken );
		params.put(KEY_PUSH_ID, pushId);
        params.put(KEY_ID, String.valueOf(id));
		params.put(KEY_VERSION, Helper.getVersion(context));

		post(MOBILE_URL, params);
	}
}
