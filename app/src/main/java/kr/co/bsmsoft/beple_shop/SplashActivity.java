package kr.co.bsmsoft.beple_shop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.bsmsoft.beple_shop.common.CommonUtil;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.model.InitModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.InitInfoTask;

public class SplashActivity extends Activity implements NetDefine {

	private final String TAG = "SplashActivity";
    private final static String ACTION_EXIT = "EXIT";
    private final static int MSG_START_APP = 1;
    private final static int MSG_CHECK_UPDATE = 2;
    private final static int MSG_SERVER_INIT_DATA = 6;

    private final static int REQUEST_CODE_TERM = 1000;

	private Setting mSetting;
	private Indicator mIndicator;
    private MainApp mainApp;


	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

                case MSG_START_APP: {

                    Intent i = new Intent(new Intent(SplashActivity.this, LoginActivity.class));
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                    break;
                }
                case MSG_CHECK_UPDATE: {

                    String appVersion = mainApp.getAppVersion();
                    String serverVersion = mainApp.getServerAppVerion();

                    if (appVersion.compareTo(serverVersion) < 0) {
                        UpdateAlertDlg(mainApp.getUpdateMsg()
                                , mainApp.getUpdateAction()
                                , mainApp.getMarketUrl());
                    } else {

                        // Auto Login
                        Message msgStartApp = new Message();
                        msgStartApp.what = MSG_START_APP;
                        sendMessageDelayed(msgStartApp, 2000);
                    }
                    break;
                }

                case MSG_SERVER_INIT_DATA:
                    //초기정보 가져오기
                    getInitInfo();
                    break;
            }
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		mSetting = new Setting(this);
		mIndicator = new Indicator(this, null);

        mainApp = globalVar.getInstance();
        mainApp.setAppVersion(Helper.getVersion(this));

        mHandler.obtainMessage(MSG_SERVER_INIT_DATA).sendToTarget();

    }

	@Override
	protected void onResume() {
		super.onResume();

	}

    /**
     * 초기 접속 정보를 가져온다.
     */
    private void getInitInfo() {

        if (!mIndicator.isShowing()) {
            mIndicator.show();
        }

        InitInfoTask task = new InitInfoTask();
        task.mCallbacks = new AbServerTask.ServerCallbacks(){

            @Override
            public void onSuccess(AbServerTask sender, JSONObject ret) {
                // TODO Auto-generated method stub
                try {

                    if (mIndicator.isShowing())
                        mIndicator.hide();

                    int code = InitInfoTask.responseCode(ret);
                    if (code == RESPONSE_OK) {

                        InitModel info = InitInfoTask.parseInit(ret);

                        mainApp.setInitData(info);

                        String systemMsg = info.getSystemMsg();

                        // system 메시지가 있다면
                        if (CommonUtil.isStringNullOrEmptyCheck(systemMsg)) {

                            String action = info.getSystemAction();
                            SystemAlertDlg(systemMsg, action);

                        } else {

                            mHandler.obtainMessage(MSG_CHECK_UPDATE)
                                    .sendToTarget();
                        }

                    } else {

                        String msg = InitInfoTask.responseMessage(ret);
                        String msgFormat = String.format("Error Code : %d\r\n%s", code, msg);
                        Helper.finishAlert(msgFormat, SplashActivity.this);
                    }

                } catch (JSONException e) {

                    e.printStackTrace();
                    Helper.finishAlert(e.getLocalizedMessage(), SplashActivity.this);
                }
            }

            @Override
            public void onFailed(AbServerTask sender, int code, String msg) {
                // 서버 접속 실패

                if (mIndicator.isShowing())
                    mIndicator.hide();

                Helper.finishAlert(getString(R.string.cannot_connect_server), SplashActivity.this);
            }

        };

        //초기 정보 가져오기
        task.getInitInfo(SplashActivity.this);

    }

	/**
	 * 시스템 메시지 Alert action : GO 계속진행 / EXIT : 종료
	 * 
	 * @param msg
	 * @param action
	 */
	private void SystemAlertDlg(String msg, final String action) {

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setCancelable(false).setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						if (ACTION_EXIT.equals(action)) {
							dialog.dismiss();
							finish();
						} else {
							// 다음 단계는 업데이트 확인
							mHandler.obtainMessage(MSG_CHECK_UPDATE)
									.sendToTarget();
							dialog.dismiss();
						}
					}
				});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(R.string.app_name);
		alert.setMessage(msg);
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}

	/**
	 * 업데이트 Alert
	 * 
	 * @param msg
	 * @param action
	 * @param marketUrl
	 */
	private void UpdateAlertDlg(String msg, final String action, final String marketUrl) {

		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {

								Intent linkIntent = new Intent(
										Intent.ACTION_VIEW, Uri.parse(mainApp.getMarketUrl()));

								startActivity(linkIntent);

								finish();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {

								if (ACTION_EXIT.equals(action)) {
									dialog.dismiss();
									finish();
								} else {
									dialog.dismiss();
									// 앱 실행
                                    Message msgStartApp = new Message();
                                    msgStartApp.what = MSG_START_APP;
                                    mHandler.sendMessageDelayed(msgStartApp, 2000);
								}
							}
						});

		AlertDialog alert = alt_bld.create();
		alert.setTitle(R.string.app_name);
		alert.setMessage(msg);
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}



}



