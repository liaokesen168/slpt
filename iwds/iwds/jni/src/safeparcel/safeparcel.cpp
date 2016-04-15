/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

#include <string>
#include <safeparcel/safeparcel.h>
#include <stdlib.h>

#ifndef INT32_MAX
#define INT32_MAX ((int32_t)(2147483647))
#endif

#define PAD_SIZE(s) (((s)+3) & ~3)

using std::string;

static const string LOG_TAG("SafeParcel: ");

namespace Iwds
{
    SafeParcel::SafeParcel()
    {
        initState();
    }

    SafeParcel::~SafeParcel()
    {
        freeDataNoInit();
    }

    void SafeParcel::initState()
    {
        m_error = NO_ERROR;
        m_data = 0;
        m_dataSize = 0;
        m_dataCapacity = 0;
        m_dataPos = 0;
    }

    void SafeParcel::freeDataNoInit()
    {
        if (m_data)
            free(m_data);
    }

    const uint8_t* SafeParcel::data() const
    {
        return m_data;
    }

    size_t SafeParcel::dataSize() const
    {
        return (m_dataSize > m_dataPos ? m_dataSize : m_dataPos);
    }

    size_t SafeParcel::dataAvail() const
    {
        return dataSize() - dataPosition();
    }

    size_t SafeParcel::dataPosition() const
    {
        return m_dataPos;
    }

    size_t SafeParcel::dataCapacity() const
    {
        return m_dataCapacity;
    }

    status_t SafeParcel::setDataSize(size_t size)
    {
        status_t error;
        error = continueWrite(size);

        if (error == NO_ERROR)
            m_dataSize = size;

        return error;
    }

    void SafeParcel::setDataPosition(size_t pos) const
    {
        m_dataPos = pos;
    }

    status_t SafeParcel::setDataCapacity(size_t size)
    {
        if (size > m_dataCapacity)
            return continueWrite(size);

        return NO_ERROR;
    }

    status_t SafeParcel::setData(const uint8_t* buffer, size_t len)
    {
        status_t error = restartWrite(len);

        if (error == NO_ERROR) {
            memcpy(const_cast<uint8_t*>(data()), buffer, len);
            m_dataSize = len;
        }

        return error;
    }

    void SafeParcel::freeData()
    {
        freeDataNoInit();
        initState();
    }

    status_t SafeParcel::errorCheck() const
    {
        return m_error;
    }

    void SafeParcel::setError(status_t error)
    {
        m_error = error;
    }

    status_t SafeParcel::continueWrite(size_t desired)
    {
        if (m_data) {
            if (desired > m_dataCapacity) {
                uint8_t* data = (uint8_t*) realloc(m_data, desired);
                if (data) {
                    m_data = data;
                    m_dataCapacity = desired;
                } else if (desired > m_dataCapacity) {
                    m_error = NO_MEMORY;
                    return NO_MEMORY;
                }

            } else {
                if (m_dataSize > desired)
                    m_dataSize = desired;

                if (m_dataPos > desired)
                    m_dataPos = desired;
            }

        } else {
            uint8_t* data = (uint8_t*) malloc(desired);
            if (!data) {
                m_error = NO_MEMORY;
                return NO_MEMORY;
            }

            m_data = data;
            m_dataSize = m_dataPos = 0;
            m_dataCapacity = desired;
        }

        return NO_ERROR;
    }

    status_t SafeParcel::finishWrite(size_t len)
    {
        m_dataPos += len;

        if (m_dataPos > m_dataSize)
            m_dataSize = m_dataPos;

        return NO_ERROR;
    }

    status_t SafeParcel::restartWrite(size_t desired)
    {
        uint8_t* data = (uint8_t*) realloc(m_data, desired);
        if (!data && desired > m_dataCapacity) {
            m_error = NO_MEMORY;
            return NO_MEMORY;
        }

        if (data) {
            m_data = data;
            m_dataCapacity = desired;
        }

        m_dataSize = m_dataPos = 0;

        return NO_ERROR;
    }

