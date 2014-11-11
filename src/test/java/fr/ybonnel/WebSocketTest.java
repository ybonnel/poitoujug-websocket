package fr.ybonnel;

import fr.ybonnel.simpleweb4j.handlers.ContentType;
import org.assertj.core.groups.Tuple;
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
        List<Message> messagesUser1 = new ArrayList<>();
        List<Message> messagesUser2 = new ArrayList<>();


        Session session1 = openSession("user1", messagesUser1::add);
        Session session2 = openSession("user2", messagesUser2::add);

        assertThat(session1).isNotNull();
        assertThat(session1.isOpen()).isTrue();

        assertThat(session2).isNotNull();
        assertThat(session2.isOpen()).isTrue();

        session1.getRemote().sendString("\"Hello user2\"");

        await().until(messagesUser1::size, equalTo(1));
        await().until(messagesUser2::size, equalTo(1));


        session2.getRemote().sendString("\"Hello user1\"");

        await().until(messagesUser1::size, equalTo(2));
        await().until(messagesUser2::size, equalTo(2));

        session1.close();

        await().until(session1::isOpen, is(false));

        session2.getRemote().sendString("\"I'm alone\"");

        await().until(messagesUser2::size, equalTo(3));

        session2.close();

        await().until(session2::isOpen, is(false));

        assertThat(messagesUser1).extracting("user", "text").containsExactly(
                Tuple.tuple("user1", "Hello user2"),
                Tuple.tuple("user2", "Hello user1"));

        assertThat(messagesUser2).extracting("user", "text").containsExactly(
                Tuple.tuple("user1", "Hello user2"),
                Tuple.tuple("user2", "Hello user1"),
                Tuple.tuple("user2", "I'm alone"));
    }
}
