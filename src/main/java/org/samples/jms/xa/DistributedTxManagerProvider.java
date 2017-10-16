package org.samples.jms.xa;

import com.atomikos.icatch.jta.UserTransactionManager;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

public class DistributedTxManagerProvider {
    private static DistributedTxManagerProvider distributedTxManagerProvider = null;
    private TransactionManager transactionManager;

    protected DistributedTxManagerProvider() {
        try {
            UserTransactionManager utm = new UserTransactionManager();
            utm.init();
            this.transactionManager = utm;
        } catch (SystemException e) {
            System.out.println("Error " + e);
        }
    }

    public static DistributedTxManagerProvider getInstance() {
        if (distributedTxManagerProvider == null) {
            synchronized (DistributedTxManagerProvider.class) {
                if (distributedTxManagerProvider == null) {
                    distributedTxManagerProvider = new DistributedTxManagerProvider();
                }
            }
        }
        return distributedTxManagerProvider;
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
