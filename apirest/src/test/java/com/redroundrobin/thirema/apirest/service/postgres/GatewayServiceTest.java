package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class GatewayServiceTest {

  private GatewayService gatewayService;

  @MockBean
  private GatewayRepository gatewayRepo;

  @MockBean
  private DeviceRepository deviceRepo;

  @MockBean
  private SensorRepository sensorRepo;

  @MockBean
  private KafkaTemplate<String, String> kafkaTemplate;

  private Device device1;
  private Device device2;
  private Device device3;

  private Gateway gateway1;
  private Gateway gateway2;

  @Before
  public void setUp() {
    gatewayService = new GatewayService(gatewayRepo, deviceRepo, sensorRepo, kafkaTemplate);

    // -------------------------------------- Set Devices ----------------------------------------
    device1 = new Device(1, "name1", 1, 1);
    device2 = new Device(2, "name2", 2, 2);
    device3 = new Device(3, "name3", 3, 3);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);
    allDevices.add(device3);

    // -------------------------------------- Set Gateways ----------------------------------------
    gateway1 = new Gateway(1, "name1");
    gateway2 = new Gateway(2, "name2");

    List<Gateway> allGateways = new ArrayList<>();
    allGateways.add(gateway1);
    allGateways.add(gateway2);

    // -------------------------------- Set Gateways to Devices --------------------------------
    device1.setGateway(gateway1);
    device2.setGateway(gateway1);

    device3.setGateway(gateway2);

    when(gatewayRepo.findAll()).thenReturn(allGateways);
    when(gatewayRepo.findByDevice(anyInt())).thenAnswer(i -> {
      Device device = deviceRepo.findById(i.getArgument(0)).orElse(null);
      if (device != null) {
        return allGateways.stream().filter(g -> device.getGateway().equals(g))
            .findFirst().orElse(null);
      } else {
        return null;
      }
    });
    when(gatewayRepo.findById(anyInt())).thenAnswer(i -> allGateways.stream().filter(g -> i.getArgument(0).equals(g.getId()))
        .findFirst());

    when(deviceRepo.findById(anyInt())).thenAnswer(i -> allDevices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
        .findFirst());

  }

  @Test
  public void findAllGateways() {
    List<Gateway> gateways = gatewayService.findAll();

    assertFalse(gateways.isEmpty());
    assertEquals(2, (long) gateways.size());
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
