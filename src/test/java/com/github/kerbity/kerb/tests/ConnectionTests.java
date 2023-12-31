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
import com.github.kerbity.kerb.creator.ClientCreator;
import com.github.kerbity.kerb.creator.ServerCreator;
import com.github.kerbity.kerb.server.Server;
import com.github.minemaniauk.developertools.testing.ResultChecker;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Contains tests to test connecting to
 * the kerb server as a kerb client.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConnectionTests {

    @Test
    @Order(0)
    public void testValidation() {
        Server server = ServerCreator.createAndStart().waitForStartup();

        // Create a client connection.
        KerbClient client = ClientCreator.create(server.getPort(), server.getAddress());
        client.connect();

        // Check if the client was validated.
        new ResultChecker()
                .expect(client.isValid())
                .expect(server.getConnectionList().get(0).isValid());
    }
}
