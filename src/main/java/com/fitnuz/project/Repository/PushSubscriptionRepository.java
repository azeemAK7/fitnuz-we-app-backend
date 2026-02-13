package com.fitnuz.project.Repository;

import com.fitnuz.project.Model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserEmail(String email);

    void deleteByEndpoint(String endpoint);

    boolean existsByEndpoint(String endpoint);
}
