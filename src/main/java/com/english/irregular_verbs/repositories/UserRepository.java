package com.english.irregular_verbs.repositories;

import com.english.irregular_verbs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
