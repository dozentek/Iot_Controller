package cn.rh.iot.driver;

/**
 * @program: IOT_Controller
 * @description: 报文的类型
 * @author: Y.Y
 * @create: 2020-09-22 12:04
 **/
public enum FrameType {
    None,       //无
    FixLength,  //固定长度
    Delimiter,  //分隔符分隔（可以是报文头、报文尾）
    LengthField //带长度字段的报文
}
