package com.example.fingerprintlandingdemo.biometricPrompt;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Created by yudoudou on 2018/12/21.
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public class BiometricPromptApi23  implements IBiometricPromptImpl {

    private static final String TAG = "BiometricPromptApi23";
    private Activity mActivity;
    private BiometricPromptDialog mDialog;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private BiometricPromptManager.OnBiometricIdentifyCallback mManagerIdentifyCallback;
    private FingerprintManager.AuthenticationCallback mFmAuthCallback
            = new FingerprintManageCallbackImpl();
    private  int purpose =  KeyProperties.PURPOSE_ENCRYPT;
    private String password;
    public BiometricPromptApi23(Activity activity) {
        mActivity = activity;
        mFingerprintManager = getFingerprintManager(activity);
    }

    @Override
    public void authenticate(int purposeEncrypt, String password, @Nullable CancellationSignal cancel,
                             @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback) {
        this.password = password;
        this.purpose = purposeEncrypt;
        //指纹识别的回调
        mManagerIdentifyCallback = callback;
        if(purposeEncrypt == KeyProperties.PURPOSE_ENCRYPT){
            //说明是解码
            mDialog = BiometricPromptDialog.newInstance(KeyProperties.PURPOSE_ENCRYPT);
        }else {
            mDialog = BiometricPromptDialog.newInstance(KeyProperties.PURPOSE_DECRYPT);
        }

        mDialog.setOnBiometricPromptDialogActionCallback(new BiometricPromptDialog.OnBiometricPromptDialogActionCallback() {
            @Override
            public void onDialogDismiss() {
                //当dialog消失的时候，包括点击userPassword、点击cancel、和识别成功之后
                if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                    mCancellationSignal.cancel();
                }
            }
            @Override
            public void onCancel() {
                //点击cancel键
                if (mManagerIdentifyCallback != null) {
                    mManagerIdentifyCallback.onCancel();
                }
            }
        });
        mDialog.show(mActivity.getFragmentManager(), "BiometricPromptApi23");

        mCancellationSignal = cancel;
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                if(mDialog!=null){
                    mDialog.dismiss();
                }
            }
        });

        try {
            CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
            getFingerprintManager(mActivity).authenticate(
                    cryptoObjectHelper.buildCryptoObject(purposeEncrypt,Base64.decode(getIvKeyName(), Base64.URL_SAFE)), mCancellationSignal,
                    0, mFmAuthCallback, null);
        } catch (Exception e) {
            mManagerIdentifyCallback.onError(200,"");
            e.printStackTrace();
        }
    }

    private String getIvKeyName() {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.readStringValue(Constant.SIV_KEY_NAME, "");
    }

    private class FingerprintManageCallbackImpl extends FingerprintManager.AuthenticationCallback {

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Log.d(TAG, "onAuthenticationError() called with: errorCode = [" + errorCode + "], errString = [" + errString + "]");
            if (mDialog!=null){
                mDialog.dismiss();
            }
            if (errorCode == 5){
                mManagerIdentifyCallback.onCancel();
            }else {
                mManagerIdentifyCallback.onError(errorCode, errString.toString());
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.d(TAG, "onAuthenticationFailed() called");
            if (mDialog!=null){
                mDialog.setState(BiometricPromptDialog.STATE_FAILED);
            }
            mManagerIdentifyCallback.onFailed();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            if (mDialog != null ){
                mDialog.setState(BiometricPromptDialog.STATE_FAILED);
            }
            mManagerIdentifyCallback.onFailed();

        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            if (mDialog!= null){
                mDialog.setState(BiometricPromptDialog.STATE_SUCCEED);
            }
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
                    if (mDialog!= null){
                        mDialog.dismiss();
                    }
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
                        if (mDialog!= null){
                            mDialog.dismiss();
                        }
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
    }

    private FingerprintManager getFingerprintManager(Context context) {
        if (mFingerprintManager == null) {
            mFingerprintManager = context.getSystemService(FingerprintManager.class);
        }
        return mFingerprintManager;
    }

    public boolean isHardwareDetected() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.isHardwareDetected();
        }
        return false;
    }

    public boolean hasEnrolledFingerprints() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }
    public boolean setKeyName(String key, String data) {
        SharePreferenceUtil sharePreferenceUtil = new SharePreferenceUtil(mActivity, Constant.KEY_BIOMETRIC_FILE_NAME);
        return sharePreferenceUtil.writeStringValue(key, data);
    }
}