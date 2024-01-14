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
import com.github.kerbity.kerb.packet.serverevent.event.CheckAliveServerEvent;
import com.github.kerbity.kerb.result.CompletableResultSet;
import com.github.kerbity.kerb.server.Server;
import com.github.minemaniauk.developertools.testing.ResultChecker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Objects;

/**
 * Contains tests for kerb server events.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerEventTests {

    @Test
    @Order(0)
    public void checkAliveEvent() {
        Server server = ServerCreator.createAndStart().waitForStartup();
        KerbClient client = ClientCreator.create(server.getPort(), server.getAddress());
        client.connect();

        CompletableResultSet<CheckAliveServerEvent> result = server.getConnectionList().get(0).callServerEvent(new CheckAliveServerEvent());

        // Ensure the event was processed correctly.
        new ResultChecker()
                .expect(result.waitForFirst() != null)
                .expect(Objects.requireNonNull(result.waitForFirst()).isAlive());
    }
}
