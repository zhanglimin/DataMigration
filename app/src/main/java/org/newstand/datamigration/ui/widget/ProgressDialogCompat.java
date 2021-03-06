package org.newstand.datamigration.ui.widget;

import android.app.ProgressDialog;
import android.content.Context;

import org.newstand.datamigration.sync.SharedExecutor;

/**
 * Created by Nick@NewStand.org on 2017/3/9 16:28
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class ProgressDialogCompat {

    public static ProgressDialog createUnCancelableIndeterminate(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        return progressDialog;
    }

    public static ProgressDialog createUnCancelableIndeterminateShow(Context context) {
        final ProgressDialog progressDialog = createUnCancelableIndeterminate(context);
        SharedExecutor.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
        return progressDialog;
    }

    public static ProgressDialog createUnCancelableIndeterminateShow(Context context, String message) {
        ProgressDialog progressDialog = createUnCancelableIndeterminate(context);
        progressDialog.setMessage(message);
        progressDialog.show();
        return progressDialog;
    }

    public static void dismiss(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
