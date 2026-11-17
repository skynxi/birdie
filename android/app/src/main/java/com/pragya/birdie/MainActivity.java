package com.pragya.birdie;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(BirdieWidgetsPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
