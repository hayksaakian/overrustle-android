package gg.destiny.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.Arrays;

/**
 * Created by Hayk on 4/23/2015.
 */
public class WatchFaceWearableConfig extends Activity implements WearableListView.ClickListener {
    private static final String TAG = "WatchFaceWearableConfig";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_picker_layout);

        WearableListView menuPicker = (WearableListView) findViewById(R.id.item_picker);
        ((TextView) findViewById(R.id.default_header)).setText("OverRustle Settings");

        menuPicker.setClickListener(this);

        menuPicker.setAdapter(new MenuListAdapter(this));
    }

    @Override
    public void onTopEmptyRegionClick() {
        Log.d(TAG, "onTopEmptyRegionClick");

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        ItemViewHolder imageViewHolder = (ItemViewHolder) viewHolder;
        String tag = (String)imageViewHolder.mImageView.getTag();

        int idx = Arrays.asList(MenuListAdapter.menus).indexOf(tag);

        if(idx == MenuListAdapter.menus.length-1){
            finish();
        }else{
            Intent intent = new Intent(this, WatchFaceWearableConfigSubMenu.class);
            intent.putExtra("list_name", MenuListAdapter.background_categories[idx]);
            intent.putExtra("pretty_list_name", tag);
            startActivity(intent);
        }
    }

}
