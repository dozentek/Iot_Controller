package cn.rh.iot.mqtt;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Device;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @Program: IOT_Controller
 * @Description: MQTT客户端的回调处理类
 * @Author: Y.Y
 * @Create: 2020-09-24 20:35
 **/
@Slf4j
public class MqttCallbackObject implements MqttCallbackExtended {

    @Getter
    private final Device device;
    private final String topic;
    private final int qos;
    private final int RECONNECT_INTERVAL;

    private final MqttChannel channel;

    public MqttCallbackObject(Device device, MqttChannel channel) {
        this.device = device;
        this.channel=channel;

        if(device.getSubscribeTopicParam()==null){
            topic=""; qos=0;
        }else {
            topic = device.getSubscribeTopicParam().getTopic();
            qos=device.getSubscribeTopicParam().getQos();
        }

        RECONNECT_INTERVAL= IotConfig.getInstance().getMqtt().getReconnectInterval()*1000;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if(!reconnect) {
            log.info("设备[{}]连接MQTT服务器成功", device.getName());
            if (topic == null || topic.trim().equals("")) {
                return;
            }

            try {
                 channel.getClient().subscribe(topic, qos);
                log.info(device.getName() + "订阅Topic[" + topic + "] 成功");
            } catch (MqttException ex) {
                log.error(device.getName() + "订阅Topic[" + topic + "]失败，问题：" + ex.toString());
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if(channel.isInitiativeDisconnecting()){
            channel.setInitiativeDisconnecting(false);
            return;
        }
        log.info(device.getName()+"与MQTT Broker 断开连接");
        if(channel.getClient()!=null && channel.getClient().isConnected()){
        }else{
            reConnect();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(channel.getClient()!=null && channel.getClient().isConnected() && device!=null){
            device.MessageArrived(topic,message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    private void reConnect() {
        try {
            if(!channel.getClient().isConnected()) {
                channel.getClient().reconnect();
                log.info(device.getName()+"重连MQTT Broker 成功");
            }
        } catch (MqttException e) {
            log.error(device.getName()+"重连MQTT Broker 失败", e.toString());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    reConnect();
                }
            },RECONNECT_INTERVAL);
        }
    }
}
