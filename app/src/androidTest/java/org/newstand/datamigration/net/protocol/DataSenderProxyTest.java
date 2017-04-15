package org.newstand.datamigration.net.protocol;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.newstand.datamigration.cache.LoadingCacheManager;
import org.newstand.datamigration.common.AbortSignal;
import org.newstand.datamigration.common.Consumer;
import org.newstand.datamigration.common.Holder;
import org.newstand.datamigration.data.model.DataCategory;
import org.newstand.datamigration.data.model.DataRecord;
import org.newstand.datamigration.net.server.ErrorCode;
import org.newstand.datamigration.net.server.TransportClient;
import org.newstand.datamigration.net.server.TransportClientProxy;
import org.newstand.datamigration.net.server.TransportServer;
import org.newstand.datamigration.net.server.TransportServerProxy;
import org.newstand.datamigration.provider.SettingsProvider;
import org.newstand.datamigration.strategy.Interval;
import org.newstand.datamigration.sync.Sleeper;
import org.newstand.datamigration.utils.Collections;
import org.newstand.datamigration.worker.transport.TransportListenerAdapter;
import org.newstand.logger.Logger;

import java.util.Random;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Nick@NewStand.org on 2017/4/13 13:25
 * E-Mail: NewStand@163.com
 * All right reserved.
 */
@RunWith(AndroidJUnit4.class)
public class DataSenderProxyTest {

    private TransportServer mServer;
    private TransportClient mClient;

    @Getter
    @Setter
    private boolean cancelable;

    private void mokeChooseData() {

        LoadingCacheManager.createDroid(InstrumentationRegistry.getTargetContext());
        LoadingCacheManager cacheManager = LoadingCacheManager.droid();
        Assert.assertTrue(cacheManager != null);

        Collections.consumeRemaining(cacheManager.get(DataCategory.Contact), new Consumer<DataRecord>() {
            @Override
            public void accept(@NonNull DataRecord dataRecord) {
                dataRecord.setChecked(new Random().nextBoolean());
            }
        });
        Collections.consumeRemaining(cacheManager.get(DataCategory.Photo), new Consumer<DataRecord>() {
            @Override
            public void accept(@NonNull DataRecord dataRecord) {
                dataRecord.setChecked(new Random().nextBoolean());
            }
        });
        Collections.consumeRemaining(cacheManager.get(DataCategory.App), new Consumer<DataRecord>() {
            @Override
            public void accept(@NonNull DataRecord dataRecord) {
                dataRecord.setChecked(new Random().nextBoolean());
            }
        });
    }

    private void mokeServerThenClient() {
        TransportServerProxy.startWithPenitentialPortsAsync("localhost", SettingsProvider.getTransportServerPorts(),
                new TransportServer.ChannelHandlerAdapter() {
                    @Override
                    public void onServerCreateFail(ErrorCode errCode) {
                        Assert.fail(errCode.toString());
                    }

                    @Override
                    public void onServerChannelCreate() {
                        mokeClient();
                    }

                    @Override
                    public void onClientChannelCreated() {
                        Logger.d("onClientChannelCreated");
                        mokeSend();
                    }
                }, new Consumer<TransportServer>() {
                    @Override
                    public void accept(@NonNull TransportServer transportServer) {
                        mServer = transportServer;
                    }
                });

    }

    private void mokeClient() {
        TransportClientProxy.startWithPenitentialPortsAsync("localhost", SettingsProvider.getTransportServerPorts(),
                new TransportClient.ChannelHandler() {
                    @Override
                    public void onServerChannelConnected() {
                        Logger.d("onServerChannelConnected");
                        dump();
                        mokeReceive();
                    }

                    @Override
                    public void onClientStop() {

                    }

                    @Override
                    public void onServerChannelConnectedFailure(ErrorCode errCode) {
                        Assert.fail(errCode.toString());
                    }
                }, new Consumer<TransportClient>() {
                    @Override
                    public void accept(@NonNull TransportClient transportClient) {
                        mClient = transportClient;
                    }
                });
    }

    private void mokeSend() {
        final Holder<Integer> count = new Holder<>();

        Sleeper.sleepQuietly(Interval.Seconds.getIntervalMills());

        final AbortSignal abortSignal = new AbortSignal();

        DataSenderProxy.send(InstrumentationRegistry.getTargetContext(), mClient, new TransportListenerAdapter() {

            @Override
            public void onStart() {
                super.onStart();
                count.setData(getStats().getTotal());
            }

            @Override
            public void onPieceSuccess(DataRecord record) {
                super.onPieceSuccess(record);
                Logger.d("send onPieceSuccess %s %s", record, getStats());

                // Cancel?
                if (isCancelable() && getStats().getLeft() <= count.getData() / 2) {
                    Logger.w("*********************Aborting ~~~~~~~~~~~~~~~~~~");
                    abortSignal.abort();
                }
            }

            @Override
            public void onPieceFail(DataRecord record, Throwable err) {
                super.onPieceFail(record, err);
                Logger.d("send onPieceFail %s %s", record, getStats());
            }

            @Override
            public void onPieceStart(DataRecord record) {
                super.onPieceStart(record);
                Logger.d("send onPieceStart %s", record);
            }

            @Override
            public void onComplete() {
                super.onComplete();
                Logger.d("send onComplete~~");
            }

            @Override
            public void onAbort(Throwable err) {
                super.onAbort(err);
                Logger.d("send onAbort %s", Logger.getStackTraceString(err));
            }
        }, abortSignal);
    }

    private void mokeReceive() {
        Sleeper.sleepQuietly(Interval.Seconds.getIntervalMills());
        DataReceiverProxy.receive(InstrumentationRegistry.getTargetContext(), mServer, new TransportListenerAdapter() {
            @Override
            public void onPieceSuccess(DataRecord record) {
                super.onPieceSuccess(record);
                Logger.d("receive onPieceSuccess %s %s", record, getStats());
            }

            @Override
            public void onPieceFail(DataRecord record, Throwable err) {
                super.onPieceFail(record, err);
                Logger.d("receive onPieceFail %s %s", record, getStats());
            }

            @Override
            public void onPieceStart(DataRecord record) {
                super.onPieceStart(record);
                Logger.d("receive onPieceStart %s", record);
            }

            @Override
            public void onComplete() {
                super.onComplete();
                Logger.d("receive onComplete~~");
            }

            @Override
            public void onAbort(Throwable err) {
                super.onAbort(err);
                Logger.d("receive onAbort %s", Logger.getStackTraceString(err));
            }
        });
    }

    private void dump() {
        Logger.d("Server %s", mServer);
        Logger.d("Client %s", mClient);
    }

    @Test
    public void send() throws Exception {
        mokeChooseData();

        mokeServerThenClient();

        Sleeper.sleepQuietly(Interval.Day.getIntervalMills());
    }

    @Test
    public void sendAndCancel() throws Exception {

        setCancelable(true);

        mokeChooseData();

        mokeServerThenClient();

        Sleeper.sleepQuietly(Interval.Day.getIntervalMills());
    }

}