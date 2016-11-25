package kr.co.bsmsoft.beple_shop.net;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import kr.co.bsmsoft.beple_shop.common.NetDefine;


public class BepleAsynHttpClient implements NetDefine {

	public final static int RESULT_OK = 0;

	private static AsyncHttpClient client = new AsyncHttpClient();


	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), responseHandler);
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void put(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.put(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void delete(String url,
			AsyncHttpResponseHandler responseHandler) {
		client.delete(getAbsoluteUrl(url), responseHandler);
	}

	public static void delete(String url, RequestParams params,
							  AsyncHttpResponseHandler responseHandler) {
		client.delete(null, getAbsoluteUrl(url), null, params, responseHandler);
	}


	private static String getAbsoluteUrl(String relativeUrl) {

		return SERVER_URL + relativeUrl;
	}

}


