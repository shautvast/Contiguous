package nl.sanderhautvast.contiguous;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListSerializerTest {


    private ObjectMapper mapper;

    @BeforeEach
    public void setup(){
        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule("mySerializers");
        module.addSerializer(new ListSerializer<>());
        mapper.registerModule(module);
    }
    @Test
    public void testStringList() throws JsonProcessingException {
        ContiguousList<String> strings = new ContiguousList<>(String.class);
        strings.add("Vogon constructor fleet");
        strings.add("Restaurant at the end of the Galaxy");
        strings.add("Publishing houses of Ursa Minor");

        String json = mapper.writeValueAsString(strings);
        assertEquals("[\"Vogon constructor fleet\"," +
                        "\"Restaurant at the end of the Galaxy\"," +
                        "\"Publishing houses of Ursa Minor\"]",
                json);
    }

    @Test
    public void testObjectList() throws JsonProcessingException {
        ContiguousList<AdamsObject> strings = new ContiguousList<>(AdamsObject.class);
        strings.add(new AdamsObject("Vogon constructor fleet"));
        strings.add(new AdamsObject("Restaurant at the end of the Galaxy"));
        strings.add(new AdamsObject("Publishing houses of Ursa Minor"));



        String json = mapper.writeValueAsString(strings);
        assertEquals("[{\"name\": \"Vogon constructor fleet\"}," +
                        "{\"name\": \"Restaurant at the end of the Galaxy\"}," +
                        "{\"name\": \"Publishing houses of Ursa Minor\"}]",
                json);
    }
}