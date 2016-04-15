/*
 *  Copyright (C) 2015 Ingenic Semiconductor
 *
 *  Wu Jiao <jiao.wu@ingenic.com, wujiaososo@qq.com>
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
 */

package com.ingenic.iwds.slpt.view.core;

import java.util.ArrayList;

import com.ingenic.iwds.slpt.PictureData;
import com.ingenic.iwds.slpt.GroupData;
import com.ingenic.iwds.slpt.SlptClock;
import com.ingenic.iwds.slpt.view.utils.BmpCreator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

public abstract class Picture {
	int width;
	int height;
	Bitmap bitmap = null;
	Picture picture = null;
	PictureGroup group = null;
	int pictureIndex;
	int backgroundColor = SlptViewComponent.INVALID_COLOR;

	public abstract void decodeBitmap();

	public abstract void recycle();

	/**
	 * 设置背景颜色，StringPicture 使用这个属性来做抗锯齿效果<br>
	 * 至于ImagePicture 目前用不到<br>
	 */
	public void setBackgroundColor(int backgroundColor) {
		// do nothing
	};

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof Picture))
			return false;
		return ((Picture) o).bitmap == bitmap;
	}

	public String getName() {
		if (picture != null)
			return picture.getName();
		return "" + group.getName() + "/" + pictureIndex;
	}

	public static class PictureContainer {
		ArrayList<PictureGroup> list = new ArrayList<Picture.PictureGroup>();
		PictureGroup miscGroup = new PictureGroup(0);

		public PictureContainer() {
			miscGroup.name = "misc";
		}

		public boolean findGroup(PictureGroup group) {
			if (group == null)
				return false;

			for (int i = 0; i < list.size(); i++) {
				if (group == list.get(i))
					return true;
			}

			return false;
		}

		public boolean findPicture(Picture picture) {
			if (picture == null)
				return false;

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).findPicture(picture))
					return true;
			}

			return false;
		}

		public boolean add(PictureGroup group) {
			if (group == null)
				return false;
			if (findGroup(group))
				return true;

			for (int i = 0; i < list.size(); i++) {
				PictureGroup g = list.get(i);
				if (group.isSubsetOf(g)) {
					group.group = g;
					return true;
				}
			}

			for (int i = 0; i < list.size(); i++) {
				PictureGroup g = list.get(i);
				if (g.isSubsetOf(group)) {
					g.group = group;
					list.set(i, group);
					group.group = null;
					return true;
				}
			}

			list.add(group);
			group.group = null;
			return true;
		}

		public boolean add(Picture picture) {
			if (picture == null)
				return false;

			if (miscGroup.findPicture(picture))
				return true;

			if (findPicture(picture))
				return true;

			Picture p = miscGroup.findSamePicture(picture);
			if (p != null) {
				picture.picture = p;
				picture.group = miscGroup;
				return true;
			}

			for (int i = 0; i < list.size(); i++) {
				p = list.get(i).findSamePicture(picture);
				if (p != null) {
					picture.picture = p;
					picture.group = list.get(i);
					return true;
				}
			}

			miscGroup.add(picture);
			picture.picture = null;
			picture.group = miscGroup;

			return true;
		}

		private static int[] bitmapBuffer = new int[1];

		private static int[] requestBuffer(Bitmap bitmap) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int len = width * height * 4;

			if (len > bitmapBuffer.length) {
				bitmapBuffer = new int[len];
			}

			return bitmapBuffer;
		}

		public static native void clearPictureGroup();

		public static native void addPictureGroup(String groupName);

		public static native void addPicture(String pictureName, int width,
				int height, int[] mem, int backgroundColor);

		private static void writeGroupToSlpt(PictureGroup group) {
			addPictureGroup(group.getName());
			for (int j = 0; j < group.list.size(); j++) {
				Picture picture = group.list.get(j);
				picture.pictureIndex = j;
				picture.decodeBitmap();

				if (picture.bitmap == null) {
					addPicture("" + picture.pictureIndex, 0, 0, null,
							SlptViewComponent.INVALID_COLOR);
					continue;
				}

				requestBuffer(picture.bitmap);

				int width = picture.bitmap.getWidth();
				int height = picture.bitmap.getHeight();
				picture.bitmap.getPixels(bitmapBuffer, 0, width, 0, 0, width,
						height);
				addPicture("" + picture.pictureIndex, width, height,
						bitmapBuffer, picture.backgroundColor);
				picture.recycle();
			}
		}

		public static void writeToSlpt(PictureContainer container) {
			ArrayList<PictureGroup> list = container.list;

			clearPictureGroup();

			writeGroupToSlpt(container.miscGroup);

			for (int i = 0; i < list.size(); i++) {
				PictureGroup group = list.get(i);
				group.groupIndex = i;
				writeGroupToSlpt(group);
			}
		}

		/**
		 * 把图片按组分别写到slptclock对象的链表里
		 */
		private static void writePictureGroupToNativeList(SlptClock slptclock,
				PictureGroup group) {
			GroupData groupData = new GroupData();
			groupData.groupName = group.getName();
			groupData.pictureNum = group.list.size();

			for (int j = 0; j < group.list.size(); j++) {
				PictureData pictureData = new PictureData();
				Picture picture = group.list.get(j);
				picture.pictureIndex = j;
				picture.decodeBitmap();

				if (picture.bitmap == null) {
					pictureData.pictureIndex = picture.pictureIndex;
					pictureData.width = 0;
					pictureData.height = 0;
					pictureData.bitmapBuffer = null;
					pictureData.backgroundColor = SlptViewComponent.INVALID_COLOR;
					groupData.pictureList.add(pictureData);
					continue;
				}

				int width = picture.bitmap.getWidth();
				int height = picture.bitmap.getHeight();
				int len = width * height * 4;
				int[] buffer = new int[len];

				picture.bitmap.getPixels(buffer, 0, width, 0, 0, width, height);

				pictureData.pictureIndex = picture.pictureIndex;
				pictureData.width = picture.bitmap.getWidth();
				pictureData.height = picture.bitmap.getHeight();
				pictureData.bitmapBuffer = buffer;
				pictureData.backgroundColor = picture.backgroundColor;

				groupData.pictureList.add(pictureData);

				picture.recycle();

			}
			slptclock.groupList.add(groupData);
		}

		/**
		 * 把图片数据写到slptclock对象中链表里
		 */
		public static void writePictureToNativeList(SlptClock slptclock,
				PictureContainer container) {
			ArrayList<PictureGroup> list = container.list;

			writePictureGroupToNativeList(slptclock, container.miscGroup);

			for (int i = 0; i < list.size(); i++) {
				PictureGroup group = list.get(i);
				group.groupIndex = i;
				writePictureGroupToNativeList(slptclock, group);
			}
		}

	}

	public static class PictureGroup {
		ArrayList<Picture> list;
		public int capacity;
		PictureGroup group = null;
		int groupIndex;
		String name;
		Picture nullPicture = new ImagePicture((byte[]) null);

		public PictureGroup(int capacity) {
			list = new ArrayList<Picture>();
			list.ensureCapacity(capacity);
			for (int i = 0; i < capacity; i++) {
				list.add(nullPicture);
			}
			this.capacity = capacity;
		}

		public boolean isSubsetOf(PictureGroup group) {
			if (group == null)
				return false;

			if (list.size() > group.list.size())
				return false;

			for (int i = 0; i < list.size(); i++) {
				if (!list.get(i).equals(group.list.get(i)))
					return false;
			}

			return true;
		}

		public boolean findPicture(Picture picture) {
			if (picture == null)
				return false;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == picture)
					return true;
			}
			return false;
		}

		public Picture findSamePicture(Picture picture) {
			int index;

			if (picture == null)
				return null;

			index = list.indexOf(picture);
			return index >= 0 ? list.get(index) : null;
		}

		public boolean set(int index, Picture picture) {
			Picture old;

			if (picture == null)
				return false;

			if (index < 0 || index >= capacity)
				return false;

			old = list.get(index);
			if (old != null)
				old.recycle();
			list.set(index, picture);

			return true;
		}

		public boolean add(Picture picture) {
			if (picture == null)
				return false;

			list.add(picture);
			capacity = list.size();

			return true;
		}

		public String getName() {
			if (group != null)
				return group.getName();
			if (name != null)
				return name;
			return "" + groupIndex;
		}

		public void setBackgroundColorForAll(int backgroundColor) {
			for (int i = 0; i < list.size(); i++) {
				list.get(i).setBackgroundColor(backgroundColor);
			}
		}
	}

	public static class ImagePicture extends Picture {
		byte[] mem = null;
		String path = null;
		Bitmap bitmap = null;

		public ImagePicture(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		public ImagePicture(byte[] mem) {
			this.mem = mem;
		}

		public ImagePicture(String path) {
			this.path = path;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof ImagePicture))
				return false;

			ImagePicture picture = (ImagePicture) o;
			if (picture.mem != null || mem != null)
				return picture.mem == mem;
			if (picture.path != null || path != null)
				return picture.path == path;
			if (picture.bitmap != null || bitmap != null)
				return picture.bitmap == bitmap;

			return true;
		}

		@Override
		public void decodeBitmap() {
			Bitmap bitmap;

			if (mem != null) {
				bitmap = BitmapFactory.decodeByteArray(mem, 0, mem.length);
				super.bitmap = bitmap;
				return;
			}

			if (path != null && path.length() != 0) {
				bitmap = BitmapFactory.decodeFile(path);
				super.bitmap = bitmap;
				return;
			}

			super.bitmap = this.bitmap;
		}

		@Override
		public void recycle() {
			if (super.bitmap != null) {
				if (!super.bitmap.isRecycled())
					super.bitmap.recycle();
				super.bitmap = null;
			}
			this.bitmap = null;
		}
	}

	public static class StringPicture extends Picture {
		String str = null;
		Typeface typeface = Typeface.DEFAULT;
		BmpCreator creator = new BmpCreator();
		float textSize = 10;
		int textColor = 0xff000000;

		public StringPicture(String str) {
			this.str = new String(str);
		}

		public void setTypeFace(Typeface typeface) {
			if (typeface == null)
				typeface = Typeface.DEFAULT;

			this.typeface = typeface;
		}

		public void setTextSize(float textSize) {
			this.textSize = textSize;
		}

		public void setTextColor(int textColor) {
			this.textColor = textColor;
		}

		@Override
		public void setBackgroundColor(int backgroundColor) {
			this.backgroundColor = backgroundColor;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof StringPicture))
				return false;

			StringPicture picture = (StringPicture) o;
			if (str != null)
				return str.equals(picture.str)
						&& typeface.equals(picture.typeface)
						&& textSize == picture.textSize
						&& textColor == picture.textColor
						&& backgroundColor == picture.backgroundColor;
			return str == picture.str;
		}

		@Override
		public void decodeBitmap() {
			Bitmap bitmap;

			creator.setTypeface(typeface);
			creator.setTextSize(textSize);
			creator.setColor(textColor);
			creator.setBackgroundColor(backgroundColor);
			creator.setAntiAlias(backgroundColor != SlptViewComponent.INVALID_COLOR);

			if (str != null && str.length() != 0) {
				if (str.length() == 1)
					creator.decodeChar(str.charAt(0));
				else
					creator.decodeString(str);
				bitmap = creator.getBitmapNoCopy();
				super.bitmap = bitmap;
				return;
			}

			super.bitmap = null;
		}

		@Override
		public void recycle() {
			creator.recycle();
			super.bitmap = null;
		}
	}

}
