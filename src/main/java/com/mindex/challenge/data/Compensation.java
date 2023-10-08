package com.mindex.challenge.data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Compensation {
    @NotNull
    private String employeeId;
    @NotNull
    private BigDecimal salary;
    private LocalDate effectiveDate;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeID(String employeeId) {
        this.employeeId = employeeId;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
