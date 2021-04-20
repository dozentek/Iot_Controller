package cn.rh.iot.core;

import cn.rh.iot.driver.base.ByteUtil;
import cn.rh.iot.driver.base.FrameType;
import cn.rh.iot.mqtt.TopicParam;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import jdk.nashorn.internal.runtime.regexp.joni.ScanEnvironment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Bridge {

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
    protected IChannel channel;

    @Getter @Setter
    protected Bootstrap bootstrap;

    @Getter
    protected boolean isStarted=false;

    @Getter @Setter
    private NetRoleType role;
    @Getter @Setter
    private NetProtocolType protocol;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    private int port;

    @Getter
    protected final HashMap<String , Device> subTopicMap=new HashMap<>();

    protected final HashMap<String , Device> devices=new HashMap<>();

    private final EventLoopGroup group;

    public Bridge(EventLoopGroup group) {
        this.group=group;
    }

    public EventLoopGroup getGroup(){
        return group;
    }
    public void AddDevice(Device device){
        if(device!=null){
            device.setParent(this);
        }else{
            return;
        }
        devices.put(device.getName(),device);

        TopicParam sub=device.getSubTopic();
        if(sub!=null) {
            subTopicMap.put(sub.getTopic(), device);
        }
    }

    public FrameType getMessageType(){
        if(devices.size()<=0){
            return FrameType.None;
        }

        Iterator<Device> it= devices.values().iterator();
        return it.next().getDriver().getType();
    }

    public int getMessageLength(){
        if(devices.size()<=0){
            return -1;
        }

        Iterator<Device> it= devices.values().iterator();
        return it.next().getDriver().getMessageLength();
    }

    public byte[] getTrailer(){
        if(devices.size()<=0){
            return null;
        }
        Iterator<Device> it= devices.values().iterator();
        return it.next().getDriver().getTrailer();
    }

    public int getLengthFieldOffset()
    {
        if(devices.size()<=0){
            return -1;
        }
        Iterator<Device> it= devices.values().iterator();
        return it.next().getDriver().getLengthFieldOffset();
    }

    public int getLengthFieldLength()
    {
        if(devices.size()<=0){
            return -1;
        }
        Iterator<Device> it= devices.values().iterator();
        return it.next().getDriver().getLengthFieldLength();
    }

    public int getLengthAdjustment()
    {
        if(devices.size()<=0){
            return -1;
        }
        Iterator<Device> it= devices.values().iterator();
        return it.next().getDriver().getLengthAdjustment();
    }


    public void SendData2Device(byte[] data){
        if( channel!=null && channel.isConnected()){
            channel.Write(data);
        }
    }

    public void Byte2JsonAndSendMqtt(byte[] data){
        if(data==null || data.length<=0){
            log.info("收到空数据集");
            return;
        }

//        log.info("收到Hex:{}",ByteUtil.bytesToHex(data));           //for Test

        for (Device device:devices.values())
        {
            if(device.getDriver().Is2Me(data))
            {
                String deviceNO="";
                String json=device.getDriver().decode(data);
                if(json!=null) {
                    String msg = Pack(device.getName(), deviceNO, json);
                    device.getMqttChannel().Write(device.getPubTopic().getTopic(), device.getPubTopic().getQos(), msg);
                }else{
                    log.info("CRC校验失败。Hex:{}",ByteUtil.bytesToHex(data));
                }
                break;
            }
        }
    }

    public void SendConnectStateTopic(boolean isConnected){

        String msg="no";
        if(isConnected){ msg="ok"; }
        else{  msg="no"; }

        for (Device device : devices.values()) {
            device.getMqttChannel().SendConnectStateTopic(msg);
        }
    }

    private String Pack(String deviceName,String deviceNO,String msg){

        return "{" +
                System.lineSeparator() +
                "\"deviceName\":\"" + deviceName + "\"," + System.lineSeparator() +
                "\"deviceNumber\":\"" + deviceNO + "\"," + System.lineSeparator() +
                msg + System.lineSeparator() +
                "}";
    }

    public void Start(){
        for (Device device:devices.values()) {
            device.MqttConnect();
        }
        channel.Connect();
    }

    public void Stop() {

        channel.Disconnect();
        try {
            Thread.sleep(50);      //保证状态报文能否顺利发送到Mqtt服务器后，再关闭与MQTT服务器的连接
        }catch (InterruptedException ex){
            log.error("[{}]调用Stop函数失败，原因：{}",name,ex.getMessage());
        }
        for (Device device : devices.values()) {
            device.MqttDisConnect();
        }
    }
}
