package bootcamp.flows;

import bootcamp.HouseContract;
import bootcamp.HouseState;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Collections;
import java.util.List;

// ******************
// * HouseTransferFlow *
// ******************
public class HouseTransferFlow {

    private HouseTransferFlow(){}

    @InitiatingFlow
    @StartableByRPC
    public static class HouseTransferInitiator extends FlowLogic<SignedTransaction>{
        private final ProgressTracker progressTracker = new ProgressTracker();

        private final Party newOwner;
        private final String address;

        public HouseTransferInitiator(Party newOwner, String address) {
            this.newOwner = newOwner;
            this.address = address;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // We get a reference to our own identity.
            Party owner = getOurIdentity();

            StateAndRef<HouseState> inputStateAndRef = getServiceHub().getVaultService().queryBy(HouseState.class)
                    .getStates().stream().filter(houseStateStateAndRef -> {
                        HouseState houseState = houseStateStateAndRef.getState().getData();
                        return houseState.getAddress().equals(this.address);
                    }).findAny().orElseThrow(() -> new FlowException("House Not Found"));

            Party notary = inputStateAndRef.getState().getNotary();

            HouseState inputHouseState = inputStateAndRef.getState().getData();
            HouseState outputHouseState = new HouseState(inputHouseState.getAddress(), inputHouseState.getBuildArea(),
                    inputHouseState.getNumberOfBedRooms(), inputHouseState.getConstructionYear(),
                    inputHouseState.getBuilder(), newOwner);

            TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                    .addInputState(inputStateAndRef)
                    .addOutputState(outputHouseState)
                    .addCommand(new HouseContract.Commands.Transfer(), ImmutableList.of(owner.getOwningKey(),
                            newOwner.getOwningKey()));

            transactionBuilder.verify(getServiceHub());

            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            FlowSession session = initiateFlow(newOwner);

            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction,
                    Collections.singletonList(session)));

            return subFlow(new FinalityFlow(fullySignedTransaction, Collections.singletonList(session)));
        }
    }


    @InitiatedBy(HouseTransferInitiator.class)
    public static class HouseTransferResponder extends FlowLogic<SignedTransaction>{
        private final FlowSession otherSide;

        public HouseTransferResponder(FlowSession otherSide) {
            this.otherSide = otherSide;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(otherSide) {

                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    // Implement responder flow transaction checks here
                }
            });

            return subFlow(new ReceiveFinalityFlow(otherSide, signedTransaction.getId()));
        }
    }
}
