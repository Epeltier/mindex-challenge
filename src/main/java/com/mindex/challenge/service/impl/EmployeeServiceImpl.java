package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Reading employee with id [{}]", id);
        return getEmployeeById(id);
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingStructure(String id) {
        LOG.debug("Generating reporting structure of employee with id [{}]", id);

        ReportingStructure reportingStructure = new ReportingStructure();
        Employee employee = getEmployeeById(id);
        reportingStructure.setEmployee(employee);
        Integer numberOfReports = fillEmployeeReports(employee, new HashSet<String>()).size();
        reportingStructure.setNumberOfReports(numberOfReports);

        return reportingStructure;
    }

    private Set<String> fillEmployeeReports(Employee employee, Set<String> visitedIds){
        if(employee.getDirectReports() == null){
            return visitedIds;
        }

        //Using index based iteration to replace each employee in directReports collection with its DB-backed value
        for(int i = 0; i<employee.getDirectReports().size(); i++){

            String employeeId = employee.getDirectReports().get(i).getEmployeeId();
            if(visitedIds.contains(employeeId)){
                throw new RuntimeException("Cyclical reference found in employee reporting hierarchy. Id: " + employee.getEmployeeId());
            }
            visitedIds.add(employeeId);
            Employee currentEmployee = employeeRepository.findByEmployeeId(employeeId);

            if (currentEmployee == null) {
                throw new RuntimeException("Invalid employeeId: " + employeeId);
            }

            employee.getDirectReports().set(i,currentEmployee);
            //recursively DFS through the reporting structure.
            fillEmployeeReports(currentEmployee, visitedIds);
        }

        return visitedIds;
    }

    private Employee getEmployeeById(String id){
        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid employeeId: " + id);
        }
        return employee;
    }
}
