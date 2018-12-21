package com.example.fingerprintlandingdemo.biometricPrompt;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fingerprintlandingdemo.R;

/**
 * Created by yudoudou on 2018/12/21.
 */
public class BiometricPromptDialog extends DialogFragment {

    public static final int STATE_NORMAL = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_SUCCEED = 4;
    private static int mPurposeEncrypt;
    private Activity mActivity;
    private OnBiometricPromptDialogActionCallback mDialogActionCallback;
    private TextView tvStateExplain;
    private TextView tvCancel;

    public interface OnBiometricPromptDialogActionCallback {
        void onDialogDismiss();
        void onCancel();
    }

    public static BiometricPromptDialog newInstance(int purposeEncrypt) {
        mPurposeEncrypt = purposeEncrypt;
        BiometricPromptDialog dialog = new BiometricPromptDialog();
        return dialog;
    }

    public void setOnBiometricPromptDialogActionCallback(OnBiometricPromptDialogActionCallback callback) {
        mDialogActionCallback = callback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupWindow(getDialog().getWindow());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_biometric_prompt_dialog, container);
        RelativeLayout rootView = view.findViewById(R.id.root_view);
        tvStateExplain = view.findViewById(R.id.tv_state_explain);
        if (mPurposeEncrypt ==  KeyProperties.PURPOSE_ENCRYPT){
            tvStateExplain.setText(mActivity.getString(R.string.verification_for_login));
        }else {
            tvStateExplain.setText(mActivity.getString(R.string.remind_fingerprint_login));
        }
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialogActionCallback != null) {
                    mDialogActionCallback.onCancel();
                }
                dismiss();
            }
        });
        rootView.setClickable(false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog);
        }
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDialogActionCallback != null) {
            mDialogActionCallback.onDialogDismiss();
        }
    }

    private void setupWindow(Window window) {
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.dimAmount = 0;
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(lp);
            window.setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public void setState(int state) {
        switch (state) {
            case STATE_NORMAL:
                tvStateExplain.setText(mActivity.getString(R.string.verification_for_login));
                break;
            case STATE_FAILED:
                tvStateExplain.setText(mActivity.getString(R.string.fingerprint_verification_failed));
                break;
            case STATE_ERROR:
                tvStateExplain.setText(mActivity.getString(R.string.fingerprint_verification));
                break;
            case STATE_SUCCEED:
                tvStateExplain.setText(mActivity.getString(R.string.fingerprint_verification_succeed));
                break;
        }
    }

}