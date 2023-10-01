package com.heydieproject.springbatchpartitioning.repository;

import com.heydieproject.springbatchpartitioning.entity.CarPark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarParkRepository extends JpaRepository<CarPark, String> {
}
