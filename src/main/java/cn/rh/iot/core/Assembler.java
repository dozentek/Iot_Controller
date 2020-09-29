package cn.rh.iot.core;

import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.net.NetChannel;
import cn.rh.iot.net.NetChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @program: IOT_Controller
 * @description: 对设备Channel进行各种设置与装配
 * @author: Y.Y
 * @create: 2020-09-22 17:29
 **/
public class Assembler {

    public static void AssembleNetChannel(Device device){

        NetChannel channel=new NetChannel((NetDevice)device);
        device.setChannel(channel);
        
        Bootstrap b= device.getBootstrap();
        b.group(DeviceManager.getInstance().getGroup());

        if (((NetDevice)device).getProtocol() == NetProtocolType.UDP) {
            b.channel(NioDatagramChannel.class);
        } else {
            b.channel(NioSocketChannel.class);
        }
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.SO_KEEPALIVE, true);

        b.handler(new NetChannelInitializer(device,channel));
    }

    public static void AssembleMqttChannel(Device device){
        device.setMqttChannel(new MqttChannel(device));
    }
}
