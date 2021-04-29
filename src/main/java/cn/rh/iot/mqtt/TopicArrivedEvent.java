package cn.rh.iot.mqtt;

import lombok.Getter;

import java.util.EventObject;

public class TopicArrivedEvent extends EventObject {

    @Getter
    private String topic;

    @Getter
    private String content;

    public TopicArrivedEvent(Object source,String topic,String content){
        super(source);
        this.topic=topic;
        this.content=content;
    }
}
