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

/**
 * @Program: IOT_Controller
 * @Description: 通过MQTT协议订阅和发布
 * @Author: Y.Y
 * @Create: 2020-09-20 12:53
 **/
@Slf4j
public class MqttChannel {

    private final int RECONNECT_INTERVAL;
    private final MqttConnectOptions options;
    private final MqttClientPersistence persistence;

    @Getter
    private MqttClient client;

    @Getter
    private final Device device;
    private final String host;

    @Setter
    private boolean isInitiativeDisconnecting=false;

    public MqttChannel(Device device) {
        this.device=device;
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

    public boolean isInitiativeDisconnecting(){
        return this.isInitiativeDisconnecting;
    }

    public void Write(String send_message) {

        if(client==null || !client.isConnected()){
            return;
        }
        if(device.getPublishTopicParam()==null){
            return;
        }

        MqttMessage msg=new MqttMessage();
        msg.setQos(device.getPublishTopicParam().getQos());
        msg.setPayload(send_message.getBytes(StandardCharsets.UTF_8));
        try {
            client.publish(device.getPublishTopicParam().getTopic(),msg);
         }catch (MqttException ex) {
        }
    }

    public void Disconnect() {
        if(client==null){
            return;
        }
        try {
            isInitiativeDisconnecting=true;
            client.disconnect();
            client.close();
            log.info("设备[{}]与MQTT服务器断开连接",device.getName());
        } catch(MqttException ex){
            log.warn(device.getName() + "与MQTT服务器断开连接失败，因为:"+ex.getMessage());
        }
    }

    public void Connect(){
        if(client!=null && client.isConnected()){
            return;
        }
        try {
            String clientId=device.getName();
            if(device.getId()!=null && !device.getId().equals("")){
                clientId=clientId+"_"+device.getId();
            }
            client = new MqttClient(host, clientId, persistence);
            client.setCallback(new MqttCallbackObject(device, this));

            client.connect(options);
        } catch(MqttException ex) {
            //如果第一次连接不到服务器，则不断尝试连接
            log.error("设备[{}]连接MQTT服务器失败，错误码：{}",device.getName() , ex.getReasonCode());
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
        if(client!=null){
            return client.isConnected();
        }
        return false;
    }

    public void SendConnectStateMessage(String state){

        if(!isConnected()) {
            return;
        }

        String connStateJson="{\n" +
                "\"deviceName\": \""+device.getName()+"\",\n" +
                "\"deviceNumber\":\""+device.getId()+"\",\n" +
                "\"msgId\":1,\n" +
                "\"payload\":{ \n" +
                "\"connectState\":\""+state+"\"\n" +
                "\t}\n" +
                "}";
        MqttMessage msg=new MqttMessage();
        msg.setQos(device.getPublishTopicParam().getQos());
        msg.setPayload(connStateJson.getBytes(StandardCharsets.UTF_8));
        try {
            client.publish(device.getPublishTopicParam().getTopic(), msg);
            log.info("设备[{}]发送信道状态报文成功,状态[{}]",device.getName(),state);

        }catch (MqttException ex) {
            log.error("设备[{}]发送信道状态报文失败,状态[{}]",device.getName(),state);
        }
    }
}
