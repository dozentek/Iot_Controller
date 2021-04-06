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

@Slf4j
public class NetChannel implements IChannel {

    @Getter @Setter
    private Channel netChannel;
    @Getter
    private final Bridge bridge;

    @Getter @Setter
    private boolean initiativeClose=false;

    public NetChannel(Bridge bridge) {
        this.bridge = bridge;
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
            b.group(bridge.getGroup());
            bridge.setBootstrap(b);

            if (bridge.getProtocol() == NetProtocolType.UDP) {
                b.channel(NioDatagramChannel.class);
            } else {
                b.channel(NioSocketChannel.class);
            }
            //连接超时设定，其将在connect()上应用
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,IotConfig.getInstance().getConnectTimeout());
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_KEEPALIVE, true);

            String ip = bridge.getIp();
            int port = bridge.getPort();
            b.remoteAddress(ip, port);

            b.handler(new NetChannelInitializer(bridge, this));
        }
        ChannelFuture future=bridge.getBootstrap().connect().awaitUninterruptibly();
        future.addListener((ChannelFutureListener) futureListener -> {
            if(futureListener.isSuccess()) {
                netChannel= futureListener.channel();
                log.info("[{}]连接成功", bridge.getName());
                bridge.SendConnectStateTopic(true);
                initiativeClose=false;
            }else{
                log.info("[{}]连接失败，尝试重连...",bridge.getName());
                futureListener.channel().eventLoop().schedule(this::Connect, IotConfig.getInstance().getReconnectInterval(), TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public void Disconnect() {
        if(netChannel!=null && netChannel.isOpen()){
            initiativeClose=true;
            ChannelFuture future=netChannel.close();
            future.addListener(new ChannelCloseListenerWithLock(bridge));
            try{
                future.await(500,TimeUnit.MILLISECONDS);
            }catch (Exception ignored){
            }
        }
    }
}
