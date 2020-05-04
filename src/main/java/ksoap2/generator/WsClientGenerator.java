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

import javax.wsdl.Operation;
import java.util.HashMap;
import java.util.List;

/**
 * Class to generate stub ws client in J2ME.
 *
 * @author Cong Kinh Nguyen
 *
 */
public final class WsClientGenerator {
    /**
     * List of WSDL operation
     */
    private final List<Operation> operationList;
    /**
     * The class.
     */
    private Class <?> clazz;

    private Class <?> stubClass;

    HashMap<String, Class<?>> serviceClassesHM;
    /**
     *
     */
    private boolean isService;

    /**
     * The generated folder.
     */
    private String generatedFolder;

    private Writer writer = new Writer();

    public WsClientGenerator(HashMap<String, Class<?>> serviceClassesHM, final Class<?> clazz, final Class<?> stubClass, boolean isService, final String generatedFolder, List<Operation> operationList) {
        this.clazz = clazz;
        this.stubClass = stubClass;
        this.isService = isService;
        this.generatedFolder = generatedFolder;
	    this.serviceClassesHM = serviceClassesHM;
	    this.operationList = operationList;
    }

    /**
     * Loads context class loader, and then generates code.
     *
     * @throws GeneratorException
     *              The generation exception.
     */
    protected void run() throws GeneratorException {

	    if(isEnum()){

	    }

        if (isService) {
            new JavaSoapServiceClientGenerator(clazz, stubClass, writer, generatedFolder, operationList).run();
            new KotlinSoapCoroutineWrapperGenerator(clazz, stubClass, writer, generatedFolder).run();
        } else {
            new KotlinRoomClassGenerator(serviceClassesHM, clazz, writer, generatedFolder).run();
            new JavaSoapClassGenerator(serviceClassesHM, clazz, writer, generatedFolder).run();
            new KotlinRoomDaoGenerator(serviceClassesHM, clazz, writer, generatedFolder).run();
            new KotlinRoomDatabaseGenerator(serviceClassesHM, clazz, stubClass, writer, generatedFolder).run();
        }
    }

	private boolean isEnum() {

		clazz.isEnum();


		return false;
	}

}