package cn.rh.iot.mqtt;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Device;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Timer;
import java.util.TimerTask;


@Slf4j
public class MqttCallbackObject implements MqttCallbackExtended {

    @Getter
    private final Device device;
    private final int RECONNECT_INTERVAL;

    private final MqttChannel channel;

    public MqttCallbackObject(Device device, MqttChannel channel) {
        this.device = device;
        this.channel=channel;
        RECONNECT_INTERVAL= IotConfig.getInstance().getMqtt().getReconnectInterval()*1000;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if(!reconnect) {
            log.info("[{}]连接MQTT服务器成功", device.getName());
            if (device.getSubTopic()==null) {
                return;
            }
            try {
                channel.getClient().subscribe(device.getSubTopic().getTopic(), device.getSubTopic().getQos());
                log.info("[{}]订阅主题成功", device.getName());
            } catch (MqttException ex) {
                log.error("[{}]订阅主题失败，问题：{}",device.getName() , ex.getMessage());
            }
        }else{
            log.info("[{}]重连MQTT服务器成功",device.getName());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if(channel.isDisconnecting()){
            return;
        }
        if(channel.getClient()!=null && !channel.getClient().isConnected()){
            log.info("[{}]与MQTT服务器重连...",device.getName());
            reConnect();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if(device!=null){
            device.MqttTopicArrived(topic,message);
        }
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    private void reConnect() {
        try {
            if(!channel.getClient().isConnected()) {
                channel.getClient().reconnect();
            }
        } catch (MqttException e) {
            log.error("[{}]重连MQTT服务器失败，原因：{}",device.getName(), e.toString());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    reConnect();
                }
            },RECONNECT_INTERVAL);
        }
    }
}
