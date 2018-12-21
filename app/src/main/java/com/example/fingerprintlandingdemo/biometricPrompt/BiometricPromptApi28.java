package com.example.fingerprintlandingdemo.biometricPrompt;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;

import com.example.fingerprintlandingdemo.R;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Created by yudoudou on 2018/12/21.
 */
@RequiresApi(Build.VERSION_CODES.P)
public class BiometricPromptApi28 implements IBiometricPromptImpl {
    private Activity mActivity;
    private BiometricPrompt mBiometricPrompt;
    private CancellationSignal mCancellationSignal;
    private FingerprintManager mFingerprintManager;
    private BiometricPromptManager.OnBiometricIdentifyCallback mManagerIdentifyCallback;
    private  int purpose =  KeyProperties.PURPOSE_ENCRYPT;
    private String password;
    private FingerprintManager.AuthenticationCallback mFmAuthCallback;
    public BiometricPromptApi28(Activity activity) {
        mActivity = activity;
        mFingerprintManager = getFingerprintManager(activity);
    }

    @Override
    public void authenticate(int purposeEncrypt, String password, @NonNull CancellationSignal cancel, @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback) {
        this.password = password;
        this.purpose = purposeEncrypt;
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        String title;
        if(purposeEncrypt == KeyProperties.PURPOSE_ENCRYPT){
            //说明是解码
            title = mActivity.getString(R.string.verification_for_login);
        }else {
            title = mActivity.getString(R.string.remind_fingerprint_login);
        }
        //指纹识别的回调
        mManagerIdentifyCallback = callback;
        mBiometricPrompt = new BiometricPrompt
                .Builder(mActivity)
                .setTitle("指纹验证")
                .setDescription(title)
                .setNegativeButton(mActivity.getResources().getString(R.string.cancel), mActivity.getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mCancellationSignal != null)
                            mCancellationSignal.cancel();
                    }
                })
                .build();
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mManagerIdentifyCallback.onCancel();
            }
        });
        try {
            CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
            mBiometricPrompt.authenticate(cryptoObjectHelper.buildCryptoObject(Base64.decode(getIvKeyName(), Base64.URL_SAFE),purposeEncrypt),
                    mCancellationSignal, mActivity.getMainExecutor(), new BiometricPromptCallbackImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private FingerprintManager getFingerprintManager(Context context) {
        if (mFingerprintManager == null) {
            mFingerprintManager = context.getSystemService(FingerprintManager.class);
        }
        return mFingerprintManager;
    }

    public boolean isHardwareDetected() {
        return mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
    }

    public boolean hasEnrolledFingerprints() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }

    private class BiometricPromptCallbackImpl extends BiometricPrompt.AuthenticationCallback  {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            if (errorCode == 5){
                mManagerIdentifyCallback.onCancel();
            }else {
                mManagerIdentifyCallback.onError(errorCode, errString.toString());
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            mManagerIdentifyCallback.onFailed();
        }

        @Override
        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            //校验密码
            if (mManagerIdentifyCallback == null) {
                return;
            }
            if (result.getCryptoObject() == null) {
                mManagerIdentifyCallback.onFailed();
                return;
            }
            final Cipher cipher = result.getCryptoObject().getCipher();
            if (purpose == KeyProperties.PURPOSE_DECRYPT) {
                //取出secret key并返回
                if (TextUtils.isEmpty(password)) {
                    mManagerIdentifyCallback.onFailed();
                    return;
                }
                try {
                    byte[] decrypted = cipher.doFinal(Base64.decode(password, Base64.URL_SAFE));
                    mManagerIdentifyCallback.onSucceeded(new String(decrypted));
                } catch (BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                    mManagerIdentifyCallback.onFailed();
                }
            } else {
                //将前面生成的data包装成secret key，存入沙盒
                try {
                    byte[] encrypted = cipher.doFinal(password.getBytes());
                    byte[] IV = cipher.getIV();
                    String se = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                    //保存解密需要的IV变量
                    String siv = Base64.encodeToString(IV, Base64.URL_SAFE);
                    if (setKeyName(Constant.SE_KEY_NAME,se) && setKeyName(Constant.SIV_KEY_NAME, siv)) {
                        mManagerIdentifyCallback.onSucceeded(se);
                    }else{
                        mManagerIdentifyCallback.onFailed();
                    }
                } catch (BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                    mManagerIdentifyCallback.onFailed();
                }
            }
        }


        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            mManagerIdentifyCallback.onFailed();
        }
    }
    public boolean setKeyName(String key, String data) {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.writeStringValue(key, data);
    }
    private String getIvKeyName() {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.readStringValue(Constant.SIV_KEY_NAME, "");
    }
}
