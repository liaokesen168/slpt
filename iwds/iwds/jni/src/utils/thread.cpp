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


#include <unistd.h>
#include <errno.h>

#include <utils/assert.h>
#include <utils/thread.h>

static const std::string LOG_TAG("Thread");

namespace Iwds
{
    Thread::Thread():
        m_tid(Handle(-1)),
        m_lock(),
        m_exitPending(false),
        m_running(false),
        m_errorStringLock(),
        m_errorString(std::string("no error"))
    {

    }

    Thread::~Thread()
    {
        Mutex::Autolock l(&m_lock);

        if (m_exitPending) {
            m_lock.unlock();
            wait();
            m_lock.lock();
        }

        Assert::dieIf(m_running, "Thread: Destroyed while"
                " thread is still running");
    }

    bool Thread::readyToRun()
    {
        return true;
    }

    void Thread::atExit()
    {

    }

    bool Thread::createThread()
    {
        pthread_attr_t attr;
        pthread_attr_init(&attr);
        pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

        int error = pthread_create((pthread_t *)(&m_tid),
                                    &attr, internalThreadLoop, this);

        pthread_attr_destroy(&attr);

        if (error) {
            setErrorString(strerror(errno));
            return false;
        }

        return true;
    }

    bool Thread::start()
    {
        Mutex::Autolock l(&m_lock);

        if (m_running) {
            setErrorString("Thread already started");
            return false;
        }

        m_exitPending = false;
        m_tid = Handle(-1);
        m_running = true;

        if (!createThread()) {
            m_running = false;
            m_tid = Handle(-1);

            return false;
        }

        return true;
    }

    void *Thread::internalThreadLoop(void *user)
    {
        Thread *self = reinterpret_cast<Thread *>(user);

        bool first = true;

        for (;;) {
            bool result;

            if (first) {
                first = false;

                result = self->readyToRun();
                if (!result)
                    Log::e(LOG_TAG, "Thread is not ready to run");

                if (result && !self->exitPending())
                    result = self->run();
            } else {
                result = self->run();
            }

            // establish a scope for m_lock
            {
                Mutex::Autolock l(&self->m_lock);
                if (result == false || self->m_exitPending) {
                    self->atExit();
                    self->m_exitPending = true;
                    self->m_running = false;
                    self->m_tid = Handle(-1);

                    self->m_threadExitedCondition.broadcast();

                    break;
                }
            }
        }

        return (void *) 0;
    }

    void Thread::requestExit()
    {
        Mutex::Autolock l(&m_lock);

        m_exitPending = true;
    }

    bool Thread::requestExitAndWait()
    {
        Mutex::Autolock l(&m_lock);

        Assert::dieIf(m_tid == Handle(pthread_self()), "Would block");

        m_exitPending = true;

        while (m_running) {
            if (!m_threadExitedCondition.wait(&m_lock)) {
                setErrorString(strerror(errno));

                return false;
            }
        }

        m_exitPending = false;

        return true;
    }

    bool Thread::wait(unsigned long ms)
    {
        Mutex::Autolock l(&m_lock);

        Assert::dieIf(m_tid == Handle(pthread_self()), "Thread: Would block");

        while (m_running) {
            if (!m_threadExitedCondition.wait(&m_lock, ms)) {
                setErrorString(strerror(errno));

                return false;
            }
        }

        return true;
    }

    timespec Thread::makeTimespec(time_t secs, long nsecs)
    {
        struct timespec ts;
        ts.tv_sec = secs;
        ts.tv_nsec = nsecs;

        return ts;
    }

    void Thread::yieldCurrentThread()
    {
        sched_yield();
    }

    Handle Thread::currentThreadId()
    {
        return Handle(pthread_self());
    }

    void Thread::sleep(unsigned long s)
    {
        nanosleep(makeTimespec(s, 0));
    }

    void Thread::msleep(unsigned long ms)
    {
        nanosleep(makeTimespec(ms / 1000, ms % 1000 * 1000 * 1000));
    }

    void Thread::usleep(unsigned long us)
    {
        nanosleep(makeTimespec(us / 1000 / 1000, us % (1000 * 1000) * 1000));
    }

    bool Thread::isRunning() const
    {
        Mutex::Autolock l(&m_lock);

        return m_running;
    }

    bool Thread::exitPending() const
    {
        Mutex::Autolock l(&m_lock);

        return m_exitPending;
    }

    std::string Thread::errorString() const
    {
        Mutex::Autolock l(&m_errorStringLock);

        return m_errorString;
    }

    void Thread::setErrorString(const std::string &errorString)
    {
        Mutex::Autolock l(&m_errorStringLock);

        m_errorString = errorString;
    }
}
