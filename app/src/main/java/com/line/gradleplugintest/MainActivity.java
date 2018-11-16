package com.line.gradleplugintest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

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
        Toast.makeText(this, girlApi.toString() + "akkkklj", Toast.LENGTH_SHORT).show();
    }

    public void loadBoyApi(View view) {
        Toast.makeText(this, boyApi.toString(), Toast.LENGTH_SHORT).show();
    }

    public void loadErrorApi(View view) {
        Toast.makeText(this, errorApi.toString(), Toast.LENGTH_SHORT).show();
    }
}
