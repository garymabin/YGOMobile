// This file is part of the "Irrlicht Engine".
// For conditions of distribution and use, see copyright notice in irrlicht.h

#include "android_tools.h"
#include "../gframe/game.h"

namespace irr {
namespace android {

static unsigned char script_buffer[0x10000];

// Not all DisplayMetrics are available through the NDK. 
// So we access the Java classes with the JNI interface.
// You can access other Java classes available in Android in similar ways.
// Function based roughly on the code from here: http://stackoverflow.com/questions/13249164/android-using-jni-from-nativeactivity
bool getDisplayMetrics(android_app* app, SDisplayMetrics & metrics) {
	if (!app || !app->activity || !app->activity->vm)
		return false;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return false;

	// get all the classes we want to access from the JVM
	jclass classNativeActivity = jni->FindClass("android/app/NativeActivity");
	jclass classWindowManager = jni->FindClass("android/view/WindowManager");
	jclass classDisplay = jni->FindClass("android/view/Display");
	jclass classDisplayMetrics = jni->FindClass("android/util/DisplayMetrics");

	if (!classNativeActivity || !classWindowManager || !classDisplay
			|| !classDisplayMetrics) {
		app->activity->vm->DetachCurrentThread();
		return false;
	}

	// Get all the methods we want to access from the JVM classes
	// Note: You can get the signatures (third parameter of GetMethodID) for all 
	// functions of a class with the javap tool, like in the following example for class DisplayMetrics:
	// javap -s -classpath myandroidpath/adt-bundle-linux-x86_64-20131030/sdk/platforms/android-10/android.jar android/util/DisplayMetrics
	jmethodID idNativeActivity_getWindowManager = jni->GetMethodID(
			classNativeActivity, "getWindowManager",
			"()Landroid/view/WindowManager;");
	jmethodID idWindowManager_getDefaultDisplay = jni->GetMethodID(
			classWindowManager, "getDefaultDisplay",
			"()Landroid/view/Display;");
	jmethodID idDisplayMetrics_constructor = jni->GetMethodID(
			classDisplayMetrics, "<init>", "()V");
	jmethodID idDisplay_getMetrics = jni->GetMethodID(classDisplay,
			"getMetrics", "(Landroid/util/DisplayMetrics;)V");

	if (!idNativeActivity_getWindowManager || !idWindowManager_getDefaultDisplay
			|| !idDisplayMetrics_constructor || !idDisplay_getMetrics) {
		app->activity->vm->DetachCurrentThread();
		return false;
	}

	// In Java the following code would be: getWindowManager().getDefaultDisplay().getMetrics(metrics);
	// Note: If you need to call java functions in time-critical places you can split getting the jmethodID's 
	// and calling the functions into separate functions as you only have to get the jmethodID's once.
	jobject windowManager = jni->CallObjectMethod(app->activity->clazz,
			idNativeActivity_getWindowManager);

	if (!windowManager) {
		app->activity->vm->DetachCurrentThread();
		return false;
	}
	jobject display = jni->CallObjectMethod(windowManager,
			idWindowManager_getDefaultDisplay);
	if (!display) {
		app->activity->vm->DetachCurrentThread();
		return false;
	}
	jobject displayMetrics = jni->NewObject(classDisplayMetrics,
			idDisplayMetrics_constructor);
	if (!displayMetrics) {
		app->activity->vm->DetachCurrentThread();
		return false;
	}
	jni->CallVoidMethod(display, idDisplay_getMetrics, displayMetrics);

	// access the fields of DisplayMetrics (we ignore the DENSITY constants)
	jfieldID idDisplayMetrics_widthPixels = jni->GetFieldID(classDisplayMetrics,
			"widthPixels", "I");
	jfieldID idDisplayMetrics_heightPixels = jni->GetFieldID(
			classDisplayMetrics, "heightPixels", "I");
	jfieldID idDisplayMetrics_density = jni->GetFieldID(classDisplayMetrics,
			"density", "F");
	jfieldID idDisplayMetrics_densityDpi = jni->GetFieldID(classDisplayMetrics,
			"densityDpi", "I");
	jfieldID idDisplayMetrics_scaledDensity = jni->GetFieldID(
			classDisplayMetrics, "scaledDensity", "F");
	jfieldID idDisplayMetrics_xdpi = jni->GetFieldID(classDisplayMetrics,
			"xdpi", "F");
	jfieldID idDisplayMetrics_ydpi = jni->GetFieldID(classDisplayMetrics,
			"ydpi", "F");

	if (idDisplayMetrics_widthPixels)
		metrics.widthPixels = jni->GetIntField(displayMetrics,
				idDisplayMetrics_widthPixels);
	if (idDisplayMetrics_heightPixels)
		metrics.heightPixels = jni->GetIntField(displayMetrics,
				idDisplayMetrics_heightPixels);
	if (idDisplayMetrics_density)
		metrics.density = jni->GetFloatField(displayMetrics,
				idDisplayMetrics_density);
	if (idDisplayMetrics_densityDpi)
		metrics.densityDpi = jni->GetIntField(displayMetrics,
				idDisplayMetrics_densityDpi);
	if (idDisplayMetrics_scaledDensity)
		metrics.scaledDensity = jni->GetFloatField(displayMetrics,
				idDisplayMetrics_scaledDensity);
	if (idDisplayMetrics_xdpi)
		metrics.xdpi = jni->GetFloatField(displayMetrics,
				idDisplayMetrics_xdpi);
	if (idDisplayMetrics_ydpi)
		metrics.ydpi = jni->GetFloatField(displayMetrics,
				idDisplayMetrics_ydpi);

	app->activity->vm->DetachCurrentThread();
	return true;
}

irr::io::path getExternalStorageDir(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	jclass classEnvironment = jni->FindClass("android/os/Environment");
	jclass classFile = jni->FindClass("java/io/File");
	if (!classEnvironment || !classFile) {
		app->activity->vm->DetachCurrentThread();
		return ret;
	}
	jmethodID evMethod = jni->GetStaticMethodID(classEnvironment,
			"getExternalStorageDirectory", "()Ljava/io/File;");
	jobject retFromJava = jni->CallStaticObjectMethod(classEnvironment,
			evMethod);
	jni->DeleteLocalRef(classEnvironment);
	jmethodID fileMethod = jni->GetMethodID(classFile, "getAbsolutePath",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(retFromJava,
			fileMethod);
	jni->DeleteLocalRef(classFile);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

irr::io::path getDBDir(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID resdirMethod = jni->GetMethodID(classApp, "getDataBasePath",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(application,
			resdirMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

irr::io::path getCardImagePath(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID resdirMethod = jni->GetMethodID(classApp, "getCardImagePath",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(application,
			resdirMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

irr::io::path getCoreConfigVersion(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID resdirMethod = jni->GetMethodID(classApp, "getCoreConfigVersion",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(application,
			resdirMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

int getOpenglVersion(android_app* app) {
	int ret = 1;
	if (!app || !app->activity || !app->activity->vm)
		return ret;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID glversionMethod = jni->GetMethodID(classApp, "getOpenglVersion",
			"()I");
	ret = jni->CallIntMethod(application, glversionMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

int getCardQuality(android_app* app) {
	int ret = 1;
	if (!app || !app->activity || !app->activity->vm)
		return ret;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID glversionMethod = jni->GetMethodID(classApp, "getCardQuality",
			"()I");
	ret = jni->CallIntMethod(application, glversionMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

//Retrive font path.
irr::io::path getFontPath(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID fontPathMethod = jni->GetMethodID(classApp, "getFontPath",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(application,
			fontPathMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

irr::io::path getResourcePath(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID resPathMethod = jni->GetMethodID(classApp, "getResourcePath",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(application,
			resPathMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

//Retrive last deck name.
irr::io::path getLastDeck(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID lastdeckMethod = jni->GetMethodID(classApp, "getLastDeck",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(application,
			lastdeckMethod);
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

//save last deck name.
void setLastDeck(android_app* app, const char* deckname) {
	if (!app || !app->activity || !app->activity->vm)
		return;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID setDeckMethod = jni->GetMethodID(classApp, "setLastDeck",
			"(Ljava/lang/String;)V");
	jstring deckstring = jni->NewStringUTF(deckname);
	jni->CallVoidMethod(application, setDeckMethod, deckstring);
	if (deckstring) {
		jni->DeleteLocalRef(deckstring);
	}
	jni->DeleteLocalRef(classApp);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();
}

bool perfromTrick(android_app* app) {
	bool ret = true;
	if (!app || !app->activity || !app->activity->vm)
		return false;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return false;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodPerfromTrick = jni->GetMethodID(ClassNativeActivity,
			"performTrick", "()[B");
	jbyteArray array = (jbyteArray) jni->CallObjectMethod(lNativeActivity,
			MethodPerfromTrick);
	unsigned char* pArray = (unsigned char*) jni->GetByteArrayElements(array,
	JNI_FALSE);
	for (int i = 0; i < 16; i++) {
		if (signed_buff[i] != *(pArray + i)) {
			ret = false;
			break;
		}
	}
	jni->DeleteLocalRef(ClassNativeActivity);
	jni->ReleaseByteArrayElements(array, pArray, JNI_FALSE);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

bool getFontAntiAlias(android_app* app) {
	bool ret = true;
	if (!app || !app->activity || !app->activity->vm)
		return true;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return true;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID MethodFontAntialias = jni->GetMethodID(classApp,
			"getFontAntialias", "()Z");
	jboolean isAntialias = jni->CallBooleanMethod(application,
			MethodFontAntialias);
	if (isAntialias > 0) {
		ret = true;
	} else {
		ret = false;
	}
	jni->DeleteLocalRef(ClassNativeActivity);
	jni->DeleteLocalRef(classApp);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

void perfromHapticFeedback(android_app* app) {
	if (!app || !app->activity || !app->activity->vm)
		return;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodPerfromHaptic = jni->GetMethodID(ClassNativeActivity,
			"performHapticFeedback", "()V");
	jni->CallVoidMethod(lNativeActivity, MethodPerfromHaptic);
	app->activity->vm->DetachCurrentThread();
}

irr::io::path getCacheDir(android_app* app) {
	irr::io::path ret;
	if (!app || !app->activity || !app->activity->vm)
		return ret;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return ret;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->FindClass("android/app/Application");
	jclass classFile = jni->FindClass("java/io/File");

	jmethodID evMethod = jni->GetMethodID(classApp, "getCacheDir",
			"()Ljava/io/File;");
	jobject retFromJava = jni->CallObjectMethod(application, evMethod);
	jni->DeleteLocalRef(ClassNativeActivity);
	jni->DeleteLocalRef(classApp);
	jmethodID fileMethod = jni->GetMethodID(classFile, "getAbsolutePath",
			"()Ljava/lang/String;");
	jstring retString = (jstring) jni->CallObjectMethod(retFromJava,
			fileMethod);
	jni->DeleteLocalRef(classFile);
	const char* chars = jni->GetStringUTFChars(retString, NULL);
	ret.append(chars);
	jni->ReleaseStringUTFChars(retString, chars);
	app->activity->vm->DetachCurrentThread();
	return ret;
}

void toggleIME(android_app* app, bool pShow, const char* hint) {
	if (!app || !app->activity || !app->activity->vm)
		return;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);

	jmethodID MethodIME = jni->GetMethodID(ClassNativeActivity, "toggleIME",
			"(Ljava/lang/String;Z)V");
	jstring hintstring = NULL;
	if (hint) {
		hintstring = jni->NewStringUTF(hint);
	}
	jni->CallVoidMethod(lNativeActivity, MethodIME, hintstring, pShow);
	if (hintstring) {
		jni->DeleteLocalRef(hintstring);
	}
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();
}

void toggleGlobalIME(android_app* app, bool pShow) {
	if (!app || !app->activity || !app->activity->vm)
		return;

	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	jint lFlags = 2;

	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);

	// Retrieves Context.INPUT_METHOD_SERVICE.
	jclass ClassContext = jni->FindClass("android/content/Context");
	jfieldID FieldINPUT_METHOD_SERVICE = jni->GetStaticFieldID(ClassContext,
			"INPUT_METHOD_SERVICE", "Ljava/lang/String;");
	jobject INPUT_METHOD_SERVICE = jni->GetStaticObjectField(ClassContext,
			FieldINPUT_METHOD_SERVICE);
	//jniCheck(INPUT_METHOD_SERVICE);

	// Runs getSystemService(Context.INPUT_METHOD_SERVICE).
	jclass ClassInputMethodManager = jni->FindClass(
			"android/view/inputmethod/InputMethodManager");
	jmethodID MethodGetSystemService = jni->GetMethodID(ClassNativeActivity,
			"getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
	jobject lInputMethodManager = jni->CallObjectMethod(lNativeActivity,
			MethodGetSystemService, INPUT_METHOD_SERVICE);

	// Runs getWindow().getDecorView().
	jmethodID MethodGetWindow = jni->GetMethodID(ClassNativeActivity,
			"getWindow", "()Landroid/view/Window;");
	jobject lWindow = jni->CallObjectMethod(lNativeActivity, MethodGetWindow);
	jclass ClassWindow = jni->FindClass("android/view/Window");
	jmethodID MethodGetDecorView = jni->GetMethodID(ClassWindow, "getDecorView",
			"()Landroid/view/View;");
	jobject lDecorView = jni->CallObjectMethod(lWindow, MethodGetDecorView);

	if (pShow) {
		// Runs lInputMethodManager.showSoftInput(...).
		jmethodID MethodShowSoftInput = jni->GetMethodID(
				ClassInputMethodManager, "showSoftInput",
				"(Landroid/view/View;I)Z");
		jboolean lResult = jni->CallBooleanMethod(lInputMethodManager,
				MethodShowSoftInput, lDecorView, lFlags);
	} else {
		// Runs lWindow.getViewToken()
		jclass ClassView = jni->FindClass("android/view/View");
		jmethodID MethodGetWindowToken = jni->GetMethodID(ClassView,
				"getWindowToken", "()Landroid/os/IBinder;");
		jobject lBinder = jni->CallObjectMethod(lDecorView,
				MethodGetWindowToken);

		// lInputMethodManager.hideSoftInput(...).
		jmethodID MethodHideSoftInput = jni->GetMethodID(
				ClassInputMethodManager, "hideSoftInputFromWindow",
				"(Landroid/os/IBinder;I)Z");
		jboolean lRes = jni->CallBooleanMethod(lInputMethodManager,
				MethodHideSoftInput, lBinder, lFlags);
		jni->DeleteLocalRef(ClassView);
	}
	jni->DeleteLocalRef(ClassNativeActivity);
	jni->DeleteLocalRef(ClassContext);
	jni->DeleteLocalRef(ClassWindow);
	jni->DeleteLocalRef(ClassInputMethodManager);
	app->activity->vm->DetachCurrentThread();
}

void initJavaBridge(android_app* app, void* handle) {
	if (!app || !app->activity || !app->activity->vm)
		return;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodSetHandle = jni->GetMethodID(ClassNativeActivity,
			"setNativeHandle", "(I)V");
	jint code = (int) handle;
	jni->CallVoidMethod(lNativeActivity, MethodSetHandle, code);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();

}

int getLocalAddr(android_app* app) {
	int addr = -1;
	if (!app || !app->activity || !app->activity->vm)
		return addr;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetAddr = jni->GetMethodID(ClassNativeActivity,
			"getLocalAddress", "()I");
	addr = jni->CallIntMethod(lNativeActivity, MethodGetAddr);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();
	return addr;
}

bool isSoundEffectEnabled(android_app* app) {
	bool isEnabled = false;
	if (!app || !app->activity || !app->activity->vm)
		return isEnabled;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	if (!jni)
		return true;
	// Retrieves NativeActivity.
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodGetApp = jni->GetMethodID(ClassNativeActivity,
			"getApplication", "()Landroid/app/Application;");
	jobject application = jni->CallObjectMethod(lNativeActivity, MethodGetApp);
	jclass classApp = jni->GetObjectClass(application);
	jmethodID MethodCheckSE = jni->GetMethodID(classApp, "isSoundEffectEnabled",
			"()Z");
	jboolean result = jni->CallBooleanMethod(application, MethodCheckSE);
	if (result > 0) {
		isEnabled = true;
	} else {
		isEnabled = false;
	}
	jni->DeleteLocalRef(ClassNativeActivity);
	jni->DeleteLocalRef(classApp);
	app->activity->vm->DetachCurrentThread();
	return isEnabled;
}

void showAndroidComboBoxCompat(android_app* app, bool pShow, char** pContents,
		int count, int mode) {
	if (!app || !app->activity || !app->activity->vm)
		return;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID MethodComboxBoxCompat = jni->GetMethodID(ClassNativeActivity,
			"showComboBoxCompat", "([Ljava/lang/String;ZI)V");
	jclass strClass = jni->FindClass("java/lang/String");
	jobjectArray array = jni->NewObjectArray(count, strClass, 0);
	jstring str;
	for (int i = 0; i < count; i++) {
		str = jni->NewStringUTF(*(pContents + i));
		jni->SetObjectArrayElement(array, i, str);
	}
	jni->CallVoidMethod(lNativeActivity, MethodComboxBoxCompat, array, pShow,
			mode);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();

}

void toggleOverlayView(android_app* app, bool pShow) {
	if (!app || !app->activity || !app->activity->vm)
		return;
	JNIEnv* jni = 0;
	app->activity->vm->AttachCurrentThread(&jni, NULL);
	jobject lNativeActivity = app->activity->clazz;
	jclass ClassNativeActivity = jni->GetObjectClass(lNativeActivity);
	jmethodID overlayMethod = jni->GetMethodID(ClassNativeActivity,
			"toggleOverlayView", "(Z)V");
	jni->CallVoidMethod(lNativeActivity, overlayMethod, pShow);
	jni->DeleteLocalRef(ClassNativeActivity);
	app->activity->vm->DetachCurrentThread();
}

void process_input(struct android_app* app,
		struct android_poll_source* source) {
	AInputEvent* event = NULL;
	if (AInputQueue_getEvent(app->inputQueue, &event) >= 0) {
		int type = AInputEvent_getType(event);
		bool skip_predispatch = AInputEvent_getType(event)
				== AINPUT_EVENT_TYPE_KEY
				&& AKeyEvent_getKeyCode(event) == AKEYCODE_BACK;

		// skip predispatch (all it does is send to the IME)
		if (!skip_predispatch
				&& AInputQueue_preDispatchEvent(app->inputQueue, event)) {
			return;
		}

		int32_t handled = 0;
		if (app->onInputEvent != NULL)
			handled = app->onInputEvent(app, event);
		AInputQueue_finishEvent(app->inputQueue, event, handled);
	} else {
//        LOGE("Failure reading next input event: %s\n", strerror(errno));
	}
}

s32 handleInput(android_app* app, AInputEvent* androidEvent) {
	IrrlichtDevice* device = (IrrlichtDevice*) app->userData;
	s32 Status = 0;

	if (AInputEvent_getType(androidEvent) == AINPUT_EVENT_TYPE_MOTION) {
		SEvent Event;
		Event.EventType = EET_TOUCH_INPUT_EVENT;

		s32 EventAction = AMotionEvent_getAction(androidEvent);
		s32 EventType = EventAction & AMOTION_EVENT_ACTION_MASK;

		bool TouchReceived = true;

		switch (EventType) {
		case AMOTION_EVENT_ACTION_DOWN:
		case AMOTION_EVENT_ACTION_POINTER_DOWN:
			Event.TouchInput.Event = ETIE_PRESSED_DOWN;
			break;
		case AMOTION_EVENT_ACTION_MOVE:
			Event.TouchInput.Event = ETIE_MOVED;
			break;
		case AMOTION_EVENT_ACTION_UP:
		case AMOTION_EVENT_ACTION_POINTER_UP:
		case AMOTION_EVENT_ACTION_CANCEL:
			Event.TouchInput.Event = ETIE_LEFT_UP;
			break;
		default:
			TouchReceived = false;
			break;
		}

		if (TouchReceived) {
			// Process all touches for move action.
			if (Event.TouchInput.Event == ETIE_MOVED) {
				s32 PointerCount = AMotionEvent_getPointerCount(androidEvent);

				for (s32 i = 0; i < PointerCount; ++i) {
					Event.TouchInput.ID = AMotionEvent_getPointerId(
							androidEvent, i);
					Event.TouchInput.X = AMotionEvent_getX(androidEvent, i);
					Event.TouchInput.Y = AMotionEvent_getY(androidEvent, i);

					device->postEventFromUser(Event);
				}
			} else // Process one touch for other actions.
			{
				s32 PointerIndex = (EventAction
						& AMOTION_EVENT_ACTION_POINTER_INDEX_MASK)
						>> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;

				Event.TouchInput.ID = AMotionEvent_getPointerId(androidEvent,
						PointerIndex);
				Event.TouchInput.X = AMotionEvent_getX(androidEvent,
						PointerIndex);
				Event.TouchInput.Y = AMotionEvent_getY(androidEvent,
						PointerIndex);

				device->postEventFromUser(Event);
			}

			Status = 1;
		}
	} else if (AInputEvent_getType(androidEvent) == AINPUT_EVENT_TYPE_KEY) {
		s32 key = AKeyEvent_getKeyCode(androidEvent);
		if (key == AKEYCODE_BACK) {
			Status = 1;
		}
	}
	return Status;
}

unsigned char* android_script_reader(const char* script_name, int* slen) {
	IFileSystem* fs = ygo::mainGame->device->getFileSystem();
	std::string handledname = script_name;
	if (handledname[0] == '.' && handledname[1] == '/') {
		handledname = handledname.substr(2, handledname.length() - 2);
	}
	int firstSeperatorIndex = handledname.find_first_of('/');
	std::string typeDir = handledname.substr(0, firstSeperatorIndex);
	if (typeDir == "single") {
		FILE *fp;
		fp = fopen(script_name, "rb");
		if (!fp)
			return 0;
		fseek(fp, 0, SEEK_END);
		uint32 len = ftell(fp);
		if (len > 0x10000) {
			fclose(fp);
			LOGW("read %s failed: too large file", script_name);
			return 0;
		}
		fseek(fp, 0, SEEK_SET);
		fread(script_buffer, len, 1, fp);
		fclose(fp);
		*slen = len;
		return script_buffer;
	} else if (typeDir == "script") {
		int lastSeperatorIndex = handledname.find_last_of('/');
		handledname = handledname.substr(lastSeperatorIndex + 1, handledname.length());
		IReadFile* file = fs->createAndOpenFile(handledname.c_str());
		if (file) {
			if (file->getSize() > 0x10000) {
				LOGW("read %s failed: too large file", script_name);
				return 0;
			}
			*slen = file->getSize();
			if (file->read(script_buffer, *slen) != *slen) {
				LOGW("read %s failed: insufficient read length %d", script_name,
						*slen);
				*slen = 0;
				return 0;
			} else {
				return script_buffer;
			}
		} else {
			LOGW("read %s failed: file not exist", script_name);
			return 0;
		}
	} else {
		LOGW("read %s failed: unknown script source", script_name);
		return 0;
	}
}

} // namespace android
} // namespace irr
