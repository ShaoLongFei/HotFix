#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_liuyue_hotfix_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++ (旧版本)";
    return env->NewStringUTF(hello.c_str());
}