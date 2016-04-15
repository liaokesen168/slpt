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


#ifndef JAVACLASS_H
#define JAVACLASS_H


#include <string>

#include <iwds.h>
#include <utils/mutex.h>


class JavaApiContext
{
public:
    virtual ~JavaApiContext()
    {

    }

    JavaApiContext() :
        m_errorStringLock(),
        m_errorString("")
    {

    }

    std::string errorString() const
    {
        Iwds::Mutex::Autolock l(&m_errorStringLock);

        return m_errorString;
    }

    void setErrorString(const std::string &errorString)
    {
        Iwds::Mutex::Autolock l(&m_errorStringLock);

        m_errorString = errorString;
    }

private:
    mutable Iwds::Mutex m_errorStringLock;
    std::string m_errorString;
};


class JavaClass
{
public:
    virtual ~JavaClass()
    {

    }

    virtual bool isAttached(JavaApiContext *context) const = 0;
};


#endif
