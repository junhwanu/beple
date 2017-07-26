package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class RegisterTypeActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

	private final String TAG = "RegisterTypeActivity";
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_register_type);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Button btnTypeShop = (Button)findViewById(R.id.btnTypeShop);
		Button btnTypeSales = (Button)findViewById(R.id.btnTypeSales);
		btnTypeShop.setOnClickListener(this);
		btnTypeSales.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.btnTypeShop) {
			Intent i = new Intent(this, RegisterShopActivity.class);
			i.putExtra(KEY_USER_TYPE, USER_TYPE_SHOP);
			startActivity(i);
			finish();
		}else if (v.getId() == R.id.btnTypeSales) {
			Intent i = new Intent(this, RegisterShopActivity.class);
			i.putExtra(KEY_USER_TYPE, USER_TYPE_AGENCY);
			startActivity(i);
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}



