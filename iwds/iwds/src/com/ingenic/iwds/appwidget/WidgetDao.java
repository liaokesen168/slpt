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
package com.ingenic.iwds.appwidget;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

class WidgetDao {

    static final String TABLE_WIDGET = "widget";

    private static final String[] COLUMNS = new String[] { WidgetColumns._ID, // 0
            WidgetColumns.RESTORED_ID, // 1
            WidgetColumns.PROVIDER_PKG, // 2
            WidgetColumns.PROVIDER_CLS, // 3
            WidgetColumns.HOST_PKG, // 4
            WidgetColumns.WIDTH, // 5
            WidgetColumns.HEIGHT // 6
            };

    private final WidgetSQLiteOpenHelper mHelper;

    WidgetDao(Context context) {
        mHelper = new WidgetSQLiteOpenHelper(context);
    }

    public long insert(WidgetInfo info) {
        return insert(info.getId(), info.rid, info.ppkg, info.pcls, info.hpkg, info.width,
                info.height);
    }

    public long insert(int id, int rid, String ppkg, String pcls, String hpkg, int w, int h) {
        ContentValues values = new ContentValues();
        values.put(WidgetColumns._ID, id);

        if (rid >= 0) {
            values.put(WidgetColumns.RESTORED_ID, rid);
        }

        values.put(WidgetColumns.PROVIDER_PKG, ppkg);
        values.put(WidgetColumns.PROVIDER_CLS, pcls);
        values.put(WidgetColumns.HOST_PKG, hpkg);

        if (w >= 0 && h >= 0) {
            values.put(WidgetColumns.WIDTH, w);
            values.put(WidgetColumns.HEIGHT, h);
        }

        synchronized (mHelper) {
            SQLiteDatabase db = mHelper.getWritableDatabase();

            long result = db.insert(TABLE_WIDGET, null, values);
            db.close();

            return result;
        }
    }

    public int update(WidgetInfo info) {
        return update(info.getId(), info.rid, info.ppkg, info.pcls, info.hpkg, info.width,
                info.height);
    }

    public int update(int id, int rid, String ppkg, String pcls, String hpkg, int w, int h) {
        ContentValues values = new ContentValues();

        if (rid >= 0) {
            values.put(WidgetColumns.RESTORED_ID, rid);
        }

        values.put(WidgetColumns.PROVIDER_PKG, ppkg);
        values.put(WidgetColumns.PROVIDER_CLS, pcls);
        values.put(WidgetColumns.HOST_PKG, hpkg);

        if (w >= 0 && h >= 0) {
            values.put(WidgetColumns.WIDTH, w);
            values.put(WidgetColumns.HEIGHT, h);
        }

        synchronized (mHelper) {
            SQLiteDatabase db = mHelper.getWritableDatabase();

            int result =
                    db.update(TABLE_WIDGET, values, WidgetColumns._ID + " = ? ",
                            new String[] { String.valueOf(id) });
            db.close();

            return result;
        }
    }

    public ArrayList<WidgetInfo> queryAll() {
        ArrayList<WidgetInfo> infos = null;

        synchronized (mHelper) {
            SQLiteDatabase db = mHelper.getReadableDatabase();

            Cursor cursor =
                    db.query(TABLE_WIDGET, COLUMNS, null, null, null, null, WidgetColumns._ID
                            + " asc");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    infos = new ArrayList<WidgetInfo>();

                    do {
                        int id = cursor.getInt(0);
                        WidgetInfo info = new WidgetInfo(id);

                        info.rid = cursor.getInt(1);
                        info.ppkg = cursor.getString(2);
                        info.pcls = cursor.getString(3);
                        info.hpkg = cursor.getString(4);
                        info.width = cursor.getInt(5);
                        info.height = cursor.getInt(6);

                        infos.add(info);
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }

            db.close();
        }

        return infos;
    }

    public void delete(WidgetInfo info) {
        delete(info.getId());
    }

    public int delete(int id) {
        synchronized (mHelper) {
            SQLiteDatabase db = mHelper.getWritableDatabase();

            int result =
                    db.delete(TABLE_WIDGET, WidgetColumns._ID + " = ? ",
                            new String[] { String.valueOf(id) });
            db.close();

            return result;
        }
    }

    public int deleteAll() {
        synchronized (mHelper) {
            SQLiteDatabase db = mHelper.getWritableDatabase();

            int result = db.delete(TABLE_WIDGET, null, null);
            db.close();

            return result;
        }
    }

    interface WidgetColumns extends BaseColumns {
        String RESTORED_ID = "rid";
        String PROVIDER_PKG = "ppkg";
        String PROVIDER_CLS = "pcls";
        String HOST_PKG = "hpkg";
        String WIDTH = "width";
        String HEIGHT = "height";
    }

    private static class WidgetSQLiteOpenHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "widget.db";
        private static final int VERSION = 1;

        private static final String SQL_CREATE = "CREATE TABLE ";
        private static final String SQL_DROP = "DROP TABLE IF EXISTS ";

        public WidgetSQLiteOpenHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE + TABLE_WIDGET + " (" + WidgetColumns._ID
                    + " INTEGER PRIMARY KEY, " + WidgetColumns.RESTORED_ID + " INTEGER, "
                    + WidgetColumns.PROVIDER_PKG + " TEXT, " + WidgetColumns.PROVIDER_CLS
                    + " TEXT, " + WidgetColumns.HOST_PKG + " TEXT, " + WidgetColumns.WIDTH
                    + " INTEGER, " + WidgetColumns.HEIGHT + " INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP + TABLE_WIDGET);
            onCreate(db);
        }
    }
}