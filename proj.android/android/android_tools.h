// This file is part of the "Irrlicht Engine".
// For conditions of distribution and use, see copyright notice in irrlicht.h

#ifndef __IRR_ANDROID_TOOLS_H__
#define __IRR_ANDROID_TOOLS_H__

#include <irrlicht.h>
#include <android_native_app_glue.h>
#include "../gframe/config.h"
#include <signal.h>

namespace irr 
{
namespace android
{

static unsigned char signed_buff[16] = {0x30, 0x82,0x2,0x41,0x30,0x82,0x1,0xAA,0xA0,0x3,0x2,0x1,
		0x2,0x2,0x4,0x53};

struct SDisplayMetrics
{
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
extern bool getDisplayMetrics(android_app* app, SDisplayMetrics & metrics);

// Get SDCard path.
extern irr::io::path getExternalStorageDir(android_app* app);

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

//Retrive user name
extern irr::io::path getUserName(android_app* app);

//Retrive font antialias options
extern bool getFontAntiAlias(android_app* app);

//Show Android compat gui;
extern void showAndroidComboBoxCompat(android_app* app, bool pShow, char** pContents, int count, int mode = 0);

/* android  event handlers*/
extern void process_input( struct android_app* app, struct android_poll_source* source);

extern s32 handleInput(android_app* app, AInputEvent* androidEvent);

}
}

#endif // __IRR_ANDROID_TOOLS_H__
