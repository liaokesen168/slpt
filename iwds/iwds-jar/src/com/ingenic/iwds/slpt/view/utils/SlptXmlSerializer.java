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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class SlptXmlSerializer implements XmlSerializer {
	XmlSerializer serializer;
	StringBuffer buffer;
	int lastDepth;

	SlptXmlSerializer() {
		serializer = Xml.newSerializer();
		buffer = new StringBuffer();
		buffer.setLength(2 + 2);
		lastDepth = 0;
		buffer.setCharAt(0, '\r');
		buffer.setCharAt(1, '\n');
		buffer.setCharAt(2, ' ');
		buffer.setCharAt(3, ' ');
	}

	@Override
	public void setFeature(String name, boolean state)
			throws IllegalArgumentException, IllegalStateException {
		serializer.setFeature(name, state);
	}

	@Override
	public boolean getFeature(String name) {
		return serializer.getFeature(name);
	}

	@Override
	public void setProperty(String name, Object value)
			throws IllegalArgumentException, IllegalStateException {
		serializer.setProperty(name, value);
	}

	@Override
	public Object getProperty(String name) {
		return serializer.getProperty(name);
	}

	@Override
	public void setOutput(OutputStream os, String encoding)
			throws IOException, IllegalArgumentException,
			IllegalStateException {
		serializer.setOutput(os, encoding);
	}

	@Override
	public void setOutput(Writer writer) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.setOutput(writer);
	}

	@Override
	public void startDocument(String encoding, Boolean standalone)
			throws IOException, IllegalArgumentException,
			IllegalStateException {
		serializer.startDocument(encoding, standalone);
	}

	@Override
	public void endDocument() throws IOException, IllegalArgumentException,
			IllegalStateException {
		serializer.endDocument();
	}

	@Override
	public void setPrefix(String prefix, String namespace)
			throws IOException, IllegalArgumentException,
			IllegalStateException {
		serializer.setPrefix(prefix, namespace);
	}

	@Override
	public String getPrefix(String namespace, boolean generatePrefix)
			throws IllegalArgumentException {
		return serializer.getPrefix(namespace, generatePrefix);
	}

	@Override
	public int getDepth() {
		return serializer.getDepth();
	}

	@Override
	public String getNamespace() {
		return serializer.getNamespace();
	}

	@Override
	public String getName() {
		return serializer.getName();
	}

	@Override
	public XmlSerializer startTag(String namespace, String name)
			throws IOException, IllegalArgumentException,
			IllegalStateException {
		return serializer.startTag(namespace, name);
	}

	@Override
	public XmlSerializer attribute(String namespace, String name,
			String value) throws IOException, IllegalArgumentException,
			IllegalStateException {
		int depth = serializer.getDepth();
		int end = 2 + depth * 2 + 2;
		int start = 2 + lastDepth * 2 + 2;

		buffer.setLength(end + name.length());
		for (int i = start; i < end; i++) {
			buffer.setCharAt(i, ' ');
		}
		buffer.replace(end, end + name.length(), name);

		lastDepth = depth;

		return serializer.attribute(namespace,
				buffer.substring(0, end + name.length()), value);
	}

	@Override
	public XmlSerializer endTag(String namespace, String name)
			throws IOException, IllegalArgumentException,
			IllegalStateException {
		return serializer.endTag(namespace, name);
	}

	@Override
	public XmlSerializer text(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		return serializer.text(text);
	}

	@Override
	public XmlSerializer text(char[] buf, int start, int len)
			throws IOException, IllegalArgumentException,
			IllegalStateException {
		return serializer.text(buf, start, len);
	}

	@Override
	public void cdsect(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.cdsect(text);
	}

	@Override
	public void entityRef(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.entityRef(text);
	}

	@Override
	public void processingInstruction(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.processingInstruction(text);
	}

	@Override
	public void comment(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.comment(text);
	}

	@Override
	public void docdecl(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.docdecl(text);
	}

	@Override
	public void ignorableWhitespace(String text) throws IOException,
			IllegalArgumentException, IllegalStateException {
		serializer.ignorableWhitespace(text);
	}

	@Override
	public void flush() throws IOException {
		serializer.flush();
	}

}
