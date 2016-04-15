/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wenzhong.sun@ingenic.com, wanmyqawdr@126.com>
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
 *
 */

package com.ingenic.iwds.datatransactor.elf;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

import com.ingenic.iwds.utils.IwdsAssert;

/**
 * 日程信息类.
 */
public class ScheduleInfo implements Parcelable {

    /** 事件数量. */
    public int eventCount;

    /** 事件. */
    public Event event[];

    /**
     * 实例化日程信息对象.
     * 
     * @param count
     *            事件数量 count等于0，表示没有日程
     */
    public ScheduleInfo(int count) {
        IwdsAssert.dieIf(this, count < 0, "Event count < 0.");

        eventCount = count;

        if (count != 0) {
            event = new Event[eventCount];
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(eventCount);

        if (event != null) {
            dest.writeTypedArray(event, flags);
        }
    }

    public static final Creator<ScheduleInfo> CREATOR = new Creator<ScheduleInfo>() {
        @Override
        public ScheduleInfo createFromParcel(Parcel source) {
            int count = source.readInt();
            ScheduleInfo info = new ScheduleInfo(count);

            info.eventCount = count;
            if (info.event != null) {
                source.readTypedArray(info.event, Event.CREATOR);
            }

            return info;
        }

        @Override
        public ScheduleInfo[] newArray(int size) {
            return new ScheduleInfo[size];
        }
    };

    /**
     * 事件类.
     */
    public static class Event implements Parcelable {

        /** 该条日程在手机的数据库中的ID. */
        public long id = 0;

        /** 标题：日程名称. */
        public String title = null;

        /** 地点：日程的地点. */
        public String eventLocation = null;

        /** 描述：日程的内容. */
        public String description = null;

        /** 开始时间：日期+时间. */
        public long dtStart = 0;

        /** 结束时间：日期+时间. */
        public long dtEnd = 0;

        /** 时区. */
        public String eventTimezone = null;

        /** 持续时间. */
        public String duration = null;

        /** 是否是一整天. */
        public int allDay = 0;

        /** 是否有闹钟. */
        public int hasAlarm = 0;

        /** 重复. */
        public String rrule = null;

        /** 提醒. */
        public Reminder reminder = null;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);

            dest.writeString(title);
            dest.writeString(eventLocation);
            dest.writeString(description);

            dest.writeLong(dtStart);
            dest.writeLong(dtEnd);

            dest.writeString(eventTimezone);
            dest.writeString(duration);

            dest.writeInt(allDay);
            dest.writeInt(hasAlarm);

            dest.writeString(rrule);

            dest.writeParcelable(reminder, flags);
        }

        public static final Creator<Event> CREATOR = new Creator<Event>() {
            @Override
            public Event createFromParcel(Parcel source) {
                Event info = new Event();

                info.id = source.readLong();

                info.title = source.readString();
                info.eventLocation = source.readString();
                info.description = source.readString();

                info.dtStart = source.readLong();
                info.dtEnd = source.readLong();

                info.eventTimezone = source.readString();
                info.duration = source.readString();

                info.allDay = source.readInt();
                info.hasAlarm = source.readInt();

                info.rrule = source.readString();

                info.reminder = source.readParcelable(Reminder.class
                        .getClassLoader());

                return info;
            }

            @Override
            public Event[] newArray(int size) {
                return new Event[size];
            }
        };

        @Override
        public String toString() {
            return "Event [id=" + id + ", title=" + title + ", eventLocation="
                    + eventLocation + ", description=" + description
                    + ", dtStart=" + dtStart + ", dtEnd=" + dtEnd
                    + ", eventTimezone=" + eventTimezone + ", duration="
                    + duration + ", allDay=" + allDay + ", hasAlarm="
                    + hasAlarm + ", rrule=" + rrule + ", reminder=" + reminder
                    + "]";
        }
    }

    /**
     * 该类对应手机端中的一条日程的提醒设置，日程可能没有设置提醒， 但是一条提醒 设置只能对应一条日程.
     * 可以在手机端的日历的数据库中的Reminders表中获取下面的字段.
     */
    public static class Reminder implements Parcelable {

        /** 该条提醒在手机端的数据库中的ID. */
        public long id;

        /** 该条提醒对应的日程的在手机端的数据库中的ID（日程的ID）. */
        public long eventId;

        /** 提醒持续的时间（分钟）. */
        public long minutes;

        /** 提醒的方式. */
        public long method;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeLong(eventId);
            dest.writeLong(minutes);
            dest.writeLong(method);
        }

        public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
            @Override
            public Reminder createFromParcel(Parcel source) {
                Reminder info = new Reminder();

                info.id = source.readLong();
                info.eventId = source.readLong();
                info.minutes = source.readLong();
                info.method = source.readLong();

                return info;
            }

            @Override
            public Reminder[] newArray(int size) {
                return new Reminder[size];
            }
        };

        @Override
        public String toString() {
            return "Reminder [id=" + id + ", eventId=" + eventId + ", minutes="
                    + minutes + ", method=" + method + "]";
        }
    }

    @Override
    public String toString() {
        return "ScheduleInfo [event=" + Arrays.toString(event) + "]";
    }

}
