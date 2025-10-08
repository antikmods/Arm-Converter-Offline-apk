#include <jni.h>
#include <string>
#include <capstone/capstone.h>
#include <sstream>
#include <vector>

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_arm_toolantik_MainActivity_convertHexToArmFormats(JNIEnv *env, jobject /*this*/, jstring hexString) {
    const char *hex = env->GetStringUTFChars(hexString, 0);
    size_t len = strlen(hex) / 2;
    unsigned char *code = new unsigned char[len];
    for (size_t i = 0; i < len; ++i) {
        sscanf(&hex[i * 2], "%2hhx", &code[i]);
    }

    csh handle;
    cs_err err;


    err = cs_open(CS_ARCH_ARM, CS_MODE_ARM, &handle);
    if (err != CS_ERR_OK) {
        env->ReleaseStringUTFChars(hexString, hex);
        delete[] code;
        return nullptr;
    }
    cs_insn *insn;
    size_t count = cs_disasm(handle, code, len, 0x1000, 0, &insn);
    std::ostringstream armResult;
    for (size_t i = 0; i < count; i++) {
        armResult << insn[i].mnemonic << " " << insn[i].op_str << "\n";
    }
    cs_close(&handle);

    err = cs_open(CS_ARCH_ARM64, CS_MODE_ARM, &handle);
    count = cs_disasm(handle, code, len, 0x1000, 0, &insn);
    std::ostringstream arm64Result;
    for (size_t i = 0; i < count; i++) {
        arm64Result << insn[i].mnemonic << " " << insn[i].op_str << "\n";
    }
    cs_close(&handle);

    
    err = cs_open(CS_ARCH_ARM, CS_MODE_THUMB, &handle);
    count = cs_disasm(handle, code, len, 0x1000, 0, &insn);
    std::ostringstream thumbResult;
    for (size_t i = 0; i < count; i++) {
        thumbResult << insn[i].mnemonic << " " << insn[i].op_str << "\n";
    }
    cs_close(&handle);

    err = cs_open(CS_ARCH_ARM, (cs_mode)(CS_MODE_THUMB | CS_MODE_BIG_ENDIAN), &handle);
    count = cs_disasm(handle, code, len, 0x1000, 0, &insn);
    std::ostringstream thumbBeResult;
    for (size_t i = 0; i < count; i++) {
        thumbBeResult << insn[i].mnemonic << " " << insn[i].op_str << "\n";
    }
    cs_close(&handle);


    err = cs_open(CS_ARCH_ARM, CS_MODE_BIG_ENDIAN, &handle);
    count = cs_disasm(handle, code, len, 0x1000, 0, &insn);
    std::ostringstream armBeResult;
    for (size_t i = 0; i < count; i++) {
        armBeResult << insn[i].mnemonic << " " << insn[i].op_str << "\n";
    }
    cs_close(&handle);

    
    jobjectArray resultArray = env->NewObjectArray(5, env->FindClass("java/lang/String"), env->NewStringUTF(""));



    env->SetObjectArrayElement(resultArray, 0, env->NewStringUTF(armResult.str().c_str()));
    env->SetObjectArrayElement(resultArray, 1, env->NewStringUTF(arm64Result.str().c_str()));
    env->SetObjectArrayElement(resultArray, 2, env->NewStringUTF(thumbResult.str().c_str()));
    env->SetObjectArrayElement(resultArray, 3, env->NewStringUTF(thumbBeResult.str().c_str()));
    env->SetObjectArrayElement(resultArray, 4, env->NewStringUTF(armBeResult.str().c_str()));


    cs_free(insn, count);
    env->ReleaseStringUTFChars(hexString, hex);
    delete[] code;

    return resultArray;
}

