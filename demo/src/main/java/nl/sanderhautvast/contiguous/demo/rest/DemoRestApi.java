package nl.sanderhautvast.contiguous.demo.rest;

import lombok.extern.slf4j.Slf4j;
import nl.sanderhautvast.contiguous.ContiguousList;
import nl.sanderhautvast.contiguous.demo.model.Customer;
import nl.sanderhautvast.contiguous.demo.repository.CustomerRepository;
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
    public List<Customer> getCustomers()  {
        try {
            ContiguousList<Customer> customers = customerRepository.getAllCustomers();
            log.info("customers {}", customers.size());
            return customers;
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}
