package kr.co.bsmsoft.beple_shop.net;

import android.util.Log;

import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;
import kr.co.bsmsoft.beple_shop.model.EventModel;
import kr.co.bsmsoft.beple_shop.model.LottoModel;
import kr.co.bsmsoft.beple_shop.model.LottoSetModel;

public class EventTask extends AbServerTask implements NetDefine {

    private String accessToken;
    public EventTask(){}
    public EventTask(String accessToken) {
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

    public void createEvent(int shopId, EventModel event) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_TIMES, event.getTimes());
        params.put(KEY_NUM_OF_LOTTO, event.getNumberOfLotto());
        params.put(KEY_EVENT_NAME, event.getEventNm());
        params.put(KEY_MESSAGE, event.getMessage());
        params.put(KEY_SHOP_ID, shopId);
        params.put(KEY_GROUP, event.getTargetGroup());

        get(CREATE_EVENT_URL, params);
    }

    public void publishEvent(int shopId, int eventId) {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_ID, eventId);
        params.put(KEY_SHOP_ID, shopId);

        get(PUBLISH_URL, params);
    }

    public void getLottoTimes() {
        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        get(LOTTO_TIMES_URL, params);
    }

    public void getLottoEventList(int shopId) {

		RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_SHOP_ID, String.valueOf(shopId));
        get(LOTTO_URL, params);
	}

    public void getSmsEventList(int shopId) {

        RequestParams params = new RequestParams();
        params.put(KEY_SHOP_ID, String.valueOf(shopId));
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        get(SMS_URL, params);
    }

    public void getSmsEvent(int id) {

        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        String url = String.format("%s%d", SMS_URL, id);
        get(url, params);
    }

    public void getLottoEvent(int id) {

        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        String url = String.format("%s%d", LOTTO_URL, id);
        get(url, params);
    }

    public void sendLottoEvent(EventModel event, int shop_id) {

        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);
        params.put(KEY_TIMES, event.getTimes());
        params.put(KEY_COUNT, event.getNumberOfLotto());
        params.put(KEY_LOTTO_TITLE, event.getEventNm());
        params.put(KEY_LOTTO_MSG, event.getMessage());
        params.put(KEY_SHOP_ID, shop_id);

        ArrayList<CustomerModel> customers = event.getCustomers();
        List<String> phoneList = new ArrayList<String>();
        for(int i=0;i<customers.size();i++) {
            phoneList.add(customers.get(i).getPhone());
        }

        params.put(KEY_ADDRESSES, phoneList);

        get(LOTTO_EVENT_SEND_URL, params);
    }

    public void updateSmsEventResult(int id) {

        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        String url = String.format("%s%d", SMS_URL, id);
        put(url, params);
    }


    public void updateLottoEventResult(int id) {

        RequestParams params = new RequestParams();
        params.put(KEY_ACCESS_TOKEN, this.accessToken);

        String url = String.format("%s%d", LOTTO_URL, id);
        put(url, params);
    }

    public static ArrayList<EventModel> parseSmsEventList(JSONObject json) throws JSONException {

        ArrayList<EventModel> eventList = new ArrayList<EventModel>();
        JSONArray arrayEventList = json.optJSONArray(KEY_EVENT_LIST);

        int arraySize = arrayEventList.length();

        for (int i = 0; i < arraySize; i++) {
            try {
                EventModel post = parseEvent(arrayEventList.getJSONObject(i));
                eventList.add(post);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return eventList;
    }

    public static EventModel parseEventDetail(JSONObject json) {

        EventModel event = null;
        Log.i("EventTask", "parseEventDetail : " + json.toString());
        JSONObject jsonEvent = json.optJSONObject(KEY_EVENT);
        if (jsonEvent != null) {
            event = parseEvent(jsonEvent);
            JSONArray arrayCustomerList = jsonEvent.optJSONArray(KEY_CUSTOMER_LIST);

            try {
                if (arrayCustomerList != null) {
                    int length = arrayCustomerList.length();

                    for (int i = 0; i < length; i++) {
                        try {
                            CustomerModel customer = parseCustomer(arrayCustomerList.getJSONObject(i));
                            customer.isSelected(1);
                            event.addCustomer(customer);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONArray arrayLottoSet = jsonEvent.optJSONArray(KEY_LOTTO_SET);

                if(arrayLottoSet != null) {
                    int length = arrayLottoSet.length();

                    LottoSetModel lottoSet = new LottoSetModel();

                    for (int i = 0; i < length; i++) {
                        try {
                            LottoModel lotto = parseLotto(arrayLottoSet.getJSONObject(i));
                            lottoSet.setTimes(lotto.getTimes());
                            lottoSet.addLotto(lotto);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    event.setLottoSet(lottoSet);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return event;
    }

    public  static EventModel parseEvent(JSONObject json) {

        EventModel event = new EventModel();

        event.setId(json.optInt(KEY_ID, 0));
        event.setShopId(json.optInt(KEY_SHOP_ID));
        event.setEventNm(json.optString(KEY_EVENT_NAME));
        event.setMessage(json.optString(KEY_MESSAGE));
        event.setStatus(json.optInt(KEY_STATUS));
        event.setTargetGroup(json.optInt(KEY_TARGET_GROUP));
        event.setSendType(json.optInt(KEY_SEND_TYPE));
        event.setRegDt(json.optString(KEY_REG_DT));
        event.setUpdDt(json.optString(KEY_UPD_DT));
        event.setSendDt(json.optString(KEY_SEND_DT));
        event.setcCount(json.optInt(KEY_C_COUNT));
        event.setTimes(json.optInt(KEY_TIMES));
        event.setNumberOfLotto(json.optInt(KEY_NUM_OF_LOTTO));
        event.setSortDt(json.optString(KEY_SORT_DT));

        return event;
    }

    public static LottoModel parseLotto(JSONObject json) {
        LottoModel lotto = new LottoModel();

        lotto.setId(json.optInt(KEY_ID, 0));
        lotto.setShop_id(json.optInt(KEY_SHOP_ID));
        lotto.setCustomer_id(json.optInt(KEY_CUSTOMER_ID));
        lotto.setLotto_num(json.optString(KEY_LOTTO_NUM));
        lotto.setTimes(json.optInt(KEY_LOTTO_TIMES));
        lotto.setReg_dt(json.optString(KEY_REG_DT));
        lotto.setSend_result(json.optInt(KEY_SEND_RESULT));
        lotto.setSort_dt(json.optString(KEY_SORT_DT));

        return lotto;
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

        return customer;
    }

    public static ArrayList<LottoModel> parseLottoTimes(JSONObject json) {
        ArrayList<LottoModel> times = new ArrayList<>();

        JSONArray arrayTimes = json.optJSONArray(KEY_LOTTO_CONF);

        int length = arrayTimes.length();

        for (int i = 0; i < length; i++) {
            try {
                LottoModel model = new LottoModel();
                JSONObject json_model = arrayTimes.getJSONObject(i);

                model.setTimes(json_model.getInt(KEY_LOTTO_TIMES));
                model.setSort_dt(json_model.getString(KEY_SORT_DT));

                times.add(model);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return times;
    }
}
