package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.utils.exception.ConflictException;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private final DeviceRepository deviceRepo;

  private final GatewayRepository gatewayRepo;

  private final SensorRepository sensorRepo;

  private final EntityRepository entityRepo;

  private boolean checkAddEditFields(boolean edit, Map<String, Object> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("gatewayId");
    allowedFields.add("frequency");
    allowedFields.add("name");
    allowedFields.add("realDeviceId");

    if (edit) {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    } else {
      return fields.containsKey("frequency")
          && fields.containsKey("gatewayId")
          && fields.containsKey("name")
          && fields.containsKey("realDeviceId");
    }
  }

  private Device addEditDevice(Device device, Map<String, Object> fields)
      throws InvalidFieldsValuesException, ConflictException {
    if (device == null) {
      device = new Device();
    }

    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      switch (entry.getKey()) {
        case "frequency":
          try {
            device.setFrequency((int) entry.getValue());
          } catch (ClassCastException nfe) {
            throw new InvalidFieldsValuesException("The frequency provided is not valid");
          }
          break;
        case "gatewayId":
          Gateway gateway = gatewayRepo.findById((int) entry.getValue()).orElse(null);
          if (gateway != null) {
            device.setGateway(gateway);
          } else {
            throw new InvalidFieldsValuesException("The gateway with provided id is not found");
          }
          break;
        case "name":
          device.setName((String) entry.getValue());
          break;
        case "realDeviceId":
          device.setRealDeviceId((int) entry.getValue());
          break;
        default:
      }
    }

    Device deviceWithSameGatewayAndRealDeviceId = deviceRepo
        .findByGatewayAndRealDeviceId(device.getGateway(), device.getRealDeviceId());
    if (deviceWithSameGatewayAndRealDeviceId != null
        && !deviceWithSameGatewayAndRealDeviceId.equals(device)) {
      throw new ConflictException("The device with provided gateway and realDeviceId "
          + "already exists");
    }

    return deviceRepo.save(device);
  }

  @Autowired
  public DeviceService(DeviceRepository deviceRepository, EntityRepository entityRepository,
                       GatewayRepository gatewayRepository, SensorRepository sensorRepository) {
    this.deviceRepo = deviceRepository;
    this.entityRepo = entityRepository;
    this.gatewayRepo = gatewayRepository;
    this.sensorRepo = sensorRepository;
  }

  public List<Device> findAll() {
    return (List<Device>) deviceRepo.findAll();
  }

  public List<Device> findAllByEntityId(int entityId) {
    return (List<Device>) deviceRepo.findAllByEntityId(entityId);
  }

  public List<Device> findAllByEntityIdAndGatewayId(int entityId, int gatewayId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return (List<Device>) deviceRepo.findAllByEntityIdAndGateway(entityId, gateway);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Device> findAllByGatewayId(int gatewayId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return (List<Device>) deviceRepo.findAllByGateway(gateway);
    } else {
      return Collections.emptyList();
    }
  }

  public Device findById(int id) {
    return deviceRepo.findById(id).orElse(null);
  }

  public Device findBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return deviceRepo.findBySensors(sensor);
    } else {
      return null;
    }
  }

  public Device findByIdAndEntityId(int id, int entityId) {
    return deviceRepo.findByIdAndEntityId(id, entityId);
  }

  public Device findByGatewayIdAndRealDeviceId(int gatewayId, int realDeviceId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return deviceRepo.findByGatewayAndRealDeviceId(gateway, realDeviceId);
    } else {
      return null;
    }
  }

  public Device addDevice(Map<String, Object> newDeviceFields) throws MissingFieldsException,
      InvalidFieldsValuesException, ConflictException {
    if (checkAddEditFields(false, newDeviceFields)) {
      return addEditDevice(null, newDeviceFields);
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Device editDevice(int deviceId, Map<String, Object> newDeviceFields)
      throws MissingFieldsException, InvalidFieldsValuesException, ElementNotFoundException,
      ConflictException {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    if (device != null) {
      if (checkAddEditFields(true, newDeviceFields)) {
        return addEditDevice(device, newDeviceFields);
      } else {
        throw MissingFieldsException.defaultMessage();
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("device");
    }
  }

  public boolean deleteDevice(int deviceId) throws ElementNotFoundException {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    if (device != null) {
      deviceRepo.delete(device);
      if (!deviceRepo.existsById(deviceId)) {
        return true;
      } else {
        return false;
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("device");
    }
  }

  public List<Device> getEnabled(boolean cmdEnabled) {
    return (List<Device>)deviceRepo.findBySensorsCmdEnabledField(cmdEnabled);
  }

  public List<Sensor> getEnabledSensorsDevice(boolean cmdEnabled, int deviceId) {
    return (List<Sensor>)deviceRepo.findByCmdEnabledAndDeviceId(cmdEnabled, deviceId);
  }
}