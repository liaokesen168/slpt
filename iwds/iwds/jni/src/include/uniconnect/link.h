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


#ifndef LINK_H
#define LINK_H


#include <string>
#include <list>

#include <memory>
#include <tr1/memory>

#include <iwds.h>

#include <uniconnect/uniconnecterrorcode.h>

#include <utils/mutex.h>
#include <utils/condition.h>


struct DataChannelTag;
struct Package;

class DataChannel;
class IORequest;
class Scheduler;
class Reader;
class Writer;
class Server;
class Client;
class Scheduler;
class ConnectionManager;

/*********************************************************************
 * Internal control path: Link states management
 */
enum LinkState
{
    STATE_DISCONNECTED = 0,
    STATE_CONNECTED = 1,
    STATE_CONNECTING,
};

inline std::string LinkStateString(enum LinkState state)
{
    std::string str;

    switch (state) {
    case LinkState::STATE_DISCONNECTED:
        str = "Disconnected";
        break;

    case LinkState::STATE_CONNECTING:
        str = "Connecting";
        break;

    case LinkState::STATE_CONNECTED:
        str = "Connected";
        break;

    default:
        Iwds::Assert::dieIf(true, "Implement me.");
        break;
    }

    return str;
}


/*********************************************************************
 * Internal control path: Link class
 */
class LinkStateChangedHandler
{
public:
    virtual ~LinkStateChangedHandler()
    {

    }

    virtual void onLinkStateChanged(
            bool isRoleAsClient,
            const std::string &address,
            const std::string &linkTag, enum LinkState) = 0;
};

class Link
{
public:
    /*****************************************************************
     * Control path: server side API
     */
    bool initializeAsServerSide();
    void stopServer();


public:
    /*****************************************************************
     * Control path: client side API
     */
    bool initializeAsClientSide();
    bool bond(const std::string &address);
    void unbond();
    bool isBonded() const;

    bool startReader();
    void waitReaderStop();

    bool startWriter();
    void stopWriter();

    void setStateChangedHandler(LinkStateChangedHandler *hander);
    std::string getRemoteAddress() const;

    bool isConnected() const;

    std::tr1::shared_ptr<DataChannelTag> trait() const;

    std::tr1::shared_ptr<ConnectionManager> getConnectionManager();

    bool isRoleAsClientSide() const;


public:
    /*****************************************************************
     * IO request API
     */
    int enqueueIORequest(std::tr1::shared_ptr<IORequest> request);


public:
    /*****************************************************************
     * Common API
     */
    std::string errorString() const;

public:
    /*****************************************************************
     * Link internal API
     */
    enum LinkState getState() const;
    void setState(enum LinkState state);

    bool setServerAddress(const std::string &address);

    void clientSideDisconnect();
    void serverSideDisconnect();

    bool enable(bool enable);

    bool reset();

    bool readyForOperation();

    bool readThreadReadyToRun();
    void readThreadAtExit();

    bool writeThreadReadyToRun();
    void writeThreadAtExit();

    bool listenerThreadReadyToRun();
    void listenerThreadAtExit();

    bool connectThreadReadyToRun();
    void connectThreadAtExit();

    void waitForStateChanged(LinkState oldState) const;
    void notifyLinkConnected();
    void notifyLinkDisconnected();

    int read(char *buffer, int maxSize);
    bool write(const char *buffer, int size);
    bool flush();

    bool waitForConnect();
    bool connect();

    int getMaxPayloadSize() const;

    /*****************************************************************
     * For IO requests management
     */
    bool processArrivedPackage(Package *package);

    std::tr1::shared_ptr<IORequest> waitIORequest();

public:
    Link(std::tr1::shared_ptr<DataChannel> dataChannel);
    ~Link();


private:
    /*****************************************************************
     * Control & Data path back-end
     */
    std::tr1::shared_ptr<DataChannel> m_dataChannel;

    /*****************************************************************
     * Control & Data path controllers
     */
    std::unique_ptr<Reader> m_reader;
    std::unique_ptr<Writer> m_writer;
    std::unique_ptr<Server> m_server;
    std::unique_ptr<Client> m_client;

    std::string m_oldRemoteAddress;

    /*****************************************************************
     * For error string
     */
    mutable Iwds::Mutex m_errorStringLock;
    std::string m_errorString;

    void setErrorString(const std::string &errorString);


    /*****************************************************************
     * For link states control
     */
    enum LinkState m_state;
    mutable Iwds::Mutex m_stateLock;
    mutable Iwds::Condition m_stateWait;

    LinkStateChangedHandler *m_stateChangedHandler;

    bool m_isRoleAsClientSide;


    /*****************************************************************
     * For connection control
     */
    std::tr1::shared_ptr<ConnectionManager> m_connectionManager;


    /*****************************************************************
     * For IO requests management
     */
    std::list<std::tr1::shared_ptr<IORequest> > m_ctrlReqQueue;
    std::list<std::tr1::shared_ptr<IORequest> > m_dataReqQueue;

    Iwds::Condition m_reqCond;

    void cancelAllDataRequest(int error);
    void cancelDataRequestByPort(Iwds::no_port_t port, int error);
};


#endif
