APPNAME="YGOMobile"

if [ -z "${NDK_ROOT+aaa}" ];then
echo "please define NDK_ROOT first"
exit 1
fi

DIR="$( cd "$(dirname "${BASH_SOURCE[0] }" )" && pwd )"

APP_ROOT="$DIR/.."
APP_ANDROID_ROOT="$DIR"

TARGET_ARCHS=("armeabi-v7a" "x86")

echo "APP_ROOT = $APP_ROOT"
echo "NDK_ROOT = $NDK_ROOT"
echo "APP_ANDROID_ROOT = $APP_ANDROID_ROOT"

#run ndk-build
echo "start building..."
"$NDK_ROOT"/ndk-build -C "$APP_ANDROID_ROOT" $*

#copy umeng update library
for arch in ${TARGET_ARCHS[@]}
do
    for lib in `ls "$APP_ANDROID_ROOT/umeng/$arch/"`
    do
        cp -r "$APP_ANDROID_ROOT/umeng/$arch/$lib" "$APP_ANDROID_ROOT/libs/$arch/"
    done
done

