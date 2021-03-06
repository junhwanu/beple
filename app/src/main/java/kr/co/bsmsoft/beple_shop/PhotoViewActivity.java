package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class PhotoViewActivity extends AppCompatActivity implements NetDefine, OnClickListener {

	private MainApp mainApp;
	private DisplayImageOptions options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_photo_view);

		mainApp = globalVar.getInstance();

		options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.displayer(new SimpleBitmapDisplayer())
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.EXACTLY)
				.showImageOnFail(R.drawable.bg_default_photo)
				.build();

		Intent i = getIntent();
		String imagePath = i.getStringExtra(KEY_IMAGE_PATH);

		ImageView imageView = (ImageView) findViewById(R.id.imagePhoto);
		Button btnClose = (Button) findViewById(R.id.btnClose);
		btnClose.setOnClickListener(this);

		Glide.with(this).load(imagePath).placeholder(R.drawable.bg_default_photo).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
	}

	@Override
	public void onClick(View v) {
		finish();
	}
}
