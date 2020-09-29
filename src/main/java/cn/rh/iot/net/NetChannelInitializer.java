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
import lombok.Getter;

import java.util.concurrent.TimeUnit;


/**
 * @program: IOT_Controller
 * @description: 为Channel装配Handler链
 * @author: Y.Y
 * @create: 2020-09-22 08:34
 **/
public class NetChannelInitializer extends ChannelInitializer<Channel> {

    private final static int MAX_LENGTH=1024;
    private final static int DEFAULT_TIMEOUT=60000;

    @Getter
    private final Device device;
    @Getter
    private final NetChannel channel;

    public NetChannelInitializer(Device device,NetChannel channel) {
        this.device=device;
        this.channel=channel;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        {
            //装配信道通断探测处理器
            int timeout = device.getTimeout();
            if (timeout <= 0) {
                timeout = DEFAULT_TIMEOUT;
            }
            pipeline.addLast(new IdleStateHandler(timeout, 0, 0, TimeUnit.MILLISECONDS));
            pipeline.addLast(new HeartbeatHandler(device));
        }

        {
            //装配定时发送询问报文的Handler
            if (device.getAskInterval() > 0) {
                pipeline.addLast(new TimeAskHandler(device));
            }
        }

        {
            //装配解决TCP粘包的解码器
            switch (device.getDriver().getType()) {
                case FixLength:
                    pipeline.addLast(new FixedLengthFrameDecoder(device.getDriver().getMessageLength()));
                    break;
                case Delimiter:
                    ByteBuf delimiter = Unpooled.copiedBuffer(device.getDriver().getTrailer());
                    pipeline.addLast(new DelimiterBasedFrameDecoder(MAX_LENGTH, delimiter));
                    break;
                case LengthField:
                    int lFieldOffset = device.getDriver().getLengthFieldOffset();
                    int lFieldLength = device.getDriver().getLengthFieldLength();
                    int lAdjustment = device.getDriver().getLengthAdjustment();
                    LengthFieldBasedFrameDecoder decoder =
                            new LengthFieldBasedFrameDecoder(MAX_LENGTH, lFieldOffset, lFieldLength, lAdjustment, 0);
                    pipeline.addLast(decoder);
                    break;
            }
        }

        {
            //装配解码器,形成json中"payload"关键字的value;
            pipeline.addLast(new ByteToJsonDecoder(device));
            //装配编码器
            pipeline.addLast(new JsonToByteEncoder(device));
        }
    }
}
