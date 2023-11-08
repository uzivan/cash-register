package com.example.cashregister.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="cash_register_services")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Service {
    @Id
    private int id;
    private String name;
    @JsonIgnore
    private String url;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String token;
    @JsonIgnore
    private String url_for_get_all_products;
    @JsonIgnore
    private String url_for_get_product_by_id;
    @JsonIgnore
    private String url_for_get_order_by_id;
    @JsonIgnore
    private String name_for_id_list;
    @JsonIgnore
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<OrderInService> orderInServices;
}
