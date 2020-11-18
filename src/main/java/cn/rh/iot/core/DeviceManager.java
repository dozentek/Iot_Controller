package cn.rh.iot.core;

import cn.rh.iot.config.DeviceConfigInfo;
import cn.rh.iot.config.IotConfig;
import cn.rh.iot.driver.base.DriverManager;
import cn.rh.iot.driver.base.IDriver;
import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.mqtt.TopicParam;
import cn.rh.iot.net.NetChannel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: 设备对象管理器
 * @Author: Y.Y
 * @Create: 2020-09-21 20:27
 **/
@Slf4j
public class DeviceManager {

    private final HashMap<String ,Device> devices=new HashMap<>();

    @Getter
    private boolean isLoaded=false;
    @Getter
    private final EventLoopGroup group = new NioEventLoopGroup();

    private static final DeviceManager _instance=new DeviceManager();

    public static DeviceManager getInstance(){
        return _instance;
    }

    public boolean load(IotConfig configObj){
        if(isLoaded){
            return true;
        }
        if(configObj==null || !configObj.isLoaded()){
            log.error("配置信息对象未加载,导致无法加载Driver");
            return false;
        }
        try {
            for (int i = 0; i < configObj.DeviceCount(); i++) {
                CreateDevice(configObj.getDeviceInfoObject2(i));
            }
        }catch (Exception ex){
            log.error("加载Driver失败，原因:{}",ex.getMessage());
            return false;
        }
        isLoaded=true;
        return true;
    }

    /*
     * @Description: 获取已经加载设备对象的数量
     * @Param: []
     * @Return: int
     * @Author: Y.Y
     * @Date: 2020/9/24 10:28
     */
    public int DeviceCount(){
        return devices.size();
    }

    /*
     * @Description: 获取所有Device对象的key（也就是设备名称）的列表
     * @Param: []
     * @Return: java.util.ArrayList<java.lang.String>
     * @Author: Y.Y
     * @Date: 2020/9/24 10:28
     */
    public ArrayList<String> getKeyList(){
        return new ArrayList<>(devices.keySet());
    }

    /*
     * @Description: 获取一个设备对象
     * @Param: [key] 设备对象的名称
     * @Return: cn.rh.iot.core.Device
     * @Author: Y.Y
     * @Date: 2020/9/24 10:29
     */
    public Device getDevice(String key){
        return devices.get(key);
    }

    public void ShutDownAllDevices(){
        Future<?> future =group.shutdownGracefully();
        future.syncUninterruptibly();
    }

    private void CreateDevice(DeviceConfigInfo info) throws Exception{

        NetDevice device=new NetDevice(group);
        device.setName(info.getName());
        device.setIntroduction(info.getInfo());
        device.setRole(info.getRole());
        device.setProtocol(info.getProtocol());
        device.setAskInterval(info.getAskInterval());
        device.setTimeout(info.getTimeout());
        device.setIp(info.getIp());
        device.setPort(info.getPort());

        IDriver driver=DriverManager.getInstance().getNewDriverObject(info.getDriverClassName());
        if(driver==null){
            throw new Exception("未找到驱动程序："+info.getDriverClassName()+".class");
        }
        device.setDriver(DriverManager.getInstance().getNewDriverObject(info.getDriverClassName()));

        ArrayList<String>  topicNames=info.getPublishTopicNameList();
        if(topicNames.size()>0){
            device.setPublishTopicParam(new TopicParam(topicNames.get(0),0));
        }else{
            device.setPublishTopicParam(null);
        }

        topicNames=info.getSubscribeTopicNameList();
        if(topicNames.size()>0){
            device.setSubscribeTopicParam(new TopicParam(topicNames.get(0),0));
        }else{
            device.setSubscribeTopicParam(null);
        }

        if(!devices.containsKey(device.name)){
            device.setChannel(new NetChannel((NetDevice)device));
            device.setMqttChannel(new MqttChannel(device));
            devices.put(device.name,device);
        }
    }
}
