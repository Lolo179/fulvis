create table items (
    item_id varchar(255) primary key,
    reference varchar(255) not null,
    name varchar(255) not null,
    stock_total integer not null,
    stock_reserved integer not null,
    constraint chk_items_stock_non_negative check (stock_total >= 0 and stock_reserved >= 0),
    constraint chk_items_stock_total_reserved check (stock_total >= stock_reserved)
);

create table orders (
    order_id varchar(255) primary key,
    status varchar(50) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    version bigint not null
);

create table order_lines (
    order_line_id bigserial primary key,
    order_id varchar(255) not null references orders(order_id) on delete cascade,
    item_id varchar(255) not null,
    quantity integer not null,
    constraint chk_order_lines_quantity_positive check (quantity > 0)
);

create table order_history (
    order_history_id bigserial primary key,
    order_id varchar(255) not null references orders(order_id) on delete cascade,
    previous_status varchar(50),
    new_status varchar(50) not null,
    timestamp timestamp with time zone not null
);
