ALTER TABLE public.tickets ADD COLUMN IF NOT EXISTS ticket_ref_no character varying(64) COLLATE pg_catalog."default" NOT NULL;
