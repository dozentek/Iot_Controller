package cn.rh.iot.mqtt;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @Program: IOT_Controller
 * @Description: 获取Mqtt的配置，创建一个单例的POJO
 * @Author: Y.Y
 * @Create: 2020-09-20 13:05
 **/
@Slf4j
public class MqttConfig {
    private static final MqttConfig mqttProperty = new MqttConfig();
    public static MqttConfig Instance() {
        return mqttProperty;
    }

    private Properties pps=new Properties();

    private MqttConfig(){
        init();
    }

    public String get(String key){
        return this.pps.getProperty(key);
    }

    public int getInt(String key){
        String value=pps.getProperty(key);
        return Integer.parseInt(value.trim());
    }

    private void init()  {
        StringBuilder builder = new StringBuilder();
        String curDir=System.getProperty("user.dir");
        String file_conf = "build\\resources\\main\\mqtt.properties";
        String filepath = builder.append(curDir).append(File.separator).append(file_conf).toString();

        try {
            FileInputStream fs = new FileInputStream(filepath);
            pps=new Properties();
            try {
                pps.load(fs);
            }catch (IOException ex){
                log.error("文件：{} 加载失败",filepath);
            }
        }catch (FileNotFoundException ex){
            log.error("文件：{} 丢失",filepath);
        }
    }
}
