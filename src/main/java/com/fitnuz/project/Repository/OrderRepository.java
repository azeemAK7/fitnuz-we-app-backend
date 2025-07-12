package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
