package com.dnnthanh.marketplace.be.platform.mapping;

import java.util.List;

/** Generic model-to-response mapping contract. */
public interface ModelResponseMapper<MODEL, RESPONSE> extends MapperContract {

    RESPONSE modelToResponse(MODEL model);

    List<RESPONSE> modelsToResponses(List<MODEL> models);
}