    status_t SafeParcel::growData(size_t len)
    {
        size_t newSize = ((m_dataSize + len) * 3) / 2;

        return (newSize <= m_dataSize) ? (status_t) NO_MEMORY
                : continueWrite(newSize);
    }

    status_t SafeParcel::writeUnpadded(const void* data, size_t len)
    {
        size_t end = m_dataPos + len;
        if (end < m_dataPos)
            return BAD_VALUE;

        if (end <= m_dataCapacity) {
    restart_write:
            memcpy(m_data + m_dataPos, data, len);
            return finishWrite(len);
        }

        status_t error = growData(len);
        if (error == NO_ERROR)
            goto restart_write;

        return error;
    }

    status_t SafeParcel::write(const void* data, size_t len)
    {
        void* const d = writeInplace(len);
        if (d) {
            memcpy(d, data, len);

            return NO_ERROR;
        }

        return m_error;
    }

    void* SafeParcel::writeInplace(size_t len)
    {
        const size_t padded = PAD_SIZE(len);

        if (m_dataPos + padded < m_dataPos)
            return NULL;

        if ((m_dataPos + padded) <= m_dataCapacity) {
    restart_write:
            uint8_t* const data = m_data + m_dataPos;

            if (padded != len) {
    #if BYTE_ORDER == BIG_ENDIAN
                static const uint32_t mask[4] = { 0x00000000, 0xffffff00,
                        0xffff0000, 0xff000000 };
    #elif BYTE_ORDER == LITTLE_ENDIAN
                static const uint32_t mask[4] = {0x00000000, 0x00ffffff,
                    0x0000ffff, 0x000000ff};
    #endif

                *reinterpret_cast<uint32_t*>(data + padded - 4) &=
                        mask[padded - len];
            }

            finishWrite(padded);

            return data;
        }

        status_t error = growData(padded);
        if (error == NO_ERROR)
            goto restart_write;

        return NULL;
    }

    status_t SafeParcel::writeInt32(int32_t val)
    {
        return writeAligned(val);
    }

    status_t SafeParcel::writeInt32Array(size_t len, const int32_t *val)
    {
        if (!val)
            return writeAligned(-1);

        status_t error = writeAligned(len);
        if (error == NO_ERROR)
            error = write(val, len * sizeof(*val));

        return error;
    }

    status_t SafeParcel::writeByteArray(size_t len, const uint8_t *val)
    {
        if (!val)
            return writeAligned(-1);

        status_t error = writeAligned(len);
        if (error == NO_ERROR)
            error = write(val, len * sizeof(*val));

        return error;
    }

    status_t SafeParcel::writeInt64(int64_t val)
    {
        return writeAligned(val);
    }

    status_t SafeParcel::writeFloat(float val)
    {
        return writeAligned(val);
    }

    #if defined(__mips__) && defined(__mips_hard_float)

    status_t SafeParcel::writeDouble(double val)
    {
        union {
            double d;
            unsigned long long ll;
        }u;
        u.d = val;
        return writeAligned(u.ll);
    }

    #else

    status_t SafeParcel::writeDouble(double val)
    {
        return writeAligned(val);
    }

    #endif

    status_t SafeParcel::writeCString(const char* str)
    {
        return write(str, strlen(str) + 1);
    }

    status_t SafeParcel::writeString16(const char16_t* str, size_t len)
    {
        if (str == NULL)
            return writeInt32(-1);

        status_t error = writeInt32(len);
        if (error == NO_ERROR) {
            len *= sizeof(char16_t);
            uint8_t* data = (uint8_t*) writeInplace(len + sizeof(char16_t));
            if (data) {
                memcpy(data, str, len);
                *reinterpret_cast<char16_t*>(data + len) = 0;

                return NO_ERROR;
            }

            error = m_error;
        }

        return error;
    }

