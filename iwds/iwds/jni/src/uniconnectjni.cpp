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


#include <string>
#include <tr1/memory>

#include <iwds.h>

#include <utils/assert.h>
#include <utils/log.h>
#include <utils/crc16.h>

#include <uniconnect/uniconnect.h>

#include <uniconnect/connection.h>
#include <uniconnect/connectionmanager.h>
#include <uniconnect/linkmanager.h>
#include <uniconnect/uniconnecterrorcode.h>

#include <jni/uniconnectjni.h>


using std::string;
using std::tr1::shared_ptr;

using Iwds::Assert;
using Iwds::Log;
using Iwds::LocalJobject;
using Iwds::toJclass;
using Iwds::Mutex;
using Iwds::GlobalJobject;
using Iwds::globalRefJobject;
using Iwds::findGlobalRefClass;
using Iwds::getMethodId;
using Iwds::toJclass;
using Iwds::AutoJenv;
using Iwds::crc16;
using Iwds::u8;
using Iwds::DeviceDescriptor;
using Iwds::no_port_t;

static const string LOG_TAG("UniconnectJni: ");

static const char *classConnectionService =
                    "com/ingenic/iwds/uniconnect/ConnectionService";
static const char *classLinkManager =
                    "com/ingenic/iwds/uniconnect/link/LinkManager";

class JniLinkStateChangedHandler : public LinkStateChangedHandler
{
public:
    JniLinkStateChangedHandler(jobject linkManager) :
        m_classLinkManager(),
        m_objLinkManager(globalRefJobject(linkManager)),
        m_methodOnLinkStateChanged(0)
    {
        Assert::dieIf(!m_objLinkManager, "Object of LinkManager is null.");

        m_classLinkManager = findGlobalRefClass(classLinkManager);

        Assert::dieIf(!m_classLinkManager,
                                    "Can not find class: LinkManager");

        m_methodOnLinkStateChanged =
                getMethodId(
                        toJclass(m_classLinkManager),
                        "onLinkStateChanged",
                        "(ZLjava/lang/String;Ljava/lang/String;I)V");

        Assert::dieIf(!m_methodOnLinkStateChanged,
                "Can not find method: LinkManager.onLinkStateChanged");
    }

    void onLinkStateChanged(
            bool isRoleAsClient,
            const string &address, const string &linkTag,
            enum LinkState state)
    {
        static Iwds::Mutex locker;

        Mutex::Autolock l(&locker);

        AutoJenv env;

        LocalJobject addr(env->NewStringUTF(address.c_str()));
        LocalJobject tag(env->NewStringUTF(linkTag.c_str()));

        env->CallVoidMethod(
                m_objLinkManager.get(),
                m_methodOnLinkStateChanged,
                isRoleAsClient ? JNI_TRUE : JNI_FALSE,
                        addr.data(), tag.data(), int(state));
    }

private:
    GlobalJobject m_classLinkManager;
    GlobalJobject m_objLinkManager;

    jmethodID m_methodOnLinkStateChanged;
};

JNIEXPORT jlong JNICALL nativeCreateConnectionByUuid(
        JNIEnv *env, jclass clazz, jstring userName, jint userPid,
                    jstring address, jstring uuid, jobject /* callBack unused */)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(userName, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string user(arg0);
    env->ReleaseStringUTFChars(userName, arg0);

    const char *arg2 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg2, "String chars is null.");

    string addr(arg2);
    env->ReleaseStringUTFChars(address, arg2);

    const char *arg3 = env->GetStringUTFChars(uuid, &isCopy);

    Assert::dieIf(!arg3, "String chars is null.");

    string id(arg3);
    env->ReleaseStringUTFChars(uuid, arg3);

    return createConnectionByUuid(user, userPid, addr, id);
}

JNIEXPORT void JNICALL nativeDestroyConnection(
        JNIEnv *env, jclass clazz, jstring address, jlong port)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    destroyConnection(addr, (no_port_t)port);
}

JNIEXPORT jint JNICALL nativeRead(
        JNIEnv *env, jclass clazz, jstring address,
        jlong port, jobject buffer, jint offset, jint maxSize)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    char *pbuffer = (char *)env->GetDirectBufferAddress(buffer);

    Assert::dieIf(!pbuffer, "Buffer is null.");

    return read(addr, (no_port_t)port, pbuffer, offset, maxSize);
}

JNIEXPORT jint JNICALL nativeWrite(
        JNIEnv *env, jclass clazz, jstring address,
        jlong port, jobject buffer, jint offset, jint maxSize)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    char *pbuffer = (char *)env->GetDirectBufferAddress(buffer);

    Assert::dieIf(!pbuffer, "Buffer is null.");

    return write(addr, (no_port_t)port, pbuffer, offset, maxSize);
}

JNIEXPORT jint JNICALL nativeAvailable(
        JNIEnv *env, jclass clazz, jstring address, jlong port)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    return available(addr, (no_port_t)port);
}

JNIEXPORT jint JNICALL nativeGetMaxPayloadSize(
        JNIEnv *env, jclass clazz, jstring address, jlong port)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    return getMaxPayloadSize(addr, (no_port_t)port);
}

JNIEXPORT jint JNICALL nativeHandshake(
        JNIEnv *env, jclass clazz, jstring address, jlong port)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    return handshake(addr, (no_port_t)port);
}

