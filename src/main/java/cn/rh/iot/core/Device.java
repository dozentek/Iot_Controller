package cn.rh.iot.core;

import cn.rh.iot.driver.base.IDriver;
import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.mqtt.TopicParam;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class Device {
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

    @Getter @Setter
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

    public void MqttTopicArrived(String topic, MqttMessage message){

        String strData=new String(message.getPayload());
        log.info("[{}]收到[{}] -> {}",name,topic, strData);

        if(subTopic.getTopic().equals(topic)) {
            byte[] outData= driver.encode(strData);
            if(outData!=null && outData.length>0 && parent.getChannel()!=null) {
                parent.getChannel().Write(outData);
            }
        }
    }
}
