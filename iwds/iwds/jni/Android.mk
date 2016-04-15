#
# Copyright (C) 2014 Ingenic Semiconductor
#
# SunWenZhong(Fighter) <wzsun@ingenic.com, wanmyqawdr@126.com>
#
# Elf/IDWS Project
#
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2 of the License, or (at your
# option) any later version.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 675 Mass Ave, Cambridge, MA 02139, USA.
#
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#
# SafeParcel library
#
COMPILER_COMMON_FLAGS := -Wall                                          \
                         -fdiagnostics-color=always

COMPILER_CPP_FLAGS := -std=c++11

COMPILER_COMMON_FLAGS += -Isrc/include

LOCAL_CFLAGS := $(COMPILER_COMMON_FLAGS)
LOCAL_CPPFLAGS := $(COMPILER_COMMON_FLAGS) $(COMPILER_CPP_FLAGS)

LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := src/safeparcel/safeparcel.cpp                        \
                   src/safeparceljni.cpp                                \
                   src/utils/exception.cpp                              \
                   src/utils/log.cpp

LOCAL_C_INCLUDES := $(KERNEL_HEADERS)

LOCAL_MODULE := safeparcel

include $(BUILD_SHARED_LIBRARY)

#
# slpt-linux library
#
include $(CLEAR_VARS)

COMPILER_COMMON_FLAGS := -Wall                                          \
                         -fdiagnostics-color=always

SLPT_BUILD_TARGET := iwds

COMPILER_COMMON_FLAGS += -Isrc/slpt/libslpt/include

LOCAL_CFLAGS := $(COMPILER_COMMON_FLAGS)

LOCAL_LDLIBS:= -llog -lm -ljnigraphics

LOCAL_C_INCLUDES := $(KERNEL_HEADERS)

include $(LOCAL_PATH)/src/slpt/libslpt/Android.mk

LOCAL_MODULE := slpt-linux

include $(BUILD_SHARED_LIBRARY)

#
# iwds library
#
include $(CLEAR_VARS)

######################################################################
# Toolschain flags
######################################################################

#
# Compiler flags
#
COMPILER_COMMON_FLAGS := -Wall                                       \
                         -fdiagnostics-color=always

COMPILER_CPP_FLAGS := -std=c++11

#
# Header files search paths
#
COMPILER_COMMON_FLAGS += -Isrc/include

LOCAL_CFLAGS := $(COMPILER_COMMON_FLAGS)
LOCAL_CPPFLAGS := $(COMPILER_COMMON_FLAGS) $(COMPILER_CPP_FLAGS)

#
# Linker flags
#
LOCAL_LDLIBS := -llog -landroid -pthread -ldl

######################################################################
# Sources
######################################################################

#
# Utils library
#
LOCAL_SRC_FILES := src/iwds.cpp                                      \
                   src/utils/log.cpp                                 \
                   src/utils/thread.cpp                              \
                   src/utils/timer.cpp                               \
                   src/utils/vendor.cpp                              \
                   src/utils/crc32.cpp                               \
                   src/utils/crc16.cpp                               \
                   src/utils/android/property.cpp

#
# Android HAL support
#
LOCAL_SRC_FILES += src/androidhal/hardware.cpp

#
# Uniconnect library
#
LOCAL_SRC_FILES += src/uniconnect/uniconnect.cpp                     \
                   src/uniconnect/connection/connectionmanager.cpp   \
                   src/uniconnect/connection/connection.cpp          \
                   src/uniconnect/link/linkmanager.cpp               \
                   src/uniconnect/link/link.cpp                      \
                   src/uniconnect/link/client.cpp                    \
                   src/uniconnect/link/server.cpp                    \
                   src/uniconnect/link/reader.cpp                    \
                   src/uniconnect/link/writer.cpp                    \
                   src/uniconnect/drivers/datachannel.cpp            \
                   src/uniconnect/drivers/bt/ble/bledatachannel.cpp            \
                   src/uniconnect/drivers/bt/android/androidbtdatachannel.cpp  \
                   src/uniconnect/drivers/bt/android/androidbtapi.cpp          \
                   src/uniconnect/drivers/bt/android/androidbtclasses.cpp      \
                   src/uniconnect/drivers/bt/android/androidmtkbtapi.cpp
#
# Smartsense library
#
LOCAL_SRC_FILES += src/smartsense/sensormanager.cpp                    \
                   src/smartsense/sensoreventcallback.cpp              \
                   src/smartsense/hal/sensordevice.cpp                 \
                   src/smartsense/sensors/sensor.cpp

#
# Smart vibrate library
#
LOCAL_SRC_FILES += src/smartvibrate/vibrate.cpp

#
# IWDS JNI entry
#
LOCAL_SRC_FILES += src/main.cpp                                      \
                   src/runtime.cpp                                   \
                   src/uniconnectjni.cpp                             \
                   src/smartsensejni.cpp                             \
                   src/slptjni.cpp                                   \
                   src/smartvibratejni.cpp

LOCAL_C_INCLUDES := $(KERNEL_HEADERS)

LOCAL_MODULE:= libiwds

include $(BUILD_SHARED_LIBRARY)
