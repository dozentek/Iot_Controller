package cn.rh.iot.driver;

import cn.rh.iot.driver.base.ByteUtil;
import cn.rh.iot.driver.base.FrameType;
import cn.rh.iot.driver.base.IDriver;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: 升降台驱动程序
 * @Author: Y.Y
 * @Create: 2020-09-23 13:58
 **/
public class LiftDriver implements IDriver {

    private final byte READ_HOLDING_REGISTER=0x03;                      //读保持寄存器
    private final byte WRITE_SINGLE_REGISTER=0x06;                      //写单个保持寄存器
    private final byte[] INFO_Address=new byte[]{0x02,0x57};            //读取寄存器地址,D600（假设寄存器编号从1开始）
    private final byte[] COMMAND_Address=new byte[]{0x01,(byte)0xF4};   //写入寄存器地址,D501（假设寄存器编号从1开始）

    private final int INFO_FRAME_LENGTH=2;                             //信息报文长度
    private final static int CTRL_FRAME_MSG_NUMBER=3;

    private final String MSG_ID="msgId";
    private final String PAYLOAD="payload";
    private final String MSG_TAG="msg";
    private final String SN_TAG="serialNumber";

    private int serialNumber=0;

    private final Object lock=new Object();

    //读取数据指令
    private final HashMap<Integer,String>  infoMap=new HashMap<>();
    public LiftDriver() {

        infoMap.put(0x0800,"上电状态");
        infoMap.put(0x8180,"天窗开启中");
        infoMap.put(0x8280,"天窗已开启/平台下降到位");
        infoMap.put(0x8480,"天窗关闭中");
        infoMap.put(0x8880,"天窗已关闭/平台准备就绪");
        infoMap.put(0x1202,"平台上升中");
        infoMap.put(0x2280,"平台上升到位");
        infoMap.put(0x4202,"平台下降中");
        infoMap.put(0x0001,"急停被按下");
        infoMap.put(0x0200,"急停被释放");
    }

    @Override
    public void InjectParams(HashMap<String, Object> params) {
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

        byte sn=payload.getByte(SN_TAG);

        byte[] data=new byte[2];

        if(msg.toUpperCase().equals("UP")){
            //使用的是小端模式
            data[0]=0x00;
            data[1]=0x04;
            return data;
        }
        if(msg.toUpperCase().equals("DOWN")){
            //使用的是小端模式
            data[0]=0x00;
            data[1]=0x08;
            return data;
        }
        return null;
    }

    @Override
    public String decode(byte[] data) {
        if(data.length<INFO_FRAME_LENGTH){
            return null;
        }
        String sb;
        byte[] byteState=new byte[4];
        byteState[0]=0x00;
        byteState[1]=0x00;
        byteState[2]=data[0];
        byteState[3]=data[1];

        int stateNumber= ByteUtil.byteArrayToInt(byteState,0,false);
        String info=infoMap.get(stateNumber);
        if(info==null){
            info="未知状态";
        }
        sb = "\"msgId\":" + 2 + "," + System.lineSeparator() +
                "\"payload\":{" + System.lineSeparator() +
                "\"stateNumber\":" + Integer.toHexString(stateNumber) + "," + System.lineSeparator() +
                "\"info\":" + info + System.lineSeparator() +
                "}";
        return sb;
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
        return 2;
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
