/**
 * 친구목록 fragment
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.adapter.ImageAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.Indicator;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;
import kr.co.bsmsoft.beple_shop.model.ImageModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.ImageTask;


/**
 * Created by brady on 15. 8. 24..
 */
public class ImageListFragment extends AbFragment implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener {

    private ImageAdapter adapter;
    private MainApp mainApp;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Indicator mIndicator;
    private TextView txtMessage;

    private final static int MSG_LOAD_IMAGE_LIST = 1;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_LOAD_IMAGE_LIST: {

                    ImageTask task = new ImageTask(mainApp.getToken());
                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            mSwipeRefreshLayout.setRefreshing(false);

                            try {
                                int code = ImageTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    ArrayList<ImageModel> shopList = ImageTask.parseImageList(ret);
                                    updateAdapter(shopList);

                                    if (shopList.size() == 0) {
                                        txtMessage.setVisibility(View.VISIBLE);
                                    }

                                }else{
                                    updateAdapter(new ArrayList<ImageModel>());
                                    Helper.alert(ImageTask.responseMessage(ret), getActivity());
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), getActivity());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {

                            mSwipeRefreshLayout.setRefreshing(false);

                            Helper.alert("서버에 접속할 수 없습니다. 네트워크 연결을 확인해 주세요.", getActivity());
                        }

                    };

                    task.getImageList(mainApp.getShopInfo().getId(), 0, 1);

                    break;
                }
            }
        }
    };

    public static ImageListFragment newInstance() {

        ImageListFragment fragment = new ImageListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_image_list, container, false);

        mIndicator = new Indicator(getActivity(), null);
        mainApp = globalVar.getInstance();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        txtMessage = (TextView) rootView.findViewById(R.id.txtMessage);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mHandler.obtainMessage(MSG_LOAD_IMAGE_LIST).sendToTarget();
            }
        });

        adapter = new ImageAdapter(getActivity(),
                new ArrayList<ImageModel>(), R.layout.cell_image);

        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    private void updateAdapter(ArrayList<ImageModel> list) {

        if (list != null) {
            adapter.clear();
            adapter.addAll(list);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler.obtainMessage(MSG_LOAD_IMAGE_LIST).sendToTarget();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //EventModel user =  adapter.getItem(position);
        //Intent i = new Intent(getActivity(), ProfileViewActivity.class);
        //i.putExtra(KEY_USER_ID, user.getFriendId());
        //startActivity(i);
    }

}
