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

package com.ingenic.iwds.slpt.view.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class SimpleFile {
	static final String TAG = "simpleFile";

	static public FileInputStream getInputStream(String path) {
		File file = new File(path);
		FileInputStream in = null;

		if (!file.exists()) {
			Log.d(TAG, "file not exist" + "[" + path + "]");
			return null;
		}

		if (!file.canRead()) {
			Log.d(TAG, "file can not be read, permission denied" + "[" + path
					+ "]");
			return null;
		}

		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Failed to get a file read stream" + "[" + path + "]");
		}

		return in;
	}

	static public Boolean closeInputStream(FileInputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				Log.d(TAG, "Failed to close file");
				return false;
			}
		}
		return true;
	}

	static public String readFileStr(String path, int len) {
		byte[] bytes = readFile(path, len);

		return bytes == null ? null : new String(bytes);
	}

	static public byte[] readFile(String path, int len) {
		byte[] buf = null;
		FileInputStream in = null;

		in = getInputStream(path);
		if (in == null)
			return null;

		try {
			buf = new byte[len];
			in.read(buf);
		} catch (IOException e) {
			Log.d(TAG, "Failed to read file" + "[" + path + "]");
			buf = null;
		}

		closeInputStream(in);

		return buf;
	}

	static public FileOutputStream getOutputStream(String path) {
		File file = new File(path);
		FileOutputStream out = null;

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Log.d(TAG, "Unable to create the new file" + "[" + path + "]");
				return null;
			}
		}

		if (!file.canWrite()) {
			Log.d(TAG, "file can not be write, permission denied" + "[" + path
					+ "]");
			return null;
		}

		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Failed to get a file out stream" + "[" + path + "]");
		}

		return out;
	}

	static public Boolean closeOutputStream(FileOutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				Log.d(TAG, "Failed to close file");
				return false;
			}
		}
		return true;
	}

	static Boolean writeFileStr(String path, String str) {
		return writeFile(path, str.getBytes());
	}

	static public Boolean writeFile(String path, byte[] bytes) {
		Boolean ret = false;
		FileOutputStream out = null;

		if (bytes == null) {
			Log.d(TAG, "Invalid args");
			return false;
		}

		out = getOutputStream(path);
		if (out == null)
			return false;

		try {
			out.write(bytes);
			out.flush();
			ret = true;
		} catch (IOException e) {
			Log.d(TAG, "Failed to write or flush file" + "[" + path + "]");
		}

		closeOutputStream(out);

		return ret;
	}
}