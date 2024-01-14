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
import com.github.kerbity.kerb.packet.event.Priority;
import com.github.kerbity.kerb.packet.event.event.PingEvent;
import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.kerbity.kerb.server.Server;
import com.github.minemaniauk.developertools.testing.ResultChecker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

/**
 * Contains tests for kerb events.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTests {

    @Test
    @Order(0)
    public void testPingEvent() {
        Server server = ServerCreator.createAndStart().waitForStartup();
        KerbClient client = ClientCreator.create(server.getPort(), server.getAddress());
        client.connect();

        // Set up an event listener for the ping event.
        client.registerListener(Priority.LOW, (EventListener<PingEvent>) event -> {
            event.set(client.getAdapted());
            return event;
        });

        // Call the ping event.
        CompletableResultSet<PingEvent> resultSet = client.callEvent(new PingEvent());

        // Ensure the event was processed correctly.
        new ResultChecker()
                .expect(resultSet.waitForFirstNonNull() != null)
                .expect(resultSet.waitForFirstNonNullAssumption().get().getName(), client.getName())
                .expect("Test", resultSet.waitForFirstNonNullAssumption().getSource().getName());
    }

    @Test
    @Order(1)
    public void testPingEventMultiple() {
        Server server = ServerCreator.createAndStart().waitForStartup();
        KerbClient client1 = ClientCreator.create(server.getPort(), server.getAddress());
        client1.connect();
        KerbClient client2 = ClientCreator.create(server.getPort(), server.getAddress());
        client2.connect();

        // Register a delayed listener.
        client1.registerListener(Priority.HIGH, (EventListener<PingEvent>) event -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
            event.set(client1.getAdapted());
            return event;
        });

        // Register a listener.
        client2.registerListener(Priority.HIGH, (EventListener<PingEvent>) event -> {
            event.set(client2.getAdapted());
            return null;
        });

        // Wait for the final result.
        CompletableResultSet<PingEvent> resultSet = client1.callEvent(new PingEvent());
        List<PingEvent> results = resultSet.waitForFinalResult();

        // Ensure there is only one result.
        // This is because the first listener took too long.
        new ResultChecker().expect(results.size() == 1);
    }
}
