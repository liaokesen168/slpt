/*
 *  Copyright (C) 2014 Ingenic Semiconductor
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

package com.ingenic.iwds.smartvibrate;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Vibrator;

import com.ingenic.iwds.common.api.ServiceManagerContext;
import com.ingenic.iwds.utils.IwdsLog;

public class VibrateServiceManager extends ServiceManagerContext {

    /**
     * <p>special_vibrate(int[] modes) 方法中数组成员的可取值</p>
     *
     * 以下表示了震动IC支持的各种震动模式，设置不同震动模式会有不同的震动效果.
     */
    public static final int VIBRATE_DELAY = 0;
    public static final int VIBRATE_STRONG_CLICK1 = 1;
    public static final int VIBRATE_STRONG_CLICK2 = 2;
    public static final int VIBRATE_STRONG_CLICK3 = 3;
    public static final int VIBRATE_SHARP_CLICK1 = 4;
    public static final int VIBRATE_SHARP_CLICK2 = 5;
    public static final int VIBRATE_SHARP_CLICK3 = 6;
    public static final int VIBRATE_SOFT_BUMP1 = 7;
    public static final int VIBRATE_SOFT_BUMP2 = 8;
    public static final int VIBRATE_SOFT_BUMP3 = 9;
    public static final int VIBRATE_DOUBLE_CLICK1 = 10;
    public static final int VIBRATE_DOUBLE_CLICK2 = 11;
    public static final int VIBRATE_TRIPLE_CLICK = 12;
    public static final int VIBRATE_SOFT_FUZZ = 13;
    public static final int VIBRATE_STRONG_FUZZ = 14;
    public static final int VIBRATE_750MS_ALERT = 15;
    public static final int VIBRATE_1000MS_ALERT = 16;
    public static final int VIBRATE_STRONG_CLICK_1 = 17;
    public static final int VIBRATE_STRONG_CLICK_2 = 18;
    public static final int VIBRATE_STRONG_CLICK_3 = 19;
    public static final int VIBRATE_STRONG_CLICK_4 = 20;
    public static final int VIBRATE_MEDIUM_CLICK_1 = 21;
    public static final int VIBRATE_MEDIUM_CLICK_2 = 22;
    public static final int VIBRATE_MEDIUM_CLICK_3 = 23;
    public static final int VIBRATE_SHARP_TICK_1 = 24;
    public static final int VIBRATE_SHARP_TICK_2 = 25;
    public static final int VIBRATE_SHARP_TICK_3 = 26;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_STRONG_1 = 27;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_STRONG_2 = 28;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_STRONG_3 = 29;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_STRONG_4 = 30;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_MEDIUM_1 = 31;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_MEDIUM_2 = 32;
    public static final int VIBRATE_SHORT_DOUBLE_CLICK_MEDIUM_3 = 33;
    public static final int VIBRATE_SHORT_DOUBLE_SHARP_TICK_1 = 34;
    public static final int VIBRATE_SHORT_DOUBLE_SHARP_TICK_2 = 35;
    public static final int VIBRATE_SHORT_DOUBLE_SHARP_TICK_3 = 36;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_STRONG_1 = 37;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_STRONG_2 = 38;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_STRONG_3 = 39;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_STRONG_4 = 40;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_MEDIUM_1 = 41;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_MEDIUM_2 = 42;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_MEDIUM_3 = 43;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_TICK_1 = 44;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_TICK_2 = 45;
    public static final int VIBRATE_LONG_DOUBLE_SHARP_CLICK_TICK_3 = 46;
    public static final int VIBRATE_BUZZ_1 = 47;
    public static final int VIBRATE_BUZZ_2 = 48;
    public static final int VIBRATE_BUZZ_3 = 49;
    public static final int VIBRATE_BUZZ_4 = 50;
    public static final int VIBRATE_BUZZ_5 = 51;
    public static final int VIBRATE_PULSING_STRONG_1 = 52;
    public static final int VIBRATE_PULSING_STRONG_2 = 53;
    public static final int VIBRATE_PULSING_MEDIUM_1 = 54;
    public static final int VIBRATE_PULSING_MEDIUM_2 = 55;
    public static final int VIBRATE_PULSING_SHARP_1 = 56;
    public static final int VIBRATE_PULSING_SHARP_2 = 57;
    public static final int VIBRATE_TRANSITION_CLICK_1 = 58;
    public static final int VIBRATE_TRANSITION_CLICK_2 = 59;
    public static final int VIBRATE_TRANSITION_CLICK_3 = 60;
    public static final int VIBRATE_TRANSITION_CLICK_4 = 61;
    public static final int VIBRATE_TRANSITION_CLICK_5 = 62;
    public static final int VIBRATE_TRANSITION_CLICK_6 = 63;
    public static final int VIBRATE_TRANSITION_HUM_1 = 64;
    public static final int VIBRATE_TRANSITION_HUM_2 = 65;
    public static final int VIBRATE_TRANSITION_HUM_3 = 66;
    public static final int VIBRATE_TRANSITION_HUM_4 = 67;
    public static final int VIBRATE_TRANSITION_HUM_5 = 68;
    public static final int VIBRATE_TRANSITION_HUM_6 = 69;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SMOOTH_1 = 70;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SMOOTH_2 = 71;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SMOOTH_1 = 72;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SMOOTH_2 = 73;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SMOOTH_1 = 74;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SMOOTH_2 = 75;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_1 = 76;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_2 = 77;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SHARP_1 = 78;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SHARP_2 = 79;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SHARP_1 = 80;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SHARP_2 = 81;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SMOOTH_1 = 82;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SMOOTH_2 = 83;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SMOOTH_1 = 84;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SMOOTH_2 = 85;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SMOOTH_1 = 86;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SMOOTH_2 = 87;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_1 = 88;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_2 = 89;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SHARP_1 = 90;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SHARP_2 = 91;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SHARP_1 = 92;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SHARP_2 = 93;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SMOOTH_1_HALF = 94;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SMOOTH_2_HALF = 95;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SMOOTH_1_HALF = 96;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SMOOTH_2_HALF = 97;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SMOOTH_1_HALF = 98;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SMOOTH_2_HALF = 99;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_1_HALF = 100;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_2_HALF = 101;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SHARP_1_HALF = 102;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_MEDIUM_SHARP_2_HALF = 103;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SHARP_1_HALF = 104;
    public static final int VIBRATE_TRANSITION_RAMP_DOWN_SHORT_SHARP_2_HALF = 105;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SMOOTH_1_HALF = 106;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SMOOTH_2_HALF = 107;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SMOOTH_1_HALF = 108;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SMOOTH_2_HALF = 109;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SMOOTH_1_HALF = 110;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SMOOTH_2_HALF = 111;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_1_HALF = 112;
    public static final int VIBRATE_TRANSITION_RAMP_UP_LONG_SHARP_2_HALF = 113;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SHARP_1_HALF = 114;
    public static final int VIBRATE_TRANSITION_RAMP_UP_MEDIUM_SHARP_2_HALF = 115;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SHARP_1_HALF = 116;
    public static final int VIBRATE_TRANSITION_RAMP_UP_SHORT_SHARP_2_HALF = 117;
    public static final int VIBRATE_LONG_BUZZ_STOPPING = 118;
    public static final int VIBRATE_SMOOTH_HUM_1 = 119;
    public static final int VIBRATE_SMOOTH_HUM_2 = 120;
    public static final int VIBRATE_SMOOTH_HUM_3 = 121;
    public static final int VIBRATE_SMOOTH_HUM_4 = 122;
    public static final int VIBRATE_SMOOTH_HUM_5 = 123;

    public class VibrateModes {
        public int[] effect;
        public int length;
    }

    public VibrateModes GetVibrateModes(int effect[], int len) {
        VibrateModes vm = new VibrateModes();
        vm.effect = new int[len];
        for (int i = 0; i < len; i++) {
            vm.effect[i] = effect[i];
        }
        return vm;
    }

    public VibrateModes GetCustomModes(int number) {
        VibrateModes vm = new VibrateModes();
        vm.effect = new int[3];

        switch (number) {
        case 1:
            vm.effect[0] = VIBRATE_DOUBLE_CLICK1;
            vm.effect[1] = VIBRATE_DOUBLE_CLICK1;
            vm.effect[2] = VIBRATE_DOUBLE_CLICK1;
            break;

        case 2:
            vm.effect[0] = VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_1;
            vm.effect[1] = VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_1;
            vm.effect[2] = VIBRATE_TRANSITION_RAMP_DOWN_LONG_SHARP_1;
            break;

        case 3:
            vm.effect[0] = VIBRATE_TRANSITION_HUM_1;
            vm.effect[1] = VIBRATE_DELAY;
            vm.effect[2] = VIBRATE_DELAY;
            break;

        default:
            return null;
        }

        return vm;
    }

    private final Vibrator sysVibrator;
    private IVibrateService m_service;

    /**
     * <p>检验硬件是否存在震动器.
     *
     * @return 如果有将返回true.
     */
    public boolean hasVibrator(){
        return sysVibrator.hasVibrator();
    }

    /**
     * 震动器以milliseconds参数震动.
     * <p>这个方法需要申请如下权限
     * {@link android.Manifest.permission#VIBRATE}.
     *
     * @param milliseconds 震动时间.
     */
    public void vibrate(long milliseconds) {
        sysVibrator.vibrate(milliseconds);
    }

    /**
     * 震动器以pattern震动模式震动.
     *
     * <p>
     * 方法将需要传递一个long数组参数, 它们的单位都是毫秒, 第一个值代表震动开启时间, 第二
     * 个值代表关闭时间, 后面开关时间依此类推
     * </p><p>
     * 第二个参数是震动周期设置, -1 代表不循环震动
     * </p>
     * <p>这个方法需要申请如下权限
     * {@link android.Manifest.permission#VIBRATE}.
     *
     * @param pattern 震动开关时间
     * @param repeat 动周期设置, -1 代表不循环震动
     */
    public void vibrate(long[] pattern, int repeat) {
        sysVibrator.vibrate(pattern, repeat);
    }

    /**
     * 振动器以modes 数组的震动效果震动.
     *
     * <p>这个方法需要申请如下权限
     * {@link android.Manifest.permission#VIBRATE}.
     *
     * @param vm 数组的内部成员是震动效果的特殊模式, 它们定义于本类中.
     */
    public void SpecialVibrate(VibrateModes vm) {
        if (vm == null)
            return;

        try {
            m_service.Drv2605Vibrate(vm.effect);
        } catch (RemoteException e) {
            IwdsLog.e(this, "Exception in vibrate: " + e.toString());
        }
    }

    /**
     * 关闭震动器.
     * <p>这个方法需要申请如下权限
     * {@link android.Manifest.permission#VIBRATE}.
     */
    public void cancel() {
        sysVibrator.cancel();
    }

    /**
     * 不要直接构造, 构造 {@link com.ingenic.iwds.common.api.ServiceClient ServiceClient} 时会自动构造
     * 详细用法参考典型用例
     */
    public VibrateServiceManager(Context context) {
        super(context);

        sysVibrator = (Vibrator) context.getSystemService("vibrator");

        m_serviceClientProxy = new ServiceClientProxy() {
            @Override
            public void onServiceConnected(IBinder service) {
                m_service = IVibrateService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(boolean unexpected) {
            }

            @Override
            public IBinder getBinder() {
                return m_service.asBinder();
            }
        };
    }
}
