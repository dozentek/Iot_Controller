package cn.rh.iot.core;

import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.net.NetChannel;

/**
 * @Program: IOT_Controller
 * @Description: 对设备Channel进行装配
 * @Author: Y.Y
 * @Create: 2020-09-22 17:29
 **/
public class Assembler {

    public static void AssembleNetChannel(Device device){
        device.setChannel(new NetChannel((NetDevice)device));
    }

    public static void AssembleMqttChannel(Device device){
        device.setMqttChannel(new MqttChannel(device));
    }
}
