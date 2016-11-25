package kr.co.bsmsoft.beple_shop.net;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import kr.co.bsmsoft.beple_shop.common.NetDefine;


public class UserTask extends AbServerTask implements NetDefine {

    private String accessToken;

    public UserTask(String accessToken) {
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

    public void registerUser(HashMap<String, String> map) {

		RequestParams params = new RequestParams();

        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            String value = map.get(key);
            params.put(key, value);
        }
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
		post(SHOP_URL, params);
	}

    public void getUserId(String userId) {

        RequestParams params = new RequestParams();

        params.put(KEY_USER_ID, userId);
        get(ADMIN_URL, params);
    }

}
