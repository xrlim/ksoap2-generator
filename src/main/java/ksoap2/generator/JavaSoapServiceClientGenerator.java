/**
 Ksoap2-generator-stub: the generating to generate web services client using
 ksoap2 (http://ksoap2.sourceforge.net/) in J2ME/CLDC 1.1 and Android
 (http://code.google.com/p/ksoap2-android/).
 
 Copyright: Copyright (C) 2010
 Contact: kinhnc@gmail.com

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 USA 

 Initial developer(s): Cong Kinh Nguyen.
 Contributor(s):
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
 *
 */
public final class JavaSoapServiceClientGenerator extends AbstractGenerator {

    /**
     * For J2ME.
     */
    public static final String HTTP_TRANSPORT_J2ME = "HttpTransport";

    /**
     * For Android.
     */
    public static final String HTTP_TRANSPORT_ANDROID = "HttpTransportSE";

    /**
     * The method to perform web services's call
     */
    public static String HTTP_TRANSPORT = HTTP_TRANSPORT_J2ME;

    private final List<Operation> operationList;

    private Class <?> stubClass;

    public JavaSoapServiceClientGenerator(final Class<?> clazz, final Class <?> stubClass, Writer writer, final String generatedFolder, List<Operation> operationList) {

        super(new HashMap<String, Class<?>>(), clazz, writer, generatedFolder);
        this.stubClass = stubClass;
        this.operationList = operationList;
        nameSpace = clazz.getPackage().getName() + ".soap";

        String replacementName = stubClass.getSimpleName().replace("Stub", "");
        int index = replacementName.indexOf("_");
        replacementName = replacementName.replaceFirst("_","");
        className = toUpperSpecificPosition(replacementName,index);
    }

    /**
     * Runs this method to generate classes in J2ME based on them in J2SE.
     *
     * @throws GeneratorException
     *              The generation exception.
     * @see AbstractGenerator#run()
     */
    protected void run() throws GeneratorException {
        super.run();
        FileManager.copyConf(nameSpace, getGeneratedFolder());
        FileManager.copyResult(nameSpace, getGeneratedFolder());
    }

