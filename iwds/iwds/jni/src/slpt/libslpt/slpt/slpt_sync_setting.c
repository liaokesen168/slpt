#include <sys/time.h>
#include <slpt_file.h>

struct setting {
	struct {
		time_t alpha32;
		time_t clock_period;
	} time;
};

static struct setting setting;

extern unsigned int alpha32;

static inline void slpt_sync_alpha32(void) {
	slpt_load_single_file("setting/alpha32", &alpha32, &setting.time.alpha32, 0);
}

void slpt_sync_settings(void) {
	slpt_sync_alpha32();
}
