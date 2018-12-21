package com.example.fingerprintlandingdemo.biometricPrompt;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

/**
 * Created by yudoudou on 2018/12/21.
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public class BiometricPromptManager {
    private IBiometricPromptImpl mImpl;
    private Activity mActivity;
    public interface OnBiometricIdentifyCallback {

        void onSucceeded(String password);

        void onFailed();

        void onError(int code, String reason);

        void onCancel();

    }

    public static BiometricPromptManager from(Activity activity) {
        return new BiometricPromptManager(activity);
    }

    public BiometricPromptManager(Activity activity) {
        mActivity = activity;
        if (isAboveApi28()) {
            mImpl = new BiometricPromptApi28(activity);
        } else if (isAboveApi23()) {
            mImpl = new BiometricPromptApi23(activity);
        }
    }

    private boolean isAboveApi28() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    private boolean isAboveApi23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public void authenticate(int purposeEncrypt, String password, @NonNull OnBiometricIdentifyCallback callback) {
        mImpl.authenticate(purposeEncrypt,password, new CancellationSignal(), callback);
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     *
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    public boolean hasEnrolledFingerprints() {
        if (isAboveApi28()) {
            return ((BiometricPromptApi28) mImpl).hasEnrolledFingerprints();
        } else if (isAboveApi23()) {
            return ((BiometricPromptApi23) mImpl).hasEnrolledFingerprints();
        } else {
            return false;
        }
    }

    /**
     * Determine if fingerprint hardware is present and functional.
     *
     * @return true if hardware is present and functional, false otherwise.
     */
    public boolean isHardwareDetected() {
        if (isAboveApi28()) {
            return ((BiometricPromptApi28) mImpl).isHardwareDetected();
//            return false;
        } else if (isAboveApi23()) {
            return ((BiometricPromptApi23) mImpl).isHardwareDetected();
        } else {
            return false;
        }
    }


    public boolean isKeyguardSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) mActivity.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            return true;
        }

        return false;
    }

    /**
     * Whether the device support biometric.
     *
     * @return
     */
    public boolean isBiometricPromptEnable() {
        return isAboveApi23()
                && isHardwareDetected()
                && hasEnrolledFingerprints()
                && isKeyguardSecure();
    }

    public String getPassword() {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.readStringValue(Constant.KEY_BIOMETRIC_PASSWORD, "");
    }

    public void setPassword(String password) {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        sharePreferenceUtil.writeStringValue(Constant.KEY_BIOMETRIC_PASSWORD, password);
    }

    /**
     * Whether fingerprint identification is turned on in app setting.
     *
     * @return
     */
    public boolean isBiometricSettingEnable(String userName) {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.readBooleanValue(Constant.KEY_BIOMETRIC_SWITCH_ENABLE + userName, false);
    }

    /**
     * Set fingerprint identification enable in app setting.
     *
     * @return
     */
    public void setBiometricSettingEnable(boolean enable,String userName) {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        sharePreferenceUtil.writeBooleanValue(Constant.KEY_BIOMETRIC_SWITCH_ENABLE + userName, enable);
    }
    public boolean setKeyName(String key, String data) {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.writeStringValue(key, data);
    }
    public void clearKey() {
        setKeyName(Constant.SE_KEY_NAME, "");
        setKeyName(Constant.SIV_KEY_NAME, "");
    }

}
