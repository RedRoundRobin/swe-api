package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GatewayServiceTest {

  @MockBean
  private GatewayRepository repo;

  @MockBean
  private DeviceService deviceService;


  private GatewayService gatewayService;


  private Device device1;
  private Device device2;
  private Device device3;

  private Gateway gateway1;
  private Gateway gateway2;


  @Before
  public void setUp() {
    gatewayService = new GatewayService(repo);
    gatewayService.setDeviceService(deviceService);

    // ----------------------------------------- Set Devices --------------------------------------
    device1 = new Device();
    device1.setId(1);
    device2 = new Device();
    device2.setId(2);
    device3 = new Device();
    device3.setId(3);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);


    // ----------------------------------------- Set Gateways --------------------------------------
    gateway1 = new Gateway();
    gateway1.setId(1);
    gateway2 = new Gateway();
    gateway2.setId(2);

    List<Gateway> allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);


    // --------------------------- Set Devices to Gateways and viceversa ---------------------------
    List<Device> gateway1Devices = new ArrayList<>();
    gateway1Devices.add(device1);
    gateway1Devices.add(device2);
    gateway1.setDevices(gateway1Devices);
    device1.setGateway(gateway1);
    device2.setGateway(gateway1);

    List<Device> gateway2Devices = new ArrayList<>();
    gateway2Devices.add(device3);
    gateway2.setDevices(gateway2Devices);
    device3.setGateway(gateway2);



    when(repo.findAll()).thenReturn(allGateways);
    when(repo.findByDevices(any(Device.class))).thenAnswer(i -> {
      Device device = i.getArgument(0);
      return allGateways.stream().filter(g -> g.getDevices().contains(device))
          .findFirst().orElse(null);
    });
    when(repo.findById(anyInt())).thenAnswer(i -> {
      return allGateways.stream().filter(g -> i.getArgument(0).equals(g.getId()))
          .findFirst();
    });

    when(deviceService.findById(anyInt())).thenAnswer(i -> {
      return allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
          .findFirst().orElse(null);
    });

  }

  @Test
  public void findAllGateways() {
    List<Gateway> gateways = gatewayService.findAll();

    assertTrue(!gateways.isEmpty());
    assertTrue(gateways.stream().count() == 2);
  }

  @Test
  public void findGatewayById() {
    Gateway gateway = gatewayService.findById(gateway1.getId());

    assertNotNull(gateway);
  }

  @Test
  public void findGatewayByDeviceId() {
    Gateway gateway = gatewayService.findByDeviceId(device2.getId());

    assertNotNull(gateway);
    assertEquals(gateway1, gateway);
  }

  @Test
  public void findGatewayByNotExistentDeviceId() {
    Gateway gateway = gatewayService.findByDeviceId(8);

    assertNull(gateway);
  }
}
