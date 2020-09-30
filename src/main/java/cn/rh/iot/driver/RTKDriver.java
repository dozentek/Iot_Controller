package cn.rh.iot.driver;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: RTK驱动
 * @Author: Y.Y
 * @Create: 2020-09-23 18:33
 **/
public class RTKDriver implements IDriver {

    private final byte[] Delimiter=new byte[]{0x0D,0x0A};  //回车+换行符 \r\n

    @Override
    public void InjectParams(HashMap<String, Object> params) {
    }

    @Override
    public byte[] encode(String jData) {
        return null;
    }

    @Override
    public String decode(byte[] data) {

        try{
            if(data.length<5){return null;}
            String s=new String(data, StandardCharsets.US_ASCII);
            String[] valueList=s.split(",");
            if(!valueList[0].toUpperCase().equals("$GPGGA")){
                return null;
            }

            double lat;
            double lon;
            double alt;
            int quality;

            String sV=valueList[2];
            lat=Integer.parseInt(sV.substring(0,1))+Double.parseDouble(sV.substring(2))/60;
            if(valueList[3].equals("S")){
                lat=-lat;
            }

            sV=valueList[4];
            lon=Integer.parseInt(sV.substring(0,2))+Double.parseDouble(sV.substring(3))/60;
            if(valueList[5].equals("W")){
                lon=-lon;
            }
            quality=Integer.parseInt(valueList[6]);
            alt=Double.parseDouble(valueList[9]);

            return  "\"msgId\":" + 2 + "," + System.lineSeparator() +
                    "\"payload\":{" + System.lineSeparator() +
                    "\"lon\":" + lon + "," + System.lineSeparator() +
                    "\"lat\":" + lat + "," + System.lineSeparator() +
                    "\"alt\":" + alt + "," + System.lineSeparator() +
                    "\"qos\":" + quality + System.lineSeparator() +
                    "}";

        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public byte[] getAskMessage() {
        return null;
    }

    @Override
    public FrameType getType() {
        return FrameType.Delimiter;
    }

    @Override
    public int getMessageLength() {
        return 0;
    }

    @Override
    public byte[] getHeader() {
        return null;
    }

    @Override
    public byte[] getTrailer() {
        return Delimiter;
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
