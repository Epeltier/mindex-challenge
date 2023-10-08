package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;
    @Autowired
    private EmployeeService employeeService;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation of employee [{}]", compensation.getEmployeeId());
        validate(compensation);
        compensationRepository.insert(compensation);
        return compensation;
    }

    @Override
    public Compensation read(String employeeId) {
        LOG.debug("Reading compensation of employee [{}]", employeeId);

        Compensation compensation = compensationRepository.findByEmployeeId(employeeId);

        if (compensation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No compensation exists for employee id: " + employeeId);
        }

        return compensation;
    }

    private void validate(Compensation compensation){

        try{
            employeeService.read(compensation.getEmployeeId());
        }
        catch(ResponseStatusException exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An employee of Id: " +compensation.getEmployeeId() + " does not exist to setup compensation for." );
        }

        Compensation existingCompensation = compensationRepository.findByEmployeeId(compensation.getEmployeeId());
        if(existingCompensation !=null ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A compensation already exists for employee id:" + compensation.getEmployeeId() );
        }
    }
}
