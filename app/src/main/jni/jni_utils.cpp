#include <stdlib.h>
#include <utils/log.h>
#include "jni_utils.h"

int register_native_methods(JNIEnv *env,
	const char *class_path_name,
	JNINativeMethod *class_methods,
	int methods_num)
{
	jclass clazz = env->FindClass(class_path_name);
	if (clazz == NULL)
	{
		LOG_ERROR("%s: unable to find class: %s", __FUNCTION__, class_path_name);
		return 0;
	}

	if (env->RegisterNatives(clazz, class_methods, methods_num) < 0)
	{
		LOG_ERROR("%s: register natives failed for %s", __FUNCTION__, class_path_name);
		return 0;
	}

	return 1;
}

jint throw_exception(JNIEnv *env, char *message)
{
    jclass clazz;
    char *class_name = "java/lang/Exception";

    clazz = env->FindClass(class_name);

    return env->ThrowNew(clazz, message);
}