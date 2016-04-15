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

import java.security.acl.Group;
import java.util.ArrayList;

import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.slpt.view.core.Picture.PictureContainer;
import com.ingenic.iwds.slpt.view.utils.KeyWriter;

public class SlptLayout extends SlptViewComponent {
	private static final String TAG = "SlptLayout";
	ArrayList<SlptViewComponent> list = new ArrayList<SlptViewComponent>();

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

	public SlptViewComponent searchParentOnTop(SlptViewComponent child) {
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

	public int add(SlptViewComponent child) {
		IwdsAssert.dieIf(TAG, child == null, "child can not be null");
		IwdsAssert.dieIf(TAG, search(child), "child already be added");
		list.add(child);
		return list.size() - 1;
	}

	public int getIndex(SlptViewComponent child) {
		return child == null ? -1 : list.indexOf(child);
	}

	public SlptViewComponent get(int index) {
		return index < list.size() ? list.get(index) : null;
	}

	public void clear() {
		list.clear();
	}

	public int size() {
		return list.size();
	}

	@Override
	protected short initType() {
		return SVIEW_LAYOUT;
	}

	@Override
	public void registerPicture(PictureContainer container) {
		super.registerPicture(container);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).registerPicture(container);
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
