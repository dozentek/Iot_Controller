package cn.rh.iot.mqtt;

import java.util.EventListener;

public interface TopicArrivedHandler extends EventListener {

    public void TopicArrived(TopicArrivedEvent event);
}
