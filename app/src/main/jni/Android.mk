LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE := capstone
LOCAL_SRC_FILES := prebuilt/libs/$(TARGET_ARCH_ABI)/libcapstone.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/prebuilt/include
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := keystone
LOCAL_SRC_FILES := prebuilt/libs/$(TARGET_ARCH_ABI)/libkeystone.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/prebuilt/include
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE := Arm-antik
LOCAL_SRC_FILES := Main.cpp Main2.cpp
LOCAL_STATIC_LIBRARIES := capstone keystone
LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/prebuilt/include \
    $(LOCAL_PATH)/capstone/include \
    $(LOCAL_PATH)/keystone/include

LOCAL_LDLIBS := -llog
LOCAL_CPPFLAGS += -std=c++17 -fexceptions -frtti
include $(BUILD_SHARED_LIBRARY)
