/*
 * Copyright 2009-2011 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <assert.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <stdio.h>

#include "jni_utils.h"
#include "utils/log.h"

static speed_t getBaudrate(jint baudrate)
{
	switch(baudrate) {
	case 0: return B0;
	case 50: return B50;
	case 75: return B75;
	case 110: return B110;
	case 134: return B134;
	case 150: return B150;
	case 200: return B200;
	case 300: return B300;
	case 600: return B600;
	case 1200: return B1200;
	case 1800: return B1800;
	case 2400: return B2400;
	case 4800: return B4800;
	case 9600: return B9600;
	case 19200: return B19200;
	case 38400: return B38400;
	case 57600: return B57600;
	case 115200: return B115200;
	case 230400: return B230400;
	case 460800: return B460800;
	case 500000: return B500000;
	case 576000: return B576000;
	case 921600: return B921600;
	case 1000000: return B1000000;
	case 1152000: return B1152000;
	case 1500000: return B1500000;
	case 2000000: return B2000000;
	case 2500000: return B2500000;
	case 3000000: return B3000000;
	case 3500000: return B3500000;
	case 4000000: return B4000000;
	default: return -1;
	}
}

jobject serial_port_open(JNIEnv *env, jclass thiz, jstring path, jint baudrate, jint flags)
{
	int fd;
	speed_t speed;
	jobject mFileDescriptor;

	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) {
			throw_exception(env, "Invalid baudrate");
		}
	}

	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = env->GetStringUTFChars(path, &iscopy);
		LOG_DEBUG("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
		fd = open(path_utf, O_RDWR | flags);
		LOG_DEBUG("open() fd = %d", fd);
		env->ReleaseStringUTFChars(path, path_utf);
		if (fd == -1)
		{
			throw_exception(env, "Cannot open port");
		}
	}

	/* Configure device */
	{
		struct termios cfg;
		LOG_DEBUG("Configuring serial port");
		if (tcgetattr(fd, &cfg))
		{
		    LOG_ERROR("tcgetattr() failed: %s", strerror(errno));
		    close(fd);
		    char msg[1024];
		    sprintf(msg, "tcgetattr() failed: %s", strerror(errno));
		    throw_exception(env, msg);
		}

        LOG_ERROR("B19200: %d", B19200);
		LOG_ERROR("B9600: %d", B9600);
		LOG_ERROR("input speed: %d", cfgetispeed(&cfg));
		LOG_ERROR("output speed: %d", cfgetospeed(&cfg));

/*
		cfmakeraw(&cfg);
		cfsetispeed(&cfg, speed);
		cfsetospeed(&cfg, speed);

		if (tcsetattr(fd, TCSANOW, &cfg))
		{
		    LOG_ERROR("tcsetattr() failed: %s", strerror(errno));
			close(fd);
			char msg[1024];
			sprintf(msg, "tcsetattr() failed: %s", strerror(errno));
			throw_exception(env, msg);
		}*/
	}

	/* Create a corresponding file descriptor */
	{
		jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
		jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
		mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
		env->SetIntField(mFileDescriptor, descriptorID, (jint)fd);
	}

	return mFileDescriptor;
}

void serial_port_close(JNIEnv *env, jobject thiz)
{
	jclass SerialPortClass = env->GetObjectClass(thiz);
	jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

	jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

	jobject mFd = env->GetObjectField(thiz, mFdID);
	jint descriptor = env->GetIntField(mFd, descriptorID);

	LOG_DEBUG("close(fd = %d)", descriptor);
	close(descriptor);
}

static const char *serial_port_class_path_name = "com/serialport/SerialPort";
static JNINativeMethod serial_port_methods[] =
{
	{"open", "(Ljava/lang/String;II)Ljava/io/FileDescriptor;", (void *) serial_port_open},
	{"close", "()V", (void *) serial_port_close}
};

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	JNIEnv *env = NULL;
	jint result = -1;

	if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK)
	{
		LOG_ERROR("%s: GetEnv failed", __FUNCTION__);
		goto end;
	}

	assert(env != NULL);

	if (register_native_methods(env, serial_port_class_path_name, serial_port_methods,
	        sizeof(serial_port_methods) / sizeof(serial_port_methods[0])) == 0)
	{
		LOG_ERROR("%s: register native methods failed", __FUNCTION__);
		goto end;
	}

	result = JNI_VERSION_1_4;

end:
	return result;
}

