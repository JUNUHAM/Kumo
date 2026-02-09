package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.ApplicationEntity;
import net.kumo.kumo.domain.entity.Enum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

}
