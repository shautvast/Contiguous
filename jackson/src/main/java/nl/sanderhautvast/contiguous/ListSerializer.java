package nl.sanderhautvast.contiguous;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ListSerializer<E> extends StdSerializer<ContiguousList<E>> {

    public ListSerializer(Class<ContiguousList<E>> t) {
        super(t);
    }

    @Override
    public void serialize(
            ContiguousList<E> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

//        value.

//        jgen.writeStartObject();
//        jgen.writeNumberField("id", value.id);
//        jgen.writeStringField("itemName", value.itemName);
//        jgen.writeNumberField("owner", value.owner.id);
//        jgen.writeEndObject();
    }
}

