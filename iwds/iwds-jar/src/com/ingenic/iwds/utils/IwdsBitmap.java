/*
 * Copyright (C) 2015 Ingenic Semiconductor
 * 
 * LiJingWen(Kevin) <kevin.jwli@ingenic.com>
 * 
 * Elf/IDWS Project
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.ingenic.iwds.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.ingenic.iwds.os.SafeParcel;
import com.ingenic.iwds.os.SafeParcelable;

/**
 * Bitmap类型数据针对于SafeParcel的封装，抽取Bitmap的关键数据：width，height，config，density，colors，isMutable等。
 */
public final class IwdsBitmap implements Parcelable, SafeParcelable {

    private boolean mIsMutable;
    private boolean mHasAlpha;
    private boolean mHasMipMap;

    private Bitmap.Config mConfig;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int[] mPixels;

    private IwdsBitmap(SafeParcel in) {
        readFromParcel(in);
    }

    private IwdsBitmap(Parcel in) {
        readFromParcel(in);
    }

    private IwdsBitmap(Bitmap bitmap) {
        copyFromBitmapInner(bitmap);
    }

    private void copyFromBitmapInner(Bitmap bitmap) {
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mPixels = new int[mWidth * mHeight];

        mDensity = bitmap.getDensity();
        mConfig = bitmap.getConfig();
        mIsMutable = bitmap.isMutable();
        mHasAlpha = bitmap.hasAlpha();
        mHasMipMap = bitmap.hasMipMap();
        bitmap.getPixels(mPixels, 0, mWidth, 0, 0, mWidth, mHeight);
    }

    /**
     * 返回IwdsBitmap基于的Bitmap是否可修改
     * 
     * @return 可修改返回true，否则返回false
     */
    public boolean isMutable() {
        return mIsMutable;
    }

    /**
     * 返回IwdsBitmap基于的Bitmap是否具有透明度
     * 
     * @return 具有透明度返回true，否则返回false
     */
    public boolean hasAlpha() {
        return mHasAlpha;
    }

    /**
     * 返回IwdsBitmap基于的Bitmap是否具有Mipmap
     * 
     * @return 具有Mipmap返回true，否则返回false
     */
    public boolean hasMipMap() {
        return mHasMipMap;
    }

    /**
     * 取得IwdsBitmap基于的Bitmap的密度
     * 
     * @return 密度
     */
    public int getDensity() {
        return mDensity;
    }

    /**
     * 取得IwdsBitmap基于的Bitmap的宽度
     * 
     * @return 宽度
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * 取得IwdsBitmap基于的Bitmap的高度
     * 
     * @return 高度
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * 取得IwdsBitmap基于的Bitmap的色彩Config
     * 
     * @return 色彩Config
     */
    public Bitmap.Config getConfig() {
        return mConfig;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(SafeParcel dest, int flags) {
        dest.writeInt(mIsMutable ? 1 : 0);
        dest.writeInt(mHasAlpha ? 1 : 0);
        dest.writeInt(mHasMipMap ? 1 : 0);
        dest.writeString(mConfig.name());

        dest.writeInt(mDensity);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
        dest.writeIntArray(mPixels);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mIsMutable ? 1 : 0);
        dest.writeInt(mHasAlpha ? 1 : 0);
        dest.writeInt(mHasMipMap ? 1 : 0);
        dest.writeString(mConfig.name());

        dest.writeInt(mDensity);
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
        dest.writeIntArray(mPixels);
    }

    private void readFromParcel(SafeParcel in) {
        mIsMutable = in.readInt() != 0;
        mHasAlpha = in.readInt() != 0;
        mHasMipMap = in.readInt() != 0;
        mConfig = Bitmap.Config.valueOf(in.readString());

        mDensity = in.readInt();
        mWidth = in.readInt();
        mHeight = in.readInt();
        mPixels = in.createIntArray();
    }

    private void readFromParcel(Parcel in) {
        mIsMutable = in.readInt() != 0;
        mHasAlpha = in.readInt() != 0;
        mHasMipMap = in.readInt() != 0;
        mConfig = Bitmap.Config.valueOf(in.readString());

        mDensity = in.readInt();
        mWidth = in.readInt();
        mHeight = in.readInt();
        mPixels = in.createIntArray();
    }

