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

import org.apache.axis.utils.bytecode.ParamReader;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Class to generate Web services client in J2ME based on Ksoap.
 *
 * @author Cong Kinh Nguyen
 */
public final class KotlinSoapCoroutineWrapperGenerator extends AbstractGenerator {
    private Class<?> stubClass;

    private String stubClientName;

    private String stubClientType;

    public KotlinSoapCoroutineWrapperGenerator(final Class<?> clazz, final Class<?> stubClass, Writer writer, final String generatedFolder) {
        super(new HashMap<String, Class<?>>(), clazz, writer, generatedFolder);
        this.stubClass = stubClass;

        String replacementName = clazz.getSimpleName().replace("Stub", "");
        int index = replacementName.indexOf("_");
        replacementName = replacementName.replaceFirst("_","");
        className = toUpperSpecificPosition(replacementName,index)+ "Async";
        isKotlin = true;
        stubClientName = firstLetterLowerCase(toUpperSpecificPosition(replacementName,index));
        stubClientType = toUpperSpecificPosition(replacementName,index);
        nameSpace = clazz.getPackage().getName() + ".soap";
    }

    /**
     * Runs this method to generate classes in J2ME based on them in J2SE.
     *
     * @throws GeneratorException The generation exception.
     * @see AbstractGenerator#run()
     */
    protected void run() throws GeneratorException {
        super.run();
    }

    @Override
    protected void writeCustomMethods(Writer writer) {
        writeExceptionParseMethod(writer);
        writeInit(writer);
    }

    private void writeInit(Writer writer) {
        writer.append("    private val " + stubClientName + ": " + stubClientType + " = " + stubClientType + "()\n\n");
    }

    /**
     * @see AbstractGenerator#writeClass(Class)
     */
    @Override
    protected void writeClass(final Class<?> clazz) throws GeneratorException {
        Writer writer = getWriter();
        Util.checkNull(clazz);
        Util.checkNull(stubClass);
        for (Method method : stubClass.getDeclaredMethods()) {
            if (hasMethod(clazz, method)) { // existed in the interface
                new MethodGenerator().run(clazz, method, writer);
            }
        }
    }

