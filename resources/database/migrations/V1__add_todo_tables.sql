CREATE TABLE todo (
    todo_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    title text NOT NULL
);

CREATE TABLE todo_item (
    todo_item_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    todo_id uuid REFERENCES todo (todo_id),
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    title text NOT NULL
);