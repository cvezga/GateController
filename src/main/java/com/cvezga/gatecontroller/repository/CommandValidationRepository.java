package com.cvezga.gatecontroller.repository;

import com.cvezga.gatecontroller.entity.CommandValidation;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;


public interface CommandValidationRepository extends CrudRepository<CommandValidation, UUID> {


}
