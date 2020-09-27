package cn.rh.iot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

/**
 * @Program: Iot_Controller
 * @Description: 配置文件对象类
 * @Author: Y.Y
 * @Create: 2020-09-27 11:18
 **/
@Slf4j
public class IotConfig {

    private static final IotConfig _instance = new IotConfig();

    @Getter
    private MqttConfigInfo mqtt;
    @Getter
    private String driverFilePath;
    @Getter
    private boolean isLoaded;

    private final ArrayList<DeviceConfigInfo> devices=new ArrayList<>();

    private IotConfig(){}

    public static IotConfig getInstance(){
        return _instance;
    }

    public DeviceConfigInfo getDeviceInfoObject(String deviceName){

        for(int i=0;i<devices.size();i++){
            if(devices.get(i).getName()==deviceName){
                return devices.get(i);
            }
        }
        return null;
    }

    public DeviceConfigInfo getDeviceInfoObject2(int index){
        if(index<0 || index>=devices.size()){
            return null;
        }

        return devices.get(index);
    }

    public int DeviceCount(){
        return devices.size();
    }


    public boolean load(String configFilePath){
        if(isLoaded){
            return true;
        }

        File file=new File(configFilePath);
        if(!file.exists()){
            log.error("配置文件["+configFilePath+"]不存在");
            return false;
        }
        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(configFilePath);
        }catch (Exception ex){
            log.error("配置文件解析失败,错误："+ex.toString());
            return false;
        }

        try{
            //获取DriverFilePath
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("DriverFilePath");
                if (nodes.getLength() <= 0) {
                    log.error("配置文件缺少配置项：“+”DriverFilePath");
                    return false;
                }
                driverFilePath = ((Element) (nodes.item(0))).getTextContent().trim();
            }

            //获取Mqtt配置信息
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("Mqtt");
                if (nodes.getLength() <= 0) {
                    log.error("配置文件缺少配置项：“+”Mqtt");
                    return false;
                }

                MqttConfigInfo mqttTmp=new MqttConfigInfo();
                if(!mqttTmp.Load((Element)nodes.item(0))) {
                    return false;
                }else{
                    mqtt=mqttTmp;
                }
            }

            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("Devices");
                if(nodes.getLength()<=0){
                    log.error("配置文件缺少Devices项");
                    return false;
                }
                nodes=((Element)nodes.item(0)).getElementsByTagName("Device");
                for(int i=0;i<nodes.getLength();i++){
                    DeviceConfigInfo device=new DeviceConfigInfo();
                    boolean res=device.Load((Element)nodes.item(i));
                    if(!res){
                        log.error("配置文件中Device解析错误");
                        devices.clear();
                        return false;
                    }
                    devices.add(device);
                }
                log.info("配置文件加载成功");
            }
            isLoaded=true;
            return true;
        }catch (Exception ex){
            log.error("配置文件加载失败，错误："+ex.toString());
            return false;
        }
    }
}
