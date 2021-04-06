package cn.rh.iot.config;

import cn.rh.iot.ContextAwareBeanLoader;
import cn.rh.iot.IotApplication;
import cn.rh.iot.core.BridgeManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class IotConfig {

    @Getter
    private MqttInfo mqtt;

    @Getter
    private int netDefaultTimeout;

    @Getter
    private int reconnectInterval;

    @Getter
    private int connectTimeout;

    @Getter
    private boolean isLoaded;

    private final ArrayList<BridgeInfo> bridges=new ArrayList<>();
    private String loadErrInfo="";

    private IotConfig(){}

    public static IotConfig getInstance(){
        return Objects.requireNonNull(ContextAwareBeanLoader.getBean(IotConfig.class));
    }

    @PreDestroy
    protected void stop() {
        BridgeManager.getInstance().ShutDownBridge();
        log.info("--IOT关闭--");
    }

    @PostConstruct
    public void loadAndStart() {

        //加载配置文件
        {
            log.info("--IOT启动--");
            log.info("配置文件加载...");

            ClassLoader classLoader = getClass().getClassLoader();
            String configFilePath = classLoader.getResource("Config.xml").getFile();

            if (!new File(configFilePath).exists()) {
                configFilePath = getParentDirectoryFromJar() + "/Config.xml";
            }

            boolean isOk = load(configFilePath);

            if (isOk) {
                log.info("配置文件加载成功");
            } else {
                log.error("配置文件加载失败" + loadErrInfo);
                log.info("--IOT关闭--");
                return;
            }
        }

        start();    //启动所有Bridge
    }

    public String getParentDirectoryFromJar() {
        ApplicationHome home = new ApplicationHome(IotApplication.class);
        return home.getDir().getPath();
    }

    public void start(){
        new Thread(() -> {
            boolean isLoadSuccess=BridgeManager.getInstance().load(IotConfig.getInstance());
            if(isLoadSuccess) {
                BridgeManager.getInstance().StartBridges();
            }
        }).start();
    }


    public BridgeInfo getBridgeInfoObject(int index){
        if(index<0 || index>=bridges.size()){
            return null;
        }
        return bridges.get(index);
    }

    public int BridgeCount(){
        return bridges.size();
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

            //获取NetChannel
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("NetChannel");
                if (nodes.getLength() <= 0) {
                    log.error("缺少配置项：NetChannel");
                    return false;
                }
                String value=((Element) (nodes.item(0))).getAttribute("defaultTimeout");
                try {
                    netDefaultTimeout = Integer.parseInt(value.trim());
                }catch (Exception ex){
                    netDefaultTimeout=30000;
                }

                value=((Element) (nodes.item(0))).getAttribute("reconnectInterval");
                try {
                    reconnectInterval = Integer.parseInt(value.trim());
                }catch (Exception ex){
                    reconnectInterval=30000;
                }

                value=((Element) (nodes.item(0))).getAttribute("connectTimeout");
                try {
                    connectTimeout = Integer.parseInt(value.trim());
                }catch (Exception ex){
                    connectTimeout=1000;
                }
            }


            //获取Mqtt服务器配置
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("Mqtt");
                if (nodes.getLength() <= 0) {
                    log.error("缺少配置项：Mqtt");
                    return false;
                }

                MqttInfo mqttTmp=new MqttInfo();
                if(!mqttTmp.Load((Element)nodes.item(0))) {
                    return false;
                }else{
                    mqtt=mqttTmp;
                }
            }

            //获取Bridges配置信息
            {
                NodeList nodes = document.getDocumentElement().getElementsByTagName("Bridges");
                if(nodes.getLength()<=0){
                    log.error("缺少配置项：Bridges");
                    return false;
                }
                nodes=((Element)nodes.item(0)).getElementsByTagName("Bridge");
                for(int i=0;i<nodes.getLength();i++){
                    BridgeInfo bridge=new BridgeInfo();
                    boolean res=bridge.Load((Element)(nodes.item(i)));
                    if(!res){
                        log.error("Bridge解析错误");
                        bridges.clear();
                        return false;
                    }
                    bridges.add(bridge);
                }
            }
            isLoaded=true;
            loadErrInfo="";
            return true;
        }catch (Exception ex){
            loadErrInfo=ex.toString();
            return false;
        }
    }

}
