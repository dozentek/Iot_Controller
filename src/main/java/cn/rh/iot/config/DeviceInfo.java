package cn.rh.iot.config;

import cn.rh.iot.core.TopicType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

@Slf4j
public class DeviceInfo {

    @Getter @Setter
    private String des;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String serialNO;

    @Getter @Setter
    private String driverClassName;

    @Getter @Setter
    private int askInterval=-1;

    @Getter @Setter
    private TopicInfo subTopic;

    @Getter @Setter
    private TopicInfo pubTopic;


    public boolean Load(Element ele){
        if(ele==null){
            return false;
        }

        name=ele.getAttribute("name");
        if(name.equals("")){
            log.error("配置文件，name项不能为空");
            return false;
        }

        des=ele.getAttribute("des");
        serialNO=ele.getAttribute("sn");

        String sInterval=ele.getAttribute("askInterval");
        if(sInterval.equals("")){
            askInterval=-1;
        }else {
            try {
                askInterval = Integer.parseInt(sInterval.trim());
            }catch (NumberFormatException ex){
                askInterval=-1;
            }
        }

        //解析驱动类
        {
            NodeList nodes = ele.getElementsByTagName("Driver");
            if (nodes.getLength() <= 0) {
                log.error("配置文件缺少: Device/Driver配置项");
                return false;
            }
            driverClassName=nodes.item(0).getTextContent().trim();
        }

        //获取订阅主题
        {
            NodeList nodes = ele.getElementsByTagName("SubTopic");
            if (nodes.getLength() > 0) {
                subTopic=new TopicInfo();
                subTopic.setTopicName(nodes.item(0).getTextContent().trim());
                subTopic.setKind(TopicType.SUB);
            } else {
                subTopic = null;
            }
        }

        //获取发布主题
        {
            NodeList nodes = ele.getElementsByTagName("PubTopic");
            if (nodes.getLength() > 0) {
                pubTopic=new TopicInfo();
                String topicName=nodes.item(0).getTextContent().trim();
                pubTopic.setTopicName(topicName);
                pubTopic.setKind(TopicType.PUB);
            } else {
                pubTopic = null;
            }
        }

        return true;
    }
}
