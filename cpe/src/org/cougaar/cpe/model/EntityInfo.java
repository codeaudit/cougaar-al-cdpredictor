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

    public EntityHistory getHistory()
    {
        return history;
    }

    public void setHistory(EntityHistory history)
    {
        this.history = history;
    }

    protected EngageByFireModel model ;
    protected Entity entity ;
    protected EntityHistory history ;
}
