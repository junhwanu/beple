package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.felipecsl.gifimageview.library.GifImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import util.gifimageview.GifDataDownloader;

public class PhotoViewActivity extends AppCompatActivity implements NetDefine, OnClickListener {

	private MainApp mainApp;
	private DisplayImageOptions options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		//TouchImageView mImageView = (TouchImageView) findViewById(R.id.imagePhoto);
		final GifImageView mImageView = (GifImageView) findViewById(R.id.imagePhoto);

		Button btnClose = (Button) findViewById(R.id.btnClose);
		btnClose.setOnClickListener(this);

		/*
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(PhotoViewActivity.this));
		imageLoader.displayImage(imagePath, mImageView, options);
		 */

		new GifDataDownloader() {
			@Override
			protected void onPostExecute(final byte[] bytes) {
				mImageView.setBytes(bytes);
				mImageView.startAnimation();
			}
		}.execute(imagePath);
	}

	@Override
	public void onClick(View v) {
		finish();
	}
}
