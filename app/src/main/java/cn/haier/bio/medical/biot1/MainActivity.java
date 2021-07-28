package cn.haier.bio.medical.biot1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


import org.eclipse.paho.client.mqttv3.MqttMessage;

import cn.haier.bio.medical.biot.BIOTManager;
import cn.haier.bio.medical.biot.BIOTMqttListener;
import cn.haier.bio.medical.biot.R;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MainActivity extends AppCompatActivity implements BIOTMqttListener {
    String topic;
    int qos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topic = "testtopic/1";
        BIOTManager.getInstance()
                .init(getApplicationContext(),"tcp://192.168.137.1:61613","admin",
                "password").changeListener(this);
        //设置是否自动重连
        BIOTManager.getInstance().setAutoReconnect(false);
        //订阅主题
        BIOTManager.getInstance().setMqttTopic(topic,qos);
//        //发布数据
//        BIOTManager.getInstance().publishMqttData("testtopic1/1","I am a good man",0,true);
//        //发布数据
//        MqttMessage msg = new MqttMessage();
//        msg.setPayload(getTestData());
//        BIOTManager.getInstance().publishMqttData(topic,msg);

    }

    @Override
    public void connectFail(String msg) {
        Log.e("BIOT","connectFail:"+msg);
    }

    @Override
    public void onBIotPrint(String message) {
        Log.i("BIOT","BioT>>>>"+message);
    }

    @Override
    public void connectSuccess(boolean reconnect) {
        Log.i("BIOT","BioT>>>>连接成功,是否为重连：" + reconnect);
    }

    @Override
    public void connectLost() {
        Log.i("BIOT","BioT>>>>connectionLost:断开");
    }

    @Override
    public void receiveMessage(String topic, MqttMessage msg) {
        Log.i("BIOT","BioT>>>>"+topic+ "-->receive message: " + msg.toString());
    }

    @Override
    public void publishComplete(MqttMessage data) {
        Log.i("BIOT","BioT>>>>Mqtt 发布消息完成:"+data);
    }

    @Override
    public void subscribedSuccess(String[] mqttTopic) {
        Log.i("BIOT","BioT>>>>订阅成功" + "topic:" + mqttTopic[0]);
    }

    @Override
    public void unSubscribedSuccess(String[] mqttTopic) {
        Log.i("BIOT","BioT>>>>移除订阅成功" + "topic:" + mqttTopic[0]);
    }

    @Override
    public void subscribedFail(String toString) {
        Log.i("BIOT","BioT>>>>订阅失败:"+toString);
    }

    @Override
    public void unSubscribedFail(String toString) {
        Log.i("BIOT","BioT>>>>移除订阅失败:"+toString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BIOTManager.getInstance().release();
    }


    private byte[] getTestData(){
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0xAF);
        buf.writeByte(0xAF);
        buf.writeByte(0xC1);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        buf.writeByte(0x00);
        byte[] response = new byte[buf.readableBytes()];
        buf.readBytes(response, 0, response.length);
        buf.release();
        return response;
    }
}