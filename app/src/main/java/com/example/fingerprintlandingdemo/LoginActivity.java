package com.example.fingerprintlandingdemo;

import android.app.Activity;
import android.content.Intent;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fingerprintlandingdemo.biometricPrompt.BiometricPromptManager;

@RequiresApi(api = 28)
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private BiometricPromptManager mManager;
    private BiometricPromptManager biometricPromptManager;
    private EditText tvUserName;
    private EditText tvPassword;
    private TextView tvLogin;
    private String password;
    private TextView tvInputPassword;
    private CheckBox cbOpenFingerprint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = new BiometricPromptManager(this);
        biometricPromptManager = new BiometricPromptManager(this);
        tvUserName = findViewById(R.id.tv_username);
        tvPassword = findViewById(R.id.tv_password);
        cbOpenFingerprint = findViewById(R.id.cb_open_fingerprint);
        tvInputPassword = findViewById(R.id.tv_input_password);
        tvLogin = findViewById(R.id.tv_login);
        tvUserName.setText("张三");
        tvPassword.setText("password");
        cbOpenFingerprint.setChecked(biometricPromptManager.isBiometricSettingEnable(tvUserName.getText() == null?"":tvUserName.getText().toString()));
        tvInputPassword.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

    }
    private void fingerprintLanding(String userName) {
        if (biometricPromptManager.isBiometricSettingEnable(userName)) {
            //校验手机是否支持指纹登陆
            if (biometricPromptManager.isHardwareDetected()) {
                if (biometricPromptManager.hasEnrolledFingerprints()) {
                    //开启了指纹登陆
                    checkFingerprintLanding(userName);
                } else {
                    //弹框提示是否跳转到系统设置页面
                    biometricPromptManager.setBiometricSettingEnable(false, userName);
                    DialogFactory.showIfJumpToSettingsActivity(this);
                }
            } else {
                Toast.makeText(this, getString(R.string.cant_suport_fingerPrint), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkFingerprintLanding(final String userName) {
        mManager.authenticate(KeyProperties.PURPOSE_DECRYPT, mManager.getPassword(), new BiometricPromptManager.OnBiometricIdentifyCallback() {
            @Override
            public void onSucceeded(String password2) {
                password = password2;
                onLogin(userName, password2);
            }

            @Override
            public void onFailed() {
            }

            @Override
            public void onError(int code, String reason) {
                Toast.makeText(LoginActivity.this, reason, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, getString(R.string.cancel_login_by_fingerprint), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLogin(String userName, String password2) {
        Toast.makeText(this, userName + "登录成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_input_password:
                setPassword(tvUserName.getText().toString().trim());
                break;
            case R.id.tv_login:
                fingerprintLanding(tvUserName.getText().toString().trim());
                break;
        }
    }

    private void setPassword(final String userMobile) {
        if (!biometricPromptManager.isBiometricSettingEnable(userMobile)){
            //校验手机是否支持指纹登陆
            if (biometricPromptManager.isHardwareDetected()){
                if (biometricPromptManager.hasEnrolledFingerprints()){
                    //跳转到密码验证页面
                    checkPassword(userMobile);
                } else {
                    //弹框提示是否跳转到系统设置页面
                    DialogFactory.showIfJumpToSettingsActivity(this);
                    cbOpenFingerprint.setChecked(biometricPromptManager.isBiometricSettingEnable(userMobile));
                    biometricPromptManager.setBiometricSettingEnable(biometricPromptManager.isBiometricSettingEnable(userMobile),userMobile);
                }
            }else {
                cbOpenFingerprint.setChecked(biometricPromptManager.isBiometricSettingEnable(userMobile));
                biometricPromptManager.setBiometricSettingEnable(biometricPromptManager.isBiometricSettingEnable(userMobile),userMobile);
            }
        }else {
            DialogFactory.createWarningDialog(this, 0, "",
                    getResources().getString(R.string.confirm_close_Fingerprint_login), getString(R.string.general_ok),
                    getString(R.string.general_cancel), 0, new DialogFactory.WarningDialogListener() {
                        @Override
                        public void onWarningDialogOK(int id) {
                            cbOpenFingerprint.setChecked(!biometricPromptManager.isBiometricSettingEnable(userMobile));
                            biometricPromptManager.setBiometricSettingEnable(!biometricPromptManager.isBiometricSettingEnable(userMobile),userMobile);
                            biometricPromptManager.clearKey();
                        }

                        @Override
                        public void onWarningDialogCancel(int id) {
                            cbOpenFingerprint.setChecked(biometricPromptManager.isBiometricSettingEnable(userMobile));
                            biometricPromptManager.setBiometricSettingEnable(biometricPromptManager.isBiometricSettingEnable(userMobile),userMobile);
                        }

                        @Override
                        public void onWarningDialogMiddle(int id) {

                        }
                    });
        }
    }

    private void checkPassword(final String userName) {
        final String password = tvPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)){
            return;
        }
        if (password.equals(password)){
            //说明密码正确
            if (mManager.isBiometricPromptEnable()) {
                mManager.authenticate(KeyProperties.PURPOSE_ENCRYPT,password,new BiometricPromptManager.OnBiometricIdentifyCallback() {
                    @Override
                    public void onSucceeded(String password) {
                        //校验成功之后，保存
                        mManager.setPassword(password);
                        biometricPromptManager.setBiometricSettingEnable(!biometricPromptManager.isBiometricSettingEnable(userName),userName);
                        cbOpenFingerprint.setChecked(biometricPromptManager.isBiometricSettingEnable(userName));
                        Toast.makeText(LoginActivity.this,"指纹登陆已开通",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                    }

                    @Override
                    public void onError(int code, String reason) {

                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }
    }
}
