package com.github.shautvast.contiguous.demo.repository;


import com.github.shautvast.contiguous.ContiguousList;
import com.github.shautvast.contiguous.JdbcResults;
import com.github.shautvast.contiguous.demo.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ContiguousList<Customer> getAllCustomers() {
        return jdbcTemplate.query("select * from customers limit 10000", rs -> {
            return JdbcResults.toList(rs, Customer.class);
        });
    }

    public List<Customer> getAllCustomersTraditional() {
        return jdbcTemplate.query("select * from customers limit 10000", (rs, rowNum) -> new Customer(
                rs.getString("name"),rs.getString("email"),
                rs.getString("streetname"), rs.getInt("housenumber"),
                rs.getString("city"), rs.getString("country")
        ));
    }

    public List<Customer> getAllCustomersHybrid() {
        return jdbcTemplate.query("select * from customers", rs -> {
            return JdbcResults.toList(rs, Customer.class);
        });
    }
}
