package kr.co.bsmsoft.beple_shop;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;
import kr.co.bsmsoft.beple_shop.net.PushTask;


public class GCMIntentService extends GCMBaseIntentService implements NetDefine {
	
	private final String LOG_TAG = "GCMIntentService";
	private MainApp mainApp;

	public GCMIntentService() {
		super(GCM_SENDER_ID);

		mainApp = globalVar.getInstance();
	}
	
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.d(LOG_TAG, arg1);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		
		if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            Setting mSetting = new Setting(this);
            boolean value = mSetting.getBoolean(KEY_ALLOW_PUSH, true);
            if (value) {
                showMessage(context, intent);
            }
		}	
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {

		// save client registration id for sending message
        MainApp mainApp = globalVar.getInstance();
		PushTask task = new PushTask(mainApp.getToken());
		task.registerPush(mainApp.getShopInfo().getId(), arg1, arg0) ;
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	private void showMessage(Context context, Intent intent){

		try {
			String title = intent.getStringExtra("title");
			String msg = intent.getStringExtra("msg");
			String ticker = intent.getStringExtra("ticker");
			String msgId = intent.getStringExtra("msgId");
			int id = Integer.parseInt(msgId);

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

			PendingIntent pendingIntent = null;
			Intent i = new Intent(context, SplashActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			pendingIntent = PendingIntent.getActivity(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;
			notification.tickerText = ticker;
			notification.when = System.currentTimeMillis();
			notification.vibrate = new long[]{500, 100, 500, 100};
			notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			//notification.setLatestEventInfo(context, title, msg, pendingIntent);

			notificationManager.notify(id, notification);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}    
}
