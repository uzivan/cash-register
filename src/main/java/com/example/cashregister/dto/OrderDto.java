package com.example.cashregister.dto;

import com.example.cashregister.dto_validation.custom_constraints.NotEmptyObject;
import com.example.cashregister.dto_validation.groups.OnCreate;
import com.example.cashregister.dto_validation.groups.OnUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@NotEmptyObject(groups = {OnCreate.class, OnUpdate.class})
public class OrderDto {
    @NotNull(groups = {OnCreate.class})
    private String client_name;
    @NotNull(groups = {OnCreate.class})
    @Size(min = 1, groups = {OnCreate.class, OnUpdate.class})
    private Map<
            @NotNull @Min(value = 1, groups = {OnCreate.class, OnUpdate.class}) Integer,
//            @Size(min = 1, groups = {OnCreate.class, OnUpdate.class})
            @NotNull @Min(value = 1, groups = {OnCreate.class, OnUpdate.class}) Integer[]>
//                    List<@NotNull @Min(value = 1, groups = {OnCreate.class, OnUpdate.class}) Integer>>
            productId;
}
