package kr.co.bsmsoft.beple_shop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;

import com.google.android.mms.APNHelper;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

import kr.co.bsmsoft.beple_shop.nokia.IMMConstants;
import kr.co.bsmsoft.beple_shop.nokia.MMContent;
import kr.co.bsmsoft.beple_shop.nokia.MMEncoder;
import kr.co.bsmsoft.beple_shop.nokia.MMMessage;
import kr.co.bsmsoft.beple_shop.nokia.MMResponse;
import kr.co.bsmsoft.beple_shop.nokia.MMSender;

public class SendMMSActivity extends Activity {

	private static final String TAG = "SendMMSActivity";
	private ConnectivityManager mConnMgr;
	private PowerManager.WakeLock mWakeLock;
	private ConnectivityBroadcastReceiver mReceiver;

	private NetworkInfo mNetworkInfo;
	private NetworkInfo mOtherNetworkInfo;

	public enum State {
		UNKNOWN,
		CONNECTED,
		NOT_CONNECTED
	}

	private State mState;
	private boolean mListening;
	private boolean mSending;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mListening = true;
		mSending = false;
		mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mReceiver = new ConnectivityBroadcastReceiver();


		new MessageManager().execute();

		/*
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, filter);

		try {
			
			// Ask to start the connection to the APN. Pulled from Android source code.
			int result = beginMmsConnectivity();

			if (result != PhoneEx.APN_ALREADY_ACTIVE) {
				Log.v(TAG, "Extending MMS connectivity returned " + result + " instead of APN_ALREADY_ACTIVE");
				// Just wait for connectivity startup without
				// any new request of APN switch.
				return;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	protected void endMmsConnectivity() {
		// End the connectivity
		try {
			Log.v(TAG, "endMmsConnectivity");
			if (mConnMgr != null) {
				mConnMgr.stopUsingNetworkFeature(
						ConnectivityManager.TYPE_MOBILE,
						PhoneEx.FEATURE_ENABLE_MMS);
			}
		} finally {
			releaseWakeLock();
		}
	}

	protected int beginMmsConnectivity() throws IOException {
		// Take a wake lock so we don't fall asleep before the message is downloaded.
		createWakeLock();

		int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, PhoneEx.FEATURE_ENABLE_MMS);

		Log.v(TAG, "beginMmsConnectivity: result=" + result);

		switch (result) {
		case PhoneEx.APN_ALREADY_ACTIVE:
		case PhoneEx.APN_REQUEST_STARTED:
			acquireWakeLock();
			return result;
		}

		throw new IOException("Cannot establish MMS connectivity");
	}

	private synchronized void createWakeLock() {
		// Create a new wake lock if we haven't made one yet.
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS Connectivity");
			mWakeLock.setReferenceCounted(false);
		}
	}

	private void acquireWakeLock() {
		// It's okay to double-acquire this because we are not using it
		// in reference-counted mode.
		mWakeLock.acquire();
	}

	private void releaseWakeLock() {
		// Don't release the wake lock if it hasn't been created and acquired.
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}
	
	private void sendMMSUsingNokiaAPI()
	{
		// Magic happens here.
		
		MMMessage mm = new MMMessage();
	    SetMessage(mm);
	    AddContents(mm);

	    MMEncoder encoder=new MMEncoder();
	    encoder.setMessage(mm);

	    try {
	      encoder.encodeMessage();
	      byte[] out = encoder.getMessage();
	      
	      MMSender sender = new MMSender();
	      APNHelper apnHelper = new APNHelper(this);
	      //List<APN> results = apnHelper.getMMSApns();

	      //if(results.size() > 0){

	    	  final String MMSCenterUrl = "http://mmsc.ktfwing.com:9082"; // "http://omms.nate.com:9082/oma_mms"; //results.get(0).MMSCenterUrl;
	    	  final String MMSProxy = ""; //"smart.nate.com"; // results.get(0).MMSProxy;
	    	  final int MMSPort = 80;//9093;// Integer.valueOf(results.get(0).MMSPort);

				//final String MMSCenterUrl = "http://omms.nate.com:9082/oma_mms"; //results.get(0).MMSCenterUrl;
				//final String MMSProxy = "smart.nate.com"; // results.get(0).MMSProxy;
				//final int MMSPort = 9093; // Integer.valueOf(results.get(0).MMSPort);

	    	  final Boolean  isProxySet =   (MMSProxy != null) && (MMSProxy.trim().length() != 0);
	    	  
	    	  sender.setMMSCURL(MMSCenterUrl);
	    	  sender.addHeader("X-NOKIA-MMSC-Charging", "100");

		      MMResponse mmResponse = sender.send(out, isProxySet, MMSProxy, MMSPort);
		      Log.d(TAG, "Message sent to " + sender.getMMSCURL());
		      Log.d(TAG, "Response code: " + mmResponse.getResponseCode() + " " + mmResponse.getResponseMessage());

		      Enumeration keys = mmResponse.getHeadersList();
		      while (keys.hasMoreElements()){
		        String key = (String) keys.nextElement();
		        String value = (String) mmResponse.getHeaderValue(key);
		        Log.d(TAG, (key + ": " + value));
		      }
		      
		      if(mmResponse.getResponseCode() == 200)
		      {
		    	  // 200 Successful, disconnect and reset.
		    	  endMmsConnectivity();
		    	  mSending = false;
		    	  mListening = false;
		      }
		      else
		      {
		    	  // kill dew :D hhaha
		      }
	      //}
	    } catch (Exception e) {
			e.printStackTrace();
	    }
	}
	
	private void SetMessage(MMMessage mm) {
	    mm.setVersion(IMMConstants.MMS_VERSION_10);
	    mm.setMessageType(IMMConstants.MESSAGE_TYPE_M_SEND_REQ);
	    mm.setTransactionId("0000000066");
	    mm.setDate(new Date(System.currentTimeMillis()));
	    mm.setFrom("01028966808/TYPE=PLMN"); // doesnt work, i wish this worked as it should be
	    mm.addToAddress("01028966808/TYPE=PLMN"); //01022520902
	    mm.setDeliveryReport(false);
	    mm.setReadReply(false);
	    mm.setSenderVisibility(IMMConstants.SENDER_VISIBILITY_SHOW);
	    mm.setSubject("This is a nice message!!");
	    mm.setMessageClass(IMMConstants.MESSAGE_CLASS_PERSONAL);
	    mm.setPriority(IMMConstants.PRIORITY_LOW);
	    mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_MIXED);
	    
//	    In case of multipart related message and a smil presentation available
//	    mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_RELATED);
//	    mm.setMultipartRelatedType(IMMConstants.CT_APPLICATION_SMIL);
//	    mm.setPresentationId("<A0>"); // where <A0> is the id of the content containing the SMIL presentation
	    
	  }

	  private void AddContents(MMMessage mm) {
	    /*Path where contents are stored*/

