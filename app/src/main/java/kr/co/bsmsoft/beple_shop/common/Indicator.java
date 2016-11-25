package kr.co.bsmsoft.beple_shop.common;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import kr.co.bsmsoft.beple_shop.R;

public class Indicator implements OnTouchListener {
	private Activity xActivity = null;
	private View xView = null;
	private View xViewOverActivity = null;
	private ViewGroup xLayout = null;
	private ImageView logoImageView;

	private boolean isFirst = true;
	private boolean isShow = false;

	private final int ACTIVITY = 1;
	private final int LAYOUT = 2;
	private final int HIDE = 3;

	private int iMode = 0;

	public Indicator(Activity activity, View layout) {
		xViewOverActivity = View.inflate(activity,
                R.layout.indicator_over_activity, null);
		xActivity = activity;

		if (layout == null) {
			iMode = ACTIVITY;
			xViewOverActivity.findViewById(R.id.progressBar2).setVisibility(
					View.VISIBLE);
		} else {
			iMode = LAYOUT;
			xViewOverActivity.findViewById(R.id.progressBar2).setVisibility(
					View.GONE);
			xViewOverActivity.setBackgroundColor(0x00000000);
			xViewOverActivity.setOnTouchListener(this);
			xView = View.inflate(activity, R.layout.indicator, null);
			xView.setOnTouchListener(this);
			xLayout = (ViewGroup) layout;
			logoImageView = (ImageView) xViewOverActivity
					.findViewById(R.id.logoImageView);
		}
	}

	public void show() {

		if (!isShow) {
			isShow = true;
			testMethod(iMode);
		}
	}

	public void noprogress_show() {

		if (!isShow) {
			isShow = true;
			testMethod(iMode);
			xViewOverActivity.findViewById(R.id.progressBar2).setVisibility(
					View.GONE);
		}
	}

	public void hide() {
		testMethod(HIDE);
	}

	public boolean isShowing() {
		return isShow;
	}

	public void setLogoImageResource(int resId) {
		logoImageView.setImageResource(resId);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	void testMethod(int mode) {
		switch (mode) {
		case ACTIVITY: {
			xViewOverActivity.setFocusable(true);

			if (isFirst) {
				xActivity.addContentView(xViewOverActivity, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				isFirst = false;
			} else {
				xViewOverActivity.setVisibility(View.VISIBLE);
			}

			break;
		}

		case LAYOUT: {
			xViewOverActivity.setFocusable(true);

			if (isFirst) {
				xActivity.addContentView(xViewOverActivity, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				xLayout.addView(xView, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				isFirst = false;
			} else {
				xViewOverActivity.setVisibility(View.VISIBLE);
				xView.setVisibility(View.VISIBLE);
			}

			break;
		}

		case HIDE: {
			xViewOverActivity.setVisibility(View.GONE);
			xViewOverActivity.setFocusable(false);
			if (xView != null) {
				xView.setVisibility(View.GONE);
			}
			isShow = false;

			break;
		}
		}
	}
}
