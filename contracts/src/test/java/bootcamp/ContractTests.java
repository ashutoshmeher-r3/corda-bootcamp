package bootcamp;

import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity builder = new TestIdentity(new CordaX500Name("Builder", "", "IN"));
    private final TestIdentity owner = new TestIdentity(new CordaX500Name("Owner", "", "IN"));
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "IN")));

    private HouseState houseState = new HouseState("Powai, Mumbai", "2000sqft", 3,
            2019, builder.getParty(), builder.getParty());

    @Test
    public void houseContractImplementsContract() {
        assert(new HouseContract() instanceof Contract);
    }

    @Test
    public void houseContractRequiresOneCommandInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two commands, will fail.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one command, will verify.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one command, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseIssueRequiresZeroInputsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has an input, will fail.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());;
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no input, will verify.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void houseIssueRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseIssueRequiresOutputofTypeHouseState(){
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(HouseContract.ID, new DummyState());
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseIssueRequiresBuilderAsRequiredSigner(){
        transaction(ledgerServices, tx -> {
            // Has wrong signer, will fail.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(owner.getPublicKey()), new HouseContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct signer, will verify.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    /**
     * House Transfer Tests
     */

    @Test
    public void houseTransferRequiresOneInputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has no input, will fail.
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has two input, will fail.
            tx.input(HouseContract.ID, houseState);
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one input, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseTransferRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no outputs, will fail.
            tx.input(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseTransferRequiresInputofTypeHouseState(){
        transaction(ledgerServices, tx -> {
            // Has wrong input type, will fail.
            tx.input(HouseContract.ID, new DummyState());
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct input type, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseTransferRequiresOutputofTypeHouseState(){
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, new DummyState());
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseTransferRequiresInputAndOutputHouseToBeTheSame(){
        transaction(ledgerServices, tx -> {
            // Has different house in output, will fail.
            HouseState houseStateOutput = new HouseState("Powai, Mumbai", "1800sqft", 2,
                    2019, builder.getParty(), owner.getParty());
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseStateOutput);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has same house in input and output, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, houseState);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void houseTransferRequiresCurrentOwnerAndNewOwnerAsRequiredSigner(){
        HouseState output = new HouseState("Powai, Mumbai", "2000sqft", 3,
                2019, builder.getParty(), owner.getParty());
        transaction(ledgerServices, tx -> {
            // Has incomplete signers, will fail.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, output);
            tx.command(Arrays.asList(builder.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has all signers, will verify.
            tx.input(HouseContract.ID, houseState);
            tx.output(HouseContract.ID, output);
            tx.command(Arrays.asList(builder.getPublicKey(), owner.getPublicKey()), new HouseContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }
}