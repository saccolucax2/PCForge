INSERT INTO Part (id, brand, model, price)
SELECT 1, 'Intel', 'Intel i5-12400F', 180.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 1);

INSERT INTO CPU (id, cores, threads, socketType, cacheSize, freqBase, freqBoost)
SELECT 1, 6, 12, 'LGA1700', 'L3 18MB', 2.5, 4.4
WHERE NOT EXISTS (SELECT 1 FROM CPU WHERE id = 1);

INSERT INTO Part (id, brand, model, price)
SELECT 2, 'Intel', 'Intel Pentium Gold G6400', 60.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 2);

INSERT INTO CPU (id, cores, threads, socketType, cacheSize, freqBase, freqBoost)
SELECT 2, 2, 4, 'LGA1200', 'L3 4MB', 4.0, 4.0
WHERE NOT EXISTS (SELECT 1 FROM CPU WHERE id = 2);

INSERT INTO Part (id, brand, model, price)
SELECT 3, 'AMD', 'AMD Ryzen 5 5600X', 200.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 3);

INSERT INTO CPU (id, cores, threads, socketType, cacheSize, freqBase, freqBoost)
SELECT 3, 6, 12, 'AM4', 'L3 32MB', 3.7, 4.6
WHERE NOT EXISTS (SELECT 1 FROM CPU WHERE id = 3);

INSERT INTO Part (id, brand, model, price)
SELECT 4, 'AMD', 'Ryzen 7 5800X', 350.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 4);

INSERT INTO CPU (id, cores, threads, socketType, cacheSize, freqBase, freqBoost)
SELECT 4, 8, 16, 'AM4', 'L3 32MB', 3.8, 4.7
WHERE NOT EXISTS (SELECT 1 FROM CPU WHERE id = 4);

INSERT INTO Part (id, brand, model, price)
SELECT 5, 'NVIDIA', 'NVIDIA GeForce GTX 1650', 160.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 5);

INSERT INTO GPU (id, memory, CUDACores, memoryBus, baseClock, boostClock, TDP)
SELECT 5, '4 GB GDDR5', 896, 128, 1485, 1665, 75
WHERE NOT EXISTS (SELECT 1 FROM GPU WHERE id = 5);

INSERT INTO Part (id, brand, model, price)
SELECT 6, 'AMD', 'AMD Radeon RX 6500 XT', 180.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 6);

INSERT INTO GPU (id, memory, CUDACores, memoryBus, baseClock, boostClock, TDP)
SELECT 6, '4 GB GDDR6', 1024, 64, 2410, 2815, 107
WHERE NOT EXISTS (SELECT 1 FROM GPU WHERE id = 6);

INSERT INTO Part (id, brand, model, price)
SELECT 7, 'AMD', 'AMD Radeon RX 6600 XT', 330.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 7);

INSERT INTO GPU (id, memory, CUDACores, memoryBus, baseClock, boostClock, TDP)
SELECT 7, '8 GB GDDR6', 2048, 128, 1968, 2589, 160
WHERE NOT EXISTS (SELECT 1 FROM GPU WHERE id = 7);

INSERT INTO Part (id, brand, model, price)
SELECT 8, 'NVIDIA', 'NVIDIA GeForce RTX 3060', 350.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 8);

INSERT INTO GPU (id, memory, CUDACores, memoryBus, baseClock, boostClock, TDP)
SELECT 8, '12 GB GDDR6', 3584, 192, 1320, 1777, 170
WHERE NOT EXISTS (SELECT 1 FROM GPU WHERE id = 8);

INSERT INTO Part (id, brand, model, price)
SELECT 9, 'Kingston', 'Kingston ValueRAM 8GB DDR4 2666 MHz', 35.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 9);

INSERT INTO RAM (id, type, capacity, frequency, CASLatency, tension)
SELECT 9, 'DDR4', 8, 2666, 'CL19-19-19-43', 1.20
WHERE NOT EXISTS (SELECT 1 FROM RAM WHERE id = 9);

INSERT INTO Part (id, brand, model, price)
SELECT 10, 'Corsair', 'Corsair Vengeance 16GB DDR4 3200 MHz', 75.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 10);

