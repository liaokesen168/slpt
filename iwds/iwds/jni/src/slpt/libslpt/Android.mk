ifneq ($(SLPT_BUILD_TARGET), iwds)
SLPT_BUILD_TARGET = shared_library
# SLPT_BUILD_TARGET = executable
endif

ifeq ($(SLPT_BUILD_TARGET), iwds)
SLPT_DIR = src/slpt/libslpt
LOCAL_CFLAGS += -DCONFIG_SLPT_LINUX_SHARED_LIBRARY
endif

ifeq ($(SLPT_BUILD_TARGET), shared_library)
SLPT_DIR = .
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_CFLAGS = -Wall -Wno-unused-parameter -UNDEBUG -D_DEBUG -g
LOCAL_CFLAGS += -DCONFIG_SLPT_LINUX_SHARED_LIBRARY
endif

ifeq ($(SLPT_BUILD_TARGET), executable)
SLPT_DIR = .
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_CFLAGS = -Wall -Wno-unused-parameter -UNDEBUG -D_DEBUG -g
LOCAL_CFLAGS += -DCONFIG_SLPT_LINUX_EXECUTABLE
endif


# do not use bmp file, use fb_region directly to save memory
LOCAL_CFLAGS += -DCONFIG_PICTURE_NO_BMP

# main.c
LOCAL_SRC_FILES += \
	${SLPT_DIR}/main.c

# app dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/app/display_test.c \
	${SLPT_DIR}/app/lcd_density.c \
	${SLPT_DIR}/app/set_timezone.c \
	${SLPT_DIR}/app/simple_dirname.c \
	${SLPT_DIR}/app/slpt_get_view_value.c \
	${SLPT_DIR}/app/slpt_print_view_value.c \
	${SLPT_DIR}/app/slpt_set_pic.c \
	${SLPT_DIR}/app/slpt_set_view_value.c \
	${SLPT_DIR}/app/clear_fb.c \
	${SLPT_DIR}/app/find_pictures_test.c \
	${SLPT_DIR}/app/slpt_load_fw.c

# fb dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/fb/fb_always_on.c \
	${SLPT_DIR}/fb/brightness_always_on.c \
	${SLPT_DIR}/fb/lcd_power_on.c \
	${SLPT_DIR}/fb/display_ctrl.c \
	${SLPT_DIR}/fb/lock_fb_pan_display.c \
	${SLPT_DIR}/fb/sysfs_pan_display.c \
	${SLPT_DIR}/fb/fb.c

# misc dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/misc/call_app.c \
	${SLPT_DIR}/misc/time.c

# slpt dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/slpt/slpt.c \
	${SLPT_DIR}/slpt/slpt_default_pictures.c \
	${SLPT_DIR}/slpt/slpt_display.c \
	${SLPT_DIR}/slpt/slpt_file.c \
	${SLPT_DIR}/slpt/slpt_load_view.c \
	${SLPT_DIR}/slpt/slpt_sync_setting.c \
	${SLPT_DIR}/slpt/slpt_write_view.c \
	${SLPT_DIR}/slpt/slpt_find_pictures.c \
	${SLPT_DIR}/slpt/slpt_ioctl.c

# utils dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/utils/string.c \
	${SLPT_DIR}/utils/arg_parse.c \
	${SLPT_DIR}/utils/malloc_with_name.c \
	${SLPT_DIR}/utils/file_ops.c \
	${SLPT_DIR}/utils/item_parser.c \
	${SLPT_DIR}/utils/key_reader.c \
	${SLPT_DIR}/utils/key_writer.c

# video dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/video/bmp_gen.c \
	${SLPT_DIR}/video/lcd_area_ops.c \
	${SLPT_DIR}/video/sin_cos_datas.c \
	${SLPT_DIR}/video/lcd_color_rotate.c \
	${SLPT_DIR}/video/lcd_region_rotate.c \
	${SLPT_DIR}/video/color_map.c \
	${SLPT_DIR}/video/color_map_save.c \
	${SLPT_DIR}/video/lcd_draw.c \
	${SLPT_DIR}/video/font_library.c

#view dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/view/num_view.c \
	${SLPT_DIR}/view/flash_pic_view.c \
	${SLPT_DIR}/view/picture.c \
	${SLPT_DIR}/view/picture_header.c \
	${SLPT_DIR}/view/pic_view.c \
	${SLPT_DIR}/view/rotate.c \
	${SLPT_DIR}/view/rotate_pic.c \
	${SLPT_DIR}/view/text_view.c \
	${SLPT_DIR}/view/view.c \
	${SLPT_DIR}/view/view_grp.c \
	${SLPT_DIR}/view/view_utils.c

