package kr.co.bsmsoft.beple_shop.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;


/**
 * Created by brady on 15. 8. 24..
 */
public class WebViewFragment extends AbFragment implements NetDefine {


    private String url;
    private MainApp mainApp;
    private Indicator mIndicator;
    private WebView mWebView;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

            }
        }
    };

    public static WebViewFragment newInstance(String link) {

        Bundle bundle = new Bundle();
        bundle.putString(KEY_URL, link);
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_webview, container, false);

        mainApp = globalVar.getInstance();
        mIndicator = new Indicator(getActivity(), null);
        this.url = getArguments().getString(KEY_URL);

        mWebView = (WebView)rootView.findViewById(R.id.webview);

        // 웹뷰에서 자바스크립트실행가능
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.clearCache(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // WebViewClient 지정
        mWebView.setWebViewClient(new WebViewClientClass());

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWebView.loadUrl(this.url);
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
}
