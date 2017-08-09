#ifndef _CLEVERLOOP_LOG_H_
#define _CLEVERLOOP_LOG_H_

#include <android/log.h>

#define TAG "MainActivity"

#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOG_DEBUG(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#endif