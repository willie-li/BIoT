package cn.haier.bio.medical.biot;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface BIOTMqttListener{
    void connectFail(String msg);
    void onBIotPrint(String message);
    void connectSuccess(boolean reconnect);
    void connectLost();
    void receiveMessage(String topic, MqttMessage msg);
    void publishComplete(MqttMessage data);
    void subscribedSuccess(String[] mqttTopic);
    void unSubscribedSuccess(String[] mqttTopic);
    void subscribedFail(String toString);
    void unSubscribedFail(String toString);
}
