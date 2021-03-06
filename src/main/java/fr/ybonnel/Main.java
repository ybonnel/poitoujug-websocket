/*
 * Copyright 2013- Yan Bonnel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ybonnel;


import fr.ybonnel.simpleweb4j.handlers.RouteParameters;
import fr.ybonnel.simpleweb4j.handlers.websocket.WebSocketListener;
import fr.ybonnel.simpleweb4j.handlers.websocket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fr.ybonnel.simpleweb4j.SimpleWeb4j.start;
import static fr.ybonnel.simpleweb4j.SimpleWeb4j.websocket;

public class Main {

    public static void main(String[] args) {
        routes();

        start();
    }


    public static void routes() {
        websocket("/chat/:name", Main::buildListenner);
    }

    private static Set<WebSocketSession<Message>> sessionOpenned = new HashSet<>();

    private static WebSocketListener<String, Message> buildListenner(RouteParameters routeParameters) {
        return WebSocketListener.<String, Message>newBuilder(String.class)
                .onConnect(sessionOpenned::add)
                .onClose(sessionOpenned::remove)
                .onMessage(text -> sendMessageToAllSession(new Message(routeParameters.getParam("name"), text)))
                .build();
    }

    private static void sendMessageToAllSession(Message message) {
        sessionOpenned.forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (IOException ignore) {
            }
        });
    }
}
