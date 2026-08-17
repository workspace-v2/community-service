package com.stocat.amumal.stock.config;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stock-sync")
public record StockSyncProperties(URI baseUrl, int batchSize) {}
