INSERT INTO gateways(gateway_id, name) VALUES(1, 'Gateway US');
INSERT INTO gateways(gateway_id, name) VALUES(2, 'Gateway SG');
INSERT INTO devices(device_id, name, frequency, gateway_id) VALUES(1, 'Device n1', 1, 1);
INSERT INTO devices(device_id, name, frequency, gateway_id) VALUES(2, 'Device n2', 2, 1);
INSERT INTO devices(device_id, name, frequency, gateway_id) VALUES(3, 'Device n1', 1, 2);
INSERT INTO sensors(sensor_id, type, device_sensor_id, device_id) VALUES(1, 'Sensor n1', 1, 1);
INSERT INTO sensors(sensor_id, type, device_sensor_id, device_id) VALUES(2, 'Sensor n2', 2, 1);
INSERT INTO sensors(sensor_id, type, device_sensor_id, device_id) VALUES(3, 'Sensor n1', 1, 2);