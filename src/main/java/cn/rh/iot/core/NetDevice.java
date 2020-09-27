package cn.rh.iot.core;

import cn.rh.iot.net.NetChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @Program: IOT_Controller
 * @Description: 网络接口设备
 * @Author: Y.Y
 * @Create: 2020-09-18 12:13
 **/
public class NetDevice extends Device {

    private static int RECONNECT_INTERVAL=3000;  //ms

    @Getter @Setter
    private NetRoleType role;
    @Getter @Setter
    private ProtocolType protocol;
    @Getter @Setter
    private InetSocketAddress address;

    private EventLoopGroup group;

    public NetDevice(EventLoopGroup group) {
        this.group=group;
    }

    public void Connect() {
        if(this.channel==null){
            return;
        }
        if(this.channel.isConnected()){
            return;
        }
        ChannelFuture future=this.bootstrap.connect(address.getAddress().getHostAddress(), address.getPort());
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if(futureListener.isSuccess()){
                    ((NetChannel)channel).setNetChannel((SocketChannel)futureListener.channel());
                }else{
                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            Connect();
                        }
                    },RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    public void Disconnect(){
        if(channel==null){
            return;
        }

    }
}
