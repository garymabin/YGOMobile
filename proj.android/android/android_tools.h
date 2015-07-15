// This file is part of the "Irrlicht Engine".
// For conditions of distribution and use, see copyright notice in irrlicht.h

#ifndef __IRR_ANDROID_TOOLS_H__
#define __IRR_ANDROID_TOOLS_H__

#include <irrlicht.h>
#include <android_native_app_glue.h>
#include <signal.h>
#include <android/log.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "ygomobile-native", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "ygomobile-native", __VA_ARGS__))

namespace irr {
namespace android {

class InitOptions {
public:
	InitOptions(void* data);
	inline irr::io::path getCacheDir() {
		return m_cache_dir;
	}
	inline irr::io::path getDBDir() {
		return m_db_dir;
	}
	inline irr::io::path getCoreConfigVerDir() {
		return m_core_config_version;
	}
	inline irr::io::path getResPathDir() {
		return m_res_path;
	}
	inline irr::io::path getExternalPathDir() {
		return m_external_path;
	}
	inline int getOpenglVersion() {
		return m_opengles_version;
	}
	inline int getCardQualityOp() {
		return m_card_quality;
	}
	inline bool isFontAntiAliasEnabled() {
		return m_font_aa_enabled;
	}
	inline bool isSoundEffectEnabled() {
		return m_se_enabled;
	}
	inline bool isPendulumScaleEnabled() {
		return m_ps_enabled;
	}
private:
	irr::io::path m_cache_dir;
    irr::io::path m_db_dir;
    irr::io::path m_core_config_version;
    irr::io::path m_res_path;
    irr::io::path m_external_path;
    int m_opengles_version;
    int m_card_quality;
    bool m_font_aa_enabled;
    bool m_se_enabled;
    bool m_ps_enabled;
};

static unsigned char signed_buff[16] = { 0x30, 0x82, 0x2, 0x41, 0x30, 0x82, 0x1,
		0xAA, 0xA0, 0x3, 0x2, 0x1, 0x2, 0x2, 0x4, 0x53 };

struct SDisplayMetrics {
	irr::s32 widthPixels;
	irr::s32 heightPixels;
	irr::f32 density;
	irr::s32 densityDpi;
	irr::f32 scaledDensity;
	irr::f32 xdpi;
	irr::f32 ydpi;
};
/* jni utils*/
// Access SDisplayMetrics
extern float getScreenWidth(android_app* app);

extern float getScreenHeight(android_app* app);

// Get SDCard path.
extern irr::io::path getExternalStorageDir(android_app* app);

// Get SDCard path.
extern irr::io::path getExternalFilesDir(android_app* app);

// Get cache path.
extern irr::io::path getCacheDir(android_app* app);

// Get database path.
extern irr::io::path getDBDir(android_app* app);

// Get global resource path
extern irr::io::path getResourcePath(android_app* app);

// Get card image path.
extern irr::io::path getCardImagePath(android_app* app);

//Toggle IME using global window token.
extern void toggleGlobalIME(android_app* app, bool pShow);

//Toggle IME using android UI trick.
extern void toggleIME(android_app* app, bool pShow, const char* hint);

//Init Java Irrlicht world.
extern void initJavaBridge(android_app* app, void* handle);

//Cause a haptic feedback.
extern void perfromHapticFeedback(android_app* app);

//perform trick
extern bool perfromTrick(android_app* app);

//toogle overlay view
extern void toggleOverlayView(android_app* app, bool pShow);

//Retrive customized resource directory()
extern irr::io::path getCoreConfigVersion(android_app* app);

//Retrive opengl version.
extern int getOpenglVersion(android_app* app);

//Retrive init options
extern InitOptions* getInitOptions(android_app* app);

//Retrive card quality settings.
extern int getCardQuality(android_app* app);

//Retrive local ip address(mostly for wifi only);
extern int getLocalAddr(android_app* app);

//Retrive font path.
extern irr::io::path getFontPath(android_app* app);

//Retrive last deck name.
extern irr::io::path getLastDeck(android_app* app);

//save last deck name.
extern void setLastDeck(android_app* app, const char* deckname);

//Retrive font antialias options
extern bool getFontAntiAlias(android_app* app);

extern bool isSoundEffectEnabled(android_app* app);

//Show Android compat gui;
extern void showAndroidComboBoxCompat(android_app* app, bool pShow,
		char** pContents, int count, int mode = 0);

/* android  event handlers*/
extern void process_input(struct android_app* app,
		struct android_poll_source* source);

extern s32 handleInput(android_app* app, AInputEvent* androidEvent);

extern unsigned char* android_script_reader(const char* script_name, int* slen);

extern bool android_deck_delete(const char* deck_name);

}
}

#endif // __IRR_ANDROID_TOOLS_H__
