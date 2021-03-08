# KAR

Example 

```
Archive:  org.apache.karaf.deployer.kar-4.2.2.jar
Length      Date    Time    Name
---------  ---------- -----   ----
        0  2018-12-16 04:56   META-INF/
      928  2018-12-16 04:56   META-INF/MANIFEST.MF
      606  2018-12-16 04:56   META-INF/DEPENDENCIES
    11358  2018-12-16 04:56   META-INF/LICENSE
      205  2018-12-16 04:56   META-INF/NOTICE
        0  2018-12-16 04:56   META-INF/maven/
        0  2018-12-16 04:56   META-INF/maven/org.apache.karaf.deployer/
        0  2018-12-16 04:56   META-INF/maven/org.apache.karaf.deployer/org.apache.karaf.deployer.kar/
      163  2018-12-16 04:56   META-INF/maven/org.apache.karaf.deployer/org.apache.karaf.deployer.kar/pom.properties
     4534  2018-12-16 04:46   META-INF/maven/org.apache.karaf.deployer/org.apache.karaf.deployer.kar/pom.xml
        0  2018-12-16 04:56   OSGI-INF/
     1324  2018-12-16 04:46   OSGI-INF/bundle.info
        0  2018-12-16 04:56   OSGI-INF/karaf-tracker/
       63  2018-12-16 04:56   OSGI-INF/karaf-tracker/org.apache.karaf.deployer.kar.osgi.Activator
        0  2018-12-16 04:56   org/
        0  2018-12-16 04:56   org/apache/
        0  2018-12-16 04:56   org/apache/karaf/
        0  2018-12-16 04:56   org/apache/karaf/deployer/
        0  2018-12-16 04:56   org/apache/karaf/deployer/kar/
     3903  2018-12-16 04:56   org/apache/karaf/deployer/kar/KarArtifactInstaller.class
        0  2018-12-16 04:56   org/apache/karaf/deployer/kar/osgi/
     1304  2018-12-16 04:56   org/apache/karaf/deployer/kar/osgi/Activator.class
        0  2018-12-16 04:56   org/apache/karaf/util/
        0  2018-12-16 04:56   org/apache/karaf/util/tracker/
    18218  2018-12-16 04:49   org/apache/karaf/util/tracker/BaseActivator.class
     6770  2018-12-16 04:49   org/apache/karaf/util/tracker/SingleServiceTracker.class
---------                     -------
    49376                     26 files
```


# Versioning


# Other Configuration files on Git

Git setup:

https://github.com/git/git/blob/v2.19.0/Documentation/technical/partial-clone.txt

Sparse setup:

The best way to get a perfect subdirectory checkout is a combination of a filter
from `git help rev-list`.

To initialize a sparse repo:

```shell script
git sparse-checkout init
# or
git init
echo "==> Set skip worktree bit to true"
git config --bool core.sparsecheckout true
git config --bool core.sparseCheckoutCone true
# Writes patterns in .git/info/sparse-checkout directly or
git sparse-checkout set "pattern"
```

Disable sparse checkout 
```shell script
git sparse-checkout disable
# or
git config core.sparseCheckout false
git config --bool core.sparseCheckoutCone false
```

# Camel

Very important Camel detail: A class's method specified by `method=` does **NOT** require a 
function's signature (although providing one does not usually break anything if the number of args matches). 
Camel does some behind-the-scenes work to pass the correct parameters.

in, for example, 
```xml
<toD uri="bean:${header.ResourceImplementationClass}?method=${header.ResourceImplementationMethod}"/>
```




# OSGi-compliant Application
The OSGi implementation is 

# Docker

For stacks to talk to each other (particularly nginx, shibd, karaf), 
create network in uibk namespace. 

docker ps become docker service ls

User network with overlay driver to allow inter-service communication. (--diver overlay is equivalent of "scope swarm")
docker network create --driver overlay --attachable uibk
 
docker stack ls

docker executorService $(docker ps -q -f name=nginx)
docker executorService -it $(docker ps -q -f name=mcs) client 

 Be careful, unlike with docker-compose, docker stack deploy will override 
 processes that have open ports. For example, if nginx is listening to ports 80 and 443 on 
 the host machine, and I start a docker container in the swarm with the same ports, then
 the swarm will take precedence over the other ports. 
 
 
Logs of services (as opposed to containers `docker logs CONTAINER`)
docker service ps --no-trunc mcs
 
 \_ mcs1.1  docker.uibk.ac.at:443/zid/dmlt/ecosystem/docker/image/karaf-el7:latest  avportal-t  Shutdown       Failed less than a second ago  "task: non-zero exit (254)"  
Discovered that this cryptic are indicates that there is a problem with the `--mount`
parameter.

Another error after several failed attempts to start a service:
Address is already in use

Just remove the overlay network and restart it. If this is not possible because it is in use by
other things, I do not know what to do.


## MCS Docker Container

The MCS docker container currently runs a Karaf container.

Because MCS requires outbound communication (or as Docker calls it, egress network)
to open SSH sessions with
SMPs, it is important to bind the Docker container network directly
to the host network (as opposed to the ingress network).

A host network does not require publishing of ports, because
there is a 1:1 relationship between docker container ports and
host ports in this case.

Nginx also requires access to the host network if you want to get access to the calling IP address.
If you run nginx in an ingress network with mode=host published ports, the calling IP will always be the gateway address.

For example, replace
```
--publish '8181:8181'
```

with

