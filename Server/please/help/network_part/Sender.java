package please.help.network_part;

import java.io.*;
import java.net.Socket;

public class Sender implements Runnable{

    private final Socket client;
    private final Object feedback;
    private final DataInputStream clientInput;
    private final DataOutputStream clientOutput;

    public Sender(Socket client, Object feedback, DataInputStream clientInput, DataOutputStream clientOutput){
        this.client = client;
        this.feedback = feedback;
        this.clientInput = clientInput;
        this.clientOutput = clientOutput;
    }
    @Override
    public void run() {
        try(ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput)){
            objectOutput.writeObject(feedback);
            objectOutput.flush();
            Listener.logger.info("Начало отправки ответа клиенту : {} bytes (ip: {}, port: {})",
                    byteOutput.toByteArray().length, client.getInetAddress().getAddress(), client.getPort());
            clientOutput.writeInt(byteOutput.toByteArray().length);
            clientOutput.write(byteOutput.toByteArray());
            clientOutput.flush();
            Listener.logger.info("Ответ отправлен (ip: {}, port: {})",
                    client.getInetAddress().getAddress(), client.getPort());

            client.close();
            clientInput.close();
            clientOutput.close();
            Listener.logger.info("Соединение с клиентом разорвано (ip: {}, port: {})",
                    client.getInetAddress().getAddress(), client.getPort());
        } catch (IOException e) {
            Listener.logger.warn("Во время отправки ответа клиенту произошла ошибка {} (ip: {}, port: {})", e,
                    client.getInetAddress().getAddress(), client.getPort());
        }
    }
}
