package kr.co.bsmsoft.beple_shop.net;

import android.content.Context;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ShopModel;


public class LoginTask extends AbServerTask implements NetDefine {

	private String accessToken;
	public LoginTask() {}
	public LoginTask(String accessToken) {
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

    public void doLogin(String userName, String password, String phoneNumber, Context context) {

        RequestParams params = new RequestParams();
        params.put(KEY_USER_NAME, userName);
        params.put(KEY_USER_PASSWORD, password);
		params.put(KEY_USER_PHONE, phoneNumber);
        post(LOGIN_URL, params);
    }

	public void getShopInfo(int id) {
		RequestParams params = new RequestParams();
		params.put(KEY_ACCESS_TOKEN, this.accessToken);
		params.put(KEY_ID, id);

		get(SHOP_INFO_URL, params);
	}
	
	public static ShopModel parseLogin(JSONObject json) throws JSONException {

		ShopModel login = new ShopModel();
		JSONObject tmp = (JSONObject) json.get(KEY_LOGIN_INFO);

		login.setId(tmp.optInt(KEY_ID, 0));
		login.setShopName(tmp.optString(KEY_SHOP_NAME));
		login.setOwnerName(tmp.optString(KEY_OWNER_NAME));
		login.setPhone1(tmp.optString(KEY_PHONE1));
		login.setPhone2(tmp.optString(KEY_PHONE2));
        login.setPhone3(tmp.optString(KEY_PHONE3));
        login.setMobile(tmp.optString(KEY_MOBILE));
        login.setImageSub1(tmp.optString(KEY_IMAGE_SUB1));
		login.setImageSub2(tmp.optString(KEY_IMAGE_SUB2));
		login.setImageSub3(tmp.optString(KEY_IMAGE_SUB3));
		login.setAddress1(tmp.optString(KEY_ADDRESS1));
		login.setAddress2(tmp.optString(KEY_ADDRESS2));
		login.setAddress3(tmp.optString(KEY_ADDRESS3));
		login.setEmail(tmp.optString(KEY_EMAIL));
		login.setPointLotto(tmp.optInt(KEY_POINT_LOTTO));
		login.setPointSms(tmp.optInt(KEY_POINT_SMS));
		login.setRegDt(tmp.optString(KEY_REG_DT));
		login.setOrgId(tmp.optInt(KEY_ORG_ID));
		login.setImage(tmp.optString(KEY_IMAGE));
		login.setcCount(tmp.optInt(KEY_C_COUNT));
		login.setExpired(tmp.optString(KEY_EXPIRED));
		login.setExpiredDt(tmp.optString(KEY_EXPIRED_DT));
		login.setSign(tmp.optString(KEY_SIGN));
		login.setType(tmp.optString(KEY_TYPE));

		return login;
	}

	/**
	 * 한줄 공지사항 파싱
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static ArrayList<String> parseSingleLineMessage(JSONObject json) throws JSONException {

		ArrayList<String> arrayMessage = new ArrayList<String>();
		JSONArray jsonMessage = json.optJSONArray(KEY_MESSAGE);

		int arraySize = jsonMessage.length();

		for (int i = 0; i < arraySize; i++) {
			try {
				JSONObject jsonObj = jsonMessage.getJSONObject(i);
				arrayMessage.add(jsonObj.optString(KEY_MESSAGE));


			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return arrayMessage;
	}

}
