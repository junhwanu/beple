package kr.co.bsmsoft.beple_shop.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.telephony.TelephonyManager;
import android.os.AsyncTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.R;

public class Helper implements NetDefine {
	
	public Helper() {}

   	public static String getVersion(Context context) {
   		
   		String version = null;
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			version = packageInfo.versionName;

		}catch(NameNotFoundException e)  {
			version = "";	
		}
		
		return version;
	}

    public static String getPackageName(Context context) {

        String pkgName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            pkgName = packageInfo.packageName;

        }catch(NameNotFoundException e)  {
            pkgName = "";
        }

        return pkgName;
    }


    public static String getUserKey(Context context) {
  		
  		String androidId = android.provider.Settings.Secure
  				.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		return androidId;
	}

    public static String getPhoneNumber(Context context) {

		String phoneNum = "";
		try {

			TelephonyManager mgr = (TelephonyManager) context
					.getSystemService(context.TELEPHONY_SERVICE);

			phoneNum = mgr.getLine1Number();
			if (phoneNum != null) {
				phoneNum = phoneNum.replace("-", "").replace("+82", "0");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
        return phoneNum;
    }

   	public static void finishAlert(String msg, final Activity activity) {
   		
   		try{
			AlertDialog.Builder altBld = new AlertDialog.Builder(activity);
			altBld.setCancelable(false).setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
	
						dialog.dismiss();
						activity.finish();
					}
				});
			
			AlertDialog alert = altBld.create();
			alert.setTitle(R.string.app_name);
			alert.setMessage(msg);
			alert.setIcon(R.mipmap.ic_launcher);
			alert.show();
			
	   	}catch (Exception e) {
				e.printStackTrace();
			}
		}	


   	public static void alert(String msg, final Context context) {
		
   		try {
			AlertDialog.Builder altBld = new AlertDialog.Builder(context);
			altBld.setCancelable(false).setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
	
						dialog.dismiss();
					}
				});
			
			AlertDialog alert = altBld.create();
			alert.setTitle(R.string.app_name);
			alert.setMessage(msg);
			alert.setIcon(R.mipmap.ic_launcher);
			alert.show();
			
   		}catch (Exception e) {
   			e.printStackTrace();
   		}
	}

	public static void sweetAlert(String msg, String title, int msgType, Context context) {

		new SweetAlertDialog(context, msgType)
				.setTitleText(title)
				.setContentText(msg)
				.setConfirmText("확인")
				.show();
	}

	public static void sweetFinishAlert(String msg, String title, int msgType, final Activity activity) {

		SweetAlertDialog dlg = new SweetAlertDialog(activity, msgType);
		dlg.setTitleText(title);
		dlg.setContentText(msg);
		dlg.setConfirmText("확인");
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				activity.finish();
			}
		});

		dlg.show();
	}


	private static class TIME_MAXIMUM{
		public static final int SEC = 60;
		public static final int MIN = 60;
		public static final int HOUR = 24;
		public static final int DAY = 30;
		public static final int MONTH = 12;
	}

	public static String formatTimeString(String strDate) {

		DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date tempDate = null;
		try {
			tempDate = sdFormat.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}

		long curTime = System.currentTimeMillis();
		long regTime = tempDate.getTime();
		long diffTime = (curTime - regTime) / 1000;

		String msg = null;
		if (diffTime < TIME_MAXIMUM.SEC) {
			// sec
			msg = "방금 전";
		} else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
			// min
			msg = diffTime + "분 전";
		} else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
			// hour
			msg = (diffTime) + "시간 전";
		} else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
			// day
			msg = (diffTime) + "일 전";
		} else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
			// day
			msg = (diffTime) + "달 전";
		} else {
			msg = (diffTime) + "년 전";
		}
		return msg;
	}

	public static Bitmap decodeSampledBitmapFromResource(String path, int reqSize) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		int originHeight = options.outHeight;
		int originWidth = options.outWidth;

		int scaleFactor = 1;

		if (originHeight >= originWidth) {
			// 세로 사진
			scaleFactor = originHeight / reqSize;
		}else{
			// 가로 사진
			scaleFactor = originWidth / reqSize;
		}

		options.inSampleSize = scaleFactor;
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		try {
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			int angle = 0;
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					angle = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					angle = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					angle = 270;
					break;
				default:
					break;
			}

			Matrix mat = new Matrix();
			mat.postRotate(angle);

			Bitmap resizedBitmap =
					Bitmap.createBitmap(bitmap, 0, 0, options.outWidth, options.outHeight, mat, true);

			System.gc();

			return resizedBitmap;

		}catch (Exception e) {
			e.printStackTrace();
			System.gc();
			return bitmap;
		}

	}
}
