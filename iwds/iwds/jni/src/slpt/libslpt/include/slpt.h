#ifndef _SLPT_H_
#define _SLPT_H_
#ifdef __cplusplus
extern "C" {
#endif

/* FB defines */
#define MAX_FB_NAME_LEN 20
#define MAX_FB_DEV_NUMS  2


#define SLPT_ROOT_DIR "/sys/slpt"
#define SLPT_DEFAULT_APP "slpt-app"

#define SLPT_FILE_TIMEZONE "clock-zone"
#define SLPT_FILE_CLOCK_TYPE "clock-type"


extern char *slpt_get_resfilename( const char *path, const char *app_name, char *buf);

#ifdef __cplusplus
}
#endif
#endif /* _SLPT_H_ */
