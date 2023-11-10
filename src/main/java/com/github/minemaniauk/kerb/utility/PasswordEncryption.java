/*
 * Kerb
 * Event and request distributor server software.
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

package com.github.minemaniauk.kerb.utility;

import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PasswordEncryption {

    /**
     * Used to encrypt a password using sha-512.
     *
     * @param password The instance of the password to hash.
     * @return The requested hash.
     */
    public static @NotNull String encrypt(@NotNull String password, @NotNull String salt) {
        try {

            // Select the algorithm.
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 512);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return new String(factory.generateSecret(spec).getEncoded()).replace("\n", "");

        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to generate a new salt.
     *
     * @return The new salt.
     */
    public static String createSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return new String(salt);
    }
}