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


#ifndef CONNECTION_H
#define CONNECTION_H


#include <tr1/memory>
#include <string>

#include <iwds.h>

#include <uniconnect/uniconnecterrorcode.h>

#include <utils/ringbuffer.h>
#include <utils/mutex.h>
#include <utils/condition.h>

class Link;
class ConnectionManager;

struct Package;
class IORequest;


class Connection
{
public:
    enum State {
        S_OPENED,
        S_CONNECTING,
        S_CONNECTING_WAIT,
        S_CONNECTED,
        S_CLOSING,
        S_CLOSED,
        S_LINK_DISCONNECTED
    };

    enum DisconnectReason {
        R_PORT_CLOSED = UniconnectErrorCode::EPORTCLOSED,
        R_PORT_DISCONNECTED = UniconnectErrorCode::EPORTDISCONNECTED,
        R_LINK_DISCONNECTED = UniconnectErrorCode::ELINKDISCONNECTED
    };

    Connection(
            ConnectionManager *connectionManager,
            std::string userName, pid_t userPid, Iwds::no_port_t port);
    ~Connection();

    Iwds::no_port_t getPort() const;
    std::string getUserName() const;
    pid_t getUserPid() const;

    int write(const char *buffer, int maxSize);
    int read(char *buffer, int maxSize);
    int available() const;
    int connect();
    void close();

private:
    ConnectionManager *m_connectionManager;

    std::string m_userName;
    pid_t m_userPid;

    Iwds::no_port_t m_port;

    Iwds::RingBuffer m_readBuffer;

    enum State m_state;
    mutable Iwds::Mutex m_stateLock;
    mutable Iwds::Condition m_stateCond;

    enum DisconnectReason m_disconnectReason;

    std::tr1::shared_ptr<IORequest> m_dataReq;
    std::tr1::shared_ptr<IORequest> m_syncReq;
    std::tr1::shared_ptr<IORequest> m_finReq;

    std::tr1::shared_ptr<IORequest> m_ackDataReq;
    std::tr1::shared_ptr<IORequest> m_ackSyncReq;
    std::tr1::shared_ptr<IORequest> m_ackFinReq;

    std::tr1::shared_ptr<IORequest> m_nackSyncReq;

    int enqueueIORequest(std::tr1::shared_ptr<IORequest> request);

    bool processArrivedPackage(Package *package);

    void setState(enum State newState);

    const char *stateToString(int state)
    {
        switch (state) {
            case S_OPENED:
                return "S_OPENED";

            case S_CONNECTING:
                return "S_CONNECTING";

            case S_CONNECTING_WAIT:
                return "S_CONNECTING_WAIT";

            case S_CONNECTED:
                return "S_CONNECTED";

            case S_CLOSING:
                return "S_CLOSING";

            case S_CLOSED:
                return "S_CLOSED";

            case S_LINK_DISCONNECTED:
                return "S_LINK_DISCONNECTED";

            default:
                Iwds::Assert::dieIf(true, "Implement me.");

                break;
        }

        return "Implement me.";
    }

    // Disable copy and assign
    Connection(const Connection &obj);
    Connection &operator=(const Connection &rhs);

    friend class ConnectionManager;
};

#endif

