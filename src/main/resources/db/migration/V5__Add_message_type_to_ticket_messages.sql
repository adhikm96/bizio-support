ALTER TABLE public.ticket_messages ADD COLUMN IF NOT EXISTS message_type integer DEFAULT 0;
