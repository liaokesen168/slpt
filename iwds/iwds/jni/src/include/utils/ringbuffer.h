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


#ifndef RINGBUFFER_H
#define RINGBUFFER_H


#include <tr1/memory>
#include <algorithm>

#include <iwds.h>

#include <utils/mutex.h>
#include <utils/assert.h>


namespace Iwds
{
    class RingBuffer
    {
    public:
        RingBuffer(int size) :
            m_head(0),
            m_size(0),
            m_buffer(size),
            m_totalSize(size)
        {
            m_buffer.shrink_to_fit();
        }

        void put(std::tr1::shared_ptr<Iwds::ByteArray> buffer)
        {
            return put(buffer->data(), buffer->size());
        }

        void put(const char *buffer, int size)
        {
            if (size > m_totalSize) {
                /*
                 * Bad case: realloc to fit new big data
                 */
                m_buffer.resize(size);
                m_buffer.shrink_to_fit();

                m_totalSize = size;
                m_size = size;
                m_head = 0;
                for (int i = 0; i < m_totalSize; i++)
                    m_buffer[i] = *buffer++;

                return;
            }

            if (size == m_totalSize) {
                m_head = 0;
                m_size = size;
                for (int i = 0; i < m_totalSize; i++)
                    m_buffer[i] = *buffer++;

                return;
            }

            int tail = (m_head + m_size) % m_totalSize;
            int first = std::min(m_totalSize - tail, size);
            for (int i = 0; i < first; i++)
                m_buffer[tail++] = *buffer++;

            if (first < size) {
                int second = size - first;
                for (tail = 0; tail < second; tail++)
                    m_buffer[tail] = *buffer++;
            }

            if (m_totalSize - m_size < size)
                m_head = tail;

            m_size = std::min(m_size + size, m_totalSize);
        }

        void get(std::tr1::shared_ptr<Iwds::ByteArray> buffer,
                                                    int size)
        {
            Iwds::Assert::dieIf(
                        size > m_size, "exceed available bytes size");

            if (buffer->capacity() < (ByteArray::size_type)size) {
                buffer->resize(size);
                buffer->shrink_to_fit();
            }

            get(buffer->data(), size);
        }

        void get(char *buffer, int size)
        {
            Iwds::Assert::dieIf(
                        size > m_size, "exceed available bytes size");

            int first = std::min(m_totalSize - m_head, size);
            for (int i = 0; i < first; i++)
                *buffer++ = m_buffer[m_head++];

            if (first < size) {
                int second = size - first;
                for (m_head = 0; m_head < second; m_head++)
                    *buffer++ = m_buffer[m_head];
            }

            m_size -= size;
        }

        void clear()
        {
            m_head = 0;
            m_size = 0;
            m_buffer.clear();
        }

        int size() const
        {
            return m_size;
        }

        bool empty() const
        {
            return m_size == 0;
        }

        bool full() const
        {
            return m_totalSize == m_size;
        }

        int capacity() const
        {
            return m_totalSize;
        }

        int availableSize() const
        {
            return m_totalSize - m_size;
        }

    private:
        int m_head;
        int m_size;

        Iwds::ByteArray m_buffer;
        int m_totalSize;
    };

}

#endif
