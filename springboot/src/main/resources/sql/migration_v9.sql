-- Tutorial: 添加 document_url 列，支持文档和 Markdown 类型
ALTER TABLE `tutorial` ADD COLUMN `document_url` varchar(500) DEFAULT NULL COMMENT '文档URL' AFTER `video_url`;
