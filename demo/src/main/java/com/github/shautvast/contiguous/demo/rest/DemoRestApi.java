package com.github.shautvast.contiguous.demo.rest;

import com.github.shautvast.contiguous.ContiguousList;
import lombok.extern.slf4j.Slf4j;
import com.github.shautvast.contiguous.demo.model.Customer;
import com.github.shautvast.contiguous.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class DemoRestApi {

    private final CustomerRepository customerRepository;

    @Autowired
    public DemoRestApi(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping(value = "/api/customers", produces = "application/json")
    public ContiguousList<Customer> getCustomers() {
        try {
            return customerRepository.getAllCustomers();
        } catch (Exception e) {
            log.error("",e);
            throw e;
        }
    }

    @GetMapping(value = "/api/customers/traditional", produces = "application/json")
    public List<Customer> getCustomersTraditional() {
        return customerRepository.getAllCustomersTraditional();
    }

    @GetMapping(value = "/api/customers/hybrid", produces = "application/json")
    public List<Customer> getCustomersHybrid() {
        return customerRepository.getAllCustomersHybrid();
    }

}
