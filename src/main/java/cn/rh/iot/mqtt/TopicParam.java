package cn.rh.iot.mqtt;

import lombok.Getter;
import lombok.Setter;

/**
 * @Program: IOT_Controller
 * @Description: 代表一个主题和其传输需要的QoS值
 * @Author: Y.Y
 * @Create: 2020-09-20 20:33
 **/
public class TopicParam {
    @Getter @Setter
    private String topic;
    @Getter @Setter
    private int qos;

    public TopicParam() {
        qos=0;
    }

    public TopicParam(String topic) {
        this.topic = topic;
        qos=0;
    }

    public TopicParam(String topic, int qos) {
        this.topic = topic;
        this.qos = qos;
    }
}
