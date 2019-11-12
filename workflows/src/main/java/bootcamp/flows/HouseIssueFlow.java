package bootcamp.flows;

import bootcamp.contracts.HouseContract;
import bootcamp.contracts.HouseState;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Collections;

// ******************
// * HouseIssueFlow *
// ******************
@InitiatingFlow
@StartableByRPC
public class HouseIssueFlow extends FlowLogic<SignedTransaction> {

    private final ProgressTracker progressTracker = new ProgressTracker();

    private final String address;
    private final String buildArea;
    private final Integer numberOfBedRooms;
    private final Integer constructionYear;

    public HouseIssueFlow(String address, String buildArea, Integer numberOfBedRooms, Integer constructionYear) {
        this.address = address;
        this.buildArea = buildArea;
        this.numberOfBedRooms = numberOfBedRooms;
        this.constructionYear = constructionYear;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        //Step3 : Build the transaction
        TransactionBuilder transactionBuilder = getTransactionBuilder();

        //Step 4: Verify Transaction against contract
        transactionBuilder.verify(getServiceHub());

        //Step 5: Sign the transaction.
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        //Step 6: Commit the transaction to the ledger
        return subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));
    }

    //Step 1: Create the output state and command
    /**
     * TODO 1: Create the output state
     * @return Party
     */
    public HouseState getOutputState(){
        return
            new HouseState(address, buildArea, numberOfBedRooms, constructionYear, getOurIdentity(), getOurIdentity());
    }

    /**
     * TODO 2: Create the command
     * @return
     */
    public Command<HouseContract.Commands.Issue> getCommand(){
        return new Command<>(
                new HouseContract.Commands.Issue(),
                ImmutableList.of(getOurIdentity().getOwningKey())
        );
    }

    //Step 2:  We choose our transaction's notary (the notary prevents double-spends).
    /**
     * TODO 3: Choose the notary
     * @return Party
     */
    public Party getNotaryForTransaction(){
        return getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
    }

    /**
     * TODO 4: Create the TransactionBuilder
     * @return
     */
    public TransactionBuilder getTransactionBuilder(){
        return new TransactionBuilder(getNotaryForTransaction())
                .addOutputState(getOutputState())
                .addCommand(getCommand());
    }

}
