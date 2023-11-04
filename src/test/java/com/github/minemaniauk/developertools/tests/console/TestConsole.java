/*
 * DeveloperTools
 * A library of tools helpful in testing and development.
 *
 * Copyright (C) 2023  MineManiaUK Staff
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

package com.github.minemaniauk.developertools.tests.console;

import com.github.minemaniauk.developertools.console.ConsoleColor;
import com.github.minemaniauk.developertools.testing.ResultChecker;
import org.junit.jupiter.api.Test;

public class TestConsole {

    @Test
    public void testConsoleColors() {
        ResultChecker resultChecker = new ResultChecker();

        // Loop though all the colors.
        for (ConsoleColor color : ConsoleColor.values()) {
            resultChecker.expect(
                    color.getCode() + "test",
                    ConsoleColor.parse(color.getPattern() + "test")
            );
        }
    }
}
