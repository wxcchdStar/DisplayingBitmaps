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
import android.graphics.Bitmap;

/**
 * Created by Chenhd on 2015/4/21.
 */
public class PhotoImageFetcher extends ImageResizer {

    public PhotoImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
    }

    public PhotoImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
    }

    @Override
    protected Bitmap processBitmap(Object imgPath) {
        return decodeSampledBitmapFromFile(String.valueOf(imgPath), mImageWidth, mImageHeight, getImageCache());
    }

    // TODO: 裁剪一个正方形的图片
    public static Bitmap cropBitmap(Bitmap bitmap, int imageSide) {
        // Scale down the bitmap if it's too large.
        int cx = 0, cy = 0, cSize;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        if (bitmapWidth > bitmapHeight) {
            cx = (bitmapWidth - bitmapHeight) / 2;
            cSize = bitmapHeight;
        } else {
            cy = (bitmapHeight - bitmapWidth) / 2;
            cSize = bitmapWidth;
        }
        bitmap = Bitmap.createBitmap(bitmap, cx, cy, cSize, cSize);
        return Bitmap.createScaledBitmap(bitmap, imageSide, imageSide, true);
    }
}
