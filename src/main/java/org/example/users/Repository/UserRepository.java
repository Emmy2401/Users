package org.example.users.Repository;

import org.example.users.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    <Optional> User findByUsername(String username);
    <Optional> User findById(Long id);
}
