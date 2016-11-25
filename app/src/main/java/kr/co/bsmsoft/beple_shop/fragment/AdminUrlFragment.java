/**
 * 친구목록 fragment
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import kr.co.bsmsoft.beple_shop.MainApp;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.globalVar;


/**
 * Created by brady on 15. 8. 24..
 */
public class AdminUrlFragment extends AbFragment implements NetDefine, View.OnClickListener {

    private MainApp mainApp;

    public static AdminUrlFragment newInstance() {

        AdminUrlFragment fragment = new AdminUrlFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_admin_url, container, false);

        mainApp = globalVar.getInstance();

        Button btnShopAdmin = (Button) rootView.findViewById(R.id.btnShopAdmin);
        Button btnAdmin = (Button) rootView.findViewById(R.id.btnAdmin);

        btnShopAdmin.setOnClickListener(this);
        btnAdmin.setOnClickListener(this);
        return rootView;
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnShopAdmin) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.beple.or.kr"));
            startActivity(browserIntent);
        }else if (view.getId() == R.id.btnAdmin) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://beple.rbiz.kr/App"));
            startActivity(browserIntent);
        }
    }
}
