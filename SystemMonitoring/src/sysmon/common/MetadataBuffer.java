package sysmon.common;

import com.google.gson.JsonParser;

/**
 * MetadataBuffer defines the methods that a concrete buffer should support.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 * @param <T>
 */
public abstract class MetadataBuffer {
	
	protected int capacity;
	protected JsonParser jsonParser;
	
	public MetadataBuffer(int capacity) {
		this.capacity = capacity;
		jsonParser = new JsonParser();
	}
	
	/**
	 * Insert the element into buffer.
	 * If buffer is full, remove oldest at the same time.
	 * @param element
	 */
	public abstract void insert(String element);
	
	/**
	 * Query the data.
	 * @param queryStmt
	 */
	public abstract void query(String queryStmt);
	
}
