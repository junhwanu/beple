package kr.co.bsmsoft.beple_shop.mms;

import android.os.AsyncTask;

/**
 * Created by brady on 15. 12. 9..
 */
public class MmsManager extends AsyncTask<Void, Void, Integer> {

    private final static String TAG = "MmsManager";

    public Callbacks mCallbacks = sDummyCallbacks;

    public interface Callbacks {
        public void onComplete();
        public void onFailed(String errMessage);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onComplete() {

        }

        @Override
        public void onFailed(String errMessage) {

        }
    };


    @Override
    protected Integer doInBackground( Void... params){

        return 0;
    }

}
