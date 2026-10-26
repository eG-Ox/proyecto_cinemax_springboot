package com.iestp.sistemagestion.dao;
import com.iestp.sistemagestion.modelo.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RegistroRepository extends JpaRepository<Registro, Long> { }
