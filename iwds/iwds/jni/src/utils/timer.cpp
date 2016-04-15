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


#include <utils/timer.h>


namespace Iwds
{
    nsecs_t systemTime(int clock)
    {
        static const clockid_t clocks[] = {
                CLOCK_REALTIME,
                CLOCK_MONOTONIC,
                CLOCK_PROCESS_CPUTIME_ID,
                CLOCK_THREAD_CPUTIME_ID,
                CLOCK_BOOTTIME
        };

        struct timespec t;
        t.tv_sec = t.tv_nsec = 0;
        clock_gettime(clocks[clock], &t);

        return nsecs_t(t.tv_sec) * 1000000000LL + t.tv_nsec;
    }

    int toMillisecondTimeoutDelay(nsecs_t referenceTime, nsecs_t timeoutTime)
    {
        int timeoutDelayMillis;
        if (timeoutTime > referenceTime) {
            uint64_t timeoutDelay = uint64_t(timeoutTime - referenceTime);
            if (timeoutDelay > uint64_t((INT_MAX - 1) * 1000000LL)) {
                timeoutDelayMillis = -1;
            } else {
                timeoutDelayMillis = (timeoutDelay + 999999LL) / 1000000LL;
            }
        } else {
            timeoutDelayMillis = 0;
        }
        return timeoutDelayMillis;
    }

    /*
     * This is our Thread class
     */

    // Start the timer.
    void DurationTimer::start(void)
    {
        gettimeofday(&mStartWhen, NULL);
    }

    // Stop the timer.
    void DurationTimer::stop(void)
    {
        gettimeofday(&mStopWhen, NULL);
    }

    // Get the duration in microseconds.
    long long DurationTimer::durationUsecs(void) const
    {
        return (long long) subtractTimevals(&mStopWhen, &mStartWhen);
    }

    // Subtract two timevals.  Returns the difference (ptv1-ptv2) in
    // microseconds.
    long long DurationTimer::subtractTimevals(const struct timeval* ptv1,
        const struct timeval* ptv2)
    {
        long long stop  = ((long long) ptv1->tv_sec) * 1000000LL +
                          ((long long) ptv1->tv_usec);
        long long start = ((long long) ptv2->tv_sec) * 1000000LL +
                          ((long long) ptv2->tv_usec);
        return stop - start;
    }

    // Add the specified amount of time to the timeval.
    void DurationTimer::addToTimeval(struct timeval* ptv, long usec)
    {
        Assert::dieIf(usec < 0, "Negative values not supported in addToTimeval");

        // normalize tv_usec if necessary
        if (ptv->tv_usec >= 1000000) {
            ptv->tv_sec += ptv->tv_usec / 1000000;
            ptv->tv_usec %= 1000000;
        }

        ptv->tv_usec += usec % 1000000;
        if (ptv->tv_usec >= 1000000) {
            ptv->tv_usec -= 1000000;
            ptv->tv_sec++;
        }
        ptv->tv_sec += usec / 1000000;
    }
}