#clock dir
LOCAL_SRC_FILES := \
	$(LOCAL_SRC_FILES) \
	${SLPT_DIR}/clock/analog_clock.c \
	${SLPT_DIR}/clock/analog_base_clock.c \
	${SLPT_DIR}/clock/analog_week_clock.c \
	${SLPT_DIR}/clock/analog_month_clock.c \
	${SLPT_DIR}/clock/analog_second_clock.c \
	${SLPT_DIR}/clock/analog_minute_clock.c \
	${SLPT_DIR}/clock/analog_hour_clock.c \
	${SLPT_DIR}/clock/background.c \
	${SLPT_DIR}/clock/current_time.c \
	${SLPT_DIR}/clock/time_notify.c \
	${SLPT_DIR}/clock/digital_clock.c \
	${SLPT_DIR}/clock/date_en_view.c \
	${SLPT_DIR}/clock/date_cn_view.c \
	${SLPT_DIR}/clock/week_en_view.c \
	${SLPT_DIR}/clock/week_cn_view.c \
	${SLPT_DIR}/clock/year_en_view.c \
	${SLPT_DIR}/clock/time_view.c \
	${SLPT_DIR}/clock/charge_picture.c

#fft dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/fft/fft.c \
	${SLPT_DIR}/fft/fft_test.c

#sview core dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/sview/core/sview.c \
	${SLPT_DIR}/sview/core/sview_methods.c \
	${SLPT_DIR}/sview/core/sview_grp.c \
	${SLPT_DIR}/sview/core/background.c \
	${SLPT_DIR}/sview/core/pic_sview.c \
	${SLPT_DIR}/sview/core/num_sview.c \
	${SLPT_DIR}/sview/core/linear_layout.c \
	${SLPT_DIR}/sview/core/absolute_layout.c \
	${SLPT_DIR}/sview/core/frame_layout.c \
	${SLPT_DIR}/sview/core/root_sview.c

#sview other
LOCAL_SRC_FILES += \
	${SLPT_DIR}/sview/time/time_num_sview.c \
	${SLPT_DIR}/sview/time/secondL_sview.c \
	${SLPT_DIR}/sview/time/secondH_sview.c \
	${SLPT_DIR}/sview/time/minuteL_sview.c \
	${SLPT_DIR}/sview/time/minuteH_sview.c \
	${SLPT_DIR}/sview/time/hourL_sview.c \
	${SLPT_DIR}/sview/time/hourH_sview.c \
	${SLPT_DIR}/sview/time/dayL_sview.c \
	${SLPT_DIR}/sview/time/dayH_sview.c \
	${SLPT_DIR}/sview/time/week_sview.c \
	${SLPT_DIR}/sview/time/monthL_sview.c \
	${SLPT_DIR}/sview/time/monthH_sview.c \
	${SLPT_DIR}/sview/time/year0_sview.c \
	${SLPT_DIR}/sview/time/year1_sview.c \
	${SLPT_DIR}/sview/time/year2_sview.c \
	${SLPT_DIR}/sview/time/year3_sview.c \
	${SLPT_DIR}/sview/analog/rotate2.c \
	${SLPT_DIR}/sview/analog/rotate_pic_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_time_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_second_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_minute_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_hour_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_day_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_week_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_month_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_am_pm_sview.c \
	${SLPT_DIR}/sview/analog/time/analog_hour_with_minute_sview.c

#sview utils
LOCAL_SRC_FILES += \
	${SLPT_DIR}/sview/utils/sview_type_to_string.c \
	${SLPT_DIR}/sview/utils/create_sview_from_key_reader.c \
	${SLPT_DIR}/sview/utils/write_sview_to_key_writer.c

#jni dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/jni/slpt_jni_picture_method.c \
	${SLPT_DIR}/jni/slpt_jni_sview_method.c \
	${SLPT_DIR}/jni/key_reader_jni.c \
	${SLPT_DIR}/jni/key_writer_jni.c

#display dir
LOCAL_SRC_FILES += \
	${SLPT_DIR}/display/slpt_display_dispatcher.c \
	${SLPT_DIR}/display/power_state.c \
	${SLPT_DIR}/display/display.c

# iwds
ifeq ($(SLPT_BUILD_TARGET), iwds)
LOCAL_LDLIBS:= -llog -lm -ljnigraphics
endif

# shared_library
ifeq ($(SLPT_BUILD_TARGET), shared_library)
LOCAL_MODULE := libslpt-linux

LOCAL_C_INCLUDES += \
$(LOCAL_PATH)/include/ \
$(KERNEL_HEADERS)

LOCAL_SHARED_LIBRARIES += \
	liblog \
	libjnigraphics

LOCAL_LDLIBS:= -llog -lm -ljnigraphics

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

endif

#executable
ifeq ($(SLPT_BUILD_TARGET), executable)
LOCAL_MODULE := slpt-linux-test

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/include/ \
	$(KERNEL_HEADERS)

LOCAL_SHARED_LIBRARIES += \
	libcutils \
	liblog \
	libjnigraphics \
	libhardware_legacy

LOCAL_LDLIBS:= -llog -lm -ljnigraphics

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)
endif
