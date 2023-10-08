package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
//I landed on creating a new top level resource for Compensation rather than sub-resourcing in Employee (Ex /employee/{id}/compensation)
//for future extensibility of the data type. Ex - querying or updating multiple employee compensations.
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(com.mindex.challenge.controller.CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    @PostMapping("/compensation")
    public Compensation create(@RequestBody @Valid Compensation compensation) {
        LOG.debug("Received compensation create request for employee [{}]", compensation);
        return compensationService.create(compensation);
    }

    @GetMapping("/compensation")
    //query parameter ?employeeId={id} to retrieve compensation by employeeId.
    public Compensation read(@RequestParam @NotNull String employeeId) {
        LOG.debug("Received compensation read request for employee id [{}]", employeeId);

        return compensationService.read(employeeId);
    }
}
