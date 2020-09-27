package cn.rh.iot.mqtt;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Device;
import lombok.Getter;
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

    private MqttClient client;

    @Getter
    private final Device device;
    private final String host;

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
        options.setKeepAliveInterval(IotConfig.getInstance().getMqtt().getKeepAliveInterval());  //心跳时间，单位秒，每隔固定时间发送心跳包
    }

    public void Write(String send_message) {

        if(client==null || !client.isConnected()){
            return;
        }

        try {
            MqttMessage msg=new MqttMessage();
            msg.setQos(device.getPublishTopicParam().getQos());
            msg.setPayload(send_message.getBytes(StandardCharsets.UTF_8));
            client.publish(device.getPublishTopicParam().getTopic(),msg);
         }catch (MqttException ex) {
            log.error("设备[{}] 发送Topic[{}]-[{}]失败",device.getName(),device.getPublishTopicParam().getTopic(),send_message);
        }
    }

    public void Disconnect() {
        if(client==null){
            return;
        }
        try {
            client.disconnect();
        } catch(MqttException ex){
            log.warn(device.getName() + "与MQTT Broker断开连接失败，因为:"+ex.getMessage());
        }
    }

    public void Connect(){
        if(client!=null && client.isConnected()){
            return;
        }
        try {
            client = new MqttClient(host, device.getName() + "_" + device.getId(), persistence);
            client.setCallback(new MqttCallbackObject(device, client));
            client.connect(options);
        } catch(MqttException ex) {
            log.error(device.getName() + "连接MQTT Broker失败，错误码：" + ex.getMessage());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    log.error(device.getName() + "重连 MQTT Broker..." );
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
}
