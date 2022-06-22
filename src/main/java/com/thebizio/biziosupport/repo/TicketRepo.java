package com.thebizio.biziosupport.repo;

import com.thebizio.biziosupport.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface    TicketRepo extends JpaRepository<Ticket, UUID> {
}
