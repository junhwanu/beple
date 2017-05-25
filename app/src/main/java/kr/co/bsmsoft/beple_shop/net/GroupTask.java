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

public class GroupTask extends AbServerTask implements NetDefine {

    private String accessToken;
    public GroupTask(){}
    public GroupTask(String accessToken) {
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

    public void createGroup(int shopId, GroupModel group) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_SHOP_ID, String.valueOf(shopId));
        params.put(KEY_GROUP_NAME, group.getGroupNm());
        params.put(KEY_GROUP_DESC, group.getGroupDesc());

        get(CREATE_GROUP_URL, params);
    }

    public void addGroup(int groupId, ArrayList<Integer> customerIds) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_GROUP_ID, String.valueOf(groupId));

        List<String> ids = new ArrayList<>();
        for(int i=0;i<customerIds.size();i++)
            ids.add(customerIds.get(i).toString());
        params.put(KEY_CUSTOMER_IDS, ids);

        get(ADD_GROUP_URL, params);
    }

    public void getGroupList(int shopId) {

		RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_SHOP_ID, String.valueOf(shopId));
        get(GROUP_URL, params);
	}

    public void getGroupMember(int groupId) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        String url = String.format("%s%d", GROUP_URL, groupId);
        get(url, params);
    }

    public static ArrayList<ContactGroupModel> parseGroupListToContactGroup(JSONObject json) throws JSONException {

        ArrayList<ContactGroupModel> groupList = new ArrayList<ContactGroupModel>();
        JSONArray arrayGroupList = json.optJSONArray(KEY_GROUP_LIST);

        Log.i("GroupTask", arrayGroupList.toString());
        int arraySize = arrayGroupList.length();

        for (int i = 0; i < arraySize; i++) {
            try {
                GroupModel group = parseGroupInContact(arrayGroupList.getJSONObject(i));
                ContactGroupModel _group = new ContactGroupModel();
                _group.setId(group.getId());
                _group.setGroupName(group.getGroupNm());
                for(int j=0;j<group.getCount();j++) _group.getGroupMember().add(new CustomerModel());
                groupList.add(_group);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return groupList;
    }

    public static ArrayList<CustomerModel> parseGroupMember(JSONObject json) throws JSONException {
        ArrayList<CustomerModel> customers = new ArrayList<CustomerModel>();
        JSONArray arrayCustomers = json.optJSONArray(KEY_CUSTOMER_LIST);

        for(int i=0;i<arrayCustomers.length();i++) {
            try {
                CustomerModel c = parseCustomer(arrayCustomers.getJSONObject(i));
                customers.add(c);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return customers;
    }

    public static ArrayList<GroupModel> parseGroupList(JSONObject json) throws JSONException {

        ArrayList<GroupModel> groupList = new ArrayList<GroupModel>();
        JSONArray arrayGroupList = json.optJSONArray(KEY_GROUP_LIST);

        int arraySize = arrayGroupList.length();

        for (int i = 0; i < arraySize; i++) {
            try {
                GroupModel post = parseGroup(arrayGroupList.getJSONObject(i));
                groupList.add(post);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return groupList;
    }

    public  static GroupModel parseGroup(JSONObject ret) throws JSONException {

        GroupModel group = new GroupModel();

        JSONObject json = ret.getJSONObject(KEY_GROUP);

        group.setId(json.optInt(KEY_ID, 0));
        group.setShopId(json.optInt(KEY_SHOP_ID));
        group.setGroupNm(json.optString(KEY_GROUP_NAME));
        group.setGroupDesc(json.optString(KEY_GROUP_DESC));
        group.setRegDt(json.optString(KEY_REG_DT));
        group.setCount(json.optInt(KEY_C_COUNT));
        return group;
    }

    public  static GroupModel parseGroupInContact(JSONObject json) throws JSONException {

        GroupModel group = new GroupModel();

        group.setId(json.optInt(KEY_ID, 0));
        group.setShopId(json.optInt(KEY_SHOP_ID));
        group.setGroupNm(json.optString(KEY_GROUP_NAME));
        group.setGroupDesc(json.optString(KEY_GROUP_DESC));
        group.setRegDt(json.optString(KEY_REG_DT));
        group.setCount(json.optInt(KEY_C_COUNT));
        return group;
    }

    public static CustomerModel parseCustomer(JSONObject json) {

        CustomerModel customer = new CustomerModel();

        customer.setId(json.optInt(KEY_ID, 0));
        customer.setShopId(json.optInt(KEY_SHOP_ID));
        customer.setCustomerName(json.optString(KEY_CUSTOMER_NM));
        customer.setPhone(json.optString(KEY_PHONE));
        customer.setBirth(json.optString(KEY_BIRTH));
        customer.setGender(json.optString(KEY_GENDER));
        customer.setAddress(json.optString(KEY_ADDRESS));
        customer.setCalType(json.optString(KEY_CAL_TYPE));
        customer.setEmail(json.optString(KEY_EMAIL));
        customer.setRegDt(json.optString(KEY_REG_DT));
        customer.setUpdDt(json.optString(KEY_UPD_DT));
        customer.setGroupId(json.optInt(KEY_GROUP_ID));
        return customer;
    }

}
