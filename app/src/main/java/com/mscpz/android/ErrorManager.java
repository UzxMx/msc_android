package com.mscpz.android;

/**
 * Created by xmx on 2017/8/5.
 */

public class ErrorManager {

    private static ErrorManager singleton;

    private String error;

    public static ErrorManager getInstance() {
        if (singleton == null) {
            synchronized (ErrorManager.class) {
                if (singleton == null) {
                    singleton = new ErrorManager();
                }
            }
        }
        return singleton;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return this.error;
    }
}
