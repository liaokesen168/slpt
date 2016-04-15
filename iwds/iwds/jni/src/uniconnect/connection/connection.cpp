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


#include <utils/assert.h>
#include <utils/log.h>
#include <utils/ringbuffer.h>

#include <uniconnect/connectionmanager.h>
#include <uniconnect/link.h>
#include <uniconnect/connection.h>
#include <uniconnect/iorequest.h>

using std::tr1::shared_ptr;
using std::string;
using std::min;

using Iwds::Log;
using Iwds::Assert;
using Iwds::ByteArray;
using Iwds::RingBuffer;
using Iwds::Mutex;
using Iwds::Condition;
using Iwds::size_pkg_t;
using Iwds::no_port_t;

static const string LOG_TAG("Uniconnect: Connection: ");

Connection::Connection(ConnectionManager *connectionManager,
                        string userName, pid_t userPid, no_port_t port) :
    m_connectionManager(connectionManager),
    m_userName(userName),
    m_userPid(userPid),
    m_port(port),

    m_readBuffer(connectionManager->getMaxPayloadSize()),

    m_state(S_OPENED),
    m_stateLock(),
    m_stateCond(),

    m_disconnectReason(R_PORT_DISCONNECTED),

    m_dataReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::DATA))),

    m_syncReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::SYNC))),
    m_finReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::FIN))),

    m_ackDataReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::ACK_DATA))),
    m_ackSyncReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::ACK_SYNC))),

    m_ackFinReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::ACK_FIN))),

    m_nackSyncReq(
            IORequest::createIORequest(
                    Package::createPackage(port, PkgType::NACK_SYNC)))
{
    IWDS_TRACE;

    /*
     * All control need sync
     */
    m_syncReq->setNeedSync(true);
    m_finReq->setNeedSync(true);
    m_ackSyncReq->setNeedSync(true);
    m_ackFinReq->setNeedSync(true);
    m_nackSyncReq->setNeedSync(true);

    Assert::dieIf(userName.empty(), LOG_TAG + "User name is empty.");

    Assert::dieIf(
            port < 0 || port > UINT_MAX,
                                LOG_TAG + "Port exceed [0, 0xffffffffU]");
}

Connection::~Connection()
{
    IWDS_TRACE;
}

