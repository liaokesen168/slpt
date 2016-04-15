package com.ingenic.iwds.slpt;

interface ISlptClockService {
	boolean startClock();
	boolean stopClock();
	boolean clockIsStart();
}