INSERT INTO RAM (id, type, capacity, frequency, CASLatency, tension)
SELECT 10, 'DDR4', 16, 3200, 'CL16-18-18-36', 1.35
WHERE NOT EXISTS (SELECT 1 FROM RAM WHERE id = 10);

INSERT INTO Part (id, brand, model, price)
SELECT 11, 'G.Skill', 'G.Skill Ripjaws 16GB DDR4 3600 MHz', 70.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 11);

INSERT INTO RAM (id, type, capacity, frequency, CASLatency, tension)
SELECT 11, 'DDR4', 16, 3600, 'CL18-22-22-42', 1.35
WHERE NOT EXISTS (SELECT 1 FROM RAM WHERE id = 11);

INSERT INTO Part (id, brand, model, price)
SELECT 12, 'Corsair', 'Corsair Vengeance 32GB DDR4 3200 MHz', 140.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 12);

INSERT INTO RAM (id, type, capacity, frequency, CASLatency, tension)
SELECT 12, 'DDR4', 32, 3200, 'CL16-18-18-36', 1.35
WHERE NOT EXISTS (SELECT 1 FROM RAM WHERE id = 12);

INSERT INTO Part (id, brand, model, price)
SELECT 13, 'Intel', 'MSI B460M PRO-VDH', 80.00
WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 13);

INSERT INTO MotherBoard (id, chipset, socket, ramSupport, slotM2)
SELECT 13, 'Intel B460', 'LGA1200', 'DDR4 fino a 128 GB (4 slot)', '2xM.2 (key M)'
WHERE NOT EXISTS (SELECT 1 FROM MotherBoard WHERE id = 13);

INSERT INTO Part (id, brand, model, price)
SELECT 14, 'Intel', 'MSI B560M PRO-VDH', 100.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 14);

INSERT INTO MotherBoard (id, chipset, socket, ramSupport, slotM2)
SELECT 14, 'Intel B560', 'LGA1200', 'DDR4 fino a 128 GB (4 slot)', '2xM.2 (key M)'
    WHERE NOT EXISTS (SELECT 1 FROM MotherBoard WHERE id = 14);

INSERT INTO Part (id, brand, model, price)
SELECT 15, 'ASUS', 'ASUS TUF Gaming X570', 180.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 15);

INSERT INTO MotherBoard (id, chipset, socket, ramSupport, slotM2)
SELECT 15, 'AMD X570', 'AM4', 'DDR4 fino a 128 GB (4 slot)', '2xM.2 (key M) con dissipatore'
    WHERE NOT EXISTS (SELECT 1 FROM MotherBoard WHERE id = 15);

INSERT INTO Part (id, brand, model, price)
SELECT 16, 'Intel', 'GigaByte X590 AORUS ELITE', 200.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 16);

INSERT INTO MotherBoard (id, chipset, socket, ramSupport, slotM2)
SELECT 16, 'Intel Z590', 'LGA1200', 'DDR4 fino a 128 GB (4 slot)', '2xM.2 (key M) con dissipatori'
    WHERE NOT EXISTS (SELECT 1 FROM MotherBoard WHERE id = 16);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 13, '1x PCIe 3.0x16'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 13 AND pcie_slot = '1x PCIe 3.0x16'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 13, '2x PCIe 3.0x1'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 13 AND pcie_slot = '2x PCIe 3.0x1'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 14, '1x PCIe 4.0x16'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 14 AND pcie_slot = '1x PCIe 4.0x16'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 14, '2x PCIe 3.0x1'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 14 AND pcie_slot = '2x PCIe 3.0x1'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 15, '2x PCIe 4.0x16'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 15 AND pcie_slot = '2x PCIe 4.0x16'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 15, '3x PCIe 4.0x1'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 15 AND pcie_slot = '3x PCIe 4.0x1'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 16, '1x PCIe 4.0x16'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 16 AND pcie_slot = '1x PCIe 4.0x16'
);

INSERT INTO MotherBoard_PCIeSlot(motherboard_id, pcie_slot)
SELECT 16, '2x PCIe 3.0x16 (x4/x1)'
WHERE NOT EXISTS (
    SELECT 1 FROM MotherBoard_PCIeSlot
    WHERE motherboard_id = 16 AND pcie_slot = '2x PCIe 3.0x16 (x4/x1)'
);

