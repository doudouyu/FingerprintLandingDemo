package com.example.fingerprintlandingdemo.biometricPrompt;

import android.os.CancellationSignal;
import android.support.annotation.NonNull;

/**
 * Created by yudoudou on 2018/12/21.
 */
public interface  IBiometricPromptImpl {
    void authenticate(int purposeEncrypt, String password, @NonNull CancellationSignal cancel,
                      @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback);
}
