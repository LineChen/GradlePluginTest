package com.line.gradleplugintest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

@FastApi
public class MainActivity extends AppCompatActivity {

    @FastApi
    BoyApi boyApi;

    @FastApi
    GirlApi girlApi;

    BoyApi errorApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadGirlApi(View view) {
        girlApi.getGirl();
    }

    public void loadBoyApi(View view) {
        boyApi.getBoy();
    }

    public void loadErrorApi(View view) {
        errorApi.getBoy();
    }
}
