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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * Class to generate complex type.
 *
 * @author Cong Kinh Nguyen
 */
public final class KotlinRoomClassGenerator extends AbstractGenerator {

    /**
     *
     */
    private static int char_distance = 'A' - 'a';

    /**
     *
     */
    private static Map<String, String> primitive2ObjectMappings =
            new HashMap<String, String>();

    static {
        primitive2ObjectMappings.put("boolean", "Boolean");
        primitive2ObjectMappings.put("byte", "Byte");
        primitive2ObjectMappings.put("short", "Short");
        primitive2ObjectMappings.put("int", "Integer");
        primitive2ObjectMappings.put("long", "Long");
        primitive2ObjectMappings.put("float", "Float");
        primitive2ObjectMappings.put("double", "Double");
    }

    /**
     *
     */
    private static Map<String, String> object2PrimitiveMappings =
            new HashMap<String, String>();

    static {
        object2PrimitiveMappings.put("boolean", "\"true\".equals");
        object2PrimitiveMappings.put("byte", "Byte.parseByte");
        object2PrimitiveMappings.put("short", "Short.parseShort");
        object2PrimitiveMappings.put("int", "Integer.parseInt");
        object2PrimitiveMappings.put("long", "Long.parseLong");
        object2PrimitiveMappings.put("float", "Float.parseFloat");
        object2PrimitiveMappings.put("double", "Double.parseDouble");
    }

    /**
     * Ignored attributes.
     */
    private static final String[] ignoredAttributes = {"__equalsCalc", "__hashCodeCalc", "typeDesc"};

    /**
     * Public constructor.
     *
     * @param serviceClassesHM
     * @param clazz            The class.
     * @param writer           The writer.
     * @param generatedFolder
     */
    public KotlinRoomClassGenerator(HashMap<String, Class<?>> serviceClassesHM, final Class<?> clazz, Writer writer, final String generatedFolder) {
        super(serviceClassesHM, clazz, writer, generatedFolder);
        nameSpace = clazz.getPackage().getName() + ".room.model";
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
        //writeConstructor(clazz);
        //writeConstructorWithParameters(clazz);
        //writeSetGetMethods(clazz);
        //writeSpecialMethodsOnSoapObject(clazz);
    }

    /**
     * {@link AbstractGenerator#writeImportedClasses(Class, Writer)}
     */
    @Override
    protected void writeImportedClasses(Class<?> clazz, Writer writer) throws GeneratorException {
        Util.checkNull(clazz, writer);
        writer.append("import androidx.annotation.NonNull;\n");
        writer.append("import androidx.room.Entity;\n");
        writer.append("import androidx.room.PrimaryKey;\n");
        writer.append("import androidx.room.ColumnInfo;\n\n");
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
        writer.append("@Entity\n");
        writer.append("data class " + clazz.getSimpleName() + " (\n\n");
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

        Writer writer = getWriter();
        Util.checkNull(declaredFields, writer);
        boolean isFirst = true;
        for (Field declaredField : declaredFields) {
            if (!isIgnored(declaredField.getName())) {
                String comma = ",";
                String nullable = "?";
                if (isFirst) {
                    writer.append("    ");
                    writer.append("@NonNull ");
                    writer.append("@PrimaryKey ");
                    isFirst = false;
                    nullable = "";
                } else {
                    writer.append("    ");
                    writer.append("@ColumnInfo(name = \"" + declaredField.getName() + "\") ");
                    nullable = "?";
                }


                // If is last elements, remove the comma
                if (declaredField == declaredFields[declaredFields.length - 1 - ignoredAttributes.length]) {
                    comma = "";
                }

                if (declaredField.getType().isArray()) {
                    writer.append("val " + declaredField.getName() + ": Array<" + convertObjectType(declaredField.getType()).getSimpleName().replace("[]", "") + ">" + nullable + comma + "\n");
                } else {
                    writer.append("val " + declaredField.getName() + ": " + convertObjectType(declaredField.getType()).getSimpleName() + nullable + comma + "\n");
                }
            }
        }
        writer.append("\n");
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


    private boolean isServiceClass(String className) {
        for (Map.Entry<String, Class<?>> serviceClass : serviceClassesHM.entrySet()) {
            if (serviceClass.getKey().equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Outs the class's close.
     */
    @Override
    protected void writeClassClose() {
        Writer writer = getWriter();
        writer.append(")\n");
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

}
