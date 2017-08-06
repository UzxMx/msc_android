#ifndef _JNI_HELPER_H_
#define _JNI_HELPER_H_

#include "jni.h"

/**
 * @return 0 failed, 1 succeeded
 */
int register_native_methods(JNIEnv *env,
	const char *class_path_name,
	JNINativeMethod *class_methods,
	int methods_num);

jint throw_exception(JNIEnv *env, char *message);

#endif