package bootcamp.contracts;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StateTests {

    private final Party builder = new TestIdentity(new CordaX500Name("Builder", "", "IN")).getParty();
    private final Party owner = new TestIdentity(new CordaX500Name("Owner", "", "IN")).getParty();

    @Test
    public void houseStateMustImplementContractStateInterface() {
        try {
            Class<HouseState> clazz = HouseState.class;
            Constructor<HouseState> constructor = clazz.getConstructor(String.class, String.class, Integer.class,
                    Integer.class, Party.class, Party.class);
            assert (constructor.newInstance(null, null, null, null, null, null) instanceof  ContractState);
        }catch (Exception e){ e.printStackTrace(); }
    }

    @Test
    public void houseStateHasAddressBuildAreaNumberOfBedRoomsAndConstructionYearAsProperties() {
        try {
            Class<HouseState> clazz = HouseState.class;
            Constructor<HouseState> constructor = clazz.getConstructor(String.class, String.class, Integer.class,
                    Integer.class, Party.class, Party.class);
            Field[] fieldsArray = constructor.newInstance(null, null, null, null, null, null).getClass().getDeclaredFields();
            List<String> fields = new ArrayList<>();
            for(Field field: fieldsArray){
               fields.add(field.getName());
            }
            assert(fields.contains("address"));
            assert(fields.contains("buildArea"));
            assert(fields.contains("numberOfBedRooms"));
            assert(fields.contains("constructionYear"));
        }catch (Exception e){ e.printStackTrace(); }
    }

    @Test
    public void houseStateHasBuilderAndOwnerAsParticipants() {
        try {
            Class<HouseState> clazz = HouseState.class;
            Constructor<HouseState> constructor = clazz.getConstructor(String.class, String.class, Integer.class,
                    Integer.class, Party.class, Party.class);
            Field[] fieldsArray = constructor.newInstance(null, null, null, null, null, null).getClass().getDeclaredFields();
            List<String> fields = new ArrayList<>();
            for(Field field: fieldsArray){
                fields.add(field.getName());
                if(field.getName().equals("builder") || field.getName().equals("owner")){
                    assert(field.getType().getName().equals("net.corda.core.identity.Party"));
                }
            }
            assert(fields.contains("builder"));
            assert(fields.contains("owner"));
        }catch (Exception e){ e.printStackTrace(); }
    }

    @Test
    public void houseStateHasAParameterizedConstructorWithAllPropertiesAndParticipants(){
         new HouseState("Powai, Mumbai", "2000sqft", 3, 2019, builder, owner);
    }

    @Test
    public void houseStateHasTwoParticipantsTheBuilderAndTheOwner() {
        HouseState houseState = new HouseState("Powai, Mumbai", "2000sqft", 3,
                3, builder, owner);
        assertEquals(2, houseState.getParticipants().size());
        assertTrue(houseState.getParticipants().contains(builder));
        assertTrue(houseState.getParticipants().contains(owner));
    }

    @Test
    public void houseStateHasGettersForAllPropertiesAndParticipants() {
        HouseState houseState = new HouseState("Powai, Mumbai", "2000sqft", 3,
                2019, builder, owner);
        assertEquals("Powai, Mumbai", houseState.getAddress());
        assertEquals("2000sqft", houseState.getBuildArea());
        assertEquals(new Integer(3), houseState.getNumberOfBedRooms());
        assertEquals(new Integer(2019), houseState.getConstructionYear());
        assertEquals(builder, houseState.getBuilder());
        assertEquals(owner, houseState.getOwner());
    }

}