    template<typename T>
    status_t SafeParcel::writeAligned(T val)
    {
        static_assert(PAD_SIZE(sizeof(T)) == sizeof(T), "");

        if ((m_dataPos + sizeof(val)) <= m_dataCapacity) {
    restart_write:
            *reinterpret_cast<T*>(m_data + m_dataPos) = val;

            return finishWrite(sizeof(val));
        }

        status_t error = growData(sizeof(val));
        if (error == NO_ERROR)
            goto restart_write;

        return error;
    }

    template<class T>
    status_t SafeParcel::readAligned(T *pArg) const
    {
        static_assert(PAD_SIZE(sizeof(T)) == sizeof(T), "");

        if ((m_dataPos + sizeof(T)) <= m_dataSize) {
            const void* data = m_data + m_dataPos;
            m_dataPos += sizeof(T);
            *pArg = *reinterpret_cast<const T*>(data);

            return NO_ERROR;

        } else {
            return NOT_ENOUGH_DATA;
        }
    }

    template<typename T>
    T SafeParcel::readAligned() const
    {
        T result;

        if (readAligned(&result) != NO_ERROR)
            result = 0;

        return result;
    }

    status_t SafeParcel::read(void* outData, size_t len) const
    {
        if ((m_dataPos + PAD_SIZE(len)) >= m_dataPos
                && (m_dataPos + PAD_SIZE(len)) <= m_dataSize) {

            memcpy(outData, m_data + m_dataPos, len);
            m_dataPos += PAD_SIZE(len);

            return NO_ERROR;
        }

        return NOT_ENOUGH_DATA;
    }

    const void* SafeParcel::readInplace(size_t len) const
    {
        if ((m_dataPos + PAD_SIZE(len)) >= m_dataPos
                && (m_dataPos + PAD_SIZE(len)) <= m_dataSize) {
            const void* data = m_data + m_dataPos;
            m_dataPos += PAD_SIZE(len);
            return data;
        }

        return NULL;
    }

    int32_t SafeParcel::readInt32() const
    {
        return readAligned<int32_t>();
    }

    status_t SafeParcel::readInt32(int32_t * pArg) const
    {
        return readAligned(pArg);
    }

    int64_t SafeParcel::readInt64() const
    {
        return readAligned<int64_t>();
    }

    status_t SafeParcel::readInt64(int64_t *pArg) const
    {
        return readAligned(pArg);
    }

    float SafeParcel::readFloat() const
    {
        return readAligned<float>();
    }

    status_t SafeParcel::readFloat(float *pArg) const
    {
        return readAligned(pArg);
    }

    #if defined(__mips__) && defined(__mips_hard_float)

    status_t SafeParcel::readDouble(double *pArg) const
    {
        union {
            double d;
            unsigned long long ll;
        }u = {0};

        status_t status;
        status = readAligned(&u.ll);
        *pArg = u.d;

        return status;
    }

    double SafeParcel::readDouble() const
    {
        union {
            double d;
            unsigned long long ll;
        }u = {0};

        u.ll = readAligned<unsigned long long>();

        return u.d;
    }

    #else

    double SafeParcel::readDouble() const
    {
        return readAligned<double>();
    }

    status_t SafeParcel::readDouble(double* pArg) const
    {
        return readAligned(pArg);
    }

    #endif

    const char* SafeParcel::readCString() const
    {
        const size_t avail = m_dataSize - m_dataPos;

        if (avail > 0) {
            const char* str = reinterpret_cast<const char*>(m_data + m_dataPos);
            const char* eos = reinterpret_cast<const char*>(memchr(str, 0, avail));

            if (eos) {
                const size_t len = eos - str;
                m_dataPos += PAD_SIZE(len + 1);

                return str;
            }
        }

        return NULL;
    }

    const char16_t* SafeParcel::readString16Inplace(size_t* outLen) const
    {
        int32_t size = readInt32();

        if (size >= 0 && size < INT32_MAX) {
            *outLen = size;
            const char16_t* str = (const char16_t*) readInplace(
                    (size + 1) * sizeof(char16_t));

            if (str != NULL)
                return str;

        }

        *outLen = 0;

        return NULL;
    }
}
