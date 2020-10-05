package cn.rh.iot.net;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Program: IOT_Controller
 * @Description: 带处理逻辑的网络连接类
 * @Author: Y.Y
 * @Create: 2020-09-25 09:48
 **/
@Slf4j
public class NetChannel implements IChannel {

    @Getter @Setter
    private Channel netChannel;
    @Getter
    private final Device device;

    @Getter @Setter
    private boolean initiativeClose=false;

    public NetChannel(NetDevice device) {
        this.device = device;
    }

    @Override
    public boolean isConnected(){
        if(netChannel==null){
            return false;
        }
        return netChannel.isActive();
    }

    @Override
    public void Write(byte[] data){
       if(netChannel!=null && netChannel.isWritable()){
           netChannel.writeAndFlush(Unpooled.copiedBuffer(data));
       }
    }

    @Override
    public void Connect(){

        if(netChannel!=null){
            netChannel.close();
            netChannel=null;
        }

        Bootstrap b=new Bootstrap();
        {
            device.setBootstrap(b);
            b.group(DeviceManager.getInstance().getGroup());

            if (((NetDevice) device).getProtocol() == NetProtocolType.UDP) {
                b.channel(NioDatagramChannel.class);
            } else {
                b.channel(NioSocketChannel.class);
            }
            //连接超时设定，其将在connect()上应用
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,IotConfig.getInstance().getConnectTimeout());
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_KEEPALIVE, true);

            String ip = ((NetDevice) device).getIp();
            int port = ((NetDevice) device).getPort();
            b.remoteAddress(ip, port);

            b.handler(new NetChannelInitializer(device, this));
        }
        ChannelFuture future=device.getBootstrap().connect().awaitUninterruptibly();
        future.addListener((ChannelFutureListener) futureListener -> {
            if(futureListener.isSuccess()) {
                netChannel= futureListener.channel();
                log.info("与设备[{}]连接成功", device.getName());
                if(device.getMqttChannel()!=null){
                    device.getMqttChannel().SendConnectStateMessage("ok");
                }
                initiativeClose=false;
            }else{
                log.info("与设备[{}]连接失败，尝试重连...",device.getName());
                futureListener.channel().eventLoop().schedule(() -> {
                    Connect();
                }, IotConfig.getInstance().getReconnectInterval(), TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public void Disconnect() {
        if(netChannel!=null && netChannel.isOpen()){
            initiativeClose=true;
            ChannelFuture future=netChannel.close();
            future.addListener(new ChannelCloseListenerWithLock(device));
            try{
                future.await(500,TimeUnit.MILLISECONDS);
            }catch (Exception ignored){
            }
        }
    }
}