```
--network "host"
```
Note that you still need to publish ports, but with mode=host and a slightly different syntax.
```
  --publish 'mode=host,target=1099,published=1099' \
  --publish 'mode=host,target=8101,published=8101' \
  --publish 'mode=host,target=8181,published=8181' \
  --publish 'mode=host,target=44444,published=44444' \
  --publish 'mode=host,target=5005,published=5005' \
```

I had to add each container to the host network because of the way shibd, nginx, and karaf work together.
With just nginx and karaf on the host network, nginx would complain about a bad gateway when the 
post from shibd returns. Perhaps there is a way around this.4

# Compile

```shell script
mvn clean install package
jar cvfm ./target/mediacontrolserver-1.0.0.jar
```


# Prepare Karaf

The features.xml` should be sufficient to provide all runtime 
dependencies for the OSGi container implemented by Karaf. 
Karaf is aware of these based on the MANIFEST.MF, which
is generated statically by the maven-bundle-plugin.
The essential runtime dependencies to be offered by Karaf are:

The following may not be embedded:
- Apache Aries Blueprint `blueprint-core`
- Apache Aries Blueprint API `blueprint-api`

The following are essential

feature:install cxf camel-cxf camel-http4 camel-http camel-jetty cxf-rs-description-openapi-v3

- camel-blueprint 
- camel-core
- org.osgi.service.http, so PAX web bundle, particularly pax-http-jetty`.
- cxf
- camel-cxf
- cxf-features-logging
- cxf-jaxrs
- cxf  cxf-rs-description-openapi-v3

For intercepting over HTTP
- camel-jetty
- camel-http4
- camel-http (maybe)

Just look at diag mcs-connect-server for a list of unsatisifed requirements.
Comapre to feature:list, which lists all available from the repositories Karaf is aware of.
Also, see bundle:tree-show mcs-connect-server for a visual dependency tree.
It sometimes contains useful debugging information: "unable to find matching export"
which means that the service registry as no services (java interfaces) that can fulfil
the import request.

```
import org.apache.cxf;version="[3.2,4)": resolved using org.apache.cxf.cxf-core [84]
import org.apache.cxf.cxf-rt-features-logging: WARNING - unable to find matching export
import org.apache.cxf.ext.logging;version="[3.2,4)": resolved using org.apache.cxf.cxf-rt-features-logging [92]
import org.apache.cxf.jaxrs.openapi;version="[3.2,4)": WARNING - unable to find matching export
```

org.apache.karaf.features.cfg contains two settings of interest: featuresRepositories and featuresBoot
The problem is that Karaf will autostart a feature, but not dependencies of that feature.

I had a conflict with cxf-features-logging which appears as cxf-rt-features-logging/3.2.7
feature:info cxf-features-logging being exclusive with mcs-connect-server
comes with cxf-core 3.2.7. Has to do with fasterxml.jackson.jaxrs.json too. Appears unrelated, however.

Useful stuff

In Karaf, restart, remove data and cache dirs: 
`shutdown -r -ca -cc`
List all Exports:
`package:exports` 
List all classes within a bundle:
bundle:classes

```
<!--<feature name="@project.name@-cxf-features-not-started-by-camel" version="@project.ext.cxfVersion@" description="Camel mostly gets CXF up and running. This handles the remainder." install="auto">-->
		<!--<feature version="@project.ext.cxfVersion@" dependency="false">cxf</feature>-->
        <!--
        	<feature version="@project.version@">cxf-core</feature>
        	<feature version="@project.version@">cxf-jaxws</feature>
        	<feature version="@project.version@">cxf-jaxrs</feature>
        	<feature version="@project.version@">cxf-databinding-jaxb</feature>
        	<feature version="@project.version@">cxf-databinding-aegis</feature>
        	<feature version="@project.version@">cxf-bindings-corba</feature>
        	<feature version="@project.version@">cxf-bindings-coloc</feature>
        	<feature version="@project.version@">cxf-http-provider</feature>
        	<feature version="@project.version@">cxf-transports-local</feature>
        	<feature version="@project.version@">cxf-transports-jms</feature>
        	<feature version="@project.version@">cxf-transports-udp</feature>
        	<feature version="@project.version@">cxf-xjc-runtime</feature>
        	<feature version="@project.version@">cxf-ws-security</feature>
        	<feature version="@project.version@">cxf-ws-rm</feature>
        	<feature version="@project.version@">cxf-ws-mex</feature>
        	<feature version="@project.version@">cxf-javascript</feature>
        	<feature version="@project.version@">cxf-frontend-javascript</feature>
        	<feature version="@project.version@">cxf-features-clustering</feature>
        	<feature version="@project.version@">cxf-features-metrics</feature>
        	<feature version="@project.version@">cxf-features-throttling</feature>
        	<feature version="@project.version@">cxf-features-logging</feature>
        -->
		<!-- This is difficult to import because the version must correlate with that of camel-cxf transitive features -->
		<!--<feature version="@project.ext.cxfVersion@" dependency="false">cxf-rs-description-openapi-v3</feature>-->
		<!--
            <feature version="@project.version@">cxf-jaxrs</feature>
            <feature version="@project.version@">cxf-jackson</feature>
            <bundle start-level="40">mvn:org.apache.cxf/cxf-rt-rs-service-description-common-openapi/@project.version@</bundle>
            <bundle start-level="35">mvn:org.apache.cxf/cxf-rt-rs-service-description-swagger-ui/@project.version@</bundle>-->
            <!--<bundle start-level="40">mvn:org.apache.cxf/cxf-rt-rs-service-description-openapi-v3/@project.version@</bundle>-->
            <!--<bundle start-level="35" dependency="true">mvn:com.fasterxml.jackson.datatype/jackson-datatype-jsr310/@cxf.jackson.version@</bundle>
            <bundle start-level="35" dependency="true">mvn:${cxf.servlet-api.group}/${cxf.servlet-api.artifact}/${cxf.servlet-api.version}</bundle>
            <bundle start-level="10" dependency="true">mvn:javax.validation/validation-api/@cxf.validation.api.version@</bundle>
            <bundle start-level="35" dependency="true">mvn:org.apache.commons/commons-lang3/${cxf.commons-lang3.version}</bundle>
            <bundle start-level="30" dependency="true">mvn:io.github.classgraph/classgraph/@cxf.classgraph.version@</bundle>
            <bundle start-level="30" dependency="true">mvn:org.javassist/javassist/@cxf.javassist.version@</bundle>
            <bundle start-level="35" dependency="true">mvn:io.swagger.core.v3/swagger-annotations/${cxf.swagger.v3.version}</bundle>
            <bundle start-level="35" dependency="true">mvn:io.swagger.core.v3/swagger-models/${cxf.swagger.v3.version}</bundle>
            <bundle start-level="35" dependency="true">mvn:io.swagger.core.v3/swagger-core/${cxf.swagger.v3.version}</bundle>
            <bundle start-level="35" dependency="true">mvn:io.swagger.core.v3/swagger-integration/${cxf.swagger.v3.version}</bundle>
            <bundle start-level="35" dependency="true">mvn:io.swagger.core.v3/swagger-jaxrs2/${cxf.swagger.v3.version}</bundle>
        -->

	<!--</feature>-->
	<!--
        <feature name="@project.name@-pax" version="@project.ext.paxVersion@" description="Gets PAX Logger up and running." install="auto">
            <feature version="@project.ext.paxVersion@" dependency="false">pax-http-jetty</feature>
            		<feature version="@project.ext.paxVersion@" dependency="false">pax-logging-api</feature>
        </feature>--><!-- needed for bundle org.apache.cxf.cxf-rt-transports-http -->
```


