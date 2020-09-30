package cn.rh.iot.net;

import cn.rh.iot.core.Device;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @Program: Iot_Controller
 * @Description: 带输入锁的网络Channel关闭操作结果监听类
 * @Author: Y.Y
 * @Create: 2020-09-30 12:04
 **/
@Slf4j
public class ChannelCloseListenerWithLock implements ChannelFutureListener {

    private final Object lock;
    private final Device device;

    public ChannelCloseListenerWithLock(Device device, Object lock) {
        this.lock = lock;
        this.device=device;
    }

    @Override
    public void operationComplete(ChannelFuture future){
        if(future.isSuccess()) {
            log.info("设备[{}]断开连接",device.getName());
            if(device.getMqttChannel()!=null){
                device.getMqttChannel().SendConnectStateMessage("no");
            }
        }else{
            log.info("设备[{}]断开连接失败。原因:{}",device.getName(),future.cause().getMessage());
        }
        if(lock!=null) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

}
