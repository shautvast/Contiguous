package nl.sanderhautvast.contiguous.demo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer {
    String name;
    String email;
    String streetname;
    int housenumber;
    String city;
    String country;
}
