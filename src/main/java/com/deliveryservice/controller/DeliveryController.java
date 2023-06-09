package com.deliveryservice.controller;

import com.deliveryservice.entity.City;
import com.deliveryservice.entity.Vehicle;
import com.deliveryservice.exceptions.ResourceNotFoundException;
import com.deliveryservice.repository.CityRepository;
import com.deliveryservice.repository.VehicleRepository;
import com.deliveryservice.service.DeliveryService;
import com.deliveryservice.service.WeatherImporter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/delivery")
public class DeliveryController {
    private final WeatherImporter weatherImporter;
    private final DeliveryService deliveryService;
    private final CityRepository cityRepository;
    private final VehicleRepository vehicleRepository;
    @GetMapping("/feeRequest")

    public ResponseEntity<Double> calculateDeliveryFee(@RequestParam String city,
                                                       @RequestParam String vehicleType,
                                                       @RequestParam(required = false) String datetime) {

        if ("Tallinn".equalsIgnoreCase(city)) {
            city = "Tallinn-Harku";
        }
        if ("Tartu".equalsIgnoreCase(city)) {
            city = "Tartu-Tõravere";
        }
        double fee = deliveryService.calculateDeliveryFee(city, vehicleType, datetime);
        return ResponseEntity.ok(fee);
    }

    @PostMapping("/cron")
    public ResponseEntity<String> setCronExpression(@RequestParam String cronExpression) {
        try {
            weatherImporter.setCronExpression(cronExpression);
            return ResponseEntity.ok("Cron expression set to " + cronExpression);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/city/setFee")
    public ResponseEntity<?> setCityFee(@RequestParam String cityName, @RequestParam Double fee) {
        City city = cityRepository.findByCityIgnoreCase(cityName)
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + cityName));

        city.setFee(fee);
        cityRepository.save(city);

        return new ResponseEntity<>("Fee updated for city: " + cityName, HttpStatus.OK);
    }

    @PostMapping("/vehicle/setFee")
    public ResponseEntity<?> setVehicleFee(@RequestParam String vehicleType, @RequestParam Double fee) {
        Vehicle vehicle = vehicleRepository.findByVehicleIgnoreCase(vehicleType)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleType));

        vehicle.setFee(fee);
        vehicleRepository.save(vehicle);

        return new ResponseEntity<>("Fee updated for vehicle: " + vehicleType, HttpStatus.OK);
    }
}
