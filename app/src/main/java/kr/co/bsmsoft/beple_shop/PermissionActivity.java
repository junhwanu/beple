/*
 * Copyright (C) 2015 Jacob Klinker
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

package kr.co.bsmsoft.beple_shop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import kr.co.bsmsoft.beple_shop.common.Setting;

public class PermissionActivity extends Activity {

    private Setting mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetting = new Setting(this);

        requestPermissions(new String[]{
                //Manifest.permission.READ_SMS,
                //Manifest.permission.SEND_SMS,
                //Manifest.permission.RECEIVE_SMS,
                //Manifest.permission.RECEIVE_MMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_NETWORK_STATE
        }, 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        mSetting.input("request_permissions", true);

        //Intent goToSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        //goToSettings.setData(Uri.parse("package:" + getPackageName()));
        //startActivity(goToSettings);

        //startActivity(new Intent(this, MainActivity.class));
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
