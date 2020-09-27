package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @program: IOT_Controller
 * @description: 将字节数据解码为json信息
 * @author: Y.Y
 * @create: 2020-09-22 08:44
 **/
public class ByteToJsonDecoder extends ByteToMessageDecoder {

    private Device device;

    public ByteToJsonDecoder(Device device) {
        this.device = device;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(device.getDriver()==null){
            return;
        }
        if(in.readableBytes()>0) {
            ByteBuf data = in.readBytes(in.readableBytes());
            byte[] inData = new byte[data.readableBytes()];
            String msg=device.getDriver().decode(inData);

            //对于无法解析的数据，返回空字符串
            if(msg==null || msg==""){
                return;
            }
            out.add(Pack(msg));
        }
    }

    private String Pack(String msg){

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(System.lineSeparator());
        sb.append("\"deviceName\":\"").append(device.getName()).append("\",").append(System.lineSeparator());
        sb.append("\"deviceNumber\":\"").append(device.getId()).append("\",").append(System.lineSeparator());
        sb.append(msg);
        sb.append("}");

        return sb.toString();
    }
}
