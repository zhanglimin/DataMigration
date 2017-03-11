package org.newstand.datamigration.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Created by Nick@NewStand.org on 2017/3/7 17:42
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public interface ResBinder {
    @StringRes
    int nameRes();

    @DrawableRes
    int iconRes();
}
