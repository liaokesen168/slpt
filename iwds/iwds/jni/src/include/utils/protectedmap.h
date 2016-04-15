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


#ifndef PROTECTED_MAP_H
#define PROTECTED_MAP_H

#include <map>
#include <utility>

#include <utils/mutex.h>
#include <utils/assert.h>

namespace Iwds
{
    template<typename Key, typename T>
    class ProtectedMap
    {
    public:
        typedef typename std::map<Key, T>::iterator iterator;
        typedef typename std::map<Key, T>::const_iterator const_iterator;
        typedef typename std::pair<iterator, bool> ReturnPair;
        typedef typename std::map<Key, T>::value_type value_type;

        ProtectedMap() :
            m_map(),
            m_lock()
        {

        }

        ProtectedMap(const ProtectedMap<Key, T> &m) :
            m_map(m.m_map),
            m_lock()
        {
            Assert::dieIf(&m == this, std::string("at: ") + __FUNCTION__);
        }

        ProtectedMap &operator=(const ProtectedMap<Key, T> &m)
        {
            Assert::dieIf(&m == this, std::string("at: ") + __FUNCTION__);

            lock();
            m_map = m.m_map;
            unlock();

            return *this;
        }

        T valueNoLock(const Key &key, const T &defaultValue = T()) const
        {
            const_iterator it = m_map.find(key);
            if (it == m_map.end())
                return defaultValue;

            return it->second;
        }

        T value(const Key &key, const T &defaultValue = T()) const
        {
            lock();
            T val = valueNoLock(key, defaultValue);
            unlock();

            return val;
        }

        bool removeOneNolock(const Key &key)
        {
            iterator it = m_map.find(key);

            if (it == m_map.end())
                return false;

            m_map.erase(it);

            return true;
        }

        bool removeOneNolock(const T &t)
        {
            for (iterator it = m_map.begin(); it != m_map.end(); it++) {
                if (it->second == t) {
                    m_map.erase(it);

                    return true;
                }
            }

            return false;
        }

        bool removeOne(const Key &key)
        {
            lock();
            bool success = removeOneNolock(key);
            unlock();

            return success;
        }

        bool removeOne(const T &t)
        {
            lock();
            bool success = removeOneNolock(t);
            unlock();

            return success;
        }

        void clearNolock()
        {
            iterator it = m_map.begin();
            while(it != m_map.end())
                it = m_map.erase(it);
        }

        void clear()
        {
            Mutex::Autolock l(&m_lock);

            iterator it = m_map.begin();
            while(it != m_map.end())
                it = m_map.erase(it);
        }

        ReturnPair insertNolock(const Key &key, const T &value)
        {
            return m_map.insert(std::make_pair(key, value));
        }

        ReturnPair insert(const Key &key, const T &value)
        {
            lock();
            ReturnPair pair = insertNolock(key, value);
            unlock();

            return pair;
        }

        void lock() const
        {
            m_lock.lock();
        }

        void unlock() const
        {
            m_lock.unlock();
        }

        const std::map<Key, T> *data() const
        {
            return &m_map;
        }

        std::map<Key, T> *data()
        {
            return &m_map;
        }

        Mutex *locker() const
        {
            return &m_lock;
        }

    private:
        std::map<Key, T> m_map;

        mutable Mutex m_lock;
    };
}

#endif
