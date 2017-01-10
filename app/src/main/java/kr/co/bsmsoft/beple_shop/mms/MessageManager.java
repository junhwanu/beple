package kr.co.bsmsoft.beple_shop.mms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;
import kr.co.bsmsoft.beple_shop.model.LottoModel;
import kr.co.bsmsoft.beple_shop.model.LottoSetModel;
import kr.co.bsmsoft.beple_shop.nokia.IMMConstants;
import kr.co.bsmsoft.beple_shop.nokia.MMContent;
import kr.co.bsmsoft.beple_shop.nokia.MMEncoder;
import kr.co.bsmsoft.beple_shop.nokia.MMMessage;
import kr.co.bsmsoft.beple_shop.nokia.MMResponse;
import kr.co.bsmsoft.beple_shop.nokia.MMSender;

/**
 * Created by brady on 15. 12. 9..
 */
public class MessageManager extends MmsManager {

    private final static String TAG = "MessageManager";

    private String messageBody;
    private ArrayList<String> address;
    private ArrayList<CustomerModel> customerModels;
    private LottoSetModel lottoSet;
    private ArrayList<String> name;
    private ArrayList<String> imageList = new ArrayList<String> ();
    private Context context;
    private Settings settings;
    private boolean isWifiEnabled;
    private WifiManager wifi;
    private Boolean useName = false;
    private Boolean addLotto = false;

    private void initSettings() {

        settings = Settings.get(context);
    }

    public MessageManager(String message,
                          ArrayList<String> address, Context context) {

        this.messageBody = message;
        this.address = address;
        this.context = context;
    }

    public MessageManager(String message,
                          ArrayList<String> address,
                          ArrayList<String> imageList, Context context) {

        this.messageBody = message;
        this.address = address;
        this.imageList = imageList;
        this.context = context;
    }

    public MessageManager(String message,
                          ArrayList<String> address, ArrayList<String> name,
                          ArrayList<String> imageList, Context context) {

        this.messageBody = message;
        this.address = address;
        this.name = name;
        this.imageList = imageList;
        this.context = context;
    }

    public MessageManager(String message,
                          ArrayList<CustomerModel> customerModels, Boolean useName,
                          ArrayList<String> imageList, Context context) {

        this.messageBody = message;
        this.customerModels = customerModels;
        this.context = context;
        this.imageList = imageList;
        this.useName = useName;
        addLotto = false;
    }


