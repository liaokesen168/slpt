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


#include <utils/vendor.h>

#include <uniconnect/drivers/datachannel.h>
#include <uniconnect/link.h>

#include <uniconnect/linkmanager.h>


using std::string;
using std::tr1::shared_ptr;

using Iwds::Log;
using Iwds::Assert;
using Iwds::Mutex;
using Iwds::ProtectedList;
using Iwds::Vendor;


static const string LOG_TAG("Uniconnect: LinkManager: ");

shared_ptr<LinkManager> LinkManager::getInstance()
{
    static shared_ptr<LinkManager> the_linkManager(new LinkManager());

    return the_linkManager;
}

LinkManager::LinkManager() :
    m_registeredChannelList(),
    m_availableChannels(),
    m_links(),
    m_errorStringLock(),
    m_errorString(""),
    m_stateHandler(0)
{

}

LinkManager::~LinkManager()
{

}

bool LinkManager::initialize()
{
    m_registeredChannelList.locker()->lock();
    for (shared_ptr<DataChannel> channel :
                        *m_registeredChannelList.data()) {
        m_registeredChannelList.locker()->unlock();

        if (!channel->probe(0)) {
            Log::e(LOG_TAG, string("probe data channel: ") +
                    channel->trait()->tag() +
                    " failed: " + channel->errorString());
            continue;
        }

        m_availableChannels.append(channel);

        m_registeredChannelList.locker()->lock();
    }
    m_registeredChannelList.locker()->unlock();

    m_registeredChannelList.clear();

    if (m_availableChannels.empty()) {
        setErrorString("no available data link");
        return false;
    }

    return true;
}

shared_ptr<Link> LinkManager::findLinkByAddress(
                                        const string &address) const
{
    Mutex::Autolock l(m_links.locker());

    for (shared_ptr<Link> link : *m_links.data())
        if (link->getRemoteAddress() == address)
            return link;

    return shared_ptr<Link>();
}

shared_ptr<Link> LinkManager::findServerSideLinkByTag(
                                    const std::string &linkTag) const
{
    Mutex::Autolock l(m_links.locker());

    for (shared_ptr<Link> link : *m_links.data())
        if (!link->isRoleAsClientSide() &&
                link->trait()->tag() == linkTag)
            return link;

    return shared_ptr<Link>();
}

void LinkManager::setStateChangedHandler(
                                    LinkStateChangedHandler *handler)
{
    m_stateHandler = handler;
}

string LinkManager::errorString() const
{
    Mutex::Autolock l(&m_errorStringLock);

    return m_errorString;
}

string LinkManager::getLinkTypes() const
{
    Mutex::Autolock l(m_availableChannels.locker());

    string linkTypes;
    for (shared_ptr<DataChannel> channel :
            *m_availableChannels.data())
        linkTypes += channel->trait()->tag() + ",";

    return linkTypes;
}

bool LinkManager::bond(const string &linkTag, const string &address)
{
    Assert::dieIf(address.empty(), "Address is empty.");

    Assert::dieIf(findLinkByAddress(address), "Already bonded address");

    Mutex::Autolock l(m_availableChannels.locker());

    for (shared_ptr<DataChannel> channel :
                        *m_availableChannels.data()) {
        if (channel->trait()->tag() == linkTag) {
            shared_ptr<Link> link(new Link(channel->createChannel()));
            if (!link->initializeAsClientSide()) {
                setErrorString("failed initialize link: " +
                        link->trait()->tag() + ": " +
                        link->errorString());

                Log::e(LOG_TAG, errorString());

                return false;
            }

            m_links.append(link);

            link->setStateChangedHandler(m_stateHandler);
            link->bond(address);

            return true;
        }
    }

    Assert::dieIf(true, string("Unavailable link tag: ") + linkTag);

    setErrorString(string("Unavailable link tag: ") + linkTag);

    return false;
}

void LinkManager::unbond(const string &address)
{
    Assert::dieIf(address.empty(), "Address is empty.");

    shared_ptr<Link> link = findLinkByAddress(address);

    Assert::dieIf(!link, "Unbond a unbonded address.");

    link->unbond();

    m_links.erase(link);
}

bool LinkManager::isBonded(const std::string &address) const
{
    return !!findLinkByAddress(address);
}

bool LinkManager::startServer(const std::string &linkTag)
{
    Assert::dieIf(
            findServerSideLinkByTag(linkTag), "Server already started.");

    Mutex::Autolock l(m_availableChannels.locker());

    for (shared_ptr<DataChannel> channel :
                    *m_availableChannels.data()) {
        if (channel->trait()->tag() == linkTag) {
            shared_ptr<Link> link(new Link(channel->createChannel()));
            if (!link->initializeAsServerSide()) {
                setErrorString("failed initialize link: " +
                        link->trait()->tag() + ": " +
                        link->errorString());

                Log::e(LOG_TAG, errorString());

                return false;
            }

            m_links.append(link);

            link->setStateChangedHandler(m_stateHandler);

            return true;
        }
    }

    Assert::dieIf(true, string("Unavailable link tag: ") + linkTag);

    setErrorString(string("Unavailable link tag: ") + linkTag);

    return false;
}

void LinkManager::stopServer(const string &linkTag)
{
    shared_ptr<Link> link = findServerSideLinkByTag(linkTag);

    Assert::dieIf(!link, "A stopped server.");

    link->stopServer();

    m_links.erase(link);
}

string LinkManager::getRemoteAddress(const string &linkTag) const
{
    shared_ptr<Link> link = findServerSideLinkByTag(linkTag);

    Assert::dieIf(!link, "A stopped server.");

    return link->getRemoteAddress();
}

void LinkManager::setErrorString (const string &errorString)
{
    Mutex::Autolock l(&m_errorStringLock);

    m_errorString = errorString;
}

void LinkManager::registerDataChannel(shared_ptr<DataChannel> dataChannel)
{
    Assert::dieIf(!dataChannel, "Oops! Data channel pointer is null.");

    m_registeredChannelList.append(dataChannel);
}

