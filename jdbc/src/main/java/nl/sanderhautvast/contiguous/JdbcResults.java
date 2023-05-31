package nl.sanderhautvast.contiguous;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Enables the end user to add values from JDBC to a list of results without creating Objects.
 * Every property must have a corresponding field in the ResultSet record (table/view/join etc)
 * optionally applying a name mapping if they are not equal
 * <p>
 * // what about null values?
 * // test test test
 */
public class JdbcResults {

    /**
     * Adds the data to an existing CList
     *
     * @param result the JDBC ResultSet
     * @param list   The list to add to
     * @throws SQLException when db throws error..
     */
    public static void addAll(ResultSet result, ContiguousList<?> list) throws SQLException {
        addAll(result, list, Function.identity());
    }

    /**
     * Adds the data to an existing CList.
     *
     * The fieldNameMapper Function does not have to map for column names that do match. So only non-equal
     * names have to be mapped.
     *
     * @param result          the JDBC ResultSet
     * @param list            The list to add to
     * @param fieldNameMapper maps the name from the element type property to the actual database column name
     * @throws SQLException when db throws error..
     */
    public static void addAll(ResultSet result, ContiguousList<?> list, Function<String, String> fieldNameMapper) throws SQLException {
        ContiguousList<?>.SetterIterator setterIterator = list.setterIterator();
        while (result.next()) {
            while (setterIterator.hasNext()) {
                ContiguousList<?>.Setter next = setterIterator.next();
                String fieldName = next.getFieldName();
                Object fieldValue;
                if (fieldName != null) {
                    String columnName = fieldNameMapper.apply(fieldName);
                    if (columnName == null) {
                        columnName = fieldName;
                    } // would it be usefull if we could add this to the state (convert Function<> to Map<>)?
                      // so that next time the entry would be in the map...
                      // -> more branch predicability for the `if`
                    fieldValue = result.getObject(columnName);
                } else {
                    // assume single Primitive as Contiguous<String>, so just 1 column in the record
                    fieldValue = result.getObject(1);
                }
                next.set(fieldValue);
            }
            setterIterator.finishObject();
        }
    }

    /**
     * Same as addAll, but creates a new CList.
     *
     * @param result      The CList
     * @param elementType the desired Object type
     * @throws SQLException when db throws error..
     */
    public static <E> ContiguousList<E> toList(ResultSet result, Class<E> elementType) throws SQLException {
        return toList(result, elementType, Function.identity());
    }

    /**
     * Same as addAll, but creates a new CList.
     * <p>
     * The fieldNameMapper Function does not have to map for column names that do match. So only non-equal
     * names have to be mapped.
     * <p>
     * @param result          The CList
     * @param elementType     the desired Object type
     * @param fieldNameMapper maps the name from the element type property to the actual database column name
     * @throws SQLException when db throws error..
     */
    public static <E> ContiguousList<E> toList(ResultSet result, Class<E> elementType, Function<String, String> fieldNameMapper) throws SQLException {
        ContiguousList<E> list = new ContiguousList<>(elementType);
        addAll(result, list, fieldNameMapper);
        return list;
    }
}
