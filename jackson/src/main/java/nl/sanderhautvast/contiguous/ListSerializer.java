package nl.sanderhautvast.contiguous;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Iterator;

public class ListSerializer<E> extends StdSerializer<ContiguousList<?>> {

    @SuppressWarnings("unchecked")
    public ListSerializer() {
        super((Class) ContiguousList.class); // ?
    }

    @Override
    public void serialize(
            ContiguousList<?> clist, JsonGenerator generator, SerializerProvider provider)
            throws IOException {
        generator.writeStartArray();

        Iterator<String> jsons = clist.jsonIterator();
        while (jsons.hasNext()) {
            generator.writeRawValue(jsons.next());
        }

        generator.writeEndArray();
    }
}

