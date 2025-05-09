package cn.haier.bio.medical.biot1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


import org.eclipse.paho.client.mqttv3.MqttMessage;

import cn.haier.bio.medical.biot.BIOTManager;
import cn.haier.bio.medical.biot.BIOTMqttListener;
import cn.haier.bio.medical.biot.R;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MainActivity extends AppCompatActivity implements BIOTMqttListener, View.OnClickListener {
    String topic;
    int qos = 0;
    Button mBtn1;
    Button mBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn1 = findViewById(R.id.tv_hello);
        mBtn1.setOnClickListener(this);
        mBtn2 = findViewById(R.id.tv_hello2);
        mBtn2.setOnClickListener(this);
        topic = "status/handle/dev type/6666";
        BIOTManager.getInstance()
                .init(getApplicationContext(),"tcp://msgtest.haierbiomedical.com:1777","haier",
                "1234").changeListener(this);
        //设置是否自动重连
        BIOTManager.getInstance().setAutoReconnect(false);
        //订阅主题
        BIOTManager.getInstance().setMqttTopic(topic,qos);
//        //发布数据
//        BIOTManager.getInstance().publishMqttData("testtopic1/1","I am a good man",0,true);
//        //发布数据


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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_hello:
                BIOTManager.getInstance().setMqttTopic(topic,qos);
                break;
            case R.id.tv_hello2:
                MqttMessage msg = new MqttMessage();
                msg.setPayload(getTestData());
//                BIOTManager.getInstance().publishMqttData("/BE0FT3E1T/BE0FT3E1T00QGLB90014/user/update",msg);
                BIOTManager.getInstance().publishMqttData("/BE0FT3E1T/BE0FT3E1T00QGLB90014/user/update","msg",0,false);
                break;
        }
    }
}