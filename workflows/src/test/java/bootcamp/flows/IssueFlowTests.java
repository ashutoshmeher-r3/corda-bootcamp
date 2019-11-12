package bootcamp.flows;

import bootcamp.contracts.HouseContract;
import bootcamp.contracts.HouseState;
import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IssueFlowTests {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
        TestCordapp.findCordapp("bootcamp.contracts"),
        TestCordapp.findCordapp("bootcamp.flows")
    )));
    private final StartedMockNode nodeA = network.createNode();
    private final StartedMockNode nodeB = network.createNode();

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void flowSelectsTheCorrectNotaryForTheTransaction() throws Exception {
        HouseIssueFlow flow = new HouseIssueFlow("Powai Mumbai", "2000sqft", 3, 2019);
        nodeA.startFlow(flow);
        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), flow.getNotaryForTransaction());
    }

    @Test
    public void flowHasAHouseStateOutputWithTheCorrectDetails() throws Exception {
        HouseIssueFlow flow = new HouseIssueFlow("Powai Mumbai", "2000sqft", 3, 2019);
        nodeA.startFlow(flow);


        assertNotNull(flow.getOutputState());
        HouseState outputState = flow.getOutputState();
        assertEquals("Powai Mumbai", outputState.getAddress());
        assertEquals("2000sqft", outputState.getBuildArea());
        assertEquals(new Integer(3), outputState.getNumberOfBedRooms());
        assertEquals(new Integer(2019), outputState.getConstructionYear());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), outputState.getBuilder());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), outputState.getOwner());
    }

    @Test
    public void flowUsesTheIssueCommandAndCorrectSigner() throws Exception {
        HouseIssueFlow flow = new HouseIssueFlow("Powai Mumbai", "2000sqft", 3, 2019);
        nodeA.startFlow(flow);

        assertTrue(flow.getCommand().getValue() instanceof HouseContract.Commands.Issue);
        assertEquals(1, flow.getCommand().getSigners().size());
        assertTrue(flow.getCommand().getSigners().contains(nodeA.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void flowBuildsTheTransactionCorrectly(){
        HouseIssueFlow flow = new HouseIssueFlow("Powai Mumbai", "2000sqft", 3, 2019);
        nodeA.startFlow(flow);

        TransactionBuilder txBuilder = flow.getTransactionBuilder();
        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), txBuilder.getNotary());

        HouseState outputState = ((HouseState)txBuilder.outputStates().get(0).getData());
        assertEquals("Powai Mumbai", outputState.getAddress());
        assertEquals("2000sqft", outputState.getBuildArea());
        assertEquals(new Integer(3), outputState.getNumberOfBedRooms());
        assertEquals(new Integer(2019), outputState.getConstructionYear());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), outputState.getBuilder());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), outputState.getOwner());


        assertTrue(txBuilder.commands().get(0).getValue() instanceof HouseContract.Commands.Issue);
        assertEquals(1, txBuilder.commands().get(0).getSigners().size());
        assertTrue(txBuilder.commands().get(0).getSigners().contains(nodeA.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void transactionConstructedByFlowHasOneOutputUsingTheCorrectContract() throws Exception {
        HouseIssueFlow flow = new HouseIssueFlow("Powai Mumbai", "2000sqft", 3, 2019);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);

        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals("bootcamp.contracts.HouseContract", output.getContract());
    }
}