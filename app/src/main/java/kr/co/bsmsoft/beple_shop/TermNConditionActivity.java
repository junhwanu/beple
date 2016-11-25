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


public class TermNConditionActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

	private final String TAG = "TermNConditionActivity";
	private Setting mSetting;
	private WebView mWebView1, mWebView2;
	private Indicator mIndicator;
	private String url1, url2;
	private RadioButton radioAgree, radioDisagree;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_term_n_condition);

		mSetting = new Setting(this);
		mIndicator = new Indicator(TermNConditionActivity.this, null);
		Button btnOk = (Button)findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		radioAgree = (RadioButton)findViewById(R.id.radioAgree);
		radioDisagree = (RadioButton)findViewById(R.id.radioDisagree);

		url1 = SERVER_URL + "/term1";
		url2 = SERVER_URL + "/term2";

		mWebView1 = (WebView)findViewById(R.id.webview1);
		mWebView2 = (WebView)findViewById(R.id.webview2);

		// 웹뷰에서 자바스크립트실행가능
		mWebView1.getSettings().setJavaScriptEnabled(true);
		mWebView2.getSettings().setJavaScriptEnabled(true);

		mWebView1.clearCache(true);
		mWebView2.clearCache(true);

		mWebView1.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView2.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		// WebViewClient 지정
		mWebView1.setWebViewClient(new WebViewClientClass());
		mWebView2.setWebViewClient(new WebViewClientClass());

		mWebView1.loadUrl(this.url1);
		mWebView2.loadUrl(this.url2);
    }

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnOk) {

			if (radioAgree.isChecked()) {
				Intent i = new Intent(this, RegisterTypeActivity.class);
				startActivity(i);
				finish();
			}else{
				finish();
			}
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



