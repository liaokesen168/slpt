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


#ifndef PROTECTED_LIST_H
#define PROTECTED_LIST_H

#include <list>

#include <utils/assert.h>
#include <utils/mutex.h>

namespace Iwds
{
    template<typename T>
    class ProtectedList
    {
    public:
        typedef typename std::list<T>::iterator iterator;
        typedef typename std::list<T>::const_iterator const_iterator;
        typedef typename std::list<T>::size_type size_type;

        ProtectedList<T>() :
            m_lock(),
            m_list()
        {

        }

        ProtectedList<T>(const ProtectedList<T> &l) :
            m_lock(),
            m_list(l.m_list)
        {
            Assert::dieIf(&l == this, std::string("at: ") + __FUNCTION__);
        }

        T &operator=(const ProtectedList<T> &l)
        {
            Assert::dieIf(&l == this, std::string("at: ") + __FUNCTION__);

            lock();
            m_list = l.m_list;
            unlock();

            return *this;
        }

        void appendNolock(const T &t)
        {
            m_list.push_back(t);
        }

        void append(const T &t)
        {
            Mutex::Autolock l(&m_lock);

            m_list.push_back(t);
        }

        void pushFrontNolock(const T &t)
        {
            m_list.push_front(t);
        }

        void pushFront(const T &t)
        {
            Mutex::Autolock l(&m_lock);

            m_list.push_front(t);
        }

        T frontNolock() const
        {
            return m_list.front();
        }

        T front() const
        {
            Mutex::Autolock l(&m_lock);

            return frontNolock();
        }

        T takeFrontNolock()
        {
            T value = frontNolock();
            m_list.pop_front();

            return value;
        }

        T takeFront()
        {
            Mutex::Autolock l(&m_lock);

            return takeFrontNolock();
        }

        size_type sizeNolock() const
        {
            return m_list.size();
        }

        size_type size() const
        {
            Mutex::Autolock l(&m_lock);

            return sizeNolock();

        }

        bool emptyNolock() const
        {
            return m_list.empty();
        }

        bool empty() const
        {
            Mutex::Autolock l(&m_lock);

            return emptyNolock();
        }

        void clearNolock()
        {
            iterator it = m_list.begin();
            while(it != m_list.end())
                it = m_list.erase(it);
        }

        void clear()
        {
            Mutex::Autolock l(&m_lock);

            iterator it = m_list.begin();
            while(it != m_list.end())
                it = m_list.erase(it);
        }

        bool erase(const T &t)
        {
            Mutex::Autolock l(&m_lock);

            return eraseNolock(t);
        }

        bool eraseNolock(const T& t)
        {
            for (iterator it = m_list.begin();
                        it != m_list.end(); it++) {
                if (*it == t) {
                    m_list.erase(it);

                    return true;
                }
            }

            return false;
        }

        std::list<T> *data()
        {
            return &m_list;
        }

        const std::list<T> *data() const
        {
            return &m_list;
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
        mutable Iwds::Mutex m_lock;

        std::list<T> m_list;
    };
}


#endif
