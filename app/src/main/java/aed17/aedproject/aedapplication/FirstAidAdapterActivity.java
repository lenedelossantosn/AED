package aed17.aedproject.aedapplication;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sharlene on 4/13/2017.
 */

public class FirstAidAdapterActivity extends PagerAdapter {
    private int[] swipe_images = {R.drawable.aid1, R.drawable.aid2, R.drawable.aid3, R.drawable.aid4};
    private LayoutInflater inflater;
    private Context swipe_ctx;

    public FirstAidAdapterActivity(Context swipe_ctx) {
        this.swipe_ctx = swipe_ctx;
    }

    @Override
    public int getCount() {
        return swipe_images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) swipe_ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vswipe = inflater.inflate(R.layout.firstaid_activity_content, container, false);
        ImageView simg = (ImageView)vswipe.findViewById(R.id.imageView);
        TextView tv = (TextView)vswipe.findViewById(R.id.textView);
        simg.setImageResource(swipe_images[position]);
        container.addView(vswipe);
        return vswipe;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();

    }
}







