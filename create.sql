create table comment (id varchar(255) not null, comment_done_by bytea, created_at int8 not null, modified_at int8 not null, text text, primary key (id))
create table dependency (id varchar(255) not null, created_at int8 not null, dependency_score float8 not null, dependency_type int4, fromid varchar(255), status int4, toid varchar(255), primary key (id))
create table dependency_description (dependency_id varchar(255) not null, description varchar(255))
create table person (username varchar(255) not null, email varchar(255), primary key (username))
create table project (id varchar(255) not null, created_at int8 not null, modified_at int8 not null, name varchar(255), primary key (id))
create table project_specified_requirements (project_id varchar(255) not null, specified_requirements varchar(255))
create table requirement (id varchar(255) not null, created_at int8 not null, modified_at int8 not null, name varchar(255), priority int4 not null, requirement_type varchar(255), status varchar(255), text text, primary key (id))
create table requirement_children (requirement_id varchar(255) not null, children_id varchar(255) not null)
create table requirement_comments (requirement_id varchar(255) not null, comments_id varchar(255) not null)
create table requirement_requirement_parts (requirement_id varchar(255) not null, requirement_parts_id varchar(255) not null)
create table requirement_part (id varchar(255) not null, created_at int8 not null, name varchar(255), text text, primary key (id))
alter table requirement_children add constraint UK_8lt203sxr8ycj6jvy1fg5x625 unique (children_id)
alter table requirement_comments add constraint UK_t1bsced8mrr8lg9nwshkmc1sa unique (comments_id)
alter table dependency_description add constraint FK64cxkribe9eha9sac823jyqeo foreign key (dependency_id) references dependency
alter table project_specified_requirements add constraint FK7j5dldrbtae2f28ucnjkt1cyh foreign key (project_id) references project
alter table requirement_children add constraint FKjfd0oxqwf80rfd5airc4qq5jv foreign key (children_id) references requirement
alter table requirement_children add constraint FKivansqbf8hbb102v1arpy97vy foreign key (requirement_id) references requirement
alter table requirement_comments add constraint FK3jnmi2airvcnl8u748a4bt55d foreign key (comments_id) references comment
alter table requirement_comments add constraint FK8b6d8ia8t51r38xx0gwj9y4jd foreign key (requirement_id) references requirement
alter table requirement_requirement_parts add constraint FK7dg6ljqv5rgajftere8xp73g0 foreign key (requirement_parts_id) references requirement_part
alter table requirement_requirement_parts add constraint FKo1qdla6ag6iby6trlv9466wsf foreign key (requirement_id) references requirement
