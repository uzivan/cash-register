package com.example.cashregister.entities;



import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "cash_register_orders_in_services")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderInService {
    @JsonIgnore
    @Id
    @TableGenerator(name = "order_in_services_gen", table = "cash_register_gen_id_orders_in_services", pkColumnName = "gen_name", valueColumnName = "gen_value", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_in_services_gen")
    private int id;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_order")
    private Order order;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_service")
    private Service service;
    private Integer id_order_in_service;

    @Override
    public String toString() {
        return "OrderInService{" +
                "id=" + id +
                ", order=" + order +
                ", service=" + service +
                ", id_order_in_service=" + id_order_in_service +
                '}';
    }
}