### Notable problem with dependencies chains

If a Java base class is imported by Camel (like `javax.bind.xml`) is also imported by a class in an application's Java code
(like `javax.xml.bind.DatatypeConverter`),
then its version will, by default, be taken from the .m2 local repository. In my case:

```
# ./repository/jakarta/xml/bind/jakarta.xml.bind-api/2.3.2/jakarta.xml.bind-api-2.3.2.jar
     3296  2018-12-27 15:30   javax/xml/bind/DatatypeConverterImpl$CalendarFormatter.class
    17174  2018-12-27 15:30   javax/xml/bind/DatatypeConverterImpl.class
     1891  2018-12-27 15:30   javax/xml/bind/DatatypeConverterInterface.class
     7176  2018-12-27 15:30   javax/xml/bind/DatatypeConverter.class
```

This caused a conflict, because Bndtools added a requirement to the OSGi MANIFEST.MF using at least version 2.3.
However, Camel was importing javax.xml.bind from of any version including 2.2 to 2.3. This opened up two dependencies.

### Noteworthy Problem with Class has two properties of the same name

This is caused by a provider specified in the rsServer block that has not been properly instantiated e.g. cannot be found.

```
org.apache.camel.processor.loadbalancer.LoadBalancer is an interface, and JAXB can't handle interfaces.
        this problem is related to the following location:
                at org.apache.camel.processor.loadbalancer.LoadBalancer
                at public org.apache.camel.processor.loadbalancer.LoadBalancer org.apache.camel.model.loadbalancer.CustomLoadBalancerDefinition.getLoadBalancer()
                at org.apache.camel.model.loadbalancer.CustomLoadBalancerDefinition
Class has two properties of the same name "optional"
        this problem is related to the following location:
                at public java.lang.Boolean org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition.getOptional()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition
                at public java.util.List org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition.getLocations()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition
                at public org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition org.apache.camel.blueprint.CamelContextFactoryBean.getCamelPropertyPlaceholder()
                at org.apache.camel.blueprint.CamelContextFactoryBean
        this problem is related to the following location:
                at public java.lang.Boolean org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition.optional
                at org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition
                at public java.util.List org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition.getLocations()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition
                at public org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition org.apache.camel.blueprint.CamelContextFactoryBean.getCamelPropertyPlaceholder()
                at org.apache.camel.blueprint.CamelContextFactoryBean
Class has two properties of the same name "path"
        this problem is related to the following location:
                at public java.lang.String org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition.getPath()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition
                at public java.util.List org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition.getLocations()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition
                at public org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition org.apache.camel.blueprint.CamelContextFactoryBean.getCamelPropertyPlaceholder()
                at org.apache.camel.blueprint.CamelContextFactoryBean
        this problem is related to the following location:
                at public java.lang.String org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition.path
                at org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition
                at public java.util.List org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition.getLocations()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition
                at public org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition org.apache.camel.blueprint.CamelContextFactoryBean.getCamelPropertyPlaceholder()
                at org.apache.camel.blueprint.CamelContextFactoryBean
Class has two properties of the same name "resolver"
        this problem is related to the following location:
                at public java.lang.String org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition.getResolver()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition
                at public java.util.List org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition.getLocations()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition
                at public org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition org.apache.camel.blueprint.CamelContextFactoryBean.getCamelPropertyPlaceholder()
                at org.apache.camel.blueprint.CamelContextFactoryBean
        this problem is related to the following location:
                at public java.lang.String org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition.resolver
                at org.apache.camel.core.xml.CamelPropertyPlaceholderLocationDefinition
                at public java.util.List org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition.getLocations()
                at org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition
                at public org.apache.camel.core.xml.CamelPropertyPlaceholderDefinition org.apache.camel.blueprint.CamelContextFactoryBean.getCamelPropertyPlaceholder()
                at org.apache.camel.blueprint.CamelContextFactoryBean

        at javax.xml.bind.ContextFinder.newInstance(ContextFinder.java:195) ~[?:?]
        at javax.xml.bind.ContextFinder.newInstance(ContextFinder.java:129) ~[?:?]
        at javax.xml.bind.ContextFinder.find(ContextFinder.java:318) ~[?:?]
        at javax.xml.bind.JAXBContext.newInstance(JAXBContext.java:478) ~[?:?]
        at javax.xml.bind.JAXBContext.newInstance(JAXBContext.java:435) ~[?:?]
        at org.apache.camel.blueprint.BlueprintModelJAXBContextFactory.newJAXBContext(BlueprintModelJAXBContextFactory.java:64) ~[?:?]
        at org.apache.camel.blueprint.handler.CamelNamespaceHandler.getJaxbContext(CamelNamespaceHandler.java:676) ~[?:?]
        at org.apache.camel.blueprint.handler.CamelNamespaceHandler.parseCamelContextNode(CamelNamespaceHandler.java:243) ~[?:?]
        ... 17 more
Caused by: com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationsException: 65 counts of IllegalAnnotationExceptions
        at com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationsException$Builder.check(IllegalAnnotationsException.java:91) ~[?:?]
        at com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl.getTypeInfoSet(JAXBContextImpl.java:445) ~[?:?]
        at com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl.<init>(JAXBContextImpl.java:277) ~[?:?]
        at com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl.<init>(JAXBContextImpl.java:124) ~[?:?]
        at com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder.build(JAXBContextImpl.java:1123) ~[?:?]
        at com.sun.xml.internal.bind.v2.ContextFactory.createContext(ContextFactory.java:147) ~[?:?]
        at com.sun.xml.internal.bind.v2.ContextFactory.createContext(ContextFactory.java:271) ~[?:?]
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:?]
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[?:?]
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:?]
        at java.lang.reflect.Method.invoke(Method.java:498) ~[?:?]
        at javax.xml.bind.ContextFinder.newInstance(ContextFinder.java:171) ~[?:?]
        at javax.xml.bind.ContextFinder.newInstance(ContextFinder.java:129) ~[?:?]
        at javax.xml.bind.ContextFinder.find(ContextFinder.java:318) ~[?:?]
        at javax.xml.bind.JAXBContext.newInstance(JAXBContext.java:478) ~[?:?]
        at javax.xml.bind.JAXBContext.newInstance(JAXBContext.java:435) ~[?:?]
        at org.apache.camel.blueprint.BlueprintModelJAXBContextFactory.newJAXBContext(BlueprintModelJAXBContextFactory.java:64) ~[?:?]
        at org.apache.camel.blueprint.handler.CamelNamespaceHandler.getJaxbContext(CamelNamespaceHandler.java:676) ~[?:?]
        at org.apache.camel.blueprint.handler.CamelNamespaceHandler.parseCamelContextNode(CamelNamespaceHandler.java:243) ~[?:?]
        ... 17 more
```


