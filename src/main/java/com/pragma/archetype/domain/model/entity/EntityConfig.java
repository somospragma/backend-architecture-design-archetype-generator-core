package com.pragma.archetype.domain.model.entity;

import java.util.List;

/**
 * Configuration for generating a domain entity.
 *
 * @param name        Entity name (e.g., "User", "Product")
 * @param fields      List of entity fields
 * @param hasId       Whether entity has an ID field
 * @param idType      Type of ID field (String, Long, UUID)
 * @param packageName Package where entity will be generated
 */
public record EntityConfig(
    String name,
    List<EntityField> fields,
    boolean hasId,
    String idType,
    String packageName) {

  /**
   * Builder for EntityConfig.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private List<EntityField> fields;
    private boolean hasId = true;
    private String idType = "String";
    private String packageName;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder fields(List<EntityField> fields) {
      this.fields = fields;
      return this;
    }

    public Builder hasId(boolean hasId) {
      this.hasId = hasId;
      return this;
    }

    public Builder idType(String idType) {
      this.idType = idType;
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public EntityConfig build() {
      return new EntityConfig(name, fields, hasId, idType, packageName);
    }
  }
}