	@Override
	protected void writeCustomMethods(Writer writer) {
		writeExceptionParseMethod(writer);
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
                new MethodGenerator().run(method, writer, operationList);
            }
        }
    }

    /**
     * Checks if the <tt>method</tt> was declared in the <tt>clazz</tt>.
     *
     * @param clazz
     *              The class.
     * @param method
     *              The method.
     * @return <tt>true</tt> if the <tt>method</tt> was declared in the
     * <tt>clazz</tt>, and <tt>false</tt> otherwise.
     */
    private boolean hasMethod(final Class <?> clazz, final Method method) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(method.getName())) {
                return true; // not necessary to check the ParaTypes.
            }
        }
        return false;
    }

	private void writeExceptionParseMethod(Writer writer) {
		writer.append("     public void convertToException(org.ksoap2.SoapFault fault, SoapSerializationEnvelope envelope) throws java.lang.Exception {\n");
		writer.append("         //TODO: meed to implement\n");
		writer.append("    };\n\n");
	}

    /**
     * Private class to generate the method at client for each web service.
     *
     * @author Cong Kinh Nguyen
     *
     */
    private class MethodGenerator {

        public void run(final Method method, Writer writer, List<Operation> operationList) throws GeneratorException {

            Util.checkNull(method, writer);
            // Skip return type is void method
            if(method.getReturnType() == void.class) {
                return;
            }
            writeMethodName(method, writer);
            writeMethodContent(method, writer, operationList);
            writeDoCloseMethod(writer);
        }

	    /**
         * Declares the method, for example
         * <pre>
         *     <code>public void test(String input) {</code>.
         * </pre> 
         *
         * @param method
         *              The method.
         * @param writer
         *              The writer.
         * @throws GeneratorException
         *              The generation exception.
         */
        private void writeMethodName(final Method method, Writer writer) throws GeneratorException {

            writer.append("    " + getModifier(method.getModifiers()) + "Result<" + convertObjectType(method.getReturnType()).getSimpleName() + "> " + method.getName() + "(");
            try {
                ParamReader pr = new ParamReader(method.getDeclaringClass());
                String [] params = pr.getParameterNames(method);
                if (params != null) {
                    Class <?> [] paramTypes = method.getParameterTypes();
                    int len = params.length;
                    if (len != paramTypes.length) {
                        throw new GeneratorException();
                    }
                    for (int i = 0; i < len; i++) {
                        if (i == 0) {
                            writer.append(paramTypes[i].getSimpleName() + " " + params[i]);
                        } else {
                            writer.append(", " + paramTypes[i].getSimpleName() + " " + params[i]);
                        }
                    }
                }
            } catch (IOException e) {
                throw new GeneratorException(e);
            }
            writer.append(") {\n");
        }

        /**
         * Creates the method's body.
         *
         * @param method The method.
         * @param writer The writer.
         * @throws GeneratorException The generation exception.
         */
        private void writeMethodContent(final Method method, Writer writer, List<Operation> operationList) throws GeneratorException {
	        String namespace = "";
	        try {
		        namespace = getNameSpace(stubClass, method);
	        } catch (Exception e) {
		        e.printStackTrace();
	        }

            Operation operation = operationList.stream().filter(op -> op.getName().compareToIgnoreCase(method.getName()) == 0).findFirst().orElse(null);
            String methodName = method.getName();
	        if(operation != null) {
	            methodName = operation.getName();
            }

	        writer.append("        String nameSpace = \"" + namespace + "\";\n");
            writer.append("        String methodName = \"" + methodName + "\";\n");
            writer.append("        String soapAction = nameSpace + methodName;\n");
	        writer.append("        SoapObject _client = new SoapObject(nameSpace, methodName);\n\n");
            ParamReader pr;
            try {
                Class <?> [] types = method.getParameterTypes();
                pr = new ParamReader(method.getDeclaringClass());
                String [] params = pr.getParameterNames(method);
                int len = types.length;
                if ((params != null) && (params.length != len)) {
                    throw new GeneratorException();
                }
                boolean isVectorDeclared = false;
                for (int i = 0; i < len; i++) {
                    if(types[i].isArray()) {
                        if (!isVectorDeclared) {
                            writer.append("        java.util.Vector _vector =  " + "new java.util.Vector();\n");
                            isVectorDeclared = true;
                        } else {
                            writer.append("        _vector =  " + "new java.util.Vector();\n");
                        }
                    }
                    writeMethodContentForSerialization(params[i], types[i], writer);
                }
            } catch (IOException e) {
                throw new GeneratorException(e);
            }
            writer.append("        SoapSerializationEnvelope _envelope = " + "new SoapSerializationEnvelope(SoapEnvelope.VER11);\n");
            writer.append("        _envelope.bodyOut = _client;\n");
	        writer.append("        _envelope.setOutputSoapObject(_client);\n");
	        writer.append("        _envelope.dotNet = true;\n");
	        writer.append("        _envelope.implicitTypes = true;\n");
	        writer.append("        _envelope.setAddAdornments(false);\n\n");

	        writer.append("        " + HTTP_TRANSPORT + " _ht = new " + HTTP_TRANSPORT + "(" + "Configuration.getWsUrl());\n");
            String soapReturnType = writeConvertSoapReturnType(method);
            writer.append("        "+soapReturnType+" _ret = null;\n");
            writer.append("        try {\n");
            writer.append("             _ht.call(soapAction, _envelope);\n");
           // writeReturnValue(method, writer);
            writer.append("             _ret = ("+soapReturnType+") _envelope.getResponse();\n");
	        writer.append("        } catch (Exception exception) {\n");
	        //writer.append("             convertToException(soapFault, _envelope);\n");
            writer.append("             return new Result.Error(exception);\n");
	        writer.append("        }\n\n");
            writeReturnValue(method, writer);
        }

	    private String getNameSpace(Class proxyClass, Method method) throws  Exception{

		    Object ob = proxyClass.newInstance();
		    Field field = proxyClass.getDeclaredField("cachedSerQNames");
		    field.setAccessible(true);

		    Vector value = (Vector)field.get(ob);

		    for (Object cachedSerQName : value) {
			    if(cachedSerQName instanceof QName){

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
        private void writeMethodContentForSerialization(final String param, final Class <?> type, Writer writer) throws GeneratorException {
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
        private void writeMethodContentForSerialOfArray(final String param, final Class <?> type, Writer writer) throws GeneratorException {

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
         * Array is not supported
         * 
         * @param method
         * @param writer
         * @throws GeneratorException
         */
        //TODO: reikia per=i8r4ti ir galbut panaudoti veliau
        private void writeReturnValue(final Method method, Writer writer) throws GeneratorException {
            Class <?> type = method.getReturnType();
            if (type.equals(void.class)) { // ignore the void type
            } else if(type.equals(String.class)) {
                writer.append("        return new Result.Success<"+type.getSimpleName()+">(_ret.toString());\n");
            } else if (type.isPrimitive()) { // primitive type
                if (type.equals(boolean.class)) {
                    writer.append("        return new Result.Success<>(Boolean.parseBoolean(_ret.toString()));\n");
                } else if (type.equals(byte.class)) {
                    writer.append("        return new Result.Success<>(Byte.parseByte(" + "_ret.toString()));\n");
                } else if (type.equals(short.class)) {
                    writer.append("        return new Result.Success<>(Short.parseShort(" + "_ret.toString()));\n");
                } else if (type.equals(int.class)) {
                    writer.append("        return new Result.Success<>(Integer.parseInt(" + "_ret.toString()));\n");
                } else if (type.equals(long.class)) {
                    writer.append("        return new Result.Success<>(Long.parseLong(" + "_ret.toString()));\n");
                } else if (type.equals(float.class)) {
                    writer.append("        return new Result.Success<>(Float.parseFloat(" + "_ret.toString()));\n");
                } else if (type.equals(double.class)) {
                    writer.append("        return new Result.Success<>(Double.parseDouble(" + "_ret.toString()));\n");
                } else { // char
                    writer.append("        return new Result.Success<"+type.getSimpleName()+">(_ret.toString().charAt(0));\n");
                }
            } else { // array or object extended on SoapObject
                if (!type.isArray()) { // object extended on SoapObject
                    writer.append("        int _len = _ret.getPropertyCount();\n");
                    writer.append("        " + type.getSimpleName() + " _returned = new " + type.getSimpleName() + "();\n");
                    writer.append("        for (int _i = 0; _i < _len; _i++) {\n");
                    writer.append("            _returned.setProperty(_i, _ret.getProperty(_i));");
                    writer.append("        }\n");
                    writer.append("        return _returned;\n");
                } else { // array
                    writer.append("       if(_ret.getPropertyCount() == 0){\n");
                    writer.append("       return new Result.Error(new Resources.NotFoundException(\""+ method.getName() +" didn't return any value.\"));\n");
                    writer.append("       }\n");
                    writer.append("       " + type.getSimpleName() + " returnArrayObject = new " + type.getSimpleName().replace("[]","") + "[_ret.getPropertyCount()];\n");
                    writer.append("       for (int rowIndex = 0; rowIndex < _ret.getPropertyCount(); rowIndex++) {\n");
                    writer.append("       returnArrayObject[rowIndex] = new "+type.getSimpleName().replace("[]","")+"((SoapObject) _ret.getProperty(rowIndex));\n");
                    writer.append("       }\n");
                    writer.append("       return new Result.Success<>(returnArrayObject);\n");
                }
            }
        }

        /**
         * Determine is soap object or soap primitive
         * @param method
         */
        private String writeConvertSoapReturnType(final Method method){
            Class <?> type = method.getReturnType();
            String soapReturnType = "SoapObject";
            if (type.equals(void.class)) { // ignore the void type
            } else if (type.isPrimitive() || type.equals(String.class)){
                return "SoapPrimitive";
            }

            return soapReturnType;
        }

        /**
         *
         * @param type
         *              The class.
         * @return <tt>true</tt> if the class type is supported, and
         * <tt>false</tt> otherwise.
         */
        private boolean isSupported(final Class <?> type) {
            Class <?> [] supported = {String.class,
		            Long.class,
		            Integer.class,
                    Short.class,
		            Byte.class,
		            Boolean.class,
		            Double.class,
                    Float.class};

            for (Class <?> clazz : supported) {
                if (type.equals(clazz)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Closes the method.
         *
         * @param writer
         *              The writer.
         */
        private void writeDoCloseMethod(Writer writer) {
            writer.append("    }\n\n\n");
        }
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

    private String toUpperSpecificPosition(String str, int index) {
        char[] chars = str.toCharArray();
        return str.substring(0, index) + String.valueOf(chars[index]).toUpperCase() + str.substring(index+1);
    }

    @Override
    protected void writeImportedClasses(Class<?> clazz, Writer writer) throws GeneratorException {
        Util.checkNull(clazz, writer);
        writer.append("import android.content.res.Resources;\n");
        writer.append("import org.ksoap2.SoapEnvelope;\n");
	    writer.append("import org.ksoap2.SoapFault;\n");
        writer.append("import org.ksoap2.serialization.SoapObject;\n");
        writer.append("import org.ksoap2.serialization.SoapPrimitive;\n");
        writer.append("import org.ksoap2.serialization.SoapSerializationEnvelope;\n");
        writer.append("import org.ksoap2.transport." + HTTP_TRANSPORT + ";\n");
	    writer.append("import org.ksoap2.serialization.PropertyInfo;\n");
        writer.append("import "+ clazz.getPackage().getName() + ".soap.model.*;\n");
        writer.append("\n");
	    //writer.append("import " + clazz.getPackage() + ".application.services.Configuration;\n\n");
    }


}