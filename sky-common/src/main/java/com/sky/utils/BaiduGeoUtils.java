package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.BaiduGeoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
// TODO 待开发
public class BaiduGeoUtils {
    private String URL = "https://api.map.baidu.com/geocoding/v3";
    private String AK;
    private String shopAddress;
    @Autowired
    private BaiduGeoProperties baiduGeoProperties;

    public void checkOutOfRange(String address){
        AK= baiduGeoProperties.getAk();
        shopAddress= baiduGeoProperties.getAddress();

        Map map=new HashMap();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", AK);

        String shopCoordinate = HttpClientUtil.doGet(URL, map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        String shopLngLat=lat+","+lng;
        map.put("address",address);

        String userCoordinate = HttpClientUtil.doGet(URL, map);
        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        location=jsonObject.getJSONObject("result").getJSONObject("location");
        lat=location.getString("lat");
        lng=location.getString("lng");

        String userLngLat=lat+","+lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject= jsonObject.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        log.info("商家与用户的距离{}",distance);

        if(distance>5000){
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
