package cn.rh.iot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;

/**
 * @Program: Iot_Controller
 * @Description: 主题配置信息类
 * @Author: Y.Y
 * @Create: 2020-09-27 11:40
 **/
@Slf4j
public class TopicConfigInfo {
    @Getter @Setter
    private String topicName;
    @Getter @Setter
    private String kind;

    public boolean equal(String topicName,String kind){
        try {
            if (this.topicName.trim() == topicName.trim() && this.kind.trim() == kind.trim()) {
                return true;
            }
        }catch (Exception ex){
            return false;
        }
        return false;
    }

    public boolean Load(Element ele){
        if(ele==null){
            return false;
        }
        topicName=ele.getTextContent().trim();
        if(topicName==""){
            log.error("配置文件，Topic项不能为空");
            return false;
        }
        if(!ele.getAttribute("kind").trim().toUpperCase().equals("IN")){
            kind="OUT";
        }else{
            kind="IN";
        }
        return true;
    }

}
