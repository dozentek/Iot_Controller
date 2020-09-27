package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * @program: IOT_Controller
 * @description: 将Json字符串编码为Byte指令
 * @author: Y.Y
 * @create: 2020-09-22 14:07
 **/
public class JsonToByteEncoder extends MessageToByteEncoder<String> {

    private Device device;

    public JsonToByteEncoder(Device device) {
        this.device = device;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {

        JSONObject obj= JSON.parseObject(msg);
        String payloadJson=obj.getString("payload");

        byte[] data=device.getDriver().encode(payloadJson);
        if(data.length>0) {
            out.writeBytes(data);
        }
    }
}
