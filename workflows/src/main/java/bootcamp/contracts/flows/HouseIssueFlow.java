package bootcamp.contracts.flows;

import bootcamp.contracts.HouseContract;
import bootcamp.contracts.HouseState;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
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
        // We choose our transaction's notary (the notary prevents double-spends).
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);//getNotaryForTransaction();
        // We get a reference to our own identity.
        Party builder = getOurIdentity();

        HouseState houseState = new HouseState(address, buildArea, numberOfBedRooms, constructionYear, builder,
                builder);

        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addOutputState(houseState)
                .addCommand(new HouseContract.Commands.Issue(), ImmutableList.of(builder.getOwningKey()));

        transactionBuilder.verify(getServiceHub());

        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        return subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));
    }

//    private Party getNotaryForTransaction(){
//        return getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
//    }
}
