package cn.rh.iot.core;

import cn.rh.iot.mqtt.MqttChannel;
import cn.rh.iot.net.NetChannel;
import cn.rh.iot.net.NetChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @program: IOT_Controller
 * @description: 对设备Channel进行各种设置与装配
 * @author: Y.Y
 * @create: 2020-09-22 17:29
 **/
public class Assembler {
    private static EventLoopGroup group = new NioEventLoopGroup();

    public static void AssembleNetChannel(Device device){

        try {
            device.setChannel(new NetChannel((NetDevice)device));
            Bootstrap b= device.getBootstrap();
            b.group(group);
            if (((NetDevice)device).getProtocol() == NetProtocolType.UDP) {
                b.channel(NioDatagramChannel.class);
            } else {
                b.channel(NioSocketChannel.class);
            }
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new NetChannelInitializer(device));

        }catch(Exception ex){

        }

    }

    public static void AssembleMqttChannel(Device device){
        String host= MqttConfig.Instance().get("url").trim();
        String username= MqttConfig.Instance().get("username").trim();
        String password= MqttConfig.Instance().get("password").trim();
        device.setMqttChannel(new MqttChannel(device,host,username,password));
    }
}
