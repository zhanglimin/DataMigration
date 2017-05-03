package org.newstand.datamigration.ui.tiles;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import org.newstand.datamigration.R;
import org.newstand.datamigration.data.event.IntentEvents;
import org.newstand.datamigration.policy.ExtraDataRule;
import org.newstand.datamigration.repo.ExtraDataRulesRepoService;
import org.newstand.datamigration.sync.SharedExecutor;
import org.newstand.datamigration.ui.activity.RulesCreatorActivity;
import org.newstand.datamigration.ui.activity.TransitionSafeActivity;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

/**
 * Created by Nick@NewStand.org on 2017/4/6 18:26
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class RulesViewerTile extends QuickTile {

    public RulesViewerTile(@NonNull Context context, final ExtraDataRule rule) {
        super(context, null);

        this.title = TextUtils.isEmpty(rule.getAlias()) ? rule.getPackageName() : rule.getAlias();
        this.iconRes = R.drawable.ic_settings_app;
        this.tileView = new SwitchTileView(getContext()) {
            @Override
            public void onClick(View v) {
                TransitionSafeActivity transitionSafeActivity = (TransitionSafeActivity) getContext();
                Intent intent = new Intent(getContext(), RulesCreatorActivity.class);
                intent.putExtra(IntentEvents.KEY_PKG_NAME, rule.getPackageName());
                transitionSafeActivity.transitionTo(intent);
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                rule.setEnabled(checked);

                SharedExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        ExtraDataRulesRepoService.get().update(getContext(), rule);
                    }
                });
            }

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(rule.isEnabled());
            }
        };
    }

}
