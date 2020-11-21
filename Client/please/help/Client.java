package please.help;

import please.help.commands.Command;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private final String ip;
    private final int port;

    public Client(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public Object sendCommand(Command command, String login, String password){
        while (true) {
            try {
                SocketChannel socket = SocketChannel.open();
                socket.configureBlocking(false);
                Selector selector = Selector.open();
                socket.register(selector, SelectionKey.OP_CONNECT);

                try {
                    socket.connect(new InetSocketAddress(ip, port));
                    if (!(selector.select(5 * 1000) == 1 && socket.finishConnect())) throw new IOException();
                }
                catch (Exception e) {
                    System.out.println("Не удается установить соединение с сервером.\n"
                            + "Прервать процесс установления соединения с сервером? (y/n)");
                    Scanner scan = new Scanner(System.in);
                    while (true) {
                        String[] input = scan.nextLine().trim().split("\\s+");
                        if (input.length == 1) {
                            if (input[0].equals("y")) {
                                return null;
                            }
                            if (input[0].equals("n")) {
                                socket.close();
                                selector.close();
                                break;
                            }
                        }
                        System.out.println("Некорректный ввод. Повторите попытку.");
                    }
                    continue;
                }

                try{
                    ClientPackage packageForServer = new ClientPackage(command, login, password);
                    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                    ObjectOutputStream output = new ObjectOutputStream(byteOutput);
                    output.writeObject(packageForServer);
                    output.flush();
                    ByteBuffer bufferToSend = ByteBuffer.wrap(byteOutput.toByteArray());

                    byteOutput.reset();
                    DataOutputStream dos = new DataOutputStream(byteOutput);
                    dos.writeInt(bufferToSend.capacity());
                    dos.flush();
                    ByteBuffer commandLen = ByteBuffer.wrap(byteOutput.toByteArray());
                    dos.close();
                    while (commandLen.hasRemaining()) socket.write(commandLen);
                    while (bufferToSend.hasRemaining()) socket.write(bufferToSend);

                    ByteBuffer buffer = ByteBuffer.allocate(50000);
                    while (buffer.position() < 4) {
                        socket.read(buffer);
                    }
                    buffer.flip();
                    int packageLength = buffer.getInt();
                    buffer.compact();

                    while (buffer.position() != packageLength) socket.read(buffer);
                    ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.array()));
                    Object feedback = input.readObject();

                    output.close();
                    input.close();
                    socket.close();
                    selector.close();
                    return feedback;
                }
                catch (Exception e){
                    System.out.println("Произошла ошибка при взаимодействии с сервером.");
                    return null;
                }
            }
            catch (Exception e){
                System.out.println("Произощла ошибка во время инициализации клиента.");
                return null;
            }
        }
    }
}
