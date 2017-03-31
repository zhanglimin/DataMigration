package org.newstand.datamigration.ui.fragment;

import android.content.Context;

import org.newstand.datamigration.common.AbortSignal;
import org.newstand.datamigration.common.StartSignal;
import org.newstand.datamigration.worker.Stats;
import org.newstand.datamigration.worker.backup.session.Session;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Nick@NewStand.org on 2017/3/29 13:27
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public abstract class DataTransportLogicFragment extends DataTransportUIFragment {

    protected static final int STATE_ATTACHED = 0x1;
    protected static final int STATE_DETACHED = 0x2;

    protected static final int STATE_TRANSPORT_START = 0x3;
    protected static final int STATE_TRANSPORT_END = 0x4;
    protected static final int STATE_TRANSPORT_PROGRESS_UPDATE = 0x5;

    @Getter
    private final Set<AbortSignal> abortSignals = new HashSet<>();
    @Getter
    private final Set<StartSignal> startSignals = new HashSet<>();

    @Getter
    @Setter
    private Session session;

    @Setter
    @Getter
    private Stats stats = new MultipleStats();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        enterState(STATE_ATTACHED);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        enterState(STATE_DETACHED);
    }

    class MultipleStats implements Stats {

        Set<Stats> childs;

        public MultipleStats(Stats... child) {
            childs = new HashSet<>();
            Collections.addAll(childs, child);
        }

        @Override
        public int getTotal() {
            int total = 0;
            for (Stats s : childs) {
                total += s.getTotal();
            }
            return total;
        }

        @Override
        public int getLeft() {
            int left = 0;
            for (Stats s : childs) {
                left += s.getLeft();
            }
            return left;
        }

        @Override
        public int getSuccess() {
            int success = 0;
            for (Stats s : childs) {
                success += s.getSuccess();
            }
            return success;
        }

        @Override
        public int getFail() {
            int fail = 0;
            for (Stats s : childs) {
                fail += s.getFail();
            }
            return fail;
        }

        @Override
        public void onSuccess() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onFail() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Stats merge(Stats with) {
            childs.add(with);
            return this;
        }
    }
}