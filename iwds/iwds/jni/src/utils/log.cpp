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


#include <utils/log.h>

namespace Iwds
{
    bool Log::sm_isDebugEnabled = false;
    void Log::setDebugEnabled(bool enable)
    {
        Log::sm_isDebugEnabled = enable;

        if (enable)
            i("Log", "Enable debug.");
        else
            i("Log", "Disable debug.");
    }

    bool Log::isDebugEnabled()
    {
        return sm_isDebugEnabled;
    }

    void Log::d(const std::string &tag, const std::string &message)
    {
        if (!Log::sm_isDebugEnabled)
            return;

        Log::d(tag.c_str(), "%s", message.c_str());
    }

    void Log::v(const std::string &tag, const std::string &message)
    {
        Log::v(tag.c_str(), "%s", message.c_str());
    }

    void Log::i(const std::string &tag, const std::string &message)
    {
        Log::i(tag.c_str(), "%s", message.c_str());
    }

    void Log::w(const std::string &tag, const std::string &message)
    {
        Log::w(tag.c_str(), "%s", message.c_str());
    }

    void Log::e(const std::string &tag, const std::string &message)
    {
        Log::e(tag.c_str(), "%s", message.c_str());
    }

    void Log::d(const char *tag, const char *fmt, ...)
    {
        if (!sm_isDebugEnabled)
            return;

        char tag_buffer[LOG_BUF_SIZE];

        sprintf(tag_buffer, "%s%s", LOG_TAG_PREFIX, tag);

        va_list ap;
        char buf[LOG_BUF_SIZE];

        va_start(ap, fmt);
        vsnprintf(buf, LOG_BUF_SIZE, fmt, ap);
        va_end(ap);

        __android_log_write(ANDROID_LOG_DEBUG, tag_buffer, buf);
    }

    void Log::v(const char *tag, const char *fmt, ...)
    {
        char tag_buffer[LOG_BUF_SIZE];

        sprintf(tag_buffer, "%s%s", LOG_TAG_PREFIX, tag);

        va_list ap;
        char buf[LOG_BUF_SIZE];

        va_start(ap, fmt);
        vsnprintf(buf, LOG_BUF_SIZE, fmt, ap);
        va_end(ap);

        __android_log_write(ANDROID_LOG_VERBOSE, tag_buffer, buf);
    }

    void  Log::i(const char *tag, const char *fmt, ...)
    {
        char tag_buffer[LOG_BUF_SIZE];

        sprintf(tag_buffer, "%s%s", LOG_TAG_PREFIX, tag);

        va_list ap;
        char buf[LOG_BUF_SIZE];

        va_start(ap, fmt);
        vsnprintf(buf, LOG_BUF_SIZE, fmt, ap);
        va_end(ap);

        __android_log_write(ANDROID_LOG_INFO, tag_buffer, buf);
    }

    void Log::w(const char *tag, const char *fmt, ...)
    {
        char tag_buffer[LOG_BUF_SIZE];

        sprintf(tag_buffer, "%s%s", LOG_TAG_PREFIX, tag);

        va_list ap;
        char buf[LOG_BUF_SIZE];

        va_start(ap, fmt);
        vsnprintf(buf, LOG_BUF_SIZE, fmt, ap);
        va_end(ap);

        __android_log_write(ANDROID_LOG_WARN, tag_buffer, buf);
    }

    void Log::e(const char *tag, const char *fmt, ...)
    {
        char tag_buffer[LOG_BUF_SIZE];

        sprintf(tag_buffer, "%s%s", LOG_TAG_PREFIX, tag);

        va_list ap;
        char buf[LOG_BUF_SIZE];

        va_start(ap, fmt);
        vsnprintf(buf, LOG_BUF_SIZE, fmt, ap);
        va_end(ap);

        __android_log_write(ANDROID_LOG_ERROR, tag_buffer, buf);
    }
}
