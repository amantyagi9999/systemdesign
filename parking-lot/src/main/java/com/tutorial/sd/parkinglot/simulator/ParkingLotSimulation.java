package com.tutorial.sd.parkinglot.simulator;

import com.tutorial.sd.parkinglot.controller.*;
import com.tutorial.sd.parkinglot.model.PricingRule;
import com.tutorial.sd.parkinglot.model.Vehicle;
import com.tutorial.sd.parkinglot.repository.*;
import com.tutorial.sd.parkinglot.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ParkingLotSimulation implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== PARKING LOT LLD SIMULATION ===");
        System.out.println("This simulation demonstrates the complete parking lot system flow\n");

        // Initialize repositories
        TicketRepository ticketRepository = new TicketRepository();
        SlotRepository slotRepository = new SlotRepository();
        FloorRepository floorRepository = new FloorRepository();
        PricingRuleRepository pricingRuleRepository = new PricingRuleRepository();
        PaymentRepository paymentRepository = new PaymentRepository();

        // Initialize services
        TicketService ticketService = new TicketService(ticketRepository);
        SlotService slotService = new SlotService(slotRepository);
        PricingService pricingService = new PricingService(pricingRuleRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        ReceiptService receiptService = new ReceiptService();
        AdminService adminService = new AdminService(floorRepository, slotRepository, pricingRuleRepository);

        // Initialize controllers
        EntryController entryController = new EntryController(ticketService, slotService);
        ExitController exitController = new ExitController(ticketService, pricingService, paymentService, receiptService, slotService);
        AdminController adminController = new AdminController(adminService);

        // === INITIALIZATION PHASE ===
        System.out.println("=== INITIALIZATION PHASE ===");
        adminController.initializeParkingLot();
        adminController.getParkingStatus();

        // === ENTRY FLOW SIMULATION ===
        System.out.println("\n=== ENTRY FLOW SIMULATION ===");

        // Simulate vehicle entries
        simulateVehicleEntry(entryController, "ABC123", Vehicle.VehicleType.CAR);
        simulateVehicleEntry(entryController, "XYZ789", Vehicle.VehicleType.BIKE);
        simulateVehicleEntry(entryController, "DEF456", Vehicle.VehicleType.TRUCK);

        // === EXIT FLOW SIMULATION ===
        System.out.println("\n=== EXIT FLOW SIMULATION ===");

        // Get active tickets for exit simulation
        List<UUID> activeTickets = ticketRepository.findActiveTickets().stream()
                .map(ticket -> ticket.getId())
                .toList();

        System.out.println("[REPOSITORY] Found " + activeTickets.size() + " active tickets");

        // Simulate vehicle exits
        for (UUID ticketId : activeTickets) {
            simulateVehicleExit(exitController, ticketId);
        }

        // === ADMIN OPERATIONS SIMULATION ===
        System.out.println("\n=== ADMIN OPERATIONS SIMULATION ===");
        simulateAdminOperations(adminController);

        // === FINAL STATUS ===
        System.out.println("\n=== FINAL STATUS ===");
        adminController.getParkingStatus();

        System.out.println("\n=== SIMULATION COMPLETED ===");
    }

    private static void simulateVehicleEntry(EntryController entryController, String licensePlate, Vehicle.VehicleType vehicleType) {
        System.out.println("\n--- Vehicle Entry Simulation ---");
        EntryController.EntryResult result = entryController.enterVehicle(licensePlate, vehicleType);

        if (result.isSuccess()) {
            System.out.println("✅ Entry successful - Ticket ID: " + result.getTicketId());
        } else {
            System.out.println("❌ Entry failed: " + result.getMessage());
        }
    }

    private static void simulateVehicleExit(ExitController exitController, UUID ticketId) {
        System.out.println("\n--- Vehicle Exit Simulation ---");
        ExitController.ExitResult result = exitController.exitVehicle(ticketId);

        if (result.isSuccess()) {
            System.out.println("✅ Exit successful - Receipt ID: " + result.getReceiptId());
            System.out.println("💰 Total Fee: $" + String.format("%.2f", result.getFee()));
            System.out.println("💳 Payment Status: SUCCESS");

            // Generate receipt text
            String receiptText = exitController.generateReceiptText(ticketId);
            System.out.println(receiptText);
        } else {
            System.out.println("❌ Exit failed: " + result.getMessage());
        }
    }

    private static void simulateAdminOperations(AdminController adminController) {
        System.out.println("\n--- Admin Operations Simulation ---");

        // Add a new floor
        adminController.addFloor(3);

        // Add slots to the new floor
        adminController.addSlotsToFloor(3, Vehicle.VehicleType.CAR, 10);
        adminController.addSlotsToFloor(3, Vehicle.VehicleType.EV, 5);

        // Update pricing for CAR
        adminController.updatePricingRule(Vehicle.VehicleType.CAR, 25.0, 60.0);

        // Update flat pricing for BIKE
        adminController.updateFlatPricing(Vehicle.VehicleType.BIKE, 35.0);

        // Update hourly pricing for TRUCK
        adminController.updateHourlyPricing(Vehicle.VehicleType.TRUCK, 40.0);

        // Add a new pricing rule for EV
        PricingRule newEvRule = new PricingRule(Vehicle.VehicleType.EV, 18.0, 50.0);
        adminController.addPricingRule(newEvRule);

        System.out.println("✅ Admin operations completed successfully");
    }
}