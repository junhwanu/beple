package kr.co.bsmsoft.beple_shop.net;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ImageModel;

public class ImageTask extends AbServerTask implements NetDefine {

    private String accessToken;
    public ImageTask(){}
    public ImageTask(String accessToken) {
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

    public void getImageList(int shopId, int categoryId, int page) {

		RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_PAGE, String.valueOf(page));
        params.put(KEY_CATEGORY_ID, String.valueOf(categoryId));
        params.put(KEY_SHOP_ID, String.valueOf(shopId));
		get(IMAGE_URL, params);
	}

    public static ArrayList<ImageModel> parseImageList(JSONObject json) throws JSONException {

        ArrayList<ImageModel> imageList = new ArrayList<ImageModel>();
        JSONArray arrayImageList = json.optJSONArray(KEY_IMAGE_LIST);

        int arraySize = arrayImageList.length();

        for (int i = 0; i < arraySize; i++) {
            try {
                ImageModel image = parseImage(arrayImageList.getJSONObject(i));
                imageList.add(image);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return imageList;
    }

    public static ImageModel parseImage(JSONObject json) {

        ImageModel image = new ImageModel();

        image.setId(json.optInt(KEY_ID, 0));
        image.setShopId(json.optInt(KEY_SHOP_ID));
        image.setFileUrl(json.optString(KEY_FILE_URL));
        image.setFileDesc(json.optString(KEY_FILE_DESC));
        image.setFileName(json.optString(KEY_FILE_NAME));
        image.setRegDt(json.optString(KEY_REG_DT));
        image.setServerAddress(json.optString(KEY_SERVER_ADDR));
        String isUse = json.optString(KEY_IS_USE);
        image.setIsUse("true".equals(isUse) ? true : false);

        return image;
    }


}
