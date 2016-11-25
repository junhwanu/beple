package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RadioButton;

import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;


public class PaymentInfoActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

	private final String TAG = "PaymentInfoActivity";
	private Setting mSetting;
	private WebView mWebView;
	private Indicator mIndicator;
	private String url;
	private RadioButton radioAgree, radioDisagree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_info);

		mSetting = new Setting(this);
		mIndicator = new Indicator(PaymentInfoActivity.this, null);
		Button btnOk = (Button)findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);

		url = SERVER_URL + "/payment";

		mWebView = (WebView)findViewById(R.id.webview1);

		// 웹뷰에서 자바스크립트실행가능
		mWebView.getSettings().setJavaScriptEnabled(true);

		mWebView.clearCache(true);

		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		// WebViewClient 지정
		mWebView.setWebViewClient(new WebViewClientClass());

		mWebView.loadUrl(this.url);
    }

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnOk) {
				finish();
		}
	}

	private class WebViewClientClass extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			view.loadUrl(url);
			return false;
		}

		public void onPageStarted(WebView view, String url,
								  android.graphics.Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mIndicator.show();
		};

		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mIndicator.hide();

		};

		public void onReceivedError(WebView view, int errorCode,
									String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

		};
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}



