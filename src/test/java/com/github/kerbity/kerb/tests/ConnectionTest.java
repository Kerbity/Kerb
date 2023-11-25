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
    @Order(0)
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
}
