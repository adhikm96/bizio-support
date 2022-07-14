ALTER TABLE public.tickets ADD COLUMN IF NOT EXISTS os_version character varying(255) COLLATE pg_catalog."default";
ALTER TABLE public.tickets ADD COLUMN IF NOT EXISTS application_version character varying(255) COLLATE pg_catalog."default";
ALTER TABLE public.tickets ADD COLUMN IF NOT EXISTS browser_version character varying(255) COLLATE pg_catalog."default";