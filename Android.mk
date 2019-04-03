LOCAL_PATH:= $(call my-dir)

########################################
# NCI Configuration
########################################
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
# LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES := \
        $(call all-java-files-under, app)
LOCAL_SRC_FILES += \
    ./src/org/opencv/engine/OpenCVEngineInterface.aidl
LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res

LOCAL_SHARED_LIBRARIES := libopencv_java3

LOCAL_PACKAGE_NAME := OpenCVDemo
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_USE_AAPT2 := true

# LOCAL_MODULE_TAGS := tests

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
