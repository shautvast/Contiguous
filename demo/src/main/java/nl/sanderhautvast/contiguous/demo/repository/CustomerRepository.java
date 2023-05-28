package nl.sanderhautvast.contiguous.demo.repository;


import lombok.extern.slf4j.Slf4j;
import nl.sanderhautvast.contiguous.ContiguousList;
import nl.sanderhautvast.contiguous.JdbcResults;
import nl.sanderhautvast.contiguous.demo.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ContiguousList<Customer> getAllCustomers() {
        return jdbcTemplate.query("select * from customers limit 5", rs -> {
            return JdbcResults.toList(rs, Customer.class);
        });
    }
}
