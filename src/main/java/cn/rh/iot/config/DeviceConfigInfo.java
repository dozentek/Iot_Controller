package cn.rh.iot.config;

import cn.rh.iot.core.NetProtocolType;
import cn.rh.iot.core.NetRoleType;
import com.sun.org.apache.xpath.internal.XPathAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * @Program: Iot_Controller
 * @Description: 设备参数配置类
 * @Author: Y.Y
 * @Create: 2020-09-27 11:25
 **/
@Slf4j
public class DeviceConfigInfo {

    private final static int DEFAULT_TIMEOUT=6000;

    @Getter @Setter
    private String name;
    @Getter @Setter
    private String info;
    @Getter @Setter
    private NetRoleType role;
    @Getter @Setter
    private NetProtocolType protocol;
    @Getter @Setter
    private int askInterval;
    @Getter @Setter
    private int timeout;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    private int port;
    @Getter @Setter
    private String driverClassName;

    private final ArrayList<TopicConfigInfo> topics=new ArrayList<>();

    public int TopicCount(){
        return topics.size();
    }

    public ArrayList<String> getPublishTopicNameList(){
        return getTopicNameList("OUT");
    }

    public ArrayList<String> getSubscribeTopicNameList(){
        return getTopicNameList("IN");
    }

    private ArrayList<String> getTopicNameList(String kind){
        ArrayList<String> nameList=new ArrayList<>();
        for(int i=0;i<topics.size();i++){
            if(topics.get(i).getKind().toUpperCase().equals(kind)){
                nameList.add(topics.get(i).getTopicName());
            }
        }
        return nameList;
    }

    public boolean ContainTopic(String topicStr,String kind){
        TopicConfigInfo topic;
        for(int i=0;i<topics.size();i++){
            topic=topics.get(i);
            if(topic.equal(topicStr,kind)){
                return true;
            }
        }
        return false;
    }

    public boolean Load(Element ele){
        if(ele==null){
            return false;
        }

        {
            try {
                NodeList nodes = XPathAPI.selectNodeList(ele, "Net/IP");
                if(nodes.getLength()<=0){
                    log.error("配置文件缺少: Device/Net/IP配置项");
                    return false;
                }
                ip=nodes.item(0).getTextContent().trim();

                nodes = XPathAPI.selectNodeList(ele, "Net/Port");
                if(nodes.getLength()<=0){
                    log.error("配置文件缺少: Device/Net/Port配置项");
                    return false;
                }
                port=Integer.parseInt(nodes.item(0).getTextContent().trim());

            }catch (Exception ex){
                log.error("配置文件解析错误 错误："+ex.getMessage());
                return false;
            }
        }

        {
            NodeList nodes = ele.getElementsByTagName("Driver");
            if (nodes.getLength() <= 0) {
                log.error("配置文件缺少: Device/Driver配置项");
                return false;
            }
            driverClassName=nodes.item(0).getTextContent().trim();
        }

        {
            String value = ele.getAttribute("name");
            name=value.trim();
            if(name.equals("")){
                log.error("配置文件缺少: Device.name配置项不能为空");
                return false;
            }
        }

        {
            String value = ele.getAttribute("intro");
            info=value.trim();
        }

        {
            String value = ele.getAttribute("role");
            if(value.trim().toUpperCase().equals("CLIENT")){
                role=NetRoleType.CLIENT;
            }else{
                role=NetRoleType.SERVER;
            }
        }

        {
            String value = ele.getAttribute("protocol");
            if(value.trim().toUpperCase().equals("UDP")){
                protocol=NetProtocolType.UDP;
            }else{
                protocol=NetProtocolType.TCP;
            }
        }

        {
            String value = ele.getAttribute("askInterval");
            if(value==null || value.trim().equals("")){
                askInterval=-1;
            }else{
                try{
                    askInterval=Integer.parseInt(value.trim());
                }catch (Exception ex){
                    log.error("配置文件缺少: Device.askInterval值非法");
                    return false;
                }
            }
        }

        {
            String value = ele.getAttribute("timeout");
            if(value.trim().equals("")){
                timeout=DEFAULT_TIMEOUT;
            }else{
                try{
                    timeout=Integer.parseInt(value.trim());
                }catch (Exception ex){
                    log.error("配置文件缺少: Device.askInterval值非法");
                    return false;
                }
            }
        }

        {
            NodeList nodes = ele.getElementsByTagName("Topics");
            if(nodes.getLength()<=0){
                log.error("配置文件缺少: Device/Topics配置项");
                return false;
            }

            nodes=((Element)nodes.item(0)).getElementsByTagName("Topic");
            for(int i=0;i<nodes.getLength();i++){
                TopicConfigInfo topic=new TopicConfigInfo();
                boolean res=topic.Load((Element)nodes.item(i));
                if(!res){
                    topics.clear();
                    return false;
                }
                topics.add(topic);
            }
        }
        return true;
    }
}
