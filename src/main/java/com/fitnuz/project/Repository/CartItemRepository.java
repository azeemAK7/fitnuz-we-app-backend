package com.fitnuz.project.Repository;


import com.fitnuz.project.Model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    CartItem findCartItemByProductIdAndCartId(Long cartId,Long poductId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1")
    List<CartItem> findCartItemByCartId(Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);


    @Modifying
    @Query("SELECT ci FROM CartItem ci WHERE ci.product.productId = ?1")
    List<CartItem> findByProductId(Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1")
    List<CartItem> findByCartId(Long cartId);



    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1")
    void deleteAllItemByCartId(Long cartId);
}
