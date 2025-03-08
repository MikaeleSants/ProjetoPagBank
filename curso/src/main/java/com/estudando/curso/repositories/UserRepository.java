package com.estudando.curso.repositories;

import com.estudando.curso.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
