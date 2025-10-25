package ro.test.demo;

import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class ConfigTransactionalManager {

        @Bean
        public JtaTransactionManager jtaTransactionManager(UserTransaction userTransaction, TransactionManager transactionManager) {
            JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
            jtaTransactionManager.setUserTransaction(userTransaction);
            jtaTransactionManager.setTransactionManager(transactionManager);
            return jtaTransactionManager;
        }

        @Bean
        public UserTransaction userTransaction() throws Exception {
            return new com.atomikos.icatch.jta.UserTransactionImp();
        }

        @Bean
        public TransactionManager atomikosTransactionManager() {
            return new UserTransactionManager();
        }

}
