package kr.co.bsmsoft.beple_shop.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Set;

public class Setting {

	// sharedPrefrences 변수 
	private Context context;
	private SharedPreferences mPreConfig;

	// 아무것도 입력이 되어있지 않을때
	public final static boolean initBool = false;
	public final static String initString  = "initValue";
	public final static String emptyString  = "";
	public final static int 	initInt		=	 -1;

	static HashMap<String, OnPreferenceChangeListener> listeners
	= new HashMap<String, OnPreferenceChangeListener>();

	/**
	 * <code>name</code>은 {@link android.preference.Preference}를 생성할 파일 이름을 나타냄.
	 * @param name
	 * @param ctx
	 */

	public Setting(Context ctx){
		this.context = ctx;
		mPreConfig = context.getSharedPreferences( "data" , Context.MODE_PRIVATE );
	}

	public boolean inputBooleanHash(HashMap<String, Boolean> hash){
		SharedPreferences.Editor ed = mPreConfig.edit();
		Set<String> keys = hash.keySet();
		for(String key : keys){
			ed.putBoolean( key , hash.get(key) );
		}
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(hash);
		}
		return b;
	}

	public boolean inputIntHash(HashMap<String, Integer> hash){
		SharedPreferences.Editor ed = mPreConfig.edit();
		Set<String> keys = hash.keySet();
		for(String key : keys){
			ed.putInt( key , hash.get(key) );
		}
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(hash);
		}
		return b;
	}

	public boolean inputStringHash(HashMap<String, String> hash){
		SharedPreferences.Editor ed = mPreConfig.edit();
		Set<String> keys = hash.keySet();
		for(String key : keys){
			ed.putString( key , hash.get(key) );
		}
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(hash);
		}
		return b;
	}

	public boolean input(String key, String value){
		SharedPreferences.Editor ed = mPreConfig.edit();
		ed.putString( key , value );
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(key, value);
		}
		return b;
	}

	public boolean input(String key, int value){
		SharedPreferences.Editor ed = mPreConfig.edit();
		ed.putInt( key , value );
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(key, value);
		}
		return b;
	}

	public boolean input(String key, boolean value){
		SharedPreferences.Editor ed = mPreConfig.edit();
		ed.putBoolean( key , value );
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(key, value);
		}
		return b;
	}

	public boolean input(String key, long value){
		SharedPreferences.Editor ed = mPreConfig.edit();
		ed.putLong( key , value );
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(key, value);
		}
		return b;
	}

	public boolean input(String key, float value){
		SharedPreferences.Editor ed = mPreConfig.edit();
		ed.putFloat(key, value);
		boolean b = ed.commit();
		if(b){
			sendMsgToListener(key, value);
		}
		return b;
	}

	 // 값(Key Data) 삭제하기
	public void del(String key){
		SharedPreferences.Editor ed = mPreConfig.edit();
		ed.remove(key);
		ed.commit();
	}

	public boolean getBoolean(String key, boolean def){
		return mPreConfig.getBoolean(key, def);
	}

	public int getInt(String key, int def){
		return mPreConfig.getInt(key, def);
	}

	public String getString(String key, String def){
		return mPreConfig.getString(key, def);
	}

	public long getLong(String key, long defValue){
		return mPreConfig.getLong(key, Long.parseLong(defValue + ""));
	}
	public float getFloat(String key, float defValue){
		return mPreConfig.getFloat(key, defValue);
	}

	private void sendMsgToListener(final String key, final Object value){
		new Thread(){
			public void run() {
				Set<String> keys = listeners.keySet();
				for(String k : keys){
					OnPreferenceChangeListener l = (OnPreferenceChangeListener)listeners.get(k);
					l.onChange(key, value);
				}
			}
		}.start();
	}


	private void sendMsgToListener(final HashMap<String, ?> hash){
		new Thread(){
			public void run() {
				Set<String> keys = listeners.keySet();
				for(String k : keys){
					OnPreferenceChangeListener l = (OnPreferenceChangeListener)listeners.get(k);
					l.onChangeInBatch(hash);
				}

			}
		}.start();
	}

	/**
	 * {@link android.preference.Preference}의 변화를 알기위해서 listener를 세팅함
	 * @param key 
	 * @param listener
	 */
	public void putListener(String key, OnPreferenceChangeListener listener){
		listeners.put(key, listener);
	}

	public void removeListener(String key){
		listeners.remove(key);
	}

	public interface OnPreferenceChangeListener {
		public void onChange(String key, Object value);
		public void onChangeInBatch(HashMap<String, ?> hash);
	}

}
