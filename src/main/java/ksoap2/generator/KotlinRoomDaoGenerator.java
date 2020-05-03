/**
 * Ksoap2-generator-stub: the generating to generate web services client using
 * ksoap2 (http://ksoap2.sourceforge.net/) in J2ME/CLDC 1.1 and Android
 * (http://code.google.com/p/ksoap2-android/).
 * <p>
 * Copyright: Copyright (C) 2010
 * Contact: kinhnc@gmail.com
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * <p>
 * Initial developer(s): Cong Kinh Nguyen.
 * Contributor(s):
 */


package ksoap2.generator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class to generate complex type.
 *
 * @author Cong Kinh Nguyen
 */
public final class KotlinRoomDaoGenerator extends AbstractGenerator {

    /**
     *
     */
    private static int char_distance = 'A' - 'a';

    /**
     * Ignored attributes.
     */
    private static final String[] ignoredAttributes = {"__equalsCalc", "__hashCodeCalc", "typeDesc"};

    /**
     * Public constructor.
     *
     * @param serviceClassesHM
     * @param clazz           The class.
     * @param writer          The writer.
     * @param generatedFolder
     */
    public KotlinRoomDaoGenerator(HashMap<String, Class<?>> serviceClassesHM, final Class<?> clazz, Writer writer, final String generatedFolder) {
        super(serviceClassesHM, clazz, writer, generatedFolder);
        className = clazz.getSimpleName() + "Dao";
        nameSpace = clazz.getPackage().getName() + ".room.dao";
        isKotlin = true;
    }

    /**
     * Runs this method to generate classes in J2ME based on them in J2SE.
     *
     * @throws GeneratorException The generation exception.
     * @see AbstractGenerator#run()
     */

    @Override
    protected void run() throws GeneratorException {
        super.run();
    }

    /**
     * {@link AbstractGenerator#writeClass(Class)}
     */
    @Override
    protected void writeClass(Class<?> clazz) throws GeneratorException {
        writeTransactionMethod(clazz);
        writeSelectMethod(clazz);
        writeIsExistsMethod(clazz);
        writeInsertMethod(clazz);
        writeDeleteMethod(clazz);
        writeCountMethod(clazz);
        writeUpdateMethod(clazz);
    }

    private void writeTransactionMethod(Class<?> clazz) {
        Writer writer = getWriter();
        writer.append("    @Transaction\n");
        writer.append("    suspend fun syncData(" + firstLetterLowerCase(clazz.getSimpleName()) + "s: Array<" + clazz.getSimpleName() + ">){\n");
        writer.append("        deleteAll()\n");
        writer.append("        insertRange(" + firstLetterLowerCase(clazz.getSimpleName()) + "s)\n");
        writer.append("    }\n\n");

    }

    private void writeSelectMethod(final Class<?> clazz) {
        Writer writer = getWriter();

        // Write ordinary select method
        writer.append("    @Query(\"SELECT * FROM " + clazz.getSimpleName() + "\")\n");
        writer.append("    suspend fun selectAll(): LiveData<Array<" + clazz.getSimpleName() + ">>\n\n");

        Field[] attributes = clazz.getDeclaredFields();
        if (attributes == null) {
            return;
        }

        for (Field attribute : attributes) {
            String name = attribute.getName();
            String type = convertObjectType(attribute.getType()).getSimpleName();
            if (!attribute.getType().isPrimitive() && attribute.getType() != String.class) {
                continue; // Select the following primitive value as key
            }
            if (isIgnored(name)) {
                continue;
            }
            // Write select single with id
            writer.append("    @Query(\"SELECT * FROM " + clazz.getSimpleName() + " WHERE " + name + " = :" + name + "\")\n");
            writer.append("    suspend fun selectSingleBy(" + name + ": " + type + "): LiveData<" + clazz.getSimpleName() + ">\n\n");

            // Write select all with id
            writer.append("    @Query(\"SELECT * FROM " + clazz.getSimpleName() + " WHERE " + name + " = :" + name + "\")\n");
            writer.append("    suspend fun selectAllBy(" + name + ": " + type + "): LiveData<Array<" + clazz.getSimpleName() + ">>\n\n");

            // Write select with array of id
            writer.append("    @Query(\"SELECT * FROM " + clazz.getSimpleName() + " WHERE " + name + " IN (:" + name + ")\")\n");
            writer.append("    suspend fun selectAllByRange(" + name + ": Array<" + type + ">): LiveData<Array<" + clazz.getSimpleName() + ">>\n\n");
            // break after this to assume that first attribute is the unique key value.
            break;
        }
    }

    private void writeIsExistsMethod(final Class<?> clazz) {
        Writer writer = getWriter();
        Field[] attributes = clazz.getDeclaredFields();
        if (attributes == null) {
            return;
        }

        for (Field attribute : attributes) {
            String name = attribute.getName();
            String type = convertObjectType(attribute.getType()).getSimpleName();

            if (!attribute.getType().isPrimitive() && attribute.getType() != String.class) {
                continue; // Select the following primitive value as key
            }

            if (isIgnored(name)) {
                continue;
            }
            // Is single item exists
            writer.append("    @Query(\"SELECT EXISTS(SELECT 1 FROM " + clazz.getSimpleName() + " WHERE " + name + " = :" + name + ")\")\n");
            writer.append("    suspend fun isExists(" + name + ": " + type + "): LiveData<Boolean>\n\n");
            // break after this to assume that first attribute is the unique key value.
            break;
        }
    }

