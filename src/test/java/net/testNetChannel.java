package net;

import cn.rh.iot.config.IotConfig;
import cn.rh.iot.core.Assembler;
import cn.rh.iot.core.Device;
import cn.rh.iot.core.DeviceManager;
import cn.rh.iot.driver.*;
import cn.rh.iot.driver.base.ByteUtil;
import cn.rh.iot.driver.base.DriverManager;
import cn.rh.iot.net.ByteToJsonDecoder;
import cn.rh.iot.net.JsonToByteEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @Program: Iot_Controller
 * @Description: 调试网络通信链路
 * @Author: Y.Y
 * @Create: 2020-09-27 20:36
 **/
@Slf4j
public class testNetChannel {

    private static final String filepath = System.getProperty("user.dir") + "\\out\\production\\resources\\Config.xml";

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
        }
        device.Stop();
    }

    @Test
    public void testBMSAndRTKDriver(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

//        Device device=DeviceManager.getInstance().getDevice("BMS");
//        device.setDriver(new BmsDriver());   //测试时，避免从文件加载的类文件与代码不一致

        Device device=DeviceManager.getInstance().getDevice("RTK");
        device.setDriver(new RTKDriver());   //测试时，避免从文件加载的类文件与代码不一致

        Assembler.AssembleNetChannel(device);

//        //BMS测试  可以一次装配多个Handler，模拟pipeline。
//        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(device.getDriver().getMessageLength()),
//                                                      new ByteToJsonDecoder(device));

        //RTK测试
        ByteBuf delimiter= Unpooled.copiedBuffer(device.getDriver().getTrailer());
        ChannelHandler delimiterHandler=new DelimiterBasedFrameDecoder(1024, delimiter);
        EmbeddedChannel channel = new EmbeddedChannel(delimiterHandler,new ByteToJsonDecoder(device));



        //编号0字节帧信息，编号1-4号字节是“电池系统电压电流信息”帧ID，5、6字节对应电压，7、8字节对应电流
//        byte[] input=new byte[]{(byte)0x88,0x18,0x02,0x28,(byte)0xF4,0x00,(byte)0x10,(byte)0x00,0x20,0x00,0x00,0x00,0x00};

//        //测试TCP粘包（长包）
//        byte[] input=new byte[]{(byte)0x88,0x18,0x02,0x28,(byte)0xF4,0x00,(byte)0x10,(byte)0x00,0x20,0x00,0x00,0x00,0x00,
//                                (byte)0x88,0x18,0x02,0x28,(byte)0xF4,0x00,(byte)0x20,(byte)0x00,0x40,0x00,0x00,0x00,0x00};


        //测试RTK报文
        String inputString="$GPGGA,014434.70,3817.13334637,N,12139.72994196,E,4,07,1.5,6.571,M,8.942,M,0.7,0016*79"+"\r\n";
        byte[] input=inputString.getBytes();

        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(input);
        ByteBuf data=buf.duplicate();

        //测试BMS报文 TCP断包
//        assertFalse(channel.writeInbound(data.readBytes(10)));
//        assertTrue(channel.writeInbound(data.readBytes(3)));

        assertTrue(channel.writeInbound(data));
        assertTrue(channel.finish());
        log.info((String)channel.readInbound());

        //测试粘包（多个报文），多次读取数据
//        json=(String)channel.readInbound();
//        log.info(json);
//        assertNull(channel.readInbound());
    }

    @Test
    public void testLiftDriver(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("Lift");
        device.setDriver(new LiftDriver());

        Assembler.AssembleNetChannel(device);

        int lFieldOffset=device.getDriver().getLengthFieldOffset();
        int lFieldLength= device.getDriver().getLengthFieldLength();
        int lAdjustment= device.getDriver().getLengthAdjustment();
        LengthFieldBasedFrameDecoder decoder=
                new LengthFieldBasedFrameDecoder(1024,lFieldOffset,lFieldLength,lAdjustment,0);

        EmbeddedChannel channel = new EmbeddedChannel(decoder,
                new ByteToJsonDecoder(device),
                new JsonToByteEncoder(device));

        //返回报文
        byte[] input=new byte[]{0x00,0x01,0x00,0x00,0x00,0x05,0x01,0x03,0x02,(byte)0x82,(byte)0x80};

        String  output="{\n" +
                "    \"deviceName\": \"Lift\",\n" +
                "    \"deviceNumber\":\"\",\n" +
                "    \"msgId\":03,\n" +
                "    \"payload\":{  \n" +
                "    \t\"serialNumber\": 01,\n" +
                "        \"msg\":\"up\"\n" +
                "\t}\n" +
                "}";

        //测试状态信息解码
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(input);
        ByteBuf data=buf.duplicate();

        assertTrue(channel.writeInbound(data));
        String json=(String)channel.readInbound();
        log.info(json);

        //测试控制指令编码
        assertTrue(channel.writeOutbound(output));
        assertTrue(channel.finish());
        ByteBuf ret=channel.readOutbound();
        byte[] controlMsg=new byte[ret.readableBytes()];
        ret.readBytes(controlMsg);

        assertTrue(controlMsg!=null);
        log.info(ByteUtil.bytesToHex(controlMsg));

    }

    @Test
    public void testMetDriver(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("Met");
        device.setDriver(new MetDriver());

        Assembler.AssembleNetChannel(device);

        int lFieldOffset=device.getDriver().getLengthFieldOffset();
        int lFieldLength= device.getDriver().getLengthFieldLength();
        int lAdjustment= device.getDriver().getLengthAdjustment();
        LengthFieldBasedFrameDecoder decoder=
                new LengthFieldBasedFrameDecoder(1024,lFieldOffset,lFieldLength,lAdjustment,0);

        EmbeddedChannel channel = new EmbeddedChannel(decoder,
                new ByteToJsonDecoder(device),
                new JsonToByteEncoder(device));

        String sHex=ByteUtil.bytesToHex(ByteUtil.intToByteArray(Float.floatToIntBits((float)1.6)));
        //返回报文
        byte[] input=new byte[]{0x00,0x01,0x00,0x00,0x00,0x23,0x01,0x03,0x20,
                0x00,0x00,0x3F,(byte)0xC0,
                (byte)0xCC,(byte)0xCD,0x3F,(byte)0xCC,
                0x00,0x00,0x3F,(byte)0xC0,
                (byte)0xCC,(byte)0xCD,0x3F,(byte)0xCC,
                0x00,0x00,0x3F,(byte)0xC0,
                (byte)0xCC,(byte)0xCD,0x3F,(byte)0xCC,
                0x00,0x00,0x3F,(byte)0xC0,
                (byte)0xCC,(byte)0xCD,0x3F,(byte)0xCC
               };

        //测试状态信息解码
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(input);
        ByteBuf data=buf.duplicate();

        assertTrue(channel.writeInbound(data));
        assertTrue(channel.finish());
        String json=(String)channel.readInbound();
        log.info(json);

    }

    @Test
    public void testBMS(){

        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("BMS");
        device.setDriver(new BmsDriver());   //测试时，避免从文件加载的类文件与代码不一致

        device.Start();
        try {
            Thread.sleep(20000);
        }catch (Exception ex){
            ;
        }
        device.Stop();
    }

    @Test
    public void testRTK(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("RTK");
        device.setDriver(new RTKDriver());   //测试时，避免从文件加载的类文件与代码不一致

        device.Start();
        try {
            Thread.sleep(20000);
        }catch (Exception ex){
            ;
        }
        device.Stop();
    }

    @Test
    public void testLift(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("Lift");
        device.setDriver(new LiftDriver());   //测试时，避免从文件加载的类文件与代码不一致

        device.Start();
        try {
            Thread.sleep(10000);
        }catch (Exception ex){
            ;
        }
        device.Stop();
    }

    @Test
    public void testMet(){
        IotConfig.getInstance().load(filepath);
        DriverManager.getInstance().load(IotConfig.getInstance().getDriverFilePath());
        DeviceManager.getInstance().load(IotConfig.getInstance());

        Device device=DeviceManager.getInstance().getDevice("Met");
        device.setDriver(new MetDriver());   //测试时，避免从文件加载的类文件与代码不一致

        device.Start();
        try {
            Thread.sleep(10000);
        }catch (Exception ex){
            ;
        }
        device.Stop();
    }
}