bool Connection::processArrivedPackage(Package *package)
{
    IWDS_TRACE;

    /*
     *****************************************************************
     *                         UNICONNECT TCP
     *****************************************************************
     *
     * Connect Protocol:
     *
     * Positive side                      Passive side
     *              SYNC----------------->
     *                  <-----------------ACK
     *              Connected             Connected
     *
     * Finish Protocol:
     *
     * Positive side                      Passive side
     *              FIN------------------>
     *                 <------------------ACK
     *              Disconnected          Disconnected
     *
     * Transfer Protocol:
     *
     * Positive side                      Passive side
     *              DATA----------------->
     *                  <-----------------Read ACK0
     *                  <-----------------Read ACK1
     *                                    ...
     *                  <-----------------Read ACKn
     *              Complete
     *****************************************************************
     */

    Mutex::Autolock l(&m_stateLock);

    PackageHeader *hdr = &package->header;

    /*
     * Debug stuff
     */
    if (Log::isDebugEnabled()) {
        Log::d(LOG_TAG.c_str(),
                "Data Path: Port: %u(%s, PID: %d), Current state: %s"
                " <--------------"
                " Package: %s, Data length: %d",
                m_port,
                m_userName.c_str(),
                m_userPid,
                stateToString(m_state),
                PackageHeader::typeToString(hdr->type),
                hdr->dataLength);
    }

    switch (m_state) {
    case S_OPENED:
        switch (hdr->type) {
        case PkgType::SYNC:
            enqueueIORequest(m_nackSyncReq);

            break;

        case PkgType::FIN:
            enqueueIORequest(m_ackFinReq);

            break;

        case PkgType::NACK_LINK:
            /*
             * Link disconnected
             */

            m_disconnectReason = R_LINK_DISCONNECTED;

            setState(S_LINK_DISCONNECTED);

            break;
        }

        /*
         * end S_OPENED
         */
        break;

    case S_CONNECTING:
        switch (hdr->type) {
        case PkgType::NACK_SYNC:

            setState(S_CONNECTING_WAIT);

            break;

        case PkgType::SYNC:
            enqueueIORequest(m_ackSyncReq);

            break;

        case PkgType::ACK_SYNC:
            m_syncReq->complete(UniconnectErrorCode::ENOERROR);
            m_stateCond.broadcast();

            setState(S_CONNECTED);

            break;

        case PkgType::FIN:
            enqueueIORequest(m_ackFinReq);

            setState(S_CONNECTING_WAIT);

            break;

        case PkgType::NACK_LINK:
            /*
             * Link disconnected
             */
            m_disconnectReason = R_LINK_DISCONNECTED;

            m_syncReq->complete(m_disconnectReason);
            m_stateCond.broadcast();

            setState(S_LINK_DISCONNECTED);

            break;
        }

        /*
         * end case S_CONNECTING
         */
        break;

    case S_CONNECTING_WAIT:
        switch (hdr->type) {
        case PkgType::SYNC:
            enqueueIORequest(m_ackSyncReq);

            m_syncReq->complete(UniconnectErrorCode::ENOERROR);
            m_stateCond.broadcast();

            setState(S_CONNECTED);

            break;

        case PkgType::FIN:
            enqueueIORequest(m_ackFinReq);

            break;

        case PkgType::NACK_LINK:
            /*
             * Link disconnected
             */
            m_disconnectReason = R_LINK_DISCONNECTED;

            m_syncReq->complete(m_disconnectReason);
            m_stateCond.broadcast();

            setState(S_LINK_DISCONNECTED);

            break;
        }

        /*
         * end case S_CONNECTING
         */
        break;

    case S_CONNECTED:
        switch (hdr->type) {
        case PkgType::DATA:
            /*
             * Process arrived data
             */
            m_readBuffer.put(package->data, hdr->dataLength);
            m_stateCond.broadcast();

            break;

        case PkgType::ACK_DATA:
            /*
             * Process Acked data
             */
            m_dataReq->acknowledgeBytes(hdr->dataLength);

            if (m_dataReq->getTransferedBytes() ==
                    m_dataReq->getPackage()->header.dataLength) {
                /*
                 * All data was confirmed received
                 * notify data request complete
                 */
                m_dataReq->complete(UniconnectErrorCode::ENOERROR);
                m_stateCond.broadcast();
            }

            break;

        case PkgType::FIN:
            enqueueIORequest(m_ackFinReq);

            m_readBuffer.clear();
            m_dataReq->complete(m_disconnectReason);
            m_stateCond.broadcast();

            setState(S_CLOSED);

            break;

        case PkgType::NACK_LINK:
            /*
             * Link disconnected
             */
            m_disconnectReason = R_LINK_DISCONNECTED;

            m_readBuffer.clear();
            m_dataReq->complete(m_disconnectReason);
            m_stateCond.broadcast();

            setState(S_LINK_DISCONNECTED);

            break;
        }

        /*
         * end S_CONNECTED
         */
        break;

    case S_CLOSING:
        switch (hdr->type) {
        case PkgType::DATA:
            /*
             * Process arrived data
             */
            m_readBuffer.put(package->data, hdr->dataLength);
            m_stateCond.broadcast();

            break;

        case PkgType::ACK_DATA:
            /*
             * Process Acked data
             */
            m_dataReq->acknowledgeBytes(hdr->dataLength);

            if (m_dataReq->getTransferedBytes() ==
                    m_dataReq->getPackage()->header.dataLength) {
                /*
                 * All data was confirmed received
                 * notify data request complete
                 */
                m_dataReq->complete(UniconnectErrorCode::ENOERROR);
                m_stateCond.broadcast();
            }

            break;

        case PkgType::FIN:
            enqueueIORequest(m_ackFinReq);

            break;


        case PkgType::ACK_FIN:
            m_readBuffer.clear();
            m_syncReq->complete(m_disconnectReason);
            m_dataReq->complete(m_disconnectReason);
            m_finReq->complete(UniconnectErrorCode::ENOERROR);

            m_stateCond.broadcast();

            setState(S_CLOSED);

            break;

        case PkgType::NACK_LINK:
            /*
             * Link disconnected
             */
            m_disconnectReason = R_LINK_DISCONNECTED;

            m_readBuffer.clear();
            m_syncReq->complete(m_disconnectReason);
            m_dataReq->complete(m_disconnectReason);
            m_finReq->complete(m_disconnectReason);

            m_stateCond.broadcast();

            setState(S_LINK_DISCONNECTED);

            break;
        }

        /*
         * end S_CLOSING
         */
        break;

    case S_CLOSED:
        switch (hdr->type) {
        case PkgType::SYNC:
            enqueueIORequest(m_nackSyncReq);

            break;

        case PkgType::FIN:
            enqueueIORequest(m_ackFinReq);

            break;
        }

        /*
         * end S_CLOSED
         */
        break;

    case S_LINK_DISCONNECTED:
        /*
         * end S_LINK_DISCONNECTED
         */
        break;

        /*
         * end switch m_state
         */

    default:
        Assert::dieIf(true, "Implement me.");

        break;
    }

    return true;
}

