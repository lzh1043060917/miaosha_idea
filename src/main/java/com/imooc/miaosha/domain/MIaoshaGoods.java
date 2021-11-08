package com.imooc.miaosha.domain;

import java.util.Date;

import lombok.Data;

@Data
public class MIaoshaGoods {
    private Long id;
    private Long goodsId;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
