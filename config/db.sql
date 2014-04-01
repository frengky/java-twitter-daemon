CREATE TABLE IF NOT EXISTS `feeds` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `screen_name` varchar(255) NOT NULL,
  `rt_count` int(10) unsigned NOT NULL,
  `mention` varchar(255) NOT NULL,
  `tweet` varchar(255) NOT NULL,
  `tweeted` datetime NOT NULL,
  `app_user` varchar(255) NOT NULL,
  `published` varchar(3) NOT NULL,
  `deleted` tinyint(1) NOT NULL,
  `created` datetime NOT NULL,
  `modified` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;