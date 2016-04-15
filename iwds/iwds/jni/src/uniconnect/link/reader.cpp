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


#include <algorithm>

#include <utils/log.h>
#include <utils/assert.h>

#include <uniconnect/drivers/datachannel.h>

#include <uniconnect/package.h>
#include <uniconnect/connection.h>
#include <uniconnect/link.h>

#include "./reader.h"


using std::min;
using std::string;
using std::tr1::shared_ptr;

using Iwds::Mutex;
using Iwds::Thread;
using Iwds::Condition;
using Iwds::Assert;
using Iwds::Log;
using Iwds::ByteArray;
using Iwds::u8;
using Iwds::u16;
using Iwds::u32;

#define LOG_TAG                                                 \
            (string("Uniconnect: Link: Reader: ") +             \
                                m_link->trait()->tag())

Reader::Reader(Link *link) :
    m_link(link),
    m_package(new Package()),
    m_wait(),
    m_lock(),
    m_startupFlag(false),
    m_requestStop(false)
{
    m_package->data = new char[m_link->getMaxPayloadSize()];
}

Reader::~Reader()
{
    IWDS_TRACE;

    delete[] m_package->data;
    delete m_package;
}

bool Reader::start()
{
    Mutex::Autolock l(&m_lock);

    if (!Thread::start()) {
        Log::e(LOG_TAG, errorString());

        return false;
    }

    while (!m_startupFlag) {
        if (!m_wait.wait(&m_lock, 5000)) {
            setErrorString("startup thread timeout for 5s");

            return false;
        }
    }

    m_startupFlag = false;

    return true;
}

void Reader::waitStop()
{
    wait();
}

bool Reader::readyToRun()
{
    if (!m_link->readThreadReadyToRun()) {
        Log::e(LOG_TAG, string("failed to start thread: ") +
                                            m_link->errorString());
        return false;
    }

    /*
     * Start writer
     */
    if (!m_link->startWriter()) {
        Log::e(LOG_TAG, m_link->errorString());

        return false;
    }

    /*
     * Signal started
     */
    {
        Mutex::Autolock l(&m_lock);

        m_startupFlag = true;
        m_wait.signal();
    }

    return true;
}

void Reader::atExit()
{
    /*
     * Set Link state to disconnected
     */
    m_link->setState(LinkState::STATE_DISCONNECTED);

    /*
     * Stop writer
     */
    m_link->stopWriter();

    /*
     * All connection NACK link
     */
    m_package->header.type = PkgType::NACK_LINK;
    m_package->header.port = 0;
    m_package->header.dataLength = 0;

    bool ok = processArrivedPackage(m_package);
    if (!ok) {
        Log::e(LOG_TAG,
                string("Failed to process arrived Package: ") +
                errorString());
    }

    m_link->readThreadAtExit();
}

bool Reader::readPackageHeader(PackageHeader *hdr)
{
    return readFully((char *)hdr, sizeof(PackageHeader));
}

bool Reader::readFully(char *buffer, int size)
{
    int bytesRead = 0;
    int readSoFar = 0;

    while (size > 0) {
        bytesRead = m_link->read(buffer + readSoFar, size);
        if (bytesRead < 0)
            return false;

        size -= bytesRead;
        readSoFar += bytesRead;
    }

    return true;
}

bool Reader::run()
{
    /*
     * Start RX
     */
    for (;;) {
        m_package->header.reset();

        bool ok = readFully(
                (char *)&m_package->header, sizeof(PackageHeader));
        if (!ok) {
            /*
             * Dump when error occur
             */
            Log::e(LOG_TAG, "Failed to read header: ");
            m_package->header.dump();

            /*
             * Link lost
             */
            break;
        }

        int size = m_package->header.dataLength;
        if (!size ||
                m_package->header.type == PkgType::ACK_DATA) {
            /*
             * Package without data
             */
            ok = processArrivedPackage(m_package);
            if (!ok) {
                Log::e(LOG_TAG,
                        string("Failed to process arrived Package: ") +
                        errorString());
            }

            continue;
        }

        ok = readFully(m_package->data, size);
        if (!ok) {
            /*
             * Dump when error occur
             */
            Log::e(LOG_TAG, "Failed to read data: ");
            m_package->header.dump();

            /*
             * Link lost
             */
            break;
        }

        /*
         * Package with data or other control packages
         */
        ok = processArrivedPackage(m_package);
        if (!ok) {
            Log::e(LOG_TAG,
                    string("Failed to process arrived Package: ") +
                    errorString());
        }
    }

    return false;
}

bool Reader::processArrivedPackage(Package *package)
{
    bool ok = m_link->processArrivedPackage(package);
    if (!ok)
        setErrorString(m_link->errorString());

    return ok;
}