Either 

```
Unable to load class com.fasterxml.jackson.jaxrs.json.JacksonXMLProvider from recipe BeanRecipe[name='JacksonXMLProvider']
```

#### Fix

The problem is multifaceted. In my case, it was related to the JAX-RS providers. 

Fixed class import
```
<bean id="JacksonXMLProvider" class="com.fasterxml.jackson.jaxrs.json.JacksonXMLProvider"/>
to
<bean id="JacksonXMLProvider" class="com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider"/>
```

Later the problem recurred (see puzzling thing below).

I added activation="eager" to both providers to ensure that an implementation exists as soon as possible.
I also added a depends-on to the Mapper Providers.

```
<bean id="JacksonJsonProvider" class="com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider" activation="eager"/>
<bean id="JacksonXMLProvider" class="com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider" activation="eager"/>
<bean id="JacksonObjectMapperProvider" class="at.ac.uibk.mcsconnect.core.api.server.JacksonObjectMapperContextResolver" depends-on="JacksonJsonProvider"/>
<bean id="JacksonXmlMapperProvider" class="at.ac.uibk.mcsconnect.core.api.server.JacksonXmlMapperContextResolver" depends-on="JacksonXMLProvider"/>
```

TODO: The cleaner way would be to have multiple depends-on in the rsServer block. I have not figured out how
to do that, despite the fact that Blueprint specifies that it may be a comma-separated list.

Added to Bindtools bnd.bnd

```
com.fasterxml.jackson.jaxrs.xml;version="${project.ext.jacksonVersion}", \
```

The puzzling thing is that the top level manager (bean for JacksonXMLProvider) should have been
instantiated before the rsServer block which specifies the providers. Only if I comment out the
references to the top level managers in the providers block do I get the real error, namely that
Blueprint is unable to instantiate the `com.fasterxml.jackson.jaxrs.json.JacksonXMLProvider` class.


