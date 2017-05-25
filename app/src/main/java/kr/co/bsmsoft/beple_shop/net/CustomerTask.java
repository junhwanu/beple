package kr.co.bsmsoft.beple_shop.net;

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

public class CustomerTask extends AbServerTask implements NetDefine {

    private String accessToken;
    public CustomerTask(){}
    public CustomerTask(String accessToken) {
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

    public void registerCustomer(int shop_id, ArrayList<CustomerModel> customers) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        List<String> phoneList = new ArrayList<String>();
        for(int i=0;i<customers.size();i++) {
            phoneList.add(customers.get(i).getPhone());
        }

        params.put(KEY_ADDRESSES, phoneList);
        params.put(KEY_SHOP_ID, shop_id);

        get(REGISTER_CUSTOMER_URL, params);
    }

    public static ArrayList<Integer> parseCustomerIds(JSONObject json) {
        ArrayList<Integer> customerIds = new ArrayList<Integer>();
        JSONArray idList = json.optJSONArray(KEY_CUSTOMER_IDS);

        if(idList != null) {
            int arraySize = idList.length();

            for (int i = 0; i < arraySize; i++) {
                customerIds.add(idList.optInt(i));
            }
        }
        return customerIds;
    }
}
