package com.examsystem.rmi.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for distributed exam operations.
 * See context/CLASS_DIAGRAM_REFERENCE.md - Phase 7 RMI.
 */
public interface ExamRemoteService extends Remote {

    String ping() throws RemoteException;

    LoginResult login(String username, String password) throws RemoteException;

    boolean saveAnswer(RemoteAnswerPayload payload) throws RemoteException;

    boolean submitExam(int assignmentId, int totalMarks) throws RemoteException;

    MonitoringSummary getMonitoringSummary(int teacherId) throws RemoteException;

    /** Pull full central database snapshot for client backup sync. */
    SyncBundle pullSyncBundle() throws RemoteException;

    /** Apply client backup changes to the central database. */
    SyncResult pushSyncBundle(SyncBundle bundle) throws RemoteException;
}
