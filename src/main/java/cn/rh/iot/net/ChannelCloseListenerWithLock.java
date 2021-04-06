package cn.rh.iot.net;

import cn.rh.iot.core.Bridge;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelCloseListenerWithLock implements ChannelFutureListener {

    private final Bridge bridge;

    public ChannelCloseListenerWithLock(Bridge bridge) {
        this.bridge=bridge;
    }

    @Override
    public void operationComplete(ChannelFuture future){
        if(future.isSuccess()) {
            log.info("与[{}]断开连接",bridge.getName());
            bridge.SendConnectStateTopic(false);
        }else{
            log.info("与[{}]断开连接失败。原因:{}",bridge.getName(),future.cause().getMessage());
        }
    }

}
