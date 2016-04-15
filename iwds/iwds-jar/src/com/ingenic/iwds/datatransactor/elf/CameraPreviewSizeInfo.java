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

import java.io.Serializable;

/**
 * 摄像头预览大小信息类.
 */
public class CameraPreviewSizeInfo implements Serializable {

    private static final long serialVersionUID = 3088957725138490091L;

    /** 如果是前置摄像头, 值为{@code true}. */
    public boolean isFrontCamera = false;

    /** 预览图像宽度. */
    public int width = 0;

    /** 预览图像高度. */
    public int heigth = 0;

    @Override
    public String toString() {
        return "CameraPreviewSizeInfo [isFrontCamera=" + isFrontCamera
                + ", width=" + width + ", heigth=" + heigth + "]";
    }
}
