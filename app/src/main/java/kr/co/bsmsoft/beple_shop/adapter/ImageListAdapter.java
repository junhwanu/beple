/**
 * 사용자 목록 리스트뷰 adapter
 */
package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.felipecsl.gifimageview.library.GifImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.PhotoViewActivity;
import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ImageModel;
import util.gifimageview.GifDataDownloader;

public class ImageListAdapter extends ArrayAdapter<ImageModel> implements NetDefine {

    private final static String TAG = "ImageListAdapter";

    private Context context;
    private ArrayList<ImageModel> items = new ArrayList<ImageModel>();
    private LayoutInflater vi;
    private DisplayImageOptions options;
    private ListView mListView;

    public ImageListAdapter(Context context, ArrayList<ImageModel> data) {
        super(context, R.layout.cell_image_small);
        this.context = context;
        this.items = data;
        this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new SimpleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.drawable.bg_default_photo)
                .build();

        mCallbacks = sDummyCallbacks;

    }

    class ViewHolder {
        GifImageView image;
        TextView title;
        TextView regDt;
        Button btnSelect;
    }

    public void addAll(ArrayList<ImageModel> images) {
        items.addAll(images);
        notifyDataSetChanged();
    }

    public ArrayList<ImageModel> getItems() {

        return items;
    }

    public void removeItem(int position) {
        items.remove(position);
    }
    public ImageModel getItem(int position) {

        return items.get(position);
    }

    public int getCount() {

        return items.size();
    }

    public void clear() {
        items.clear();
    }

    private View createView(int position) {

        ViewHolder holder = null;
        View v = null;
        holder = new ViewHolder();

        v = vi.inflate(R.layout.cell_image_small, null);
        holder.image = (GifImageView) v.findViewById(R.id.image);
        holder.title = (TextView) v.findViewById(R.id.txtTitle);
        holder.regDt = (TextView) v.findViewById(R.id.txtRegDt);
        holder.btnSelect = (Button) v.findViewById(R.id.btnSelect);
        holder.btnSelect.setOnClickListener(mOnImageSelectListener);

        v.setTag(holder);
        return v;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        View v = convertView;
        final GifImageView imageView;
        final ImageModel item = items.get(position);

        if (v == null) {

            v = createView(position);
            holder = (ViewHolder) v.getTag();

        } else{

            holder = (ViewHolder) v.getTag();
        }

        String imageServerPath = String.format("%s/%s", item.getServerAddress(), item.getFileUrl());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        imageLoader.displayImage(imageServerPath, holder.image, options);

        holder.title.setText(item.getFileDesc());
        holder.regDt.setText(Helper.formatTimeString(item.getRegDt()));

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imageServerPath = String.format("%s/%s", item.getServerAddress(), item.getFileUrl());
                Intent i = new Intent(context, PhotoViewActivity.class);
                i.putExtra(KEY_IMAGE_PATH, imageServerPath);
                context.startActivity(i);
            }
        });

        imageView = holder.image;
        new GifDataDownloader() {
            @Override
            protected void onPostExecute(final byte[] bytes) {
                imageView.setBytes(bytes);
                imageView.startAnimation();
            }
        }.execute(item.getServerAddress() + "/" + item.getFileUrl());
        /*
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCallbacks.onSelectedItem(item);
            }
        });
       */

        return v;

    }

    public ListView getListView() {
        return mListView;
    }

    public void setListView(ListView mListView) {
        this.mListView = mListView;
    }

    private View.OnClickListener mOnImageSelectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int position = mListView.getPositionForView((View) v.getParent());
            mCallbacks.onImageSelect(position);

        }
    };

    public Callbacks mCallbacks = sDummyCallbacks;

    public interface Callbacks {
        public void onImageSelect(int position);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onImageSelect(int position) {

        }
    };

}
