package kr.co.bsmsoft.beple_shop.net;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.CategoryModel;

public class CategoryTask extends AbServerTask implements NetDefine {

    private String accessToken;
    public CategoryTask(){}
    public CategoryTask(String accessToken) {
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


    public void getCategoryList() {

		RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
		get(CATEGORY_URL, params);
	}

    public static ArrayList<CategoryModel> parseCategoryList(JSONObject json) throws JSONException {

        ArrayList<CategoryModel> categoryList = new ArrayList<CategoryModel>();
        JSONArray arrayCategoryList = json.optJSONArray(KEY_CATEGORY_LIST);

        int arraySize = arrayCategoryList.length();

        for (int i = 0; i < arraySize; i++) {
            try {
                CategoryModel image = parseCategory(arrayCategoryList.getJSONObject(i));
                categoryList.add(image);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return categoryList;
    }

    public static CategoryModel parseCategory(JSONObject json) {

        CategoryModel image = new CategoryModel();

        image.setId(json.optInt(KEY_ID, 0));
        image.setCount(json.optInt(KEY_COUNT));
        image.setCategoryName(json.optString(KEY_CATEGORY_NAME));

        return image;
    }


}
