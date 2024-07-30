package com.zqswjtu.freemall.product.service.impl;

import com.zqswjtu.freemall.product.service.Car;
import org.springframework.stereotype.Service;

@Service("mzdCar")
public class MzdCar implements Car {
    @Override
    public String run() {
        return "I'm MZD...";
    }
}