INSERT INTO Part (id, brand, model, price)
SELECT 17, '', 'Cooler Master MasterBox Q300L', 50.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 17);

INSERT INTO Cas (id, formFactor, Bay, Pannelli, Ventola)
SELECT 17, 'Micro-ATX / Mini-ITX', '2x2.5",1x3.5', 'forati', '1x120 mm frontale'
    WHERE NOT EXISTS (SELECT 1 FROM Cas WHERE id = 17);

INSERT INTO Part (id, brand, model, price)
SELECT 18, '', 'NZXT H510', 80.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 18);

INSERT INTO Cas (id, formFactor, Bay, Pannelli, Ventola)
SELECT 18, 'Mid-Tower ATX', '2x2.5",2x3.5', 'vetro temperato lato sinistro, acciaio lato destro', '2x120 mm (1 frontale, 1 posteriore)'
    WHERE NOT EXISTS (SELECT 1 FROM Cas WHERE id = 18);

INSERT INTO Part (id, brand, model, price)
SELECT 19, '', 'Fractal Design Mashify C', 100.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 19);

INSERT INTO Cas (id, formFactor, Bay, Pannelli, Ventola)
SELECT 19, 'Mid-Tower ATX', '2x2.5",2x3.5', 'frontale in mesh, vetro temperato lato sinistro', '2x120 mm (1 frontale, 1 posteriore)'
    WHERE NOT EXISTS (SELECT 1 FROM Cas WHERE id = 19);

INSERT INTO Part (id, brand, model, price)
SELECT 20, '', 'Phanteks Eclipse P400A', 90.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 20);

INSERT INTO Cas (id, formFactor, Bay, Pannelli, Ventola)
SELECT 20, 'Mid-Tower ATX', '2x2.5",2x3.5', 'fronte in mesh, vetro temperato lato sinistro', '3x120 mm frontali, 1x120 mm posteriore'
    WHERE NOT EXISTS (SELECT 1 FROM Cas WHERE id = 20);

INSERT INTO Part (id, brand, model, price)
SELECT 21, 'EVGA', 'EVGA 500 W BR 80+ Bronze', 50.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 21);

INSERT INTO PSU (id, wattage, efficiencyRating, ventola)
SELECT 21, 550, '80+ Bronze', '120 mm'
    WHERE NOT EXISTS (SELECT 1 FROM PSU WHERE id = 21);

INSERT INTO Part (id, brand, model, price)
SELECT 22, 'Corsair', 'Corsair CV550 550 W 80+ Bronze', 60.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 22);

INSERT INTO PSU (id, wattage, efficiencyRating, ventola)
SELECT 22, 550, '80+ Bronze', '120 mm'
    WHERE NOT EXISTS (SELECT 1 FROM PSU WHERE id = 22);

INSERT INTO Part (id, brand, model, price)
SELECT 23, 'EVGA', 'EVGA 600 BR 600 W 80+ Bronze', 70.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 23);

INSERT INTO PSU (id, wattage, efficiencyRating, ventola)
SELECT 23, 600, '80+ Bronze', '120 mm'
    WHERE NOT EXISTS (SELECT 1 FROM PSU WHERE id = 23);

INSERT INTO Part (id, brand, model, price)
SELECT 24, 'Corsair', 'Corsair RM750 750 W 80+ Gold', 120.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 24);

INSERT INTO PSU (id, wattage, efficiencyRating, ventola)
SELECT 24, 750, '80+ Gold', '135 mm zero-RPM mode'
    WHERE NOT EXISTS (SELECT 1 FROM PSU WHERE id = 24);

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 21, '1xATX 20+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 21 AND connector = '1xATX 20+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 21, '1xEPS 4+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 21 AND connector = '1xEPS 4+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 22, '1xATX 20+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 22 AND connector = '1xATX 20+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 22, '1xEPS 4+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 22 AND connector = '1xEPS 4+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 22, '2xPCIe 6+2 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 22 AND connector = '2xPCIe 6+2 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 23, '1xATX 20+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 23 AND connector = '1xATX 20+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 23, '1xEPS 4+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 23 AND connector = '1xEPS 4+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 23, '2xPCIe 6+2 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 23 AND connector = '2xPCIe 6+2 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 24, '1xATX 20+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 24 AND connector = '1xATX 20+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 24, '1xEPS 4+4 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 24 AND connector = '1xEPS 4+4 pin');