    /**
     * 把{@link IwdsBitmap}数组转换为{@link Bitmap}数组
     * 
     * @param array {@link IwdsBitmap}数组
     * @return 转换得到的{@link Bitmap}数组
     */
    public static Bitmap[] arrayToLocal(IwdsBitmap[] array) {
        final int N = array.length;
        final Bitmap[] result = new Bitmap[N];

        for (int i = 0; i < N; i++) {
            result[i] = array[i].toBitmap();
        }
        return result;
    }

    /**
     * 把{@link IwdsBitmap}数组转换为{@link Bitmap}数组
     * 
     * @param context 目标上下文
     * @param array {@link IwdsBitmap}数组
     * @return 转换得到的{@link Bitmap}数组
     */
    public static Bitmap[] arrayToLocal(Context context, IwdsBitmap[] array) {
        final int N = array.length;
        final Bitmap[] result = new Bitmap[N];

        for (int i = 0; i < N; i++) {
            result[i] = array[i].toBitmap(context);
        }
        return result;
    }

    /**
     * 把{@link IwdsBitmap}数组转换为{@link Bitmap}数组
     * 
     * @param display 目标屏幕参数
     * @param array {@link IwdsBitmap}数组
     * @return 转换得到的{@link Bitmap}数组
     */
    public static Bitmap[] arrayToLocal(DisplayMetrics display, IwdsBitmap[] array) {
        final int N = array.length;
        final Bitmap[] result = new Bitmap[N];

        for (int i = 0; i < N; i++) {
            result[i] = array[i].toBitmap(display);
        }
        return result;
    }

    /**
     * 把{@link Bitmap}数组转换为{@link IwdsBitmap}数组
     * 
     * @param array {@link Bitmap}数组
     * @return 转换得到的{@link IwdsBitmap}数组
     */
    public static IwdsBitmap[] arrayToRemote(Bitmap[] array) {
        final int N = array.length;
        final IwdsBitmap[] result = new IwdsBitmap[N];

        for (int i = 0; i < N; i++) {
            result[i] = fromBitmap(array[i]);
        }
        return result;
    }

    /**
     * 把{@link Bitmap}转换为{@link IwdsBitmap}
     * 
     * @param bitmap 数据源{@link Bitmap}
     * @return 转换结果{@link IwdsBitmap}
     */
    public static IwdsBitmap fromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        if (bitmap.isRecycled()) {
            throw new IllegalStateException("Can't create from a recycled bitmap.");
        }

        return new IwdsBitmap(bitmap);
    }

    private Bitmap copyIfNeed(Bitmap b) {
        if (!mIsMutable == b.isMutable()) {
            configBitmap(b);
            return b;
        }

        Bitmap result = b.copy(mConfig, mIsMutable);
        b.recycle();
        configBitmap(result);
        return result;
    }

    private void configBitmap(Bitmap b) {
        b.setDensity(mDensity);

        if (mHasAlpha != b.hasAlpha()) {
            b.setHasAlpha(mHasAlpha);
        }

        if (mHasMipMap != b.hasMipMap()) {
            b.setHasMipMap(mHasMipMap);
        }
    }

    /**
     * 转换为{@link Bitmap}
     * 
     * @return 转换结果{@link Bitmap}
     */
    public Bitmap toBitmap() {
        Bitmap b = Bitmap.createBitmap(mPixels, mWidth, mHeight, mConfig);
        return copyIfNeed(b);
    }

    /**
     * 转换为{@link Bitmap}
     * 
     * @param context 目标上下文
     * @return 转换结果{@link Bitmap}
     */
    public Bitmap toBitmap(Context context) {
        return toBitmap(context.getResources().getDisplayMetrics());
    }

    /**
     * 转换为{@link Bitmap}
     * 
     * @param display 目标屏幕参数
     * @return 转换结果{@link Bitmap}
     */
    public Bitmap toBitmap(DisplayMetrics display) {
        Bitmap b = Bitmap.createBitmap(display, mPixels, mWidth, mHeight, mConfig);
        return copyIfNeed(b);
    }

    public static final Creator CREATOR = new Creator();

    public static final class Creator implements Parcelable.Creator<IwdsBitmap>,
            SafeParcelable.Creator<IwdsBitmap> {

        @Override
        public IwdsBitmap createFromParcel(SafeParcel source) {
            return new IwdsBitmap(source);
        }

        @Override
        public IwdsBitmap createFromParcel(Parcel source) {
            return new IwdsBitmap(source);
        }

        @Override
        public IwdsBitmap[] newArray(int size) {
            return new IwdsBitmap[size];
        }
    }
}