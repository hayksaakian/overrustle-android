package gg.destiny.app;

import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Hayk on 5/5/2015.
 */
public class ItemViewHolder extends WearableListView.ViewHolder {
    final ImageView mImageView;
    final TextView mTextView;

    public ItemViewHolder(View iView) {
        super(iView);
        mImageView = (ImageView)iView.findViewById(R.id.circle);
        mTextView = (TextView)iView.findViewById(R.id.background_name);
    }
}
