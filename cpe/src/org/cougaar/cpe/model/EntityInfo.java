package org.cougaar.cpe.model;

import org.cougaar.cpe.model.EngageByFireModel;
import org.cougaar.cpe.model.Entity;

import java.io.Serializable;

/**
 * Contains all relevant information about entities managed by or
 * known about by the WorldState.
 */
public class EntityInfo implements Serializable {

    public EntityInfo(Entity entity) {
        this.entity = entity;
    }

    public EntityInfo( Entity entity, EngageByFireModel model) {
        this.entity = entity;
        this.model = model;
    }

    public EngageByFireModel getModel() {
        return model;
    }

    public Entity getEntity() {
        return entity;
    }

    public long getEntityStateTimestamp() {
        return entityStateTimestamp;
    }

    /**
     * When this was last updated.
     * @param entityStateTimestamp
     */
    public void setEntityStateTimestamp(long entityStateTimestamp) {
        this.entityStateTimestamp = entityStateTimestamp;
    }

    protected EngageByFireModel model ;
    protected Entity entity ;
    protected long entityStateTimestamp ;
}
