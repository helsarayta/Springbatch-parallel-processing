package com.heydieproject.springbatchpartitioning.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heydieproject.springbatchpartitioning.entity.CarPark;
import com.heydieproject.springbatchpartitioning.repository.CarParkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CarParkWriter implements ItemWriter<CarPark> {
    @Autowired
    private CarParkRepository carParkRepository;
    @Autowired
    private WebClient.Builder webClient;

    @Value("${url.base.convert}")
    String urlBaseConvert;

    @Override
    public void write(List<? extends CarPark> list) throws Exception {
        log.info("Thread Run => {}", Thread.currentThread().getName());

        List<CarPark> carParks = list.stream().map(carPark -> {
            String convertGeo = webClient.build()
                    .get()
                    .uri(urlBaseConvert + "/commonapi/convert/3857to4326?Y=" + carPark.getYCoord() + "&X=" + carPark.getXCoord())
                    .retrieve()
                    .bodyToMono(String.class).block();
            Map<String, Object> req;
            try {
                req = new ObjectMapper().readValue(convertGeo, Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            log.info("CONVERTING.. => {}", req.get("latitude")+" & "+req.get("longitude"));
            return new CarPark(
                    carPark.getCarParkNo(),
                    carPark.getAddress(),
                    Float.valueOf(req.get("latitude").toString()),
                    Float.valueOf(req.get("longitude").toString()),
                    carPark.getCarParkType(),
                    carPark.getTypeOfParkingSystem(),
                    carPark.getShortTermParking(),
                    carPark.getFreeParking(),
                    carPark.getNightParking(),
                    carPark.getCarParkDecks(),
                    carPark.getGantryHeight(),
                    carPark.getCarParkBasement()
            );
        }).collect(Collectors.toList());

//        for(CarPark carPark : list) {
//            String convertGeo = convert(urlBaseConvert+"/commonapi/convert/3857to4326?Y=" + carPark.getYCoord() + "&X=" + carPark.getXCoord());
//            Map<String, Object> req = new ObjectMapper().readValue(convertGeo, Map.class);
//            log.info("LIST ==>> {}", req.get("latitude")+" <> "+req.get("longitude"));
//            carPark.setXCoord(Float.valueOf(req.get("latitude").toString()));
//            carPark.setYCoord(Float.valueOf(req.get("longitude").toString()));
//
//            saveList.add(carPark);
//        }
        carParkRepository.saveAll(carParks);
    }

    private String convert(String Uri) {
        return webClient.build().get().uri(Uri)
                .retrieve()
                .bodyToMono(String.class).block();
    }

}
