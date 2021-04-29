package cn.rh.iot.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


@Slf4j
public class MqttCallbackObject implements MqttCallbackExtended {

    private final MqttChannel channel;

    public MqttCallbackObject(MqttChannel channel) {
        this.channel=channel;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if(channel==null){return;}
        if(!reconnect) {
            log.info("[{}]连接MQTT服务器成功",channel.getName());

            for (TopicParam topic : channel.getSubTopics().values()) {
                try {
                    channel.getClient().subscribe(topic.getTopic(), topic.getQos());
                    log.info("[{}]订阅主题[{}]成功", channel.getName(), topic.getTopic());
                } catch (MqttException ex) {
                    log.error("[{}]订阅主题[{}]失败，问题：{}", channel.getName(), topic.getTopic(), ex.getMessage());
                }
            }
        }else{
            log.info("[{}]重连MQTT服务器成功",channel.getName());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if(channel.isDisconnecting()){
            return;
        }
        if(channel.getClient()!=null && !channel.getClient().isConnected()){
            log.info("[{}]与MQTT服务器重连...",channel.getName());
            reConnect();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        if(channel!=null){
            channel.MessageArrived(topic,message);
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
            log.error("[{}]重连MQTT服务器失败，原因：{}",channel.getName(), e.toString());
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    reConnect();
                }
            },channel.getReconnect_Interval());
        }
    }
}
