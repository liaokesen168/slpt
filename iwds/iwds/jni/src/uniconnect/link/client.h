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


#ifndef CLIENT_H
#define CLIENT_H


#include <string>
#include <memory>

#include <utils/thread.h>
#include <utils/mutex.h>
#include <utils/condition.h>
#include <utils/assert.h>
#include <utils/log.h>


class Link;

class Client : public Iwds::Thread
{
public:
    Client(Link *link);
    ~Client();

    bool start();

protected:
    bool readyToRun();
    void atExit();
    bool run();

private:
    Link *m_link;

    Iwds::Condition m_wait;
    Iwds::Mutex m_lock;

    bool m_startupFlag;
};

#endif
