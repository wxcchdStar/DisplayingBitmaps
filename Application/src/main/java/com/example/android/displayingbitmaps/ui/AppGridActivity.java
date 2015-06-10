package com.example.android.displayingbitmaps.ui;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.imageloader.AppIconFetcher;
import com.example.android.displayingbitmaps.imageloader.ImageCache;
import com.example.android.displayingbitmaps.imageloader.RecyclingImageView;
import com.example.android.displayingbitmaps.imageloader.Utils;

import java.util.List;

/**
 * Created by Chenhd on 2015/4/22.
 */
public class AppGridActivity extends FragmentActivity {

    private AppIconFetcher mImageFetcher;
    private AppsAdapter mAdapter;
    private List<ResolveInfo> mApps;

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        int dp24 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, ImageGridFragment.IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
        mImageFetcher = new AppIconFetcher(this, dp24);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);

        loadApps();
        ListView list = (ListView) findViewById(R.id.listview);
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
        mAdapter = new AppsAdapter();
        list.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    public class AppsAdapter extends BaseAdapter {

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i;
            if (convertView == null) {
                i = new RecyclingImageView(getApplicationContext());
            } else {
                i = (ImageView) convertView;
            }
            ResolveInfo info = mApps.get(position);
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mImageFetcher.loadImage(info.activityInfo, i);
            return i;
        }

        public final int getCount() {
            return mApps.size();
        }

        public final Object getItem(int position) {
            return mApps.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }

}
