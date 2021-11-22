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


public class EpilepsyInfoAdapterActivity extends PagerAdapter {
    private int[] epi_images = {R.drawable.aed_happy, R.drawable.info1, R.drawable.info2, R.drawable.info3, R.drawable.info4, R.drawable.info5, R.drawable.info6, R.drawable.info7, R.drawable.info8, R.drawable.info9, R.drawable.info10};
    private LayoutInflater inflater;
    private Context epi_ctx;

    public EpilepsyInfoAdapterActivity(Context epi_ctx) {
        this.epi_ctx = epi_ctx;
    }

    @Override
    public int getCount() {
        return epi_images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==(LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) epi_ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vepi = inflater.inflate(R.layout.content_epilepsy_info, container, false);
        ImageView epimg = (ImageView)vepi.findViewById(R.id.imageViewInfo);
        TextView tvepi = (TextView)vepi.findViewById(R.id.textViewInfo);
        epimg.setImageResource(epi_images[position]);
        container.addView(vepi);
        return vepi;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.invalidate();

    }
}







