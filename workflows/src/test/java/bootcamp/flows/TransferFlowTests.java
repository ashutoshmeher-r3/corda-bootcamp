package bootcamp.flows;

import bootcamp.contracts.HouseContract;
import bootcamp.contracts.HouseState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransferFlowTests {

    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("bootcamp.contracts"),
            TestCordapp.findCordapp("bootcamp.flows")
    )));
    private final StartedMockNode nodeA = network.createNode();
    private final StartedMockNode nodeB = network.createNode();

    @Before
    public void setup() {
        network.runNetwork();
        HouseIssueFlow issueFlow = new HouseIssueFlow("Powai Mumbai", "2000sqft", 3, 2019);
        nodeA.startFlow(issueFlow);
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void flowHasTheCorrectInputUsedInTheTransaction() throws Exception {

        HouseTransferFlow.HouseTransferInitiator flow = new HouseTransferFlow.HouseTransferInitiator(
                nodeB.getInfo().getLegalIdentities().get(0),"Powai Mumbai");
        nodeA.startFlow(flow);

        StateAndRef<HouseState> inputStateAndRef = flow.getInputState();
        HouseState houseState = inputStateAndRef.getState().getData();

        assertEquals("Powai Mumbai", houseState.getAddress());
        assertEquals("2000sqft", houseState.getBuildArea());
        assertEquals(new Integer(3), houseState.getNumberOfBedRooms());
        assertEquals(new Integer(2019), houseState.getConstructionYear());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), houseState.getBuilder());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), houseState.getOwner());
    }

    @Test
    public void flowHasAHouseStateOutputWithTheCorrectDetails() throws Exception {
        HouseTransferFlow.HouseTransferInitiator flow = new HouseTransferFlow.HouseTransferInitiator(
                nodeB.getInfo().getLegalIdentities().get(0),"Powai Mumbai");
        nodeA.startFlow(flow);


        assertNotNull(flow.getOutputState());
        HouseState outputState = flow.getOutputState();
        assertEquals("Powai Mumbai", outputState.getAddress());
        assertEquals("2000sqft", outputState.getBuildArea());
        assertEquals(new Integer(3), outputState.getNumberOfBedRooms());
        assertEquals(new Integer(2019), outputState.getConstructionYear());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), outputState.getBuilder());
        assertEquals(nodeB.getInfo().getLegalIdentities().get(0), outputState.getOwner());
    }

    @Test
    public void flowUsesTheTransferCommandAndCorrectSigner() throws Exception {
        HouseTransferFlow.HouseTransferInitiator flow = new HouseTransferFlow.HouseTransferInitiator(
                nodeB.getInfo().getLegalIdentities().get(0),"Powai Mumbai");
        nodeA.startFlow(flow);

        assertTrue(flow.getCommand().getValue() instanceof HouseContract.Commands.Transfer);
        assertEquals(2, flow.getCommand().getSigners().size());
        assertTrue(flow.getCommand().getSigners().contains(nodeA.getInfo().getLegalIdentities().get(0).getOwningKey()) &&
                flow.getCommand().getSigners().contains(nodeB.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    @Test
    public void flowSelectsTheCorrectNotaryForTheTransaction() throws Exception {
        HouseTransferFlow.HouseTransferInitiator flow = new HouseTransferFlow.HouseTransferInitiator(
                nodeB.getInfo().getLegalIdentities().get(0),"Powai Mumbai");
        nodeA.startFlow(flow);

        assertEquals(flow.getInputState().getState().getNotary(), flow.getNotaryFromInputState());
    }

    @Test
    public void flowBuildsTheTransactionCorrectly() throws Exception{
        HouseTransferFlow.HouseTransferInitiator flow = new HouseTransferFlow.HouseTransferInitiator(
                nodeB.getInfo().getLegalIdentities().get(0),"Powai Mumbai");
        nodeA.startFlow(flow);

        TransactionBuilder txBuilder = flow.getTransactionBuilder();
        assertEquals(flow.getInputState().getState().getNotary(), txBuilder.getNotary());

        HouseState outputState = ((HouseState)txBuilder.outputStates().get(0).getData());
        assertEquals("Powai Mumbai", outputState.getAddress());
        assertEquals("2000sqft", outputState.getBuildArea());
        assertEquals(new Integer(3), outputState.getNumberOfBedRooms());
        assertEquals(new Integer(2019), outputState.getConstructionYear());
        assertEquals(nodeA.getInfo().getLegalIdentities().get(0), outputState.getBuilder());
        assertEquals(nodeB.getInfo().getLegalIdentities().get(0), outputState.getOwner());


        assertTrue(txBuilder.commands().get(0).getValue() instanceof HouseContract.Commands.Transfer);
        assertEquals(2, txBuilder.commands().get(0).getSigners().size());
        assertTrue(txBuilder.commands().get(0).getSigners().contains(nodeA.getInfo().getLegalIdentities().get(0).getOwningKey())
            && txBuilder.commands().get(0).getSigners().contains(nodeB.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }
}
