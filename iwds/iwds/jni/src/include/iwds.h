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


#ifndef IWDS_H
#define IWDS_H


#include <unistd.h>
#include <dirent.h>
#include <errno.h>

#include <string.h>

#include <jni.h>

#include <memory>
#include <tr1/memory>
#include <string>
#include <vector>
#include <utility>

#include <utils/log.h>
#include <utils/assert.h>
#include <utils/android/property.h>
#include <utils/vendor.h>

#include <sys/ioctl.h>


#ifndef __packed
# define __packed       __attribute__((__packed__))
#endif

#define iwds_bswap_32(x) \
     ((((x) & 0xff000000) >> 24) | (((x) & 0x00ff0000) >>  8) |      \
      (((x) & 0x0000ff00) <<  8) | (((x) & 0x000000ff) << 24))

#define IWDS_TRACE_MSG(message)    Iwds::Trace trace(LOG_TAG,        \
            std::string(__FUNCTION__) + std::string(" ") + (message))
#define IWDS_TRACE                 IWDS_TRACE_MSG("")

namespace Iwds
{
    typedef void * Handle;
    typedef JavaVM Jvm;
    typedef JNIEnv Jenv;
    typedef std::tr1::shared_ptr<_jobject> GlobalJobject;
    typedef std::vector<char> ByteArray;

    typedef unsigned char  u8;
    typedef unsigned short u16;
    typedef unsigned int   u32;

    typedef int size_pkg_t;
    typedef u32 no_port_t;

    void saveJvm(Jvm *jvm);
    Jvm *getJvm();

    Jenv *getJenv();

    Jenv *attachCurrentThread();
    void detachCurrentThread();

    GlobalJobject findGlobalRefClass(const std::string &className);
    void deleteGlobalRef(jobject globalRef);

    jmethodID getMethodId(jclass clazz, const std::string &name,
                                                const std::string &sig);
    jmethodID getStaticMethodId(jclass clazz,
                        const std::string &name, const std::string &sig);

    class AutoJenv
    {
    public:
        AutoJenv() :
            m_attached(false)
        {
            Jvm *vm = getJvm();
            if (vm->GetEnv((void **)&m_jenv, JNI_VERSION_1_6) != JNI_OK) {
                m_jenv = attachCurrentThread();
                m_attached = true;
            }
        }

        ~AutoJenv()
        {
            if (m_attached)
                getJvm()->DetachCurrentThread();
        }

        Jenv *operator->() const
        {
            return m_jenv;
        }

        Jenv operator*() const
        {
            return *m_jenv;
        }

        Jenv *data() const
        {
            return m_jenv;
        }

    private:
        Jenv *m_jenv;
        bool m_attached;

        /*
         * disables
         */
        AutoJenv(const AutoJenv &obj);
        AutoJenv &operator=(const AutoJenv &rhs);
    };

    class LocalJobject
    {
    public:
        explicit LocalJobject(jobject obj) :
            m_obj(obj)
        {

        }

        ~LocalJobject()
        {
            if (m_obj) {
                AutoJenv env;

                env->DeleteLocalRef(m_obj);
            }
        }

        /*
         * can not operating like: objA = objB = objC;
         */
        void operator=(jobject obj)
        {
            if (m_obj) {
                AutoJenv env;

                env->DeleteLocalRef(m_obj);
            }

            m_obj = obj;
        }

        jobject data() const
        {
            return m_obj;
        }

        operator bool() const
        {
            return !!m_obj;
        }

    private:
        jobject m_obj;

        /*
        * disables
        */
        LocalJobject(const LocalJobject &obj);
        LocalJobject &operator=(const LocalJobject &rhs);
    };

    GlobalJobject globalRefJobject(jobject refTo);
    inline jclass toJclass(GlobalJobject globalObj)
    {
        return static_cast<jclass>(globalObj.get());
    }

    inline jclass toJclass(const LocalJobject &localObj)
    {
        return static_cast<jclass>(localObj.data());
    }

    class Trace
    {
    public:
        Trace(std::string logTag, std::string funcName) :
            m_logTag(logTag),
            m_funcName(funcName)
        {
            Log::d(m_logTag, std::string("Trace: enter ") + m_funcName);
        }

        ~Trace()
        {
            Log::d(m_logTag, std::string("Trace: exit ") + m_funcName);
        }

    private:
        std::string m_logTag;
        std::string m_funcName;
    };

    bool checkExceptionAndDump(const std::string &logTag);

    struct DeviceDescriptor
    {
        enum {
            DEVICE_CLASS_WEARABLE = 0,
            DEVICE_CLASS_SMARTHOME = 1,
            DEVICE_CLASS_MOBILE = 2,
        };

        enum {
            WEARABLE_DEVICE_SUBCLASS_WATCH = 1,
            WEARABLE_DEVICE_SUBCLASS_GLASS = 2,

            MOBILE_DEVICE_SUBCLASS_SMARTPHONE = 1,
        };

        int deviceClass;
        int deviceSubClass;

        void dump()
        {
            Log::i("DeviceDescriptor", "===== Dump =====");

            Log::i("DeviceDescriptor",
                    "Device class: %d", deviceClass);

            Log::i("DeviceDecritptor",
                    "Device subclass: %d", deviceSubClass);

            Log::i("DeviceDescriptor", "================");
        }
    };

    class IwdsRuntime
    {
    public:
        static std::tr1::shared_ptr<IwdsRuntime> getInstance()
        {
            static std::tr1::shared_ptr<IwdsRuntime>
                                    the_iwdsRuntime(new IwdsRuntime);
            return the_iwdsRuntime;
        }