```
Caused by: java.lang.ClassNotFoundException: com.fasterxml.jackson.jaxrs.json.JacksonXMLProvider not found by com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider [53]
        at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1639)
        at org.apache.felix.framework.BundleWiringImpl.access$200(BundleWiringImpl.java:80)
        at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:2053)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        at org.apache.felix.framework.BundleWiringImpl.getClassByDelegation(BundleWiringImpl.java:1414)
        at org.apache.felix.framework.BundleWiringImpl.searchImports(BundleWiringImpl.java:1660)
        at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1590)
        at org.apache.felix.framework.BundleWiringImpl.access$200(BundleWiringImpl.java:80)
        at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:2053)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        at org.apache.felix.framework.Felix.loadBundleClass(Felix.java:1927)
        at org.apache.felix.framework.BundleImpl.loadClass(BundleImpl.java:978)
        at org.apache.aries.blueprint.container.BlueprintContainerImpl.loadClass(BlueprintContainerImpl.java:471)
        at org.apache.aries.blueprint.container.BlueprintRepository.loadClass(BlueprintRepository.java:524)
        at org.apache.aries.blueprint.container.GenericType.parse(GenericType.java:135)
        at org.apache.aries.blueprint.di.AbstractRecipe.doLoadType(AbstractRecipe.java:169)

```

###

Embed jars in bundles with Bndtools

Compile as normal in gradle. Copy the necessary jar packages to the build output directory and tell
OSGi how to find them. Bndtools can do the copying automatically by adding `-includeresource` in the bnd.bnd file. 
I used the lib subdirectory to store the embedded jars. 

Sometime Gradle (when default Java VM is not the same as this build)
needs to know where Java lives: `-Dorg.gradle.java.home=/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/`
 
Add the following to bnd.bnd.
```
Bundle-Classpath: \
    ., \
    lib/jsch-${project.ext.jcraftVersion}.jar

-includeresource: \
    lib/jsch-${project.ext.jcraftVersion}.jar=jsch-${project.ext.jcraftVersion}.jar

Import-Package: \
    !com.jcraft.jsch, \
```

Note that the `.` is necessary to include the main jar (instead of overwriting the default Bundle-Classpath).



The following is unique to the `maven-bundle-plugin` for Apache Felix,
which does not automatically embed the jar files (how does that work? maven hides that.)

```
Embed-Package: \
    com.jcraft, \
```

## CXF Bus Managers Problem

Because I import the CXF bus, I need some features from cxf-core.

Caused by: java.lang.ClassCastException: Cannot cast org.apache.cxf.bus.managers.CXFBusLifeCycleManager to org.apache.cxf.buslifecycle.BusLifeCycleManager

The org.apache.cxf.buslifecycle.BusLifeCycleManager class must be imported. This is exported by something that feature CXF has.

There seems to have been a major change between 3.2.x and 3.3.x

```xml
    <!-- CXF Core Bus conveniently called "jaxrs" so that we can bind the jsonProvider to it for all traffic -->
    <cxfcore:bus id="jaxrs" bus="busJaxrs">
        <!--
            This dublinCore needs to be set 'true' in order to give preference to JacksonJsonProvider
            over the default Jettison provider.
          -->
        <cxfcore:properties>
            <entry key="skip.default.json.provider.registration" value="true"/><!-- only register explicit json providers -->
            <!--<entry key="schema-validation-enabled" value="true" />
            <entry key="mtom-enabled" value="true" />-->
        </cxfcore:properties>
        <cxfcore:features>
            <cxfcore:logging/>
        </cxfcore:features>
    </cxfcore:bus>
```

Other removed stuff from bnd.bnd

```
    org.apache.aries.blueprint, \
        org.apache.camel.component.cxf, \
        org.apache.camel.component.cxf.blueprint, \
        org.apache.camel.impl, \
```


## Aries Blueprint Namespace Registration

Watch out for error: 

16:55:05.853 INFO [FelixStartLevel] Aries Blueprint packages not available. So namespaces will not be registered

or namespace handler is missing http://camel.apache.org/schema/blueprint/cxf http://camel.apache.org/schema/blueprint/camel-cxf.xsd

*IMPORTANT* Check whether the CXF classes are included in the bundle. They *MUST* not be included. If they are,
then the XML namespaces might register with the wrong CXF instance (in this case, not the one registered with Aries Blueprint).
It could also be possible that two features import that same feature twice i.e. camel-cxf loads apache cxf.


The blueprint runtime parses the blueprint descriptors twice. The first pass is fast, and finds only every namespace that is used by the blueprint bundle. If the blueprint bundle uses a non-standard namespace, then the blueprint container attempts to locate NamespaceHandler services in the OSGi service registry for each custom namespace. A NamespaceHandler service advertises every xml namespace that it can process by using OSGi service properties. The blueprint runtime does not parse the blueprint xml until NamespaceHandler services can be found for every custom namespace that is used in the bundle.
Check the Blueprint XMLs for improper classpaths of classes. For example, the JacksonJaxbJsonProvider.java path was wrong.
https://www.ibm.com/support/knowledgecenter/SSD28V_liberty/com.ibm.websphere.wlp.core.doc/ae/rwlp_blueprint_namespace_handler.html

Tried removing schemaLocation http://camel.apache.org/schema/blueprint/cxf https://camel.apache.org/schema/cxf/camel-cxf-blueprint.xsd

I tried specifying an older camel blueprint cxf schema as used in the ESB running 2.22.5 with 
camel-cxf-2.11.0-blueprint: https://camel.apache.org/schema/cxf/camel-cxf-2.11.0-blueprint.xsd

## Logging
It is best to add a logging API (like PAX LOGGING) in the application, but
at explicitly announce (in the MANIFEST.MF) that this OSGi bundle will have the implementation
supplied (provided) by the OSGi container.

```xml
<Import-Package>
org.slf4j.*; provider=paxlogging
</Import-Package>
```

## Karaf

Dependencies are specified and handled by the features.xml file and OSGI MANIFEST.MF.

### Users

