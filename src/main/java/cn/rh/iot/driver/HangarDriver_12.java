package cn.rh.iot.driver;

import cn.rh.iot.driver.base.ByteUtil;
import cn.rh.iot.driver.base.DeviceState;
import cn.rh.iot.driver.base.FrameType;
import cn.rh.iot.driver.base.IDriver;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

public class HangarDriver_12 implements IDriver {

    private final byte MSG_HEAD=(byte)0xFA;                            //报文头
    private final int  MSG_LENGTH=6;                                   //报文长度
    private byte DEVICE_ID=0x03;                                       //设备标识（低6位）

    private final int  HANGAR_INFO_INDEX=2;
    private final int  SEAT_INFO_INDEX=3;

    private final static int CTRL_FRAME_MSG_NUMBER=3;

    private final String MSG_ID="msgId";
    private final String PAYLOAD="payload";
    private final String MSG_TAG="msg";
    private final String SN_TAG="serialNumber";
    private final String SEAT_NO="seatNumber";

    private final DeviceState unKnownState=new DeviceState(99,"未知状态");

    //读取数据指令
    private final HashMap<Byte, DeviceState>  infoMapHangar=new HashMap<>();    //机库
    private final HashMap<Byte, DeviceState>  infoMapSeat =new HashMap<>();     //机位
    public HangarDriver_12() {

        infoMapHangar.put((byte) 0x01,new DeviceState(1,"初始化"));
        infoMapHangar.put((byte) 0x02,new DeviceState(2,"机库下降到位"));
        infoMapHangar.put((byte) 0x03,new DeviceState(3,"机库上升中"));
        infoMapHangar.put((byte) 0x04,new DeviceState(4,"机库上升到位"));
        infoMapHangar.put((byte) 0x05,new DeviceState(5,"机库下降中"));

        infoMapSeat.put((byte) 0x01,new DeviceState(1,"机位复位/入库完成"));
        infoMapSeat.put((byte) 0x02,new DeviceState(2,"机位出库中"));
        infoMapSeat.put((byte) 0x03,new DeviceState(3,"机位出库完成"));
        infoMapSeat.put((byte) 0x04,new DeviceState(4,"机位入库中"));

    }

    @Override
    public void InjectParams(HashMap<String, Object> tags) {
        //根据输入的SN(设备编号)设置DEVICE_ID
        if(tags!=null && tags.size()>0){
            Object value=tags.values().iterator().next();
            try {
                if (value != null) {
                    DEVICE_ID = (byte) Integer.parseInt(value.toString().trim());
                }
            }catch (NumberFormatException ex){
                ;
            }
        }
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
        String seatNumber =payload.getString(SEAT_NO);

        if(msg==null){return null;}

        byte[] data=new byte[MSG_LENGTH];

        data[0]=MSG_HEAD;
        data[1]=(byte)((DEVICE_ID | 0xC0) & 0xFF );
        data[2]=0x00;
        data[3]=(byte)0xFF;

        boolean isA = seatNumber.trim().toUpperCase().equals("A");

        switch (msg.trim()){
            case "up":
                data[3]=0x01;
                break;
            case "down":
                data[3]=0x02;
                break;
            case "seat_reset":
                if(seatNumber.equals("")){
                    return null;
                }else {
                    if(isA) {
                        data[3] = 0x03;
                    }else{
                        data[3] = 0x05;
                    }
                }
                break;
            case "seat_out":
                if(seatNumber.equals("")){
                    return null;
                }else {
                    if(isA) {
                        data[3] = 0x04;
                    }else{
                        data[3] = 0x06;
                    }
                }
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

        DeviceState h_state=infoMapHangar.get(data[HANGAR_INFO_INDEX]);
        DeviceState seat_A_state= infoMapSeat.get((byte)((data[SEAT_INFO_INDEX] & 0xF0)>>4));
        DeviceState seat_B_state= infoMapSeat.get((byte)(data[SEAT_INFO_INDEX] & 0x0F));

        if(h_state==null){
            h_state=unKnownState;
        }
        if(seat_A_state==null){
            seat_A_state=unKnownState;
        }
        if(seat_B_state==null){
            seat_B_state=unKnownState;
        }

        return "\"msgId\":" + 2 + "," + System.lineSeparator() +
               "\"payload\":{" + System.lineSeparator() +
                "\"hangar_stateCode\":" + h_state.getCode() + ","+System.lineSeparator()+
                "\"seat_A_stateCode\":" + seat_A_state.getCode() + ","+System.lineSeparator()+
                "\"seat_B_stateCode\":" + seat_B_state.getCode() + ","+ System.lineSeparator()+
               "\"hangar_info\":" +"\""+ h_state.getInfo() +"\""+ ","+System.lineSeparator()+
               "\"seat_A_info\":" +"\""+ seat_A_state.getInfo() +"\""+ ","+System.lineSeparator()+
               "\"seat_B_info\":" +"\""+ seat_B_state.getInfo() +"\""+ System.lineSeparator()+
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
