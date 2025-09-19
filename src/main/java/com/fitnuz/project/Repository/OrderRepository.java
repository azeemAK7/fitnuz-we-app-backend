package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o")
    Double getTotalRevenue();

    List<Order> findByEmail(String userEmail, Sort sortByAndOrderType);
}

