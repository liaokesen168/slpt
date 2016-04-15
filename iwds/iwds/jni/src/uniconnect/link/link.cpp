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
#include <utils/thread.h>
#include <utils/condition.h>

#include <uniconnect/drivers/datachannel.h>
#include <uniconnect/link.h>
#include <uniconnect/connectionmanager.h>
#include <uniconnect/iorequest.h>
#include <uniconnect/uniconnecterrorcode.h>

#include "./server.h"
#include "./client.h"
#include "./reader.h"
#include "./writer.h"


using std::tr1::shared_ptr;
using std::unique_ptr;

using std::string;
using std::list;

using Iwds::Mutex;
using Iwds::Condition;
using Iwds::Thread;
using Iwds::Assert;
using Iwds::Log;
using Iwds::ByteArray;
using Iwds::IwdsRuntime;
using Iwds::no_port_t;


#define LOG_TAG (string("Uniconnect: Link: ") +             \
                    this->trait()->tag()) + ": "

Link::Link(shared_ptr<DataChannel> dataChannel) :
    m_dataChannel(dataChannel),
    m_reader(new Reader(this)),
    m_writer(new Writer(this)),
    m_server(new Server(this)),
    m_client(new Client(this)),
    m_oldRemoteAddress(""),
    m_errorStringLock(),
    m_errorString(""),
    m_state(LinkState::STATE_DISCONNECTED),
    m_stateLock(),
    m_stateWait(),
    m_stateChangedHandler(0),
    m_isRoleAsClientSide(false),
    m_connectionManager(new ConnectionManager(this)),
    m_ctrlReqQueue(),
    m_dataReqQueue(),
    m_reqCond()
{

}

Link::~Link()
{

}

bool Link::isRoleAsClientSide() const
{
    return m_isRoleAsClientSide;
}

bool Link::startReader()
{
    bool success = m_reader->start();
    if (!success) {
        setErrorString(string("start reader failed: ") +
                                            m_reader->errorString());
        return false;
    }

    return true;
}

void Link::waitReaderStop()
{
    m_reader->waitStop();
}

bool Link::startWriter()
{
    bool success = m_writer->start();
    if (!success) {
        setErrorString(string("start writer failed: ") +
                                            m_writer->errorString());
        return false;
    }

    return true;
}

void Link::stopWriter()
{
    m_writer->stop();
}

bool Link::initializeAsClientSide()
{
    m_isRoleAsClientSide = true;

    bool success = m_dataChannel->probe(0);
    if (!success) {
        setErrorString(string("probe data channel failed: ") +
                                        m_dataChannel->errorString());
        return false;
    }

    success = m_client->start();
    if (!success) {
        setErrorString(string("start client failed: ") +
                                        m_client->errorString());
        return false;
    }

    return true;
}

bool Link::initializeAsServerSide()
{
    if (!IwdsRuntime::getInstance()->isXburstPlatform()) {
        /*
         * This line pin uniconnect on xburst platform
         */
        return initializeAsClientSide();
    }

    m_isRoleAsClientSide = false;

    bool success = m_dataChannel->probe(0);
    if (!success) {
        setErrorString(string("probe data channel failed: ") +
                                        m_dataChannel->errorString());
        return false;
    }

    success = m_server->start();
    if (!success) {
        setErrorString(string("start server failed: ") +
                                        m_server->errorString());
        return false;
    }

    return true;
}

string Link::getRemoteAddress() const
{
    return m_dataChannel->getRemoteAddress();
}

bool Link::isConnected() const
{
    return getState() == LinkState::STATE_CONNECTED;
}

bool Link::bond(const std::string &address)
{
    return setServerAddress(address);
}

void Link::unbond()
{
    m_client->requestExit();

    setServerAddress("");
    clientSideDisconnect();

    m_client->wait();
}

bool Link::isBonded() const
{
    return !getRemoteAddress().empty();
}

void Link::stopServer()
{
    m_server->requestExit();

    serverSideDisconnect();

    m_server->wait();
}

bool Link::enable(bool enable)
{
    return m_dataChannel->enable(enable);
}

