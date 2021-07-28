package cn.haier.bio.medical.biot;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class BIOTManager {
    private volatile static BIOTManager manager;
    private BIOTMqtt mqtt;

    public static BIOTManager getInstance(){
        if(manager == null){
            synchronized (BIOTManager.class){
                if(manager == null){
                    manager = new BIOTManager();
                }
            }
        }
        return manager;
    }

    /**
     * 初始化mqtt
     * @param context
     * @param serverUri
     * @param userName
     * @param passWord
     * @return
     */
    public BIOTManager init(Context context, String serverUri, String userName, String passWord) {
        if (this.mqtt == null) {
            this.mqtt = new BIOTMqtt();
            this.mqtt.init(context,serverUri,userName,passWord);
        }
        return manager;
    }

    /**
     * 设置是否自动重连,第二次自动重连后生效
     * @param isAutoReconnect
     */
    public void setAutoReconnect(boolean isAutoReconnect){
        if (null != this.mqtt) {
            this.mqtt.setAutoReconnect(isAutoReconnect);
        }
    }

    /**
     * 发布订阅topic及内容
     * @param topic
     * @param msg
     * @param qos
     * @param retained
     */
    public void publishMqttData(String topic, String msg, int qos, boolean retained){
        if (null != this.mqtt) {
            this.mqtt.publishMessage(topic,msg,qos,retained);
        }
    }
    public void publishMqttData(String topic, MqttMessage msg){
        if (null != this.mqtt) {
            this.mqtt.publishMessage(topic,msg);
        }
    }

    /**
     * 添加订阅主题
     * @param topic
     * @param qos
     */
    public void setMqttTopic(String topic,int qos){
        if (null != this.mqtt) {
            this.mqtt.setTopicAndQos(topic, qos);
        }
    }

    /**
     * 移除订阅主题
     * @param topic
     */
    public void removeMqttTopic(String topic){
        this.mqtt.unsubscribeToTopic(topic);
    }

    /**
     * 释放mqtt
     */
    public void release() {
        if (null != this.mqtt) {
            this.mqtt.release();
            this.mqtt = null;
        }
    }

    /**
     * 设置回调
     * @param listener
     * @return
     */
    public BIOTManager changeListener(BIOTMqttListener listener) {
        if (null != this.mqtt) {
            this.mqtt.changeListener(listener);
        }
        return manager;
    }
}
