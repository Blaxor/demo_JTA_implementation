package ro.test.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        classes = { DemoApplication.class, PrimaryDatasourceConfig.class, SecondDatabaseConfig.class }
)
class DistributedTransactionTests {

    @Autowired
    private TestServiceClass service;

    @Autowired @Qualifier("primaryJdbcTemplate")
    private JdbcTemplate jdbc1;

    @Autowired @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate jdbc2;

    @BeforeEach
    void createTable(){
        jdbc1.update("CREATE TABLE IF NOT EXISTS `t` ("
                + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "`name` VARCHAR(255) NOT NULL, "
                + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (`id`), KEY `idx_t_name` (`name`)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        jdbc2.update("CREATE TABLE IF NOT EXISTS `t` ("
                + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                + "`name` VARCHAR(255) NOT NULL, "
                + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "PRIMARY KEY (`id`), KEY `idx_t_name` (`name`)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

    }

    @BeforeEach
    void clean() {

        jdbc1.update("TRUNCATE TABLE t");
        jdbc2.update("TRUNCATE TABLE t");
    }

    @Test
    void commitAcrossBothDatabases() {
        service.writeBoth("A1", "A2");
        Integer c1 = jdbc1.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "A1");
        Integer c2 = jdbc2.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "A2");
        assertEquals(1, c1);
        assertEquals(1, c2);
    }

    @Test
    void rollbackWhenFailureOccurs() {
        assertThrows(TestRunTimeException.class, () -> service.writeThenFail("B1", "B2"));
        // After rollback, neither insert should be visible
        Integer c1 = jdbc1.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "B1");
        Integer c2 = jdbc2.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "B2");
        assertEquals(0, c1);
        assertEquals(0, c2);
    }
    @Test
    void testWithTempTable(){
        assertThrows(TestRunTimeException.class, () -> service.complexOperation("C1", "C2"));
        // After rollback, neither insert should be visible
        Integer c1 = jdbc1.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "C1");
        Integer c2 = jdbc2.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "C2");
        assertEquals(0, c1);
        assertEquals(0, c2);
    }

    @Test
    void testWithWriteLock(){
        try {
            service.lockJdbc2();
            service.writeLock("C1", "C2");
        }catch (Exception e){
            e.printStackTrace();
        }
        // After rollback, neither insert should be visible
        Integer c1 = jdbc1.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "C1");
        Integer c2 = jdbc2.queryForObject("SELECT COUNT(*) FROM t WHERE name=?", Integer.class, "C2");
        assertEquals(0, c1);
        assertEquals(0, c2);
    }
}
