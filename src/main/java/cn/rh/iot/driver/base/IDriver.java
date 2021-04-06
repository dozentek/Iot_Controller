package cn.rh.iot.driver.base;

import java.util.HashMap;

/**
 * @Program: IOT_Controller
 * @Description: 设备驱动程序接口
 * @Author: Y.Y
 * @Create: 2020-09-18 11:25
 **/
public interface IDriver {

    /*
     * @description: 将参数信息注入到驱动程序
     * @Param: [params 参数信息Map]
     * @Return: void
     * @Author: Y.Y
     * @Date: 2020/9/21 10:46
     */
    public void InjectParams(HashMap<String,Object> params);


    /*
     * @description: 询问这个报文是否发给自己的
     * @Param: byte[] 报文
     * @Return: boolean  是否是给我的报文
     * @Author: Y.Y
     * @Date: 2020/9/21 10:48
     */
    public boolean Is2Me(byte[] data);

    /*
     * @description: 编码函数
     * @Param: [jData json字符串]
     * @Return: byte[]  编码后的字节数组
     * @Author: Y.Y
     * @Date: 2020/9/21 10:48
     */
    public byte[] encode(String jData);

    /*
     * @description: 解码函数
     * @Param: [data 待解码的字节数组]
     * @Return: String  json字符串
     * @Author: Y.Y
     * @Date: 2020/9/21 10:50
     */
    public String decode(byte[] data);

    /*
     * @description: 获取询问报文（用来定时向设备请求数据）
     * @Param: []
     * @Return: byte[]  报文字节数组
     * @Author: Y.Y
     * @Date: 2020/9/21 10:50
     */
    public byte[] getAskMessage();
    

    public FrameType getType();

    /*
     * @description: 获取报文长度（仅对固定长度报文）
     * @Param: []
     * @Return: int
     * @Author: Y.Y
     * @Date: 2020/9/22 9:07
     */
    public int getMessageLength();

    /*
     * @description: 获取报文头
     * @Param: []
     * @Return: byte[]
     * @Author: Y.Y
     * @Date: 2020/9/22 9:11
     */
    public byte[] getHeader();

    /*
     * @description: 获取报文尾
     * @Param: []
     * @Return: byte[]
     * @Author: Y.Y
     * @Date: 2020/9/22 9:11
     */
    public byte[] getTrailer();

    public int getLengthFieldOffset();

    public int getLengthFieldLength();

    public int getLengthAdjustment();

}
