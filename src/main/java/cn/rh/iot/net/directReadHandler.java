package cn.rh.iot.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Program: Iot_Controller
 * @Description: 直接读取测试类
 * @Author: Y.Y
 * @Create: 2020-09-28 14:21
 **/
@Slf4j
public class directReadHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer=(ByteBuf)msg;
        byte[] bytArray = new byte[buffer.readableBytes()];
        buffer.getBytes(0,bytArray);
        log.info(new String(bytArray));
    }
}
