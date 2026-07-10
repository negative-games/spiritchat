package gg.moonrise.chat.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.moonrise.chat.SpiritChatPlugin;
import gg.moonrise.chat.config.section.database.DatabaseSettings;
import gg.moonrise.chat.storage.SqlDialect;
import gg.moonrise.chat.storage.SqlSchema;
import gg.moonrise.chat.storage.SqlStorageSizing;
import gg.moonrise.engine.state.Reloadable;
import gg.moonrise.moss.spring.Disableable;
import gg.moonrise.moss.spring.SpringComponent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringComponent
@RequiredArgsConstructor
public class SqlStorageService implements Disableable, Reloadable {

    private final SpiritChatPlugin plugin;
    private final ConfigService configService;

    private final AtomicReference<StorageResources> resources = new AtomicReference<>();
    private final ThreadLocal<StorageResources> taskResources = new ThreadLocal<>();

    @PostConstruct
    public synchronized void init() {
        connect();
    }

    @Override
    public synchronized void reload() {
        connect();
    }

    public boolean isAvailable() {
        StorageResources current = resources.get();
        return current != null && current.canSchedule();
    }

    public SqlDialect dialect() {
        StorageResources current = currentResources();
        if (current == null || !current.hasOpenDataSource()) {
            throw new IllegalStateException("SQL storage is not available");
        }
        return current.dialect();
    }

    public Connection connection() throws SQLException {
        StorageResources current = currentResources();
        if (current == null || !current.hasOpenDataSource()) {
            throw new SQLException("SQL data source is not available");
        }
        return current.dataSource().getConnection();
    }

    public <T> CompletableFuture<T> supplyAsync(SqlSupplier<T> supplier) {
        StorageResources current = resources.get();
        if (current == null || !current.canSchedule()) {
            return CompletableFuture.failedFuture(new IllegalStateException("SQL storage is not available"));
        }

        try {
            return CompletableFuture.supplyAsync(() -> {
                taskResources.set(current);
                try {
                    return supplier.get();
                } catch (SQLException exception) {
                    throw new RuntimeException(exception);
                } finally {
                    taskResources.remove();
                }
            }, current.executor());
        } catch (RejectedExecutionException exception) {
            return CompletableFuture.failedFuture(new IllegalStateException("SQL executor is not available", exception));
        }
    }

    private StorageResources currentResources() {
        StorageResources current = taskResources.get();
        return current == null ? resources.get() : current;
    }

    public CompletableFuture<Void> runAsync(SqlRunnable runnable) {
        return supplyAsync(() -> {
            runnable.run();
            return null;
        });
    }

    private synchronized void connect() {
        DatabaseSettings settings = configService.get().getDatabaseSettings();
        SqlDialect newDialect = SqlDialect.from(settings.getType());
        HikariDataSource newDataSource = null;
        ExecutorService newExecutor = null;

        try {
            HikariConfig hikariConfig = hikariConfig(settings, newDialect);
            newDataSource = new HikariDataSource(hikariConfig);
            createTables(newDataSource, newDialect);
            newExecutor = Executors.newFixedThreadPool(
                    SqlStorageSizing.workerThreads(settings, newDialect),
                    threadFactory(newDialect)
            );

            StorageResources oldResources = resources.getAndSet(new StorageResources(newDataSource, newExecutor, newDialect));
            closeResources(oldResources);

            log.info("Connected to SpiritChat {} storage.", newDialect);
        } catch (SQLException | RuntimeException exception) {
            if (isAvailable()) {
                log.error("Failed to reconnect SpiritChat {} storage. Keeping the previous storage connection.", newDialect, exception);
            } else {
                log.error("Failed to initialize SpiritChat {} storage. Storage-backed features will use config defaults.", newDialect, exception);
            }
            closeExecutor(newExecutor);
            closeDataSource(newDataSource);
        }
    }

    private HikariConfig hikariConfig(DatabaseSettings settings, SqlDialect dialect) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SpiritChat-" + dialect);
        config.setConnectionTimeout(10_000L);
        config.setValidationTimeout(5_000L);

        switch (dialect) {
            case SQLITE -> {
                plugin.getDataFolder().mkdirs();
                File databaseFile = new File(plugin.getDataFolder(), settings.getDatabase());
                File parent = databaseFile.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                config.setDriverClassName("org.sqlite.JDBC");
                config.setMaximumPoolSize(SqlStorageSizing.connectionPoolSize(settings, dialect));
            }
            case MYSQL -> {
                config.setJdbcUrl("jdbc:mysql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabase());
                config.setUsername(settings.getUsername());
                config.setPassword(settings.getPassword());
                config.setMaximumPoolSize(SqlStorageSizing.connectionPoolSize(settings, dialect));
            }
            case MARIADB -> {
                config.setJdbcUrl("jdbc:mariadb://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabase());
                config.setUsername(settings.getUsername());
                config.setPassword(settings.getPassword());
                config.setMaximumPoolSize(SqlStorageSizing.connectionPoolSize(settings, dialect));
            }
            case POSTGRESQL -> {
                config.setJdbcUrl("jdbc:postgresql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabase());
                config.setUsername(settings.getUsername());
                config.setPassword(settings.getPassword());
                config.setMaximumPoolSize(SqlStorageSizing.connectionPoolSize(settings, dialect));
            }
        }

        return config;
    }

    private void createTables(HikariDataSource source, SqlDialect dialect) throws SQLException {
        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : SqlSchema.connectionStatements(dialect)) {
                statement.execute(sql);
            }
            for (String sql : SqlSchema.tableStatements(dialect)) {
                statement.execute(sql);
            }
            for (String sql : SqlSchema.indexStatements(dialect)) {
                statement.execute(sql);
            }
        }
    }

    private ThreadFactory threadFactory(SqlDialect dialect) {
        AtomicInteger index = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "SpiritChat-SQL-" + dialect + "-" + index.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private synchronized void closeResources() {
        closeResources(resources.getAndSet(null));
    }

    private void closeResources(StorageResources current) {
        if (current == null) return;

        try {
            current.executor().execute(() -> closeDataSource(current.dataSource()));
        } catch (RejectedExecutionException exception) {
            closeDataSource(current.dataSource());
        } finally {
            closeExecutor(current.executor());
        }
    }

    private void closeExecutor(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void closeDataSource(HikariDataSource source) {
        if (source != null && !source.isClosed()) {
            source.close();
        }
    }

    @Override
    public void onDisable() {
        closeResources();
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {
        T get() throws SQLException;
    }

    @FunctionalInterface
    public interface SqlRunnable {
        void run() throws SQLException;
    }

    private record StorageResources(HikariDataSource dataSource, ExecutorService executor, SqlDialect dialect) {

        private boolean canSchedule() {
            return dataSource != null && !dataSource.isClosed() && executor != null && !executor.isShutdown();
        }

        private boolean hasOpenDataSource() {
            return dataSource != null && !dataSource.isClosed();
        }
    }
}
