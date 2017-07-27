package com.mscpz.android.net;

/**
 * Created by xuemingxiang on 16-11-14.
 */

public class URL {

//    public static final String HOST = "https://xinya.me";
//    public static final String HOST = "http://test.xinya.me";
    public static final String HOST = "http://192.168.1.5:3000";
//    public static final String HOST = "http://192.168.0.102:3000";

    public static final String PREFIX = HOST + "/devices/api";

    public static final String URL_SEND_MSG = PREFIX + "/home/send_msg.json";

    public static final String URL_GET_BABIES = PREFIX + "/user_babies.json";

    public static final String URL_BABIES = PREFIX + "/user_babies";

    public static final String URL_V2 = PREFIX + "/v2";

    public static final String URL_V3 = PREFIX + "/v3";

    public static final String URL_GET_CATEGORIES = PREFIX + "/v2/course_categories.json";
}
