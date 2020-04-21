package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

  private final GatewayRepository gatewayRepo;

  private final DeviceRepository deviceRepo;

  @Autowired
  public GatewayService(GatewayRepository gatewayRepository, DeviceRepository deviceRepository) {
    this.gatewayRepo = gatewayRepository;
    this.deviceRepo = deviceRepository;
  }

  public List<Gateway> findAll() {
    return (List<Gateway>) gatewayRepo.findAll();
  }

  public List<Gateway> findAllByEntityId(int entityId) {
    return (List<Gateway>) gatewayRepo.findAllByEntityId(entityId);
  }

  public Gateway findByDeviceId(int deviceId) {
    return gatewayRepo.findByDevice(deviceId);
  }

  public Gateway findByDeviceIdAndEntityId(int deviceId, int entityId) {
    return gatewayRepo.findByDeviceIdAndEntityId(deviceId, entityId);
  }

  public Gateway findById(int id) {
    return gatewayRepo.findById(id).orElse(null);
  }

  public Gateway findByIdAndEntityId(int id, int entityId) {
    return gatewayRepo.findByIdAndEntityId(id, entityId);
  }

  public Gateway addGateway(Map<String, String> newGatewayFields) throws MissingFieldsException,
      InvalidFieldsValuesException {
    if (checkAddEditFields(false, newGatewayFields)) {
      if (gatewayRepo.findByName(newGatewayFields.get("name")) == null) {
        Gateway gateway = new Gateway(newGatewayFields.get("name"));
        return gatewayRepo.save(gateway);
      } else {
        throw new InvalidFieldsValuesException("The gateway with provided name already exists");
      }
    } else {
      throw MissingFieldsException.defaultMessage();
    }
  }

  public Gateway editGateway(int gatewayId, Map<String, String> fieldsToEdit) throws MissingFieldsException,
      InvalidFieldsValuesException {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway == null) {
      throw new InvalidFieldsValuesException("The gateway with provided id is not found");
    } else {
      if (checkAddEditFields(true, fieldsToEdit)) {
        if (gatewayRepo.findByName(fieldsToEdit.get("name")) == null) {
          gateway.setName(fieldsToEdit.get("name"));
          return gatewayRepo.save(gateway);
        } else {
          throw new InvalidFieldsValuesException("The gateway with provided name already exists");
        }
      } else {
        throw MissingFieldsException.defaultMessage();
      }
    }
  }

  public boolean deleteGateway(int gatewayId) throws ElementNotFoundException {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      gatewayRepo.delete(gateway);
      if (!gatewayRepo.existsById(gatewayId)) {
        return true;
      } else {
        return false;
      }
    } else {
      throw ElementNotFoundException.notFoundMessage("gateway");
    }
  }

  private boolean checkAddEditFields(boolean edit, Map<String, String> fields) {
    List<String> allowedFields = new ArrayList<>();
    allowedFields.add("name");

    if (edit) {
      return fields.keySet().stream().anyMatch(allowedFields::contains);
    } else {
      return fields.containsKey("name");
    }
  }
}