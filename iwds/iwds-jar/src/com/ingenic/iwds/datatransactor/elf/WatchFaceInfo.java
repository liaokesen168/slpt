/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
 *
 *  Elf/IDWS Project
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation; either version 2 of the License, or (at your
 *  option) any later version.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package com.ingenic.iwds.datatransactor.elf;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.utils.IwdsAssert;

public class WatchFaceInfo implements Parcelable {
    public String name; // 表盘名字
    public Background background; // 整个表盘背景
    public DigitalStyleWatchFace digitalWatchFace; // 数字表盘设置，没有为null
    public AnalogStyleWatchFace analogWatchFace; // 模拟表盘设置，没有为null

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(background, flags);
        dest.writeParcelable(digitalWatchFace, flags);
        dest.writeParcelable(analogWatchFace, flags);
    }

    public static final Creator<WatchFaceInfo> CREATOR = new Creator<WatchFaceInfo>() {
        @Override
        public WatchFaceInfo createFromParcel(Parcel source) {
            WatchFaceInfo info = new WatchFaceInfo();

            info.name = source.readString();

            info.background = source.readParcelable(Background.class
                    .getClassLoader());

            info.digitalWatchFace = source
                    .readParcelable(DigitalStyleWatchFace.class
                            .getClassLoader());

            info.analogWatchFace = source
                    .readParcelable(AnalogStyleWatchFace.class.getClassLoader());

            return info;
        }

        @Override
        public WatchFaceInfo[] newArray(int size) {
            return new WatchFaceInfo[size];
        }
    };

    public static class DigitalStyleWatchFace implements Parcelable {
        public TextDisplayProfile hourDisplayProfile; // 小时显示
        public TextDisplayProfile minuteDisplayProfile; // 分钟显示
        public TextDisplayProfile separatorDisplayProfile; // 小时与分钟间的分隔符

        public TextDisplayProfile traditionalChineseCalenderDisplayProfile; // 中国农历
        public TextDisplayProfile dateDateDisplayProfile; // 日期显示

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(hourDisplayProfile, flags);
            dest.writeParcelable(minuteDisplayProfile, flags);
            dest.writeParcelable(separatorDisplayProfile, flags);

            dest.writeParcelable(traditionalChineseCalenderDisplayProfile,
                    flags);
            dest.writeParcelable(dateDateDisplayProfile, flags);
        }

        public static final Creator<DigitalStyleWatchFace> CREATOR = new Creator<DigitalStyleWatchFace>() {
            @Override
            public DigitalStyleWatchFace createFromParcel(Parcel source) {
                DigitalStyleWatchFace info = new DigitalStyleWatchFace();

                info.hourDisplayProfile = source
                        .readParcelable(TextDisplayProfile.class
                                .getClassLoader());

                info.minuteDisplayProfile = source
                        .readParcelable(TextDisplayProfile.class
                                .getClassLoader());

                info.separatorDisplayProfile = source
                        .readParcelable(TextDisplayProfile.class
                                .getClassLoader());

                info.traditionalChineseCalenderDisplayProfile = source
                        .readParcelable(TextDisplayProfile.class
                                .getClassLoader());

                info.dateDateDisplayProfile = source
                        .readParcelable(TextDisplayProfile.class
                                .getClassLoader());
                return info;
            }

            @Override
            public DigitalStyleWatchFace[] newArray(int size) {
                return new DigitalStyleWatchFace[size];
            }
        };

        @Override
        public String toString() {
            return "DigitalStyleWatchFace [hourDisplayProfile="
                    + hourDisplayProfile + ", minuteDisplayProfile="
                    + minuteDisplayProfile + ", separatorDisplayProfile="
                    + separatorDisplayProfile
                    + ", traditionalChineseCalenderDisplayProfile="
                    + traditionalChineseCalenderDisplayProfile
                    + ", dataDateDisplayProfile=" + dateDateDisplayProfile
                    + "]";
        }
    } // end DigitalStyleWatchFace

    public static class AnalogStyleWatchFace implements Parcelable {
        public int updateInterval = 1000; // 秒针刷新频率（单位ms）

        public Picture hourPointer; // 大表盘使用的时针的图片
        public Picture minutePointer; // 大表盘使用的分针的图片
        public Picture secondPointer; // 大表盘使用的秒针的图片

        public InnerWatchFace month; // 内嵌月份盘
        public InnerWatchFace week; // 内嵌星期盘
        public InnerWatchFace ampm; // 内嵌上下午盘

        public TypefaceProfile dateDisplayProfile; // 日期显示设置

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(updateInterval);

            dest.writeParcelable(hourPointer, flags);
            dest.writeParcelable(minutePointer, flags);
            dest.writeParcelable(secondPointer, flags);

            dest.writeParcelable(month, flags);
            dest.writeParcelable(week, flags);
            dest.writeParcelable(ampm, flags);

            dest.writeParcelable(dateDisplayProfile, flags);
        }

        public static final Creator<AnalogStyleWatchFace> CREATOR = new Creator<AnalogStyleWatchFace>() {
            @Override
            public AnalogStyleWatchFace createFromParcel(Parcel source) {
                AnalogStyleWatchFace info = new AnalogStyleWatchFace();

                info.updateInterval = source.readInt();

                info.hourPointer = source.readParcelable(Picture.class
                        .getClassLoader());

                info.minutePointer = source.readParcelable(Picture.class
                        .getClassLoader());

                info.secondPointer = source.readParcelable(Picture.class
                        .getClassLoader());

                info.month = source.readParcelable(InnerWatchFace.class
                        .getClassLoader());

                info.week = source.readParcelable(InnerWatchFace.class
                        .getClassLoader());

                info.ampm = source.readParcelable(InnerWatchFace.class
                        .getClassLoader());

                info.dateDisplayProfile = source
                        .readParcelable(TypefaceProfile.class.getClassLoader());

                return info;
            }

            @Override
            public AnalogStyleWatchFace[] newArray(int size) {
                return new AnalogStyleWatchFace[size];
            }
        };

        @Override
        public String toString() {
            return "AnalogStyleWatchFace [updateInterval=" + updateInterval
                    + ", hourPointer=" + hourPointer + ", minutePointer="
                    + minutePointer + ", secondPointer=" + secondPointer
                    + ", month=" + month + ", week=" + week + ", ampm=" + ampm
                    + ", dateDisplayProfile=" + dateDisplayProfile + "]";
        }
    } // end AnalogStyleWatchFace

    public static class InnerWatchFace implements Parcelable {
        public Background background; // 大背景
        public Picture pointer; // 指针

        public int posX; // 表盘位置
        public int posY; // 表盘位置

        public TypefaceProfile fontDisplayProfile; // 文字显示设置

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(background, flags);
            dest.writeParcelable(pointer, flags);

            dest.writeInt(posX);
            dest.writeInt(posY);

            dest.writeParcelable(fontDisplayProfile, flags);
        }

        public static final Creator<InnerWatchFace> CREATOR = new Creator<InnerWatchFace>() {
            @Override
            public InnerWatchFace createFromParcel(Parcel source) {
                InnerWatchFace info = new InnerWatchFace();

                info.background = source.readParcelable(Background.class
                        .getClassLoader());

                info.pointer = source.readParcelable(Picture.class
                        .getClassLoader());

                info.posX = source.readInt();
                info.posY = source.readInt();

                info.fontDisplayProfile = source
                        .readParcelable(TypefaceProfile.class.getClassLoader());

                return info;
            }

            @Override
            public InnerWatchFace[] newArray(int size) {
                return new InnerWatchFace[size];
            }
        };

        @Override
        public String toString() {
            return "InnerWatchFace [background=" + background + ", pointer="
                    + pointer + ", posX=" + posX + ", posY=" + posY
                    + ", textDisplayProfile=" + fontDisplayProfile + "]";
        }
    }

    public static class TypefaceProfile implements Parcelable {
        public String typefaceName; // 字体名称
        public int color; // 字体颜色
        public int unit; // 字体大小单位(TypedValue.COMPLEX_UNIT_SP,
                         // TypedValue.COMPLEX_UNIT_PT ...)
        public float size; // 字体大小

        public Background background; // 文字背景

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(typefaceName);
            dest.writeInt(color);
            dest.writeInt(unit);

            dest.writeFloat(size);

            dest.writeParcelable(background, flags);
        }

        public static final Creator<TypefaceProfile> CREATOR = new Creator<TypefaceProfile>() {
            @Override
            public TypefaceProfile createFromParcel(Parcel source) {
                TypefaceProfile info = new TypefaceProfile();

                info.typefaceName = source.readString();
                info.color = source.readInt();

                info.size = source.readFloat();

                info.background = source.readParcelable(Background.class
                        .getClassLoader());

                return info;
            }

            @Override
            public TypefaceProfile[] newArray(int size) {
                return new TypefaceProfile[size];
            }
        };

        @Override
        public String toString() {
            return "TypefaceProfile [typefaceName=" + typefaceName + ", color="
                    + color + ", unit=" + unit + ", size=" + size
                    + ", background=" + background + "]";
        }
    }

    public static class TextDisplayProfile implements Parcelable {
        public int posX; // x轴坐标
        public int posY; // y轴坐标
        public TypefaceProfile typefaceProfile; // 字体设置

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(posX);
            dest.writeInt(posY);

            dest.writeParcelable(typefaceProfile, flags);
        }

        public static final Creator<TextDisplayProfile> CREATOR = new Creator<TextDisplayProfile>() {
            @Override
            public TextDisplayProfile createFromParcel(Parcel source) {
                TextDisplayProfile info = new TextDisplayProfile();

                info.posX = source.readInt();
                info.posY = source.readInt();

                info.typefaceProfile = source
                        .readParcelable(TypefaceProfile.class.getClassLoader());

                return info;
            }

            @Override
            public TextDisplayProfile[] newArray(int size) {
                return new TextDisplayProfile[size];
            }
        };

        @Override
        public String toString() {
            return "TextDisplayProfile [posX=" + posX + ", posY=" + posY
                    + ", typefaceProfile=" + typefaceProfile + "]";
        }
    }

    public static class Picture implements Parcelable {
        private byte[] m_data; // 图片内容 （对端使用 BitmapFactory.decodeByteArray
                               // 之类的函数装载图片）

        private int m_width; // 图片宽度（单位同Bitmap.getWidth()）
        private int m_height; // 图片高度 (单位同bitmap.getHeight())

        public Picture(byte[] data, int width, int height) {
            IwdsAssert.dieIf(this, data == null || data.length == 0,
                    "Data is null or empty.");

            IwdsAssert.dieIf(this, width <= 0 || height <= 0,
                    "Width <= 0 or Heigth <= 0.");

            m_data = data;

            m_width = width;
            m_height = height;
        }

        public byte[] getData() {
            return m_data;
        }

        public int getWidth() {
            return m_width;
        }

        public int getHeight() {
            return m_height;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(m_data.length);
            dest.writeByteArray(m_data);

            dest.writeInt(m_width);
            dest.writeInt(m_height);
        }

        public static final Creator<Picture> CREATOR = new Creator<Picture>() {
            @Override
            public Picture createFromParcel(Parcel source) {
                int length = source.readInt();
                byte[] data = new byte[length];
                source.readByteArray(data);

                int w = source.readInt();
                int h = source.readInt();

                Picture info = new Picture(data, w, h);

                return info;
            }

            @Override
            public Picture[] newArray(int size) {
                return new Picture[size];
            }
        };

        @Override
        public String toString() {
            return "Picture [m_data=" + Arrays.toString(m_data) + ", m_width="
                    + m_width + ", m_height=" + m_height + "]";
        }
    }

    public static class Background implements Parcelable {
        public int color = -1; // 如果不是纯色背景，此处为-1
        public Picture picture; // 如果为纯色背景，此处为null

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(color);
            dest.writeParcelable(picture, flags);
        }

        public static final Creator<Background> CREATOR = new Creator<Background>() {
            @Override
            public Background createFromParcel(Parcel source) {
                Background info = new Background();

                info.color = source.readInt();
                info.picture = source.readParcelable(Picture.class
                        .getClassLoader());

                return info;
            }

            @Override
            public Background[] newArray(int size) {
                return new Background[size];
            }
        };

        @Override
        public String toString() {
            return "Background [color=" + color + ", picture=" + picture + "]";
        }
    }
} // end WatchFaceInfo
