import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool _unspentTransactionOutputs;

    public TxHandler(UTXOPool utxoPool) {
        _unspentTransactionOutputs = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {    
        double inputSum = 0;  
        double outputSum = 0;      
        ArrayList<Transaction.Input> inputs = tx.getInputs();         
        for (Transaction.Input input : inputs) {
            UTXO inputUtxo = new UTXO(input.prevTxHash, input.outputIndex);
            //(1) all outputs claimed by {@code tx} are in the current UTXO pool
            if(_unspentTransactionOutputs.contains(inputUtxo) == false)
            {
                return false;
            }
            
            Transaction.Output prevOutput = _unspentTransactionOutputs.getTxOutput(inputUtxo);
            inputSum += prevOutput.value;
            //(2) the signatures on each input of {@code tx} are valid
            if(Crypto.verifySignature(prevOutput.address, tx.getRawDataToSign(inputs.indexOf(input)), input.signature) == false)
            {
                return false;
            }

            //(3) no UTXO is claimed multiple times by {@code tx},
            for(int i = inputs.indexOf(input) + 1; i < inputs.size(); i++)
            {
                if(input.equals(inputs.get(i)))
                {
                    return false;
                }
            }
        }
        
        //(4) all of output values are non-negative
        for (Transaction.Output output : tx.getOutputs()) {
            outputSum += output.value;
            if(output.value < 0)
            {
                return false;
            }
        }

        //(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false otherwise.
        if(inputSum < outputSum)
        {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> result = new ArrayList<Transaction>();;
        for (Transaction transaction : possibleTxs) {
            if(isValidTx(transaction))
            {
                for (Transaction.Input input : transaction.getInputs()) {                    
                    _unspentTransactionOutputs.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
                }
                int index = 0;
                for (Transaction.Output op : transaction.getOutputs()) {
                    _unspentTransactionOutputs.addUTXO(new UTXO(transaction.getHash(), index), op);
                    index += 1;
                }
                result.add(transaction);
            }
        }
        Transaction[] arr = new Transaction[result.size()];
        result.toArray(arr);
        return arr;
    }

}
