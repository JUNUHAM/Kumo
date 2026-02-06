package net.kumo.kumo.repository;

import net.kumo.kumo.domain.entity.TokyoNoGeocodedEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface TokyoNoGeocodedRepository extends BaseRepository<TokyoNoGeocodedEntity> {

    // 특별히 추가할 게 없다면 비워둬도 됨
}