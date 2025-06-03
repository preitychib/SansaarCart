package com.preeti.sansarcart.repository;


import com.preeti.sansarcart.entity.Cart;
import com.preeti.sansarcart.entity.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
}
