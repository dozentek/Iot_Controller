package cn.rh.iot.driver;

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
    private final byte[] INFO_Address=new byte[]{(byte)0xD6,0x00};      //读取数据地址
    private final byte[] COMMAND_Address=new byte[]{(byte)0xD5,0x01};   //发送指令地址

    private final int INFO_FRAME_LENGTH=11;                             //信息报文长度
    private final static int CTRL_FRAME_MSG_NUMBER=03;

    private final String MSG_ID="msgId";
    private final String PAYLOAD="payload";
    private final String MSG_TAG="msg";
    private final String SN_TAG="serialNumber";

    private int serialNumber=0;

    private final Object lock=new Object();

    //读取数据指令
    private final byte[] askMessage=new byte[]{0x00,0x00,0x00,0x00,0x00,0x06,0x01,0x04,INFO_Address[0],INFO_Address[1],0x00,0x01};
    private final byte[] ctrlMessage=new byte[]{0x00,0x00,0x00,0x06,0x01,0x06,COMMAND_Address[0],COMMAND_Address[1]};
    private final HashMap<Integer,String>  infoMap=new HashMap<>();
    public LiftDriver() {

        infoMap.put(0x0800,"上电状态");
        infoMap.put(0x8180,"天窗开启中");
        infoMap.put(0x8280,"天窗已开启/平台上升到位");
        infoMap.put(0x8480,"天窗关闭中");
        infoMap.put(0x8880,"天窗已关闭/平台准备就绪");
        infoMap.put(0x1202,"平台上升中");
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

        byte[] data=new byte[ctrlMessage.length+4];
        data[0]=0x00;
        data[1]=sn;

        int le=ctrlMessage.length;
        System.arraycopy(ctrlMessage, 0, data, 2, le);

        if(msg.toUpperCase().equals("UP")){
            data[2+le]=0x04;
            data[2+le+1]=0x00;
            return data;
        }
        if(msg.toUpperCase().equals("DOWN")){
            data[2+le]=0x08;
            data[2+le+1]=0x00;
            return data;
        }
        return null;
    }

    @Override
    public String decode(byte[] data) {
        if(data.length<INFO_FRAME_LENGTH){
            return null;
        }
        if(data[7]==READ_HOLDING_REGISTER){
//            int returnSn=ByteUtil.bytesToUShort(data,0,false);
//            if(returnSn!=serialNumber){
//                //返回的数据报文不是当前询问的应答报文，丢弃
//                return null;
//            }
            byte[] byteState=new byte[4];
            byteState[0]=0x00;
            byteState[1]=0x00;
            byteState[2]=data[9];
            byteState[3]=data[10];

            int stateNumber=ByteUtil.byteArrayToInt(byteState,0,false);
            String info=infoMap.get(stateNumber);
            if(info==null){
                info="未知状态";
            }

            String sb = "\"msgId\":" + 02 + "," + System.lineSeparator() +
                    "\"payload\":{" + System.lineSeparator() +
                    "\"stateNumber\":" + Integer.toHexString(stateNumber) + "," + System.lineSeparator() +
                    "\"info\":" + info + System.lineSeparator() +
                    "}";
            return sb;

        }else if(data[7]==WRITE_SINGLE_REGISTER){
            String sb = "\"msgId\":" + 04 + "," + System.lineSeparator() +
                    "\"payload\":{" + System.lineSeparator() +
                    "\"serialNumber\":" + serialNumber +
                    "}";
            return sb;
        }else{
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
        return -1;
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
