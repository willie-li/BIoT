package cn.haier.bio.medical.biot;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.ref.WeakReference;

import cn.haier.bio.medical.biot.db.DBHelper;
import cn.haier.bio.medical.biot.db.DatabaseManger;
import cn.haier.bio.medical.biot.db.MqttModel;
import cn.haier.bio.medical.biot.wifi.NetworkChangeReceiver;
import cn.haier.bio.medical.biot.wifi.NetworkUtil;

public class BIOTMqtt implements NetworkChangeReceiver.NetStateChangeObserver {
    private static BIOTMqtt mqtt;
    private String serverUri;//(协议+地址+端口号)
    private String userName;
    private String passWord;
    private String clientId;
    private Context mContext;
    private volatile MqttAndroidClient mqttAndroidClient;
//    private MqttCallback callback;
    private String mqttTopic;
    private int qos;
    private boolean autoReconnect = true;
    private WeakReference<BIOTMqttListener> listener;


    public BIOTMqtt(){}

    public void init(Context context, String serverUri, String userName,
                      String passWord){
        this.serverUri = serverUri;
        this.userName = userName;
        this.passWord = passWord;
        mContext = context;
        clientId = MqttClient.generateClientId();
        NetworkChangeReceiver.registerObserver(mContext,this);
        DatabaseManger.getInstance().init(context);
        initConnect();
    }

    public void changeListener(BIOTMqttListener listener) {
        if(!NetworkUtil.isNetworkAvailable(mContext)){
            listener.onBIotPrint("Mqtt 网络不可用");
        }
        this.listener = new WeakReference<>(listener);
    }

    private boolean isConnect() {
        if (mqttAndroidClient != null) {
            return mqttAndroidClient.isConnected();
        }
        return false;
    }

    private void initConnect() {
        if (isConnect()) {
            return;
        }
        mqttAndroidClient = new MqttAndroidClient(mContext, serverUri, clientId);
        mqttAndroidClient.close();
        mqttAndroidClient.registerResources(mContext);
        mqttAndroidClient.setCallback(new MyMqttCallbackExtended());
        //mqtt连接参数设置
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        //设置自动重连
        mqttConnectOptions.setAutomaticReconnect(autoReconnect);
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录
        // 这里设置为true表示每次连接到服务器都以新的身份连接
        mqttConnectOptions.setCleanSession(false);
        //设置连接的用户名
        mqttConnectOptions.setUserName(userName);
        //设置连接的密码
        mqttConnectOptions.setPassword(passWord.toCharArray());
        // 设置超时时间 单位为秒
        mqttConnectOptions.setConnectionTimeout(10);
        // 设置会话心跳时间 单位为秒 服务器会每隔20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        mqttConnectOptions.setKeepAliveInterval(20);
        try {
            mqttAndroidClient.connect(mqttConnectOptions);
            DatabaseManger.getInstance().insertData("guoguoguo");
            Log.d("ddd","size:"+ DatabaseManger.getInstance().queryAll().toString());
        } catch (Exception e) {
            e.printStackTrace();
            if (null != this.listener && null != this.listener.get()) {
                listener.get().connectFail("未设置BIOTMqttListener监听："+e.toString());
            }
        }
    }

    /**
     * 设置是否自动重连,默认为true
     *
     * @param isAuto
     */
    public void setAutoReconnect(boolean isAuto) {
        autoReconnect = isAuto;
    }

    /**
     * 修改订阅主题
     *
     * @param topic 主题
     * @param qos    QOS ＝　0/1/2  　最多一次　　最少一次　多次
     */
    public void setTopicAndQos(String topic, int qos) {
        this.mqttTopic = topic;
        this.qos = qos;
        subscribeToTopic();
    }

