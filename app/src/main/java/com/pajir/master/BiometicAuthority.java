package com.pajir.master;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.Executor;

public class BiometicAuthority extends AppCompatActivity {
    private static final String TAG = "Biometic_Master";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        biometricAuth();
    }

    private void biometricAuth() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "biometricAuth: biometric success");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.d(TAG, "biometricAuth: biometric error no hardware");
                //Toast.makeText(this, "当前硬件不支持该操作", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.d(TAG, "biometricAuth: biometric error hw unavailable");
                //Toast.makeText(this, "无法生物识别验证身份", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.d(TAG, "biometricAuth: biometric error none enrolled");
                //Toast.makeText(this, "请设置指纹", Toast.LENGTH_SHORT).show();
                return;
        }
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d(TAG, "onAuthenticationError: error");
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                delCurInfo();
                stopService(new Intent(BiometicAuthority.this, FloatingWindowService.class));
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "onAuthenticationSucceeded: succeeded");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "onAuthenticationFailed: failed");
            }
        });
        // create Biometric Dialog
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("确认放弃")
                .setDescription("请验证指纹")
                .setNegativeButtonText("不放弃了")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    // 服务结束前删除信息
    private void delCurInfo(){
        MasterDBHelper dbHelper = new MasterDBHelper(this, "master.db", null, 4);
        //MasterDBHelper dbHelper = new MasterDBHelper(context, "master.db", null, 3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("CurRecord", "id >= ?", new String[] {"0"});
        db.close();
        dbHelper.close();
    }
}