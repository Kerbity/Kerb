/*
 * Kerb
 * Event and request distributor server software.
 *
 * Copyright (C) 2023  Smuddgge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.kerbity.kerb.tests;

import com.github.kerbity.kerb.client.KerbClient;
import com.github.kerbity.kerb.client.listener.EventListener;
import com.github.kerbity.kerb.creator.ClientCreator;
import com.github.kerbity.kerb.creator.ServerCreator;
import com.github.kerbity.kerb.event.Priority;
import com.github.kerbity.kerb.event.event.PingEvent;
import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.kerbity.kerb.server.Server;
import com.github.minemaniauk.developertools.testing.ResultChecker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConnectionTest {

    @Test
    @Order(0)
    public void testValidation() {
        Server server = ServerCreator.createAndStart().waitForStartup();

        // Create a client connection.
        KerbClient client = ClientCreator.create(server.getPort(), server.getAddress());
        client.connect();

        new ResultChecker().expect(client.isValid());
    }

    @Test
    @Order(1)
    public void testPingEvent() {
        Server server = ServerCreator.createAndStart().waitForStartup();

        // Create a client connection.
        KerbClient client = ClientCreator.create(server.getPort(), server.getAddress());
        client.connect();

        // Set up an event listener for the ping event.
        client.registerListener(Priority.LOW, (EventListener<PingEvent>) event -> {
            event.setWasReceived(true);
            return event;
        });

        // Call the ping event.
        CompletableResultSet<PingEvent> resultSet = client.callEvent(new PingEvent("Test"));

        new ResultChecker()
                .expect(resultSet.waitForFirstNonNull() != null)
                .expect(resultSet.waitForFirstNonNullAssumption().wasReceived())
                .expect("Test", resultSet.waitForFirstNonNullAssumption().getSource().getName());
    }

    @Test
    @Order(2)
    public void testPingEventMultiple() {
        Server server = ServerCreator.createAndStart().waitForStartup();
        server.setDebugMode(true);
        KerbClient client1 = ClientCreator.create(server.getPort(), server.getAddress());
        client1.connect();
        KerbClient client2 = ClientCreator.create(server.getPort(), server.getAddress());
        client2.connect();

        client1.registerListener(Priority.HIGH, (EventListener<PingEvent>) event -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
            event.setWasReceived(true);
            return event;
        });

        client2.registerListener(Priority.HIGH, (EventListener<PingEvent>) event -> {
            event.setWasReceived(true);
            return null;
        });

        CompletableResultSet<PingEvent> resultSet = client1.callEvent(new PingEvent("Test"));
        List<PingEvent> results = resultSet.waitForFinalResult();

        new ResultChecker().expect(results.size() == 1);
    }
}
