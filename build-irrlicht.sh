#! /usr/bin/env bash

if [ -z "${NDK_ROOT+aaa}" ];then
echo "please define NDK_ROOT first"
exit 1
fi

DEFAULT_APP_NAME=Irrlicht
DEFAULT_BUILD_INTERMMIDIATE_PATH="$TMPDIR/$DEFAULT_APP_NAME.$$/"

if [ -z "${BUILD_INTERMIDIATE_PATH}" ];then
BUILD_INTERMIDIATE_PATH=$DEFAULT_BUILD_INTERMMIDIATE_PATH
fi

if [ -z "${APP_NAME}" ];then
APP_NAME=$DEFAULT_APP_NAME
fi

if [ -z "${TARGET_ARCHS}" ];then
TARGET_ARCHS=("armeabi-v7a" "x86")
fi

echo "NDK_ROOT = $NDK_ROOT"
echo "APP_NAME = $APP_NAME"


DIR="$( cd "$(dirname "${BASH_SOURCE[0] }" )" && pwd )"
APP_ROOT="$DIR"

IRRLICHT_BUILD_ROOT="$DIR/irrlicht/source/Irrlicht/Android/"
TARGET_BUILD_PATH="$APP_ROOT/build/android/$APP_NAME"


#run ndk-build
echo "start building..."
for arch in ${TARGET_ARCHS[@]}
do
mkdir -p "$TARGET_BUILD_PATH/$arch/"
"$NDK_ROOT"/ndk-build -C "$IRRLICHT_BUILD_ROOT" NDK_DEBUG=0 $* "APP_ABI := $arch" \
    NDK_APP_OUT="$BUILD_INTERMIDIATE_PATH" IRRLICHT_LIB_PATH="$TARGET_BUILD_PATH/$arch/"
done



