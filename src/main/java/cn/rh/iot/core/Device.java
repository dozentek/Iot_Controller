package cn.rh.iot.core;

import cn.rh.iot.driver.base.IDriver;
import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.mqtt.TopicArrivedEvent;
import cn.rh.iot.mqtt.TopicArrivedHandler;
import cn.rh.iot.mqtt.TopicParam;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class Device implements TopicArrivedHandler {
    @Getter @Setter
    private String des;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String sN;

    @Getter @Setter
    protected IDriver driver;

    @Getter
    protected int askInterval=-1;

    @Getter @Setter
    private TopicParam subTopic;

    @Getter @Setter
    private TopicParam pubTopic;

    @Getter @Setter
    private Bridge parent;

    protected MqttChannel mqttChannel;

    protected Timer askTimer=new Timer(true);

    public void setAskInterval(int value){
        askInterval=value;
        if(askInterval>0){
            askTimer.schedule(doAsk(),0,askInterval);
        }else{
            askTimer.cancel();
        }
    }

    public MqttChannel getMqttChannel(){
        return mqttChannel;
    }

    public void setMqttChannel(MqttChannel channel){
        if(!channel.equals(mqttChannel)){
            mqttChannel=channel;

            if(mqttChannel!=null){
                mqttChannel.AddTopicArrivedHandler(this);
            }
        }
    }

    public void MqttConnect(){
        if(mqttChannel!=null){
            mqttChannel.Connect();
        }
    }

    public void MqttDisConnect(){
        if(mqttChannel!=null){
            mqttChannel.Disconnect();
        }
    }

    private TimerTask doAsk(){

        return new TimerTask() {
            @Override
            public void run() {
                if (parent != null && driver != null) {
                    parent.SendData2Device(driver.getAskMessage());
                }
            }
        };
    }

    private void MqttTopicArrived(String topic, String message){

        log.info("[{}]收到[{}] -> {}",name,topic, message);

        if(subTopic.getTopic().equals(topic)) {
            byte[] outData= driver.encode(message);
            if(outData!=null && outData.length>0 && parent.getChannel()!=null) {
                parent.getChannel().Write(outData);
            }
        }
    }

    @Override
    public void TopicArrived(TopicArrivedEvent event) {
        MqttTopicArrived(event.getTopic(),event.getContent());
    }
}
