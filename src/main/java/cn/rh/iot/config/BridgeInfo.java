package cn.rh.iot.config;

import cn.rh.iot.core.NetProtocolType;
import cn.rh.iot.core.NetRoleType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BridgeInfo {

    private final static int DEFAULT_TIMEOUT=6000;

    @Getter @Setter
    private String name;
    @Getter @Setter
    private String info;
    @Getter @Setter
    private NetRoleType role;
    @Getter @Setter
    private NetProtocolType protocol;

    @Getter @Setter
    private int timeout;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    private int port;

    private final ArrayList<DeviceInfo> deviceInfoList=new ArrayList<>();

    public int DeviceCount(){

        return deviceInfoList.size();
    }

    public List<DeviceInfo> getDeviceInfoList() {
        return deviceInfoList;
    }

    public boolean Load(Element ele){
        if(ele==null){
            return false;
        }

        //解析Net信息
        {
            try {
                NodeList nodes = ele.getElementsByTagName("Channel");
                if(nodes.getLength()<=0){
                    log.error("缺少配置项: Bridge/Channel");
                    return false;
                }
                Element deviceEle=((Element)nodes.item(0));

                if(!deviceEle.hasAttribute("IP"))
                {
                    log.error("缺少配置项: Channel/IP");
                    return false;
                }
                if(!deviceEle.hasAttribute("Port"))
                {
                    log.error("缺少配置项: Channel/Port");
                    return false;
                }

                ip=deviceEle.getAttribute("IP").trim();
                port=Integer.parseInt(deviceEle.getAttribute("Port").trim());

            }catch (Exception ex){
                log.error("解析错误："+ex.getMessage());
                return false;
            }
        }

        {
            String value = ele.getAttribute("name");
            name=value.trim();
            if(name.equals("")){
                log.error("配置项: Bridge/name不能为空");
                return false;
            }
        }

        {
            String value = ele.getAttribute("intro");
            info=value.trim();
        }

        {
            String value = ele.getAttribute("role");
            if(value.trim().toUpperCase().equals("CLIENT")){
                role=NetRoleType.CLIENT;
            }else{
                role=NetRoleType.SERVER;
            }
        }

        {
            String value = ele.getAttribute("protocol");
            if(value.trim().toUpperCase().equals("UDP")){
                protocol=NetProtocolType.UDP;
            }else{
                protocol=NetProtocolType.TCP;
            }
        }

        {
            String value = ele.getAttribute("timeout");
            if(value.trim().equals("")){
                timeout=DEFAULT_TIMEOUT;
            }else{
                try{
                    timeout=Integer.parseInt(value.trim());
                }catch (Exception ex){
                    log.error("配置项: Bridge/askInterval值非法");
                    return false;
                }
            }
        }

        //解析设备配置信息
        {
            NodeList nodes = ele.getElementsByTagName("Devices");
            if(nodes.getLength()<=0){
                log.error("缺少配置项: Bridge/Devices");
                return false;
            }

            nodes=((Element)nodes.item(0)).getElementsByTagName("Device");

            for(int i=0;i<nodes.getLength();i++){
                DeviceInfo deviceInfo=new DeviceInfo();
                boolean res=deviceInfo.Load((Element)nodes.item(i));
                if(!res){
                    deviceInfoList.clear();
                    return false;
                }
                deviceInfoList.add(deviceInfo);
            }
        }
        return true;
    }
}
