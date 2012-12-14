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

		db.begin();
		for(ODocument data : db.browseClass("MachineData")) {
			Integer dataTimestamp = data.field("timestamp");
			if(timestamp - dataTimestamp >= capacity) {
				data.delete();
			}
		}
		machineData.save();
		db.commit();
	}
	
	@Override
	public void query(String queryStmt) {
		List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>(queryStmt));
		for(ODocument document : result) {
			System.out.println(document.toJSON());
		}
	}
	
	
}
