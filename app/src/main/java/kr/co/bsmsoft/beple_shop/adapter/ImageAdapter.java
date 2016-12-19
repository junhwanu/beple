package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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


@SuppressWarnings("rawtypes")
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> implements NetDefine {

    private Context context;
    private ArrayList<ImageModel> items;
    private int item_layout;
    private DisplayImageOptions options;

    public Callbacks mCallbacks = sDummyCallbacks;

    public interface Callbacks {
        public	void onSelectedItem(ImageModel item);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onSelectedItem(ImageModel item) {

        }
    };

    public ImageAdapter(Context context, ArrayList<ImageModel> items, int item_layout) {
        this.context=context;
        this.items=items;
        this.item_layout=item_layout;

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new SimpleBitmapDisplayer())
                //.displayer(new FadeInBitmapDisplayer(300))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(R.drawable.bg_default_photo)
                .build();

        mCallbacks = sDummyCallbacks;
    }

    public boolean addAll(ArrayList<ImageModel> list) {

        boolean result =  items.addAll(list);
        this.notifyDataSetChanged();
        return result;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).inflate(item_layout, null);
        v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ImageModel item = items.get(position);

        String imageServerPath = String.format("%s/%s", SERVER_URL, item.getFileUrl());
        ImageLoader imageLoader = ImageLoader.getInstance();
        if(!imageLoader.isInited()) imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        imageLoader.displayImage(imageServerPath, holder.image, options);

        holder.title.setText(item.getFileDesc());
        holder.regDt.setText(Helper.formatTimeString(item.getRegDt()));

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imageServerPath = String.format("%s/%s", SERVER_URL, item.getFileUrl());
                Intent i = new Intent(context, PhotoViewActivity.class);
                i.putExtra(KEY_IMAGE_PATH, imageServerPath);
                context.startActivity(i);
            }
        });

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCallbacks.onSelectedItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView regDt;
        CardView cardview;

        public ViewHolder(View itemView) {
            super(itemView);
            image=(ImageView)itemView.findViewById(R.id.image);
            title=(TextView)itemView.findViewById(R.id.txtTitle);
            regDt=(TextView)itemView.findViewById(R.id.txtRegDt);
            cardview=(CardView)itemView.findViewById(R.id.cardview);
        }
    }
}