Setup users inside of users.properties
Karaf client connects to an existing Karaf instance using SSH in the background. Any
user that uses client must therefore be a member of the SSH role, defined in `org.apache.karaf.shell.cfg`

```
sshRole = ssh

users.properties format: username = password,[role],_g_:groupname
_g_\:groupname = [role]
```

e.g.
```
admin = password,_g_:admingroup
_g_\:admingroup = admin,group,manager,viewer,ssh
```

Enable client mode for Karaf (User, group, role management) in Karaf

The default "realm" in Karaf is `karaf`. To confirm user, group, and roles, 
first set the realm. Then all realm- commands and user- commands will have a 
context and work.
```
jaas:realm-manage --realm karaf
jaas:user-add c102273 <password>
jaas:role-add c102273 admin
jaas:update
```

### systemd service

Karaf comes with a generation script

```shell script
sudo sh karaf-service.sh -k /usr/local/karaf/karaf -u karaf -g karaf -f karaf-service-template.systemd -n karaf.service
```

This will write a `karaf.service.conf` file to karaf's local etc file and generate a unit file that can be linked to systemd.

```shell script
mkdir -p /usr/local/lib/systemd/system
ln -s $(pwd)/karaf.service /usr/local/lib/systemd/system/karaf.service
groupadd karaf
useradd -g karaf karaf
chown -R karaf:karaf /usr/local/karaf
systemctl enable --now karaf
```

## Install Karaf Script on REHL6

See script: `install_karaf.sh`

client does not authenticate properly, but ssh should work if a
key is added to keys.properties

```
> jaas:realm-list
Index │ Realm Name │ Login Module Class Name
──────┼────────────┼───────────────────────────────────────────────────────────────
1     │ karaf      │ org.apache.karaf.jaas.modules.properties.PropertiesLoginModule
2     │ karaf      │ org.apache.karaf.jaas.modules.publickey.PublickeyLoginModule
3     │ karaf      │ org.apache.karaf.jaas.modules.audit.FileAuditLoginModule
4     │ karaf      │ org.apache.karaf.jaas.modules.audit.LogAuditLoginModule
5     │ karaf      │ org.apache.karaf.jaas.modules.audit.EventAdminAuditLoginModule
```

The PropertiesLoginModule class is contained within org.apache.karaf.jaas.modules-4.2.5.jar.

Extract with unzip and decompile with jad.

```shell script
unzip org.apache.karaf.jaas.modules-4.2.5.jar
jad ./org/apache/karaf/jaas/modules/properties/PropertiesLoginModule.class
```



# Shibboleth

Generate certificates

```shell script
shib-keygen -f
```

Uncomment desired attributes in `/etc/shibboleth/attribute-map-xml`




```shell script
./keygen.sh -e https://avportal-t.uibk.ac.at/shibboleth
./metagen.sh -c sp-cert.pem -h avportal-t.uibk.ac.at -e https://avportal-t.uibk.ac.at/shibboleth > avportal-t_metadata.xml
wget https://eduid.at/keys/aconet-metadata-signing.crt -O /etc/shibboleth/aconet-metadata-signing.crt
```

See https://github.com/nginx-shib/nginx-http-shibboleth/issues/31

# Adding External Non-OSGI Bundles 

See Karaf section for newer information. This contains info from older attempts.


Example of Jackson. If I wanted to use a newer version of Jackson (as opposed to the one provided by cxf), I could add 
the latest version in a couple of ways. The first way uses jackson-jaxrs-json-provider, which includes what I really need,
namely the base (or core) + databind shown in the second example. Use wrap:mvn protocol to provide ad-hoc OSGi metadata. 
Use CDATA to handle escaping special characters in this xml that are used by OSGi.

```xml
<bundle><![CDATA[wrap:mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-json-provider/${fasterxml.version}$Bundle-SymbolicName=jackson-jaxrs-json-provider&Bundle-Version=${fasterxml.version}]]></bundle>    <!--  jackson-jaxrs-base=jackson-core+jackson-databind -->
<bundle><![CDATA[wrap:mvn:com.fasterxml.jackson.jaxrs/jackson-jaxrs-base/${fasterxml.version}$Bundle-SymbolicName=jackson-jaxrs-base&Bundle-Version=${fasterxml.version}]]></bundle>
```

Or I could wrap my bundles into my oen feature:

```xml
<feature name="jackson" description="Jackson support for REST API." version="${fasterxml.version}" install="auto" start-level="70">
	<bundle dependency="true" start-level="100"><![CDATA[wrap:mvn:com.fasterxml.jackson.core/jackson-core/${fasterxml.version}]]></bundle>
	<bundle dependency="true" start-level="100"><![CDATA[wrap:mvn:com.fasterxml.jackson.core/jackson-annotations/${fasterxml.version}]]></bundle>
	<bundle dependency="true" start-level="100"><![CDATA[wrap:mvn:com.fasterxml.jackson.core/jackson-databind/${fasterxml.version}]]></bundle>
</feature>
```

# FOr safe keeping

