package nl.sanderhautvast.contiguous;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ListSerializer<E> extends StdSerializer<ContiguousList> {

    public ListSerializer() {
        super(ContiguousList.class);
    }

    @Override
    public void serialize(
            ContiguousList clist, JsonGenerator generator, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeStartArray();

        if (clist.isSimpleElementType()){
            Iterator<?> iterator = clist.valueIterator();
            while (iterator.hasNext()){
                generator.writeString(iterator.next().toString());
            }
        } else {

        }

        generator.writeEndArray();
    }
}

