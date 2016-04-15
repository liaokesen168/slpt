#ifndef _SVIEW_UTILS_H_
#define _SVIEW_UTILS_H_
#ifdef __cplusplus
extern "C" {
#endif

#include <sview/sview_base.h>

struct key_reader;

extern const char *sview_type_strings[SVIEW_NUMS];
extern const char *sview_type_short_strings[SVIEW_NUMS];

static inline const char *sview_type_to_string(unsigned int type) {
	return type < SVIEW_NUMS ? sview_type_strings[type] : "INVALID_TYPE";
}

static inline const char *sview_type_to_short_string(unsigned int type) {
	return type < SVIEW_NUMS ? sview_type_short_strings[type] : "invalid_type";
}

extern struct sview *create_sview_from_key_reader(struct key_reader *reader);

extern struct key_writer *write_sview_to_key_writer(struct sview *view);

#ifdef __cplusplus
}
#endif
#endif /* _SVIEW_UTILS_H_ */