void Connection::setState(enum State newState)
{
    int oldState = m_state;

    m_state = newState;

    if (Log::isDebugEnabled()) {
        Log::d(LOG_TAG.c_str(),
                "====== Port: %u, State changed: %s ---> %s ======",
                m_port,
                stateToString(oldState),
                stateToString(newState));
    }
}

no_port_t Connection::getPort() const
{
    return m_port;
}

string Connection::getUserName() const
{
    return m_userName;
}

pid_t Connection::getUserPid() const
{
    return m_userPid;
}

int Connection::write(const char *buffer, int maxSize)
{
    Mutex::Autolock l(&m_stateLock);

    /*
     * Debug stuff
     */
    if (Log::isDebugEnabled()) {
        Log::d(LOG_TAG.c_str(),
                "Port: %u, write max size: %d",
                m_port,
                maxSize);
    }

    if (m_state != S_CONNECTED) {
        /*
         * Debug stuff
         */
        if (Log::isDebugEnabled()) {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, write result: %s",
                    m_port,
                    uniconnectErrorCodeToString(m_disconnectReason));
        }

        return m_disconnectReason;
    }

    /*
     * Reset for transfer
     */
    m_dataReq->reset();
    std::tr1::shared_ptr<Package> pkg = m_dataReq->getPackage();

    /*
     * Initialize transfer
     */
    pkg->header.port = m_port;
    pkg->header.dataLength = maxSize;
    pkg->header.type = PkgType::DATA;
    pkg->data = const_cast<char *>(buffer);

    /*
     * Wait for transfer complete
     */
    enqueueIORequest(m_dataReq);
    while (!m_dataReq->isComplete())
        m_stateCond.wait(&m_stateLock);

    int bytesWritten = m_dataReq->getTransferedBytes();

    /*
     * Debug stuff
     */
    if (Log::isDebugEnabled()) {
        if (!bytesWritten) {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, write result: %s",
                    m_port,
                    uniconnectErrorCodeToString(m_dataReq->getError()));
        } else {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, write result: no error, bytes: %d",
                    m_port,
                    bytesWritten);
        }
    }

    return bytesWritten ? bytesWritten : m_dataReq->getError();
}

int Connection::read(char *buffer, int maxSize)
{
    Mutex::Autolock l(&m_stateLock);

    /*
     * Debug stuff
     */
    if (Log::isDebugEnabled()) {
        Log::d(LOG_TAG.c_str(),
                "Port: %u, read max size: %d",
                m_port,
                maxSize);
    }

    if (m_state != S_CONNECTED) {
        /*
         * Debug stuff
         */
        if (Log::isDebugEnabled()) {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, read result: %s",
                    m_port,
                    uniconnectErrorCodeToString(m_disconnectReason));
        }

        return m_disconnectReason;
    }

    /*
     * Wait data arrived or error
     */
    int bytesRead = min(maxSize, m_readBuffer.size());
    while (!bytesRead) {
        m_stateCond.wait(&m_stateLock);

        if (m_state != S_CONNECTED) {
            /*
             * Return for port disconnect
             */

            /*
             * Debug stuff
             */
            if (Log::isDebugEnabled()) {
                Log::d(LOG_TAG.c_str(),
                        "Port: %u, read result: %s",
                        m_port,
                        uniconnectErrorCodeToString(m_disconnectReason));
            }

            return m_disconnectReason;
        }

        bytesRead = min(maxSize, m_readBuffer.size());
    }

    /*
     * Get bytes
     */
    m_readBuffer.get(buffer, bytesRead);

    /*
     * Send ACKed
     */
    m_ackDataReq->reset();
    PackageHeader *hdr = &m_ackDataReq->getPackage()->header;

    hdr->port = m_port;
    hdr->type = ACK_DATA;
    hdr->dataLength = bytesRead;

    m_ackDataReq->setWaitSend(true);
    m_ackDataReq->setNeedSync(true);

    int error = enqueueIORequest(m_ackDataReq);

    if (!error) {
        /*
         * State unlocked area
         * Must unlock state lock to avoid deadlock
         */
        m_stateLock.unlock();

        /*
         * Request locked
         */
        m_ackDataReq->lock();

        while (!m_ackDataReq->isComplete())
            error = m_ackDataReq->waitComplete();

        /*
         * Request unlocked
         */
        m_ackDataReq->unlock();

        /*
         * State locked area
         */
        m_stateLock.lock();
    }

    /*
     * Debug stuff
     */
    if (Log::isDebugEnabled()) {
        if (error) {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, read result: %s",
                    m_port,
                    uniconnectErrorCodeToString(error));
        } else {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, read result: no error, bytes: %d",
                    m_port,
                    bytesRead);
        }
    }

    return error ? error : bytesRead;
}

