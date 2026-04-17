package com.sky.service.impl;

import com.sky.dto.ShopInfoDTO;
import com.sky.service.ShopService;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl implements ShopService {
    @Override
    public ShopInfoDTO getMerchantInfo() {
        ShopInfoDTO shopInfoDTO = new ShopInfoDTO();
        shopInfoDTO.setPhone("13618769753");
        return shopInfoDTO;
    }
}
