package de.neemann.digital.draw.graphics;

import java.awt.geom.Path2D;
import java.util.Iterator;

public interface GenericPath extends Iterable<VectorInterface> {

    /**
     * Transforms this GenericPath
     *
     * @param transform the transformation
     * @return the transformed GenericPath
     */
	public GenericPath transform(Transform transform);
	
	/**
	 * @enumerate points 
	 * used for calculate bounding box
	 */
	public Iterator<VectorInterface> iterator();
	
    /**
     * @return true if polygon is closed
     * used for select draw vs fill
     */
	public boolean isClosed();

	/**
	 * @return String svg graphics  
	 */
	public String toString();

	/**
	 * @return Path2D for swing graphics 
	 */
	public Path2D toPath2D(); 

}
