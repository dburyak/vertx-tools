package com.dburyak.vertx.core.domain;

import io.vertx.core.json.JsonObject;

public interface DomainObject<ID, T extends DomainObject<ID, T>> {
    T setAllFromJson(JsonObject json);

    JsonObject toJson();

    ID getDbId();

    void setDbId(ID newDbId);
}
