/*
 *  Copyright (C) 2014 Ingenic Semiconductor
 *
 *  ZhangYanMing <yanming.zhang@ingenic.com, jamincheung@126.com>
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

package com.ingenic.iwds.smartspeech;

/**
 * 语音识别服务错误编码
 */
public class RemoteSpeechErrorCode {

    /**
     * 失败
     */
    public static final int MSP_ERROR_FAIL = -1;

    /**
     * 异常
     */
    public static final int MSP_ERROR_EXCEPTION = -2;

    /**
     * 成功状态
     */
    public static final int SUCCESS = 0;

    /**
     * 基码
     */
    public static final int MSP_ERROR_GENERAL = 10100;

    /**
     * 内存越界
     */
    public static final int MSP_ERROR_OUT_OF_MEMORY = 10101;

    /**
     * 文件没有发现
     */
    public static final int MSP_ERROR_FILE_NOT_FOUND = 10102;

    /**
     * 不支持
     */
    public static final int MSP_ERROR_NOT_SUPPORT = 10103;

    /**
     * 没有实现
     */
    public static final int MSP_ERROR_NOT_IMPLEMENT = 10104;

    /**
     * 没有权限
     */
    public static final int MSP_ERROR_ACCESS = 10105;

    /**
     * 无效的参数
     */
    public static final int MSP_ERROR_INVALID_PARA = 10106;

    /**
     * 无效的参数值
     */
    public static final int MSP_ERROR_INVALID_PARA_VALUE = 10107;

    /**
     * 无效的句柄
     */
    public static final int MSP_ERROR_INVALID_HANDLE = 10108;

    /**
     * 无效的数据
     */
    public static final int MSP_ERROR_INVALID_DATA = 10109;

    /**
     * 没有授权许可
     */
    public static final int MSP_ERROR_NO_LICENSE = 10110;

    /**
     * 没有初始化
     */
    public static final int MSP_ERROR_NOT_INIT = 10111;

    /**
     * 空句柄
     */
    public static final int MSP_ERROR_NULL_HANDLE = 10112;

    /**
     * 溢出
     */
    public static final int MSP_ERROR_OVERFLOW = 10113;

    /**
     * MSC超时
     */
    public static final int ERROR_MSP_TIMEOUT = 10114;

    /**
     * 打开文件出错
     */
    public static final int MSP_ERROR_OPEN_FILE = 10115;

    /**
     * 没有发现
     */
    public static final int MSP_ERROR_NOT_FOUND = 10116;

    /**
     * 没有足够的内存
     */
    public static final int MSP_ERROR_NO_ENOUGH_BUFFER = 10117;

    /**
     * MSC没有数据
     */
    public static final int ERROR_MSP_NO_DATA = 10118;

    /**
     * 没有更多的数据
     */
    public static final int MSP_ERROR_NO_MORE_DATA = 10119;

    /**
     * 已经存在
     */
    public static final int MSP_ERROR_ALREADY_EXIST = 10121;

    /**
     * 加载模块失败
     */
    public static final int MSP_ERROR_LOAD_MODULE = 10122;

    /**
     * 忙碌
     */
    public static final int MSP_ERROR_BUSY = 10123;

    /**
     * 无效的配置项
     */
    public static final int MSP_ERROR_INVALID_CONFIG = 10124;

    /**
     * 版本错误
     */
    public static final int MSP_ERROR_VERSION_CHECK = 10125;

    /**
     * 取消
     */
    public static final int MSP_ERROR_CANCELED = 10126;

    /**
     * 无效的媒体类型
     */
    public static final int MSP_ERROR_INVALID_MEDIA_TYPE = 10127;

    /**
     * 初始化Config实例
     */
    public static final int MSP_ERROR_CONFIG_INITIALIZE = 10128;

    /**
     * 建立句柄
     */
    public static final int MSP_ERROR_CREATE_HANDLE = 10129;

    /**
     * 编解码库未加载
     */
    public static final int MSP_ERROR_CODING_LIB_NOT_LOAD = 10130;

    /**
     * 网络一般错误
     */
    public static final int MSP_ERROR_NET_GENERAL = 10200;

    /**
     * 打开套接字
     */
    public static final int MSP_ERROR_NET_OPENSOCK = 10201;

    /**
     * 套接字连接
     */
    public static final int MSP_ERROR_NET_CONNECTSOCK = 10202;

    /**
     * 套接字接收
     */
    public static final int MSP_ERROR_NET_ACCEPTSOCK = 10203;

    /**
     * 发送
     */
    public static final int MSP_ERROR_NET_SENDSOCK = 10204;

    /**
     * 接收
     */
    public static final int MSP_ERROR_NET_RECVSOCK = 10205;

    /**
     * 无效的套接字
     */
    public static final int MSP_ERROR_NET_INVALIDSOCK = 10206;

    /**
     * 无效的地址
     */
    public static final int MSP_ERROR_NET_BADADDRESS = 10207;

