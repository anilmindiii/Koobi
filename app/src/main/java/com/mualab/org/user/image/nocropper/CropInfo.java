package com.mualab.org.user.image.nocropper;

import android.graphics.Bitmap;
import android.graphics.Rect;

class CropInfo {
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final boolean addPadding;
    public final int verticalPadding;
    public final int horizontalPadding;
    public final int paddingColor;

    public CropInfo(int x, int y, int width, int height) {
        this(x, y, width, height, false, 0, 0, -1);
    }

    public CropInfo(int x, int y, int width, int height, boolean addPadding, int horizontalPadding, int verticalPadding, int paddingColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.addPadding = addPadding;
        this.horizontalPadding = horizontalPadding;
        this.verticalPadding = verticalPadding;
        this.paddingColor = paddingColor;
    }

    public static CropInfo cropCompleteBitmap(Bitmap bitmap, boolean paddingToMakeSquare, int horizontalPadding, int verticalPadding, int paddingColor) {
        return new CropInfo(0, 0, bitmap.getWidth(), bitmap.getHeight(), paddingToMakeSquare, horizontalPadding, verticalPadding, paddingColor);
    }

    public static CropInfo cropFromRect(Rect rect, boolean paddingToMakeSquare, int horizontalPadding, int verticalPadding, int paddingColor) {
        return new CropInfo(rect.left, rect.top, rect.width(), rect.height(), paddingToMakeSquare, horizontalPadding, verticalPadding, paddingColor);
    }


    public CropInfo scaleInfo(float scale) {
        return new CropInfo(
                (int)(x * scale),
                (int)(y * scale),
                (int)(width * scale),
                (int)(height * scale),
                addPadding,
                (int)(horizontalPadding * scale),
                (int)(verticalPadding * scale),
                paddingColor);
    }

    /**
     * Get CropInfo if you want to crop un-rotated/original bitmap.
     * Note: This can be used only if the bitmap is rotated by 90 degrees clockwise. For 180, 270, use
     * {@link #rotate90XTimes(int, int, int)}.
     *
     * @param bitmapWidth width of the rotated bitmap, ie. height of the original bitmap
     * @return projected {@link CropInfo} which can be used to crop the original image with same bounds.
     */
    public CropInfo rotate90(int bitmapWidth) {

        // Bitmap is already rotated by 90 degrees clockwise. So the crop info corresponds to that.
        // We need to project this crop info for the bitmap which was not rotated by 90 degrees.
        // Project crop info for 90 degrees anticlockwise

        //noinspection SuspiciousNameCombination
        return new CropInfo(
                y,
                bitmapWidth - (x + width),
                height,
                width,
                addPadding,
                verticalPadding,
                horizontalPadding,
                paddingColor
        );
    }

    /**
     *
     * Get CropInfo if you want to crop un-rotated/original bitmap.
     *
     * Use case: User rotates bitmap to 90 degrees and crops. You want to apply this crop to the original (un-rotated)
     * bitmap, you can use this method with times = 1, bitmapWidth = width of the rotated bitmap (which is height of the original bitmap),
     * bitmapHeight = height of the rotated bitmap (which is width of the original bitmap).
     *
     * Use times = 2 for 180 degrees and times = 3 for 270 degrees.
     *
     * @param bitmapWidth - width of rotated bitmap.
     * @param bitmapHeight - height of rotated bitmap.
     * @param times - number of times the bitmap has been rotated by 90 degrees clockwise.
     * @return projected {@link CropInfo} which can be used to crop the original image with same bounds.
     */
    public CropInfo rotate90XTimes(int bitmapWidth, int bitmapHeight, int times) {
        int rotate = times%4;
        if (rotate == 0) {
            return this;
        }

        CropInfo info = this;
        for (int i=0; i<rotate; i++) {
            int width = i%2 == 1 ? bitmapHeight : bitmapWidth;
            info = info.rotate90(width);
        }
        return info;
    }

    @Override
    public String toString() {
        return "CropInfo{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", addPadding=" + addPadding +
                ", verticalPadding=" + verticalPadding +
                ", horizontalPadding=" + horizontalPadding +
                '}';
    }
}
