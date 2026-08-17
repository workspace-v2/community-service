package com.stocat.amumal.stock.repository;

import com.stocat.amumal.stock.domain.StockSyncCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockSyncCheckpointRepository extends JpaRepository<StockSyncCheckpoint, String> {}
