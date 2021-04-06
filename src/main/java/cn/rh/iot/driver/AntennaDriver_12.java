package cn.rh.iot.driver;

import cn.rh.iot.driver.base.ByteUtil;
import cn.rh.iot.driver.base.DeviceState;
import cn.rh.iot.driver.base.FrameType;
import cn.rh.iot.driver.base.IDriver;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

public class AntennaDriver_12 implements IDriver {

    private final byte MSG_HEAD=(byte)0xFA;                            //报文头
    private final int  MSG_LENGTH=6;                                   //报文长度
    private final byte DEVICE_ID=0x05;                                 //设备标识（低6位）

    private final int  INFO_INDEX=3;

    private final static int CTRL_FRAME_MSG_NUMBER=3;

    private final String MSG_ID="msgId";
    private final String PAYLOAD="payload";
    private final String MSG_TAG="msg";
    private final String SN_TAG="serialNumber";

    private final DeviceState unKnownState=new DeviceState(99,"未知状态");

    //读取数据指令
    private final HashMap<Byte, DeviceState> infoMap =new HashMap<>();

    public AntennaDriver_12() {

        infoMap.put((byte) 0x01,new DeviceState(1,"倒伏状态"));
        infoMap.put((byte) 0x02,new DeviceState(2,"倒伏转直立中"));
        infoMap.put((byte) 0x03,new DeviceState(3,"直立状态"));
        infoMap.put((byte) 0x04,new DeviceState(4,"直立转倒伏中"));

    }

    @Override
    public void InjectParams(HashMap<String, Object> params) {
    }

    @Override
    public boolean Is2Me(byte[] data) {
        if(data==null || data.length!=MSG_LENGTH || data[0]!=MSG_HEAD){
            return false;
        }
        return (data[1] & 0x3F) == (DEVICE_ID & 0xFF);
    }

    @Override
    public byte[] encode(String jData) {

        JSONObject jsonObject=JSONObject.parseObject(jData);
        Integer msgId= jsonObject.getInteger(MSG_ID);
        if(msgId==null){ return null;}
        if(msgId !=CTRL_FRAME_MSG_NUMBER){return null;}

        JSONObject payload=jsonObject.getJSONObject(PAYLOAD);
        if(payload==null){return null;}
        String msg=payload.getString(MSG_TAG);
        if(msg==null){return null;}

        byte[] data=new byte[MSG_LENGTH];
        data[0]=MSG_HEAD;
        data[1]=(byte)((DEVICE_ID | 0xC0) & 0xFF );
        data[2]=0x00;
        data[3]=(byte)0xFF;

        switch (msg.trim()){
            case "up":
                data[3]=0x02;
                break;
            case "down":
                data[3]=0x01;
                break;
        }
        if(data[3]==(byte)0xFF) {
            return null;
        }

        byte[] crc=ByteUtil.CRC16(data,1,3);
        data[4]=crc[0];
        data[5]=crc[1];

        return data;

    }

    @Override
    public String decode(byte[] data) {
        if(!Is2Me(data)){
            return null;
        }

        //CRC16校验
        byte[]  crc=ByteUtil.CRC16(data,1,3);
        if(data[4]!=crc[0] || data[5]!=crc[1]){
            return null;
        }

        DeviceState state = infoMap.get(data[INFO_INDEX]);

        if(state ==null){
            state =unKnownState;
        }

        return  "\"msgId\":" + 2 + "," + System.lineSeparator() +
                "\"payload\":{" + System.lineSeparator() +
                "\"stateCode\":" + state.getCode() + ","+System.lineSeparator()+
                "\"info\":" +"\""+ state.getInfo() +"\""+ System.lineSeparator()+
                "}";
    }

    @Override
    public byte[] getAskMessage() {
        return null;
    }

    @Override
    public FrameType getType() {
        return FrameType.FixLength;
    }

    @Override
    public int getMessageLength() {
        return MSG_LENGTH;
    }

    @Override
    public byte[] getHeader() {
        return null;
    }

    @Override
    public byte[] getTrailer() {
        return null;
    }

    @Override
    public int getLengthFieldOffset() {
        return -1;
    }

    @Override
    public int getLengthFieldLength() {
        return -1;
    }

    @Override
    public int getLengthAdjustment() {
        return -1;
    }

}
