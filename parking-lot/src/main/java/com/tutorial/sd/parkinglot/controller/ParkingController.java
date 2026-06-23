package com.tutorial.sd.parkinglot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parking")
public class ParkingController {

    @GetMapping("/lot")
    public String getParkingLot() {
        return "Parking Lot";
    }
}