		// You need to have this file in your SD. Otherwise error..


		File file = new File(Environment.getExternalStorageDirectory(), "19.png"); 
	    Uri outputFileUri = Uri.fromFile( file );
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    Bitmap b;

    	try {
			b = Media.getBitmap(getContentResolver(), outputFileUri);
			b.compress(CompressFormat.JPEG, 90, os);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    // Adds text content
	    MMContent part1 = new MMContent();
	    byte[] buf1 = os.toByteArray();
	    part1.setContent(buf1, 0, buf1.length);
	    part1.setContentId("<0>");
	    part1.setType(IMMConstants.CT_IMAGE_JPEG);
	    mm.addContent(part1);


		  MMContent part2 = new MMContent();
		  byte[] buf2 = new byte[] {};
		  try {
			  buf2 = "본문테스트<br>http://www.naver.com".getBytes("euc-kr"); //euc-kr
		  } catch (UnsupportedEncodingException e) {
			  e.printStackTrace();
		  }
		  part2.setContent(buf2, 0, buf2.length);
		  part2.setContentId("<1>");
		  part2.setType(IMMConstants.CT_TEXT_HTML);
		  mm.addContent(part2);

		  //part2.setType(IMMConstants.CT_TEXT_HTML + "; charset=\"euc-kr\";");


	  }

	private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || mListening == false) {
				Log.w(TAG, "onReceived() called with " + mState.toString() + " and " + intent);
				return;
			}

			boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			if (noConnectivity) {
				mState = State.NOT_CONNECTED;
			} else {
				mState = State.CONNECTED;
			}

			mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			mOtherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

//			mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
//			mIsFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);


			// Check availability of the mobile network.
			if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
				Log.v(TAG, "   type is not TYPE_MOBILE_MMS, bail");
				return;
			}

			if (!mNetworkInfo.isConnected()) {
				Log.v(TAG, "   TYPE_MOBILE_MMS not connected, bail");
				return;
			}
			else
			{ 
				Log.v(TAG, "connected..");

				if(mSending == false)
				{
					mSending = true;
					sendMMSUsingNokiaAPI();
				}
			}
		}
	};



	public class MessageManager extends AsyncTask<Void, Void, Integer> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();


		}

		@Override
		protected Integer doInBackground( Void... params){

			sendMMSUsingNokiaAPI();
			return 0;
		}



		@Override
		protected void onPostExecute( Integer result){

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

	}
}