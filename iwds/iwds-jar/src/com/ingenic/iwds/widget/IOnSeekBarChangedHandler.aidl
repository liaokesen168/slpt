package com.ingenic.iwds.widget;

oneway interface IOnSeekBarChangedHandler {

void handleProgressChanged(String callingPkg, int viewId, int progress, boolean fromUser);

void handleStartTrackingTouch(String callingPkg, int viewId);

void handleStopTrackingTouch(String callingPkg, int viewId);

}