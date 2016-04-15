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

#ifndef SAFEPARCEL_H
#define SAFEPARCEL_H

#include <sys/types.h>
#include <utils/errors.h>

using Iwds::status_t;

namespace Iwds
{
    class SafeParcel
    {
    public:
        SafeParcel();
        virtual ~SafeParcel();

        const uint8_t* data() const;
        size_t dataSize() const;
        size_t dataAvail() const;
        size_t dataPosition() const;
        size_t dataCapacity() const;

        status_t setDataSize(size_t size);
        void setDataPosition(size_t pos) const;
        status_t setDataCapacity(size_t capacity);

        status_t setData(const uint8_t* buffer, size_t len);

        void freeData();

        status_t errorCheck() const;
        void setError(status_t error);

        status_t write(const void* data, size_t len);
        void* writeInplace(size_t len);
        status_t writeUnpadded(const void* data, size_t len);
        status_t writeInt32(int32_t val);
        status_t writeInt64(int64_t val);
        status_t writeFloat(float val);
        status_t writeDouble(double val);
        status_t writeCString(const char* str);
        status_t writeString16(const char16_t* str, size_t len);
        status_t writeInt32Array(size_t len, const int32_t *val);
        status_t writeByteArray(size_t len, const uint8_t *val);

        status_t read(void* outData, size_t len) const;
        const void* readInplace(size_t len) const;
        int32_t readInt32() const;
        status_t readInt32(int32_t *pArg) const;
        int64_t readInt64() const;
        status_t readInt64(int64_t *pArg) const;
        float readFloat() const;
        status_t readFloat(float *pArg) const;
        double readDouble() const;
        status_t readDouble(double *pArg) const;
        const char* readCString() const;
        const char16_t* readString16Inplace(size_t* outLen) const;

    private:
        SafeParcel(const SafeParcel&);
        SafeParcel& operator=(const SafeParcel&);

        void initState();
        void freeDataNoInit();

        status_t finishWrite(size_t len);
        status_t growData(size_t len);
        status_t continueWrite(size_t desired);
        status_t restartWrite(size_t desired);

        template<class T>
        status_t readAligned(T *pArg) const;

        template<class T> T readAligned() const;

        template<typename T>
        status_t writeAligned(T val);

        status_t m_error;
        uint8_t* m_data;
        size_t m_dataSize;
        size_t m_dataCapacity;
        mutable size_t m_dataPos;
    };
}

#endif //SAFEPARCEL_H
