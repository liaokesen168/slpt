/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
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


#ifndef TIMER_H
#define TIMER_H

#include <sys/types.h>
#include <sys/time.h>

#include <stdint.h>

#include <errno.h>

#include <utils/log.h>
#include <utils/assert.h>

namespace Iwds {
    typedef int64_t nsecs_t;       // nano-seconds

    inline void nanosleep(timespec amount)
    {
        /*
         * We'd like to use clock_nanosleep.
         *
         * But clock_nanosleep is from POSIX.1-2001 and both are *not*
         * affected by clock changes when using relative sleeps, even for
         * CLOCK_REALTIME.
         *
         * nanosleep is POSIX.1-1993
         */

        int r;
        do {
            r = ::nanosleep(&amount, &amount);
        } while (r == -1 && errno == EINTR);
    }

    inline nsecs_t seconds_to_nanoseconds(nsecs_t secs)
    {
        return secs * 1000000000;
    }

    inline nsecs_t milliseconds_to_nanoseconds(nsecs_t secs)
    {
        return secs * 1000000;
    }

    inline nsecs_t microseconds_to_nanoseconds(nsecs_t secs)
    {
        return secs * 1000;
    }

    inline nsecs_t nanoseconds_to_seconds(nsecs_t secs)
    {
        return secs / 1000000000;
    }

    inline nsecs_t nanoseconds_to_milliseconds(nsecs_t secs)
    {
        return secs / 1000000;
    }

    inline nsecs_t nanoseconds_to_microseconds(nsecs_t secs)
    {
        return secs / 1000;
    }

    inline nsecs_t s2ns(nsecs_t v)
    {
        return seconds_to_nanoseconds(v);
    }

    inline nsecs_t ms2ns(nsecs_t v)
    {
        return milliseconds_to_nanoseconds(v);
    }

    inline nsecs_t us2ns(nsecs_t v)
    {
        return microseconds_to_nanoseconds(v);
    }

    inline nsecs_t ns2s(nsecs_t v)
    {
        return nanoseconds_to_seconds(v);
    }

    inline nsecs_t ns2ms(nsecs_t v)
    {
        return nanoseconds_to_milliseconds(v);
    }

    inline nsecs_t ns2us(nsecs_t v)
    {
        return nanoseconds_to_microseconds(v);
    }

    inline nsecs_t seconds(nsecs_t v)
    {
        return s2ns(v);
    }

    inline nsecs_t milliseconds(nsecs_t v)
    {
        return ms2ns(v);
    }

    inline nsecs_t microseconds(nsecs_t v)
    {
        return us2ns(v);
    }

    enum {
        SYSTEM_TIME_REALTIME = 0,  // system-wide realtime clock
        SYSTEM_TIME_MONOTONIC = 1, // monotonic time since unspecified starting point
        SYSTEM_TIME_PROCESS = 2,   // high-resolution per-process clock
        SYSTEM_TIME_THREAD = 3,    // high-resolution per-thread clock
        SYSTEM_TIME_BOOTTIME = 4   // same as SYSTEM_TIME_MONOTONIC, but including CPU suspend time
    };

    // return the system-time according to the specified clock
    nsecs_t systemTime(int clock = SYSTEM_TIME_MONOTONIC);

    /**
     * Returns the number of milliseconds to wait between the reference time and the timeout time.
     * If the timeout is in the past relative to the reference time, returns 0.
     * If the timeout is more than INT_MAX milliseconds in the future relative to the reference time,
     * such as when timeoutTime == LLONG_MAX, returns -1 to indicate an infinite timeout delay.
     * Otherwise, returns the difference between the reference time and timeout time
     * rounded up to the next millisecond.
     */
    int toMillisecondTimeoutDelay(nsecs_t referenceTime, nsecs_t timeoutTime);


    /*
     * Time the duration of something.
     *
     * Includes some timeval manipulation functions.
     */
    class DurationTimer {
    public:
        DurationTimer() {}
        ~DurationTimer() {}

        // Start the timer.
        void start();
        // Stop the timer.
        void stop();
        // Get the duration in microseconds.
        long long durationUsecs() const;

        // Subtract two timevals.  Returns the difference (ptv1-ptv2) in
        // microseconds.
        static long long subtractTimevals(const struct timeval* ptv1,
            const struct timeval* ptv2);

        // Add the specified amount of time to the timeval.
        static void addToTimeval(struct timeval* ptv, long usec);

    private:
        struct timeval  mStartWhen;
        struct timeval  mStopWhen;
    };
}

#endif
