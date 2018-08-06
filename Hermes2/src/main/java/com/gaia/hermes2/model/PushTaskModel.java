package com.gaia.hermes2.model;

import com.gaia.hermes2.bean.PushTaskBean;

public interface PushTaskModel {
	PushTaskBean insert(PushTaskBean bean);

	boolean update(PushTaskBean bean);

	boolean updateGcm(String taskId,int gcmSuccess,int gcmFailure);
	
	boolean updateApns(String taskId,int apnsSuccess,int apnsFailure);
	
	boolean updateTaskState(String taskId,boolean isDone);
	
	PushTaskBean findByTaskId(String id);
	
	
}
