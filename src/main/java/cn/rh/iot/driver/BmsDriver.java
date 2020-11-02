package cn.rh.iot.driver;

import cn.rh.iot.driver.base.ByteUtil;
import cn.rh.iot.driver.base.FrameType;
import cn.rh.iot.driver.base.IDriver;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: BMS电池管理设备驱动程序
 * @Author: Y.Y
 * @Create: 2020-09-22 22:52
 **/
public class BmsDriver implements IDriver {

    private final int ID_V_C=0x180228F4;
    private final int ID_DTC=0x140728F4;
    private final int FRAME_LENGTH=13;
    private final String  DTC_LEVEL_1="一级告警";
    private final String  DTC_LEVEL_2="二级告警";
    private final String  DTC_LEVEL_3="三级告警";
    private final String  DTC_LEVEL_4="维护报警";

    private final String[] WARN_STRING =new String[]{
            "放电温度高","放电温度低","单体高","单体低","单体差异大","SOC低","SOC高","",
            "充电电流大","放电电流大","温差大","绝缘低","总压高","总压低","充电温度高","充电温度低"
    };

    private final String[]  ERROR_STRING=new String[]{
            "SBMU硬件故障","电池故障","SBMU通讯故障","温升快","温度采集故障","电压采集故障","预充故障","簇间压差大",
            "SBCU通讯异常","EMS通讯故障","PCS通讯故障","","继电器黏连故障","","","","上位机强制控制"
    };

    @Override
    public void InjectParams(HashMap<String, Object> params) {
    }

    @Override
    public byte[] encode(String jData) {
        return null;
    }

    @Override
    public String decode(byte[] data) {

        if(data.length!=13) { return null; }

        String jsonStr;
        int id=ByteUtil.byteArrayToInt(data,1,false);
        if(id==ID_V_C){
            double  voltage=ByteUtil.bytesToUShort(data,5,false)*0.1;
            double  current=ByteUtil.bytesToShort(data,5+2,false)*0.1;
            double  SOC=ByteUtil.bytesToUShort(data,5+4,false)*0.1;
            double  SOH=ByteUtil.bytesToUShort(data,5+6,false)*0.1;

            jsonStr = "\"msgId\":" + 2 + "," + System.lineSeparator() +
                    "\"payload\":{" + System.lineSeparator() +
                    "\"voltage\": " + voltage + "," + System.lineSeparator() +
                    "\"current\": " + current +"," +System.lineSeparator() +
                    "\"SOC\": " + SOC +"," +System.lineSeparator() +
                    "\"SOH\": " + SOH + System.lineSeparator() +
                    "}";
            return jsonStr;

        }else if(id==ID_DTC) {

            long value=ByteUtil.longFrom8Bytes(data,5,false);
            if(value==0) { return null; }

            String errorCodeStr=Long.toHexString(value);
            String errorString=getErrorString(value);

            jsonStr = "\"msgId\":\"" + 2 + "\"," + System.lineSeparator() +
                    "\"payload\":\"{" + System.lineSeparator() +
                    "\"DTC\": " + "\"" + errorCodeStr + "\"," + System.lineSeparator() +
                    "\"warning\": " + "\"" + errorString + "\"" + System.lineSeparator() +
                    "}";
            return jsonStr;
        }
        return null;
    }

    /*
     * @Description: 根据告警故障码生成故障信息字符串
     * @Param: [value]
     * @Return: java.lang.String
     * @Author: Y.Y
     * @Date: 2020/9/23 10:55
     */
    private String getErrorString(long value){

        StringBuilder errorString=new StringBuilder();
        byte[] errorValues=ByteUtil.longToBytes(value);


        //一级告警
        boolean needAddTopic;
        boolean[] bitValues1;
        boolean[] bitValues2;

        needAddTopic=(errorValues[0]!=0) ||(errorValues[1]!=0);
        bitValues1=ByteUtil.byteToBit(errorValues[0]);
        bitValues2=ByteUtil.byteToBit(errorValues[1]);
        fillErrorString(DTC_LEVEL_3,needAddTopic,bitValues1,bitValues2,errorString,WARN_STRING);

        //二级告警
        needAddTopic=(errorValues[2]!=0) ||(errorValues[3]!=0);
        bitValues1=ByteUtil.byteToBit(errorValues[2]);
        bitValues2= ByteUtil.byteToBit(errorValues[3]);
        fillErrorString(DTC_LEVEL_2,needAddTopic,bitValues1,bitValues2,errorString,WARN_STRING);

        //三级告警
        needAddTopic=(errorValues[4]!=0) ||(errorValues[5]!=0);
        bitValues1=ByteUtil.byteToBit(errorValues[4]);
        bitValues2=ByteUtil.byteToBit(errorValues[5]);
        fillErrorString(DTC_LEVEL_3,needAddTopic,bitValues1,bitValues2,errorString,WARN_STRING);

        //维修告警
        needAddTopic=(errorValues[6]!=0) ||(errorValues[7]!=0);
        bitValues1=ByteUtil.byteToBit(errorValues[6]);
        bitValues2=ByteUtil.byteToBit(errorValues[7]);
        fillErrorString(DTC_LEVEL_4,needAddTopic,bitValues1,bitValues2,errorString,ERROR_STRING);


        if(errorString.length()>0 && errorString.substring(errorString.length() - 1).equals("|")){
            errorString.delete(errorString.length()-1,errorString.length()-1 );
        }

        return errorString.toString();
    }

    private void fillErrorString(String levelString,boolean needAddTopic,boolean[] bitValues1,boolean[] bitValues2,StringBuilder errorString,String[] infoList){
        if(needAddTopic){
            errorString.append(levelString).append(":");
            for(int i=0;i<8;i++){
                if(bitValues1[i]){
                    errorString.append(infoList[i]).append("|");
                }
            }
            for(int i=0;i<8;i++){
                if(bitValues2[i]){
                    errorString.append(infoList[8 + i]).append("|");
                }
            }
        }
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
        return FRAME_LENGTH;
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


}
