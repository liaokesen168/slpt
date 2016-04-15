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


#include "./androidbtdatachannel.h"

#include "androidmtkbtapi.h"


using std::unique_ptr;
using std::shared_ptr;

using std::string;

using Iwds::Log;
using Iwds::Assert;
using Iwds::AutoJenv;
using Iwds::getJenv;
using Iwds::getJvm;
using Iwds::Jenv;
using Iwds::findGlobalRefClass;
using Iwds::getStaticMethodId;
using Iwds::getMethodId;
using Iwds::GlobalJobject;
using Iwds::toJclass;
using Iwds::globalRefJobject;
using Iwds::checkExceptionAndDump;
using Iwds::LocalJobject;
using Iwds::Mutex;
using Iwds::Thread;

AndroidMtkBtApi::AndroidMtkBtApi(AndroidBtDataChannel *dataChannel) :
    AndroidBtApi(dataChannel)
{

}
