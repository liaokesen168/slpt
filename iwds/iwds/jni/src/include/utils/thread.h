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


#ifndef THREAD_H
#define THREAD_H

#include <sched.h>
#include <pthread.h>

#include <string>

#include <iwds.h>

#include <utils/timer.h>
#include <utils/mutex.h>
#include <utils/condition.h>
#include <utils/log.h>
#include <utils/assert.h>

namespace Iwds
{
    class Thread
    {
    public:
        // Create a Thread object, but doesn't create or start the associated
        // thread. See the run() method.
        Thread();
        virtual ~Thread();

        // Start the thread in threadLoop() which needs to be implemented.
        virtual bool start();

        // Ask this object's thread to exit. This function is asynchronous, when the
        // function returns the thread might still be running. Of course, this
        // function can be called from a different thread.
        virtual void requestExit();

        // Call requestExit() and wait until this object's thread exits.
        // BE VERY CAREFUL of deadlocks. In particular, it would be silly to call
        // this function from this object's thread. Will dead by WOULD_BLOCK in
        // that case.
        bool requestExitAndWait();

        // Indicates whether this thread is running or not.
        bool isRunning() const;

        std::string errorString() const;

        bool wait(unsigned long ms = ULONG_MAX);

        static void sleep(unsigned long s);
        static void msleep(unsigned long ms);
        static void usleep(unsigned long us);

        static void yieldCurrentThread();
        static Handle currentThreadId();

    protected:
        // exitPending() returns true if requestExit() has been called.
        bool exitPending() const;

        void setErrorString(const std::string &errorString);

        // Good place to do one-time initializations
        virtual bool readyToRun();

        // Good place to do some cleanup works when thread exit
        virtual void atExit();

        // Derived class must implement threadLoop(). The thread starts its life
        // here. There are two ways of using the Thread object:
        // 1) loop: if threadLoop() returns true, it will be called again if
        //          requestExit() wasn't called.
        // 2) once: if threadLoop() returns false, the thread will exit upon return.
        virtual bool run() = 0;

    private:
        static timespec makeTimespec(time_t secs, long nsecs);

        bool createThread();
        static void *internalThreadLoop(void *user);

        // always hold m_lock when reading or writing
        Handle m_tid;
        mutable Mutex m_lock;
        Condition m_threadExitedCondition;

        // note that all accesses of m_exitPending and m_running need to hold mLock
        volatile bool m_exitPending;
        volatile bool m_running;

        mutable Mutex m_errorStringLock;
        std::string m_errorString;
    };
}

#endif
