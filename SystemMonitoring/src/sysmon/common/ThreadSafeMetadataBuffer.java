package sysmon.common;

import java.util.List;

import sysmon.util.GlobalParameters;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * We leverage the embedded document database OrientDB inside.
 * @author yexijiang
 *
 * @param <T>
 */
public class ThreadSafeMetadataBuffer extends MetadataBuffer {

	private String dbUrl = "local:/tmp/db/monitoring";
	private ODatabaseDocumentTx db;
	
	
	private int removed = 0;
	
	public ThreadSafeMetadataBuffer(int capacity) {
		super(capacity);
		try {
			db = new ODatabaseDocumentTx(dbUrl).open(GlobalParameters.EMBEDDED_DB_USERNAME, GlobalParameters.EMBEDDED_DB_PASSWORD);
		} catch(OStorageException e) {
			db = new ODatabaseDocumentTx(dbUrl).create();
		}
		
	}

	@Override
	public void insert(String element) {
		ODocument machineData = new ODocument("MachineData");
		machineData.fromJSON(element);
		Integer timestamp = machineData.field("timestamp");
		List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>("select max(timestamp) as max from MachineData"));
		
		//	eliminate duplicate records
		if(!result.isEmpty()) {
			Object max = result.get(0).field("max");
			if(max != null) {
				int maxInt = Integer.parseInt(max.toString()); 
				if(maxInt >= timestamp) {
					++removed;
					return;
				}
			}
		}
		
		machineData.save();
		for(ODocument data : db.browseClass("MachineData")) {
			Integer dataTimestamp = data.field("timestamp");
			if(timestamp - dataTimestamp >= capacity) {
				data.delete();
			}
		}
		
	}
	
	@Override
	public void query(String queryStmt) {
		List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(queryStmt));
//		for(ODocument document : result) {
//			System.out.println(document.toJSON());
//		}
		double avgInterval = 0.0;
		int lastTimestamp = -1;
		int maximumInterval = 0; 
		int minimumInterval = 0;
		for(ODocument doc : result) {
			int timestamp = Integer.parseInt(doc.field("timestamp").toString());
			
			if(lastTimestamp == -1) {
				lastTimestamp = timestamp;
			}
			else {
				int diff = timestamp - lastTimestamp; 
//				System.out.print(diff + "\t");
				maximumInterval = maximumInterval > diff ? maximumInterval : diff;
				minimumInterval = minimumInterval < diff ? minimumInterval : diff;
				lastTimestamp = timestamp;
				avgInterval += diff;
			}
		}
		avgInterval /= (result.size() - 1);
		System.out.println("\nsize:	" + result.size() + "\tavg interval:" + avgInterval + "\tMaximum Interval:" + maximumInterval + "\tMinimum Interval:" + minimumInterval + "\tDuplicate removed:" + removed);
	}
	
	
}
