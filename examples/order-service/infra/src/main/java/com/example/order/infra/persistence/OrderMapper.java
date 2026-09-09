package com.example.order.infra.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order 持久化对象的 MyBatis-Plus Mapper。
 *
 * @author GuoYang create on 2026/9/7 15:30
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderPo> {}
