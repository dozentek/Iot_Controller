package cn.rh.iot.net;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Bridge;
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

public class NetChannelInitializer extends ChannelInitializer<Channel> {

    private final static int MAX_LENGTH=1024;
    private final static int DEFAULT_TIMEOUT=20000;

    @Getter
    private final Bridge bridge;
    @Getter
    private final NetChannel channel;

    public NetChannelInitializer(Bridge bridge, NetChannel channel) {
        this.bridge = bridge;
        this.channel=channel;
    }

    @Override
    protected void initChannel(Channel ch) {

        ChannelPipeline pipeline = ch.pipeline();

        //----装配信道连通状态判断及处理Handler
        {
            int timeout = bridge.getTimeout();
            if (timeout <= 0) {
                timeout = IotConfig.getInstance().getNetDefaultTimeout();
            }
            pipeline.addLast("HeartListener",new IdleStateHandler(timeout, 0, 0, TimeUnit.MILLISECONDS));
        }

        //----装配解决TCP粘包的解码器
        {
            switch (bridge.getMessageType()) {
                case FixLength:
                    pipeline.addLast(new FixedLengthFrameDecoder(bridge.getMessageLength()));
                    break;
                case Delimiter:
                    ByteBuf delimiter = Unpooled.copiedBuffer(bridge.getTrailer());
                    pipeline.addLast(new DelimiterBasedFrameDecoder(MAX_LENGTH, delimiter));
                    break;
                case LengthField:
                    int lFieldOffset = bridge.getLengthFieldOffset();
                    int lFieldLength = bridge.getLengthFieldLength();
                    int lAdjustment = bridge.getLengthAdjustment();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_LENGTH, lFieldOffset,
                                         lFieldLength, lAdjustment, 0));
                    break;
            }
        }

        //----装配解码器
        {
            pipeline.addLast("B2J",new ByteToJsonDecoder(bridge));
        }
    }
}
