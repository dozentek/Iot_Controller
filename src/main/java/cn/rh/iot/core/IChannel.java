package cn.rh.iot.core;

/**
 * @Program: IOT_Controller
 * @Description: 通信链路接口
 * @Author: Y.Y
 * @Create: 2020-09-25 11:34
 **/
public interface IChannel {
    public boolean isConnected();
    public void Write(byte[] data);
    public void Connect();
    public void Disconnect();
}
