package cn.rh.iot.mqtt;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Device;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class MqttChannel {

    private final int RECONNECT_INTERVAL;
    private final MqttConnectOptions options;
    private final MqttClientPersistence persistence;

    @Getter @Setter
    private MqttClient client;

    @Getter
    private final Device device;

    private final String host;

    @Getter
    private final Lock disConnectLocker=new ReentrantLock();

    @Setter
    private boolean isDisconnecting =false;

    public MqttChannel(Device device) {
        this.device = device;
        this.host= IotConfig.getInstance().getMqtt().getServerURI();

        RECONNECT_INTERVAL= IotConfig.getInstance().getMqtt().getReconnectInterval()*1000;

        persistence=new MemoryPersistence();
        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(IotConfig.getInstance().getMqtt().getUsername());
        options.setPassword(IotConfig.getInstance().getMqtt().getPassword().toCharArray());
        options.setConnectionTimeout(IotConfig.getInstance().getMqtt().getConnectionTimeout());
        options.setKeepAliveInterval(IotConfig.getInstance().getMqtt().getKeepAliveInterval());
    }

    public boolean isDisconnecting(){
        return this.isDisconnecting;
    }

    public void Write(String topic,int qos,String send_message) {

        if(client==null || !client.isConnected()){
            return;
        }

        MqttMessage msg=new MqttMessage();
        msg.setQos(qos);
        msg.setPayload(send_message.getBytes(StandardCharsets.UTF_8));
        try {
            client.publish(topic,msg);
            log.info("[{}]发送[{}]成功，内容( {} )", device.getName(),topic,send_message);
        }catch (MqttException ex) {
            log.error("[{}]发送[{}]失败，内容( {} )", device.getName(),topic,send_message);
        }
    }

    public void Disconnect() {
        if(client==null){
            return;
        }

        try{
            isDisconnecting =true;
            client.disconnect();
            client.close();
            log.info("[{}]与主动MQTT服务器断开连接", device.getName());

        }catch(MqttException ex){
            log.error("[{}]与主动MQTT服务器断开连接失败，原因：{}:", device.getName(), ex.getMessage());
        }finally {
            isDisconnecting =false;
        }
    }

    public void Connect() {
        if(client!=null && client.isConnected()){
            return;
        }
        isDisconnecting =false;
        try {
            String clientId= device.getName()+"_"+ System.currentTimeMillis();  //避免clientId重复
            client = new MqttClient(host, clientId, persistence);
            client.setCallback(new MqttCallbackObject(device, this));
            client.connect(options);
        } catch(MqttException ex) {
            log.error("[{}]连接MQTT服务器失败，错误码：{}", device.getName() , ex.getReasonCode());
            client=null;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    log.error("设备[{}]重连MQTT服务器...",device.getName());
                    Connect();
                }
            }, RECONNECT_INTERVAL);
        }
    }

    public boolean isConnected() {
        if(client==null){
            return false;
        }
        return client.isConnected();
    }

    public void SendConnectStateTopic(String state){

        if(!isConnected()) {
            return;
        }

        String connStateJson="{\n" +
                "\"deviceName\": \""+ device.getName()+"\",\n" +
                "\"deviceNumber\":\"\",\n" +
                "\"msgId\":1,\n" +
                "\"payload\":{ \n" +
                "\"connectState\":\""+state+"\"\n" +
                "\t}\n" +
                "}";

        try {
            MqttMessage msg=new MqttMessage();
            msg.setQos(device.getPubTopic().getQos());
            msg.setPayload(connStateJson.getBytes(StandardCharsets.UTF_8));

            client.publish(device.getPubTopic().getTopic(), msg);
            log.info("[{}]发送信道状态报文成功,状态[{}]", device.getName(), state);

        }catch (MqttException ex) {
            log.error("[{}]发送信道状态报文失败,状态[{}], 原因：{}", device.getName(),state,ex.getMessage());
        }
    }
}
