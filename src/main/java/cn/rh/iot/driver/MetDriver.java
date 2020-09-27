package cn.rh.iot.driver;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: 气象站驱动
 * @Author: Y.Y
 * @Create: 2020-09-23 19:21
 **/
public class MetDriver implements IDriver{

    private final byte READ_HOLDING_REGISTER=0x03;                      //读保持寄存器
    private final byte WRITE_SINGLE_REGISTER=0x06;                      //写单个保持寄存器
    private final byte[] INFO_Address=new byte[]{0x00,(byte)0x00};      //读取数据地址

    private final String MSG_ID="msgId";
    private final String PAYLOAD="payload";

    private int serialNumber=0;

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
            if (data.length !=(7+2+16*2)){ return null;}

            int iStartIndex=9;
            float temp=ByteUtil.byteArrayToFloat(data,iStartIndex+2*4);
            float hr=ByteUtil.byteArrayToFloat(data,iStartIndex+3*4);
            float wind=ByteUtil.byteArrayToFloat(data,iStartIndex);
            float rainfall=ByteUtil.byteArrayToFloat(data,iStartIndex+10*4);

            StringBuilder sb=new StringBuilder();
            sb.append("\"msgId\":").append(02).append(",").append(System.lineSeparator());
            sb.append("\"payload\":{").append(System.lineSeparator());
            sb.append("\"temp\":").append(temp+",").append(System.lineSeparator());
            sb.append("\"hr\":").append(hr+",").append(System.lineSeparator());
            sb.append("\"wind\":").append(wind+",").append(System.lineSeparator());
            sb.append("\"rainfall\":").append(rainfall+",").append(System.lineSeparator());
            sb.append("}");
            return sb.toString();

        }catch (Exception ex) {
            return null;
        }
    }

    @Override
    public byte[] getAskMessage() {
        askMessage[1]=getSerialNumber();
        return askMessage;
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
        return 0;
    }

    @Override
    public int getLengthFieldLength() {
        return 0;
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
