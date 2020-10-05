package cn.rh.iot.core;

import cn.rh.iot.driver.IDriver;
import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.mqtt.TopicParam;
import io.netty.bootstrap.Bootstrap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: 代表一个外接设备的类
 * @Author: Y.Y
 * @Create: 2020-09-18 11:51
 **/
@Slf4j
public abstract class Device {
    @Getter @Setter
    protected String name;
    @Getter @Setter
    protected String id="";
    @Getter @Setter
    protected String introduction;
    @Getter @Setter
    protected int askInterval;
    @Getter @Setter
    protected int timeout;

    @Getter @Setter
    protected TopicParam subscribeTopicParam;
    @Getter @Setter
    protected TopicParam publishTopicParam;

    @Getter @Setter
    protected IDriver driver;
    @Getter @Setter
    protected MqttChannel mqttChannel;
    @Getter @Setter
    protected IChannel channel;

    @Getter @Setter
    protected Bootstrap bootstrap;
    @Getter
    protected HashMap<String ,Object> injectParams=new HashMap<>();

    @Getter
    protected boolean isStarted=false;

    public void MessageArrived(MqttMessage message){
        if(this.driver!=null) {
            String strData=new String(message.getPayload());
            log.info("设备[{}]收到控制报文-{}",this.getName(),strData);
            byte[] outData= driver.encode(strData);
            if(channel!=null && outData.length>0 ) {
                channel.Write(outData);
            }

        }
    }

    public void Start(){
        if(mqttChannel!=null && channel!=null){
            mqttChannel.Connect();
            channel.Connect();
        }
    }

    public void Stop(){
        if(mqttChannel!=null && channel!=null){
            channel.Disconnect();
            mqttChannel.Disconnect();
        }
    }

}
