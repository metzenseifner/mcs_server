package at.ac.uibk.mcsconnect.common.impl;

import at.ac.uibk.mcsconnect.common.api.Preparable;
import at.ac.uibk.mcsconnect.common.api.PreparableFactory;
import at.ac.uibk.mcsconnect.common.api.PreparableCacheImpl;
import at.ac.uibk.mcsconnect.functional.common.Function;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
        name = "at.ac.uibk.mcsconnect.common.impl.PreparableFactoryCacheImpl",
        immediate = true
)
public class PreparableFactoryCacheImpl<A, V> implements PreparableFactory<A, V> {

    @Activate
    public PreparableFactoryCacheImpl() {}

    @Override
    public Preparable create(Preparable a) {
        return new PreparableCacheImpl<A, V>(a);
    }

    @Override
    public Preparable create(Preparable a, Function validator) {
        return new PreparableCacheImpl<A, V>(a, validator);
    }
}
