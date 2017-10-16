package org.samples.jms.xa;

import java.util.Properties;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class XAPublisherSample {
    public static final String NAMING_FACTORY_INITIAL = "java.naming.factory.initial";

    public static final String PROVIDER_URL = "java.naming.provider.url";

    public static final String TOPIC_PREFIX = "topic.";

    public static final String QUEUE_PREFIX = "queue.";

    private static final String connectionFac = "XAConnectionFactory";

    private static final String queueName = "MyXAQueue";

    private static final String url = "tcp://localhost:61616";

    private static final String broker = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";

    public static void main(String[] args) throws Exception {
        xaManagerPublish();
    }

    public static void xaManagerPublish() throws Exception {
        InitialContext initialContext = getInitialContext();

        XAConnectionFactory connectionFactory = (XAConnectionFactory) initialContext.lookup(connectionFac);

        Destination xaTestQueue = (Destination) initialContext.lookup(queueName);

        XAConnection xaConnection = connectionFactory.createXAConnection();
        xaConnection.start();
        XASession xaSession = xaConnection.createXASession();

        // Get XAResource and JMS session from the XASession. XAResource is given to
        // the TM and JMS Session is given to the AP.
        XAResource xaResource = xaSession.getXAResource();

        DistributedTxManagerProvider distributedTxManagerProvider = DistributedTxManagerProvider.getInstance();
        distributedTxManagerProvider.getTransactionManager().begin();

        Transaction transaction = distributedTxManagerProvider.getTransactionManager().getTransaction();
        transaction.enlistResource(xaResource);

        Session session = xaSession.getSession();

        // AP
        MessageProducer producer = session.createProducer(xaTestQueue);

        producer.send(session.createTextMessage("Test 1"));

        distributedTxManagerProvider.getTransactionManager().rollback();

        // Closing the JMS Session
        session.close();

        // Closing the XASession
        xaSession.close();

        xaConnection.close();
    }

    private static InitialContext getInitialContext() throws NamingException {
        Properties properties = new Properties();
        properties.put(NAMING_FACTORY_INITIAL, broker);
        properties.put(PROVIDER_URL, url);
        properties.put(QUEUE_PREFIX + queueName, queueName);

        return new InitialContext(properties);
    }

}
