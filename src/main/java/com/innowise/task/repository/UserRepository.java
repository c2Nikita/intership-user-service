package com.innowise.task.repository;

import com.innowise.task.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.name = :name, u.surname = :surname WHERE u.id = :id")
    int updateNameAndSurnameById(@Param("id") Long id,
                                  @Param("name") String name,
                                  @Param("surname") String surname);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE users SET active = :active WHERE id = :id", nativeQuery = true)
    int setActiveStatus(@Param("id") Long id, @Param("active") boolean active);

}
