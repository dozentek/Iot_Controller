package cn.rh.iot.driver.base;

import java.nio.ByteBuffer;

public class ByteUtil {


    public static short bytesToShort(byte[] bytes, int offset,boolean littleEndian) {
        short value = 0;
        for (int i = 0; i < 2; i++) {
            int shift= (littleEndian ? i : (1 - i)) * 8;
            value +=(bytes[offset+i] & 0xFF) << shift;
        }
        return value;
    }

    public static int bytesToUShort(byte[] bytes, int offset, boolean littleEndian) {
        int value = 0;

        for(int i = 0; i < 2; i++) {
            int shift= (littleEndian ? i : (1 - i)) * 8;
            value +=(bytes[offset+i] & 0xFF) << shift;
        }
        return value;
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] bytes,int offset) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[offset+i] & 0xFF) << shift;
        }
        return value;
    }
    /*
     * @Description: 将前后16位调换位置后，遵循IEEE 754浮点标准
     * @Param: [bytes, offset]
     * @Return: float
     * @Author: Y.Y
     * @Date: 2020/9/29 16:13
     */
    public static float byteArrayToFloatExchange16Bit(byte[] bytes, int offset) {
        byte[] realBytes=new byte[]{bytes[offset+2],bytes[offset+3],bytes[offset],bytes[offset+1]};
        return Float.intBitsToFloat(byteArrayToInt(realBytes,0));
    }

    public static int byteArrayToInt(byte[] bytes,int offset, boolean littleEndian) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (littleEndian ? i : (3 - i)) * 8;
            value +=(bytes[offset+i] & 0xFF) << shift;
        }
        return value;
    }

    public static long longFrom8Bytes(byte[] input, int offset, boolean littleEndian) {
        long value = 0;
        // 循环读取每个字节通过移位运算完成long的8个字节拼装
        for (int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : (7 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) input[offset + count] << shift);
        }
        return value;
    }

    public static byte[] longToBytes(long x)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static boolean[] byteToBit(byte b) {

        boolean[] value=new boolean[8];
        for(int i=0;i<8;i++) {

            value[i]=((b >> i) & 0x1)==0x1;
        }
        return value;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3-1];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if((j+1)!=bytes.length) {
                hexChars[j * 3 + 2] = 0x20;   //空格符
            }
        }
        return new String(hexChars);
    }

    public static String getFixLengthHexString(String str,int length){

        if(str.length()>=length){
            return str;
        }else{
            int iCount=length-str.length();
            StringBuilder rtn = new StringBuilder();
            for(int i=0;i<iCount;i++){
                rtn.append("0");
            }
            return rtn.append(str).toString();
        }

    }

}
