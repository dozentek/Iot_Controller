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
    private final MqttClient client;
    private final String topic;
    private final int qos;
    private final int RECONNECT_INTERVAL;

    public MqttCallbackObject(Device device, MqttClient client) {
        this.device = device;
        this.client=client;
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

        log.info(device.getName()+"连接"+serverURI+"成功");

        if(topic==null || topic.trim()==""){
            return;
        }

        try {
            client.subscribe(topic, qos);
            log.info(device.getName()+"订阅Topic["+topic+"] 成功");
        }catch (MqttException ex){
            log.error(device.getName()+"订阅Topic["+topic+"]失败，问题："+ ex.toString());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.info(device.getName()+"与MQTT Broker 断开连接");
        if(client!=null && client.isConnected()){
            return;
        }else{
            reConnect();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(client!=null && client.isConnected() && device!=null){
            device.MessageArrived(topic,message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    private void reConnect() {
        try {
            if(!client.isConnected()) {
                client.reconnect();
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
