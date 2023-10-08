package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CompensationServiceImplUnitTest {

    @Mock
    private CompensationRepository compensationRepository;
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    public CompensationServiceImpl compensationService;


    @Test(expected = ResponseStatusException.class)
    public void getCompensationGivenInvalidId(){
        Mockito.when(compensationRepository.findByEmployeeId("12345")).thenReturn(null);
        compensationService.read("12345");
    }

    @Test
    public void readGivenExistingCompensation(){
        Compensation compensation = new Compensation();
        compensation.setEmployeeID("54321");
        compensation.setSalary(BigDecimal.valueOf(50000));
        compensation.setEffectiveDate(LocalDate.of(2023, 10, 8));

        Mockito.when(compensationRepository.findByEmployeeId("54321")).thenReturn(compensation);
        Compensation acutalCompensation =  compensationService.read("54321");

        assertEquals(compensation.getEmployeeId(), acutalCompensation.getEmployeeId());
        assertEquals(compensation.getSalary(), acutalCompensation.getSalary());
        assertEquals(compensation.getEffectiveDate(), acutalCompensation.getEffectiveDate());

    }

    @Test(expected = ResponseStatusException.class)
    public void getReportingStructureGivenInvalidId(){
        Mockito.when(compensationRepository.findByEmployeeId("12345")).thenReturn(null);
        compensationService.read("12345");
    }


    @Test
    public void createCompensation(){
        Compensation compensation = new Compensation();
        compensation.setEmployeeID("54321");
        compensation.setSalary(BigDecimal.valueOf(50000));
        compensation.setEffectiveDate(LocalDate.of(2023, 10, 8));

        //mock returns employee that exists
        Mockito.when(employeeService.read("54321")).thenReturn(new Employee());
        Mockito.when(compensationRepository.findByEmployeeId("54321")).thenReturn(null);

        Compensation createdCompensation = compensationService.create(compensation);

        assertEquals(compensation.getEmployeeId(), createdCompensation.getEmployeeId());
        assertEquals(compensation.getSalary(), createdCompensation.getSalary());
        assertEquals(compensation.getEffectiveDate(), createdCompensation.getEffectiveDate());
    }

    @Test(expected = ResponseStatusException.class)
    public void createCompensationGivenEmployeeIdDoesntExist(){
        Compensation compensation = new Compensation();
        compensation.setEmployeeID("54321");
        compensation.setSalary(BigDecimal.valueOf(50000));

        Mockito.when(employeeService.read("54321")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        compensationService.create(compensation);
    }
}
