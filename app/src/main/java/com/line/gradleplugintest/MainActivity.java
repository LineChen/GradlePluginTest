package com.line.gradleplugintest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.line.libuse.TestLibUse;
import com.line.libuse.api.BoyApi;
import com.line.libuse.api.FastApi;
import com.line.libuse.api.GirlApi;

@FastApi
public class MainActivity extends AppCompatActivity {

    @FastApi
    BoyApi boyApi;

    @FastApi
    GirlApi girlApi;

    BoyApi errorApi;
    private TestLibUse testLibUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testLibUse = new TestLibUse();
    }

    public void loadGirlApi(View view) {
        girlApi.getGirl();
        testLibUse.loadGirlApi();
    }

    public void loadBoyApi(View view) {
        boyApi.getBoy();
        testLibUse.loadBoyApi();
    }

    public void loadErrorApi(View view) {
        errorApi.getBoy();
    }
}
