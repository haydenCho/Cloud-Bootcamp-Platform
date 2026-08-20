package com.solcho.bootcamp.unit.repository;

import com.solcho.bootcamp.unit.entity.Unit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findAllByOrderBySortOrderAsc();

    Optional<Unit> findByCode(String code);

    boolean existsByCode(String code);
}
