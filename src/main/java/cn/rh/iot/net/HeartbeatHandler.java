package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Program: Iot_Controller
 * @Description: 心跳监测链路连通处理类，要放到
 * @Author: Y.Y
 * @Create: 2020-09-29 18:15
 **/
@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private final Device device;

    public HeartbeatHandler(Device device) {
        this.device = device;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {

        if (evt instanceof IdleStateEvent) {
            //---发送链路断路Mqtt报文
            if (device.getMqttChannel() != null) {
                device.getMqttChannel().SendConnectStateMessage("no");
            }
            //---尝试重连
            log.info("设备[{}]掉线，开始重连...", device.getName());
            if (device.getChannel() != null) {
                ctx.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        device.getChannel().Connect();
                    }
                }, 10, TimeUnit.MILLISECONDS);
            }
        }
    }
}
