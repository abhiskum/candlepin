/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.audit;

import org.candlepin.audit.Event.Target;
import org.candlepin.audit.Event.Type;
import org.candlepin.guice.PrincipalProvider;
import org.candlepin.model.Consumer;
import org.candlepin.test.TestUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import javax.jms.JMSException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * AMQPBusPublisherTest
 */
public class AMQPBusPublisherTest {

    private ObjectMapper mapper;
    private AMQPBusPublisher publisher;
    private QpidConnection qpid;

    @Before
    public void init() {
        mapper = new ObjectMapper();
        qpid = mock(QpidConnection.class);
        publisher = new AMQPBusPublisher(mapper, qpid);
    }

    @Test
    public void testClose() throws JMSException {
        publisher.close();
        verify(qpid).close();
    }

    @Test
    public void testApply() throws IOException {
        PrincipalProvider pp = mock(PrincipalProvider.class);
        when(pp.get()).thenReturn(TestUtil.createPrincipal("admin", null, null));

        EventFactory factory = new EventFactory(pp, new ObjectMapper());
        Consumer c = TestUtil.createConsumer();
        Event e = factory.consumerCreated(c);

        String value = publisher.apply(e);

        Event e1 = mapper.readValue(value, Event.class);
        assertEquals(e.getType(), e1.getType());
        assertEquals(e.getTarget(), e1.getTarget());
    }

    @Test
    public void onEvent() throws JMSException {
        PrincipalProvider pp = mock(PrincipalProvider.class);
        when(pp.get()).thenReturn(TestUtil.createPrincipal("admin", null, null));

        EventFactory factory = new EventFactory(pp, mapper);
        Consumer c = TestUtil.createConsumer();
        Event e = factory.consumerCreated(c);

        publisher.onEvent(e);


        verify(qpid).sendTextMessage(Mockito.eq(Target.CONSUMER),
            Mockito.eq(Type.CREATED),
            Mockito.contains("TestConsumer"));

    }
}
