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

#include <unistd.h>

#include <uniconnect/package.h>
#include <uniconnect/connection.h>
#include <uniconnect/connectionmanager.h>
#include <uniconnect/link.h>
#include <uniconnect/iorequest.h>
#include <uniconnect/uniconnecterrorcode.h>

using std::string;
using std::tr1::shared_ptr;

using Iwds::ProtectedMap;
using Iwds::Assert;
using Iwds::Mutex;
using Iwds::Log;
using Iwds::no_port_t;

static const string LOG_TAG("Uniconnect: ConnectionManager: ");

ConnectionManager::ConnectionManager(Link *link) :
    m_connectionMap(),
    m_errorStringLock(),
    m_errorString(""),
    m_link(link)
{

}

ConnectionManager::~ConnectionManager()
{

}

shared_ptr<Connection> ConnectionManager::createConnection(
                const string &userName, pid_t userPid, no_port_t port)
{
    Mutex::Autolock l(m_connectionMap.locker());

    shared_ptr<Connection> conn = m_connectionMap.valueNoLock(port);
    if (conn) {
        /*
         * already registered port, failed
         */
        setErrorString(string("failed to create the"
                " port holding by process: ") + conn->m_userName);
        Log::e(LOG_TAG, errorString());

        return shared_ptr<Connection>();
    }

    shared_ptr<Connection> connection(
                            new Connection(this, userName, userPid, port));

    m_connectionMap.insertNolock(port, connection);

    return connection;
}


void ConnectionManager::destroyConnection(
                                    shared_ptr<Connection> connection)
{
    connection->close();

    m_connectionMap.removeOne(connection->getPort());
}

shared_ptr<Connection> ConnectionManager::findConnectionByPort(
                                                        no_port_t port)
{
    return m_connectionMap.value(port);
}


string ConnectionManager::errorString() const
{
    Mutex::Autolock l(&m_errorStringLock);

    return m_errorString;
}

void ConnectionManager::setErrorString(const std::string &errorString)
{
    Mutex::Autolock l(&m_errorStringLock);

    m_errorString = errorString;
}

int ConnectionManager::getMaxPayloadSize() const
{
    return m_link->getMaxPayloadSize();
}

Link *ConnectionManager::getLink() const
{
    return m_link;
}

int ConnectionManager::enqueueIORequest(shared_ptr<IORequest> request)
{
    return m_link->enqueueIORequest(request);
}

bool ConnectionManager::processArrivedPackage(Package *package)
{
    IWDS_TRACE;

    Mutex::Autolock l(m_connectionMap.locker());

    PackageHeader *hdr = &package->header;

    if (hdr->type == PkgType::NACK_LINK)
    {
        /*
         * Handle NACK_LINK
         */
        for (ProtectedMap<
                int, shared_ptr<Connection> >::value_type value :
                                            *m_connectionMap.data()) {
            shared_ptr<Connection> connection = value.second;

            connection->processArrivedPackage(package);
        }

        return true;
    }

    shared_ptr<Connection> connection =
                                m_connectionMap.valueNoLock(hdr->port);
    if (!connection) {
        /*
         * Handle NACK protocol
         */

        if (hdr->type == PkgType::SYNC) {
            shared_ptr<IORequest> nackSyncReq =
                        IORequest::createIORequest(
                            Package::createPackage(
                                    hdr->port, PkgType::NACK_SYNC));

            /*
             * Need sync
             */
            nackSyncReq->setNeedSync(true);

            enqueueIORequest(nackSyncReq);

        } else if (hdr->type == PkgType::FIN) {
            shared_ptr<IORequest> ackFinReq =
                        IORequest::createIORequest(
                            Package::createPackage(
                                    hdr->port, PkgType::ACK_FIN));

            /*
             * Need sync
             */
            ackFinReq->setNeedSync(true);

            enqueueIORequest(ackFinReq);
        }

        return true;
    }

    return connection->processArrivedPackage(package);
}
