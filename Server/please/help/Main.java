package please.help;

public class Main {

    public static void main(String[] args) {
        CollectionManager manager = CollectionManagerBuilder.createCollectionManager(args);
        if (manager != null) manager.start();
    }
}
