package com.imooc.miaosha.vo;

import java.util.Date;

import com.imooc.miaosha.domain.Goods;

import lombok.Data;

@Data // 为了查询出miaosha_goods和goods俩表的信息，所以要有这样一个类
public class GoodsVo extends Goods {
    private Double miaoshaPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
