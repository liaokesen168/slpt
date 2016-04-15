package com.ingenic.iwds.slpt;

import java.util.Arrays;

public class RleBuffer {
	private final static int SIZE = 10 * 1000;
	private int[] rleBuf = new int[SIZE];
	private int rleOffset = 0;
	
	public int add(int colors[], int sendOffset) {
		int count = 0;
		int color;
		int i;

		if (colors.length <= sendOffset)
			return 0;

		if (rleBuf.length <= rleOffset)
			return 0;

		for (i = sendOffset, color = colors[sendOffset]; i < colors.length; i++) {
			if (colors[i] == color) {
				count++;
				continue;
			}

			rleBuf[rleOffset++] = color;
			rleBuf[rleOffset++] = count;

			if (rleOffset >= rleBuf.length)
				break;

			color = colors[i];
			count = 1;
		}

		if (rleOffset < rleBuf.length) {
			rleBuf[rleOffset++] = color;
			rleBuf[rleOffset++] = count;
		}

		return i;
	}

	public int[] getBuffer() {
		int[] buf;

		if (rleOffset == 0)
			buf = null;
		else if (rleOffset == rleBuf.length)
			buf = rleBuf;
		else
			buf = Arrays.copyOfRange(rleBuf, 0, rleOffset);

		clear();

		return buf;
	}

	public void clear() {
		rleOffset = 0;
	}

	public int getOffset() {
		return rleOffset;
	}

}
