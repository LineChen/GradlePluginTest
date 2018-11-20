package com.line.libuse;

import android.util.Log;

import com.line.libuse.api.BoyApi;
import com.line.libuse.api.FastApi;
import com.line.libuse.api.GirlApi;

/**
 * Created by chenliu on 2018/11/20.
 */

@FastApi
public class TestLibUse {
    @FastApi
    BoyApi boyApi;

    @FastApi
    GirlApi girlApi;

    public void loadGirlApi() {
        girlApi.getGirl();
        Log.d("TestLibUse", "loadGirlApi");
    }

    public void loadBoyApi() {
        boyApi.getBoy();
        Log.d("TestLibUse", "loadBoyApi");
    }
}
