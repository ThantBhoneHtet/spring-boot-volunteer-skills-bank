package group11.volunteer_skills_bank.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import group11.volunteer_skills_bank.model.Opportunity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class OpportunityCompleteService {

    private static final Logger logger = LoggerFactory.getLogger(OpportunityCompleteService.class);
    private static final int MAX_IN_CLAUSE_SIZE = 10; // Firestore limit for IN queries
    private final Firestore firestore;

    public List<Opportunity> getCompletedOpportunitiesForVolunteer(String volunteerId)
            throws ExecutionException, InterruptedException {
        logger.info("Fetching completed opportunities for volunteerId: {}", volunteerId);
        CollectionReference oppMembersRef = firestore.collection("opportunityMembers");
        Query query = oppMembersRef.whereArrayContains("volunteers", volunteerId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<String> oppIds = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            logger.debug("Processing document: {}", doc.getId());
            Map<String, Object> data = doc.getData();
            if (data == null || !data.containsKey("volunteers")) {
                logger.warn("Document {} has no volunteers field", doc.getId());
                continue;
            }
            String oppId = (String) data.get("opportunityId");
            if (oppId != null && !oppId.isEmpty()) {
                logger.debug("Found opportunityId: {} for volunteerId: {}", oppId, volunteerId);
                oppIds.add(oppId);
            } else {
                logger.warn("No opportunityId found in document: {}", doc.getId());
            }
        }

        if (oppIds.isEmpty()) {
            logger.info("No opportunityIds found for volunteerId: {}", volunteerId);
            return new ArrayList<>();
        }

        return fetchCompletedOpportunities(oppIds, volunteerId, "volunteer");
    }

    public List<Opportunity> getCompletedOpportunitiesForTeam(String teamId)
            throws ExecutionException, InterruptedException {
        logger.info("Fetching completed opportunities for teamId: {}", teamId);
        CollectionReference oppMembersRef = firestore.collection("opportunityMembers");
        Query query = oppMembersRef.whereArrayContains("teams", teamId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<String> oppIds = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            logger.debug("Processing document: {}", doc.getId());
            Map<String, Object> data = doc.getData();
            if (data == null || !data.containsKey("teams")) {
                logger.warn("Document {} has no teams field", doc.getId());
                continue;
            }
            String oppId = (String) data.get("opportunityId");
            if (oppId != null && !oppId.isEmpty()) {
                logger.debug("Found opportunityId: {} for teamId: {}", oppId, teamId);
                oppIds.add(oppId);
            } else {
                logger.warn("No opportunityId found in document: {}", doc.getId());
            }
        }

        if (oppIds.isEmpty()) {
            logger.info("No opportunityIds found for teamId: {}", teamId);
            return new ArrayList<>();
        }

        return fetchCompletedOpportunities(oppIds, teamId, "team");
    }
    
    // New method to fetch active opportunities for a team
    public List<Opportunity> getActiveOpportunitiesForTeam(String teamId)
            throws ExecutionException, InterruptedException {
        logger.info("Fetching active opportunities for teamId: {}", teamId);
        CollectionReference oppMembersRef = firestore.collection("opportunityMembers");
        Query query = oppMembersRef.whereArrayContains("teams", teamId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<String> oppIds = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null || !data.containsKey("teams")) {
                continue;
            }
            String oppId = (String) data.get("opportunityId");
            if (oppId != null && !oppId.isEmpty()) {
                oppIds.add(oppId);
            }
        }

        if (oppIds.isEmpty()) {
            logger.info("No opportunityIds found for teamId: {}", teamId);
            return new ArrayList<>();
        }

        return fetchActiveOpportunities(oppIds, teamId);
    }

    public List<Opportunity> getScheduledOpportunitiesForVolunteer(String volunteerId)
            throws ExecutionException, InterruptedException {
        logger.info("Fetching scheduled opportunities for volunteerId: {}", volunteerId);
        CollectionReference oppMembersRef = firestore.collection("opportunityMembers");
        Query query = oppMembersRef.whereArrayContains("volunteers", volunteerId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<String> oppIds = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            logger.debug("Processing document: {}", doc.getId());
            Map<String, Object> data = doc.getData();
            if (data == null || !data.containsKey("volunteers")) {
                logger.warn("Document {} has no volunteers field", doc.getId());
                continue;
            }
            String oppId = (String) data.get("opportunityId");
            if (oppId != null && !oppId.isEmpty()) {
                logger.debug("Found opportunityId: {} for volunteerId: {}", oppId, volunteerId);
                oppIds.add(oppId);
            } else {
                logger.warn("No opportunityId found in document: {}", doc.getId());
            }
        }

        if (oppIds.isEmpty()) {
            logger.info("No opportunityIds found for volunteerId: {}", volunteerId);
            return new ArrayList<>();
        }

        return fetchScheduledOpportunities(oppIds, volunteerId);
    }
    
    public List<Opportunity> getScheduledOpportunitiesForTeam(String teamId)
            throws ExecutionException, InterruptedException {
        logger.info("Fetching scheduled opportunities for teamId: {}", teamId);
        CollectionReference oppMembersRef = firestore.collection("opportunityMembers");
        Query query = oppMembersRef.whereArrayContains("teams", teamId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<String> oppIds = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            logger.debug("Processing document: {}", doc.getId());
            Map<String, Object> data = doc.getData();
            if (data == null || !data.containsKey("teams")) {
                logger.warn("Document {} has no teams field", doc.getId());
                continue;
            }
            String oppId = (String) data.get("opportunityId");
            if (oppId != null && !oppId.isEmpty()) {
                logger.debug("Found opportunityId: {} for teamId: {}", oppId, teamId);
                oppIds.add(oppId);
            } else {
                logger.warn("No opportunityId found in document: {}", doc.getId());
            }
        }

        if (oppIds.isEmpty()) {
            logger.info("No opportunityIds found for teamId: {}", teamId);
            return new ArrayList<>();
        }

        return fetchScheduledOpportunitiesForTeam(oppIds, teamId);
    }

    public List<Opportunity> getAllOpportunities() throws ExecutionException, InterruptedException {
        logger.info("Fetching all opportunities from opportunities collection");
        List<Opportunity> opportunities = new ArrayList<>();
        CollectionReference oppRef = firestore.collection("opportunities");
        ApiFuture<QuerySnapshot> querySnapshot = oppRef.get();
        QuerySnapshot snapshot = querySnapshot.get();

        logger.debug("Query returned {} documents", snapshot.getDocuments().size());
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            logger.debug("Processing document ID: {}", doc.getId());
            Map<String, Object> data = doc.getData();
            String oppId = (String) data.get("opportunityId");
            String status = (String) data.get("status");
            logger.debug("Raw data - opportunityId: {}, status: {}", oppId, status);

            try {
                Opportunity opp = doc.toObject(Opportunity.class);
                if (opp != null) {
                    logger.debug("Deserialized - ID: {}, title: {}, status: {}", 
                        doc.getId(), opp.getTitle(), opp.getStatus());
                    opportunities.add(opp);
                } else {
                    logger.warn("Failed to deserialize opportunity with ID: {}", doc.getId());
                }
            } catch (Exception e) {
                logger.error("Error deserializing opportunity with ID: {}: {}", doc.getId(), e.getMessage(), e);
            }
        }

        if (opportunities.isEmpty()) {
            logger.info("No opportunities found in opportunities collection");
        } else {
            logger.info("Found {} opportunities in opportunities collection", opportunities.size());
        }

        return opportunities;
    }
    
    private List<Opportunity> fetchActiveOpportunities(List<String> oppIds, String teamId)
        throws ExecutionException, InterruptedException {
        List<Opportunity> opportunities = new ArrayList<>();
        CollectionReference oppRef = firestore.collection("opportunities");

        for (int i = 0; i < oppIds.size(); i += MAX_IN_CLAUSE_SIZE) {
            int end = Math.min(i + MAX_IN_CLAUSE_SIZE, oppIds.size());
            List<String> batchIds = oppIds.subList(i, end);
            logger.info("Querying active opportunities batch with IDs: {}", batchIds);

            Query oppQuery = oppRef.whereIn(FieldPath.documentId(), batchIds)
                    .whereEqualTo("status", "active");
            ApiFuture<QuerySnapshot> oppSnapshot = oppQuery.get();

            for (DocumentSnapshot doc : oppSnapshot.get().getDocuments()) {
                try {
                    Opportunity opp = doc.toObject(Opportunity.class);
                    if (opp != null) {
                        logger.debug("Found active opportunity: {} with ID: {}, organizationId: {}",
                                opp.getTitle(), doc.getId(), opp.getOrganizationId());
                        opportunities.add(opp);
                    } else {
                        logger.warn("Failed to deserialize opportunity with ID: {}", doc.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error deserializing opportunity with ID: {}: {}", doc.getId(), e.getMessage(), e);
                }
            }
        }
        
        if (opportunities.isEmpty()) {
            logger.info("No active opportunities found for teamId: {} with oppIds: {}", teamId, oppIds);
        } else {
            logger.info("Found {} active opportunities for teamId: {}", opportunities.size(), teamId);
        }

        return opportunities;
    }

    private List<Opportunity> fetchCompletedOpportunities(List<String> oppIds, String entityId, String entityType)
            throws ExecutionException, InterruptedException {
        List<Opportunity> opportunities = new ArrayList<>();
        CollectionReference oppRef = firestore.collection("opportunities");

        for (int i = 0; i < oppIds.size(); i += MAX_IN_CLAUSE_SIZE) {
            int end = Math.min(i + MAX_IN_CLAUSE_SIZE, oppIds.size());
            List<String> batchIds = oppIds.subList(i, end);
            logger.info("Querying completed opportunities batch with IDs: {}", batchIds);

            Query oppQuery = oppRef.whereIn(FieldPath.documentId(), batchIds)
                                   .whereEqualTo("status", "complete");
            ApiFuture<QuerySnapshot> oppSnapshot = oppQuery.get();

            for (DocumentSnapshot doc : oppSnapshot.get().getDocuments()) {
                try {
                    Opportunity opp = doc.toObject(Opportunity.class);
                    if (opp != null) {
                        logger.debug("Found completed opportunity: {} with ID: {}, organizationId: {}", 
                            opp.getTitle(), doc.getId(), opp.getOrganizationId());
                        opportunities.add(opp);
                    } else {
                        logger.warn("Failed to deserialize opportunity with ID: {}", doc.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error deserializing opportunity with ID: {}: {}", doc.getId(), e.getMessage(), e);
                }
            }
        }

        if (opportunities.isEmpty()) {
            logger.info("No completed opportunities found for {}Id: {} with oppIds: {}", 
                entityType, entityId, oppIds);
        } else {
            logger.info("Found {} completed opportunities for {}Id: {}", 
                opportunities.size(), entityType, entityId);
        }

        return opportunities;
    }

    private List<Opportunity> fetchScheduledOpportunities(List<String> oppIds, String volunteerId)
            throws ExecutionException, InterruptedException {
        List<Opportunity> opportunities = new ArrayList<>();
        CollectionReference oppRef = firestore.collection("opportunities");
        Date now = new Date();

        for (int i = 0; i < oppIds.size(); i += MAX_IN_CLAUSE_SIZE) {
            int end = Math.min(i + MAX_IN_CLAUSE_SIZE, oppIds.size());
            List<String> batchIds = oppIds.subList(i, end);
            logger.info("Querying scheduled opportunities batch with IDs: {}", batchIds);

            Query oppQuery = oppRef.whereIn(FieldPath.documentId(), batchIds);
            ApiFuture<QuerySnapshot> oppSnapshot = oppQuery.get();

            for (DocumentSnapshot doc : oppSnapshot.get().getDocuments()) {
                try {
                    Opportunity opp = doc.toObject(Opportunity.class);
                    
                    // Manually filter the retrieved documents in-memory.
                    String status = opp.getStatus() != null ? opp.getStatus().trim() : null;
                    String approvalStatus = opp.getApprovalStatus() != null ? opp.getApprovalStatus().trim() : null;

                    if (opp != null 
                        && "active".equals(status) 
                        && "approved".equals(approvalStatus) 
                        && opp.getStartDate() != null 
                        && opp.getStartDate().toDate().after(now)) {
                            
                        logger.debug("Found scheduled opportunity: {} with ID: {}, organizationId: {}", 
                            opp.getTitle(), doc.getId(), opp.getOrganizationId());
                        opportunities.add(opp);
                    } else {
                        logger.debug("Opportunity with ID {} did not meet the criteria for scheduled opportunities.", doc.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error deserializing opportunity with ID: {}: {}", doc.getId(), e.getMessage(), e);
                }
            }
        }

        if (opportunities.isEmpty()) {
            logger.info("No scheduled opportunities found for volunteerId: {} with oppIds: {}", volunteerId, oppIds);
        } else {
            logger.info("Found {} scheduled opportunities for volunteerId: {}", opportunities.size(), volunteerId);
        }

        return opportunities;
    }
    
    private List<Opportunity> fetchScheduledOpportunitiesForTeam(List<String> oppIds, String teamId)
            throws ExecutionException, InterruptedException {
        List<Opportunity> opportunities = new ArrayList<>();
        CollectionReference oppRef = firestore.collection("opportunities");
        Date now = new Date();

        for (int i = 0; i < oppIds.size(); i += MAX_IN_CLAUSE_SIZE) {
            int end = Math.min(i + MAX_IN_CLAUSE_SIZE, oppIds.size());
            List<String> batchIds = oppIds.subList(i, end);
            logger.info("Querying scheduled opportunities batch with IDs: {}", batchIds);

            Query oppQuery = oppRef.whereIn(FieldPath.documentId(), batchIds);
            ApiFuture<QuerySnapshot> oppSnapshot = oppQuery.get();

            for (DocumentSnapshot doc : oppSnapshot.get().getDocuments()) {
                try {
                    Opportunity opp = doc.toObject(Opportunity.class);
                    
                    String status = opp.getStatus() != null ? opp.getStatus().trim() : null;
                    String approvalStatus = opp.getApprovalStatus() != null ? opp.getApprovalStatus().trim() : null;

                    if (opp != null 
                        && "active".equals(status) 
                        && "approved".equals(approvalStatus) 
                        && opp.getStartDate() != null 
                        && opp.getStartDate().toDate().after(now)) {
                            
                        logger.debug("Found scheduled opportunity: {} with ID: {}, organizationId: {}", 
                            opp.getTitle(), doc.getId(), opp.getOrganizationId());
                        opportunities.add(opp);
                    } else {
                        logger.debug("Opportunity with ID {} did not meet the criteria for scheduled opportunities.", doc.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error deserializing opportunity with ID: {}: {}", doc.getId(), e.getMessage(), e);
                }
            }
        }

        if (opportunities.isEmpty()) {
            logger.info("No scheduled opportunities found for teamId: {} with oppIds: {}", teamId, oppIds);
        } else {
            logger.info("Found {} scheduled opportunities for teamId: {}", opportunities.size(), teamId);
        }

        return opportunities;
    }
}