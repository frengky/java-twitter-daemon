CREATE TABLE IF NOT EXISTS `feeds` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `screen_name` varchar(255) NOT NULL,
  `mention` varchar(255) NOT NULL,
  `tweet` varchar(255) NOT NULL,
  `timeline` varchar(255) NOT NULL,
  `published` varchar(3) NOT NULL,
  `deleted` tinyint(1) NOT NULL,
  `tweeted` datetime NOT NULL,
  `created` datetime NOT NULL,
  `modified` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