    private void writeInsertMethod(final Class<?> clazz) {
        Writer writer = getWriter();
        writer.append("    @Insert(onConflict = OnConflictStrategy.REPLACE)\n");
        writer.append("    suspend fun insert(" + firstLetterLowerCase(clazz.getSimpleName()) + ": " + clazz.getSimpleName() + "): Long\n\n");

        writer.append("    @Insert(onConflict = OnConflictStrategy.REPLACE)\n");
        writer.append("    suspend fun insertRange(" + firstLetterLowerCase(clazz.getSimpleName()) + "s: Array<" + clazz.getSimpleName() + ">): Long\n\n");
    }

    private void writeDeleteMethod(final Class<?> clazz) {
        Writer writer = getWriter();
        // Delete Single
        writer.append("    @Delete\n");
        writer.append("    suspend fun delete(" + firstLetterLowerCase(clazz.getSimpleName()) + ": " + clazz.getSimpleName() + ")\n\n");

        // Delete Many
        writer.append("    @Delete\n");
        writer.append("    suspend fun deleteRange(" + firstLetterLowerCase(clazz.getSimpleName()) + "s: Array<" + clazz.getSimpleName() + ">)\n\n");

        // Delete All
        writer.append("    @Delete\n");
        writer.append("    suspend fun deleteAll()\n\n");
    }

    private void writeCountMethod(Class<?> clazz) {
        Writer writer = getWriter();
        // Count
        writer.append("    @Query(\"SELECT COUNT(*) FROM " + clazz.getSimpleName() + "\")\n");
        writer.append("    suspend fun count(): LiveData<Int>\n\n");
    }

    private void writeUpdateMethod(Class<?> clazz) {
        Writer writer = getWriter();
        // Update
        writer.append("    @Update\n");
        writer.append("    suspend fun update(" + firstLetterLowerCase(clazz.getSimpleName()) + ": " + clazz.getSimpleName() + "): LiveData<Int>\n\n");

        // Update Range
        writer.append("    @Update\n");
        writer.append("    suspend fun updateRange(" + firstLetterLowerCase(clazz.getSimpleName()) + "s: Array<" + clazz.getSimpleName() + ">): LiveData<Int>\n\n");
    }


    /**
     * {@link AbstractGenerator#writeImportedClasses(Class, Writer)}
     */
    @Override
    protected void writeImportedClasses(Class<?> clazz, Writer writer) throws GeneratorException {
        Util.checkNull(clazz, writer);
        writer.append("import androidx.lifecycle.LiveData\n");
        writer.append("import androidx.room.*\n");
        writer.append("import " + clazz.getPackage().getName() + ".model.*\n");
    }

    /**
     * Declares the class.
     *
     * @param clazz The class which is taken by Java reflection API.
     * @throws GeneratorException The generation exception.
     */
    @Override
    protected final void writeClassDeclaration(final Class<?> clazz) throws GeneratorException {
        Writer writer = getWriter();
        Util.checkNull(clazz, writer);

        writer.append("/**\n");
        writer.append("* The Data Access Object for the [" + clazz.getSimpleName() + "] class.\n");
        writer.append("*/\n");
        writer.append("@Dao\n");
        writer.append("interface " + className + " {\n\n");
    }

    /**
     * Outs the declared attributes. This method is overridden due to generated
     * attributes, for example: __equalsCalc, __hashCodeCalc, typeDesc.
     *
     * @param declaredFields The declared fields.
     * @throws GeneratorException The generation exception.
     */
    @Override
    protected final void writeAttributes(final Field declaredFields[]) throws GeneratorException {
        // Override to don't write any attributes.
    }

    /**
     * @param attrName The attribute name.
     * @return <tt>true</tt> if the attribute is ignored, and <tt>false</tt>
     * otherwise.
     */
    private boolean isIgnored(final String attrName) {
        for (String ignored : ignoredAttributes) {
            if (ignored.equals(attrName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param ch The input.
     * @return The caps char.
     */
    private char getCapsChar(char ch) {
        if (('a' <= ch) && (ch <= 'z')) {
            return (char) (ch + char_distance);
        }
        return ch;
    }

    private Class<?> convertObjectType(Class<?> type) {
        if (type == boolean.class) {
            return Boolean.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == byte.class) {
            return Byte.class;
        } else if (type == int.class) {
            return Integer.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type.isArray()) {
            return convertObjectType(type.getComponentType());
        } else {
            return type;
        }
    }

    /**
     * Convert first character to lower case.
     *
     * @param stringValue
     * @return
     */
    private String firstLetterLowerCase(String stringValue) {
        if (stringValue == null || stringValue == "") {
            return stringValue;
        }
        return stringValue.substring(0, 1).toLowerCase() + stringValue.substring(1);
    }

}
