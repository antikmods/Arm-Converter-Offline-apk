#include <jni.h>
#include <string>
#include <keystone/keystone.h>
#include <sstream>
#include <iomanip>

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_arm_toolantik_MainActivity_convertAsmToHex(JNIEnv *env, jobject /*this*/, jstring asmString) {
    const char *asmCode = env->GetStringUTFChars(asmString, 0);

    ks_engine *ks;
    ks_err err;
    unsigned char *encode;
    size_t size;
    size_t count;
    
    std::ostringstream armHex, arm64Hex, thumbHex, thumbBeHex, armBeHex;

    auto formatHex = [](std::ostringstream &out, unsigned char *encode, size_t size) {
        for (size_t i = 0; i < size; i++) {
            out << std::setw(2) << std::setfill('0') << std::hex << (int)encode[i] << " ";
        }
    };

    err = ks_open(KS_ARCH_ARM, KS_MODE_ARM, &ks);
    if (err == KS_ERR_OK && ks_asm(ks, asmCode, 0, &encode, &size, &count) == KS_ERR_OK) {
        formatHex(armHex, encode, size);
        ks_free(encode);
    }
    ks_close(ks);

    err = ks_open(KS_ARCH_ARM64, KS_MODE_LITTLE_ENDIAN, &ks);
    if (err == KS_ERR_OK && ks_asm(ks, asmCode, 0, &encode, &size, &count) == KS_ERR_OK) {
        formatHex(arm64Hex, encode, size);
        ks_free(encode);
    }
    ks_close(ks);


    err = ks_open(KS_ARCH_ARM, KS_MODE_THUMB, &ks);
    if (err == KS_ERR_OK && ks_asm(ks, asmCode, 0, &encode, &size, &count) == KS_ERR_OK) {
        formatHex(thumbHex, encode, size);
        ks_free(encode);
    }
    ks_close(ks);


    err = ks_open(KS_ARCH_ARM, (ks_mode)(KS_MODE_THUMB | KS_MODE_BIG_ENDIAN), &ks);
    if (err == KS_ERR_OK && ks_asm(ks, asmCode, 0, &encode, &size, &count) == KS_ERR_OK) {
        formatHex(thumbBeHex, encode, size);
        ks_free(encode);
    }
    ks_close(ks);


    err = ks_open(KS_ARCH_ARM, KS_MODE_BIG_ENDIAN, &ks);
    if (err == KS_ERR_OK && ks_asm(ks, asmCode, 0, &encode, &size, &count) == KS_ERR_OK) {
        formatHex(armBeHex, encode, size);
        ks_free(encode);
    }
    ks_close(ks);

    jobjectArray resultArray = env->NewObjectArray(5, env->FindClass("java/lang/String"), env->NewStringUTF(""));


    env->SetObjectArrayElement(resultArray, 0, env->NewStringUTF(armHex.str().c_str()));
    env->SetObjectArrayElement(resultArray, 1, env->NewStringUTF(arm64Hex.str().c_str()));
    env->SetObjectArrayElement(resultArray, 2, env->NewStringUTF(thumbHex.str().c_str()));
    env->SetObjectArrayElement(resultArray, 3, env->NewStringUTF(thumbBeHex.str().c_str()));
    env->SetObjectArrayElement(resultArray, 4, env->NewStringUTF(armBeHex.str().c_str()));

    env->ReleaseStringUTFChars(asmString, asmCode);
    return resultArray;
}