    /**
     *移除订阅主题
     * @param topic
     */
    public void unsubscribeToTopic(String topic) {
        this.mqttTopic = topic;
        if (mqttAndroidClient != null || !mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.unsubscribe(mqttTopic,mContext.getApplicationContext(), new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        if (null != listener && null != listener.get()) {
                            listener.get().unSubscribedSuccess(new String[]{mqttTopic});
                        }

                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        if (null != listener && null != listener.get()) {
                            listener.get().unSubscribedFail(exception.toString());
                        }
                    }

                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 订阅主题
     */
    private void subscribeToTopic() {
        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected() || TextUtils.isEmpty(mqttTopic)) {
            return;
        }
        try {
            mqttAndroidClient.subscribe(mqttTopic, qos, mContext.getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if (null != listener && null != listener.get()) {
                        listener.get().subscribedSuccess(new String[]{mqttTopic});
                    }

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if (null != listener && null != listener.get()) {
                        listener.get().subscribedFail(exception.toString());
                    }
                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
            if (null != listener && null != listener.get()) {
                listener.get().subscribedFail(e.toString());
            }
        }
    }

    /**
     * 发布
     * @param topic
     * @param msg
     * @param qos
     * @param retained
     */
    public void publishMessage(String topic, String msg, int qos, boolean retained) {
        if (isConnect()) {
            try {
                mqttAndroidClient.publish(topic, msg.getBytes(), qos, retained);
                listener.get().onBIotPrint("Mqtt 发布消息：" + msg);
                if (!mqttAndroidClient.isConnected()) {
                    listener.get().onBIotPrint("Mqtt服务器已断开:"+mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
                }
            } catch (MqttException e) {
                listener.get().onBIotPrint("Error Publishing: " + e.toString());
            }
        }
    }
    public void publishMessage(String topic, MqttMessage msg) {
        if (isConnect()) {
            try {
                mqttAndroidClient.publish(topic, msg);
                listener.get().onBIotPrint("Mqtt 发布消息：" + toHexString(msg.getPayload()));
                if (!mqttAndroidClient.isConnected()) {
                    listener.get().onBIotPrint("Mqtt服务器已断开:"+mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
                }
            } catch (MqttException e) {
                listener.get().onBIotPrint("Error Publishing: " + e.toString());
            }
        }
    }

    public void release() {
        if (mqttAndroidClient == null) {
            return;
        }
        try {
            mqttAndroidClient.close();
            mqttAndroidClient.disconnect();
            mqttAndroidClient.unregisterResources();
            mqtt = null;
            DatabaseManger.getInstance().close();
            NetworkChangeReceiver.unRegisterObserver(mContext,this);
            mqttAndroidClient = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect() {
        listener.get().onBIotPrint("network onDisconnect");
    }

    @Override
    public void onMobileConnect() {
        listener.get().onBIotPrint("network onMobileConnect");
    }

    @Override
    public void onWifiConnect() {
        listener.get().onBIotPrint("network onWifiConnect");
    }

    public class MyMqttCallbackExtended implements MqttCallbackExtended {
        /**
         * 连接完成回调
         *
         * @param reconnect true 断开重连,false 首次连接
         * @param serverURI 服务器URI
         */
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            if (null != listener && null != listener.get()) {
                listener.get().connectSuccess(reconnect);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            if (null != listener && null != listener.get()) {
                listener.get().connectLost();
            }
        }
        /**
         * 消息接收，如果在订阅的时候没有设置IMqttMessageListener，那么收到消息则会在这里回调。
         * 如果设置了IMqttMessageListener，则消息回调在IMqttMessageListener中
         *
         * @param topic
         * @param message
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (null != listener && null != listener.get()) {
                listener.get().receiveMessage(topic, message);
            }
        }
        /**
         * 交付完成回调。在publish消息的时候会收到此回调.
         * qos:
         * 0 发送完则回调
         * 1 或 2 会在对方收到时候回调
         *
         * @param token
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            if (null != listener && null != listener.get()) {
                try {
                    listener.get().publishComplete(token.getMessage());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public  String toHexString(byte[] data) {
        String s = "";
        for (int i = 0; i < data.length; ++i) {
            s += String.format("%02X ", data[i]);
        }
        return s;
    }
}
