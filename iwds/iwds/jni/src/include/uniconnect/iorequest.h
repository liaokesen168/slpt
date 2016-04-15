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


#ifndef IOREQUEST_H
#define IOREQUEST_H


#include <tr1/memory>

#include <iwds.h>

#include <uniconnect/uniconnecterrorcode.h>
#include <uniconnect/package.h>

class IORequest
{
public:
    static std::tr1::shared_ptr<IORequest> createIORequest(
                                std::tr1::shared_ptr<Package> package)
    {
        return std::tr1::shared_ptr<IORequest>(new IORequest(package));
    }

    Iwds::Mutex *locker()
    {
        return &m_lock;
    }

    void lock()
    {
        m_lock.lock();
    }

    void unlock()
    {
        m_lock.unlock();
    }

    void reset()
    {
        m_package->reset();

        m_waitSend = false;
        m_needSync = false;

        m_bytesTransfered = 0;

        m_complete = false;
        m_error = UniconnectErrorCode::ENOERROR;
    }

    bool isWaitSend() const
    {
        return m_waitSend;
    }

    bool isNeedSync() const
    {
        return m_needSync;
    }

    bool isComplete() const
    {
        return m_complete;
    }

    int waitComplete() const
    {
        m_cond.wait(&m_lock);

        return m_error;
    }

    void complete(int error)
    {
        m_complete = true;
        m_error = error;

        m_cond.broadcast();
    }

    void acknowledgeBytes(int nbytes)
    {
        m_bytesTransfered += nbytes;
    }

    int getTransferedBytes() const
    {
        return m_bytesTransfered;
    }

    std::tr1::shared_ptr<Package> getPackage()
    {
        return m_package;
    }

    int getError()
    {
        return m_error;
    }

    /*
     * internal usage
     */
    void setWaitSend(bool enable)
    {
        m_waitSend = enable;
    }

    void setNeedSync(bool enable)
    {
        m_needSync = enable;
    }

    ~IORequest()
    {

    }

private:
    std::tr1::shared_ptr<Package> m_package;

    bool m_waitSend;
    bool m_needSync;

    int m_bytesTransfered;

    bool m_complete;
    int m_error;

    mutable Iwds::Mutex m_lock;
    mutable Iwds::Condition m_cond;

    IORequest(std::tr1::shared_ptr<Package> package) :
        m_package(package),
        m_waitSend(false),
        m_needSync(false),
        m_bytesTransfered(0),
        m_complete(false),
        m_error(UniconnectErrorCode::ENOERROR),
        m_lock(),
        m_cond()
    {

    }
};

#endif
