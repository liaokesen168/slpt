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

/**
 * 简单ID分配器，采用自增的方式分配ID。
 */
public class SimpleIDAlloter {

    /**
     * 无效的ID
     */
    public static final int INVALID = 0;
    private int mCurrentId;

    private SimpleIDAlloter() {
        mCurrentId = INVALID;
    }

    /**
     * 获得一个简单ID分配器的新实例。
     * 
     * @return 新的简单ID分配器实例
     */
    public static SimpleIDAlloter newInstance() {
        return new SimpleIDAlloter();
    }

    /**
     * 分配ID
     * 
     * @return 分配的ID
     */
    public int allocation() {
        return ++mCurrentId;
    }

    /**
     * 初始化分配器
     */
    public void initialize() {
        mCurrentId = INVALID;
    }

    /**
     * 判断ID是否有效
     * 
     * @param id 待判断ID
     * @return 有效返回true，无效返回false
     */
    public static boolean isValid(int id) {
        return id != INVALID;
    }
}