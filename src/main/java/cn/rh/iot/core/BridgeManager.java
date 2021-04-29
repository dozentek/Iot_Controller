package cn.rh.iot.core;

import cn.rh.iot.config.BridgeInfo;
import cn.rh.iot.config.DeviceInfo;
import cn.rh.iot.config.IotConfig;
import cn.rh.iot.config.TopicInfo;
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

@Slf4j
public class BridgeManager {

    @Getter
    private boolean isLoaded=false;

    @Getter
    private final EventLoopGroup group = new NioEventLoopGroup();

    private final HashMap<String , Bridge> bridges=new HashMap<>();

    private static final BridgeManager _instance=new BridgeManager();
    public static BridgeManager getInstance(){
        return _instance;
    }

    public boolean load(IotConfig configObj){
        if(isLoaded){
            return true;
        }
        if(configObj==null || !configObj.isLoaded()){
            log.error("配置信息加载失败");
            return false;
        }
        try {
            for (int i = 0; i < configObj.BridgeCount(); i++) {
                CreateBridge(configObj.getBridgeInfoObject(i));
            }
        }catch (Exception ex){
            log.error("加载信息失败，原因:{}",ex.getMessage());
            return false;
        }
        isLoaded=true;
        return true;
    }

    public void ShutDownBridge(){

        for(Bridge bridge:bridges.values()){
            bridge.Stop();
        }

        Future<?> future =group.shutdownGracefully();
        future.syncUninterruptibly();
    }

    public void StartBridges(){
        for(Bridge bridge:bridges.values()){
            bridge.Start();
        }
    }

    private void CreateBridge(BridgeInfo info) throws Exception{

        if(!bridges.containsKey(info.getName())) {

            Bridge bridge = new Bridge(group);

            bridge.setName(info.getName());
            bridge.setIntroduction(info.getInfo());
            bridge.setRole(info.getRole());
            bridge.setProtocol(info.getProtocol());
            bridge.setIp(info.getIp());
            bridge.setPort(info.getPort());
            bridge.setTimeout(info.getTimeout());

            for (DeviceInfo deviceInfo : info.getDeviceInfoList()) {
                Device device = new Device();
                {
                    device.setName(deviceInfo.getName());
                    device.setDes(deviceInfo.getDes());
                    device.setSN(deviceInfo.getSerialNO());
                    device.setAskInterval(deviceInfo.getAskInterval());
                    device.setPubTopic(CreateTopic(deviceInfo.getPubTopic()));
                    device.setSubTopic(CreateTopic(deviceInfo.getSubTopic()));
                    {
                        IDriver driver = DriverManager.getInstance().getNewDriverObject(deviceInfo.getDriverClassName());
                        if (driver == null) {
                            throw new Exception("未找到驱动程序：" + deviceInfo.getDriverClassName() + ".class");
                        }
                        device.setDriver(DriverManager.getInstance().getNewDriverObject(deviceInfo.getDriverClassName()));

                        //将SN（设备编号）注入驱动程序
                        if(device.getSN()!=null && !device.getSN().trim().equals("")){
                            HashMap<String,Object> tags=new HashMap<>();
                            tags.put("sn",device.getSN().trim());
                            device.getDriver().InjectParams(tags);
                        }
                    }
                    MqttChannel mqttChannel=new MqttChannel(device.getName(),IotConfig.getInstance().getMqtt().getServerURI(),
                            IotConfig.getInstance().getMqtt().getReconnectInterval()*1000);

                    mqttChannel.AddSubTopic(device.getSubTopic());
                    device.setMqttChannel(mqttChannel);
                    device.setParent(bridge);
                }
                bridge.AddDevice(device);
            }

            bridge.setChannel(new NetChannel(bridge));
            bridges.put(bridge.name, bridge);
        }
    }

    private TopicParam CreateTopic(TopicInfo info){
        if(info==null){
            return null;
        }

        TopicParam t=new TopicParam();
        t.setQos(0);
        t.setTopic(info.getTopicName());
        return t;
    }
}
