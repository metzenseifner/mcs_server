/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package mcs.convention.plugin;

import aQute.bnd.gradle.BndBuilderPlugin;
import aQute.bnd.gradle.Bundle;
import com.github.lburgazzoli.gradle.plugin.karaf.KarafPlugin;
import com.github.lburgazzoli.gradle.plugin.karaf.KarafPluginExtension;
import com.github.lburgazzoli.gradle.plugin.karaf.features.KarafFeaturesExtension;
import com.github.lburgazzoli.gradle.plugin.karaf.features.model.FeatureDependencyDescriptor;
import com.github.lburgazzoli.gradle.plugin.karaf.features.model.FeatureDescriptor;
import groovy.lang.Closure;
import mcs.convention.plugin.versionmanager.VersionManager;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.JavaTestFixturesPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//import static mcs.convention.plugin.Utility.createFeature;

/**
 * A simple 'hello world' plugin.
 */
public class McsConventionPlugin implements Plugin<Project> {

    private static final String COMPONENT_PREFIX = "MCS :: ";
    private static final String COMMON_GROUP = "at.ac.uibk.mcsconnect";
    private static final String DEFAULT_KARAF_XSD_VERSION = "1.3.0";
    private static final JavaVersion JAVA_VERSION = JavaVersion.VERSION_1_8;

    // define these in gradle.properties in each project
    private static final String REPO_USER = Defaults.DEFAULT_KOMAR_REPO_USERNAME;
    private static final String REPO_PASSWORD = Defaults.DEFAULT_KOMAR_REPO_PASSWORD;

    public static final VersionManager VERSION_MANAGER = new VersionManager();

