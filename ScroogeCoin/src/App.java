import java.security.PublicKey;

public class App {
    public static void main(String[] args) throws Exception {
        
        var transactionHandler = new TxHandler(new UTXOPool());
        transactionHandler.handleTxs(new Transaction[] { });
        System.out.println("Hello, World!");
    }
}
