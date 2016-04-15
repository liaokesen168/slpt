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
package com.ingenic.iwds.datatransactor;

import java.io.Serializable;

/**
 * 文件信息存储类, 包含了文件名和长度等信息
 */
public class FileInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6561042250086394606L;

    /**
     * 文件名
     */
    public String name;

    /**
     * 文件长度
     */
    public long length;

    /**
     * 文件传输断点
     */
    public int chunkIndex;

    /**
     * 文件传输块大小
     */
    public int chunkSize;
}
