package cn.rh.iot.net;

import cn.rh.iot.core.Device;
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

    private final Device device;

    public ByteToJsonDecoder(Device device) {
        this.device = device;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            if(device.getChannel()!=null){
                ((NetChannel)device.getChannel()).setInitiativeClose(true);
            }
            Reconnect("发送数据超时");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(device.getChannel()!=null){
            if(!((NetChannel)device.getChannel()).isInitiativeClose()){
                Reconnect("失去连接");
            }
        }
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
            String realJson=Pack(msg);
            out.add(realJson);         //这行代码没有用

            if(device.getMqttChannel()!=null){
                device.getMqttChannel().Write(realJson);
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

    private void Reconnect(String disconnectReason){

        //尝试重连
        log.info("设备[{}]{}，尝试重连...", device.getName(),disconnectReason);

        //发送链路断路Mqtt报文
        if (device.getMqttChannel() != null) {
            device.getMqttChannel().SendConnectStateMessage("no");
        }
        if (device.getChannel() != null) {
            //重要，要在重连之前移走定时Handler，否则它会定时触发userEventTriggered，造成多次Reconnect
            ((NetChannel) device.getChannel()).getNetChannel().pipeline().remove("HeartListener");
            device.getChannel().Connect();
        }
    }
}
