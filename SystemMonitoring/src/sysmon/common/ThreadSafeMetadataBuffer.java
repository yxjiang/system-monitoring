package sysmon.common;

import java.util.LinkedList;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

public class ThreadSafeMetadataBuffer<T> extends MetadataBuffer<T> {

	private Buffer metaDataBuffer;
	
	public ThreadSafeMetadataBuffer(int capacity) {
		super(capacity);
		metaDataBuffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer());
	}

	@Override
	public void insert(T element) {
		metaDataBuffer.add(element);
	}

}
