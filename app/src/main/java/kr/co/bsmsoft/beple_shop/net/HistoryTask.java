package kr.co.bsmsoft.beple_shop.net;

import android.util.Log;

import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;
import kr.co.bsmsoft.beple_shop.model.GroupModel;
import kr.co.bsmsoft.beple_shop.model.MmsModel;

public class HistoryTask extends AbServerTask implements NetDefine {

    private String accessToken;
    public HistoryTask(){}
    public HistoryTask(String accessToken) {
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

    public void createMmsHistory(MmsModel mms) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_SHOP_ID, String.valueOf(mms.getShopId()));
        params.put(KEY_TYPE, mms.getType());
        params.put(KEY_MESSAGE, mms.getMessage());
        params.put(KEY_PHONE_LIST, mms.getPhoneList());
        params.put(KEY_IMAGE_URL, mms.getImage_url());

        get(CREATE_MMS_HISTORY_URL, params);
    }

    public void updateMmsHistory(int id, String phone) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_GROUP_ID, String.valueOf(id));
        params.put(KEY_PHONE, phone);

        post(UPDATE_MMS_HISTORY_URL, params);
    }

    public static int getGroupId(JSONObject json) throws JSONException {
        return json.optInt(KEY_GROUP_ID);
    }
}
