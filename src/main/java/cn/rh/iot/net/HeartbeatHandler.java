package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Program: Iot_Controller
 * @Description: 心跳监测链路连通处理类，要放到
 * @Author: Y.Y
 * @Create: 2020-09-29 18:15
 **/
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private Device device;

    public HeartbeatHandler(Device device) {
        this.device = device;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            if(device.getMqttChannel()!=null){
                device.getMqttChannel().SendConnectStateMessage("no");
            }
        }
    }
}
