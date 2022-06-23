package com.thebizio.biziosupport.repo;

import com.thebizio.biziosupport.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketMessageRepo extends JpaRepository<TicketMessage, UUID> {
}
