package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Program: IOT_Controller
 * @Description: 定时发送询问报文给设备
 * @Author: Y.Y
 * @Create: 2020-09-22 16:12
 **/
@Slf4j
public class TimeAskHandler extends ChannelInboundHandlerAdapter {

    //为什么是ChannelInboundHandler，而不是ChannelOutboundHandler？
    //因为只有InboundHandler才能感知channel是否Active
    private final Device device;
    private volatile ScheduledFuture<?> askFuture;

    public TimeAskHandler(Device device) {
        this.device = device;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        int interval = device.getAskInterval();
        if (interval > 0) {
            byte[] askMessage = device.getDriver().getAskMessage();
            askFuture = ctx.executor().scheduleAtFixedRate(new TimerSendMessageTask(ctx, askMessage), 0, interval, TimeUnit.MILLISECONDS);
        }
    }

    private class TimerSendMessageTask implements Runnable {

        private final ChannelHandlerContext ctx;
        private final byte[] message;

        public TimerSendMessageTask(ChannelHandlerContext ctx, byte[] message) {
            this.ctx = ctx;
            this.message = message;
        }

        @Override
        public void run() {
            if(ctx.channel().isActive()) {
                ChannelHandlerContext askFrameHandlerContext=ctx.pipeline().context("askFrameHandler");
                if(askFrameHandlerContext!=null) {
                    ChannelFuture future = askFrameHandlerContext.writeAndFlush(Unpooled.copiedBuffer(message));
                    future.addListener((ChannelFutureListener) futureListener -> {
                        if (futureListener.isSuccess()) {
                            log.info("设备[{}]发送数据获取报文成功", device.getName());
                        } else {
                            log.info("设备[{}]发送数据获取报文失败", device.getName());
                        }
                    });
                }
            }
        }
    }
}
