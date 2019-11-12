package bootcamp.flows;

import bootcamp.contracts.HouseContract;
import bootcamp.contracts.HouseState;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.hibernate.Session;

import java.util.ArrayList;
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

            TransactionBuilder transactionBuilder = getTransactionBuilder();

            // Step 5: Verify the transaction against the contract.
            transactionBuilder.verify(getServiceHub());

            // Step 6: Self-Sign the transaction
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

            // Step 7: Collection Signature from CounterParty
            List<FlowSession> flowSessions = new ArrayList<>();
            FlowSession newOwnerSession = initiateFlow(newOwner);
            flowSessions.add(newOwnerSession);

            if(!(getOurIdentity().equals(getInputState().getState().getData().getBuilder()))) {
                FlowSession builderSession = initiateFlow(getInputState().getState().getData().getBuilder());
                flowSessions.add(builderSession);
            }

            SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(signedTransaction,
                    Collections.singleton(newOwnerSession)));

            // Step 8: Notarise the transaction and record the state updates.
            return subFlow(new FinalityFlow(fullySignedTransaction, flowSessions));
        }

        // Step 1: Fetch the input state from the vault and filter it based in the address. The filtered result is used
        // as in input to the transaction.
        /**
         * TODO 1: Fetch the input state from the vault.
         * @return StateAndRef
         * @throws FlowException
         */
        public StateAndRef<HouseState> getInputState() throws FlowException{
            return getServiceHub().getVaultService().queryBy(HouseState.class)
                    .getStates().stream().filter(houseStateStateAndRef -> {
                HouseState houseState = houseStateStateAndRef.getState().getData();
                return houseState.getAddress().equals(this.address);
            }).findAny().orElseThrow(() -> new FlowException("House Not Found"));
        }

        // Step 2: Create the output state and command
        /**
         * TODO 2: Create the output state of the transaction.
         * @return HouseState
         * @throws FlowException
         */
        public HouseState getOutputState() throws FlowException{
            HouseState inputHouseState = getInputState().getState().getData();
            return new HouseState(inputHouseState.getAddress(), inputHouseState.getBuildArea(),
                    inputHouseState.getNumberOfBedRooms(), inputHouseState.getConstructionYear(),
                    inputHouseState.getBuilder(), newOwner);
        }

        /**
         * TODO 3: Create the command to be used in the transaction.
         * @return HouseState
         * @throws FlowException
         */
        public Command<HouseContract.Commands.Transfer> getCommand(){
            return new Command<>(
                    new HouseContract.Commands.Transfer(),
                    ImmutableList.of(getOurIdentity().getOwningKey(), newOwner.getOwningKey())
            );
        }

        // Step 3: Fetch the notary, note that the notary should be same that was used to create the input state.
        /**
         * TODO 4: Fetch the notary for the transaction.
         * @return HouseState
         * @throws FlowException
         */
        public Party getNotaryFromInputState() throws FlowException{
            return getInputState().getState().getNotary();
        }

        // Step 4: Create the transaction builder.
        /**
         * TODO 5: Create the transaction builder.
         * @return
         * @throws FlowException
         */
        public TransactionBuilder getTransactionBuilder() throws FlowException{
            return new TransactionBuilder(getNotaryFromInputState())
                    .addInputState(getInputState())
                    .addOutputState(getOutputState())
                    .addCommand(getCommand());
        }
    }

    // Step 9 Build the Responder Flow
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