    /**
     * Checks if the <tt>method</tt> was declared in the <tt>clazz</tt>.
     *
     * @param clazz  The class.
     * @param method The method.
     * @return <tt>true</tt> if the <tt>method</tt> was declared in the
     * <tt>clazz</tt>, and <tt>false</tt> otherwise.
     */
    private boolean hasMethod(final Class<?> clazz, final Method method) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(method.getName())) {
                return true; // not necessary to check the ParaTypes.
            }
        }
        return false;
    }

    private void writeExceptionParseMethod(Writer writer) {

    }

    /**
     * Private class to generate the method at client for each web service.
     *
     * @author Cong Kinh Nguyen
     */
    private class MethodGenerator {

        public void run(final Class<?> clazz, final Method method, Writer writer) throws GeneratorException {

            Util.checkNull(method, writer);
            // Skip return type is void method
            if (method.getReturnType() == void.class) {
                return;
            }
            writeMethodName(method, writer);
            writeMethodContent(clazz, method, writer);
            writeDoCloseMethod(writer);
        }

        /**
         * Declares the method, for example
         * <pre>
         *     <code>public void test(String input) {</code>.
         * </pre>
         *
         * @param method The method.
         * @param writer The writer.
         * @throws GeneratorException The generation exception.
         */
        private void writeMethodName(final Method method, Writer writer) throws GeneratorException {

            //suspend fun getUserByUsernamePassword(userName: String, password: String) : Result<Array<SysUserS>>? {
            writer.append("    suspend fun " + method.getName() + "(");
            writeMethodParameters(method, writer, true);

            if (method.getReturnType().isArray()) {
                writer.append("): " + "Result<Array<" + method.getReturnType().getSimpleName().replace("[]", "") + ">>? {\n");
            } else {
                writer.append("): " + "Result<" + convertObjectType(method.getReturnType()).getSimpleName() + ">? {\n");
            }
        }


        /**
         * write the parameters for method
         *
         * @param method
         * @param writer
         * @param includeDataType
         * @throws GeneratorException
         */
        private void writeMethodParameters(final Method method, Writer writer, boolean includeDataType) throws GeneratorException {
            try {
                ParamReader pr = new ParamReader(method.getDeclaringClass());
                String[] params = pr.getParameterNames(method);
                if (params != null) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    int len = params.length;
                    if (len != paramTypes.length) {
                        throw new GeneratorException();
                    }
                    for (int i = 0; i < len; i++) {
                        if (includeDataType) {
                            String dataType = convertObjectType(paramTypes[i]).getSimpleName();
                            if (paramTypes[i].isArray()) {
                                dataType = "Array<" + convertObjectType(paramTypes[i]).getSimpleName().replace("[]", "") + ">";
                            }

                            if (i == 0) {
                                writer.append(params[i] + ": " + dataType);
                            } else {
                                writer.append(", " + params[i] + ": " + dataType);
                            }
                        } else {
                            String conversionType = "";
                            if (paramTypes[i].isArray() && paramTypes[i].getComponentType().isPrimitive()) {
                                conversionType = ".to" + convertObjectType(paramTypes[i]).getSimpleName().replace("[]", "") + "Array()";
                            }

                            if (i == 0) {
                                writer.append(params[i] + conversionType);
                            } else {
                                writer.append(", " + params[i] + conversionType);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new GeneratorException(e);
            }
        }

        /**
         * Creates the method's body.
         *
         * @param method The method.
         * @param writer The writer.
         * @throws GeneratorException The generation exception.
         */
        private void writeMethodContent(final Class<?> clazz, final Method method, Writer writer) throws GeneratorException {
            String namespace = "";
            try {
                namespace = getNameSpace(stubClass, method);
            } catch (Exception e) {
                e.printStackTrace();
            }
            writer.append("        return withContext(Dispatchers.IO) {\n");
            writer.append("                " + stubClientName + "." + method.getName() + "(");
            writeMethodParameters(method, writer, false);
            writer.append(")\n");
            writer.append("        }\n");
            //writer.append("}\n\n");
        }

        private String getNameSpace(Class proxyClass, Method method) throws Exception {

            Object ob = proxyClass.newInstance();
            Field field = proxyClass.getDeclaredField("cachedSerQNames");
            field.setAccessible(true);

            Vector value = (Vector) field.get(ob);

            for (Object cachedSerQName : value) {
                if (cachedSerQName instanceof QName) {

                    QName qName = ((QName) cachedSerQName);
                    //if(qName.getLocalPart().equals(method.getName())){
                    return qName.getNamespaceURI();
                    //}
                }
            }
            return "";
        }

        /**
         * Only the array of one dimension is supported.
         *
         * @param param
         * @param type
         * @param writer
         * @throws GeneratorException
         */
        private void writeMethodContentForSerialization(final String param, final Class<?> type, Writer writer) throws GeneratorException {
            writer.append("        ");
            if (type.isPrimitive()) {
                writer.append("_client.addProperty(\"" + param + "\", " + param + " + \"\");\n");
            } else {
                if (isSupported(type)) {
                    writer.append("_client.addProperty(\"" + param + "\", " + param + ");\n");
                } else if (type.equals(byte[].class)) {
                    writer.append("_client.addProperty(\"" + param
                            + "\", new org.ksoap2.serialization.SoapPrimitive("
                            + "SoapEnvelope.ENC, \"base64\", "
                            + "org.kobjects.base64.Base64.encode("
                            + param + ")));\n");
                } else if (type.isArray()) {
                    writeMethodContentForSerialOfArray(param, type, writer);
                } else { // other objects
                    writer.append("PropertyInfo property = new PropertyInfo();\n");
                    writer.append("        property.setNamespace(NAMESPACE);\n");
                    writer.append("        property.setName(\"" + param + "\");\n");
                    writer.append("        property.setValue(" + param + ");\n");
                    writer.append("        _client.addProperty(property);\n\n");
                }
            }
        }

        /**
         * Only the array of one dimension is processed to tested the result
         * of generation.
         *
         * @param param
         * @param type
         * @param writer
         * @throws GeneratorException
         */
        private void writeMethodContentForSerialOfArray(final String param, final Class<?> type, Writer writer) throws GeneratorException {

            writer.append("        if (" + param + " != null) {\n");
            writer.append("            int _len = " + param + ".length;\n");
            writer.append("            for (int _i = 0; _i < _len; _i++) {\n");
            writer.append("                ");
            if (type.equals(boolean[].class)) {
                writer.append("_vector.addElement(new Boolean(" + param + "[_i]));\n");
            } else if (type.equals(short[].class)) {
                writer.append("_vector.addElement(new Short(" + param + "[_i]));\n");
            } else if (type.equals(int[].class)) {
                writer.append("_vector.addElement(new Integer(" + param + "[_i]));\n");
            } else if (type.equals(long[].class)) {
                writer.append("_vector.addElement(new Long(" + param + "[_i]));\n");
            } else if (type.equals(float[].class)) {
                writer.append("_vector.addElement(new Float(" + param + "[_i]));\n");
            } else if (type.equals(double[].class)) {
                writer.append("_vector.addElement(new Double(" + param + "[_i]));\n");
            } else if ((type.equals(Boolean[].class)) ||
                    (type.equals(Byte[].class)) ||
                    (type.equals(Short[].class)) ||
                    (type.equals(Integer[].class)) ||
                    (type.equals(Long[].class)) ||
                    (type.equals(Float[].class)) ||
                    (type.equals(Double[].class)) ||
                    (type.equals(String[].class))) {
                writer.append("_vector.addElement(" + param + "[_i]);\n");
            } else {
                // for array of other objects and array of more one dimension
                writer.append("_vector.addElement(" + param + "[_i]);\n");
            }
            writer.append("            }\n");
            writer.append("        }\n");
            writer.append("        ");
            writer.append("_client.addProperty(\"" + param + "\", _vector);\n");
        }

        /**
         * @param type The class.
         * @return <tt>true</tt> if the class type is supported, and
         * <tt>false</tt> otherwise.
         */
        private boolean isSupported(final Class<?> type) {
            Class<?>[] supported = {String.class,
                    Long.class,
                    Integer.class,
                    Short.class,
                    Byte.class,
                    Boolean.class,
                    Double.class,
                    Float.class};

            for (Class<?> clazz : supported) {
                if (type.equals(clazz)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Closes the method.
         *
         * @param writer The writer.
         */
        private void writeDoCloseMethod(Writer writer) {
            writer.append("    }\n\n\n");
        }
    }

    @Override
    protected void writeImportedClasses(Class<?> clazz, Writer writer) throws GeneratorException {
        Util.checkNull(clazz, writer);

        writer.append("import kotlinx.coroutines.Dispatchers\n");
        writer.append("import kotlinx.coroutines.withContext\n");
        writer.append("import "+ clazz.getPackage().getName() + ".soap.model.*\n");
        writer.append("\n");
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
        } else if (type.isArray() && type.getComponentType().isPrimitive()) {
            return convertObjectType(type.getComponentType());
        } else {
            return type;
        }
    }

    /**
     * Convert first character to lower case.
     * @param stringValue
     * @return
     */
    private String firstLetterLowerCase(String stringValue){
        if(stringValue == null || stringValue == ""){
            return stringValue;
        }
        return stringValue.substring(0, 1).toLowerCase() + stringValue.substring(1);
    }

    private String toUpperSpecificPosition(String str, int index) {
        char[] chars = str.toCharArray();
        return str.substring(0, index) + String.valueOf(chars[index]).toUpperCase() + str.substring(index+1);
    }
}