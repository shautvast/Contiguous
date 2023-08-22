package com.github.shautvast.contiguous.benchmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* copied from demo, need common module */
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
