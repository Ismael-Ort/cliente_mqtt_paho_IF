package com.weathernet.repositories;

import com.weathernet.models.AlertaRegla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaReglaRepository extends JpaRepository<AlertaRegla, Long> {
}