JNIEXPORT jboolean JNICALL nativeBondAddress(
        JNIEnv *env, jclass clazz, jstring linkTag, jstring address)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(linkTag, &isCopy);
    const char *arg1 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    Assert::dieIf(!arg1, "String chars is null.");

    string tag(arg0);
    string addr(arg1);
    env->ReleaseStringUTFChars(linkTag, arg0);
    env->ReleaseStringUTFChars(address, arg1);

    return bondAddress(tag, addr) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL nativeUnbond
                        (JNIEnv *env, jclass clazz, jstring address)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(address, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string addr(arg0);
    env->ReleaseStringUTFChars(address, arg0);

    unbondAddress(addr);
}

JNIEXPORT jstring JNICALL nativeGetLinkTypes(JNIEnv *env, jclass clazz)
{
    string linkTypes = getLinkTypes();

    return env->NewStringUTF(linkTypes.c_str());
}

JNIEXPORT void JNICALL nativeSetLinkStateChangedHandler(
                        JNIEnv *env, jclass clazz, jobject linkManager)
{
    LinkStateChangedHandler *handler =
                            new JniLinkStateChangedHandler(linkManager);

    setLinkStateChangedHandler(handler);
}

JNIEXPORT jboolean JNICALL nativeStartServer(
                            JNIEnv *env, jclass clazz, jstring linkTag)
{
    IWDS_TRACE;

    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(linkTag, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string tag(arg0);
    env->ReleaseStringUTFChars(linkTag, arg0);

    return startServer(tag) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL nativeStopServer(
                            JNIEnv *env, jclass clazz, jstring linkTag)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(linkTag, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string tag(arg0);
    env->ReleaseStringUTFChars(linkTag, arg0);

    stopServer(tag);
}

JNIEXPORT jstring JNICALL nativeGetRemoteAddress(
                            JNIEnv *env, jclass clazz, jstring linkTag)
{
    jboolean isCopy;

    const char *arg0 = env->GetStringUTFChars(linkTag, &isCopy);

    Assert::dieIf(!arg0, "String chars is null.");

    string tag(arg0);
    env->ReleaseStringUTFChars(linkTag, arg0);

    string address = getRemoteAddress(tag);

    return env->NewStringUTF(address.c_str());
}

static JNINativeMethod gConnectionServiceMethods[] =
{
    {
            "nativeCreateConnectionByUuid",
            "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;"
            "Lcom/ingenic/iwds/uniconnect/IConnectionCallBack;)J",
            (void *)nativeCreateConnectionByUuid
    },

    {
            "nativeDestroyConnection",
            "(Ljava/lang/String;J)V",
            (void *)nativeDestroyConnection
    },

    {
            "nativeWrite",
            "(Ljava/lang/String;JLjava/nio/ByteBuffer;II)I",
            (void *)nativeWrite
    },

    {
            "nativeRead",
            "(Ljava/lang/String;JLjava/nio/ByteBuffer;II)I",
            (void *)nativeRead
    },

    {
            "nativeAvailable",
            "(Ljava/lang/String;J)I",
            (void *)nativeAvailable
    },

    {
            "nativeGetMaxPayloadSize",
            "(Ljava/lang/String;J)I",
            (void *)nativeGetMaxPayloadSize
    },

    {
            "nativeHandshake",
            "(Ljava/lang/String;J)I",
            (void *)nativeHandshake
    }
};

static JNINativeMethod gLinkManagerMethods[] =
{
    {
            "nativeBondAddress",
            "(Ljava/lang/String;Ljava/lang/String;)Z",
            (void *)nativeBondAddress
    },

    {
            "nativeUnbond",
            "(Ljava/lang/String;)V",
            (void *)nativeUnbond
    },

    {
            "nativeGetLinkTypes",
            "()Ljava/lang/String;",
            (void *)nativeGetLinkTypes
    },

    {
            "nativeSetLinkStateChangedHandler",
            "(Lcom/ingenic/iwds/uniconnect/link/LinkManager;)V",
            (void *)nativeSetLinkStateChangedHandler,
    },

    {
            "nativeStartServer",
            "(Ljava/lang/String;)Z",
            (void *)nativeStartServer,
    },

    {
            "nativeStopServer",
            "(Ljava/lang/String;)V",
            (void *)nativeStopServer,
    },

    {
            "nativeGetRemoteAddress",
            "(Ljava/lang/String;)Ljava/lang/String;",
            (void *)nativeGetRemoteAddress,
    },
};

jint registerUniconnectNatives(Iwds::Jenv *env)
{
    LocalJobject clazz(env->FindClass(classConnectionService));
    if (!clazz) {
        Log::e(LOG_TAG, "Can not find class: ConnectionService");

        return JNI_ERR;
    }

    jint error = env->RegisterNatives(
            toJclass(clazz),
            gConnectionServiceMethods,
            sizeof(gConnectionServiceMethods) / sizeof(JNINativeMethod));
    if (error) {
        Log::e(LOG_TAG, "Can not register natives for ConnectionService");

        return error;
    }

    clazz = env->FindClass(classLinkManager);
    if (!clazz) {
        Log::e(LOG_TAG, "Can not find class: LinkManager");

        return JNI_ERR;
    }

    error = env->RegisterNatives(
            toJclass(clazz),
            gLinkManagerMethods,
            sizeof(gLinkManagerMethods) / sizeof(JNINativeMethod));
    if (error) {
        Log::e(LOG_TAG, "Can not register natives for LinkManager");

        return error;
    }

    return JNI_OK;
}

bool initializeUniconnect()
{
    if (!initUniconnect()) {
        Log::e(LOG_TAG, string("Uniconnect initialize failed: ") +
                                        getLinkManagerErrorString());
        return false;
    }

    Log::d(LOG_TAG, "Initialize done.");

    return true;
}
