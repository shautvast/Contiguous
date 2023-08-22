package com.github.shautvast.contiguous.benchmark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.shautvast.contiguous.ContiguousList;
import com.github.shautvast.contiguous.ListSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1)
public class JmhBenchmark {

    @org.openjdk.jmh.annotations.State(Scope.Benchmark)
    public static class State {
        //mimick database
        final List<Customer> customers = new ArrayList<>();
        final ObjectMapper jacksonMapper = new ObjectMapper();

        @Setup()
        public void setup() {
            final SimpleModule contiguousListModule = new SimpleModule("contiguous_module");
            contiguousListModule.addSerializer(new ListSerializer());
            jacksonMapper.registerModule(contiguousListModule);

            final RandomStuffGenerator generator = new RandomStuffGenerator();
            for (int i = 0; i < 10_000; i++) {
                Customer customer = new Customer();
                String firstName = generator.generateFirstName();
                String lastName = generator.generateLastName();
                customer.name = firstName + " " + lastName;
                customer.email = firstName + "." + lastName + "@icemail.com";
                customer.streetname = generator.generateStreetName();
                customer.housenumber = generator.generateSomeNumber();
                customer.city = generator.generateSomeCityInIceland();
                customer.country = generator.generateIceland();
                customers.add(customer);
            }
        }
    }

    @Benchmark
    public String contiguous(State state) throws JsonProcessingException {
        //naive mimick read from database and add to ContiguousList
        ContiguousList<Customer> customers = new ContiguousList<>(Customer.class);
        customers.addAll(state.customers);

        return state.jacksonMapper.writeValueAsString(customers);
    }

    @Benchmark
    public String classic(State state) throws JsonProcessingException {
        //naive mimick read from database  and add to ArrayList
        List<Customer> customers = new ArrayList<>();
        customers.addAll(state.customers);

        return state.jacksonMapper.writeValueAsString(customers);
    }
}