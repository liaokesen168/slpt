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

#ifndef CONDITION_H
#define CONDITION_H

#include <stdint.h>
#include <sys/types.h>
#include <time.h>

#include <limits.h>

#include <pthread.h>

#include <utils/timer.h>
#include <utils/mutex.h>

namespace Iwds
{
    /*
     * Condition variable class.  The implementation is system-dependent.
     *
     * Condition variables are paired up with mutexes.  Lock the mutex,
     * call wait(), then either re-wait() if things aren't quite what you want,
     * or unlock the mutex and continue.  All threads calling wait() must
     * use the same mutex for a given Condition.
     */
    class Condition {
    public:
        enum {
            PRIVATE = 0,
            SHARED = 1
        };

        enum WakeUpType
        {
            WAKE_UP_ONE = 0,
            WAKE_UP_ALL = 1
        };

        Condition()
        {
            pthread_cond_init(&m_cond, NULL);
        }

        Condition(int type)
        {
            if (type == SHARED) {
                pthread_condattr_t attr;
                pthread_condattr_init(&attr);
                pthread_condattr_setpshared(&attr, PTHREAD_PROCESS_SHARED);
                pthread_cond_init(&m_cond, &attr);
                pthread_condattr_destroy(&attr);
            } else {
                pthread_cond_init(&m_cond, NULL);
            }
        }

        ~Condition()
        {
            pthread_cond_destroy(&m_cond);
        }

        bool wait(Mutex *mutex, unsigned long ms = ULONG_MAX)
        {
            if (ms == ULONG_MAX)
                return pthread_cond_wait(&m_cond, &mutex->m_mutex) == 0;
            else
                return waitRelative(mutex, ms);
        }

        // same with relative timeout
        bool waitRelative(Mutex *mutex, unsigned long ms)
        {
            struct timeval tv;
            gettimeofday(&tv, (struct timezone *) NULL);

            struct timespec ts;
            ts.tv_sec = tv.tv_sec + (ms / 1000);
            ts.tv_nsec = (tv.tv_usec + (ms % 1000) * 1000L ) * 1000L;

            return pthread_cond_timedwait(&m_cond, &mutex->m_mutex, &ts) == 0;
        }

        // Signal the condition variable, allowing one thread to continue.
        void signal()
        {
            pthread_cond_signal(&m_cond);
        }

        // Signal the condition variable, allowing one or all threads to continue.
        void signal(WakeUpType type)
        {
            if (type == WAKE_UP_ONE) {
                signal();
            } else {
                broadcast();
            }
        }

        // Signal the condition variable, allowing all threads to continue.
        void broadcast()
        {
            pthread_cond_broadcast(&m_cond);
        }

    private:
        pthread_cond_t m_cond;
    };
}

#endif
