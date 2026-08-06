package com.dnnthanh.marketplace.be.platform.mapping;

import java.util.List;

/** Generic request-to-model mapping contract. */
public interface RequestModelMapper<REQUEST, MODEL> extends MapperContract {

    MODEL requestToModel(REQUEST request);

    List<MODEL> requestsToModels(List<REQUEST> requests);
}
