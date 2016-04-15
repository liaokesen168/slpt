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

#ifndef MUTEX_H
#define MUTEX_H

#include <stdint.h>
#include <sys/types.h>
#include <time.h>

#include <string>

#include <pthread.h>

namespace Iwds
{
    class Condition;

    /*
     * Simple mutex class.  The implementation is system-dependent.
     *
     * The mutex must be unlocked by the thread that locked it.  They are not
     * recursive, i.e. the same thread can't lock it multiple times.
     */
    class Mutex
    {
    public:
        enum
        {
            PRIVATE = 0, SHARED = 1
        };

        Mutex()
        {
            pthread_mutex_init(&m_mutex, NULL);
        }

        Mutex(__attribute__((unused)) const std::string &name)
        {
            Mutex(name.c_str());
        }

        Mutex(int type, __attribute__((unused)) const std::string &name)
        {
            Mutex(type, name.c_str());
        }

        Mutex(__attribute__((unused)) const char *name)
        {
            pthread_mutex_init(&m_mutex, NULL);
        }

        Mutex(int type, __attribute__((unused)) const char *name)
        {
            if (type == SHARED)
            {
                pthread_mutexattr_t attr;
                pthread_mutexattr_init(&attr);
                pthread_mutexattr_setpshared(&attr, PTHREAD_PROCESS_SHARED);
                pthread_mutex_init(&m_mutex, &attr);
                pthread_mutexattr_destroy(&attr);
            } else {
                pthread_mutex_init(&m_mutex, NULL);
            }
        }

        ~Mutex()
        {
            pthread_mutex_destroy(&m_mutex);
        }

        // lock or unlock the mutex
        bool lock()
        {
            return pthread_mutex_lock(&m_mutex) == 0;
        }

        void unlock()
        {
            pthread_mutex_unlock(&m_mutex);
        }

        // lock if possible; returns true on success, false on error otherwise
        bool tryLock()
        {
            return pthread_mutex_trylock(&m_mutex) == 0;
        }

        // Manages the mutex automatically. It'll be locked when Autolock is
        // constructed and released when Autolock goes out of scope.
        class Autolock
        {
        public:
            Autolock(Mutex* mutex) :
                m_lock(mutex)
            {
                m_lock->lock();
            }

            ~Autolock()
            {
                m_lock->unlock();
            }
        private:
            Mutex *m_lock;
        };

    private:
        friend class Condition;

        // A mutex cannot be copied
        Mutex(const Mutex &mutex);
        Mutex &operator =(const Mutex &mutex);

        pthread_mutex_t m_mutex;
    };

    /*
     * Automatic mutex.  Declare one of these at the top of a function.
     * When the function returns, it will go out of scope, and release the
     * mutex.
     */
    typedef Mutex::Autolock AutoMutex;
}

#endif
