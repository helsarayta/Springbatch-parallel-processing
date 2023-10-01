package com.heydieproject.springbatchpartitioning.config;

import com.heydieproject.springbatchpartitioning.entity.CarPark;
import org.springframework.batch.item.ItemProcessor;

public class CarParkProcessor implements ItemProcessor<CarPark,CarPark> {
    @Override
    public CarPark process(CarPark carPark) throws Exception {
        return carPark;
    }
}