    private static final Map<String, String> EXTERNAL_LIBS;
    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("biz.aQute.bnd:biz.aQute.bnd.gradle", "biz.aQute.bnd:biz.aQute.bnd.gradle:4.2.0");
        tempMap.put("org.apache.cxf:cxf-rt-rs-service-description-openapi-v3", "org.apache.cxf:cxf-rt-rs-service-description-openapi-v3:" + VERSION_MANAGER.get("org.apache.cxf")); // 3.2.14
        tempMap.put("org.apache.cxf:cxf-rt-rs-client", "org.apache.cxf:cxf-rt-rs-client:" + VERSION_MANAGER.get("org.apache.cxf"));
        tempMap.put("org.apache.cxf:cxf-rt-frontend-jaxrs", "org.apache.cxf:cxf-rt-frontend-jaxrs:" + VERSION_MANAGER.get("org.apache.cxf"));
        tempMap.put("org.apache.cxf:cxf-rt-frontend-jaxws", "org.apache.cxf:cxf-rt-frontend-jaxws:" + VERSION_MANAGER.get("org.apache.cxf"));
        tempMap.put("org.apache.cxf:cxf-rt-rs-service-description-swagger", "org.apache.cxf:cxf-rt-rs-service-description-swagger:" + VERSION_MANAGER.get("org.apache.cxf"));
        tempMap.put("org.apache.cxf:cxf-rt-rs-service-description-swagger-ui", "org.apache.cxf:cxf-rt-rs-service-description-swagger-ui:" + VERSION_MANAGER.get("org.apache.cxf"));
        tempMap.put("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider", "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:" + VERSION_MANAGER.get("com.fasterxml.jackson.jaxrs"));
        tempMap.put("com.fasterxml.jackson.jaxrs:jackson-jaxrs-xml-provider", "com.fasterxml.jackson.jaxrs:jackson-jaxrs-xml-provider:" + VERSION_MANAGER.get("com.fasterxml.jackson.jaxrs"));
        tempMap.put("com.fasterxml.jackson.datatype:jackson-datatype-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:" + VERSION_MANAGER.get("com.fasterxml.jackson.datatype"));
        tempMap.put("com.fasterxml.jackson.datatype:jackson-datatype-jdk8", "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:" + VERSION_MANAGER.get("com.fasterxml.jackson.datatype"));
        tempMap.put("com.fasterxml.jackson.module:jackson-module-jaxb-annotations", "com.fasterxml.jackson.module:jackson-module-jaxb-annotations:" + VERSION_MANAGER.get("com.fasterxml.jackson.jaxrs"));
        tempMap.put("com.fasterxml.jackson.core:jackson-annotations", "com.fasterxml.jackson.core:jackson-annotations:" + VERSION_MANAGER.get("com.fasterxml.jackson.core"));
        tempMap.put("com.fasterxml.jackson.core:jackson-databind", "com.fasterxml.jackson.core:jackson-databind:" + VERSION_MANAGER.get("com.fasterxml.jackson.core"));
        tempMap.put("com.fasterxml.jackson.dataformat:jackson-dataformat-xml", "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:" + VERSION_MANAGER.get("com.fasterxml.jackson.dataformat"));
        tempMap.put("javax.xml.bind:jaxb-api", "javax.xml.bind:jaxb-api:" + "2.1");
        tempMap.put("org.slf4j:slf4j-api", "org.slf4j:slf4j-api:1.7.30");
        tempMap.put("org.apache.logging.log4j:log4j-slf4j-impl","org.apache.logging.log4j:log4j-slf4j-impl:2.13.2");
        tempMap.put("io.swagger:swagger-jaxrs", "io.swagger:swagger-jaxrs:1.6.2");
        tempMap.put("com.sun.activation:jakarta.activation", "com.sun.activation:jakarta.activation:1.2.2");
        tempMap.put("com.jcraft:jsch", "com.jcraft:jsch:0.1.55");
        tempMap.put("javax.xml.ws:jaxws-api", "javax.xml.ws:jaxws-api:2.2.8"); // for OpenApiFeature
        tempMap.put("org.apache.aries.blueprint:org.apache.aries.blueprint.annotation.api", "org.apache.aries.blueprint:org.apache.aries.blueprint.annotation.api:1.0.1"); // // for bnd annotations in AdminResourceServer to handle OSGI activation/deactivation
        tempMap.put("org.osgi:org.osgi.core", "org.osgi:org.osgi.core:" + VERSION_MANAGER.get("osgi")); // importing this could result in dep probs with Karaf
        //tempMap.put("org.osgi:org.osgi.compendium", "org.osgi:org.osgi.compendium:" + VERSION_MANAGER.get("osgi")); // importing this could result in dep probs with Karaf
        tempMap.put("org.osgi:org.osgi.service.component.annotations", "org.osgi:org.osgi.service.component.annotations:" + VERSION_MANAGER.get("org.osgi.service.component.annotations"));
        tempMap.put("org.osgi:osgi.cmpn", "org.osgi:osgi.cmpn:" + VERSION_MANAGER.get("osgi"));
        tempMap.put("org.osgi:org.osgi.service.component", "org.osgi:org.osgi.service.component:" +  "1.4.0");
        tempMap.put("io.swagger.core.v3:swagger-annotations", "io.swagger.core.v3:swagger-annotations:" + "2.1.6");
        tempMap.put("javax.annotation:javax.annotation-api", "javax.annotation:javax.annotation-api:"+ "1.3.2");
        tempMap.put("javax.ws.rs:javax.ws.rs-api", "javax.ws.rs:javax.ws.rs-api:" + "2.1.1"); // 2.0 fails to find org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl
        tempMap.put("org.yaml:snakeyaml", "org.yaml:snakeyaml:" + "1.27");
        tempMap.put("org.junit.jupiter:junit-jupiter-api", "org.junit.jupiter:junit-jupiter-api:" + VERSION_MANAGER.get("junit"));
        tempMap.put("org.junit.jupiter:junit-jupiter-params", "org.junit.jupiter:junit-jupiter-params:" + VERSION_MANAGER.get("junit"));
        tempMap.put("org.junit.jupiter:junit-jupiter-engine", "org.junit.jupiter:junit-jupiter-engine:" + VERSION_MANAGER.get("junit"));
        tempMap.put("org.assertj:assertj-core", "org.assertj:assertj-core:" + "3.16.1");
        tempMap.put("org.mockito:mockito-core", "org.mockito:mockito-core:" + "3.3.3");

