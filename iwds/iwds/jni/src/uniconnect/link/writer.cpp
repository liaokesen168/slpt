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
#include <uniconnect/link.h>

#include "./writer.h"

using std::string;
using std::tr1::shared_ptr;

using Iwds::Mutex;
using Iwds::Thread;
using Iwds::Condition;
using Iwds::Assert;
using Iwds::Log;
using std::min;
using Iwds::u32;
using Iwds::u16;
using Iwds::u8;

#define LOG_TAG                                                 \
            (string("Uniconnect: Link: Writer: ") +             \
                                m_link->trait()->tag())

Writer::Writer(Link *link) :
    m_link(link),
    m_wait(),
    m_lock(),
    m_startupFlag(false)
{

}

Writer::~Writer()
{

}

bool Writer::start()
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

void Writer::stop()
{
    shared_ptr<IORequest> stopReq(
            IORequest::createIORequest(
                    Package::createPackage(0, PkgType::NACK_LINK)));

    m_link->enqueueIORequest(stopReq);

    wait();
}

bool Writer::readyToRun()
{
    if (!m_link->writeThreadReadyToRun()) {
        Log::e(LOG_TAG,string("failed to start thread: ") +
                                            m_link->errorString());
        return false;
    }

    {
        Mutex::Autolock l(&m_lock);

        m_startupFlag = true;
        m_wait.signal();
    }

    return true;
}

void Writer::atExit()
{
    m_link->writeThreadAtExit();
}

bool Writer::mustStop(const PackageHeader *hdr)
{
    return hdr->type == PkgType::NACK_LINK;
}

bool Writer::run()
{
    for (;;) {
        /*
         * Dequeue request, control request first
         */
        shared_ptr<IORequest> request = m_link->waitIORequest();

        /*
         * Request locked area
         */
        Mutex::Autolock l(request->locker());

        shared_ptr<Package> pkg = request->getPackage();
        PackageHeader *hdr = &pkg->header;

        if (mustStop(hdr)) {
            /*
             * Quit now if stop requested
             */
            break;
        }

        /*
         * Prepare IO transfer
         */
        char *buffer = (char *)hdr;
        int hdrSize = sizeof(struct PackageHeader);
        int dataLength = hdr->dataLength;

        int transferSize = hdrSize;

        if (dataLength &&
                hdr->type != PkgType::ACK_DATA) {
            /*
             * Combined transfer if with pay-load
             */
            buffer = const_cast<char *>(pkg->data) - hdrSize;

            memmove(buffer, hdr, hdrSize);

            transferSize += dataLength;
        }

        /*
         * Do IO transfer
         */
        bool writeOK = false;
        bool syncOK = true;

        writeOK = write(buffer, transferSize);
        if (writeOK && request->isNeedSync()) {
            /*
             * Synchronization needed, so do flush
             */
            syncOK = m_link->flush();
        }

        if (!writeOK) {
            /*
             * Dump header when error occur
             */
            Log::e(LOG_TAG, "Failed to write package: ");
            hdr->dump();
        }

        if (request->isWaitSend()) {
            /*
             * Wake up sender
             */
            request->complete(writeOK && syncOK ?
                                UniconnectErrorCode::ENOERROR :
                                UniconnectErrorCode::ELINKDISCONNECTED);
        }

        /*
         * Debug stuff
         */
        if (Log::isDebugEnabled()) {
            Log::d(string(LOG_TAG).c_str(),
                    "Data Path: Package: %s,"
                    " Data length: %d --------------> Port: %u,"
                    " Send Result: %s",
                    PackageHeader::typeToString(hdr->type),
                    hdr->dataLength,
                    hdr->port,
                    writeOK && syncOK ? "Success." : "Failed.");
        }
    }

    return false;
}

bool Writer::write(const char *buffer, int size)
{
    return m_link->write(buffer, size);
}

bool Writer::flush()
{
    return m_link->flush();
}