    /**
     * 绑定次序
     */
    public static final int MSP_ERROR_NET_BINDSEQUENCE = 10208;

    /**
     * 套接字没有打开
     */
    public static final int MSP_ERROR_NET_NOTOPENSOCK = 10209;

    /**
     * 没有绑定
     */
    public static final int MSP_ERROR_NET_NOTBIND = 10210;

    /**
     * 没有监听
     */
    public static final int MSP_ERROR_NET_NOTLISTEN = 10211;

    /**
     * 连接关闭
     */
    public static final int MSP_ERROR_NET_CONNECTCLOSE = 10212;

    /**
     * 非数据报套接字
     */
    public static final int MSP_ERROR_NET_NOTDGRAMSOCK = 10213;

    /**
     * MSC DNS解析失败
     */
    public static final int ERROR_MSP_DNS = 10214;

    /**
     * 录音失败
     */
    public static final int ERROR_RECODER = 800001;

    /**
     * MSC会话ID缺失
     */
    public static final int ERROR_NOT_SID = 800002;
    // public static final int ERROR_NO_MATCH = 800003;

    /**
     * 网络超时
     */
    public static final int ERROR_NET_TIMEOUT = 800004;

    /**
     * 引擎初始化失败
     */
    public static final int ERROR_SPEECH_INIT = 800005;

    /**
     * 服务器异常
     */
    public static final int ERROR_SERVER_AUTHO = 800006;

    /**
     * 识别状态错误
     */
    public static final int ERROR_STATUS = 800007;

    /**
     * 网络异常
     */
    public static final int ERROR_NETWORK = 800008;

    /**
     * 服务端异常
     */
    public static final int ERROR_SERVER_EXPECTION = 800009;

    /**
     * 没有录音数据
     */
    public static final int ERROR_NO_DATA = 800010;

    /**
     * 本地识别引擎忙
     */
    public static final int ERROR_AITALK_BUSY = 800011;
    // public static final int ERROR_SPEECH_TIMEOUT = 800012;

    /**
     * 识别响应超时
     */
    public static final int ERROR_RESPONSE_TIMEOUT = 800013;

    /**
     * 本地识别引擎错误
     */
    public static final int ERROR_AITALK = 800014;

    /**
     * 本地识别引擎资源错误
     */
    public static final int ERROR_AITALK_RES = 800016;

    /**
     * 联系人查询错误
     */
    public static final int ERROR_QUERY_CONTACT = 800017;

    /**
     * 识别参数错误
     */
    public static final int ERROR_RECO_PARAM = 800018;

    /**
     * MSC识别参数错误
     */
    public static final int ERROR_MSC_PARAM = 800019;

    /**
     * 本地识别引擎参数错误
     */
    public static final int ERROR_AITALK_PARAM = 800020;

    /**
     * MSC结果错误
     */
    public static final int ERROR_MSC_RESULT = 800021;

    /**
     * MSC没有识别结果
     */
    public static final int ERROR_MSC_NO_RESULT = 800022;

    /**
     * 联系人不存在
     */
    public static final int ERROR_CONTACT_NOT_EXIST = 800023;

    /**
     * 在线合成超时
     */
    public static final int ERROR_MSC_TTS_TIME_OUT = 800024;

    /**
     * 没有获取到用户ID
     */
    public static final int ERROR_NO_OSSP_UID = 800025;

    /**
     * 取消识别失败
     */
    public static final int ERROR_CANCEL_RECO = 800026;

    /**
     * 没有匹配的结果
     */
    public static final int ERROR_NO_FILTER_RESULT = 800027;

    /**
     * 本地合成引擎没有资源
     */
    public static final int ERROR_AISOUND_NO_RES = 800041;

    /**
     * 本地合成引擎未初始化
     */
    public static final int ERROR_AISOUND_NO_INIT = 800042;

    /**
     * 本地合成引擎参数错误
     */
    public static final int ERROR_AISOUND_PARAM = 800043;

    /**
     * 本地识别引擎未初始化
     */
    public static final int ERROR_AITALK_NO_INIT = 800050;

    /**
     * 本地识别引擎初始化中
     */
    public static final int ERROR_AITALK_INITING = 800051;

    /**
     * 本地识别引擎库错误
     */
    public static final int ERROR_AITALK_LIBRARY = 800052;

    /**
     * 本地识别场景错误
     */
    public static final int ERROR_AITALK_FOCUS = 800053;

    /**
     * 识别对象为空
     */
    public static final int ASRRECOGNIZER_IS_NULL = 801005;

    /**
     * 识别消息队列添加失败
     */
    public static final int ADD_MESSAGE_FAILE = 801006;

    /**
     * 识别状态错误
     */
    public static final int ASRRECOGNIZER_STATES_WRONG = 801007;

    /**
     * 识别消息队列为空
     */
    public static final int MESSAGE_PROCESS_NULL = 801008;

    /**
     * 网络未连接
     */
    public static final int NETWORK_NOT_AVAILABLE = 801009;

