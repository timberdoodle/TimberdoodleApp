LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := packetsocket
LOCAL_SRC_FILES := packetsocket.c
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := packetsocket_creator
LOCAL_SRC_FILES := packetsocket_creator.c
include $(BUILD_EXECUTABLE)

# Rename packet socket creator so it gets placed in the library folder

# Windows
all: $(LOCAL_MODULE)
	cmd /c move /Y libs\arm64-v8a\packetsocket_creator libs\arm64-v8a\libpacketsocket_creator.so
	cmd /c move /Y libs\armeabi\packetsocket_creator libs\armeabi\libpacketsocket_creator.so
	cmd /c move /Y libs\armeabi-v7a\packetsocket_creator libs\armeabi-v7a\libpacketsocket_creator.so
	cmd /c move /Y libs\mips\packetsocket_creator libs\mips\libpacketsocket_creator.so
	cmd /c move /Y libs\mips64\packetsocket_creator libs\mips64\libpacketsocket_creator.so
	cmd /c move /Y libs\x86\packetsocket_creator libs\x86\libpacketsocket_creator.so
	cmd /c move /Y libs\x86_64\packetsocket_creator libs\x86_64\libpacketsocket_creator.so

# Linux
#	$(shell (mv libs/arm64-v8a/packetsocket_creator libs/arm64-v8a/libpacketsocket_creator.so))
#	$(shell (mv libs/armeabi/packetsocket_creator libs/armeabi/libpacketsocket_creator.so))
#	$(shell (mv libs/armeabi-v7a/packetsocket_creator libs/armeabi-v7a/libpacketsocket_creator.so))
#	$(shell (mv libs/mips/packetsocket_creator libs/mips/libpacketsocket_creator.so))
#	$(shell (mv libs/mips64/packetsocket_creator libs/mips64/libpacketsocket_creator.so))
#	$(shell (mv libs/x86/packetsocket_creator libs/x86/libpacketsocket_creator.so))
#	$(shell (mv libs/x86_64/packetsocket_creator libs/x86_64/libpacketsocket_creator.so))

# Windows
#all: $(LOCAL_MODULE)
#	cmd /c move /Y libs\$(TARGET_ARCH_ABI)\packetsocket_creator libs\$(TARGET_ARCH_ABI)\libpacketsocket_creator.so
# Linux:
#all: $(LOCAL_MODULE)
#	$(shell (mv libs/$(TARGET_ARCH_ABI)/packetsocket_creator libs/$(TARGET_ARCH_ABI)/libpacketsocket_creator.so))
