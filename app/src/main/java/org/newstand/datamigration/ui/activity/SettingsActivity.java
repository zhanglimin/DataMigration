package org.newstand.datamigration.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.newstand.datamigration.R;
import org.newstand.datamigration.ui.tiles.DonateTile;
import org.newstand.datamigration.ui.tiles.LicenceTile;
import org.newstand.datamigration.ui.tiles.ThemedCategory;
import org.newstand.datamigration.ui.tiles.TransitionAnimationTile;
import org.newstand.datamigration.ui.tiles.WorkModeTile;

import java.util.List;

import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;

/**
 * Created by Nick@NewStand.org on 2017/3/14 17:49
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class SettingsActivity extends TransitionSafeActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHomeAsUp();
        setTitle(getTitle());
        setContentView(R.layout.activity_with_container_template);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, SettingsFragment.getInstance()).commit();
    }

    public static class SettingsFragment extends DashboardFragment {
        public static SettingsFragment getInstance() {
            return new SettingsFragment();
        }

        @Override
        protected void onCreateDashCategories(List<Category> categories) {

            Category view = new ThemedCategory();
            view.titleRes = R.string.tile_category_view;
            TransitionAnimationTile animationTile = new TransitionAnimationTile(getContext());
            view.addTile(animationTile);


            Category strategy = new ThemedCategory();
            view.titleRes = R.string.tile_category_strategy;
            WorkModeTile workModeTile = new WorkModeTile(getContext());
            strategy.addTile(workModeTile);

            Category about = new ThemedCategory();
            about.titleRes = R.string.tile_category_about;
            LicenceTile licenceTile = new LicenceTile(getActivity());
            about.addTile(licenceTile);
            DonateTile donateTile = new DonateTile(getContext());
            about.addTile(donateTile);

            categories.add(view);
            categories.add(strategy);
            categories.add(about);

            super.onCreateDashCategories(categories);
        }
    }
}