        EXTERNAL_LIBS = Collections.unmodifiableMap(tempMap);
    }

    public void apply(Project project) {

        // Apply Global Plugins
        PluginManager pluginManager = project.getPluginManager();
        pluginManager.apply(JavaPlugin.class);
        pluginManager.apply(MavenPublishPlugin.class);
        pluginManager.apply(BndBuilderPlugin.class);
        pluginManager.apply(KarafPlugin.class);
        //pluginManager.apply(JavaTestFixturesPlugin.class); // supports interproject test classes

        // Apply Group Name
        project.setProperty("group", COMMON_GROUP);

        // Set default version on subprojects
        if (project.getRootProject() != null) {
            project.setVersion(project.getRootProject().getVersion());
        }

        // Add configuration to allow interproject test imports
        /**
         * Imitate this
         * configurations {
         *     fakeArtifacts.extendsFrom testCompile
         * }
         * task testJar(type: Jar) {
         *     classifier "test"
         *     from sourceSets.test.output
         * }
         * artifacts {
         *     fakeArtifacts testJar
         * }
         */
        project.getConfigurations().create("testArtifacts").extendsFrom(project.getConfigurations().getByName("testCompile"));
        project.getTasks().register("testJar", Jar.class, c -> {
            c.getArchiveClassifier().set("test");
            c.from(project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("test").getOutput());
        });
        project.getArtifacts().add("testArtifacts", project.getTasks().getByName("testJar"));

        // Add source code dependency repositories
        RepositoryHandler repositoryHandler = project.getRepositories();
        repositoryHandler.mavenLocal();
        repositoryHandler.jcenter();
        repositoryHandler.mavenCentral();

        DependencyHandler dependencyHandler = project.getDependencies();

        // Add properties using project extension properties
        //project.set("EXTERNAL_LIBS", EXTERNAL_LIBS); // sets existing, fails if property is missing
        ExtraPropertiesExtension extraPropertiesExtension = project.getExtensions().getByType(ExtraPropertiesExtension.class);
        extraPropertiesExtension.set("EXTERNAL_LIBS", EXTERNAL_LIBS);
        extraPropertiesExtension.set("versions", VERSION_MANAGER);


        // used to specify feature loading order
        //FeatureDependencyDescriptor dep = new FeatureDependencyDescriptor("karaf") {};

        // We first need to create an extension for the Features which means calling getFeatures() once
        //KarafPluginExtension karafPluginExtension = project.getExtensions().getByType(KarafPluginExtension.class);
        //KarafFeaturesExtension karafFeaturesExtension = karafPluginExtension.getFeatures(); // calls new KarafFeaturesExtension(project).


        // Configure global Karaf environment
        // KarafFeaturesExtension does not exist at this time
        //KarafFeaturesExtension karafFeaturesExtension = project.getExtensions().getByType(KarafFeaturesExtension.class);
        //karafFeaturesExtension.repository("mvn:org.apache.aries.jax.rs/org.apache.aries.jax.rs.features/1.0.6/xml");
        //karafFeaturesExtension.repository("mvn:org.apache.cxf.dosgi/cxf-dosgi/2.3.0/xml/features");
        //karafFeaturesExtension.repository("mvn:org.apache.aries.rsa/rsa-features/RELEASE/xml/features");
        //karafFeaturesExtension.setXsdVersion(DEFAULT_KARAF_XSD_VERSION);

        //FeatureDescriptor karafRequirements = new FeatureDescriptor(project) {};
        //karafRequirements.setName("karaf-requirements");
        //karafRequirements.setDescription("A collection of common required OSGI environment features.");
        //karafRequirements.setDetails("This enforces a standardized runtime environment for all bundles.");


        // we need to populate the private featureDescriptors. The only way
        // with the version as of writing is using the groovy method
        //
        // def feature(Closure closure)
        //karafFeaturesExtension.getFeatureDescriptors(); // returns List<FeatureDescriptor>

        //createFeature(karafPluginExtension, karafFeaturesExtension, "deploy");
        //FeatureDescriptor configFeature = createFeature(project, "config");
        //FeatureDescriptor systemFeature = createFeature(project, "system");
        //FeatureDescriptor scrFeature = createFeature(project, "scr");
        //FeatureDescriptor cxfDosgiCommonFeature = createFeature(project, "cxf-dosgi-common");
        //FeatureDescriptor cxfDosgiProviderRsFeature = createFeature(project, "cxf-dosgi-provider-rs");

        //karafPluginExtension.features(new Closure<List<FeatureDescriptor>>(karafPluginExtension) { // hacked owner with null
        //    @Override
        //    public List<FeatureDescriptor> call() {
        //        List<FeatureDescriptor> tmp = new LinkedList<>();
        //        tmp.add(karafRequirements);
        //        tmp.add(deployFeature);
        //        tmp.add(configFeature);
        //        tmp.add(systemFeature);
        //        tmp.add(scrFeature);
        //        tmp.add(cxfDosgiCommonFeature);
        //        tmp.add(cxfDosgiProviderRsFeature);
        //        return tmp;
        //    };
        //});

        // Add bundle task
        project.getTasks().register("bundle",  Bundle.class, b -> {
            //from sourceSets.main.output
            b.from(project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main").getOutput());
        });

        JavaPluginConvention jpc = project.getConvention().getPlugin(JavaPluginConvention.class);
        jpc.setSourceCompatibility(JAVA_VERSION);
        jpc.setTargetCompatibility(JAVA_VERSION);

        project.getTasks().withType(JavaCompile.class).forEach(j -> {
            j.getOptions().setEncoding(StandardCharsets.UTF_8.toString());
        });

        String repoUser = (String) project.getProperties().get(REPO_USER);
        String repoPassword = (String) project.getProperties().get(REPO_PASSWORD);

        // update publishing configuration
        PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

        // add default repos
        publishingExtension.getRepositories().maven(repo -> {
            repo.setName("nexus");
            repo.setUrl(Defaults.KOMAR_REPO);
            repo.credentials(credentials ->  {
                credentials.setUsername(Defaults.DEFAULT_KOMAR_REPO_USERNAME);
                credentials.setPassword(Defaults.DEFAULT_KOMAR_REPO_PASSWORD);
            });
        });

        // add maven publication with source and javadoc jar
        publishingExtension.getPublications().create("maven", MavenPublication.class, mp -> {
            mp.from(project.getComponents().getByName("java"));
        });
    }
}