    /**
     * 其他类型错误
     */
    public static final int OTHER_TYPE_ERROR = 801999;

    /**
     * 无有效的网络连接
     */
    public static final int ERROR_NO_NETWORK = 20001;

    /**
     * 网络连接超时
     */
    public static final int ERROR_NETWORK_TIMEOUT = 20002;

    /**
     * 网络异常
     */
    public static final int ERROR_NET_EXPECTION = 20003;

    /**
     * 无有效的结果
     */
    public static final int ERROR_INVALID_RESULT = 20004;

    /**
     * 无匹配结果
     */
    public static final int ERROR_NO_MATCH = 20005;

    /**
     * 录音失败
     */
    public static final int ERROR_AUDIO_RECORD = 20006;

    /**
     * 未检测到语音
     */
    public static final int ERROR_NO_SPPECH = 20007;

    /**
     * 音频输入超时
     */
    public static final int ERROR_SPEECH_TIMEOUT = 20008;

    /**
     * 无效的文本输入
     */
    public static final int ERROR_EMPTY_UTTERANCE = 20009;

    /**
     * 文件读写失败
     */
    public static final int ERROR_FILE_ACCESS = 20010;

    /**
     * 音频播放失败
     */
    public static final int ERROR_PLAY_MEDIA = 20011;

    /**
     * 无效的参数
     */
    public static final int ERROR_INVALID_PARAM = 20012;

    /**
     * 文本溢出
     */
    public static final int ERROR_TEXT_OVERFLOW = 20013;

    /**
     * 无效数据
     */
    public static final int ERROR_INVALID_DATA = 20014;

    /**
     * 用户未登陆
     */
    public static final int ERROR_LOGIN = 20015;

    /**
     * 无效授权
     */
    public static final int ERROR_PERMISSION_DENIED = 20016;

    /**
     * 被异常打断
     */
    public static final int ERROR_INTERRUPT = 20017;

    /**
     * 未知错误
     */
    public static final int ERROR_UNKNOWN = 20999;

    /**
     * 没有安装语音组件
     */
    public static final int ERROR_COMPONENT_NOT_INSTALLED = 21001;

    /**
     * 引擎不支持
     */
    public static final int ERROR_ENGINE_NOT_SUPPORTED = 21002;

    /**
     * 初始化失败
     */
    public static final int ERROR_ENGINE_INIT_FAIL = 21003;

    /**
     * 调用失败
     */
    public static final int ERROR_ENGINE_CALL_FAIL = 21004;

    /**
     * 引擎繁忙
     */
    public static final int ERROR_ENGINE_BUSY = 21005;

    /**
     * 本地引擎未初始化
     */
    public static final int ERROR_LOCAL_NO_INIT = 22001;

    /**
     * 本地引擎无资源
     */
    public static final int ERROR_LOCAL_RESOURCE = 22002;

    /**
     * 本地引擎内部错误
     */
    public static final int ERROR_LOCAL_ENGINE = 22003;

    /**
     * 本地唤醒引擎被异常打断
     */
    public static final int ERROR_IVW_INTERRUPT = 22004;

    /**
     * 版本过低
     */
    public static final int ERROR_VERSION_LOWER = 22005;

    /**
     * 本地识别语法不存在
     */
    public static final int ERROR_LOCAL_GRAMMAR = 22006;

    /**
     * 远端设备连接断开
     */
    public static final int ERROR_REMOTE_DISCONNECTED = 30000;

    /**
     * 正在录音，状态繁忙
     */
    public static final int ERROR_AUDIO_RECORDER_BUSY = 30001;

    /**
     * 录音初始化失败
     */
    public static final int ERROR_INIT_RECORDER = 30002;

    /**
     * 录音启动失败
     */
    public static final int ERROR_START_RECORDER = 30003;

    /**
     * 录音复位失败
     */
    public static final int ERROR_RESET_RECORDER = 30004;

    /**
     * 录音停止失败
     */
    public static final int ERROR_STOP_RECORDER = 30005;

    /**
     * 服务客户端调用错误
     */
    public static final int ERROR_CLIENT = 30006;

    /**
     * 非法状态错误
     */
    public static final int ERROR_ILLEGAL_STATE = 30007;

    /**
     * 播放初始化失败
     */
    public static final int ERROR_INIT_TRACKER = 30008;

    /**
     * 播放启动失败
     */
    public static final int ERROR_START_TRACKER = 30009;

    /**
     * 播放暂停失败
     */
    public static final int ERROR_PAUSE_TRACKER = 30010;

    /**
     * 播放恢复失败
     */
    public static final int ERROR_RESUME_TRACKER = 30011;

    /**
     * 音频播放失败
     */
    public static final int ERROR_WRITE_TRACKER = 30012;

    /**
     * 正在播放，状态繁忙
     */
    public static final int ERROR_AUDIO_TRACKER_BUSY = 30013;

    /**
     * 远端设备服务被异常结束
     */
    public static final int ERROR_REMOTE_SERVICE_KILLED = 30014;
}
