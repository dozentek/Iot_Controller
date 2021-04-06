package cn.rh.iot.core;


public interface IChannel {

    boolean isConnected();

    void Write(byte[] data);

    void Connect();

    void Disconnect();
}
