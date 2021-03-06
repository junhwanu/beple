package kr.co.bsmsoft.beple_shop.mms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import kr.co.bsmsoft.beple_shop.nokia.IMMConstants;
import kr.co.bsmsoft.beple_shop.nokia.MMContent;
import kr.co.bsmsoft.beple_shop.nokia.MMEncoder;
import kr.co.bsmsoft.beple_shop.nokia.MMMessage;
import kr.co.bsmsoft.beple_shop.nokia.MMResponse;
import kr.co.bsmsoft.beple_shop.nokia.MMSender;

/**
 * Created by brady on 15. 12. 9..
 */
public class Message2Manager extends MmsManager {

    private final static String TAG = "Message2Manager";

    private String messageBody;
    private ArrayList<String> address;
    private ArrayList<String> imageList = new ArrayList<String> ();
    private Context context;
    private Settings settings;
    private boolean isWifiEnabled;
    private WifiManager wifi;

    private void initSettings() {

        settings = Settings.get(context);
    }

    public Message2Manager(String message,
                           ArrayList<String> address, Context context) {

        this.messageBody = message;
        this.address = address;
        this.context = context;
    }

    public Message2Manager(String message,
                           ArrayList<String> address,
                           ArrayList<String> imageList, Context context) {

        this.messageBody = message;
        this.address = address;
        this.imageList = imageList;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        initSettings();

        boolean mobileDataEnabled = Utils.isMobileDataEnabled(context);
        if (!mobileDataEnabled) {
            cancel(true);
            mCallbacks.onComplete();
            return;
        }

        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        isWifiEnabled = wifi.isWifiEnabled();

        if (!useWifi(context)) {
            wifi.setWifiEnabled(false);
        }

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
    protected Integer doInBackground( Void... params){

        ApnUtils.initDefaultApns(context, new ApnUtils.OnApnFinishedListener() {
            @Override
            public void onFinished() {
                settings = Settings.get(context, true);

                String[] arrayAddress = address.toArray(new String[0]);

                // 이미지 로드
                Bitmap[] images = null;
                if (imageList != null && imageList.size() > 0) {
                    images = new Bitmap[imageList.size()];

                    for (int i = 0; i < imageList.size(); i++) {
                        String imagePath = imageList.get(i);
                        images[i] = BitmapFactory.decodeFile(imagePath);
                    }
                }

                for (int i = 0; i < arrayAddress.length; i++) {

                    MMMessage mm = new MMMessage();
                    SetMessage(mm, arrayAddress[i], "");

                    // add message
                    AddText(mm, messageBody, 0);

                    // add image
                    for (int k=0; k<images.length; k++) {
                        AddImage(mm, images[k], k+1);
                    }


                    MMEncoder encoder = new MMEncoder();
                    encoder.setMessage(mm);

                    try {
                        encoder.encodeMessage();
                        byte[] out = encoder.getMessage();

                        MMSender sender = new MMSender();
                        String MMSProxy = settings.getMmsProxy();
                        int MMSPort = Integer.valueOf(settings.getMmsPort());
                        final Boolean isProxySet = (MMSProxy != null) && (MMSProxy.trim().length() != 0);

                        sender.setMMSCURL(settings.getMmsc());
                        sender.addHeader("X-NOKIA-MMSC-Charging", "100");

                        MMResponse mmResponse = sender.send(out, isProxySet, MMSProxy, MMSPort);
                        Log.d(TAG, "Message sent to " + sender.getMMSCURL());
                        Log.d(TAG, "Response code: " + mmResponse.getResponseCode() + " " + mmResponse.getResponseMessage());

                        Enumeration keys = mmResponse.getHeadersList();
                        while (keys.hasMoreElements()) {
                            String key = (String) keys.nextElement();
                            String value = (String) mmResponse.getHeaderValue(key);
                            Log.d(TAG, (key + ": " + value));
                        }

                        if (mmResponse.getResponseCode() == 200) {
                            // 200 Successful, disconnect and reset.
                            //endMmsConnectivity();
                            //mSending = false;
                            //mListening = false;
                        } else {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
            }
        });

        return 0;
    }

    @Override
    protected void onPostExecute( Integer result){

        if (!useWifi(context)) {
            wifi.setWifiEnabled(isWifiEnabled);
        }

        mCallbacks.onComplete();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    private void AddImage(MMMessage mm, Bitmap b, int contentId) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 90, os);

        // Adds text content
        MMContent part1 = new MMContent();
        byte[] buf1 = os.toByteArray();
        part1.setContent(buf1, 0, buf1.length);
        String sId = String.format("<%d>", contentId);
        part1.setContentId(sId);
        part1.setType(IMMConstants.CT_IMAGE_JPEG);
        mm.addContent(part1);

    }

    private void AddText(MMMessage mm, String text, int contentId) {

        MMContent part1 = new MMContent();
        byte[] buf = new byte[] {};
        try {
            text = text + " ";
            buf = text.getBytes("euc-kr");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        part1.setContent(buf, 0, buf.length);
        String sId = String.format("<%d>", contentId);
        part1.setContentId(sId);
        part1.setType(IMMConstants.CT_TEXT_HTML);
        mm.addContent(part1);

    }

    private void SetMessage(MMMessage mm, String addresss, String subject) {
        mm.setVersion(IMMConstants.MMS_VERSION_10);
        mm.setMessageType(IMMConstants.MESSAGE_TYPE_M_SEND_REQ);
        mm.setTransactionId("0000000066");
        mm.setDate(new Date(System.currentTimeMillis()));
        mm.setFrom("01028966808/TYPE=PLMN"); // doesnt work, i wish this worked as it should be

        String addr = String.format("%s//TYPE=PLMN", addresss);
        mm.addToAddress(addr);
        mm.setDeliveryReport(false);
        mm.setReadReply(false);
        mm.setSenderVisibility(IMMConstants.SENDER_VISIBILITY_SHOW);
        mm.setSubject(subject);
        mm.setMessageClass(IMMConstants.MESSAGE_CLASS_PERSONAL);
        mm.setPriority(IMMConstants.PRIORITY_LOW);
        mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_MIXED);

//	    In case of multipart related message and a smil presentation available
//	    mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_RELATED);
//	    mm.setMultipartRelatedType(IMMConstants.CT_APPLICATION_SMIL);
//	    mm.setPresentationId("<A0>"); // where <A0> is the id of the content containing the SMIL presentation

    }
}
