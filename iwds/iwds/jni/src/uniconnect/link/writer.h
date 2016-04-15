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


#ifndef WRITER_H
#define WRITER_H


#include <string>

#include <utils/thread.h>
#include <utils/mutex.h>
#include <utils/condition.h>
#include <utils/protectedqueue.h>
#include <uniconnect/iorequest.h>
#include <uniconnect/package.h>


class Link;

class Writer : public Iwds::Thread
{
public:
    Writer(Link *link);
    ~Writer();

    bool start();
    void stop();

protected:
    bool readyToRun();
    void atExit();
    bool run();

private:
    Link *m_link;

    Iwds::Condition m_wait;
    Iwds::Mutex m_lock;

    bool m_startupFlag;

    bool write(const char *buffer, int size);
    bool flush();

    bool mustStop(const PackageHeader *hdr);
};


#endif
