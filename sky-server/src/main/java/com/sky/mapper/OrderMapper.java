package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询历史订单数据
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 修改订单状态、支付状态和支付时间
     *
     * @param orderStatus     订单状态
     * @param orderPaidStatus 支付状态
     * @param checkoutTime    支付时间
     * @param id              订单 ID
     */
    @Update("update orders " +
            "set status = #{orderStatus}, " +
            "pay_status = #{orderPaidStatus}, " +
            "checkout_time = #{checkoutTime} " +
            "where id = #{id}")
    void updateStatus(
            @Param("orderStatus") Integer orderStatus,
            @Param("orderPaidStatus") Integer orderPaidStatus,
            @Param("checkoutTime") LocalDateTime checkoutTime,
            @Param("id") Long id
    );
}
