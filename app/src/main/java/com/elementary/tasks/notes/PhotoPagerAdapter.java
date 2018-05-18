package com.elementary.tasks.notes;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elementary.tasks.databinding.FragmentImageBinding;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

class PhotoPagerAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private List<NoteImage> mPhotosUrl;


    PhotoPagerAdapter(Context context, List<NoteImage> images) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPhotosUrl = images;
    }

    @Override
    public int getCount() {
        return mPhotosUrl.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FragmentImageBinding binding = FragmentImageBinding.inflate(mLayoutInflater, container, false);
        ImageView imageView = binding.ivPhoto;
        loadPhoto(imageView, position);
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(imageView);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        container.addView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((PhotoView) object);
    }

    private void loadPhoto(ImageView imageView, int position) {
        NoteImage image = mPhotosUrl.get(position);
        Glide.with(imageView.getContext().getApplicationContext())
                .load(image.getImage())
                .into(imageView);
    }
}