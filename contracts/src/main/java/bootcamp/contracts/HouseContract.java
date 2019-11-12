package bootcamp.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

// ************
// * HouseContract *
// ************

/**
 * TODO 1: Implement Contract Interface
 */
public class HouseContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "bootcamp.contracts.HouseContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        /**
         * TODO: Write Common Verification Logic
         */
        if(tx.getCommands().size() != 1)
            throw new IllegalArgumentException("One Command Expected");

        if(tx.getOutputs().size()!=1)
            throw new IllegalArgumentException("One Output Expected");

        if(!(tx.getOutput(0) instanceof HouseState))
            throw new IllegalArgumentException("Output of Type HouseState Expected");

        if(tx.getCommand(0).getValue() instanceof Commands.Issue){
            verifyIssue(tx);
        }else if(tx.getCommand(0).getValue() instanceof Commands.Transfer){
            verifyTransfer(tx);
        }else{
            throw new IllegalArgumentException("Unrecognized Command");
        }
    }

    private void verifyIssue(LedgerTransaction tx){
        /**
         * TODO: Write Verification Logic for Issue
         */

        if(tx.getInputs().size()!=0)
            throw new IllegalArgumentException("Zero Inputs Excepted");

        if(!(tx.getCommand(0).getSigners().contains(((HouseState)tx.getOutput(0)).getBuilder().getOwningKey())))
            throw new IllegalArgumentException("Buider must sign");

    }

    private void verifyTransfer(LedgerTransaction tx){
        /**
         * TODO: Write Verification Logic for Transfer
         */

        if(tx.getInputs().size()!=1)
            throw new IllegalArgumentException("One Input Excepted");

        if(!(tx.getInput(0) instanceof HouseState))
            throw new IllegalArgumentException("Input of Type HouseState Expected");

        if(!(tx.getCommand(0).getSigners().contains(((HouseState)tx.getOutput(0)).getOwner().getOwningKey()) &&
                tx.getCommand(0).getSigners().contains(((HouseState)tx.getInput(0)).getOwner().getOwningKey())))
            throw new IllegalArgumentException("Owner must sign");

        if(!compare((HouseState)tx.getInput(0), (HouseState)tx.getOutput(0)))
            throw new IllegalArgumentException("Incorrect House Transferred");
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Issue implements Commands {}
        class Transfer implements Commands {}
    }

    private boolean compare(HouseState input, HouseState output){
        if(input.getAddress().equals(output.getAddress()) && input.getBuildArea().equals(output.getBuildArea())
            && input.getNumberOfBedRooms().equals(output.getNumberOfBedRooms())
                && input.getConstructionYear().equals(output.getConstructionYear()) ){
            return true;
        }else{
            return false;
        }
    }
}