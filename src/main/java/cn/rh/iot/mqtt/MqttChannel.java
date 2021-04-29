package cn.rh.iot.mqtt;

import cn.rh.iot.config.IotConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
public class MqttChannel {


    @Getter
    private MqttClient client;

    @Getter
    private final String name;
    @Getter
    private final String host;
    @Getter
    private final int reconnect_Interval;

    @Setter
    private boolean isDisconnecting =false;


    private final MqttConnectOptions options;
    private final MqttClientPersistence persistence;
    private final HashMap<String,TopicParam >  subTopics;

    private Collection<TopicArrivedHandler>   topicArrivedListeners;


    public MqttChannel(String name,String host,int reconnect_interval) {

        subTopics=new HashMap<>();

        this.name=name;
        this.host=host;

        reconnect_Interval = reconnect_interval;

        persistence=new MemoryPersistence();
        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(IotConfig.getInstance().getMqtt().getUsername());
        options.setPassword(IotConfig.getInstance().getMqtt().getPassword().toCharArray());
        options.setConnectionTimeout(IotConfig.getInstance().getMqtt().getConnectionTimeout());
        options.setKeepAliveInterval(IotConfig.getInstance().getMqtt().getKeepAliveInterval());
    }

    public void AddSubTopic(TopicParam topic){
        if( topic!=null && !subTopics.containsKey(topic.getTopic())){
            subTopics.put(topic.getTopic(),topic);
        }
    }

    public HashMap<String,TopicParam > getSubTopics(){
        return subTopics;
    }

    public boolean isDisconnecting(){
        return this.isDisconnecting;
    }

    public void Write(String senderName,String topic,int qos,String send_message) {

        if(client==null || !client.isConnected()){
            return;
        }

        MqttMessage msg=new MqttMessage();
        msg.setQos(qos);
        msg.setPayload(send_message.getBytes(StandardCharsets.UTF_8));
        try {
            client.publish(topic,msg);
            log.info("[{}]发送主题[{}]成功，内容( {} )", senderName,topic,send_message);
        }catch (MqttException ex) {
            log.error("[{}]发送主题[{}]失败，内容( {} )",senderName,topic,send_message);
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
            log.info("[{}]与MQTT服务器主动断开连接",name);

        }catch(MqttException ex){
            log.error("[{}]与MQTT服务器主动断开连接失败，原因：{}:", name, ex.getMessage());
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
            String clientId= name+"_"+ System.currentTimeMillis();  //避免clientId重复
            client = new MqttClient(host, clientId, persistence);
            client.setCallback(new MqttCallbackObject(this));
            client.connect(options);
        } catch(MqttException ex) {
            log.error("[{}]连接MQTT服务器失败，错误码：{}", name, ex.getReasonCode());
            client=null;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    log.error("设备[{}]重连MQTT服务器...",name);
                    Connect();
                }
            }, reconnect_Interval);
        }
    }

    public boolean isConnected() {
        if(client==null){
            return false;
        }
        return client.isConnected();
    }

    public void AddTopicArrivedHandler(TopicArrivedHandler listener){
        if(topicArrivedListeners==null){
            topicArrivedListeners=new HashSet<>();
        }
        if(listener!=null && !topicArrivedListeners.contains(listener)) {
            topicArrivedListeners.add(listener);
        }
    }

    public void RemoveTopicArrivedHandler(TopicArrivedHandler listener)
    {
        if(topicArrivedListeners!=null){
            topicArrivedListeners.remove(listener);
        }
    }

    public void MessageArrived(String topic, MqttMessage message) {
        if(topicArrivedListeners!=null){
            String msg=new String(message.getPayload());
            TopicArrivedEvent event=new TopicArrivedEvent(this,topic,msg);

            for (TopicArrivedHandler listener : topicArrivedListeners) {
                listener.TopicArrived(event);
            }
        }
    }
}
