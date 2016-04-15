#include <common.h>
#include <view.h>
#include <set_key_value.h>

const struct key_value_param view_params[] = {
	KEY_VALUE_DEF(struct view, center_hor, "center-hor", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, center_ver, "center-ver", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, follow_mode, "follow-mode", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, replace_mode, "replace-mode", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, replace_color, "replace-color", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, show, "show", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, level, "level", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, start.x, "start-x", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct view, start.y, "start-y", KEY_TYPE_INT),
};

const struct key_value_param pic_view_params[] = {
	KEY_VALUE_DEF(struct pic_view, pic_name, "picture", KEY_TYPE_STR),
};

const struct key_value_param flash_pic_view_params[] = {
	KEY_VALUE_DEF(struct num_view, num, "num", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct num_view, grp_name, "pic_grp", KEY_TYPE_STR),
};

const struct key_value_param num_view_params[] = {
	KEY_VALUE_DEF(struct num_view, num, "num", KEY_TYPE_INT),
	KEY_VALUE_DEF(struct num_view, grp_name, "pic_grp", KEY_TYPE_STR),
};

const struct key_value_param text_view_params[] = {

};

const struct key_value_param digital_clock_cn_view_params[] = {

};

const struct key_value_param digital_clock_en_view_params[] = {

};

const struct key_value_param analog_clock_view_params[] = {

};

static inline int slpt_set_pic_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, pic_view_params, ARRAY_SIZE(pic_view_params));
}

static inline int slpt_set_num_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, num_view_params, ARRAY_SIZE(num_view_params));
}

static inline int slpt_set_flash_pic_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, flash_pic_view_params, ARRAY_SIZE(flash_pic_view_params));
}

static inline int slpt_set_text_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, text_view_params, ARRAY_SIZE(text_view_params));
}

static inline int slpt_set_digital_clock_cn_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, digital_clock_cn_view_params,
                         ARRAY_SIZE(digital_clock_cn_view_params));
}

static inline int slpt_set_digital_clock_en_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, digital_clock_en_view_params, ARRAY_SIZE(digital_clock_en_view_params));
}

static inline int slpt_set_analog_clock_view(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, analog_clock_view_params, ARRAY_SIZE(analog_clock_view_params));
}

static inline int slpt_set_view_common(struct view *view, const char *key, void *valp) {
	return set_key_value(view, key, valp, view_params, ARRAY_SIZE(view_params));
}

static inline int slpt_set_view(struct view *view, const char *key, void *valp) {
	int ret;

	if (!slpt_set_view_common(view, key, valp))
		return 0;

	switch (view_type(view)) {
	case VIEW_NUM: ret = slpt_set_num_view(view, key, valp); break;
	case VIEW_FLASH_PIC: ret = slpt_set_flash_pic_view(view, key, valp); break;
	case VIEW_PIC: ret = slpt_set_pic_view(view, key, valp); break;
	case VIEW_TEXT: ret = slpt_set_text_view(view, key, valp); break;
	case VIEW_DIGITAL_CLOCK_EN: ret = slpt_set_digital_clock_en_view(view, key, valp); break;
	case VIEW_DIGITAL_CLOCK_CN: ret = slpt_set_digital_clock_cn_view(view, key, valp); break;
	case VIEW_ANALOG_CLOCK: ret = slpt_set_analog_clock_view(view, key, valp); break;
	default: assert(0);
	}

	return ret;
}
