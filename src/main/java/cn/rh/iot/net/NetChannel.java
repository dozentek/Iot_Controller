package cn.rh.iot.net;

import cn.rh.iot.core.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @Program: IOT_Controller
 * @Description: 带处理逻辑的网络连接类
 * @Author: Y.Y
 * @Create: 2020-09-25 09:48
 **/
@Slf4j
public class NetChannel implements IChannel {

    private static int RECONNECT_INTERVAL=3000;  //ms

    @Getter @Setter
    private Channel netChannel;
    @Getter
    private final Bootstrap bootstrap;
    @Getter
    private final Device device;
    @Getter @Setter
    private NetRoleType role;
    @Getter @Setter
    private ProtocolType protocol;
    @Getter @Setter
    private InetSocketAddress address;

    public NetChannel(NetDevice device) {
        this.device = device;
        bootstrap=new Bootstrap();
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
           netChannel.writeAndFlush(data);
       }
    }

    @Override
    public void Connect(){
        if(netChannel!=null && netChannel.isActive()){
            return;
        }
        ChannelFuture future=bootstrap.connect(address.getAddress().getHostAddress(), address.getPort());
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
        if(netChannel!=null){
            netChannel.close();
        }
    }
}
