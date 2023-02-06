INSERT INTO url (name, created_at)
VALUES ('https://www.example.com', '2022-02-01 10:10:10');

INSERT INTO url_check (status_code, title, h1, description, created_at, url_id)
VALUES (200, 'Example title', 'Example h1', 'Example description texts', '2022-02-01 10:11:10', 1);
