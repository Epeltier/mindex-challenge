package com.mindex.challenge.service.impl;


import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceImplUnitTest {


    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    public EmployeeServiceImpl employeeService;

    @Test(expected = ResponseStatusException.class)
    public void getReportingStructureGivenInvalidId(){
        Mockito.when(employeeRepository.findByEmployeeId("12345")).thenReturn(null);
        employeeService.getReportingStructure("12345");
    }

    @Test
    public void getReportingStructureGivenNoReports(){
        Mockito.when(employeeRepository.findByEmployeeId("54321")).thenReturn(getTestEmployee("54321"));
        ReportingStructure reportingStructure = employeeService.getReportingStructure("54321");

        assertEquals(Integer.valueOf(0),reportingStructure.getNumberOfReports());
    }

    @Test
    public void getReportingStructureGivenOneDirectReport(){
        Employee employee1 = getTestEmployee("54321");
        Employee employee2 = getTestEmployee("123");
        employee1.setDirectReports(new ArrayList<>(Collections.singletonList(employee2)));

        Mockito.when(employeeRepository.findByEmployeeId("54321")).thenReturn(employee1);
        Mockito.when(employeeRepository.findByEmployeeId("123")).thenReturn(employee2);
        ReportingStructure reportingStructure = employeeService.getReportingStructure("54321");

        assertEquals(Integer.valueOf(1),reportingStructure.getNumberOfReports());
    }

    @Test
    public void getReportingStructureGivenCyclicalReport(){
        Employee employee1 = getTestEmployee("54321");
        Employee employee2 = getTestEmployee("123");
        employee1.setDirectReports(new ArrayList<>(Collections.singletonList(employee2)));
        employee2.setDirectReports(new ArrayList<>(Collections.singletonList(employee1)));

        Mockito.when(employeeRepository.findByEmployeeId("54321")).thenReturn(employee1);
        Mockito.when(employeeRepository.findByEmployeeId("123")).thenReturn(employee2);

        boolean exceptionOcurred = false;
        try {
             employeeService.getReportingStructure("54321");
        }
        catch(RuntimeException e){
            exceptionOcurred = true;
            assertEquals("Cyclical reference found in employee reporting hierarchy. Id: 54321", e.getMessage());
        }

        assertTrue(exceptionOcurred);
    }

    private Employee getTestEmployee(String id) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setFirstName("first");
        employee.setLastName("last");
        return employee;
    }
}
