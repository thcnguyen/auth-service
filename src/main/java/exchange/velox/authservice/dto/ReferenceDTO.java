package exchange.velox.authservice.dto;

import java.io.Serializable;

/**
 * Present object which is related to emails.
 */
public class ReferenceDTO implements Serializable {

    private String entityName;
    private String entityId;
    private Integer entitySearchableId;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Integer getEntitySearchableId() {
        return entitySearchableId;
    }

    public void setEntitySearchableId(Integer entitySearchableId) {
        this.entitySearchableId = entitySearchableId;
    }

    @Override
    public String toString() {
        return "ReferenceDTO{" +
                    "entityName='" + entityName + '\'' +
                    ", entityId='" + entityId + '\'' +
                    ", entitySearchableId=" + entitySearchableId +
                    '}';
    }
}
