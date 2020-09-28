package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import cn.rh.iot.core.IChannel;
import cn.rh.iot.core.NetDevice;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
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
                netChannel=(SocketChannel)futureListener.channel();
                log.info("与设备："+device.getName()+"连接成功");
            }else{
                futureListener.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        log.info(device.getName()+"连接失败，尝试重接...");
                        Connect();
                    }
                },RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public void Disconnect() {
        if(netChannel!=null && netChannel.isOpen()){
            ChannelFuture future=netChannel.close();
            future.addListener((ChannelFutureListener) futureListener -> {
                if(futureListener.isSuccess()){
                    log.info("与设备："+device.getName()+"断开连接");
                }
            });
        }
    }
}
