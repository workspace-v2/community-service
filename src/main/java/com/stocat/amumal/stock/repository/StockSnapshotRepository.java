package com.stocat.amumal.stock.repository;

import com.stocat.amumal.stock.domain.StockSnapshot;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockSnapshotRepository extends JpaRepository<StockSnapshot, String> {

  List<StockSnapshot> findAllBySymbolIn(Collection<String> symbols);

  List<StockSnapshot> findAllByName(String name, Pageable pageable);
}
