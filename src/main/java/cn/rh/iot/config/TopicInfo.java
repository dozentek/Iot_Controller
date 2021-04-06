package cn.rh.iot.config;

import cn.rh.iot.core.TopicType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;

@Slf4j
public class TopicInfo {
    @Getter @Setter
    private String topicName;
    @Getter @Setter
    private TopicType kind;
}
