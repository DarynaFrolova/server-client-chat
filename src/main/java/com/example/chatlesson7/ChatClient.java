package com.example.chatlesson7;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ChatClient {

    private final Controller controller;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String login;

    public ChatClient(Controller controller) {
        this.controller = controller;
    }

    public String getLogin() {
        return login;
    }

    public void openConnection() throws Exception {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        final Thread readThread = new Thread(() -> {
            try {
                waitAuthenticate();
                readMessage();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        readThread.setDaemon(true);
        readThread.start();

    }

    private void readMessage() throws IOException {
        while (true) {
            final String message = in.readUTF();
            System.out.println("Receive message: " + message);
            if (Command.isCommand(message)) {
                final Command command = Command.getCommand(message);
                final String[] params = command.parse(message);
                if (command == Command.END) {
                    controller.setAuth(false);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                    continue;
                }
                if (command == Command.CLIENTS) {
                    controller.updateClientList(params);
                    continue;
                }
                if (command == Command.NICK) {
                    controller.addMessage("Nick successfully changed to " + params[0]);
                    continue;
                }
            }
            controller.addMessage(message);
            controller.saveClientHistory();
            controller.saveChatHistory();
        }
    }

    private void waitAuthenticate() throws IOException, InterruptedException {
        while (true) {
            final String msgAuth = in.readUTF();
            if (Command.isCommand(msgAuth)) {
                final Command command = Command.getCommand(msgAuth);
                final String[] params = command.parse(msgAuth);
                if (command == Command.AUTHOK) {
                    final String nick = params[0];
                    login = params[1];
                    File history = new File("history.txt");
                    if (history.exists()) {
                        controller.loadHistory();
                    }
                    controller.addMessage("Successful authorization under nick " + nick);
                    controller.setAuth(true);
                    break;
                }
                if (Command.ERROR.equals(command)) {
                    Platform.runLater(() -> controller.showError(params));
                }
            }
        }
    }

    private void closeConnection() throws SQLException {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Send message: " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}