package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
