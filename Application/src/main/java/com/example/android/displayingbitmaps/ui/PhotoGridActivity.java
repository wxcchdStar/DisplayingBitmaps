package com.example.android.displayingbitmaps.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.displayingbitmaps.R;
import com.example.android.displayingbitmaps.imageloader.ImageCache;
import com.example.android.displayingbitmaps.imageloader.ImageWorker;
import com.example.android.displayingbitmaps.imageloader.PhotoImageFetcher;
import com.example.android.displayingbitmaps.imageloader.RecyclingImageView;
import com.example.android.displayingbitmaps.imageloader.Utils;
import com.example.android.displayingbitmaps.imageloader.VideoImageFetcher;

/**
 * Created by Chenhd on 2015/4/21.
 */
public class PhotoGridActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] VEDIO_PROJECTION = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.DATA
    };

    private static final String[] PHOTO_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATA
    };

    private static final int INDEX_PIC_ID = 0;
    private static final int INDEX_BUCKET_ID = 1;
    private static final int INDEX_PATH = 2;

    private ImageWorker mImageFetcher;

    private int mImageThumbSize;
    private GridView mGridView;
    private ImageAdapter mAdapter;

    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_grid_fragment);

        mType = getIntent().getIntExtra("type", 0);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, ImageGridFragment.IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
        mImageFetcher = mType == 0 ? new PhotoImageFetcher(this, 2 * mImageThumbSize)
                : new VideoImageFetcher(this, 2 * mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);

        mAdapter = new ImageAdapter(this);
        mGridView = (GridView) findViewById(R.id.gridView);
        mGridView.setColumnWidth(mImageThumbSize);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        getSupportLoaderManager().initLoader(0, null, this);
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
        getSupportLoaderManager().destroyLoader(0);
        mImageFetcher.closeCache();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                mType == 0 ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Video.Media.EXTERNAL_CONTENT_URI ,
                mType == 0 ? PHOTO_PROJECTION : VEDIO_PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private class ImageAdapter extends CursorAdapter {
        public ImageAdapter(Context context) {
            super(context, null, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ImageView imageView = new RecyclingImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.getLayoutParams().height = mImageThumbSize;
            return imageView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String path = cursor.getString(INDEX_PATH);
            ImageView imageView = (ImageView) view;
            mImageFetcher.loadImage(path, imageView);
        }

    }
}
