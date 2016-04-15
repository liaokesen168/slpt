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


#ifndef LINKMANAGER_H
#define LINKMANAGER_H


#include <tr1/memory>
#include <memory>
#include <string>

#include <utils/mutex.h>
#include <utils/assert.h>
#include <utils/protectedlist.h>

#include <uniconnect/link.h>


class DataChannel;

class LinkManager
{
public:
    static std::tr1::shared_ptr<LinkManager> getInstance();
    ~LinkManager();

    void registerDataChannel(
                    std::tr1::shared_ptr<DataChannel> dataChannel);

    bool initialize();

    std::string errorString() const;

    std::string getLinkTypes() const;
    std::tr1::shared_ptr<Link> findLinkByAddress(
                                    const std::string &address) const;
    std::tr1::shared_ptr<Link> findServerSideLinkByTag(
                                    const std::string &linkTag) const;
    /*
     * client side api
     */
    bool bond(const std::string &linkTag, const std::string &address);
    void unbond(const std::string &address);
    bool isBonded(const std::string &address) const;

    /*
     * server side api
     */
    bool startServer(const std::string &linkTag);
    void stopServer(const std::string &linkTag);

    std::string getRemoteAddress(const std::string &linkTag) const;
    void setStateChangedHandler(LinkStateChangedHandler *handler);

private:
    LinkManager();

    void setErrorString(const std::string &errorString);

    Iwds::ProtectedList<
        std::tr1::shared_ptr<DataChannel> > m_registeredChannelList;
    Iwds::ProtectedList<
        std::tr1::shared_ptr<DataChannel> > m_availableChannels;

    Iwds::ProtectedList<std::tr1::shared_ptr<Link> > m_links;

    mutable Iwds::Mutex m_errorStringLock;
    std::string m_errorString;

    LinkStateChangedHandler *m_stateHandler;
};

#endif
