package kr.co.bsmsoft.beple_shop;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.klinker.android.send_message.Utils;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.fragment.AbFragment;
import kr.co.bsmsoft.beple_shop.fragment.AdminUrlFragment;
import kr.co.bsmsoft.beple_shop.fragment.ImageListFragment;
import kr.co.bsmsoft.beple_shop.fragment.LottoEventFragment;
import kr.co.bsmsoft.beple_shop.fragment.MainFragment;
import kr.co.bsmsoft.beple_shop.fragment.SmsEventFragment;
import kr.co.bsmsoft.beple_shop.fragment.WebViewFragment;

public class MainActivity extends AppCompatActivity implements AbFragment.Callbacks, NetDefine {

    private static final long DRAWER_CLOSE_DELAY_MS = 250;

    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private MainApp mainApp;
    private Setting mSetting;
    private int selectedItemId;
    private final Handler mDrawerActionHandler = new Handler();
    private AbFragment fragment = null;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetting = new Setting(this);
        if (!mSetting.getBoolean("request_permissions", false) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        mainApp = globalVar.getInstance();
        mSetting = new Setting(this);

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {

                selectedItemId = menuItem.getItemId();
                drawerLayout.closeDrawers();

                mDrawerActionHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doAction(menuItem);
                    }
                }, DRAWER_CLOSE_DELAY_MS);

                return true;
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        initFragment();
    }


    private void initFragment() {

        fragment = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void doAction(MenuItem menuItem) {

        int action = menuItem.getItemId();
        switch (action){

            case R.id.item_about: {

                menuItem.setChecked(true);
                setupFragment(action);
                break;
            }
            case R.id.item_lotto_event: {
                menuItem.setChecked(true);
                setupFragment(action);
                break;
            }

            case R.id.item_sms_event: {
                menuItem.setChecked(true);
                setupFragment(action);
                break;
            }

            case R.id.item_send_mms: {

                if (checkMMSEnv()) {
                    Intent i = new Intent(this, DirectMmsViewActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(i);
                }
                break;
            }

            case R.id.item_image_manage: {

                menuItem.setChecked(true);
                setupFragment(action);

                break;
            }

            case R.id.item_notices: {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bam.or.kr/"));
                startActivity(browserIntent);
                break;
            }

            case R.id.item_logout: {
                mainApp.logout();
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
            }
            case R.id.item_payment:{
                Intent i = new Intent(this, PaymentInfoActivity.class);
                startActivity(i);
                break;
            }

            default:
                break;

        }
    }

    private boolean checkMMSEnv() {

        boolean mobileDataEnabled = Utils.isMobileDataEnabled(this);
        if (!mobileDataEnabled) {
            Helper.alert("모바일 데이터를 사용할수 없습니다.", this);
            return false;
        }

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //boolean isWifiEnabled = wifi.isWifiEnabled();

        if (!useWifi(this)) {
            wifi.setWifiEnabled(false);
        }

        return true;
    }

    public boolean useWifi(Context context) {

        if (Utils.isMmsOverWifiEnabled(context)) {
            ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnMgr != null) {
                NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if ((niWF != null) && (niWF.isConnected())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onNewIntent(Intent intent){

    }

    private void setupFragment(int id) {

        switch(id) {

            case R.id.item_about:

                fragment = AdminUrlFragment.newInstance();
                //fragment = ShopFragment.newInstance();
                break;

            case R.id.item_lotto_event:
                fragment = LottoEventFragment.newInstance();
                break;

            case R.id.item_sms_event:
                fragment = SmsEventFragment.newInstance();
                break;

            case R.id.item_image_manage:
                fragment = ImageListFragment.newInstance();
                break;

            case R.id.item_notices:
                fragment = WebViewFragment.newInstance(mainApp.getNoticeUrl());
                break;

            default:
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        if(fm.getBackStackEntryCount() > 1){
            fm.popBackStack();
        } else{

            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("알림")
                    .setContentText("종료 하시겠습니까?")
                    .setConfirmText("확인")
                    .setCancelText("취소")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {

                            sweetAlertDialog.dismissWithAnimation();
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_ITEM_ID, selectedItemId);
    }


    @Override
    public void onAction(AbFragment sender, int target) {

        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(target);
        doAction(menuItem);
    }

}
