package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

/**
 * @Program: IOT_Controller
 * @Description: 定时发送询问报文给设备
 * @Author: Y.Y
 * @Create: 2020-09-22 16:12
 **/
public class TimeAskHandler extends ChannelInboundHandlerAdapter {

    Logger logger= LoggerFactory.getLogger(TimeAskHandler.class);

    private final Device device;
    private volatile ScheduledFuture<?> askFuture;

    public TimeAskHandler(Device device) {
        this.device = device;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        logger.info(TimeAskHandler.class.toString()+"--"+"channelActive");
        //如果需要定时询问
        if (device.getAskInterval() > 0) {
            //信道建立后，马上发送询问报文
            ctx.writeAndFlush(Unpooled.copiedBuffer(device.getDriver().getAskMessage()));

            //启动定时询问任务
            byte[] askMessage = device.getDriver().getAskMessage();
            int interval = device.getAskInterval();
            askFuture = ctx.executor().scheduleAtFixedRate(new TimerSendMessageTask(ctx, askMessage), 0, interval, TimeUnit.MILLISECONDS);

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (askFuture != null){
            askFuture.cancel(true);
            askFuture = null;
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
//        device.Connect();
    }

    private class TimerSendMessageTask implements Runnable {

        private final ChannelHandlerContext ctx;
        private byte[] message;

        public TimerSendMessageTask(ChannelHandlerContext ctx, byte[] message) {
            this.ctx = ctx;
            this.message = message;
        }

        @Override
        public void run() {
            if(ctx.channel().isActive()) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(message));
            }
        }
    }
}
