package com.ampara.tourism.repository;

import com.ampara.tourism.entity.TransportRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {

    List<TransportRoute> findByTypeIgnoreCase(String type);

    List<TransportRoute> findByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);

    List<TransportRoute> findByOriginIgnoreCaseOrDestinationIgnoreCase(String origin, String destination);
}
