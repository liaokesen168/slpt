/**
 * 
 */
package com.example.cloudtest;

import java.util.Calendar;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 *
 */
public class Utils {
    /**
     * 获取某日期的毫秒数
     * @param  year  年
     * @param  month 月
     * @param  day   日
     * @return       指定年月日的毫秒数
     */
    public static long DateMilliSeconds(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);

        return calendar.getTimeInMillis();
    };
    
    /**
     * 获取某日期的毫秒数
     * @param  year   年
     * @param  month  月
     * @param  day    日
     * @param  hour   小时
     * @param  minute 分
     * @param  second 秒
     * @return        指定年月日小时分秒的毫秒数
     */
    public static long DateMilliSeconds(int year, int month, int day, int hour,
            int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);

        return calendar.getTimeInMillis();
    };

    /**
     * 判断表达式是否为邮件
     * @param  email 邮件字符串
     * @return       
     */
    public static boolean isEmail(String email) {
        String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
        Pattern pattern = Pattern.compile(regex);

        return ((email != null) && pattern.matcher(email).matches());
    }

    /**
     * 判断表达式是否为手机号码
     * @param  phone 手机号码字符串
     * @return     
     */
    public static boolean isPhoneNumber(String phone) {
        Pattern pattern = Pattern.compile("1[0-9]{10}");
        return ((phone != null) && pattern.matcher(phone).matches());
    }
    
    /**
     * 显示Toast
     * @param context 上下文
     * @param text    显示文字
     */
    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示Toast，显示位置在 view 附近
     * @param context 上下文
     * @param text    显示文字
     * @param view    
     */
    public static void showToast(Context context, String text, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, location[0], location[1]);
        toast.show();
    }

    /**
     * 控件获得焦点
     * @param view 
     */
    public static void setFocus(View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.requestFocusFromTouch();
    }

}
