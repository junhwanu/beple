package kr.co.bsmsoft.beple_shop.net;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbServerTask {

	public interface ServerCallbacks {
		public void onSuccess(AbServerTask sender, JSONObject ret);
		public void onFailed(AbServerTask sender, int code, String msg);
        //public void onUnauthorize(AbServerTask sender, int code, String msg);
	}
	
	public ServerCallbacks mCallbacks = sDummyCallbacks;
	
	private static ServerCallbacks sDummyCallbacks = new ServerCallbacks() {

		@Override
		public void onSuccess(AbServerTask sender, JSONObject ret) {
		}

		@Override
		public void onFailed(AbServerTask sender, int code, String msg) {
		}

    };

	public abstract void success(JSONObject ret);
	public abstract void failed(int code, Header[] a, Throwable e, JSONObject ret);

	public void get(String url, RequestParams params) {


        BepleAsynHttpClient.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                success(response);
            }

            @Override
            public void onFailure(int code, Header[] a, Throwable e, JSONObject ret) {
                failed(code, a, e, ret);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                failed(statusCode, headers, throwable, null);
            }
        });
	}

    public void get(String url) {

        BepleAsynHttpClient.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                success(response);
            }

            @Override
            public void onFailure(int code, Header[] a, Throwable e, JSONObject ret) {

                failed(code, a, e, ret);
            }
        });
    }

	public void post(String url, RequestParams params) {

        BepleAsynHttpClient.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                success(response);
            }

            @Override
            public void onFailure(int code, Header[] a, Throwable e, JSONObject ret) {
                failed(code, a, e, ret);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                failed(statusCode, headers, throwable, null);
            }

        });
	}

	public void put(String url, RequestParams params) {

        BepleAsynHttpClient.put(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                success(response);
            }

            @Override
            public void onFailure(int code, Header[] a, Throwable e, JSONObject ret) {

                failed(code, a, e, ret);
            }
        });
	}
	
	public void delete(String url, RequestParams params) {

        BepleAsynHttpClient.delete(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                success(response);
            }

            @Override
            public void onFailure(int code, Header[] a, Throwable e, JSONObject ret) {

                failed(code, a, e, ret);
            }
        });
	}


    public void delete(String url) {

        BepleAsynHttpClient.delete(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                success(response);
            }

            @Override
            public void onFailure(int code, Header[] a, Throwable e, JSONObject ret) {

                failed(code, a, e, ret);
            }
        });
    }


	public static int responseCode(JSONObject ret) throws JSONException {
		
		JSONObject response = (JSONObject) ret.get("response");
		Integer code = (Integer) response.get("code");
		
		return code.intValue();
	}

	public static String responseMessage(JSONObject ret) throws JSONException {
		
		JSONObject response = (JSONObject) ret.get("response");
		String msg = (String) response.get("msg");

		return msg;
	}

	
}
