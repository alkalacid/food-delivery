package com.fooddelivery.user.repository;

import com.fooddelivery.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    List<UserAddress> findByUserId(Long userId);
    
    @Query("SELECT a FROM UserAddress a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<UserAddress> findDefaultByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM UserAddress a WHERE a.id = :id AND a.user.id = :userId")
    Optional<UserAddress> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultForUser(@Param("userId") Long userId);
    
    long countByUserId(Long userId);
}

