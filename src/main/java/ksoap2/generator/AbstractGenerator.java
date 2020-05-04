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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class to generate classes in J2ME based on them in J2SE.
 *
 * @author Cong Kinh Nguyen
 *
 */
public abstract class AbstractGenerator {

    /**
     * Attribute to contain class input.
     */
    private Class<?> clazz;
    HashMap<String, Class<?>> serviceClassesHM;
    /**
     * Attribute to contain the writer.
     */
    private Writer writer;

    /**
     * The generated folder to store generated code.
     */
    private String generatedFolder;

    /**
     * The class name for the object
     */
    public String className;

    /**
     * The name space for the class
     */
    public String nameSpace;

    /**
     * Is Kotlin file
     */
    public boolean isKotlin = false;



    /**
     * Public constructor.
     *
     * @param serviceClassesHM
     * @param clazz
     *              The class.
     * @param writer
     *              The writer.
     * @param generatedFolder
     */
    public AbstractGenerator(HashMap<String, Class<?>> serviceClassesHM, final Class<?> clazz, Writer writer, final String generatedFolder) {
        this.clazz = clazz;
        this.writer = writer;
        this.generatedFolder = generatedFolder;
        this.serviceClassesHM = serviceClassesHM;
        className = clazz.getSimpleName();
        nameSpace = clazz.getPackage().getName();
    }

    /**
     * Runs this method to generate classes in J2ME based on them in J2SE.
     *
     * @throws GeneratorException
     *              The generation exception.
     */
    protected void run() throws GeneratorException {
        Util.checkNull(clazz, writer);
        writer.clear();
        writePackage(clazz);
        writeImportedClasses(clazz, writer);
        writeClassDeclaration(clazz);
        writeAttributes(clazz.getDeclaredFields());
        writeClass(clazz);
        writeCustomMethods(writer);
        writeClassClose();
        String fileExtension = isKotlin ? "kt" : "java";
        FileManager.createSourceFile(clazz, writer, generatedFolder, className, nameSpace, fileExtension);
    }

    protected void writeCustomMethods(Writer writer) {

    }

    /**
     * Outs class's package.
     *
     * @param clazz
     *              The class.
     */
    private void writePackage(final Class<?> clazz) {
        if(nameSpace!= null || !nameSpace.equals("")){
            writer.append("package " + nameSpace + ";\n\n");
            return;
        }

        String classname = clazz.getName();
        int index = classname.lastIndexOf('.');
        if (index >= 0) {
            String packageName = classname.substring(0, index);
            writer.append("package " + packageName + ";\n\n");
        }
    }

    /**
     * Outs imported classes.
     *
     * @param clazz
     *              The class.
     * @param writer
     *              The writer.
     * @throws GeneratorException
     *              The generation exception.
     */
    protected abstract void writeImportedClasses(final Class<?> clazz, Writer writer) throws GeneratorException;

    /**
     * Declares the class.
     *
     * @param clazz
     *              The class which is taken by Java reflection API.
     * @throws GeneratorException
     *              The generation exception.
     */
    protected void writeClassDeclaration(final Class<?> clazz) throws GeneratorException {
        Util.checkNull(clazz);
        writer.append("@SuppressWarnings(\"unchecked\")\n");
        if (!isKotlin) {
            writer.append("public final class " + className + " {\n\n");
        } else {
            writer.append("class " + className + " {\n\n");
        }
    }

    /**
     * Outs the declared attributes.
     *
     * @param declaredFields
     *              The declared fields.
     * @throws GeneratorException
     *              The generation exception.
     */
    protected void writeAttributes(final Field declaredFields[]) throws GeneratorException {
        Util.checkNull(declaredFields);
        for (Field declaredField : declaredFields) {
            writer.append(getModifier(declaredField.getModifiers())
                    + declaredField.getType().getCanonicalName()
                    + " " + declaredField.getName() + ";\n");
        }
    }

    /**
     *
     * @param modifier
     *              The modifier value.
     * @return one of three values: <tt>private</tt>, <tt>protected</tt>,
     * and <tt>public</tt>.
     */
    protected final String getModifier(int modifier) {
        if (modifier == Modifier.PRIVATE) {
            return "private ";
        } else if (modifier == Modifier.PROTECTED) {
            return "protected ";
        } else {
            return "public ";
        }
    }

    /**
     * Implements this method to generate classes in J2Me based on them in J2SE.
     * What we have to implement is the constructor(s) and the method(s).
     *
     * @param clazz
     *              The class.
     * @throws GeneratorException
     *              The generation exception.
     */
    protected abstract void writeClass(final Class<?> clazz) throws GeneratorException;

    /**
     * Outs the class's close.
     */
    protected void writeClassClose() {
        writer.append("}\n");
    }

    /**
     *
     * @return The input class of the constructor.
     */
    public final Class<?> getClazz() {
        return clazz;
    }

    /**
     *
     * @return The input writer of constructor.
     */
    public final Writer getWriter() {
        return writer;
    }

    /**
     *
     * @return The path to store generated code.
     */
    public final String getGeneratedFolder() {
        return this.generatedFolder;
    }
}
