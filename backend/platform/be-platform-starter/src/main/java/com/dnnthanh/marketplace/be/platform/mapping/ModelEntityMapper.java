package com.dnnthanh.marketplace.be.platform.mapping;

import java.util.List;

/** Generic model/entity mapping contract for persistence boundaries. */
public interface ModelEntityMapper<MODEL, ENTITY> extends MapperContract {

    ENTITY modelToEntity(MODEL model);

    MODEL entityToModel(ENTITY entity);

    List<ENTITY> modelsToEntities(List<MODEL> models);

    List<MODEL> entitiesToModels(List<ENTITY> entities);
}
