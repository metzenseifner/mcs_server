package mcs.convention.plugin;

import com.github.lburgazzoli.gradle.plugin.karaf.KarafPluginExtension;
import com.github.lburgazzoli.gradle.plugin.karaf.features.KarafFeaturesExtension;
import com.github.lburgazzoli.gradle.plugin.karaf.features.model.FeatureDescriptor;
import groovy.lang.Closure;
import groovy.lang.Reference;
import org.gradle.api.Project;

public final class Utility {


    // The closure is
    //


    public static void createFeature(KarafPluginExtension karafext, KarafFeaturesExtension featuresext, String name) {
        Reference fd = new Reference(FeatureDescriptor.class);
        featuresext.feature(new Closure<FeatureDescriptor>(featuresext) {
            @Override
            public FeatureDescriptor call() {
                fd.setProperty("name", name);
                return (FeatureDescriptor) fd.get();
            };
        });
    }



}