bool Link::reset()
{
    return m_dataChannel->reset();
}

bool Link::readyForOperation()
{
    if (!m_dataChannel->readyForOperation()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

bool Link::readThreadReadyToRun()
{
    if (!m_dataChannel->readThreadReadyToRun()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

void Link::readThreadAtExit()
{
    m_dataChannel->readThreadAtExit();
}

bool Link::writeThreadReadyToRun()
{
    if (!m_dataChannel->writeThreadReadyToRun()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

void Link::writeThreadAtExit()
{
    m_dataChannel->writeThreadAtExit();
}

bool Link::listenerThreadReadyToRun()
{
    if (!m_dataChannel->listenerThreadReadyToRun()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

void Link::listenerThreadAtExit()
{
    m_dataChannel->listenerThreadAtExit();
}

bool Link::connectThreadReadyToRun()
{
    if (!m_dataChannel->connectThreadReadyToRun()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

void Link::connectThreadAtExit()
{
    m_dataChannel->connectThreadAtExit();
}

int Link::read(char *buffer, int maxSize)
{
    return m_dataChannel->read(buffer, maxSize);
}

bool Link::write(const char *buffer, int size)
{
    return m_dataChannel->write(buffer, size);
}

bool Link::flush()
{
    return m_dataChannel->flush();
}

bool Link::waitForConnect()
{
    if (!m_dataChannel->waitForConnect()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

void Link::setStateChangedHandler(LinkStateChangedHandler *handler)
{
    Mutex::Autolock l(&m_stateLock);

    m_stateChangedHandler = handler;
}

enum LinkState Link::getState() const
{
    Mutex::Autolock l(&m_stateLock);

    return m_state;
}

void Link::waitForStateChanged(LinkState oldState) const
{
    IWDS_TRACE;

    Mutex::Autolock l(&m_stateLock);

    while (oldState == m_state)
        m_stateWait.wait(&m_stateLock);
}

void Link::notifyLinkConnected()
{
    IWDS_TRACE;

    LinkStateChangedHandler *handler;

    m_stateLock.lock();

    handler = m_stateChangedHandler;

    m_stateLock.unlock();

    if (handler) {
        m_oldRemoteAddress = getRemoteAddress();

        handler->onLinkStateChanged(
                isRoleAsClientSide(),
                m_oldRemoteAddress,
                trait()->tag(),
                LinkState::STATE_CONNECTED);
    }
}

void Link::notifyLinkDisconnected()
{
    IWDS_TRACE;

    LinkStateChangedHandler *handler;

    m_stateLock.lock();

    handler = m_stateChangedHandler;

    m_stateLock.unlock();

    if (handler) {
        handler->onLinkStateChanged(
                isRoleAsClientSide(),
                m_oldRemoteAddress,
                trait()->tag(),
                LinkState::STATE_DISCONNECTED);
    }
}

bool Link::setServerAddress(const string &address)
{
    if (address == m_dataChannel->getRemoteAddress())
        return true;

    if (!m_dataChannel->setRemoteAddress(address)) {
        /*
         * Die is better
         */
        Assert::dieIf(true, m_dataChannel->errorString());

        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

void Link::clientSideDisconnect()
{
    m_dataChannel->clientSideDisconnect();
}

void Link::serverSideDisconnect()
{
    m_dataChannel->serverSideDisconnect();
}

void Link::setState(enum LinkState state)
{
    Mutex::Autolock l(&m_stateLock);

    if (state == m_state)
        return;

    Log::d(LOG_TAG,
            string("State changed: ") + LinkStateString(m_state) +
                                    " ---> " + LinkStateString(state));
    m_state = state;

    m_stateWait.broadcast();
}

bool Link::connect()
{
    if (!m_dataChannel->connect()) {
        setErrorString(m_dataChannel->errorString());

        return false;
    }

    return true;
}

int Link::getMaxPayloadSize() const
{
    return m_dataChannel->getMaxPayloadSize();
}

shared_ptr<ConnectionManager> Link::getConnectionManager()
{
    return m_connectionManager;
}

shared_ptr<DataChannelTag> Link::trait() const
{
    return m_dataChannel->trait();
}

void Link::setErrorString(const string &errorString)
{
    Mutex::Autolock l(&m_errorStringLock);

    m_errorString = errorString;
}

string Link::errorString() const
{
    Mutex::Autolock l(&m_errorStringLock);

    return m_errorString;
}

bool Link::processArrivedPackage(Package *package)
{
    return m_connectionManager->processArrivedPackage(package);
}

shared_ptr<IORequest> Link::waitIORequest()
{
    Mutex::Autolock l(&m_stateLock);

    while (m_ctrlReqQueue.empty() &&
                m_dataReqQueue.empty())
        m_reqCond.wait(&m_stateLock);

    shared_ptr<IORequest> req;

    if (!m_ctrlReqQueue.empty()) {
        /*
         * Take control request first
         */
        req = m_ctrlReqQueue.back();

        m_ctrlReqQueue.pop_back();

    } else {
        req = m_dataReqQueue.back();

        m_dataReqQueue.pop_back();
    }

    return req;
}

void Link::cancelAllDataRequest(int error)
{
    list<shared_ptr<IORequest> >::iterator it = m_dataReqQueue.begin();
    for (; it != m_dataReqQueue.end(); it++) {
        shared_ptr<IORequest> request = *it;

        Mutex::Autolock l(request->locker());

        if (request->isWaitSend())
                request->complete(error);

        it = m_dataReqQueue.erase(it);
    }
}

void Link::cancelDataRequestByPort(no_port_t port, int error)
{
    list<shared_ptr<IORequest> >::iterator it = m_dataReqQueue.begin();
    for (; it != m_dataReqQueue.end(); it++) {
        shared_ptr<IORequest> request = *it;

        Mutex::Autolock l(request->locker());

        PackageHeader *hdr = &request->getPackage()->header;
        if (hdr->port == port) {
            if (request->isWaitSend())
                request->complete(error);

            it = m_dataReqQueue.erase(it);
        }
    }
}

int Link::enqueueIORequest(shared_ptr<IORequest> request)
{
    Mutex::Autolock l(&m_stateLock);

    PackageHeader *hdr = &request->getPackage()->header;

    /*
     * Do not enqueue not NACK_LINK request when link lost
     */
    if (hdr->type != PkgType::NACK_LINK &&
            m_state != LinkState::STATE_CONNECTED) {

        return UniconnectErrorCode::ELINKDISCONNECTED;
    }

    /*
     * Process Fin request
     */
    if (hdr->type == PkgType::FIN) {
        /*
         * Cancel all data requests belong to
         * that port start finish protocol
         */
        cancelDataRequestByPort(
                    hdr->port, UniconnectErrorCode::EPORTCLOSED);

    } else if (hdr->type == PkgType::NACK_LINK) {
        /*
         * Process NACK_LINK request
         */
        cancelAllDataRequest(UniconnectErrorCode::ELINKDISCONNECTED);
    }

    /*
     * Enqueue request
     */
    if (hdr->type == PkgType::DATA) {
        /*
         * Debug stuff
         */
        if (Log::isDebugEnabled()) {
            Log::d(string(LOG_TAG).c_str(),
                    "Data Path: Package: DATA,"
                    " Data length: %d --------------> Port: %u",
                    hdr->dataLength,
                    hdr->port);
        }
        /*
         * Enqueue
         */
        m_dataReqQueue.push_front(request);

    } else {
        /*
         * Debug stuff
         */
        if (Log::isDebugEnabled()) {
            Log::d(string(LOG_TAG).c_str(),
                    "Data Path: Package: %s,"
                    " Data length: %d --------------> Port: %u",
                    PackageHeader::typeToString(hdr->type),
                    hdr->dataLength,
                    hdr->port);
        }
        /*
         * Enqueue
         */
        m_ctrlReqQueue.push_front(request);
    }

    /*
     * Wake-up request dequeue
     */
    m_reqCond.broadcast();

    return 0;
}
