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

import android.graphics.Typeface;

import com.ingenic.iwds.slpt.view.core.Picture.PictureContainer;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;
import com.ingenic.iwds.utils.IwdsAssert;

/**
 * sview 中布局类的基类<br>
 * <br>
 * layout（SlptLayout）中可以添加随意个数的子sview（SlptViewComponent）<br>
 */
public class SlptLayout extends SlptViewComponent {
	private static final String TAG = "SlptLayout";
	ArrayList<SlptViewComponent> list = new ArrayList<SlptViewComponent>();

	/**
	 * 对所有的 SlptViewComponent 执行 setTextAttr 操作
	 * 
	 * @see SlptViewComponent.setTextAttr
	 */
	public void setTextAttrForAll(float textSize, int textColor,
			Typeface typeface) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			view.setTextAttr(textSize, textColor, typeface);
		}
	}

	/**
	 * 对所有的 SlptPictureView 执行 setStringPicture 操作
	 * 
	 * @see SlptPictureView.setStringPicture
	 */
	public void setStringPictureForAll(String str) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptPictureView)
				((SlptPictureView) view).setStringPicture(str);
		}
	}

	/**
	 * 对所有的 SlptPictureView 执行 setStringPicture 操作
	 * 
	 * @see SlptPictureView.setStringPicture
	 */
	public void setStringPictureForAll(char ch) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptPictureView)
				((SlptPictureView) view).setStringPicture(ch);
		}
	}

	/**
	 * 对所有的 SlptPictureView 执行 setImagePicture 操作
	 * 
	 * @see SlptPictureView.setImagePicture
	 */
	public void setImagePictureForAll(byte[] mem) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptPictureView)
				((SlptPictureView) view).setImagePicture(mem);
		}
	}

	/**
	 * 对所有的 SlptPictureView 执行 setImagePicture 操作
	 * 
	 * @see SlptPictureView.setImagePicture
	 */
	public void setImagePictureForAll(String path) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptPictureView)
				((SlptPictureView) view).setImagePicture(path);
		}
	}

	/**
	 * 对所有的 SlptNumView 执行 setStringPictureArray 操作
	 * 
	 * @see SlptNumView.setStringPictureArray
	 */
	public void setStringPictureArrayForAll(String[] array) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptNumView)
				((SlptNumView) view).setStringPictureArray(array);
		}
	}

	/**
	 * 对所有的 SlptNumView 执行 setStringPictureArray 操作
	 * 
	 * @see SlptNumView.setImagePictureArray
	 */
	public void setImagePictureArrayForAll(String[] array) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptNumView)
				((SlptNumView) view).setImagePictureArray(array);
		}
	}

	/**
	 * 对所有的 SlptNumView 执行 setStringPictureArray 操作
	 * 
	 * @see SlptNumView.setImagePictureArray
	 */
	public void setImagePictureArrayForAll(byte[][] array) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (view instanceof SlptNumView)
				((SlptNumView) view).setImagePictureArray(array);
		}
	}

	/**
	 * 深度优先搜索是否包含给定的sview
	 * 
	 * @param child
	 *            被搜索的sview
	 * 
	 * @return <p>
	 *         搜索结果<br>
	 *         false: 未搜索到<br>
	 *         true: 搜索到了<br>
	 * 
	 */
	public boolean search(SlptViewComponent child) {
		if (child == null)
			return false;

		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (child == view) {
				return true;
			}
			if (view instanceof SlptLayout) {
				if (((SlptLayout) view).search(child))
					return true;
			}
		}

		return false;
	}

	/**
	 * 查找直接包含给定sview的layout
	 * 
	 * @param child
	 *            被查找的sview
	 * 
	 * @return 包含给定sview的layout
	 */
	public SlptViewComponent searchParent(SlptViewComponent child) {
		for (int i = 0; i < list.size(); i++) {
			SlptViewComponent view = list.get(i);
			if (child == view) {
				return this;
			}
			if (view instanceof SlptLayout) {
				if (((SlptLayout) view).search(child))
					return view;
			}
		}

		return null;
	}

	/**
	 * 添加给定的sview
	 * 
	 * @param child
	 *            被添加的sview
	 * 
	 * @return 在layout中序号
	 */
	public int add(SlptViewComponent child) {
		IwdsAssert.dieIf(TAG, child == null, "child can not be null");
		IwdsAssert.dieIf(TAG, search(child), "child already be added");

		if (child instanceof SlptLayout)
			IwdsAssert.dieIf(TAG, ((SlptLayout) child).search(this),
					"can not add parent to a child");
		list.add(child);
		child.parent = this;

		return list.size() - 1;
	}

	/**
	 * 获得给定的sview在layout中的序号，不会在子layout中搜索
	 * 
	 * @param child
	 *            被搜索的sview
	 * 
	 * @return 在layout中序号。-1：表示不不直接包含在layout中
	 */
	public int getIndex(SlptViewComponent child) {
		return child == null ? -1 : list.indexOf(child);
	}

	/**
	 * 返回序号为index的sview
	 */
	public SlptViewComponent get(int index) {
		return index < list.size() ? list.get(index) : null;
	}

	/**
	 * 清除所有的子sview
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * 返回直接包含的子sview的个数
	 */
	public int size() {
		return list.size();
	}

	@Override
	protected short initType() {
		return SVIEW_LAYOUT;
	}

	@Override
	public void registerPicture(PictureContainer container,
			RegisterPictureParam param) {
		RegisterPictureParam mParam = param.clone();

		if (background.picture != null)
			mParam.backgroundColor = SlptViewComponent.INVALID_COLOR;
		else if (background.color != INVALID_COLOR)
			mParam.backgroundColor = background.color;

		super.registerPicture(container, mParam);

		for (int i = 0; i < list.size(); i++) {
			list.get(i).registerPicture(container, mParam);
		}
	}

	@Override
	public void writeConfigure(KeyWriter writer) {
		super.writeConfigure(writer);
		writer.writeInt(list.size());

		for (int i = 0; i < list.size(); i++) {
			list.get(i).writeConfigure(writer);
		}
	}
}
