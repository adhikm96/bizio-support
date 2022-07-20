package com.thebizio.biziosupport.repo;

import com.thebizio.biziosupport.entity.Ticket;
import com.thebizio.biziosupport.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepo extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByTicketRefNo(String ticketRefNo);
    Page<Ticket> findByOpenedBy(Pageable pageable,String openedBy);

    Page<Ticket> findByAssignedTo(Pageable pageable,String assignedTo);

    Page<Ticket> findByAssignedToAndStatus(Pageable pageable,String assignedTo,TicketStatus status);

    Page<Ticket> findByOpenedByAndAssignedToAndStatus(Pageable pageable,String openedBy,String assignedTo,TicketStatus status);

    Page<Ticket> findByStatus(Pageable pageable, TicketStatus status);

    Optional<Ticket> findByOpenedByAndTicketRefNo(String openedBy,String ticketRefNo);

    Page<Ticket> findByOpenedByAndStatus(Pageable pageable,String openedBy, TicketStatus status);

    Page<Ticket> findByOpenedByAndAssignedTo(Pageable pageable,String openedBy,String assignedTo);
}
