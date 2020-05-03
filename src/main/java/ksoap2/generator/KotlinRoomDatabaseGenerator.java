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
import java.util.*;

/**
 * Class to generate Web services client in J2ME based on Ksoap.
 *
 * @author Cong Kinh Nguyen
 */
public final class KotlinRoomDatabaseGenerator extends AbstractGenerator {
    private Class<?> stubClass;

    private String stubClientName;

    public KotlinRoomDatabaseGenerator(HashMap<String, Class<?>> serviceClassesHM, final Class<?> clazz, final Class<?> stubClass, Writer writer, final String generatedFolder) {
        super(serviceClassesHM, clazz, writer, generatedFolder);
        this.stubClass = stubClass;
        className = stubClass.getSimpleName().replace("Stub", "") + "Database";
        isKotlin = true;
        stubClientName = firstLetterLowerCase(stubClass.getSimpleName().replace("Stub", ""));
        nameSpace = clazz.getPackage().getName() + ".room";
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
        writer.append("    companion object {\n");
        writer.append("        // For Singleton instantiation\n");
        writer.append("        @Volatile private var instance: " + className + "? = null\n\n");
        writer.append("        fun getInstance(context: Context): " + className + " {\n");
        writer.append("            return instance ?: synchronized(this) {\n");
        writer.append("                instance ?: buildDatabase(context).also { instance = it }\n");
        writer.append("            }\n");
        writer.append("        }\n\n");
        writer.append("        // Create and pre-populate the database. See this article for more details:\n");
        writer.append("        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785\n");
        writer.append("        private fun buildDatabase(context: Context): " + className + " {\n");
        writer.append("            return Room.databaseBuilder(context, " + className + "::class.java, \"" + className + "\")\n");
        writer.append("            .addCallback(object : RoomDatabase.Callback() {\n");
        writer.append("                override fun onCreate(db: SupportSQLiteDatabase) {\n");
        writer.append("                    super.onCreate(db)\n");
        writer.append("                    // seed some data here.\n");
        writer.append("                }\n");
        writer.append("            })\n");
        writer.append("            .build()\n");
        writer.append("        }\n");
        writer.append("    }\n\n");
    }

    /**
     * @see AbstractGenerator#writeClass(Class)
     */
    @Override
    protected void writeClass(final Class<?> clazz) throws GeneratorException {
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
        for (Map.Entry<String, Class<?>> serviceClass : serviceClassesHM.entrySet()) {
            String serviceClassName = stubClass.getCanonicalName().replace("Stub", "");

            // Skip Service Class org.ajn.Ims_wsSoap -> {Class@2723} "interface org.ajn.Ims_wsSoap"
            if (serviceClassName.equals(serviceClass.getKey())) {
                continue;
            }
            // Skip Axis Class
            Class<?> clazz = serviceClass.getValue();
            if (javax.xml.rpc.Service.class.isAssignableFrom(clazz)
                    || org.apache.axis.client.Stub.class.isAssignableFrom(clazz)
                    || org.apache.axis.client.Service.class.isAssignableFrom(clazz)
            ) {
                continue;
            }

            writer.append("    abstract fun " + firstLetterLowerCase(clazz.getSimpleName()) + "Dao(): " + clazz.getSimpleName() + "Dao\n\n");

        }
    }


    @Override
    protected void writeImportedClasses(Class<?> clazz, Writer writer) throws GeneratorException {
        Util.checkNull(clazz, writer);

        writer.append("import kotlinx.coroutines.Dispatchers\n");
        writer.append("import kotlinx.coroutines.withContext\n");
        writer.append("import android.content.Context\n");
        writer.append("import androidx.room.Database\n");
        writer.append("import androidx.room.Room\n");
        writer.append("import androidx.room.RoomDatabase\n");
        writer.append("import androidx.room.TypeConverters\n");
        writer.append("import androidx.sqlite.db.SupportSQLiteDatabase\n");
        writer.append("import androidx.work.OneTimeWorkRequestBuilder\n");
        writer.append("import androidx.work.WorkManager\n");
        writer.append("import " + clazz.getPackage().getName() + ".model.*\n");
        writer.append("import " + clazz.getPackage().getName() + ".room.dao.*\n");
        writer.append("\n");
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
        writer.append("* The Room database for this app\n");
        writer.append("*/\n");

        writer.append("@Database(entities = [");//GardenPlanting::class, Plant::class
        writeDaoClasses(writer);
        writer.append("], version = 1, exportSchema = false)\n");
        writer.append("abstract class " + className + " : RoomDatabase() {\n");
    }

    private void writeDaoClasses(Writer writer) {
        boolean isFirst = true;
        for (Map.Entry<String, Class<?>> serviceClass : serviceClassesHM.entrySet()) {
            String serviceClassName = stubClass.getCanonicalName().replace("Stub", "");

            // Skip Service Class org.ajn.Ims_wsSoap -> {Class@2723} "interface org.ajn.Ims_wsSoap"
            if (serviceClassName.equals(serviceClass.getKey())) {
                continue;
            }
            // Skip Axis Class
            Class<?> clazz = serviceClass.getValue();
            if (javax.xml.rpc.Service.class.isAssignableFrom(clazz)
                    || org.apache.axis.client.Stub.class.isAssignableFrom(clazz)
                    || org.apache.axis.client.Service.class.isAssignableFrom(clazz)
            ) {
                continue;
            }

            if (isFirst) {
                writer.append(clazz.getSimpleName() + "Dao::class");
                isFirst = false;
            } else {
                writer.append(", " + clazz.getSimpleName() + "Dao::class");
            }
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