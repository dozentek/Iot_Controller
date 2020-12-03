package cn.rh.iot.config;

import cn.rh.iot.ContextAwareBeanLoader;
import cn.rh.iot.IotApplication;
import cn.rh.iot.core.DeviceManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @Program: Iot_Controller
 * @Description: 配置文件对象类
 * @Author: Y.Y
 * @Create: 2020-09-27 11:18
 **/
@Slf4j
@Service
public class IotConfig {

//    private static final IotConfig _instance = new IotConfig();

//    @Value("${mqtt.server}")
//    private String cnfMqttServer;
//    @Value("${mqtt.keepalive}")
//    private Integer cnfMqttKeepAlive;
//    @Value("${mqtt.timeout}")
//    private Integer cnfMqttTimeout;

    @Getter
    private MqttConfigInfo mqtt;
    @Getter
    private String driverFilePath;

    @Getter
    private int netDefaultTimeout;

    @Getter
    private int reconnectInterval;

    @Getter
    private int connectTimeout;

    @Getter
    private boolean isLoaded;

    private final ArrayList<DeviceConfigInfo> devices=new ArrayList<>();

    private IotConfig(){}

    public static IotConfig getInstance(){
        return Objects.requireNonNull(ContextAwareBeanLoader.getBean(IotConfig.class));
    }

    @PreDestroy
    protected void stop() {
        log.info("-----IOT关闭-----");
    }

    @PostConstruct
    public void loadAndStart() {
        log.info("-----IOT启动-----");
        log.info("config file loading ...");

        log.info("jar path:{}", getParentDirectoryFromJar());

        ClassLoader classLoader = getClass().getClassLoader();
        String configFilePath= classLoader.getResource("Config.xml").getFile();
        if (!new File(configFilePath).exists()) {
            configFilePath = getParentDirectoryFromJar() +"/Config.xml";
        }

        boolean isOk = load(configFilePath);

        if (isOk) {
            log.info("config file load success");
        } else {
            log.error("配置文件[{}]加载失败.",configFilePath);
            log.info("-----IOT关闭-----");
            return;
        }

        start();

    }
    public String getParentDirectoryFromJar() {
        ApplicationHome home = new ApplicationHome(IotApplication.class);
        return home.getDir().getPath();    // returns the folder where the jar is. This is what I wanted.
//        home.getSource(); // returns the jar absolute path.
    }
    public void start(){
        new Thread(() -> {
            DeviceManager.getInstance().load(IotConfig.getInstance());
            ArrayList<String> deviceKeyList = DeviceManager.getInstance().getKeyList();
            for (String s : deviceKeyList) {
                DeviceManager.getInstance().getDevice(s).Start();
            }
        }).start();
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

            //获取DriverFilePath
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("NetChannel");
                if (nodes.getLength() <= 0) {
                    log.error("配置文件缺少配置项：“+”NetChannel");
                    return false;
                }
                String value=((Element) (nodes.item(0))).getAttribute("defaultTimeout");
                if(value==null || !isInteger(value.trim())){
                    netDefaultTimeout=30000;
                }else {
                    netDefaultTimeout = Integer.parseInt(value.trim());
                }

                value=((Element) (nodes.item(0))).getAttribute("reconnectInterval");
                if(value==null || !isInteger(value.trim())){
                    reconnectInterval=8000;
                }else {
                    reconnectInterval = Integer.parseInt(value.trim());
                }

                value=((Element) (nodes.item(0))).getAttribute("connectTimeout");
                if(value==null || !isInteger(value.trim())){
                    connectTimeout=1000;
                }else {
                    connectTimeout = Integer.parseInt(value.trim());
                }
            }


            //获取Mqtt配置信息
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("Mqtt");
                if (nodes.getLength() <= 0) {
                    log.error("配置文件缺少配置项：“+”Mqtt");
                    return false;
                }

                MqttConfigInfo mqttTmp=new MqttConfigInfo();
//                mqttTmp.setServerURI(cnfMqttServer);
//                mqttTmp.setKeepAliveInterval(cnfMqttKeepAlive);
//                mqttTmp.setConnectionTimeout(cnfMqttTimeout);
//                mqttTmp.setReconnectInterval(cnfMqttKeepAlive);
//                mqtt = mqttTmp;

                if(!mqttTmp.Load((Element)nodes.item(0))) {
                    return false;
                }else{
                    mqtt=mqttTmp;
//                    mqtt.setServerURI(cnfMqttServer);
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

    private static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}