    public MessageManager(String message,
                          ArrayList<CustomerModel> customerModels, LottoSetModel lottoSet, Boolean useName,
                          ArrayList<String> imageList, Context context) {

        this.messageBody = message;
        this.customerModels = customerModels;
        this.lottoSet = lottoSet;
        this.context = context;
        this.imageList = imageList;
        this.useName = useName;
        addLotto = true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        initSettings();

        boolean mobileDataEnabled = Utils.isMobileDataEnabled(context);
        if (!mobileDataEnabled) {
            cancel(true);
            mCallbacks.onFailed("모바일 데이터를 사용할수 없습니다.");
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

                String mmsc = settings.getMmsc();

                sendMMSwithLottoSet();
                //sendMMSForKT();
/*
                if (mmsc.contains("ktfwing")) {
                    sendMMSForKT();
                }else if (mmsc.contains("uplus")) {
                    sendMMSForKT();
                }else{
                    sendMMS();
                }
*/
            }
        });

        return 0;
    }

    /*
    private void sendMMS() {

        String[] arrayAddress = address.toArray(new String[0]);

        com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
        sendSettings.setMmsc(settings.getMmsc());
        sendSettings.setProxy(settings.getMmsProxy());
        sendSettings.setPort(settings.getMmsPort());
        sendSettings.setUseSystemSending(false);
        sendSettings.setDeliveryReports(true);
        sendSettings.setMimetype(settings.getMmsMimetype());

        //Intent syncIntent = new Intent(context, LottoEventViewActivity.class);
        //PendingIntent pIntent = PendingIntent.getActivity(context, 0, syncIntent, 0);
        //sendSettings.setPendingIntent(pIntent);

        Bitmap[] images = null;
        if (imageList != null && imageList.size() > 0) {
            images = new Bitmap[imageList.size()];

            for (int i = 0; i < imageList.size(); i++) {
                String imagePath = imageList.get(i);
                images[i] = BitmapFactory.decodeFile(imagePath);
            }
        }

        //String arrayAddress1[]={"01022520902", "01022520902","01022520902","01022520902","01022520902",};

        for (int i = 0; i < arrayAddress.length; i++) {
            Transaction transaction = new Transaction(context, sendSettings);

            Message message = new Message(messageBody, arrayAddress[i]);
            message.setType(Message.TYPE_SMSMMS);
            message.setSave(false);
            message.setImages(images);

            //message.addVideo();
            transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);

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
*/
    private void sendMMSwithLottoSet() {
        // 이미지 로드
        Bitmap[] images = null;
        if (imageList != null && imageList.size() > 0) {
            images = new Bitmap[imageList.size()];

            for (int i = 0; i < imageList.size(); i++) {
                String imagePath = imageList.get(i);
                images[i] = BitmapFactory.decodeFile(imagePath);
            }
        }

        //for (int i = 0; i < customerModels.size(); i++) {
        for(CustomerModel model : customerModels) {
            String _messageBody = messageBody;

            // 선택되지 않은 Customer
            if(model.isSelected() == 0) continue;

            MMMessage mm = new MMMessage();
            SetMessage(mm, model.getPhone(), "");

            if(useName) {
                if(model.getCustomerName() != null && model.getCustomerName().length() > 0) {
                    _messageBody = _messageBody.replace(context.getString(R.string.str_name), model.getCustomerName());
                }
                else if(model.getPhone() != null && model.getPhone().length() > 0)
                    _messageBody = _messageBody.replace(context.getString(R.string.str_name), model.getPhone());
                else
                    _messageBody = _messageBody.replace(context.getString(R.string.str_name), "");
            }

            _messageBody = _messageBody.replace("\n", "\r\n");

            if(addLotto) {
                // add lotto set to messageBody
                String msgLotto = createLottoMessage(lottoSet.getLottoSetByCustomerID(model.getId()), model.getPhone());

                // add message
                _messageBody = _messageBody + "\r\n" + msgLotto;
            }

            AddText(mm, _messageBody, 0);

            // add images
            if (imageList != null && imageList.size() > 0) {
                try {

                    for (int k=0; k<images.length; k++) {
                        String imagePath = imageList.get(k);

                        byte[] byteArray = null;
                        File file = new File(imagePath);
                        InputStream inputStream = new FileInputStream(file);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] b = new byte[1024 * 8];
                        int bytesRead = 0;

                        while ((bytesRead = inputStream.read(b)) != -1) {
                            bos.write(b, 0, bytesRead);
                        }

                        byteArray = bos.toByteArray();

                        String fileName = file.getName().toLowerCase();
                        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

                        String imageType = IMMConstants.CT_IMAGE_JPEG;
                        if (fileExt.equals("gif")) {
                            imageType = IMMConstants.CT_IMAGE_GIF;
                        }else if (fileExt.equals("png")) {
                            imageType = IMMConstants.CT_IMAGE_PNG;
                        }

                        AddImage(mm, byteArray, k+1, imageType);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // add image
            //for (int k=0; k<images.length; k++) {
            //    AddImage(mm, images[k], k+1);
            //}


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

    private void sendMMSForKT() {

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

            // add images
            if (imageList != null && imageList.size() > 0) {
                try {

                    for (int k=0; k<images.length; k++) {
                        String imagePath = imageList.get(k);

                        byte[] byteArray = null;
                        File file = new File(imagePath);
                        InputStream inputStream = new FileInputStream(file);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] b = new byte[1024 * 8];
                        int bytesRead = 0;

                        while ((bytesRead = inputStream.read(b)) != -1) {
                            bos.write(b, 0, bytesRead);
                        }

                        byteArray = bos.toByteArray();

                        String fileName = file.getName().toLowerCase();
                        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

                        String imageType = IMMConstants.CT_IMAGE_JPEG;
                        if (fileExt.equals("gif")) {
                            imageType = IMMConstants.CT_IMAGE_GIF;
                        }else if (fileExt.equals("png")) {
                            imageType = IMMConstants.CT_IMAGE_PNG;
                        }

                        AddImage(mm, byteArray, k+1, imageType);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // add image
            //for (int k=0; k<images.length; k++) {
            //    AddImage(mm, images[k], k+1);
            //}


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

    private void AddImage(MMMessage mm, byte[] buf, int contentId, String imageType) {

        // Adds text content
        MMContent part1 = new MMContent();
        part1.setContent(buf, 0, buf.length);
        String sId = String.format("<%d>", contentId);
        part1.setContentId(sId);
        part1.setType(imageType);
        mm.addContent(part1);

    }

    /*
    private void AddImage(MMMessage mm, Bitmap b, int contentId) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //b.compress(Bitmap.CompressFormat.PNG, 100, os);

        // Adds text content
        MMContent part1 = new MMContent();
        byte[] buf1 = os.toByteArray();
        part1.setContent(buf1, 0, buf1.length);
        String sId = String.format("<%d>", contentId);
        part1.setContentId(sId);
        part1.setType(IMMConstants.CT_IMAGE_GIF); //CT_IMAGE_JPEG
        mm.addContent(part1);

    }
    */

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

        String mmsc = settings.getMmsc();
        if (mmsc.contains("ktfwing")) {
            //part1.setType(settings.getMmsMimetype());
            part1.setType(settings.getMmsMimetype() + ";charset=\"euc-kr\";");
        }else{
            part1.setType(settings.getMmsMimetype() + ";charset=\"euc-kr\";");
        }

        mm.addContent(part1);

    }

    private void SetMessage(MMMessage mm, String addresss, String subject) {

        String sender = Helper.getPhoneNumber(context);
        mm.setVersion(IMMConstants.MMS_VERSION_10);
        mm.setMessageType(IMMConstants.MESSAGE_TYPE_M_SEND_REQ);
        mm.setTransactionId("0000000066");
        mm.setDate(new Date(System.currentTimeMillis()));
        mm.setFrom(sender + "/TYPE=PLMN"); // doesnt work, i wish this worked as it should be

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



    private byte[] readVideo(String videoPath) {

        try {
            @SuppressWarnings("resource")
            FileInputStream v_input = new FileInputStream(videoPath);
            ByteArrayOutputStream objByteArrayOS = new ByteArrayOutputStream();
            byte[] byteBufferString = new byte[1024];
            for (int readNum; (readNum = v_input.read(byteBufferString)) != -1;)
            {
                objByteArrayOS.write(byteBufferString, 0, readNum);
                System.out.println("read " + readNum + " bytes,");
            }

            return objByteArrayOS.toByteArray();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String createLottoMessage(ArrayList<LottoModel> lottoModelArrayList, String phone) {
        String msg = "";

        // Add header
        msg += context.getString(R.string.str_lotto_header);
        msg += context.getString(R.string.str_lotto_times);
        msg += context.getString(R.string.str_lotto_day);

        // Change temp times and date to real value
        msg = msg.replace("000", String.valueOf(lottoSet.getTimes()));

        String date = lottoModelArrayList.get(0).getSort_dt();
        msg = msg.replace("yyyy-mm-dd",date);

        // Add body
        int index = 1;
        for(LottoModel model : lottoModelArrayList) {
            msg = msg + "【응모권" + index++ + "】\r\n";
            for(int i=0;i<6;i++) {
                msg = msg + model.getLotto_num().get(i);
                if(i < 5) msg = msg + ", ";
            }
            msg = msg + "\r\n";
        }

        // Add tail
        msg += context.getString(R.string.str_lotto_tail);

        return msg;
    }
}
