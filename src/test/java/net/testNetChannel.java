package net;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Assembler;
import cn.rh.iot.core.Device;
import cn.rh.iot.core.DeviceManager;
import cn.rh.iot.driver.BmsDriver;
import cn.rh.iot.driver.DriverManager;
import cn.rh.iot.net.ByteToJsonDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @Program: Iot_Controller
 * @Description: 调试网络通信链路
 * @Author: Y.Y
 * @Create: 2020-09-27 20:36
 **/
@Slf4j
public class testNetChannel {

    private static String filepath = System.getProperty("user.dir") + "\\out\\production\\resources\\ConfigReal.xml";

    @Test
    public void testChannelWrite(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("BMS");
        Assembler.AssembleNetChannel(device);
        device.Start();

        device.getChannel().Write(new byte[]{0x24,0x24});
        try {
            Thread.sleep(2000);
        }catch (Exception ex){
            ;
        }
        device.Stop();
    }

    @Test
    public void testChannelRead(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("BMS");
        Assembler.AssembleNetChannel(device);
        device.Start();
        try {
            Thread.sleep(2000);
        }catch (Exception ex){
            ;
        }
        device.Stop();
    }

    @Test
    public void testFixedLengthFrameDecoder_ByteToJsonDecoder(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("BMS");
        device.setDriver(new BmsDriver());   //测试时，避免从文件加载的类文件与代码不一致

        Assembler.AssembleNetChannel(device);

        //可以一次装配多个Handler，模拟pipeline。
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(device.getDriver().getMessageLength()),
                                                      new ByteToJsonDecoder(device));


        //编号0字节帧信息，编号1-4号字节是“电池系统电压电流信息”帧ID，5、6字节对应电压，7、8字节对应电流
//        byte[] input=new byte[]{(byte)0x88,0x18,0x02,0x28,(byte)0xF4,0x00,(byte)0x10,(byte)0x00,0x20,0x00,0x00,0x00,0x00};

        //测试TCP粘包（长包）
        byte[] input=new byte[]{(byte)0x88,0x18,0x02,0x28,(byte)0xF4,0x00,(byte)0x10,(byte)0x00,0x20,0x00,0x00,0x00,0x00,
                                (byte)0x88,0x18,0x02,0x28,(byte)0xF4,0x00,(byte)0x20,(byte)0x00,0x40,0x00,0x00,0x00,0x00};


        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(input);
        ByteBuf data=buf.duplicate();

        //测试TCP断包
//        assertFalse(channel.writeInbound(data.readBytes(10)));
//        assertTrue(channel.writeInbound(data.readBytes(3)));

        assertTrue(channel.writeInbound(data.readBytes(26)));

        assertTrue(channel.finish());
        String json=(String)channel.readInbound();
        log.info(json);
        json=(String)channel.readInbound();
        log.info(json);
        assertNull(channel.readInbound());
    }
}
