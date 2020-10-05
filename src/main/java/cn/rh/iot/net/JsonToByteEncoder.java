package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Program: IOT_Controller
 * @Description: 将Json字符串编码为Byte指令
 * @Author: Y.Y
 * @Create: 2020-09-22 14:07
 **/
@Slf4j
public class JsonToByteEncoder extends MessageToByteEncoder<String> {

    private final static int CTRL_FRAME_MSG_NUMBER=3;
    private final Device device;

    public JsonToByteEncoder(Device device) {
        this.device = device;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {

        JSONObject obj= JSON.parseObject(msg);
        int msgNumber=obj.getInteger("msgId");

        if(device.getDriver()==null){
            throw new Exception(device.getName()+"没有加载驱动程序");
        }

        if(msgNumber==CTRL_FRAME_MSG_NUMBER) {
            byte[] data = device.getDriver().encode(msg);
            if (data!=null && data.length > 0) {
                out.writeBytes(data);
            }
        }
    }

}
