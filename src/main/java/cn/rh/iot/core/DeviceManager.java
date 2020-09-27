package cn.rh.iot.core;

import cn.rh.iot.driver.DriverManager;
import cn.rh.iot.driver.IDriver;
import cn.rh.iot.mqtt.TopicParam;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @Program: IOT_Controller
 * @Description: 设备对象管理器
 * @Author: Y.Y
 * @Create: 2020-09-21 20:27
 **/
@Slf4j
public class DeviceManager {

    private HashMap<String ,Device> devices=new HashMap<>();

    @Getter
    private boolean isLoaded=false;
    private String driverFileDir;
    private DriverManager driverManager;
    private final EventLoopGroup group = new NioEventLoopGroup();


    /*
     * @Description: 根据配置信息，加载所有需要通信的设备对象
     * @Param: [configFilePathname]  配置文件
     * @Return: boolean 是否加载成功
     * @Author: Y.Y
     * @Date: 2020/9/24 10:25
     */
    public boolean Load(String configFilePathname){
        if(isLoaded){
            return true;
        }
        File file=new File(configFilePathname);

        if(!file.exists()){
            log.error("配置文件["+configFilePathname+"]丢失");
            return false;
        }else{
            isLoaded=CreateDevices(configFilePathname);
        }
        return  isLoaded;
    }

    /*
     * @Description: 获取已经加载设备对象的数量
     * @Param: []
     * @Return: int
     * @Author: Y.Y
     * @Date: 2020/9/24 10:28
     */
    public int DeviceCount(){
        return devices.size();
    }

    /*
     * @Description: 获取所有Device对象的key（也就是设备名称）的列表
     * @Param: []
     * @Return: java.util.ArrayList<java.lang.String>
     * @Author: Y.Y
     * @Date: 2020/9/24 10:28
     */
    public ArrayList<String> getKeyList(){

        ArrayList<String> res=new ArrayList<>();

        Iterator it=devices.keySet().iterator();

        while(it.hasNext()){
            String key = (String)it.next();
            res.add(key);
        }
        return res;
    }

    /*
     * @Description: 获取一个设备对象
     * @Param: [key] 设备对象的名称
     * @Return: cn.rh.iot.core.Device
     * @Author: Y.Y
     * @Date: 2020/9/24 10:29
     */
    public Device getDevice(String key){
        return devices.get(key);
    }

    /*
     * @Description: 加载配置文件，创建Device对象并加载Device驱动
     * @Param: [configFile]
     * @Return: boolean
     * @Author: Y.Y
     * @Date: 2020/9/24 11:22
     */
    private boolean CreateDevices(String configFile){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(configFile);

            NodeList nodes=document.getDocumentElement().getElementsByTagName("DriverFilePath");
            if(nodes.getLength()>0){
                driverFileDir=((Element)(nodes.item(0))).getTextContent().trim();
                driverManager=new DriverManager(driverFileDir);
            }else{
                log.error("配置项DriverFilePath未找到");
                return false;
            }

            NodeList devices=document.getDocumentElement().getElementsByTagName("Device");
            for(int i=0;i<devices.getLength();i++){
                Element ele=(Element)devices.item(i);
                CreateDevice(ele);
            }
        }catch (Exception ex){
            log.error(ex.toString());
            devices.clear();
            return false;
        }
        return true;
    }

    /*
     * @Description: 解析XmlElement配置，创建设备对象，并自动加入到管理器中
     * @Param: [ele] 设备对象对应的xml配置文件中的配置项
     * @Return: void
     * @Author: Y.Y
     * @Date: 2020/9/24 10:30
     */
    private void CreateDevice(Element ele) throws Exception{
        String name=ele.getAttribute("name").trim();
        if(name.isEmpty()){
            String errStr="Device标签缺少name属性";
            log.error(errStr);
            throw new Exception(errStr);
        }

        NetDevice device=new NetDevice(group);
        device.setName(name);
        device.setIntroduction(ele.getAttribute("intro").trim());

        if(ele.getAttribute("role").trim().toUpperCase()=="CLIENT") {
            device.setRole(NetRoleType.CLIENT);
        }else{
            device.setRole(NetRoleType.SERVER);
        }

        if(ele.getAttribute("protocol").trim().toUpperCase()=="UDP") {
            device.setProtocol(ProtocolType.UDP);
        }else{
            device.setProtocol(ProtocolType.TCP);
        }

        if(ele.getAttribute("askInterval").trim().isEmpty()){
            device.setAskInterval(-1);  //askInterval<=0，表示不需要定时询问
        }else{
            int interval=Integer.parseInt(ele.getAttribute("askInterval").trim());
            device.setAskInterval(interval);
        }

        if(ele.getAttribute("timeout").trim().isEmpty()){
            device.setTimeout(60000);
        }else{
            int timeout=Integer.parseInt(ele.getAttribute("timeout").trim());
            device.setTimeout(timeout);
        }

        NodeList nodes=ele.getElementsByTagName("Net");
        if(nodes.getLength()==0){
            String errStr="Device标签缺少Net节点";
            log.error(errStr);
            throw new Exception(errStr);
        }else{

            String ip =((Element)nodes.item(0)).getElementsByTagName("IP").item(0).getTextContent().trim();
            int  port=Integer.parseInt(((Element)nodes.item(0)).getElementsByTagName("Port").item(0).getTextContent().trim());
            InetSocketAddress address=new InetSocketAddress(ip,port);
            device.setAddress(address);
        }

        nodes=ele.getElementsByTagName("Driver");
        if(nodes.getLength()==0){
            String errStr="Device标签缺少Driver节点";
            log.error(errStr);
            throw new Exception(errStr);
        }else{
            IDriver driver=driverManager.getNewDriverObject(nodes.item(0).getTextContent().trim());
            if(driver==null){
                String errStr="驱动:"+nodes.item(0).getTextContent().trim()+"不存在";
                log.error(errStr);
                throw new Exception(errStr);
            }else{
                device.setDriver(driver);

                NodeList paramNode=ele.getElementsByTagName("Params");
                if(paramNode.getLength()>0){
                    NodeList params=paramNode.item(0).getChildNodes();
                    if(params.getLength()>0){
                        HashMap<String,Object> ps=new HashMap<>();
                        for(int i=0;i<params.getLength();i++){
                            ps.put(((Element)params.item(i)).getAttribute("name"),
                                    ((Element)params.item(i)).getAttribute("value"));
                        }
                        driver.InjectParams(ps);
                    }
                }
            }
        }

        nodes=ele.getElementsByTagName("Topic");
        if(nodes.getLength()==0){
            log.error("Device标签缺少Topic节点");
            throw new Exception("Device标签缺少Topic节点");
        }else{
            NodeList topics=((Element)nodes.item(0)).getElementsByTagName("PubTopic");
            if(topics.getLength()>0){
                device.setPublishTopicParam(new TopicParam(topics.item(0).getTextContent().trim(),0));
            }else{
                device.setPublishTopicParam(null);
            }

            topics=((Element)nodes.item(0)).getElementsByTagName("SubTopic");
            if(topics.getLength()>0){
                device.setSubscribeTopicParam(new TopicParam(topics.item(0).getTextContent().trim(),0));
            }else{
                device.setSubscribeTopicParam(null);
            }
        }

        //为Device装配通信链路
        Assembler.AssembleMqttChannel(device);
        Assembler.AssembleNetChannel(device);

        if(!devices.containsKey(device.name)){
            devices.put(device.name,device);
        }
    }
}
