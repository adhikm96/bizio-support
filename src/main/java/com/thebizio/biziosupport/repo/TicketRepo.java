package com.thebizio.biziosupport.repo;

import com.thebizio.biziosupport.dto.TicketStatusMetricsDto;
import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepo extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByTicketRefNo(String ticketRefNo);

    Page<Ticket> findAllByOrderByCreatedDateDesc(Pageable pageable);
    Page<Ticket> findByOpenedByOrderByCreatedDateDesc(Pageable pageable,String openedBy);

    Page<Ticket> findByAssignedToOrderByCreatedDateDesc(Pageable pageable,String assignedTo);

    Page<Ticket> findByAssignedToAndStatusOrderByCreatedDateDesc(Pageable pageable,String assignedTo,TicketStatus status);

    Page<Ticket> findByOpenedByAndAssignedToAndStatusOrderByCreatedDateDesc(Pageable pageable,String openedBy,String assignedTo,TicketStatus status);

    Page<Ticket> findByStatusOrderByCreatedDateDesc(Pageable pageable, TicketStatus status);

    Optional<Ticket> findByOpenedByAndTicketRefNo(String openedBy,String ticketRefNo);

    Page<Ticket> findByOpenedByAndStatusOrderByCreatedDateDesc(Pageable pageable,String openedBy, TicketStatus status);

    Page<Ticket> findByOpenedByAndAssignedToOrderByCreatedDateDesc(Pageable pageable,String openedBy,String assignedTo);


    @Query("SELECT new com.thebizio.biziosupport.dto.TicketStatusMetricsDto(t.status,COUNT(t.status)) FROM Ticket t WHERE t.openedBy = :openedBy GROUP BY t.status")
    List<TicketStatusMetricsDto> countTicketByStatusAndOpenedBy(String openedBy);

    @Query("SELECT new com.thebizio.biziosupport.dto.TicketStatusMetricsDto(t.status,COUNT(t.status)) FROM Ticket t GROUP BY t.status")
    List<TicketStatusMetricsDto> countTicketByStatus();

    @Query("SELECT COUNT(*) FROM Ticket t WHERE t.openedBy = :openedBy")
    long countTicketsByOpenedBy(String openedBy);

    @Query("SELECT COUNT(*) FROM Ticket t")
    long countTickets();
}
