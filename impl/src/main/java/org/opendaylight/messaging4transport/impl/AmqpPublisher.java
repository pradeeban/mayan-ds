/*
 * Copyright (c) 2015 Pradeeban Kathiravelu and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.messaging4transport.impl;

import org.apache.qpid.amqp_1_0.jms.impl.*;
import org.opendaylight.messaging4transport.constants.Messaging4TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

/**
 * The class that publishes AMQP messages.
 */
public class AmqpPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(AmqpPublisher.class);

    /**
     * Publishes the RPC, data tree, and notifications to the given destination/topic.
     *
     * @param msg - the message text to be sent
     */
    public static void publish(String msg) {
        String destination = Messaging4TransportConstants.AMQP_TOPIC_EVENT_DESTINATION;
        try {
            publish(destination, msg);
        } catch (JMSException e) {
            LOG.error("JMS Exception in publishing to the AMQP broker", e);
        } catch (InterruptedException e) {
            LOG.error("Interrupted Exception in publishing to the AMQP broker", e);
        }
    }

    /**
     * Publishes the data to the given destination
     *
     * @param destination The destination topic
     * @param msg         - the message text to be sent
     * @throws JMSException         if sending the data to the broker fails
     * @throws InterruptedException if interrupted
     */
    public static void publish(String destination, String msg) throws JMSException, InterruptedException {
        String user = AmqpConfig.getUser();
        String password = AmqpConfig.getPassword();
        String host = AmqpConfig.getHost();
        int port = AmqpConfig.getPort();

        ConnectionFactoryImpl factory = new ConnectionFactoryImpl(host, port, user, password);

        Connection connection = factory.createConnection(user, password);
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(AmqpConfig.getDestination(destination));
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        sendMessages(session, producer, msg);
    }

    /**
     * Generates random messages
     *
     * @param session  session
     * @param producer message producer
     * @param msgText  the message to be sent in text format.
     * @throws JMSException if message transfer failed.
     */
    public static void sendMessages(Session session, MessageProducer producer, String msgText) throws JMSException {
        TextMessage msg = session.createTextMessage(msgText);
        producer.send(msg);
    }
}