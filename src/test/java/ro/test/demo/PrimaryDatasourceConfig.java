package ro.test.demo;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
@Configuration
public class PrimaryDatasourceConfig {

    @Primary
    @Bean(name = "primaryDataSource", initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "primary.datasource")
    public DataSource primaryDataSource() {
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        java.util.Properties xaProps = new java.util.Properties();
        xaProps.setProperty("user", "deiu");
        xaProps.setProperty("password", "password");
        xaProps.setProperty("URL", "jdbc:mysql://localhost:3306/db1");
        ds.setXaProperties(xaProps);
        ds.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");
        return ds;
    }

    @Primary
    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        return new JdbcTemplate(primaryDataSource);
    }
}
