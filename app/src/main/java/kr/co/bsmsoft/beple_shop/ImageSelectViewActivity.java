package kr.co.bsmsoft.beple_shop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.EndlessScrollListener;
import kr.co.bsmsoft.beple_shop.adapter.ImageListAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.CategoryModel;
import kr.co.bsmsoft.beple_shop.model.ImageModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.CategoryTask;
import kr.co.bsmsoft.beple_shop.net.ImageTask;

public class ImageSelectViewActivity extends AppCompatActivity implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener, ImageListAdapter.Callbacks {

    private Toolbar toolbar;
    private ImageListAdapter adapter;
    private MainApp mainApp;
    private ListView imageList;
    private Indicator mIndicator;
    private TextView txtMessage, txtCategory;
    private ImageButton btnChange;
    private ArrayList<CategoryModel> categoryList;
    private int currentPage = 1;


    private final static int MSG_LOAD_IMAGE_LIST = 1;
    private final static int MSG_CATEGORY_LIST = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_LOAD_IMAGE_LIST: {

                    ImageTask task = new ImageTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = ImageTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    ArrayList<ImageModel> imageList = ImageTask.parseImageList(ret);
                                    updateAdapter(imageList);
                                }else{
                                    updateAdapter(new ArrayList<ImageModel>());
                                    Helper.alert(ImageTask.responseMessage(ret), ImageSelectViewActivity.this);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), ImageSelectViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", ImageSelectViewActivity.this);
                        }

                    };

                    task.getImageList(mainApp.getShopInfo().getId(), selectedCategory.getId(), currentPage);

                    break;
                }

                case MSG_CATEGORY_LIST: {

                    CategoryTask task = new CategoryTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = CategoryTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    categoryList = CategoryTask.parseCategoryList(ret);
                                    CategoryModel shopCategory = new CategoryModel();
                                    shopCategory.setId(0);
                                    shopCategory.setCategoryName("가맹점 홍보이미지");
                                    categoryList.add(0, shopCategory);
                                    selectedCategory = categoryList.get(0);

                                    obtainMessage(MSG_LOAD_IMAGE_LIST).sendToTarget();

                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), ImageSelectViewActivity.this);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", ImageSelectViewActivity.this);
                        }

                    };

                    task.getCategoryList();

                    break;
                }

            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mIndicator = new Indicator(this, null);
        mainApp = globalVar.getInstance();

        imageList = (ListView) findViewById(R.id.imageListView);

        txtMessage = (TextView) findViewById(R.id.txtMessage);
        txtCategory = (TextView) findViewById(R.id.txtCategory);
        btnChange = (ImageButton) findViewById(R.id.btnChange);
        btnChange.setOnClickListener(this);

        adapter = new ImageListAdapter(this, new ArrayList<ImageModel>());
        adapter.mCallbacks = this;
        adapter.setListView(imageList);

        imageList.setAdapter(adapter);
        imageList.setOnItemClickListener(this);
        imageList.setOnScrollListener(scrollListener);

        mHandler.obtainMessage(MSG_CATEGORY_LIST).sendToTarget();
    }

    private EndlessScrollListener scrollListener = new EndlessScrollListener() {

        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            currentPage++;
            mHandler.obtainMessage(MSG_LOAD_IMAGE_LIST).sendToTarget();
        }
    };

    private void updateAdapter(ArrayList<ImageModel> list) {

        if (list != null) {
            adapter.addAll(list);
        }
    }

    private void initDataSet() {
        currentPage = 1;
        adapter.clear();
        scrollListener.reset();
        adapter.notifyDataSetChanged();
        mHandler.obtainMessage(MSG_LOAD_IMAGE_LIST).sendToTarget();
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

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnChange) {
            selectCategory();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        final ImageModel item = adapter.getItem(i);

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("이미지 추가")
                .setContentText("선택하신 이미지를 추가하시겠습니까?")
                .setConfirmText("확인")
                .setCancelText("취소")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        sDialog.dismiss();
                        try{Thread.sleep(200);}catch(Exception e){}
                        new DownloadTask().execute(item);
                    }
                })
                .show();

    }

    @Override
    public void onImageSelect(int position) {

        final ImageModel item = adapter.getItem(position);

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("이미지 추가")
                .setContentText("선택하신 이미지를 추가하시겠습니까?")
                .setConfirmText("확인")
                .setCancelText("취소")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        sDialog.dismiss();
                        try{Thread.sleep(200);}catch(Exception e){}
                        new DownloadTask().execute(item);
                    }
                })
                .show();
    }


    private class DownloadTask extends AsyncTask<ImageModel, Void, Integer> {

        private static final int S_OK = 1;
        private static final int F_ERR = 0;
        private File outputFile = null;
        private SweetAlertDialog pDialog;
        private ImageModel image;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new SweetAlertDialog(ImageSelectViewActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("다운로드 중...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Integer doInBackground( ImageModel... params){

            image = params[0];
            int count = 0;

            outputFile = new File(getFilesDir(), image.getFileName());

            if (outputFile.exists()) {
                return S_OK;
            }else {

                try {
                    String imageServerPath = String.format("%s/%s", image.getServerAddress(), image.getFileUrl());
                    URL url = new URL(imageServerPath);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d("DownloadTask", "response code : " + connection.getResponseCode());
                        connection.disconnect();
                        return F_ERR;
                    }

                    int lenghtOfFile = connection.getContentLength();
                    if (lenghtOfFile == -1) {
                        Log.d("DownloadTask", "contentLength : " + lenghtOfFile);
                        return F_ERR;
                    }

                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(outputFile);

                    byte data[] = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                    connection.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                    return F_ERR;
                }
            }

            return S_OK;
        }

        @Override
        protected void onPostExecute(Integer result){

            if (result == S_OK) {

                pDialog.dismissWithAnimation();

                Intent i = new Intent();
                i.putExtra(KEY_IMAGE_PATH, outputFile.getAbsolutePath());
                i.putExtra(KEY_SERVER_ADDR, image.getServerAddress());
                i.putExtra(KEY_FILE_URL, image.getFileUrl());
                setResult(RESULT_OK, i);
                finish();

            }else{

                pDialog.setTitleText("다운로드 실패")
                        .setContentText("이미지 다운로드에 실패하였습니다. 관리자에게 문의해 주세요.")
                        .setConfirmText("확인")
                        .setConfirmClickListener(null)
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    private int selectedIndex = 0;
    private CategoryModel selectedCategory;

    private void selectCategory() {

        final String arrayCategory[] = new String[categoryList.size()];

        for (int i=0; i<categoryList.size(); i++) {
            arrayCategory[i] = categoryList.get(i).getCategoryName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이미지 카테고리");
        builder.setSingleChoiceItems(arrayCategory, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                selectedIndex = whichButton;
            }
        });
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (selectedIndex < 0) {
                    return;
                }

                selectedCategory = categoryList.get(selectedIndex);
                updateCategory();

            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        builder.create().show();
    }

    private void updateCategory() {

        txtCategory.setText(selectedCategory.getCategoryName());
        initDataSet();
    }

}
