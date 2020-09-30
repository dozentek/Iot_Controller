package cn.rh.iot.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Program: Iot_Controller
 * @Description: 测试用数据处理Handler（单元测试用）
 * @Author: Y.Y
 * @Create: 2020-09-28 13:43
 **/
public class directHandler extends MessageToByteEncoder<byte[]> {
    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        if(msg!=null && msg.length>0){
            out.writeBytes(msg);
        }
    }
}

