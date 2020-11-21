package please.help.network_part;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import please.help.CollectionManager;
import please.help.Mode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Listener implements Runnable{

    public static final Logger logger = (Logger) LoggerFactory.getLogger(Listener.class.getName());
    private final CollectionManager manager;
    private final ServerSocket server;

    public static final ForkJoinPool poolForExecutors = ForkJoinPool.commonPool();
    public static final ExecutorService poolForSenders = Executors.newCachedThreadPool();

    private Listener(ServerSocket server, CollectionManager manager){
        this.server = server;
        this.manager = manager;
    }

    public static Listener createServer(int port, CollectionManager manager){
        try{
            logger.info("Попытка инициализации сервера: port: {}", port);
            ServerSocket server = new ServerSocket(port);
            server.setSoTimeout(5*1000);
            logger.info("Сервер начал работу:  port: {}", server.getLocalPort());
            return new Listener(server, manager);
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void run(){
        while (manager.getManagerMode() != Mode.EXIT) {
            try {
                Socket client = server.accept();
                Thread newThreadForClient = new Thread(new Receiver(client, manager,
                        new DataInputStream(new BufferedInputStream(client.getInputStream())),
                        new DataOutputStream(new BufferedOutputStream(client.getOutputStream()))));
                newThreadForClient.setDaemon(true);
                newThreadForClient.start();
            }
            catch (IOException ignore){}
        }
        logger.info("Сервер прекращает работу по команде с консоли");
    }
}
