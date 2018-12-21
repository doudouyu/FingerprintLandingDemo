package com.example.fingerprintlandingdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

class DialogFactory {

    /**
     * 是否跳转到系统页面
     */
    public static void showIfJumpToSettingsActivity(final Activity context) {
        DialogFactory.createWarningDialog(context, 0, "",
                context.getResources().getString(R.string.confirm_jump_to_setting_activity),context.getString(R.string.general_ok),
                context.getString(R.string.general_cancel), 0, new DialogFactory.WarningDialogListener() {
                    @Override
                    public void onWarningDialogOK(int id) {
                        context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }

                    @Override
                    public void onWarningDialogCancel(int id) {

                    }

                    @Override
                    public void onWarningDialogMiddle(int id) {
                    }
                });
    }
    public static Dialog createWarningDialog(final Context context, final int id, String title, String warning, String positiveButton, String negativeButton, int resId,
                                             final WarningDialogListener listener) {
        return createWarningDialog(context, id, title, warning, positiveButton, negativeButton, null, resId, listener);
    }
    public static Dialog createWarningDialog(final Context context, final int id, String title, String warning, String positiveButton, String negativeButton, String middleButton, int resId,
                                             final WarningDialogListener listener) {
        return createWarningDialog(context, id, title, warning, positiveButton, negativeButton, middleButton, resId, listener, true);
    }
    public static Dialog createWarningDialog(final Context context, final int id, String title, String warning, String positiveButton, String negativeButton, String middleButton, int resId,
                                             final WarningDialogListener listener, final boolean isback) {

        final Dialog dialog = new Dialog(context, R.style.MyDialog);
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        dialog.setContentView(R.layout.dialog_warningdialog_normal);

        TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_warning_title);
        TextView dialogContent = (TextView) dialog.findViewById(R.id.dialog_warning_content);
        LinearLayout doubleBtnLayout = (LinearLayout) dialog.findViewById(R.id.ll_dialog_bottom_button);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button btnOK = (Button) dialog.findViewById(R.id.btn_ok);
        Button btnSingleOK = (Button) dialog.findViewById(R.id.btn_single_ok);
        dialog.setCanceledOnTouchOutside(false);
        if (TextUtils.isEmpty(title)) {
            dialogTitle.setVisibility(View.GONE);
        } else {
            dialogTitle.setVisibility(View.VISIBLE);
            dialogTitle.setText(title);
        }

        if (TextUtils.isEmpty(warning)) {
            dialogContent.setVisibility(View.GONE);
        } else {
            dialogContent.setVisibility(View.VISIBLE);
            dialogContent.setText(warning);
        }

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                if (listener != null) {
                    listener.onWarningDialogCancel(id);
                }
            }
        });
        if (!TextUtils.isEmpty(positiveButton)) {
            btnOK.setText(positiveButton);
            btnSingleOK.setText(positiveButton);
            btnOK.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (listener != null) {
                        listener.onWarningDialogOK(id);
                    }

                    dialog.dismiss();
                }
            });
        }
        if (!TextUtils.isEmpty(negativeButton)) {
            btnCancel.setText(negativeButton);
            btnCancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (listener != null) {
                        listener.onWarningDialogCancel(id);
                    }
                    dialog.dismiss();
                }
            });
        } else {
            doubleBtnLayout.setVisibility(View.INVISIBLE);
            btnSingleOK.setVisibility(View.VISIBLE);

        }
        btnSingleOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (listener != null) {
                    listener.onWarningDialogOK(id);
                }
                dialog.dismiss();
            }
        });

        if (!((Activity) context).isFinishing()) {
            dialog.show();
        }
        return dialog;
    }

    /**
     * 警告型Dialog的回调
     */
    public interface WarningDialogListener {
        public void onWarningDialogOK(int id);

        public void onWarningDialogCancel(int id);

        public void onWarningDialogMiddle(int id);

    }
    /**
     * 文本框和一个确定按钮
     */
    public static void createNoCancelDialog(Context mContext) {
        final Dialog noticeDialog  = new Dialog(mContext,R.style.dialog_version_update_style);
        View view = View.inflate(mContext,R.layout.dialog_no_cancel_only_text,null);
        TextView tvTitle = (TextView)view.findViewById(R.id.tv_title);
        TextView tvConfirm = (TextView)view.findViewById(R.id.tv_confirm);
        tvTitle.setText(mContext.getResources().getString(R.string.please_open_fingerprint_landing));
        noticeDialog.setContentView(view);
        noticeDialog.setCancelable(false);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticeDialog.dismiss();
            }
        });
        noticeDialog.show();
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        Window dialogWindow = noticeDialog.getWindow();
        dialogWindow.setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (width * 0.7); // 宽度
        dialogWindow.setAttributes(lp);
    }
}
