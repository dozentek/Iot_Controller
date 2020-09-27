package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


/**
 * @program: IOT_Controller
 * @description: 为Channel装配Handler链
 * @author: Y.Y
 * @create: 2020-09-22 08:34
 **/
public class NetChannelInitializer extends ChannelInitializer<Channel> {

    private static int MAX_LENGTH=1024;

    private Device device;
    public NetChannelInitializer(Device device) {
        this.device=device;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        //注册空闲事件处理器
        int timeout=device.getTimeout();
        if(timeout<=0) { timeout=10; }
        pipeline.addLast(new IdleStateHandler(timeout,0,0, TimeUnit.MILLISECONDS));

        //装配定时发送询问报文的Handler
        if(device.getAskInterval()>0){
            pipeline.addLast(new TimeAskHandler(device));
        }
        //装配解决TCP粘包的解码器
        switch (device.getDriver().getType()){
            case FixLength:
                pipeline.addLast(new FixedLengthFrameDecoder(device.getDriver().getMessageLength()));
                break;
            case Delimiter:
                ByteBuf delimiter= Unpooled.copiedBuffer(device.getDriver().getTrailer());
                pipeline.addLast(new DelimiterBasedFrameDecoder(MAX_LENGTH, delimiter));
                break;
            case LengthField:
                int lFieldOffset=device.getDriver().getLengthFieldOffset();
                int lFieldLength= device.getDriver().getLengthFieldLength();
                int lAdjustment= device.getDriver().getLengthAdjustment();
                LengthFieldBasedFrameDecoder decoder=
                        new LengthFieldBasedFrameDecoder(MAX_LENGTH,lFieldOffset,lFieldLength,lAdjustment,0);
                pipeline.addLast(decoder);
                break;
        }

        //装配报文解码器,形成json中"payload"关键字的value;
        pipeline.addLast(new ByteToJsonDecoder(device));

        //装配报文编码器
        pipeline.addLast(new JsonToByteEncoder(device));
    }
}
