package cn.rh.iot.driver;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: RTK驱动
 * @Author: Y.Y
 * @Create: 2020-09-23 18:33
 **/
public class RTKDriver implements IDriver {



    private  byte[] Delimiter=new byte[]{0x24};  //$

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

            String s=new String(data,"ascii");
            String[] valueList=s.split(",");

            if(valueList[0].toUpperCase()!="GPGGA" && valueList[0].toUpperCase()!="$GPGGA"){
                return null;
            }

            double lat;
            double lon;
            double alt;
            int quality;

            String sV=valueList[2];
            lat=Integer.parseInt(sV.substring(0,1))+Double.parseDouble(sV.substring(2))/60;
            if(valueList[3]=="S"){
                lat=-lat;
            }

            sV=valueList[4];
            lon=Integer.parseInt(sV.substring(0,2))+Double.parseDouble(sV.substring(3))/60;
            if(valueList[5]=="W"){
                lon=-lon;
            }
            quality=Integer.parseInt(valueList[6]);
            alt=Double.parseDouble(valueList[9]);

            StringBuilder sb=new StringBuilder();
            sb.append("\"msgId\":").append(02).append(",").append(System.lineSeparator());
            sb.append("\"payload\":{").append(System.lineSeparator());
            sb.append("\"lon\":").append(lon).append(","+System.lineSeparator());
            sb.append("\"lat\":").append(lat).append(","+System.lineSeparator());
            sb.append("\"alt\":").append(alt).append(","+System.lineSeparator());
            sb.append("\"qos\":").append(quality).append(System.lineSeparator());
            sb.append("}");
            return sb.toString();

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
