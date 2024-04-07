package book.store.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMysqlContainer extends MySQLContainer<CustomMysqlContainer> {
    private static final String DB_IMAGE = "mysql:8.0.33";

    private static CustomMysqlContainer mysqlContainer;

    private CustomMysqlContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomMysqlContainer getInstance() {
        if (mysqlContainer == null) {
            mysqlContainer = new CustomMysqlContainer();
        }
        return mysqlContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", mysqlContainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", mysqlContainer.getUsername());
        System.setProperty("TEST_DB_PASSWORD", mysqlContainer.getPassword());
    }

    @Override
    public void stop() {

    }
}
