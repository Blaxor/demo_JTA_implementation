package ro.test.demo;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TestServiceClass {

    private final JdbcTemplate jdbc1;
    private final JdbcTemplate jdbc2;

    public TestServiceClass(@Qualifier("primaryJdbcTemplate") JdbcTemplate jdbc1,
                            @Qualifier("secondaryJdbcTemplate") JdbcTemplate jdbc2) {
        this.jdbc1 = jdbc1;
        this.jdbc2 = jdbc2;
    }

    @Transactional
    public void writeBoth(String name1, String name2) {
        jdbc1.update("INSERT INTO t(name) VALUES (?)", name1);
        jdbc2.update("INSERT INTO t(name) VALUES (?)", name2);
    }

    @Transactional
    public void writeThenFail(String name1, String name2) {
        jdbc1.update("INSERT INTO t(name) VALUES (?)", name1);
        // Simulate a failure on the second database write
        jdbc2.update("INSERT INTO t(name) VALUES (?)", name2);

        log.info("Simulating failure after writes");

        throw new TestRunTimeException("Simulated failure after writes");
    }

    @Transactional
    public void complexOperation(String name1, String name2) {
        jdbc1.update("INSERT INTO t(name) VALUES (?)", name1);
        jdbc2.update("INSERT INTO t(name) VALUES (?)", name2);

        jdbc1.update("CREATE TEMPORARY TABLE IF NOT EXISTS audit_log ("
                + "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "operation VARCHAR(255) NOT NULL, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (id)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        jdbc2.update("CREATE TEMPORARY TABLE IF NOT EXISTS jdbc2testtable ("
                + "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "operation VARCHAR(255) NOT NULL, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (id)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        // Additional complex logic can be added here
        throw new TestRunTimeException("Simulated failure during complex operation");
    }
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void lockJdbc2(){
        jdbc2.update("LOCK TABLES t WRITE");
    }
    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public void writeLock(String name1, String name2) {
        log.info("Writing to jdbc 1");
        jdbc1.update("INSERT INTO t(name) VALUES (?)", name1);
        log.info("Writing to jdbc 2");
        jdbc2.update("INSERT INTO t(name) VALUES (?)", name2);

    }

}
