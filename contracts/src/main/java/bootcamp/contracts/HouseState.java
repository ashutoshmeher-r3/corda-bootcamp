package bootcamp.contracts;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

// *********
// * HouseState *
// *********
/**
 * TODO 1: Implement ContractState
 */
@BelongsToContract(HouseContract.class)
public class HouseState implements ContractState {

    /**
     * TODO 2: List properties of House State
     */
    private final String address;
    private final String buildArea;
    private final Integer numberOfBedRooms;
    private final Integer constructionYear;

    /**
     * TODO 3: List Participants of the House State
     */
    private final Party builder;
    private final Party owner;


    /**
     * TODO 4: Create the constructor
     * Parameterized Constructor to instantiate the house state with properties and parties
     */
    public HouseState(String address, String buildArea, Integer numberOfBedRooms, Integer constructionYear,
                      Party builder, Party owner) {
        this.address = address;
        this.buildArea = buildArea;
        this.numberOfBedRooms = numberOfBedRooms;
        this.constructionYear = constructionYear;
        this.builder = builder;
        this.owner = owner;
    }


    /**
     * TODO 5: Retun the Paticipant list from getParticipants method
     * @return participantList
     *
     * This method should return a list of all participants who should know about the house.
     */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(builder, owner);
    }

    /**
     * TODO 6: Create the Getters for All Properties and Participants
     * Getters
     */
    public String getAddress() {
        return address;
    }

    public String getBuildArea() {
        return buildArea;
    }

    public Integer getNumberOfBedRooms() {
        return numberOfBedRooms;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public Party getBuilder() {
        return builder;
    }

    public Party getOwner() {
        return owner;
    }
}