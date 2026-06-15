package com.codeGroup.repository;

import com.codeGroup.model.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroRepository extends JpaRepository<Membro, Long> {
}
