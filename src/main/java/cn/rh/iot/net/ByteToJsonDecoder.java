package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Program: IOT_Controller
 * @Description: 将字节数据解码为json信息
 * @Author: Y.Y
 * @Create: 2020-09-22 08:44
 **/
public class ByteToJsonDecoder extends ByteToMessageDecoder {

    private final Device device;

    public ByteToJsonDecoder(Device device) {
        this.device = device;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out){
        if(device.getDriver()==null){
            return;
        }
        if(in.readableBytes()>0) {
            byte[] inData = new byte[in.readableBytes()];
            in.readBytes(inData);
            String msg=device.getDriver().decode(inData);

            //对于无法解析的数据，返回空字符串
            if(msg==null || msg.equals("")){
                return;
            }
            out.add(Pack(msg));

            if(device.getMqttChannel()!=null){
                device.getMqttChannel().Write(Pack(msg));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private String Pack(String msg){

        return "{" +
                System.lineSeparator() +
                "\"deviceName\":\"" + device.getName() + "\"," + System.lineSeparator() +
                "\"deviceNumber\":\"" + (device.getId() == null ? "\"" : device.getId()) + "\"," + System.lineSeparator() +
                msg + System.lineSeparator() +
                "}";
    }
}