int Connection::available() const
{
    IWDS_TRACE;

    Mutex::Autolock l(&m_stateLock);

    return m_readBuffer.availableSize();
}

int Connection::connect()
{
    IWDS_TRACE;

    Mutex::Autolock l(&m_stateLock);

    if (m_state != S_OPENED) {
        if (m_state == S_LINK_DISCONNECTED) {
            /*
             * Link lost
             */

            /*
             * Debug stuff
             */
            if (Log::isDebugEnabled()) {
                Log::d(LOG_TAG.c_str(),
                        "Port: %u, connect result: %s",
                        m_port,
                        uniconnectErrorCodeToString(
                                UniconnectErrorCode::ELINKDISCONNECTED));
            }

            return UniconnectErrorCode::ELINKDISCONNECTED;
        }

        /*
         * Debug stuff
         */
        if (Log::isDebugEnabled()) {
            Log::d(LOG_TAG.c_str(),
                    "Port: %u, connect result: %s",
                    m_port,
                    uniconnectErrorCodeToString(
                                        UniconnectErrorCode::EPORTBUSY));
        }

        return UniconnectErrorCode::EPORTBUSY;
    }

    /*
     * Start connect protocol
     */
    setState(S_CONNECTING);

    enqueueIORequest(m_syncReq);
    while (!m_syncReq->isComplete())
        m_stateCond.wait(&m_stateLock);

    /*
     * Debug stuff
     */
    if (Log::isDebugEnabled()) {
        Log::d(LOG_TAG.c_str(),
                "Port: %u, connect result: %s",
                m_port,
                uniconnectErrorCodeToString(m_syncReq->getError()));
    }

    return m_syncReq->getError();
}

void Connection::close()
{
    IWDS_TRACE;

    Mutex::Autolock l(&m_stateLock);

    m_disconnectReason = R_PORT_CLOSED;

    if (m_state == S_OPENED ||
            m_state == S_CLOSED ||
            m_state == S_LINK_DISCONNECTED) {
        /*
         * Here port is in closed state
         */
        setState(S_CLOSED);

        Log::d(LOG_TAG.c_str(),
                "Port: %u, close result: no error", m_port);

        return;
    }

    /*
     * Here port is in connecting/connected status,
     * so we process close with finish protocol
     */
    if (m_state != S_CLOSING)
        enqueueIORequest(m_finReq);

    setState(S_CLOSING);

    while (!m_finReq->isComplete()) {
        bool success = m_stateCond.wait(&m_stateLock, 3000);
        if (success)
            continue;

        /*
         * Force reset link
         */
        Link *link = m_connectionManager->getLink();
        link->isRoleAsClientSide() ?
                link->clientSideDisconnect() : link->serverSideDisconnect();

        /*
         * Complete all as link disconnected
         */
        m_disconnectReason = R_LINK_DISCONNECTED;

        m_readBuffer.clear();
        m_syncReq->complete(m_disconnectReason);
        m_dataReq->complete(m_disconnectReason);
        m_finReq->complete(m_disconnectReason);

        setState(S_CLOSED);

        m_stateCond.broadcast();

        Log::e(LOG_TAG.c_str(), "Force reset link by 3 seconds timeout"
                " when close port(due to system data channel error).");

        break;
    }

    /*
     * Debug stuff
     */
    Log::d(LOG_TAG.c_str(),
            "Port: %u, close result: %s",
            m_port,
            uniconnectErrorCodeToString(m_finReq->getError()));
}

int Connection::enqueueIORequest(
                            std::tr1::shared_ptr<IORequest> request)
{
    return m_connectionManager->enqueueIORequest(request);
}

