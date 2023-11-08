package com.example.cashregister.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="cash_register_orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    @TableGenerator(name = "order_gen", table = "cash_register_gen_id_orders", pkColumnName = "gen_name", valueColumnName = "gen_value", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_gen")
    private int id;
    private String client_name;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderInService> orderInServices = null;
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", client_name='" + client_name + '\'' +
                ", orderInServices=" + orderInServices +
                '}';
    }
}
