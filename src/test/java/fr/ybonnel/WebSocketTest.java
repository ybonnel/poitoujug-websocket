package fr.ybonnel;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import static com.jayway.awaitility.Awaitility.await;
import static fr.ybonnel.Main.routes;
import static fr.ybonnel.simpleweb4j.SimpleWeb4j.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

public class WebSocketTest {

    /** port choisi au hasard */
    private static int port = ThreadLocalRandom.current().nextInt(20000, 30000);
    private WebSocketClient client;

    /**
     * Démarrage du serveur et du client websocket.
     */
    @Before
    public void setup() throws Exception {
        routes();
        setPort(port);
        start(false);

        client = new WebSocketClient();
        client.start();
    }

    /**
     * Arrêt du serveur et du client websocket.
     */
    @After
    public void stopServer() throws Exception {
        stop();

        client.stop();
    }


    @Test
    public void canConnect() throws Exception {
        String name = "user";
        Session session = client.connect(new WebSocketAdapter() {},
                new URI("ws://localhost:" + port + "/chat/" + name)).get();

        assertThat(session).isNotNull();
        assertThat(session.isOpen()).isTrue();

        session.close();

        await().until(session::isOpen, is(false));
    }

    @Test
    public void canTalkWithMyself() throws Exception {
        // TODO must do some tests
    }

    @Test
    public void canTalkWithOtherPeople() throws Exception {
        // TODO must do some tests
    }
}
