package piotrrr.thesis.common;

import soc.qase.tools.vecmath.Vector3f;

public interface GameObject {

    /**
     * @return a String describing the given object in a detail
     */
    String toDetailedString();

    /**
     * @return the position of the particular object
     */
    Vector3f getObjectPosition();
}
