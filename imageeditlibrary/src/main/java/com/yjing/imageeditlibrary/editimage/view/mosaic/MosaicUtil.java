/*
 *          Copyright (C) 2016 jarlen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.yjing.imageeditlibrary.editimage.view.mosaic;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class MosaicUtil {

    private static final int MAX_SIZE = 1 * 1024 * 1024; //每张图片最大允许加载1M的
    private  int zoomNum = 4;
    public static enum Effect {
        MOSAIC, BLUR, FLOWER,
    }

    private MosaicUtil(){}

    public static MosaicUtil newInstance(){
        return MosaicUtilModule.instance ;
    }

    static class MosaicUtilModule{
        private static MosaicUtil instance = new MosaicUtil();
    }

    public  void  checkZoomNum(int width,int height){
        int totalMemory = width * height * 4;
        if (totalMemory > MAX_SIZE) {
            zoomNum = (int) Math.ceil(totalMemory / MAX_SIZE);
        }
    }

    public int getZoomNum(){
        return zoomNum;
    }
    /**
     * 马赛克效果(Native)
     *
     * @param bitmap 原图
     * @return 马赛克图片
     */
    public  Bitmap getMosaic(Bitmap bitmap) {
        int width = bitmap.getWidth() / zoomNum;
        int height = bitmap.getHeight() / zoomNum;
        int radius = 10;

        Bitmap mosaicBitmap = Bitmap.createBitmap(width, height,
                Config.ARGB_4444);
        Canvas canvas = new Canvas(mosaicBitmap);

        int horCount = (int) Math.ceil(width / (float) radius);
        int verCount = (int) Math.ceil(height / (float) radius);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = radius * horIndex;
                int t = radius * verIndex;
                int r = l + radius;
                if (r > width) {
                    r = width;
                }
                int b = t + radius;
                if (b > height) {
                    b = height;
                }
                int color = bitmap.getPixel(l * zoomNum, t * zoomNum);
                Rect rect = new Rect(l, t, r, b);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
        canvas.save();

        return mosaicBitmap;
    }


    /**
     * 模糊效果
     *
     * @param bitmap 原图像
     * @return 模糊图像
     */
    public  Bitmap getBlur(Bitmap bitmap) {
        int iterations = 1;
        int radius = 8;
        int width = bitmap.getWidth() / zoomNum;
        int height = bitmap.getHeight() / zoomNum;
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap blured = Bitmap.createBitmap(width, height,
                Config.ARGB_4444);
        getPixels(bitmap, inPixels);
//		bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++) {
            blur(inPixels, outPixels, width, height, radius);
            blur(outPixels, inPixels, height, width, radius);
        }
        blured.setPixels(inPixels, 0, width, 0, 0, width, height);
        return blured;
    }

    private  void getPixels(Bitmap bitmap, int[] inPixels) {
        int height = bitmap.getHeight() / zoomNum;
        int width = bitmap.getWidth() / zoomNum;
        int count = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                inPixels[count++] = bitmap.getPixel(j * zoomNum, i * zoomNum);
            }
        }
    }

    private  void blur(int[] in, int[] out, int width, int height,
                             int radius) {
        int widthMinus = width - 1;
        int tableSize = 2 * radius + 1;
        int divide[] = new int[256 * tableSize];

        for (int index = 0; index < 256 * tableSize; index++) {
            divide[index] = index / tableSize;
        }

        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -radius; i <= radius; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
                        | (divide[tg] << 8) | divide[tb];

                int i1 = x + radius + 1;
                if (i1 > widthMinus)
                    i1 = widthMinus;
                int i2 = x - radius;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    private  int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }
}
