package cn.haier.bio.medical.biot.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import java.util.ArrayList;
import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private List<NetStateChangeObserver> mObservers = new ArrayList<>();
    private int mType = -1;
    private static boolean isRegister = false;

    private static class InstanceHolder {
        private static final NetworkChangeReceiver INSTANCE = new NetworkChangeReceiver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            int connectivityStatus = NetworkUtil.getConnectivityStatus(context);
            notifyObservers(connectivityStatus);
        }

    }

    public static void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(InstanceHolder.INSTANCE, intentFilter);
        isRegister = true;
    }

    public static void unRegisterReceiver(Context context) {
        if (isRegister) {
            context.unregisterReceiver(InstanceHolder.INSTANCE);
        }
    }

    public static void registerObserver(Context context,NetStateChangeObserver observer) {
        if (observer == null) {
            return;
        }
        if (!InstanceHolder.INSTANCE.mObservers.contains(observer)) {
            InstanceHolder.INSTANCE.mObservers.add(observer);
        }
        registerReceiver(context);
    }

    public static void unRegisterObserver(Context context,NetStateChangeObserver observer) {
        if (observer == null) {
            return;
        }
        if (InstanceHolder.INSTANCE.mObservers == null) {
            return;
        }
        InstanceHolder.INSTANCE.mObservers.remove(observer);
        unRegisterReceiver(context);
    }

    private void notifyObservers(int networkType) {
        if (mType == networkType) {
            return;
        }
        mType = networkType;
        if (networkType == NetworkUtil.TYPE_MOBILE) {
            for (NetStateChangeObserver observer : mObservers) {
                observer.onMobileConnect();
            }
        } else if (networkType == NetworkUtil.TYPE_WIFI) {
            for (NetStateChangeObserver observer : mObservers) {
                observer.onWifiConnect();
            }
        } else {
            for (NetStateChangeObserver observer : mObservers) {
                observer.onDisconnect();
            }
        }
    }

    public interface NetStateChangeObserver {

        void onDisconnect();

        void onMobileConnect();

        void onWifiConnect();
    }
}
