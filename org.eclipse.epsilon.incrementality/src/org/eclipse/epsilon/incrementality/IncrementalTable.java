package org.eclipse.epsilon.incrementality;

import java.util.LinkedList;
import java.util.List;

public class IncrementalTable implements IncrementalResource {
	
	List<IncrementalRecord> incrementalRecords = new LinkedList<>();

	@Override
	public void add(IncrementalRecord incrementalRecord) {
		incrementalRecords.add(incrementalRecord);
	}
}