        int getDeviceClass()
        {
            Assert::dieIf(
                    !m_isInitialized, "IWDS runtime is uninitialized.");

            return m_deviceDescritpor->deviceClass;
        }

        int getDeviceSubclass()
        {
            Assert::dieIf(
                    !m_isInitialized, "IWDS runtime is uninitialized.");

            return m_deviceDescritpor->deviceSubClass;
        }

        bool isXburstPlatform() const
        {
            Assert::dieIf(
                    !m_isInitialized, "IWDS runtime is uninitialized.");

            return m_isXburstPlatform;
        }

        bool isMtkPlatform() const
        {
            Assert::dieIf(
                    !m_isInitialized, "IWDS runtime is uninitialized.");

            return m_isMtkPlatform;
        }

        /*
         * do not invoke
         */
        bool initialize(
                std::tr1::shared_ptr<DeviceDescriptor> deviceDescriptor)
        {
            Assert::dieIf(
                    m_isInitialized, "IWDS runtime already initialized.");

            Assert::dieIf(!deviceDescriptor, "Device descriptor is null.");

            m_deviceDescritpor = deviceDescriptor;

            m_isInitialized = true;

            std::string apiLevel =
                    Property::getProperty("ro.build.version.sdk", "0");
            m_androidApiLevel = ::atoi(apiLevel.data());

            m_isXburstPlatform = Vendor::isXburstPlatform();
            m_isMtkPlatform = Vendor::isMtkPlatform();

            /*
             * Set max binder threads count to 2G
             */
            int success = setMaxBinderThreadCount(0xffffffff &~ (0x1 << 31));
            Assert::dieIf(!success, "Failed to set max binder threads count.");

            return true;
        }

        void dump()
        {
            Assert::dieIf(
                    !m_isInitialized, "IWDS runtime is uninitialized.");

            Log::i("IwdsRuntime", "===== Dump =====");

            Log::i("IwdsRuntime",
                    "Android API level: %d", m_androidApiLevel);

            Log::i("IwdsRuntime",
                    "Xburst platform: %s",
                    m_isXburstPlatform ? "true" : "false");

            Log::i("IwdsRuntime",
                    "MTK platform: %s",
                    m_isMtkPlatform ? "true" : "false");

            Log::i("IwdsRuntime", "================");
        }


    private:
        IwdsRuntime() :
            m_isInitialized(false),
            m_androidApiLevel(-1),
            m_isXburstPlatform(false),
            m_isMtkPlatform(false)
        {

        }

        bool setMaxBinderThreadCount(size_t maxCount)
        {
            DIR *dir= opendir("/proc/self/fd");
            if (dir == NULL) {
                /*
                 * Failed
                 */
                return false;
            }

            struct dirent *dirp;
            while ((dirp = readdir(dir)) != NULL) {
                if (!(dirp->d_type & DT_LNK))
                    continue;

                std::string path = std::string("/proc/self/fd/") + dirp->d_name;

                char name[256];
                int size = readlink(path.c_str(), name, 256);
                if (size < 0)
                    continue;

                name[size] = 0;
                if (strcmp(name, "/dev/binder") == 0) {
                    /*
                     * matched
                     */

                    /*
                     * IOCTL code here:
                     *
                     * #define BINDER_SET_MAX_THREADS          _IOW('b', 5, size_t)
                     */
                    int fd = atoi(dirp->d_name);
                    int error;

                    if (sizeof(size_t) == 8) {
                        /*
                         * i am confused about why calculate a wrong
                         * magic number from ndk _IOW macro of AArch 64,
                         * so hacking out the correct magic number from
                         * deassemble here:
                         *
                         * 0000000000032998 <_ZN7android12ProcessState27setThreadPoolMaxThreadCountEm>:
                         * 32998:   a9bd7bfd    stp x29, x30, [sp,#-48]!
                         * 3299c:   910003fd    mov x29, sp
                         * 329a0:   f9000bf3    str x19, [sp,#16]
                         * 329a4:   9100c3a2    add x2, x29, #0x30
                         * 329a8:   52800013    mov w19, #0x0                       // #0
                         * 329ac:   b9400800    ldr w0, [x0,#8]
                         * 329b0:   f81f8c41    str x1, [x2,#-8]!
                         * 329b4:   528c40a1    mov w1, #0x6205                 // #25093
                         * 329b8:   72a80081    movk    w1, #0x4004, lsl #16
                         * 329bc:   97ffb215    bl  1f210 <ioctl@plt>
                         * 329c0:   3100041f    cmn w0, #0x1
                         * 329c4:   540000a0    b.eq    329d8 <_ZN7android12ProcessState27setThreadPoolMaxThreadCountEm+0x40>
                         */
                        error = ioctl(fd, 0x40046205, &maxCount);
                    } else {
                        error = ioctl(fd, _IOW('b', 5, size_t), &maxCount);
                    }

                    if (error)
                        break;

                    closedir(dir);

                    /*
                     * Success
                     */
                    return true;
                }
            }

            closedir(dir);

            /*
             * Failed
             */
            return false;
        }

        bool m_isInitialized;
        std::tr1::shared_ptr<DeviceDescriptor> m_deviceDescritpor;

        /*
         * Elements
         */
        int m_androidApiLevel;

        bool m_isXburstPlatform;
        bool m_isMtkPlatform;
    };

}

#endif
