package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeReportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        employeeReportingStructureUrl = "http://localhost:" + port + "/employee/{id}/reporting-structure";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }


    @Test
    public void testReportingStructureGivenNoDirectReports() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();
        ReportingStructure reportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, createdEmployee.getEmployeeId()).getBody();


        assertEquals(Integer.valueOf(0), reportingStructure.getNumberOfReports());
        assertEmployeeEquivalence(createdEmployee, reportingStructure.getEmployee());
    }

    @Test
    public void testReportingStructureGivenLayedReports() {
        Employee testEmployee1 = new Employee();
        testEmployee1.setFirstName("George");
        testEmployee1.setLastName("Washington");
        testEmployee1.setDepartment("Engineering");
        testEmployee1.setPosition("Developer");

        Employee testEmployee2 = new Employee();
        testEmployee2.setFirstName("Thomas");
        testEmployee2.setLastName("Jefferson");
        testEmployee2.setDepartment("Engineering");
        testEmployee2.setPosition("Developer");
        testEmployee2.setDirectReports(new ArrayList<>(Collections.singletonList(new Employee())));

        Employee testEmployee3 = new Employee();
        testEmployee3.setFirstName("John");
        testEmployee3.setLastName("Adams");
        testEmployee3.setDepartment("Engineering");
        testEmployee3.setPosition("Developer");
        testEmployee3.setDirectReports(new ArrayList<>(Collections.singletonList(new Employee())));


        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        //set employee 2 as manager of employee 1
        testEmployee2.getDirectReports().get(0).setEmployeeId(createdEmployee1.getEmployeeId());

        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee2, Employee.class).getBody();
        //set employee 3 as manager of employee 2
        testEmployee3.getDirectReports().get(0).setEmployeeId(createdEmployee2.getEmployeeId());

        Employee createdEmployee3 = restTemplate.postForEntity(employeeUrl, testEmployee3, Employee.class).getBody();

        ReportingStructure reportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, createdEmployee3.getEmployeeId()).getBody();

        assertEquals(Integer.valueOf(2), reportingStructure.getNumberOfReports());
        assertEmployeeEquivalence(testEmployee3, reportingStructure.getEmployee());
        assertEmployeeEquivalence(testEmployee2, reportingStructure.getEmployee().getDirectReports().get(0));
        assertEmployeeEquivalence(testEmployee1, reportingStructure.getEmployee().getDirectReports().get(0).getDirectReports().get(0));
    }

    @Test
    public void testReportingStructureGivenCyclicalReports() {
        Employee testEmployee1 = new Employee();
        testEmployee1.setFirstName("George");
        testEmployee1.setLastName("Washington");
        testEmployee1.setDepartment("Engineering");
        testEmployee1.setPosition("Developer");

        Employee testEmployee2 = new Employee();
        testEmployee2.setFirstName("Thomas");
        testEmployee2.setLastName("Jefferson");
        testEmployee2.setDepartment("Engineering");
        testEmployee2.setPosition("Developer");
        testEmployee2.setDirectReports(new ArrayList<>(Collections.singletonList(new Employee())));

        Employee testEmployee3 = new Employee();
        testEmployee3.setFirstName("John");
        testEmployee3.setLastName("Adams");
        testEmployee3.setDepartment("Engineering");
        testEmployee3.setPosition("Developer");
        testEmployee3.setDirectReports(new ArrayList<>(Collections.singletonList(new Employee())));


        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        //set employee 2 as manager of employee 1
        testEmployee2.getDirectReports().get(0).setEmployeeId(createdEmployee1.getEmployeeId());

        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee2, Employee.class).getBody();
        //set employee 3 as manager of employee 2
        testEmployee3.getDirectReports().get(0).setEmployeeId(createdEmployee2.getEmployeeId());

        Employee createdEmployee3 = restTemplate.postForEntity(employeeUrl, testEmployee3, Employee.class).getBody();

        //set employee 1 as manager of employee 3, creating cyclical reference.
        createdEmployee1.setDirectReports(new ArrayList<>(Collections.singletonList(new Employee())));
        createdEmployee1.getDirectReports().get(0).setEmployeeId(createdEmployee3.getEmployeeId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(createdEmployee1, headers),
                        Employee.class,
                        createdEmployee1.getEmployeeId()).getBody();

        ResponseEntity<String> response = restTemplate.getForEntity(employeeReportingStructureUrl, String.class, createdEmployee1.getEmployeeId());
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Cyclical reference found in employee reporting hierarchy"));
    }


    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
