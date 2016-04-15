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


#ifndef PROTECTED_QUEUE_H
#define PROTECTED_QUEUE_H

#include <queue>
#include <string>

#include <utils/mutex.h>
#include <utils/assert.h>

namespace Iwds
{
    template<typename T>
    class ProtectedQueue
    {
    public:
        typedef typename std::queue<T>::size_type size_type;

        ProtectedQueue() :
            m_queue(),
            m_lock()
        {

        }

        ProtectedQueue(const ProtectedQueue<T> &q) :
            m_queue(q.m_queue),
            m_lock()
        {
            Assert::dieIf(&q == this, std::string("at: ") + __FUNCTION__);
        }

        ProtectedQueue &operator=(const ProtectedQueue<T> &q)
        {
            Assert::dieIf(&q == this, std::string("at: ") + __FUNCTION__);

            lock();
            m_queue = q.m_queue;
            unlock();

            return *this;
        }

        void pushNolock(const T &value)
        {
            m_queue.push(value);
        }

        void push(const T &value)
        {
            Mutex::Autolock l(&m_lock);

            m_queue.push(value);
        }

        void popNolock()
        {
            m_queue.pop();
        }

        void pop()
        {
            Mutex::Autolock l(&m_lock);

            popNolock();
        }

        T frontNolock() const
        {
            return m_queue.front();
        }

        T front() const
        {
            Mutex::Autolock l(&m_lock);

            return frontNolock();
        }

        T takeFrontNolock()
        {
            T value = frontNolock();
            popNolock();

            return value;
        }

        T takeFront()
        {
            Mutex::Autolock l(&m_lock);

            return takeFrontNolock();
        }

        bool emptyNolock() const
        {
            return m_queue.empty();
        }

        bool empty() const
        {
            Mutex::Autolock l(&m_lock);

            return emptyNolock();
        }

        size_type sizeNolock() const
        {
            return m_queue.size();
        }

        size_type size() const
        {
            Mutex::Autolock l(&m_lock);

            return sizeNolock();
        }

        void lock() const
        {
            m_lock.lock();
        }

        void unlock() const
        {
            m_lock.unlock();
        }

        Mutex *locker() const
        {
            return &m_lock;
        }

    private:
        std::queue<T> m_queue;
        mutable Mutex m_lock;
    };
}

#endif
