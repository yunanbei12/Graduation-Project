package com.kinetic.sports.bean.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLifecycleMessage {

    private Long orderId;

    private Integer orderType;

    private Integer status;

    private String orderNumber;
}
