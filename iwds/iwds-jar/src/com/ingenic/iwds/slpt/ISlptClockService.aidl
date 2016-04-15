package com.ingenic.iwds.slpt;

import com.ingenic.iwds.slpt.SlptClock;

interface ISlptClockService {
	boolean startClock();
	boolean stopClock();
	boolean clockIsStart();
	boolean sendSlptStart(String uuid);
	boolean sendSlptEnd(String uuid);
	boolean sendViewArr(in byte[] arr);
	boolean sendGroupData(in SlptClock slptClock);
	boolean sendRleArray(in int[] rle);
}