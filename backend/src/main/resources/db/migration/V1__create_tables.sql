-- Schema for User and Todo entities. JPA ddl-auto is validate; all DDL lives here.

CREATE TABLE public.users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL
);

CREATE TABLE public.todos (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    completed  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL,
    user_id    BIGINT       NOT NULL REFERENCES public.users (id)
);

CREATE INDEX idx_todos_user_id ON public.todos (user_id);
