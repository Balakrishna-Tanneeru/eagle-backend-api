package com.eagle.drive.repository;

import com.eagle.drive.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByRider_Id(Long riderId);

    List<Ride> findByDriver_Id(Long driverId);

    boolean existsByDriver_IdAndStatusIn(Long driverId, List<String> statuses);

    List<Ride> findByRider_IdOrderByCreatedAtDesc(Long riderId);

    List<Ride> findByDriver_IdOrderByCreatedAtDesc(Long driverId);
}
