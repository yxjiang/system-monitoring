package sysmon.common;

import java.util.LinkedList;

/**
 * MetadataBuffer defines the methods that a concrete buffer should support.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 * @param <T>
 */
public abstract class MetadataBuffer<T> {
	
	protected int capacity;
	
	public MetadataBuffer(int capacity) {
		this.capacity = capacity;
	}
	
	/**
	 * Insert the element into buffer.
	 * If buffer is full, remove oldest at the same time.
	 * @param element
	 */
	public abstract void insert(T element);
	
}
