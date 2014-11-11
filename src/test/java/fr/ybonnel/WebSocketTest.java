package fr.ybonnel;

import fr.ybonnel.simpleweb4j.handlers.ContentType;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static com.jayway.awaitility.Awaitility.await;
import static fr.ybonnel.Main.routes;
import static fr.ybonnel.simpleweb4j.SimpleWeb4j.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

public class WebSocketTest {

    /**
     * port choisi au hasard
     */
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


    private Session openSession(String name, Consumer<Message> consumer) throws InterruptedException, java.util.concurrent.ExecutionException, IOException, URISyntaxException {
        return client.connect(new WebSocketAdapter() {
                                  @Override
                                  public void onWebSocketText(String message) {
                                      consumer.accept(ContentType.GSON.fromJson(message, Message.class));
                                  }
                              },
                new URI("ws://localhost:" + port + "/chat/" + name)).get();
    }


    @Test
    public void canConnect() throws Exception {
        List<Message> messages = new ArrayList<>();
        Session session = openSession("user", messages::add);

        assertThat(session).isNotNull();
        assertThat(session.isOpen()).isTrue();

        session.close();

        await().until(session::isOpen, is(false));

        assertThat(messages).isEmpty();
    }

    @Test
    public void canTalkWithMyself() throws Exception {

        List<Message> messages = new ArrayList<>();

        Session session = openSession("user", messages::add);

        assertThat(session).isNotNull();
        assertThat(session.isOpen()).isTrue();

        session.getRemote().sendString("\"i'm alone\"");

        await().until(messages::size, equalTo(1));

        Message message = messages.get(0);

        assertThat(message).isEqualToIgnoringGivenFields(new Message("user", "i'm alone"), "date");

        session.close();

        await().until(session::isOpen, is(false));
    }

    @Test
    public void canTalkWithOtherPeople() throws Exception {
        // TODO must do some tests
    }
}
