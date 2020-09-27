## 一、主题（Topic）命名约定

### 1、格式

> ######     1、 设备名称/报文类型
>
> #####     2、UAV/无人机ID/报文类型



### 2、各设备、无人机的主题（Topic）

- ##### 升降台PLC

  > 控制报文：Lift/Ctrl
  >
  > 数据报文：Lift/Info

- ##### BMS

  > 控制报文：BMS/Ctrl
  >
  > 数据报文：BMS/Info

- ##### RTK

  > 控制报文：RTK/Ctrl
  >
  > 数据报文：RTK/Info

- ##### 气象站

  > 控制报文：Met/Ctrl
  >
  > 数据报文：Met/Info

- ##### 无人机（APP）

  > 控制报文：UAV/01/Ctrl
  >
  > 数据报文：UAV/01/Info



## 二、数据报文内容格式

### 1、格式约定

```json
{
    "deviceName": "XXX",		//设备名称
    "deviceId": "6F9619FF-8B86-D011-B42D-00C04FC964FF",	//设备编号（仅针对UAV有用）
    "msgId": 01,     			//报文编号 其中：通信链路通断报文编号=01
    "payload":{               //报文内容
    	key1:value1,    	//键值对
    	key2:value2,
        ...
    	keyN:valueN
	}
}
```



### 2、具体报文内容：

### （1）通信链路通断报文

```json
{
    "deviceName": "XXX"	,
    "deviceNumber":"6F9619FF-8B86-D011-B42D-00C04FC964FF",
    "msgId":01,
    "payload":{               
    	"connectstate":"ok"   //ok:连通，no:断开
	}
}
```



### （2）BMS信息报文

- 信息报文

```json
{
    "deviceName": "BMS"	,
    "deviceNumber":"",
    "msgId":02,				  //信息报文
    "payload":{               
    	"voltage":24.5,		  //电压。单位：V
        "current":10.2,       //电流。单位：A
        "DTC": "0xFFFFFFFF",  //64位，16进制
    	"warning":"放电温度高|总压低"   //报警信息
	}
}
```

### （3）升降台报文

- 信息报文

```json
{
    "deviceName": "Lift",	
    "deviceNumber":"",
    "msgId":02,				    //信息报文
    "payload":{               
    	"stateNumber":"0x0800",	//状态编号,16进制
    	"info":"上电状态"
	}
}
```

- 控制报文

```json
{
    "deviceName": "Lift",	
    "deviceNumber":"",
    "msgId":03,					//控制报文
    "payload":{  
    	"serialNumber": 0x01,   //指令顺序号（1-255,循环）
        "msg":"up"	        	//指令   "up":上升触发, "down":下降触发;
	}
}
```

- 控制反馈报文

```json
{
    "deviceName": "Lift",
    "deviceNumber":"",
    "msgId":04,				    //控制反馈报文
    "payload":{               
    	"serialNumber": 0x01
	}
}
```



### （4）RTK

- 信息报文

```json
{
    "deviceName": "RTK",	
    "deviceNumber":"",
    "msgId":02,				    //信息报文
    "payload":{               
    	"lon": 11402.3291611,	//经度  东经是正,西经是负
    	"lat": 5107.0017737,    //纬度  南纬是负,北纬是正
    	"alt": 48.47,			//海拔  天线离海平面的高度
    	"qos": 9				//定位质量指示  (0~9)
	}
}
```

### （5）气象站

- 信息报文

```json
{
    "deviceName": "Met"	,
    "deviceNumber":"",
    "msgId":02,				    //信息报文
    "payload":{               
    	"temp": 32.5,			//温度 双精度, ℃
    	"hr": 32,     			//湿度 双精度, %RH
    	"wind": 2.1,			//风速 双精度, m/s
    	"rainfall":0.05			//降雨量 双精度, mm/min
	}
}
```

### （6）无人机

- 信息报文

```json
{
    "deviceName": "UAV"	,
    "deviceNumber":"6F9619FF-8B86-D011-B42D-00C04FC964FF",
    "msgId":02,				    //信息报文
    "payload":{               
    	"speed": 5.1,			//空间速度 双精度, m/s
    	"state":"就绪",  	       //状态  字符串
    	"power": 32,			//电量   双精度, %
    	"lon": 11402.3291611,	//经度  东经是正,西经是负
    	"lat": 5107.0017737,    //纬度  南纬是负,北纬是正
    	"height": 48.47,		//高度  m
        "vx": "",               //x轴速度  单位m/s
		"vy": "",
		"vz": "",
        "gtbh": "#010",         //杆塔编号字符串
		"progress": "10/50"     //进度字符串 当前进度/目标总进度
	}
}
```

- 控制报文

```json
{
    "deviceName": "UAV",
    "deviceNumber":"6F9619FF-8B86-D011-B42D-00C04FC964FF",
    "msgId":03,				    //控制报文
    "payload":{               
    	"serialNumber": 0x01	
        ...
	}
}
```

- 控制反馈报文

```json
{
    "deviceName": "UAV",
    "deviceNumber":"6F9619FF-8B86-D011-B42D-00C04FC964FF",
    "msgId":04,				    //控制反馈报文
    "payload":{               
    	"serialNumber": 0x01
	}
}
```



