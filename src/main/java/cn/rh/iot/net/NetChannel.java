package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import cn.rh.iot.core.IChannel;
import cn.rh.iot.core.NetDevice;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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

    private static final int RECONNECT_INTERVAL=3000;  //ms

    @Getter @Setter
    private Channel netChannel;
    @Getter
    private final Device device;

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
        if(device==null ||(netChannel!=null && netChannel.isActive())){
            return;
        }
        String ip=((NetDevice)device).getIp();
        int port=((NetDevice)device).getPort();

        ChannelFuture future=device.getBootstrap().connect(ip, port);
        future.addListener((ChannelFutureListener) futureListener -> {
            if(futureListener.isSuccess()){
                netChannel= futureListener.channel();
                log.info("设备[{}]连接成功",device.getName());
                if(device.getMqttChannel()!=null){
                    device.getMqttChannel().SendConnectStateMessage("ok");
                }
            }else{
                futureListener.channel().eventLoop().schedule(() -> {
                    log.info("设备[{}]连接失败，尝试重连...",device.getName());
                    Connect();
                },RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public void Disconnect() {
        Disconnect(null);
    }

    @Override
    public void Disconnect(Object lock) {
        if(netChannel!=null && netChannel.isOpen()){
            ChannelFuture future=netChannel.close();
            future.addListener(new ChannelCloseListenerWithLock(device,lock));
        }
    }
}
