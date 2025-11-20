package org.github.Alyas100.cluster;

/**
 * Handles leader election in the cluster.
 * Only the leader node schedules jobs, others are workers.
 */
public interface LeaderElection {
    void startElection();
    void stopElection();
    boolean isLeader();
    String getCurrentLeader();
    void volunteerForLeadership();
}