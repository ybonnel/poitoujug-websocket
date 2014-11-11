package fr.ybonnel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static fr.ybonnel.Main.routes;
import static fr.ybonnel.simpleweb4j.SimpleWeb4j.*;

public class WebSocketTest {

    /** port choisi au hasard */
    private static int port = ThreadLocalRandom.current().nextInt(20000, 30000);

    /**
     * Démarrage du serveur et du client websocket.
     */
    @Before
    public void setup() throws Exception {
        routes();
        setPort(port);
        start(false);
    }

    /**
     * Arrêt du serveur et du client websocket.
     */
    @After
    public void stopServer() throws Exception {
        stop();
    }


    @Test
    public void canConnect() throws Exception {
        // TODO must do some tests
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