```bnd
//Bundle-Blueprint: OSGI-INF/blueprint/blueprint-camel-intercept.xml,OSG
// I-INF/blueprint/blueprint-camel-routes.xml,OSGI-INF/blueprint/bluepri
// nt-camel-rsServer.xml,OSGI-INF/blueprint/blueprint-locations.xml,OSGI
// -INF/blueprint/blueprint-threads.xml

//Import-Package: \
//            !com.ibm.*' \
//            !com.sun.*' \
//            !org.junit' \
//            !org.dom4j.*' \
//            !org.apache.karaf.*' \
//            !org.apache.camel.test.*' \
//            !org.apache.log.*' \
//            !org.ops4j.pax.exam.*' \
//            !org.ops4j.pax.swissbox.*' \
//            !org.ops4j.pax.tinybundles.*' \
//            !org.codehaus.*' \
//            !org.apache.felix.*' \
//            !org.apache.abdera.*' \
//            !com.jcraft.*' \
//            !org.apache.aries.*' \
//            !org.apache.camel-core' \
//            !org.apache.camel-paxlogging' \
//            !org.osgi.service.blueprint' \
//            !org.apache.camel.*' \
//            org.slf4j.*; provider=paxlogging' \
//            javax.crypto.*' \
//            javax.ws.rs.ext.*' \
//            javax.ws.rs.core.*' \
//            com.fasterxml.jackson.jaxrs.*' \
//            com.fasterxml.jackson.databind.*' \
//            com.fasterxml.jackson.core.*' \
//            com.fasterxml.jackson.annotation.*' \
//            javax.ws.rs;version=0;resolution:=optional' \
//            org.apache.camel.blueprint' \
//            org.apache.camel.component.cxf' \
//            org.apache.cxf.*
```


# Reverse Proxy nginx

```
proxy_pass http://10.0.2.23:8181
proxy_set_header Host $host;
proxy_redirect http:// https://;
proxy_http_version 1.1;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header Upgrade $http_upgrade;
proxy_set_header Connection $connection_upgrade;
```

# SSL Certificate

## Diffie-Hellman Key

```
openssl dhparam -out /etc/ssl/certs/dhparam.pem 4096
```

## Letsencrypt Get Certificate

No process may be running on port 80.

```
certbot certonly -d domain.at
```

# Serializing & Deserializing Objects

Jackson 1.7 added the ability to register serializers and deserializers
over the Module interface. Each module maps to subtypes, in an order preferring
the more specific to less specific.

To make CXF-RS (JAX-RS API) use the ObjectMapper, two things must happen.
 
 1. Any serialized formats that Jackson (`com.fasterxml.jackson.jaxrs` package provides classes to integrate with JAX-RS 
    as providers) should handle must be registered with the JAX-RS Provider API. 
    Jackson only handles JSON and XML, so there are max. 2 possibilities.

    
 2. Once the providers are registered, the providers must register an implementation of the JAX-RS `ContextResolver<T>`.
    The context here is a resource class (or other providers). The resource classes of Jackson are:
     `ObjectMapper` handles JSON. `XmlMapper` extends `ObjectMapper` to handle XML. Ex. `ContextResolver<ObjectMapper>`, `ContextResolver<XmlMapper>`
    These mappers (databinders) are an aggregate of many modules (each module handles an object), 
    which can be applied like `mapper.registerModule(SimpleModule)` for both `ObjectMapper` and `XmlMapper`.
    The code at 
    `https://github.com/FasterXML/jackson-jaxrs-providers/blob/master/xml/src/main/java/com/fasterxml/jackson/jaxrs/xml/JacksonXMLProvider.java#L201`
    demonstrates why a separate provider (`ObjectMapper` + `XmlMapper`) must be registered for each format--JSON and XML i.e.
    the `ContextResolver` can only return either an `ObjectMapper` or an `XmlMapper` object.


This can happen in the `rsServer` block of Blueprint XML (if using XML):

```xml

<bean id="JacksonJsonProvider" class="com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider"/>
<bean id="JacksonXMLProvider" class="com.fasterxml.jackson.jaxrs.json.JacksonXMLProvider"/>
<bean id="JacksonObjectMapperProvider"
      class="at.ac.uibk.mcsconnect.core.api.server.JacksonObjectMapperContextResolver"/>
<bean id="JacksonXmlMapperProvider" class="at.ac.uibk.mcsconnect.core.api.server.JacksonXmlMapperContextResolver"/>
<cxf:providers>
<ref component-id="JacksonJsonProvider"/>
<ref component-id="JacksonObjectMapperProvider"/>
<ref component-id="JacksonXMLProvider"/>
<ref component-id="JacksonXmlMapperProvider"/>
</cxf:providers>
```

Once this happens, then the JAX-RS `MessageBodyReader` and `MessageBodyWriter` handlers know how to handle
serialized data.

## Dependencies 

Providers:

- JSON `"com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:${jacksonVersion}"` Bundle ``
- XML `"com.fasterxml.jackson.jaxrs:jackson-jaxrs-xml-provider:${jacksonVersion}"` Bundle `Jackson-JAXRS-XML`

Note also that:

- ObjectMapper handles JSON
- XmlMapper handles XML

See `https://github.com/FasterXML/jackson-modules-base`.

# XML Errors

If the SAX parser results in the error:
 Content is not allowed in prolog.
 
 Check the byte order marker on the XML. It might be that UTF-8 is specified, but that the byte order marker specifies
 UTF-16 (0xFEFF instead of UTF-8 0xEFBBBF). The file output: XML 1.0 document, UTF-8 Unicode (with BOM) text works nicely.
 
 # META-INF from VLE-CONNECT-REST
 
Manifest-Version: 1.0 
Bnd-LastModified: 1572270301069
Build-Jdk: 1.8.0_222
Built-By: jonathan
Bundle-Blueprint: OSGI-INF/blueprint/dsReference.xml,OSGI-INF/blueprint/
 eExamRoutes.xml,OSGI-INF/blueprint/genericBeans.xml,OSGI-INF/blueprint/
 jms.xml,OSGI-INF/blueprint/olatCourseRoutes.xml,OSGI-INF/blueprint/olat
 EmailRoutes.xml,OSGI-INF/blueprint/olatExamRoutes.xml,OSGI-INF/blueprin
 t/olatRest.xml,OSGI-INF/blueprint/olatUserRoutes.xml,OSGI-INF/blueprint
 /properties.xml,OSGI-INF/blueprint/rsServer.xml,OSGI-INF/blueprint/sisR
 outes.xml,OSGI-INF/blueprint/testRoutes.xml