INSERT INTO PSU_Connector(psu_id, connector)
SELECT 24, '2xPCIe 6+2 pin'
    WHERE NOT EXISTS (
    SELECT 1 FROM PSU_Connector
    WHERE psu_id = 24 AND connector = '2xPCIe 6+2 pin');

INSERT INTO Part (id, brand, model, price)
SELECT 25, 'Intel', 'Intel Stock Cooler', 15.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 25);

INSERT INTO Cooler (id, type, socketCompatibility, ventola, heatPipes)
SELECT 25, 'Air Cooler stock', 'LGA115x', '92 mm PWM, 800-2300 RPM', '3 (alluminio)'
    WHERE NOT EXISTS (SELECT 1 FROM Cooler WHERE id = 25);

INSERT INTO Part (id, brand, model, price)
SELECT 26, '', 'Cooler Master Hyper 212 Black Edition', 40.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 26);

INSERT INTO Cooler (id, type, socketCompatibility, ventola, heatPipes)
SELECT 26, 'Air Cooler', 'LGA1700/1200/115X, AM4/AM3+', '120 mm PWM, 600-2000 RPM', '4 (rame)'
    WHERE NOT EXISTS (SELECT 1 FROM Cooler WHERE id = 26);

INSERT INTO Part (id, brand, model, price)
SELECT 27, '', 'be quiet! Pure Rock 2', 45.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 27);

INSERT INTO Cooler (id, type, socketCompatibility, ventola, heatPipes)
SELECT 27, 'AirCooler', 'LGA1200/115x, AM4/AM3+', '120 mm PWM, 200-1600 RPM', '4 (rame)'
    WHERE NOT EXISTS (SELECT 1 FROM Cooler WHERE id = 27);

INSERT INTO Part (id, brand, model, price)
SELECT 28, 'Corsair', 'Corsair H100i RGB PLATINUM', 120.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 28);

INSERT INTO Cooler (id, type, socketCompatibility, ventola, heatPipes)
SELECT 28, 'AIO Liquid Cooler 240 mm', 'LGA1700/1200/115x, AM4/AM5', '2x120 mm PWM, 400-2400 RPM', 'tubi 380 mm in treccia'
    WHERE NOT EXISTS (SELECT 1 FROM Cooler WHERE id = 28);

INSERT INTO Part (id, brand, model, price)
SELECT 29, 'Kingston', 'Kingston A400 240 GB SATA III', 30.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 29);

INSERT INTO SSD (id, formFactor, interfaceType, capacity, readSpeed, writeSpeed)
SELECT 29, '2.5', 'SATA III', '240 GB', 500,450
    WHERE NOT EXISTS (SELECT 1 FROM SSD WHERE id = 29);

INSERT INTO Part (id, brand, model, price)
SELECT 30, 'Crucial', 'Crucial MX500 500 GB SATA III', 55.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 30);

INSERT INTO SSD (id, formFactor, interfaceType, capacity, readSpeed, writeSpeed)
SELECT 30, '2.5', 'SATA III', '500 GB', 560,510
    WHERE NOT EXISTS (SELECT 1 FROM SSD WHERE id = 30);

INSERT INTO Part (id, brand, model, price)
SELECT 31, 'Samsung', 'Samsung 970 EVO Plus 1 TB NVMe M.2', 120.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 31);

INSERT INTO SSD (id, formFactor, interfaceType, capacity, readSpeed, writeSpeed)
SELECT 31, 'M.2 2280', 'NVMe PCIe 3.0 x4', '1 TB', 3500,3300
    WHERE NOT EXISTS (SELECT 1 FROM SSD WHERE id = 31);

INSERT INTO Part (id, brand, model, price)
SELECT 32, 'Kingston', 'Western Digital Black SN750 1 TB NVMe M.2', 140.00
    WHERE NOT EXISTS (SELECT 1 FROM Part WHERE id = 32);

INSERT INTO SSD (id, formFactor, interfaceType, capacity, readSpeed, writeSpeed)
SELECT 32, 'M.2 2280', 'NVMe PCIe 3.0 x4', '1 TB', 3470,3000
    WHERE NOT EXISTS (SELECT 1 FROM SSD WHERE id = 32);