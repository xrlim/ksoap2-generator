Ksoap2 stub generator with Gradle

1. Requirements:
- JDK 6
- The JAVA_HOME variable is set.

2. What are supported?
- The program generates code in J2ME/Android from the WSDL document.

- This version can generate web services client for web services such as:
+ Methods of web service has parameters with primitive types, but only the array
  of one dimension is supported.
+ Methods of web service has parameters with object types (these types also
  require in J2ME) such as String, Integer, Hashtable, etc. But only the array
  of one dimension of these types is supported.
+ Methods of web service has parameters with object types defined by user, these
  types are composed of the attributes that they are primitive types, object
  types (natively supported in J2ME such as Integer, Float, etc.), except the
  array of them.

3. How to generate web services client running on J2ME and Android using ksoap2?
  a. Configure the task wsdl2ksoap in build.gradle.
  b. Just run the Gradle with gradlew or intelliJ
  c. Grab the code in build/generated

4. How to use generated code?
- Before invoking web services from J2ME or Android, you just need to configure
  the Configuration class (in the generated code) with the URL address of web
  services. Note that if you are running on the emulator (or real devices),
  the IP address of itself is always 127.0.0.1 (localhost). There are some
  examples for configuring the class as the following:

  // incorrect because of using localhost
  Configuration.setConfiguration("http://localhost:8080/Ws2Ksoap/services/HelloWorld");

  // incorrect because of using 127.0.0.1
  Configuration.setConfiguration("http://127.0.0.1:8080/Ws2Ksoap/services/HelloWorld");

  // correct because the emulator can find the web services.
  Configuration.setConfiguration("http://192.168.2.3:8080/Ws2Ksoap/services/HelloWorld");
