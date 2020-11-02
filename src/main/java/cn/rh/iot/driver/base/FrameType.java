package cn.rh.iot.driver.base;

/**
 * @Program: IOT_Controller
 * @Description: 报文的类型
 * @Author: Y.Y
 * @Create: 2020-09-22 12:04
 **/
public enum FrameType {
    None,
    FixLength,  //固定长度
    Delimiter,  //分隔符分隔（可以是报文头、报文尾）
    LengthField //带长度字段的报文
}
