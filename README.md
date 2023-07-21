# zio-http-quill-demo

## SQL scripts

```sql
create table "user"
(
    user_id  serial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null
);
```
