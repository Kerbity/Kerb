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

package com.github.kerbity.kerb;

import com.github.kerbity.kerb.task.TaskContainer;
import com.github.minemaniauk.developertools.console.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Represents a connection to a socket.
 */
public abstract class Connection extends TaskContainer {

    private @Nullable Socket socket;
    private @NotNull Logger logger;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    /**
     * Used to create an instance of a new connection.
     */
    public Connection() {
        this.logger = new Logger(false)
                .setBothPrefixes("[UNDEFINED]");
    }

    /**
     * Used to get if the connection is in
     * debug mode.
     *
     * @return True if in debug mode.
     */
    protected abstract boolean getDebugMode();

    /**
     * Used to get the instance of the socket.
     *
     * @return The instance of the socket.
     */
    protected @Nullable Socket getSocket() {
        return this.socket;
    }

    /**
     * Used to set up the in and out streams.
     * This will enable the sending and reading of data.
     *
     * @return True if successful.
     */
    protected boolean setupStreams(@NotNull Socket socket, @NotNull Logger logger) {
        this.socket = socket;
        this.logger = logger;

        try {

            if (this.getDebugMode()) this.logger.log("[DEBUG] Setting up streams.");

            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;

        } catch (IOException exception) {
            this.logger.warn("Exception occurred when setting up client connection streams.");
            throw new RuntimeException(exception);
        }
    }

    /**
     * Used to send data though the socket.
     *
     * @param data The data to send.
     */
    protected void send(@NotNull String data) {
        if (this.socket == null) return;
        if (this.socket.isClosed()) return;

        this.printWriter.println(data);
        if (this.getDebugMode()) this.logger
                .createExtension("[" + this.socket.getLocalPort() + "] ")
                .log("&7[DEBUG] Send {data: \"" + data + "\"}");
    }

    /**
     * Used to send an array of bytes though the socket.
     *
     * @param byteArray The byte array.
     */
    protected void send(byte[] byteArray) {
        if (this.socket == null) return;
        if (this.socket.isClosed()) return;

        StringBuilder builder = new StringBuilder();
        for (byte item : byteArray) {
            builder.append(item).append(",");
        }
        this.send(builder.toString());
        if (this.getDebugMode()) this.logger
                .createExtension("[" + this.socket.getLocalPort() + "] ")
                .log("&7[DEBUG] Send {data: \"" + builder + "\"}");
    }

    /**
     * Used to read a line from the socket.
     * If there are no lines it will wait
     * till a line is written.
     *
     * @return Data read from the socket
     * @throws IOException Read error
     */
    protected String read() throws IOException {
        if (socket == null) return null;
        if (socket.isClosed()) return null;

        try {

            if (this.getDebugMode()) this.logger
                    .createExtension("[" + this.socket.getLocalPort() + "] ")
                    .log("&7[DEBUG] Waiting for data.");

            String data = this.bufferedReader.readLine();
            if (this.getDebugMode()) this.logger
                    .createExtension("[" + this.socket.getLocalPort() + "] ")
                    .log("&7[DEBUG] Read {data: \"" + data + "\"}");
            return data;

        } catch (SocketException exception) {
            if (exception.getMessage().contains("Socket closed")) {
                if (this.getDebugMode()) this.logger
                        .createExtension("[" + this.socket.getLocalPort() + "] ")
                        .log("&7[DEBUG] Unable to read data as socket was closed.");
                return null;
            }
            throw new RuntimeException(exception);
        }
    }

    protected byte[] readBytes() throws IOException {
        if (socket == null) return null;
        if (socket.isClosed()) return null;

        String byteListString = this.read();

        byte[] byteList = new byte[byteListString.split(",").length];

        int index = 0;
        for (String byteString : byteListString.split(",")) {
            byteList[index] = Byte.parseByte(byteString);
            index++;
        }

        if (this.getDebugMode()) this.logger
                .createExtension("[" + this.socket.getLocalPort() + "] ")
                .log("&7[DEBUG] Read {data: \"" + byteList + "\"}");
        return byteList;
    }

    /**
     * Used to close the input and output streams.
     *
     * @throws IOException Error when closing the buffered reader.
     */
    protected void closeStreams() throws IOException {
        this.printWriter.close();
        this.bufferedReader.close();
    }

    /**
     * Used to create and load a key store.
     *
     * @param certificate The instance of the certificate
     *                    to load into the keystore.
     * @param password    The instance of the keystore password.
     * @return The instance of the keystore.
     */
    public static @NotNull KeyStore createKeyStore(@NotNull File certificate, @NotNull String password)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream inputStream = new FileInputStream(certificate);
        keyStore.load(inputStream, password.toCharArray());
        return keyStore;
    }

    /**
     * Used to create the trust manager.
     *
     * @param certificate The instance of the certificate
     *                    to load into the trust manager.
     * @param password    The instance of the password.
     * @param logger      The instance of the logger.
     * @return The new instance of a trust manager.
     */
    public static @NotNull X509TrustManager createTrustManager(@NotNull File certificate, @NotNull String password, @NotNull Logger logger)
            throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, IOException, CertificateException {

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        InputStream inputStream1 = new FileInputStream(certificate);
        trustStore.load(inputStream1, password.toCharArray());
        trustManagerFactory.init(trustStore);

        X509TrustManager x509TrustManager = null;
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                x509TrustManager = (X509TrustManager) trustManager;
                break;
            }
        }

        if (x509TrustManager == null) {
            logger.warn("X509 for trust manager is null.");
            throw new NullPointerException();
        }

        return x509TrustManager;
    }

    /**
     * Used to create an instance of the
     * key manager.
     *
     * @param keyStore The instance of the keystore.
     * @param password The instance of the password.
     * @param logger   The instance of the logger.
     * @return A new instance of a key manager.
     */
    public static @NotNull X509KeyManager createKeyManager(@NotNull KeyStore keyStore, @NotNull String password, @NotNull Logger logger)
            throws NoSuchAlgorithmException, NoSuchProviderException, UnrecoverableKeyException, KeyStoreException {

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
        keyManagerFactory.init(keyStore, password.toCharArray());
        X509KeyManager x509KeyManager = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                x509KeyManager = (X509KeyManager) keyManager;
                break;
            }
        }

        if (x509KeyManager == null) {
            logger.warn("X509 key manager has returned null.");
            throw new NullPointerException();
        }

        return x509KeyManager;
    }
}
