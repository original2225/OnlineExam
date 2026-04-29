-- 答卷提交后共享追问聊天室
create table if not exists `exam_record_chat_message` (
  `id` int primary key auto_increment,
  `record_id` int not null comment '考试记录ID',
  `sender_id` int not null comment '发送人ID',
  `sender_role` varchar(20) not null comment '发送人角色',
  `sender_name` varchar(100) default null comment '发送人姓名',
  `content` text not null comment '消息内容',
  `message_type` varchar(20) not null default 'text' comment '消息类型',
  `is_deleted` tinyint(1) not null default 0 comment '是否删除',
  `created_at` datetime not null default current_timestamp comment '创建时间',
  index `idx_chat_record_id` (`record_id`, `id`),
  index `idx_chat_sender` (`sender_id`, `sender_role`),
  constraint `fk_chat_message_record` foreign key (`record_id`) references `exam_record` (`id`) on delete cascade
) comment='答卷追问室消息';

create table if not exists `exam_record_chat_read` (
  `id` int primary key auto_increment,
  `record_id` int not null comment '考试记录ID',
  `user_id` int not null comment '用户ID',
  `user_role` varchar(20) not null comment '用户角色',
  `last_read_message_id` int default 0 comment '最后已读消息ID',
  `last_read_at` datetime default null comment '最后已读时间',
  unique key `uk_chat_read_user_record` (`record_id`, `user_id`, `user_role`),
  index `idx_chat_read_user` (`user_id`, `user_role`),
  constraint `fk_chat_read_record` foreign key (`record_id`) references `exam_record` (`id`) on delete cascade
) comment='答卷追问室已读状态';