Bundle-Description: RESTful WebService backend for Vle-Connect
Bundle-ManifestVersion: 2
Bundle-Name: UIBK :: Vle-Connect :: RESTful WebService
Bundle-SymbolicName: vle-connect-rest
Bundle-Version: 4.3.3.SNAPSHOT
Created-By: Apache Maven Bundle Plugin
Embed-Dependency: *;scope=compile
Import-Package: javax.sql,javax.ws.rs;version="[2.1,3)",org.apache.camel
 ;version="[2.22,3)",org.apache.camel.component.jms;version="[2.22,3)",o
 rg.apache.camel.impl;version="[2.22,3)",org.apache.camel.processor.aggr
 egate;version="[2.22,3)",org.apache.activemq;version="[5.15,6)",org.apa
 che.camel.component.http;version="[2.22,3)"
Import-Service: javax.sql.DataSource;multiple:=false;filter="(datasource
 .name=SisProd)",javax.sql.DataSource;multiple:=false;filter="(datasourc
 e.name=SisTest)"
Tool: Bnd-2.1.0.20130426-122213


# Docker Secrets

The unencrypted yaml (yaml) is structured like this:

```yaml
uibk_dockersecrets:
  containers:
    nginx:
      files:
        'key':
          source: '/etc/pki/tls/private/avportal-t.uibk.ac.at20181211.key'
        'crt':
          source: '/etc/pki/tls/certs/avportal-t_uibk_ac_at_3894516/avportal-t_uibk_ac_at.crt'
```


The encrypted yaml (eyaml) is structured like this:
```yaml
uibk_dockersecrets:
  containers:
    mcs:
      props:
        esbuser: >
          ENC[PKCS7,MIIBeQYJKoZIhvcNAQcDoIIBajCCAWYCAQAxggEhMIIBHQIBADAFMAACAQEw
          DQYJKoZIhvcNAQEBBQAEggEAVtAYAPi+0qXe3msM6gMQ9P6Zm7ULwsmpg7u6
          NBNEbCm5MvaBwCY2rBDKFu6r1wXoA7AFqbCuXiJbxr+KaFUPob6DSv6sHP2W
          a37wGFSqZgkA1MnhZLPQx8Bh7Ffee+J5w1FvMMZEkoArkXekynUBylhOy5gt
          HFzUX9TmMwLka+k/LuBGcphNkgnIo+lT2c52jfBhpOMBgmrfl2R/xgIZFmfV
          xnwvhErSLGIJpHmQlw9ABZV0Ud7LcyTjKzt7UgZ2447/FDyW9Nq/qDP5zQkM
          u4a4nrRrbmblkshP2jTPoQbEHlqMZRzPm0dmEiuPU4wPkNn1bj4a/ADlbXpC
          N0O0eDA8BgkqhkiG9w0BBwEwHQYJYIZIAWUDBAEqBBD7fN5UfqfbL0WgWm9b
          55hDgBAibx6n12qjH4LRr/sTqCmo]
        esbpass: >
          ENC[PKCS7,MIIBeQYJKoZIhvcNAQcDoIIBajCCAWYCAQAxggEhMIIBHQIBADAFMAACAQEw
          DQYJKoZIhvcNAQEBBQAEggEABrfRGnTMEmxVUbON70gZFUE74xf53T8UpGl0
          XARazkW3K7S54m6Na7Kn3xDo6RNJesxpbVvEOA1kB2BhmtCHlkaYuG+PSt25
          CjOqD8j1kb6xKxfKPDFCJx/+nq78oicthqQdxOTTf2ltFNvZAOmzzPfbmiYz
          QdAqHpJmWtqaRMOb9hqoTmmODBLOmv0kX2V6IIBOrf2uKHwn6iT++eBYFfHH
          PwSRaG6e/2IG/8AEaOBK8ic6+l4OY104+ELN4yBB+rwAp5XSRV7zT49gBrAW
          ZcjPGrPj4bz2OgpO56w1/TLD0D8YHowNlb/2XEmtESPgjutBZjbs1rlrTzXU
          2R6VljA8BgkqhkiG9w0BBwEwHQYJYIZIAWUDBAEqBBC4vbm4UE9j0h4R+RKw
          XywUgBDs0kUjBTT7x2y5RTyHk3ub]
```

Create encrypted keys like this:

```shell script
eyaml encrypt -l 'esbuser' -p --pkcs7-public-key /etc/puppet/keys/public_key.pkcs7.pem
```
where `-p` means that the source input will be entered on the uibkTerminal as a read.

See `eyaml encrypt --help`.


# Log Stuff

Consider catching Caused by: javax.ws.rs.BadRequestException: HTTP 400 Bad Request
on ESB client.

```xml
<route id="camel-routes.getOpenApi">
    <from uri="direct-vm:camel-routes.getOpenApi"/>
    <log loggingLevel="INFO" message="Fetching OpenAPI specification"/><!-- Simulate endpoint-->
</route>
```


# TODOs
 
Exception propogation related

If an SSH session is not connected, for example. Should this happen?
```json
{
  "statuscode": "NOT_MODIFIED",
  "message": "SshSessionLockable 138.232.11.2:22023: Session.connect: java.net.SocketTimeoutException: Read timed out"
}
```