CREATE TABLE IF NOT EXISTS public.tickets
(
    id uuid NOT NULL,
    application integer,
    attachments character varying(255) COLLATE pg_catalog."default",
    browser integer,
    closed_by character varying(255) COLLATE pg_catalog."default",
    description character varying(255) COLLATE pg_catalog."default",
    device_type integer,
    opened_by character varying(255) COLLATE pg_catalog."default",
    os integer,
    status integer,
    ticket_type integer,
    title character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT tickets_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.ticket_messages
(
    id uuid NOT NULL,
    attachments character varying(255) COLLATE pg_catalog."default",
    message character varying(255) COLLATE pg_catalog."default",
    owner character varying(255) COLLATE pg_catalog."default",
    ticket_id uuid,
    CONSTRAINT ticket_messages_pkey PRIMARY KEY (id),
    CONSTRAINT fk8c9vw45c3j5m6arnwp4c3iaby FOREIGN KEY (ticket_id)
        REFERENCES public.tickets (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);