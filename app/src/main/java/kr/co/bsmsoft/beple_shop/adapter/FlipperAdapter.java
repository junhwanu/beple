package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import kr.co.bsmsoft.beple_shop.R;

/**
 * Created by junhw on 2017-05-25.
 */

public class FlipperAdapter extends BaseAdapter {
    private final Context mContext;
    LayoutInflater inflater;
    List<Integer> imageId;

    public FlipperAdapter(Context context, List<Integer> ids) {
        mContext = context;
        imageId = ids;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imageId.size();
    }

    @Override
    public Object getItem(int position) {
        return imageId.get(position);
    }

    @Override
    public long getItemId(int position) {
        return imageId.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.cell_flipper, parent, false);
        }
        ImageView iv = (ImageView)convertView.findViewById(R.id.iv_flipper);
        iv.setImageResource(imageId.get(position));

        return convertView;
    }
}
