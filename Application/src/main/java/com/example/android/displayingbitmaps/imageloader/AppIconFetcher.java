/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.displayingbitmaps.imageloader;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 磁盘缓存默认是JPEG有损压缩之后存入文件的，而ICON是不能被JPEG有损压缩的，所以需要设置PNG无损压缩。
 * Created by Chenhd on 2015/4/22.
 */
public class AppIconFetcher extends ImageResizer {
    private PackageManager mPm;

    public AppIconFetcher(Context context, int imageSize) {
        super(context, imageSize);
        mPm = context.getPackageManager();
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        Drawable iconDrawable = null;
        if (ActivityInfo.class.isInstance(data)) {
            ActivityInfo info = (ActivityInfo) data;
            iconDrawable = info.loadIcon(mPm);
        } else if (String.class.isInstance(data)) {
            try {
                iconDrawable = mPm.getApplicationIcon((String) data);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        Bitmap result = null;
        if (iconDrawable != null) {
            int width = iconDrawable.getIntrinsicWidth();
            int height = iconDrawable.getIntrinsicHeight();
            if (iconDrawable instanceof BitmapDrawable) {
                result = ((BitmapDrawable) iconDrawable).getBitmap();
            } else {
                result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                Canvas canvas = new Canvas(result);
                iconDrawable.setBounds(0, 0, width, width);
                iconDrawable.draw(canvas);
            }
        }
        return result;
    }

}
