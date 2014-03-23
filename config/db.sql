
CREATE TABLE IF NOT EXISTS `feeds` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL,
  `user_name` varchar(255) NOT NULL,
  `screen_name` varchar(255) NOT NULL,
  `rt_count` int(10) unsigned NOT NULL,
  `tweet` text NOT NULL,
  `tweeted` datetime DEFAULT NULL,
  `app_user` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;


