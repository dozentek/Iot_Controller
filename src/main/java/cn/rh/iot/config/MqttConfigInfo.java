package cn.rh.iot.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @Program: Iot_Controller
 * @Description: Mqtt配置信息类
 * @Author: Y.Y
 * @Create: 2020-09-27 11:25
 **/
@Slf4j
@Data
public class MqttConfigInfo {

    private String serverURI;
    private String username;
    private String password;

    private int reconnectInterval;
    private int connectionTimeout;
    private int keepAliveInterval;

    public boolean Load(Element element){
        if(element==null){
            return false;
        }

        {
            NodeList nodes = element.getElementsByTagName("ServerURI");
            if (nodes.getLength() <= 0) {
                log.error("配置文件缺少配置项：“+”Mqtt/ServerURI");
                return false;
            }
            serverURI = nodes.item(0).getTextContent().trim();
        }

        {
            NodeList nodes = element.getElementsByTagName("Username");
            if (nodes.getLength() <= 0) {
                log.error("配置文件缺少配置项：“+”Mqtt/Username");
                return false;
            }
            username = nodes.item(0).getTextContent().trim();
        }

        {
            NodeList nodes = element.getElementsByTagName("Password");
            if (nodes.getLength() <= 0) {
                log.error("配置文件缺少配置项：“+”Mqtt/Password");
                return false;
            }
            password = nodes.item(0).getTextContent().trim();
        }

        {
            String value = element.getAttribute("reconnectInterval");
            try {
                reconnectInterval = Integer.parseInt(value);
            }catch (Exception ex){
                reconnectInterval =-1;
            }
        }

        {
            String value = element.getAttribute("connectionTimeout");
            try {
                connectionTimeout = Integer.parseInt(value);
            }catch (Exception ex){
                connectionTimeout =-1;
            }
        }

        {
            String value = element.getAttribute("keepAliveInterval");
            try {
                keepAliveInterval = Integer.parseInt(value);
            }catch (Exception ex){
                keepAliveInterval =-1;
            }
        }

        return true;
    }
}
