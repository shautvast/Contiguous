package nl.sanderhautvast.contiguous.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    String name;
    String email;
    String streetname;
    int housenumber;
    String city;
    String country;
}
