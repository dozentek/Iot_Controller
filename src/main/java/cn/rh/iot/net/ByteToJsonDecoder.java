package cn.rh.iot.net;

import cn.rh.iot.core.Bridge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Program: IOT_Controller
 * @Description: 将字节数据解码为json信息
 * @Author: Y.Y
 * @Create: 2020-09-22 08:44
 **/
@Slf4j
public class ByteToJsonDecoder extends ByteToMessageDecoder {

    private final Bridge bridge;

    public ByteToJsonDecoder(Bridge device) {
        this.bridge = device;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            if(bridge.getChannel()!=null){
                ((NetChannel) bridge.getChannel()).setInitiativeClose(true);
            }
            Reconnect("发送数据超时");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(bridge.getChannel()!=null){
            if(!((NetChannel) bridge.getChannel()).isInitiativeClose()){
                Reconnect("失去连接");
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out){

        if(in.readableBytes()>0) {
            byte[] inData = new byte[in.readableBytes()];
            in.readBytes(inData);
            bridge.Byte2JsonAndSendMqtt(inData);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("[{}]连接异常，原因：{}", bridge.getName(),cause.getMessage());
    }


    private void Reconnect(String disconnectReason){

        bridge.SendConnectStateTopic(false);
        //尝试重连
        log.info("[{}]{}，尝试重连...", bridge.getName(),disconnectReason);

        if (bridge.getChannel() != null) {
            //重要，要在重连之前移走定时Handler，否则它会定时触发userEventTriggered，造成多次Reconnect
            ((NetChannel) bridge.getChannel()).getNetChannel().pipeline().remove("HeartListener");
            bridge.getChannel().Connect();
        }
    }
}
