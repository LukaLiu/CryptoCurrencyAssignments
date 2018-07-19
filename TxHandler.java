import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool utxoPool;
	
    public TxHandler(UTXOPool utxoPool) {
    	
    		this.utxoPool = new UTXOPool(utxoPool);
        // IMPLEMENT THIS
    	
    }

    /**
     * @return true if:
     * (1) all outputs claimed by tx are in the current UTXO pool, 
     * (2) the signatures on each input of tx are valid, 
     * (3) no UTXO is claimed multiple times by tx,
     * (4) all of  txs output values are non-negative, and
     * (5) the sum of tx s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    		double sumIn = 0;
    		double sumOut = 0;
    		UTXOPool uPool = new UTXOPool();
    		
    		ArrayList<Transaction.Input> inputs = tx.getInputs();
    		for(int index = 0;index<tx.numInputs();index++) {
    			Transaction.Input input = inputs.get(index);
    			UTXO  utxo = new UTXO(input.prevTxHash,input.outputIndex);
    			//if utxo is in the pool
    			if(!utxoPool.contains(utxo)) {
    				return false;
    			}
    			//get the tx output to obtain public key for verify
    			Transaction.Output output = utxoPool.getTxOutput(utxo);
    			if(output==null) {
    				return false;
    			}else {
    				if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(index), input.signature)) {
    					return false;
    				}
    				if(uPool.contains(utxo)) {
    					return false;
    				}
    				uPool.addUTXO(utxo, output);
    				sumIn+=output.value;
    			}
    		}
    		
    		ArrayList<Transaction.Output> outputs = tx.getOutputs();
    		for(Transaction.Output output:outputs) {
    			if(output.value<0) {
    				return false;
    			}
    			sumOut+=output.value;
    		}
    		return sumIn>=sumOut;
        // IMPLEMENT THIS
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    		HashSet<Transaction> validTxs = new HashSet<Transaction>();
    		for(Transaction tx:possibleTxs) {
    			if(isValidTx(tx)) {
    				validTxs.add(tx);
    				for(Transaction.Input input:tx.getInputs()) {
    					UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
    					utxoPool.removeUTXO(utxo);
    				}
    				for(int index=0;index<tx.numOutputs();index++) {
    					UTXO utxo = new UTXO(tx.getHash(),index);
    					utxoPool.addUTXO(utxo, tx.getOutput(index));
    				}
    			}
    			
    		}
    		Transaction[] validTx = null;
    		validTx = validTxs.toArray(new Transaction[validTxs.size()]);
    		
    		return validTx;
    }

}
