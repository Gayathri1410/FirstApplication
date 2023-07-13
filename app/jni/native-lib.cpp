#include <jni.h>
#include <memory>


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_firstapplication_NativeCrashHelper_generateNativeCrash(JNIEnv *env, jclass clazz) {
    std::shared_ptr<int> pInt = std::make_shared<int>();

    int *crashPtr = NULL;
    *crashPtr = 1; // Trying to dereference a null pointer, which will cause a native crash
}







