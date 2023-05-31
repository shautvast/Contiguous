package nl.sanderhautvast.contiguous.demo;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import nl.sanderhautvast.contiguous.ListSerializer;
import nl.sanderhautvast.contiguous.demo.repository.RandomStuffGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@SpringBootApplication
public class DemoApplication {
    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driverClass}")
    private String driverClass;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            log.info("Loading the database with test data");
            JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource());
            jdbcTemplate.execute("drop table if exists customers");
            jdbcTemplate.execute("create table customers (name varchar(100), email varchar(100), streetname varchar(100), housenumber integer, city varchar(100), country varchar(100))");
            final RandomStuffGenerator generator = new RandomStuffGenerator();
            for (int i = 0; i < 10_000; i++) {
                jdbcTemplate.update("insert into customers (name, email, streetname, housenumber, city, country) values(?,?,?,?,?,?)",
                        ps -> {
                            String firstName = generator.generateFirstName();
                            String lastName = generator.generateLastName();
                            ps.setString(1, firstName + " " + lastName);
                            ps.setString(2, firstName + "." + lastName + "@icemail.com");
                            ps.setString(3, generator.generateStreetName());
                            ps.setInt(4, generator.generateSomeNumber());
                            ps.setString(5, generator.generateSomeCityInIceland());
                            ps.setString(6, generator.generateIceland());
                        });
            }
            log.info("Database loading finished successfully");
        };
    }

    @Bean
    public Module jacksonModule() {
        final SimpleModule module = new SimpleModule("contiguous_module");
        module.addSerializer(new ListSerializer());
        return module;
    }


    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(datasource());
    }

    @Bean
    public DataSource datasource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(driverClass);
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }
}
