package com.sukhyna_mykola.vkmusic;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class CustomPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<Sound> pages = new ArrayList<>();

    public CustomPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        pages = (ArrayList<Sound>) SoundLab.get(mContext).getSounds();
    }

    // Returns the number of pages to be displayed in the ViewPager.
    @Override
    public int getCount() {
        return pages.size();
    }

    // Returns true if a particular object (page) is from a particular page
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // This method should create the page for the given position passed to it as an argument.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // Inflate the layout for the page
        View v = mLayoutInflater.inflate(R.layout.player_fragment, container, false);
        // Find and populate data into the page (i.e set the image)
        Sound sound = SoundLab.get(mContext).getSounds().get(position);
        ( (TextView)v.findViewById(R.id.title_sound_player)).setText(sound.getTitle());
        ( (TextView)v.findViewById(R.id.artist_sound_player)).setText(sound.getArtist());
        container.addView(v);
        // Return the page
        return v;
    }

    // Removes the page from the container for the given position.
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}