package com.nextcharge.api.config;

import com.nextcharge.api.model.*;
import com.nextcharge.api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final StationImageRepository stationImageRepository;
    private final StationAmenityRepository stationAmenityRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeedRunner(UserRepository userRepository, StationRepository stationRepository,
                          ChargerRepository chargerRepository, StationImageRepository stationImageRepository,
                          StationAmenityRepository stationAmenityRepository, FleetVehicleRepository fleetVehicleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.stationImageRepository = stationImageRepository;
        this.stationAmenityRepository = stationAmenityRepository;
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            // Already seeded
            return;
        }

        // Seed Users
        User admin = User.builder()
                .name("Admin Administrator")
                .email("admin@nextcharge.in")
                .password(passwordEncoder.encode("admin123"))
                .phone("+919999999999")
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .walletBalance(BigDecimal.valueOf(10000.00))
                .build();

        User owner = User.builder()
                .name("Karan Malhotra (Station Operator)")
                .email("owner@nextcharge.in")
                .password(passwordEncoder.encode("owner123"))
                .phone("+918888888888")
                .role(User.Role.STATION_OWNER)
                .status(User.Status.ACTIVE)
                .walletBalance(BigDecimal.valueOf(5000.00))
                .build();

        User customer = User.builder()
                .name("Rohan Sharma")
                .email("customer@nextcharge.in")
                .password(passwordEncoder.encode("customer123"))
                .phone("+917777777777")
                .role(User.Role.CUSTOMER)
                .status(User.Status.ACTIVE)
                .walletBalance(BigDecimal.valueOf(1500.00)) // Seeding direct starting wallet balance
                .build();

        User fleetManager = User.builder()
                .name("Vijay Singh (Zomato Fleet)")
                .email("fleet@nextcharge.in")
                .password(passwordEncoder.encode("fleet123"))
                .phone("+916666666666")
                .role(User.Role.FLEET_MANAGER)
                .status(User.Status.ACTIVE)
                .walletBalance(BigDecimal.valueOf(25000.00))
                .build();

        userRepository.saveAll(List.of(admin, owner, customer, fleetManager));

        // Seed Stations (under owner Malhotra)
        Station station1 = Station.builder()
                .name("NextCharge Hub - Indiranagar")
                .description("Premium high-speed charging center located in the heart of Indiranagar. Features high speed CCS2 dc fast chargers, restrooms, and a cafe.")
                .address("100 Feet Road, Indiranagar, Bengaluru, Karnataka 560038")
                .latitude(BigDecimal.valueOf(12.97189))
                .longitude(BigDecimal.valueOf(77.64115))
                .operatingHours("24/7")
                .owner(owner)
                .status(Station.ApprovalStatus.APPROVED)
                .isActive(true)
                .build();

        Station station2 = Station.builder()
                .name("NextCharge Park - Whitefield")
                .description("Convenient charging stop adjacent to tech park. 150kW hypercharging available.")
                .address("ITPL Main Road, Whitefield, Bengaluru, Karnataka 560066")
                .latitude(BigDecimal.valueOf(12.9698))
                .longitude(BigDecimal.valueOf(77.7499))
                .operatingHours("06:00 AM - 11:00 PM")
                .owner(owner)
                .status(Station.ApprovalStatus.APPROVED)
                .isActive(true)
                .build();

        Station station3 = Station.builder()
                .name("NextCharge Hub - Koramangala")
                .description("New facility with restroom amenities and waiting lounge. Approvals pending.")
                .address("80 Feet Road, Koramangala 4th Block, Bengaluru, Karnataka 560034")
                .latitude(BigDecimal.valueOf(12.9352))
                .longitude(BigDecimal.valueOf(77.6245))
                .operatingHours("24/7")
                .owner(owner)
                .status(Station.ApprovalStatus.PENDING_APPROVAL)
                .isActive(true)
                .build();

        stationRepository.saveAll(List.of(station1, station2, station3));

        // Seed Images
        stationImageRepository.saveAll(List.of(
                StationImage.builder().station(station1).imageUrl("https://images.unsplash.com/photo-1563720223185-11003d516935?w=600&auto=format&fit=crop&q=60").build(),
                StationImage.builder().station(station2).imageUrl("https://images.unsplash.com/photo-1563720223523-491ff04651de?w=600&auto=format&fit=crop&q=60").build(),
                StationImage.builder().station(station3).imageUrl("https://images.unsplash.com/photo-1620121692029-d088224ddc74?w=600&auto=format&fit=crop&q=60").build()
        ));

        // Seed Amenities
        stationAmenityRepository.saveAll(List.of(
                StationAmenity.builder().station(station1).amenityName(StationAmenity.AmenityType.WIFI).build(),
                StationAmenity.builder().station(station1).amenityName(StationAmenity.AmenityType.CAFE).build(),
                StationAmenity.builder().station(station1).amenityName(StationAmenity.AmenityType.RESTROOM).build(),
                
                StationAmenity.builder().station(station2).amenityName(StationAmenity.AmenityType.PARKING).build(),
                StationAmenity.builder().station(station2).amenityName(StationAmenity.AmenityType.RESTROOM).build(),
                
                StationAmenity.builder().station(station3).amenityName(StationAmenity.AmenityType.WAITING_AREA).build(),
                StationAmenity.builder().station(station3).amenityName(StationAmenity.AmenityType.RESTROOM).build()
        ));

        // Seed Chargers
        chargerRepository.saveAll(List.of(
                Charger.builder().station(station1).name("FastCCS Delta 60").connectorType(Charger.ConnectorType.CCS2).chargingSpeedKw(60).pricePerKwh(BigDecimal.valueOf(18.50)).status(Charger.ChargerStatus.AVAILABLE).build(),
                Charger.builder().station(station1).name("AC Type 2 Smart").connectorType(Charger.ConnectorType.TYPE2).chargingSpeedKw(22).pricePerKwh(BigDecimal.valueOf(12.00)).status(Charger.ChargerStatus.AVAILABLE).build(),
                
                Charger.builder().station(station2).name("HyperCCS Super 150").connectorType(Charger.ConnectorType.CCS2).chargingSpeedKw(150).pricePerKwh(BigDecimal.valueOf(24.50)).status(Charger.ChargerStatus.AVAILABLE).build(),
                Charger.builder().station(station2).name("GB/T Express 60").connectorType(Charger.ConnectorType.GBT).chargingSpeedKw(60).pricePerKwh(BigDecimal.valueOf(16.00)).status(Charger.ChargerStatus.AVAILABLE).build(),
                
                Charger.builder().station(station3).name("Turbo CHAdeMO").connectorType(Charger.ConnectorType.CHADEMO).chargingSpeedKw(50).pricePerKwh(BigDecimal.valueOf(20.00)).status(Charger.ChargerStatus.AVAILABLE).build()
        ));

        // Seed Fleet Vehicles
        fleetVehicleRepository.saveAll(List.of(
                FleetVehicle.builder().fleetManager(fleetManager).plateNumber("KA03MY4321").model("Tata Nexon EV").driverName("Amit Kumar").build(),
                FleetVehicle.builder().fleetManager(fleetManager).plateNumber("KA01JZ9876").model("MG ZS EV").driverName("Suresh Raina").build()
        ));
    }
}
