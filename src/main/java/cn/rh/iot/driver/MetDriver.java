package cn.rh.iot.driver;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: 气象站驱动
 * @Author: Y.Y
 * @Create: 2020-09-23 19:21
 **/
@Slf4j
public class MetDriver implements IDriver{

    private final byte READ_HOLDING_REGISTER=0x03;                      //读保持寄存器
    private final byte WRITE_SINGLE_REGISTER=0x06;                      //写单个保持寄存器
    private final byte[] INFO_Address=new byte[]{0x00,(byte)0x00};      //读取数据地址

    private final String MSG_ID="msgId";
    private final String PAYLOAD="payload";

    private int serialNumber=0;

    private final Object lock=new Object();

    //读取数据指令(只读了16个寄存器）
    private final byte[] askMessage=new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x04,INFO_Address[0],INFO_Address[1],0x00,0x10};


    @Override
    public void InjectParams(HashMap<String, Object> params) {
    }

    @Override
    public byte[] encode(String jData) {
        return null;
    }

    @Override
    public String decode(byte[] data) {
        try {
            if (data.length !=(7+2+4*8)){ return null;}

            int iStartIndex=9;

            //转换前需要高低16bit调换位置，返回0x01,0x02,0x03,0x04，实际为0x03,0x04,0x01,0x02。
            float wind=ByteUtil.byteArrayToFloatExchange16Bit(data,iStartIndex);
            float temp=ByteUtil.byteArrayToFloatExchange16Bit(data,iStartIndex+2*4);
            float hr=ByteUtil.byteArrayToFloatExchange16Bit(data,iStartIndex+3*4);
            float rainfall=ByteUtil.byteArrayToFloatExchange16Bit(data,iStartIndex+7*4);

            String sb = "\"msgId\":" + 02 + "," + System.lineSeparator() +
                    "\"payload\":{" + System.lineSeparator() +
                    "\"temp\":" + temp + "," + System.lineSeparator() +
                    "\"hr\":" + hr + "," + System.lineSeparator() +
                    "\"wind\":" + wind + "," + System.lineSeparator() +
                    "\"rainfall\":" + rainfall + System.lineSeparator() +
                    "}";
            return sb;

        }catch (Exception ex) {
            return null;
        }
    }

    @Override
    public byte[] getAskMessage() {
        byte[] reMessage=askMessage.clone();
        synchronized(lock) {
            reMessage[1] = getSerialNumber();
        }
        return reMessage;
    }

    @Override
    public FrameType getType() {
        return FrameType.LengthField;
    }

    @Override
    public int getMessageLength() {
        return 0;
    }

    @Override
    public byte[] getHeader() {
        return new byte[0];
    }

    @Override
    public byte[] getTrailer() {
        return new byte[0];
    }

    @Override
    public int getLengthFieldOffset() {
        return 4;
    }

    @Override
    public int getLengthFieldLength() {
        return 2;
    }

    @Override
    public int getLengthAdjustment() {
        return 0;
    }

    private byte getSerialNumber(){
        serialNumber=(serialNumber+1)/255;
        return (byte)serialNumber;
    }
}
