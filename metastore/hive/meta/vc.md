# 数据库: vc

> 本文档包含数据库 `vc` 中**最近 30 天内有更新**的 385 张表的元数据信息，用于 AI 训练学习。
> （库内共 512 张表，127 张表因超过 30 天未更新已被过滤）

## 表目录

1. [ads_pv_character_gmv_dd](#ads_pv_character_gmv_dd) - 无描述
2. [ads_pv_character_talk_cnt_dd](#ads_pv_character_talk_cnt_dd) - 无描述
3. [ads_pv_uc_action_info_dd](#ads_pv_uc_action_info_dd) - 无描述
4. [ads_pv_user_behavior_dd](#ads_pv_user_behavior_dd) - 无描述
5. [ads_rec_dispatch_human_characters_index_general](#ads_rec_dispatch_human_characters_index_general) - 无描述
6. [ads_rec_theater_feature](#ads_rec_theater_feature) - 无描述
7. [ads_rec_theater_pool_l1](#ads_rec_theater_pool_l1) - 无描述
8. [ads_uc_ctype_talk_cnt_a_d](#ads_uc_ctype_talk_cnt_a_d) - 无描述
9. [ads_underage_user_easy_fetch_verify_dd](#ads_underage_user_easy_fetch_verify_dd) - 未成年命中及认证结果
10. [ads_vc_ab_platform_experiment_metric_dd](#ads_vc_ab_platform_experiment_metric_dd) - 破次元恋人AB实验指标展示表
11. [ads_vc_ab_platform_experiment_metric_di](#ads_vc_ab_platform_experiment_metric_di) - 无描述
12. [ads_vc_character_choose_a_d](#ads_vc_character_choose_a_d) - 无描述
13. [ads_vc_character_package_rank_ct_a_d](#ads_vc_character_package_rank_ct_a_d) - 无描述
14. [ads_vc_character_period_top100_n_d](#ads_vc_character_period_top100_n_d) - 无描述
15. [ads_vc_growth_ad_period_cvr_tmp](#ads_vc_growth_ad_period_cvr_tmp) - 无描述
16. [ads_vc_hive2kafka_202501_i_h](#ads_vc_hive2kafka_202501_i_h) - 两小时未回复
17. [ads_vc_hive2kafka_202501_n_d](#ads_vc_hive2kafka_202501_n_d) - 春节版本---人群包
18. [ads_vc_hive2redis_202501_n_d](#ads_vc_hive2redis_202501_n_d) - 春节版本---人群包
19. [ads_vc_hive2redis_202503_n_d](#ads_vc_hive2redis_202503_n_d) - 自动化短信---人群包，数据有效期：36h
20. [ads_vc_hot_characters_a_d](#ads_vc_hot_characters_a_d) - 无描述
21. [ads_vc_lose_user_talk_period_cnt_n_d](#ads_vc_lose_user_talk_period_cnt_n_d) - 流失用户
22. [ads_vc_om_22605_i_d](#ads_vc_om_22605_i_d) - 无描述
23. [ads_vc_om_25313_a_d](#ads_vc_om_25313_a_d) - 无描述
24. [ads_vc_om_3116_i_d](#ads_vc_om_3116_i_d) - 无描述
25. [ads_vc_om_3704_d](#ads_vc_om_3704_d) - 无描述
26. [ads_vc_om_3827_2h_v2_i_h](#ads_vc_om_3827_2h_v2_i_h) - 无描述
27. [ads_vc_om_3827_a_d](#ads_vc_om_3827_a_d) - 用户活跃明细表
28. [ads_vc_tags_data_bitmap_dd](#ads_vc_tags_data_bitmap_dd) - 无描述
29. [ads_vc_tags_data_dd](#ads_vc_tags_data_dd) - 标签数据存储表
30. [ads_vc_uc_talk_period_cnt_n_d](#ads_vc_uc_talk_period_cnt_n_d) - 用户角色聊天统计汇总表
31. [ads_vc_uc_talk_top20_n_d](#ads_vc_uc_talk_top20_n_d) - 无描述
32. [ads_vc_uct_persons_n_d](#ads_vc_uct_persons_n_d) - 用户角色聊天轮次人群包圈定表
33. [ads_vc_user_consume_period_n_d](#ads_vc_user_consume_period_n_d) - 用户活跃明细表
34. [ads_vc_user_expire_assets_push_di](#ads_vc_user_expire_assets_push_di) - 无描述
35. [ads_vc_user_period_data_summary_dd](#ads_vc_user_period_data_summary_dd) - 无描述
36. [ads_vc_user_talk_period_cnt_n_d](#ads_vc_user_talk_period_cnt_n_d) - 新老注册用户聊天统计表
37. [ads_vc_yesterdayregister_todaynotactive_a_d](#ads_vc_yesterdayregister_todaynotactive_a_d) - 无描述
38. [bridge_exp_metric](#bridge_exp_metric) - 无描述
39. [bridge_exp_metric_dd](#bridge_exp_metric_dd) - 无描述
40. [dim_user](#dim_user) - 无描述
41. [dim_user_dd](#dim_user_dd) - 无描述
42. [dim_vc_character_info_a_d](#dim_vc_character_info_a_d) - 角色信息明细表
43. [dim_vc_discardted_users_a_d](#dim_vc_discardted_users_a_d) - 无描述
44. [dim_vc_user_info_a_d](#dim_vc_user_info_a_d) - 用户信息明细表
45. [dwd_ab_platform_exp_user_di](#dwd_ab_platform_exp_user_di) - 无描述
46. [dwd_ad_growth_new_di](#dwd_ad_growth_new_di) - 无描述
47. [dwd_ad_growth_return_di](#dwd_ad_growth_return_di) - 无描述
48. [dwd_device_active_di](#dwd_device_active_di) - 无描述
49. [dwd_device_all_dd](#dwd_device_all_dd) - 无描述
50. [dwd_device_new_di](#dwd_device_new_di) - 无描述
51. [dwd_device_return_di](#dwd_device_return_di) - 无描述
52. [dwd_pv_simulator_summary_i_d](#dwd_pv_simulator_summary_i_d) - 无描述
53. [dwd_pv_simulator_talk_cnt_i_d](#dwd_pv_simulator_talk_cnt_i_d) - 无描述
54. [dwd_pv_simulator_talk_info_i_d](#dwd_pv_simulator_talk_info_i_d) - 无描述
55. [dwd_vc_activity_detail_a_d](#dwd_vc_activity_detail_a_d) - 无描述
56. [dwd_vc_app_event_i_d](#dwd_vc_app_event_i_d) - 事件按天划分表
57. [dwd_vc_asset_consume_info_i_d](#dwd_vc_asset_consume_info_i_d) - 无描述
58. [dwd_vc_character_package_i_d](#dwd_vc_character_package_i_d) - 无描述
59. [dwd_vc_character_tags_1h_a_d](#dwd_vc_character_tags_1h_a_d) - 无描述
60. [dwd_vc_character_tags_a_d](#dwd_vc_character_tags_a_d) - 无描述
61. [dwd_vc_character_tags_v2_dd](#dwd_vc_character_tags_v2_dd) - 无描述
62. [dwd_vc_daily_message_users_i_d](#dwd_vc_daily_message_users_i_d) - 无描述
63. [dwd_vc_device_growth_attribution_i_d](#dwd_vc_device_growth_attribution_i_d) - 无描述
64. [dwd_vc_device_user_correlation_a_d](#dwd_vc_device_user_correlation_a_d) - user_id,device_udid,user_code关联表
65. [dwd_vc_free_character_i_d](#dwd_vc_free_character_i_d) - 无描述
66. [dwd_vc_lofter_real_users_mapping_a_d](#dwd_vc_lofter_real_users_mapping_a_d) - 用户活跃明细表
67. [dwd_vc_order_i_d](#dwd_vc_order_i_d) - 订单表
68. [dwd_vc_pmessage_info_7d_a_d](#dwd_vc_pmessage_info_7d_a_d) - 无描述
69. [dwd_vc_special_event_i_d](#dwd_vc_special_event_i_d) - 无描述
70. [dwd_vc_uc_talk_info_i_d](#dwd_vc_uc_talk_info_i_d) - 用户角色聊天明细表
71. [dwd_vc_uc_talk_message_energy_info_i_d](#dwd_vc_uc_talk_message_energy_info_i_d) - 聊天能量明细表
72. [dwd_vc_ucr_order_amount_i_d](#dwd_vc_ucr_order_amount_i_d) - 订单归因到角色表
73. [dwd_vc_unregistered_deviceid_userid_mapping_dd](#dwd_vc_unregistered_deviceid_userid_mapping_dd) - 未注册的设备id和生成的userid的对应关系
74. [dwd_vc_user_active_i_d](#dwd_vc_user_active_i_d) - 用户每日活跃明细表
75. [dwd_vc_user_activity_schedule_i_d](#dwd_vc_user_activity_schedule_i_d) - 无描述
76. [dwd_vc_user_asset_income_detail_a_d](#dwd_vc_user_asset_income_detail_a_d) - 无描述
77. [dwd_vc_user_consecutive_active_a_d](#dwd_vc_user_consecutive_active_a_d) - 无描述
78. [dwd_vc_user_income_record_i_d](#dwd_vc_user_income_record_i_d) - 无描述
79. [dwd_vc_user_simulator_asset_consume_di](#dwd_vc_user_simulator_asset_consume_di) - 无描述
80. [dwd_vc_user_simulator_chats_info_di](#dwd_vc_user_simulator_chats_info_di) - 无描述
81. [dws_ab_platform_experiment_metric_di](#dws_ab_platform_experiment_metric_di) - 无描述
82. [dws_ab_platform_user_metric_di](#dws_ab_platform_user_metric_di) - 无描述
83. [dws_ab_platform_vc_metric_di](#dws_ab_platform_vc_metric_di) - ab实验vc相关原子指标表
84. [dws_ab_platform_vc_metric_expand_di](#dws_ab_platform_vc_metric_expand_di) - ab实验平台vc的指标汇总展开表
85. [dws_par_device_session_di](#dws_par_device_session_di) - 无描述
86. [dws_rec_character_tags_dd](#dws_rec_character_tags_dd) - 无描述
87. [dws_rec_pool_l0](#dws_rec_pool_l0) - 无描述
88. [dws_rec_pool_ugc_l0](#dws_rec_pool_ugc_l0) - 无描述
89. [dws_rec_role_features](#dws_rec_role_features) - 无描述
90. [dws_rec_user_features_syn](#dws_rec_user_features_syn) - 无描述
91. [dws_user_profile_prefer_tags_art_style_dd](#dws_user_profile_prefer_tags_art_style_dd) - 无描述
92. [dws_user_profile_prefer_tags_category_dd](#dws_user_profile_prefer_tags_category_dd) - 无描述
93. [dws_user_profile_prefer_tags_identity_dd](#dws_user_profile_prefer_tags_identity_dd) - 无描述
94. [dws_user_profile_prefer_tags_ip_dd](#dws_user_profile_prefer_tags_ip_dd) - 无描述
95. [dws_user_profile_prefer_tags_personality_dd](#dws_user_profile_prefer_tags_personality_dd) - 无描述
96. [dws_user_profile_prefer_tags_relationship_dd](#dws_user_profile_prefer_tags_relationship_dd) - 无描述
97. [dws_user_profile_prefer_tags_settings_dd](#dws_user_profile_prefer_tags_settings_dd) - 无描述
98. [dws_user_profile_prefer_tags_xp_dd](#dws_user_profile_prefer_tags_xp_dd) - 无描述
99. [dws_user_profile_prefer_worldviews_dd](#dws_user_profile_prefer_worldviews_dd) - 无描述
100. [dws_user_profile_show_3d_dd](#dws_user_profile_show_3d_dd) - 无描述
101. [dws_user_profile_user_talk_cnt_di](#dws_user_profile_user_talk_cnt_di) - 无描述
102. [dws_vc_character_behavior_info_a_d](#dws_vc_character_behavior_info_a_d) - 无描述
103. [dws_vc_character_behavior_info_view](#dws_vc_character_behavior_info_view) - 无描述
104. [dws_vc_character_energy_consume_top100_dd](#dws_vc_character_energy_consume_top100_dd) - 无描述
105. [dws_vc_character_tabulate_data_a_d](#dws_vc_character_tabulate_data_a_d) - 无描述
106. [dws_vc_character_tabulate_data_view](#dws_vc_character_tabulate_data_view) - 无描述
107. [dws_vc_character_tc_info_i_d](#dws_vc_character_tc_info_i_d) - 角色维度汇总表
108. [dws_vc_daily_hot_tags_dd](#dws_vc_daily_hot_tags_dd) - 无描述
109. [dws_vc_daily_hot_words_dd](#dws_vc_daily_hot_words_dd) - 无描述
110. [dws_vc_device_active_a_d](#dws_vc_device_active_a_d) - 设备活跃累计表
111. [dws_vc_device_growth_dau_stratify_i_d](#dws_vc_device_growth_dau_stratify_i_d) - 无描述
112. [dws_vc_feed_v2_part1_rank_a_d](#dws_vc_feed_v2_part1_rank_a_d) - 无描述
113. [dws_vc_feed_v2_part2_rank_a_d](#dws_vc_feed_v2_part2_rank_a_d) - 无描述
114. [dws_vc_feed_v2_part3_rank_a_d](#dws_vc_feed_v2_part3_rank_a_d) - 无描述
115. [dws_vc_feed_v2_part_all_rank_a_d](#dws_vc_feed_v2_part_all_rank_a_d) - 无描述
116. [dws_vc_uc_daily_action_i_d](#dws_vc_uc_daily_action_i_d) - 无描述
117. [dws_vc_uc_talk_day_cnt_single_i_d](#dws_vc_uc_talk_day_cnt_single_i_d) - 用户角色聊天信息，按天汇总表
118. [dws_vc_uc_talk_summary_single_a_d](#dws_vc_uc_talk_summary_single_a_d) - 用户角色聊天信息汇总
119. [dws_vc_user_active_retain_point_i_d](#dws_vc_user_active_retain_point_i_d) - 用户活跃留存表
120. [dws_vc_user_behavior_info_a_d](#dws_vc_user_behavior_info_a_d) - 用户消费汇总表
121. [dws_vc_user_behavior_info_view](#dws_vc_user_behavior_info_view) - 无描述
122. [dws_vc_user_simulator_chats_di](#dws_vc_user_simulator_chats_di) - 无描述
123. [dws_vc_user_simulator_energy_consume_di](#dws_vc_user_simulator_energy_consume_di) - 无描述
124. [dws_vc_user_tc_info_i_d](#dws_vc_user_tc_info_i_d) - 用户维度汇总表
125. [dws_vc_user_ugc_ogc_talk_cnt_30d_di](#dws_vc_user_ugc_ogc_talk_cnt_30d_di) - 用户近30天聊天轮数汇总日报表
126. [dws_vc_user_ugc_type_talk_cnt_di](#dws_vc_user_ugc_type_talk_cnt_di) - 每日不同人群聊天类型轮数统计中间表
127. [ods_binlog_vc_character_di](#ods_binlog_vc_character_di) - 无描述
128. [ods_binlog_vc_event_info_di](#ods_binlog_vc_event_info_di) - 无描述
129. [ods_binlog_vc_push_message_info_di](#ods_binlog_vc_push_message_info_di) - 无描述
130. [ods_binlog_vc_user_account_di](#ods_binlog_vc_user_account_di) - 无描述
131. [ods_binlog_vc_user_asset_consume_flow_di](#ods_binlog_vc_user_asset_consume_flow_di) - 无描述
132. [ods_binlog_vc_user_character_memory_record_di](#ods_binlog_vc_user_character_memory_record_di) - 无描述
133. [ods_binlog_vc_user_character_memory_relation_di](#ods_binlog_vc_user_character_memory_relation_di) - 无描述
134. [ods_binlog_vc_user_deduction_record_di](#ods_binlog_vc_user_deduction_record_di) - 无描述
135. [ods_binlog_vc_user_dungeon_chat_record_di](#ods_binlog_vc_user_dungeon_chat_record_di) - 无描述
136. [ods_binlog_vc_user_event_task_detail_di](#ods_binlog_vc_user_event_task_detail_di) - 无描述
137. [ods_binlog_vc_user_event_task_info_di](#ods_binlog_vc_user_event_task_info_di) - 无描述
138. [ods_binlog_vc_user_message_record_di](#ods_binlog_vc_user_message_record_di) - 无描述
139. [ods_binlog_vc_user_message_record_partition_di](#ods_binlog_vc_user_message_record_partition_di) - 无描述
140. [ods_binlog_vc_user_purchase_record_di](#ods_binlog_vc_user_purchase_record_di) - 无描述
141. [ods_binlog_vc_user_simulator_detail_info_di](#ods_binlog_vc_user_simulator_detail_info_di) - 无描述
142. [ods_binlog_vc_vibe_game_instance_di](#ods_binlog_vc_vibe_game_instance_di) - 无描述
143. [ods_binlog_vc_vibe_game_log_di](#ods_binlog_vc_vibe_game_log_di) - 无描述
144. [ods_db_ab_app_nd](#ods_db_ab_app_nd) - 无描述
145. [ods_db_ab_exp_event_group_nd](#ods_db_ab_exp_event_group_nd) - 无描述
146. [ods_db_ab_exp_group_nd](#ods_db_ab_exp_group_nd) - 无描述
147. [ods_db_ab_exp_metric_nd](#ods_db_ab_exp_metric_nd) - 无描述
148. [ods_db_ab_exp_nd](#ods_db_ab_exp_nd) - 无描述
149. [ods_db_ab_exp_result_nd](#ods_db_ab_exp_result_nd) - 无描述
150. [ods_db_ab_flow_domain_nd](#ods_db_ab_flow_domain_nd) - 无描述
151. [ods_db_ab_flow_layer_nd](#ods_db_ab_flow_layer_nd) - 无描述
152. [ods_db_ab_flow_scene_nd](#ods_db_ab_flow_scene_nd) - 无描述
153. [ods_db_ab_metric_business_line_nd](#ods_db_ab_metric_business_line_nd) - 无描述
154. [ods_db_ab_metric_detail_nd](#ods_db_ab_metric_detail_nd) - 无描述
155. [ods_db_ab_metric_dim_nd](#ods_db_ab_metric_dim_nd) - 无描述
156. [ods_db_ab_metric_scene_nd](#ods_db_ab_metric_scene_nd) - 无描述
157. [ods_db_ab_trace_nd](#ods_db_ab_trace_nd) - 无描述
158. [ods_db_ab_white_list_nd](#ods_db_ab_white_list_nd) - 无描述
159. [ods_db_ad_channel_config_nd](#ods_db_ad_channel_config_nd) - 无描述
160. [ods_db_rt1h_vc_character_package_config_nd](#ods_db_rt1h_vc_character_package_config_nd) - 无描述
161. [ods_db_rt1h_vc_rank_config_nd](#ods_db_rt1h_vc_rank_config_nd) - 无描述
162. [ods_db_rt1h_vc_user_simulator_init_info_nd](#ods_db_rt1h_vc_user_simulator_init_info_nd) - 无描述
163. [ods_db_vc_account_access_token_nd](#ods_db_vc_account_access_token_nd) - 无描述
164. [ods_db_vc_account_bind_record_nd](#ods_db_vc_account_bind_record_nd) - 无描述
165. [ods_db_vc_account_map_relation_nd](#ods_db_vc_account_map_relation_nd) - 无描述
166. [ods_db_vc_activity_module_a_d](#ods_db_vc_activity_module_a_d) - 无描述
167. [ods_db_vc_activity_module_nd](#ods_db_vc_activity_module_nd) - 无描述
168. [ods_db_vc_activity_nd](#ods_db_vc_activity_nd) - 无描述
169. [ods_db_vc_ad_watch_order_nd](#ods_db_vc_ad_watch_order_nd) - 无描述
170. [ods_db_vc_admin_auth_log_nd](#ods_db_vc_admin_auth_log_nd) - 无描述
171. [ods_db_vc_admin_menu_nd](#ods_db_vc_admin_menu_nd) - 无描述
172. [ods_db_vc_admin_operation_log_nd](#ods_db_vc_admin_operation_log_nd) - 无描述
173. [ods_db_vc_admin_review_nd](#ods_db_vc_admin_review_nd) - 无描述
174. [ods_db_vc_admin_role_group_nd](#ods_db_vc_admin_role_group_nd) - 无描述
175. [ods_db_vc_admin_role_menu_relation_nd](#ods_db_vc_admin_role_menu_relation_nd) - 无描述
176. [ods_db_vc_admin_user_role_relation_nd](#ods_db_vc_admin_user_role_relation_nd) - 无描述
177. [ods_db_vc_adornment_nd](#ods_db_vc_adornment_nd) - 无描述
178. [ods_db_vc_ai_experiment_log_nd](#ods_db_vc_ai_experiment_log_nd) - 无描述
179. [ods_db_vc_ai_scene_code_nd](#ods_db_vc_ai_scene_code_nd) - 无描述
180. [ods_db_vc_ai_scene_rule_nd](#ods_db_vc_ai_scene_rule_nd) - 无描述
181. [ods_db_vc_annual_gift_character_rank_nd](#ods_db_vc_annual_gift_character_rank_nd) - 无描述
182. [ods_db_vc_annual_gift_character_stats_nd](#ods_db_vc_annual_gift_character_stats_nd) - 无描述
183. [ods_db_vc_annual_gift_detail_nd](#ods_db_vc_annual_gift_detail_nd) - 无描述
184. [ods_db_vc_annual_gift_user_stats_nd](#ods_db_vc_annual_gift_user_stats_nd) - 无描述
185. [ods_db_vc_backend_tag_nd](#ods_db_vc_backend_tag_nd) - 无描述
186. [ods_db_vc_batch_record_nd](#ods_db_vc_batch_record_nd) - 无描述
187. [ods_db_vc_batch_task_nd](#ods_db_vc_batch_task_nd) - 无描述
188. [ods_db_vc_birthday_daily_task_nd](#ods_db_vc_birthday_daily_task_nd) - 无描述
189. [ods_db_vc_birthday_question_bank_nd](#ods_db_vc_birthday_question_bank_nd) - 无描述
190. [ods_db_vc_birthday_support_gift_nd](#ods_db_vc_birthday_support_gift_nd) - 无描述
191. [ods_db_vc_birthday_support_gift_record_nd](#ods_db_vc_birthday_support_gift_record_nd) - 无描述
192. [ods_db_vc_birthday_support_ranking_nd](#ods_db_vc_birthday_support_ranking_nd) - 无描述
193. [ods_db_vc_birthday_user_answer_record_nd](#ods_db_vc_birthday_user_answer_record_nd) - 无描述
194. [ods_db_vc_board_game_message_record_nd](#ods_db_vc_board_game_message_record_nd) - 无描述
195. [ods_db_vc_call_back_log_nd](#ods_db_vc_call_back_log_nd) - 无描述
196. [ods_db_vc_category_nd](#ods_db_vc_category_nd) - 无描述
197. [ods_db_vc_character_ai_content_nd](#ods_db_vc_character_ai_content_nd) - 无描述
198. [ods_db_vc_character_choose_nd](#ods_db_vc_character_choose_nd) - 无描述
199. [ods_db_vc_character_corpus_nd](#ods_db_vc_character_corpus_nd) - 无描述
200. [ods_db_vc_character_free_config_nd](#ods_db_vc_character_free_config_nd) - 无描述
201. [ods_db_vc_character_hot_top_nd](#ods_db_vc_character_hot_top_nd) - 无描述
202. [ods_db_vc_character_impression_like_nd](#ods_db_vc_character_impression_like_nd) - 无描述
203. [ods_db_vc_character_impression_nd](#ods_db_vc_character_impression_nd) - 无描述
204. [ods_db_vc_character_moment_backup_nd](#ods_db_vc_character_moment_backup_nd) - 无描述
205. [ods_db_vc_character_moment_nd](#ods_db_vc_character_moment_nd) - 无描述
206. [ods_db_vc_character_nd](#ods_db_vc_character_nd) - 无描述
207. [ods_db_vc_character_ogc_convert_log_nd](#ods_db_vc_character_ogc_convert_log_nd) - 无描述
208. [ods_db_vc_character_package_config_nd](#ods_db_vc_character_package_config_nd) - 无描述
209. [ods_db_vc_character_secret_nd](#ods_db_vc_character_secret_nd) - 无描述
210. [ods_db_vc_character_skin_nd](#ods_db_vc_character_skin_nd) - 无描述
211. [ods_db_vc_character_tag_relation_nd](#ods_db_vc_character_tag_relation_nd) - 无描述
212. [ods_db_vc_character_voice_pack_nd](#ods_db_vc_character_voice_pack_nd) - 无描述
213. [ods_db_vc_comment_nd](#ods_db_vc_comment_nd) - 无描述
214. [ods_db_vc_config_nd](#ods_db_vc_config_nd) - 无描述
215. [ods_db_vc_content_analyze_nd](#ods_db_vc_content_analyze_nd) - 无描述
216. [ods_db_vc_content_audit_nd](#ods_db_vc_content_audit_nd) - 无描述
217. [ods_db_vc_data_character_choose_nd](#ods_db_vc_data_character_choose_nd) - 无描述
218. [ods_db_vc_date_card_lottery_record_nd](#ods_db_vc_date_card_lottery_record_nd) - 无描述
219. [ods_db_vc_date_card_nd](#ods_db_vc_date_card_nd) - 无描述
220. [ods_db_vc_date_card_physical_exchange_nd](#ods_db_vc_date_card_physical_exchange_nd) - 无描述
221. [ods_db_vc_date_card_reward_acquire_log_nd](#ods_db_vc_date_card_reward_acquire_log_nd) - 无描述
222. [ods_db_vc_date_card_story_nd](#ods_db_vc_date_card_story_nd) - 无描述
223. [ods_db_vc_delay_task_nd](#ods_db_vc_delay_task_nd) - 无描述
224. [ods_db_vc_device_record_nd](#ods_db_vc_device_record_nd) - 无描述
225. [ods_db_vc_device_report_record_nd](#ods_db_vc_device_report_record_nd) - 无描述
226. [ods_db_vc_dungeon_chat_backtrace_nd](#ods_db_vc_dungeon_chat_backtrace_nd) - 无描述
227. [ods_db_vc_event_config_nd](#ods_db_vc_event_config_nd) - 无描述
228. [ods_db_vc_event_info_nd](#ods_db_vc_event_info_nd) - 无描述
229. [ods_db_vc_front_tag_nd](#ods_db_vc_front_tag_nd) - 无描述
230. [ods_db_vc_game_banner_nd](#ods_db_vc_game_banner_nd) - 无描述
231. [ods_db_vc_game_favorite_nd](#ods_db_vc_game_favorite_nd) - 无描述
232. [ods_db_vc_game_follow_nd](#ods_db_vc_game_follow_nd) - 无描述
233. [ods_db_vc_game_hot_recommend_nd](#ods_db_vc_game_hot_recommend_nd) - 无描述
234. [ods_db_vc_game_info_nd](#ods_db_vc_game_info_nd) - 无描述
235. [ods_db_vc_game_like_nd](#ods_db_vc_game_like_nd) - 无描述
236. [ods_db_vc_game_record_nd](#ods_db_vc_game_record_nd) - 无描述
237. [ods_db_vc_game_relation_record_nd](#ods_db_vc_game_relation_record_nd) - 无描述
238. [ods_db_vc_game_room_nd](#ods_db_vc_game_room_nd) - 无描述
239. [ods_db_vc_game_user_account_nd](#ods_db_vc_game_user_account_nd) - 无描述
240. [ods_db_vc_game_user_lofter_bind_nd](#ods_db_vc_game_user_lofter_bind_nd) - 无描述
241. [ods_db_vc_game_workshop_draft_nd](#ods_db_vc_game_workshop_draft_nd) - 无描述
242. [ods_db_vc_gift_nd](#ods_db_vc_gift_nd) - 无描述
243. [ods_db_vc_guide_card_log_nd](#ods_db_vc_guide_card_log_nd) - 无描述
244. [ods_db_vc_id_auth_nd](#ods_db_vc_id_auth_nd) - 无描述
245. [ods_db_vc_invitation_reward_records_nd](#ods_db_vc_invitation_reward_records_nd) - 无描述
246. [ods_db_vc_invite_code_nd](#ods_db_vc_invite_code_nd) - 无描述
247. [ods_db_vc_invite_relation_nd](#ods_db_vc_invite_relation_nd) - 无描述
248. [ods_db_vc_item_group_nd](#ods_db_vc_item_group_nd) - 无描述
249. [ods_db_vc_item_nd](#ods_db_vc_item_nd) - 无描述
250. [ods_db_vc_jump_link_nd](#ods_db_vc_jump_link_nd) - 无描述
251. [ods_db_vc_like_record_nd](#ods_db_vc_like_record_nd) - 无描述
252. [ods_db_vc_lofter_group_member_nd](#ods_db_vc_lofter_group_member_nd) - 无描述
253. [ods_db_vc_lofter_group_nd](#ods_db_vc_lofter_group_nd) - 无描述
254. [ods_db_vc_log_task_nd](#ods_db_vc_log_task_nd) - 无描述
255. [ods_db_vc_log_upload_nd](#ods_db_vc_log_upload_nd) - 无描述
256. [ods_db_vc_main_task_nd](#ods_db_vc_main_task_nd) - 无描述
257. [ods_db_vc_moment_read_nd](#ods_db_vc_moment_read_nd) - 无描述
258. [ods_db_vc_order_nd](#ods_db_vc_order_nd) - 无描述
259. [ods_db_vc_periphery_act_nd](#ods_db_vc_periphery_act_nd) - 无描述
260. [ods_db_vc_periphery_lottery_records_nd](#ods_db_vc_periphery_lottery_records_nd) - 无描述
261. [ods_db_vc_periphery_order_nd](#ods_db_vc_periphery_order_nd) - 无描述
262. [ods_db_vc_periphery_product_nd](#ods_db_vc_periphery_product_nd) - 无描述
263. [ods_db_vc_plot_summary_nd](#ods_db_vc_plot_summary_nd) - 无描述
264. [ods_db_vc_prompt_test_task_nd](#ods_db_vc_prompt_test_task_nd) - 无描述
265. [ods_db_vc_prompt_test_task_result_detail_nd](#ods_db_vc_prompt_test_task_result_detail_nd) - 无描述
266. [ods_db_vc_prompt_test_task_result_nd](#ods_db_vc_prompt_test_task_result_nd) - 无描述
267. [ods_db_vc_props_nd](#ods_db_vc_props_nd) - 无描述
268. [ods_db_vc_push_message_info_nd](#ods_db_vc_push_message_info_nd) - 无描述
269. [ods_db_vc_rank_config_nd](#ods_db_vc_rank_config_nd) - 无描述
270. [ods_db_vc_rank_data_nd](#ods_db_vc_rank_data_nd) - 无描述
271. [ods_db_vc_rank_list_nd](#ods_db_vc_rank_list_nd) - 无描述
272. [ods_db_vc_redeem_code_nd](#ods_db_vc_redeem_code_nd) - 无描述
273. [ods_db_vc_redeem_code_receive_record_nd](#ods_db_vc_redeem_code_receive_record_nd) - 无描述
274. [ods_db_vc_reward_task_detail_nd](#ods_db_vc_reward_task_detail_nd) - 无描述
275. [ods_db_vc_reward_task_nd](#ods_db_vc_reward_task_nd) - 无描述
276. [ods_db_vc_rule_info_nd](#ods_db_vc_rule_info_nd) - 无描述
277. [ods_db_vc_simulator_attr_lib_nd](#ods_db_vc_simulator_attr_lib_nd) - 无描述
278. [ods_db_vc_simulator_controller_lib_nd](#ods_db_vc_simulator_controller_lib_nd) - 无描述
279. [ods_db_vc_simulator_editorial_nd](#ods_db_vc_simulator_editorial_nd) - 无描述
280. [ods_db_vc_simulator_info_nd](#ods_db_vc_simulator_info_nd) - 无描述
281. [ods_db_vc_simulator_pick_list_nd](#ods_db_vc_simulator_pick_list_nd) - 无描述
282. [ods_db_vc_simulator_room_nd](#ods_db_vc_simulator_room_nd) - 无描述
283. [ods_db_vc_simulator_room_participant_nd](#ods_db_vc_simulator_room_participant_nd) - 无描述
284. [ods_db_vc_simulator_template_nd](#ods_db_vc_simulator_template_nd) - 无描述
285. [ods_db_vc_simulator_zone_nd](#ods_db_vc_simulator_zone_nd) - 无描述
286. [ods_db_vc_sub_task_nd](#ods_db_vc_sub_task_nd) - 无描述
287. [ods_db_vc_suggestion_nd](#ods_db_vc_suggestion_nd) - 无描述
288. [ods_db_vc_suicide_prevention_record_nd](#ods_db_vc_suicide_prevention_record_nd) - 无描述
289. [ods_db_vc_support_detail_nd](#ods_db_vc_support_detail_nd) - 无描述
290. [ods_db_vc_support_plan_nd](#ods_db_vc_support_plan_nd) - 无描述
291. [ods_db_vc_tag_nd](#ods_db_vc_tag_nd) - 无描述
292. [ods_db_vc_url_source_info_nd](#ods_db_vc_url_source_info_nd) - 无描述
293. [ods_db_vc_urs_callback_log_nd](#ods_db_vc_urs_callback_log_nd) - 无描述
294. [ods_db_vc_user_account_config_nd](#ods_db_vc_user_account_config_nd) - 无描述
295. [ods_db_vc_user_account_nd](#ods_db_vc_user_account_nd) - 无描述
296. [ods_db_vc_user_activity_record_nd](#ods_db_vc_user_activity_record_nd) - 无描述
297. [ods_db_vc_user_app_small_module_nd](#ods_db_vc_user_app_small_module_nd) - 无描述
298. [ods_db_vc_user_asset_consume_flow_all_dd](#ods_db_vc_user_asset_consume_flow_all_dd) - 无描述
299. [ods_db_vc_user_asset_consume_flow_i_d](#ods_db_vc_user_asset_consume_flow_i_d) - 无描述
300. [ods_db_vc_user_asset_consume_flow_nd](#ods_db_vc_user_asset_consume_flow_nd) - 无描述
301. [ods_db_vc_user_asset_left_nd](#ods_db_vc_user_asset_left_nd) - 无描述
302. [ods_db_vc_user_asset_nd](#ods_db_vc_user_asset_nd) - 无描述
303. [ods_db_vc_user_backpack_nd](#ods_db_vc_user_backpack_nd) - 无描述
304. [ods_db_vc_user_benefits_nd](#ods_db_vc_user_benefits_nd) - 无描述
305. [ods_db_vc_user_birthday_support_nd](#ods_db_vc_user_birthday_support_nd) - 无描述
306. [ods_db_vc_user_character_ai_experiment_nd](#ods_db_vc_user_character_ai_experiment_nd) - 无描述
307. [ods_db_vc_user_character_choose_model_log_nd](#ods_db_vc_user_character_choose_model_log_nd) - 无描述
308. [ods_db_vc_user_character_choose_model_nd](#ods_db_vc_user_character_choose_model_nd) - 无描述
309. [ods_db_vc_user_character_group_nd](#ods_db_vc_user_character_group_nd) - 无描述
310. [ods_db_vc_user_character_memory_record_nd](#ods_db_vc_user_character_memory_record_nd) - 无描述
311. [ods_db_vc_user_character_memory_relation_nd](#ods_db_vc_user_character_memory_relation_nd) - 无描述
312. [ods_db_vc_user_character_relation_nd](#ods_db_vc_user_character_relation_nd) - 无描述
313. [ods_db_vc_user_character_simulator_chat_relation_nd](#ods_db_vc_user_character_simulator_chat_relation_nd) - 无描述
314. [ods_db_vc_user_character_skin_nd](#ods_db_vc_user_character_skin_nd) - 无描述
315. [ods_db_vc_user_date_card_nd](#ods_db_vc_user_date_card_nd) - 无描述
316. [ods_db_vc_user_deduction_record_nd](#ods_db_vc_user_deduction_record_nd) - 无描述
317. [ods_db_vc_user_diary_nd](#ods_db_vc_user_diary_nd) - 无描述
318. [ods_db_vc_user_diary_visit_log_nd](#ods_db_vc_user_diary_visit_log_nd) - 无描述
319. [ods_db_vc_user_dungeon_chat_branch_nd](#ods_db_vc_user_dungeon_chat_branch_nd) - 无描述
320. [ods_db_vc_user_dungeon_chat_record_nd](#ods_db_vc_user_dungeon_chat_record_nd) - 无描述
321. [ods_db_vc_user_event_count_nd](#ods_db_vc_user_event_count_nd) - 无描述
322. [ods_db_vc_user_event_task_detail_nd](#ods_db_vc_user_event_task_detail_nd) - 无描述
323. [ods_db_vc_user_event_task_info_nd](#ods_db_vc_user_event_task_info_nd) - 无描述
324. [ods_db_vc_user_feedback_record_nd](#ods_db_vc_user_feedback_record_nd) - 无描述
325. [ods_db_vc_user_fetter_notice_nd](#ods_db_vc_user_fetter_notice_nd) - 无描述
326. [ods_db_vc_user_gift_nd](#ods_db_vc_user_gift_nd) - 无描述
327. [ods_db_vc_user_income_record_nd](#ods_db_vc_user_income_record_nd) - 无描述
328. [ods_db_vc_user_interact_message_nd](#ods_db_vc_user_interact_message_nd) - 无描述
329. [ods_db_vc_user_item_subscription_record_nd](#ods_db_vc_user_item_subscription_record_nd) - 无描述
330. [ods_db_vc_user_like_op_log_nd](#ods_db_vc_user_like_op_log_nd) - 无描述
331. [ods_db_vc_user_message_list_nd](#ods_db_vc_user_message_list_nd) - 无描述
332. [ods_db_vc_user_message_record_partition_nd](#ods_db_vc_user_message_record_partition_nd) - 无描述
333. [ods_db_vc_user_mystery_gift_prize_nd](#ods_db_vc_user_mystery_gift_prize_nd) - 无描述
334. [ods_db_vc_user_numeric_nd](#ods_db_vc_user_numeric_nd) - 无描述
335. [ods_db_vc_user_op_record_nd](#ods_db_vc_user_op_record_nd) - 无描述
336. [ods_db_vc_user_preference_nd](#ods_db_vc_user_preference_nd) - 无描述
337. [ods_db_vc_user_props_consume_flow_nd](#ods_db_vc_user_props_consume_flow_nd) - 无描述
338. [ods_db_vc_user_props_nd](#ods_db_vc_user_props_nd) - 无描述
339. [ods_db_vc_user_purchase_record_nd](#ods_db_vc_user_purchase_record_nd) - 无描述
340. [ods_db_vc_user_real_auth_notice_nd](#ods_db_vc_user_real_auth_notice_nd) - 无描述
341. [ods_db_vc_user_secret_nd](#ods_db_vc_user_secret_nd) - 无描述
342. [ods_db_vc_user_share_record_nd](#ods_db_vc_user_share_record_nd) - 无描述
343. [ods_db_vc_user_simulator_detail_info_nd](#ods_db_vc_user_simulator_detail_info_nd) - 无描述
344. [ods_db_vc_user_simulator_group_log_nd](#ods_db_vc_user_simulator_group_log_nd) - 无描述
345. [ods_db_vc_user_simulator_init_info_nd](#ods_db_vc_user_simulator_init_info_nd) - 无描述
346. [ods_db_vc_user_simulator_play_relation_nd](#ods_db_vc_user_simulator_play_relation_nd) - 无描述
347. [ods_db_vc_user_simulator_result_log_nd](#ods_db_vc_user_simulator_result_log_nd) - 无描述
348. [ods_db_vc_user_simulator_result_unlock_nd](#ods_db_vc_user_simulator_result_unlock_nd) - 无描述
349. [ods_db_vc_user_support_task_nd](#ods_db_vc_user_support_task_nd) - 无描述
350. [ods_db_vc_user_topic_box_like_nd](#ods_db_vc_user_topic_box_like_nd) - 无描述
351. [ods_db_vc_user_topic_box_nd](#ods_db_vc_user_topic_box_nd) - 无描述
352. [ods_db_vc_user_votes_nd](#ods_db_vc_user_votes_nd) - 无描述
353. [ods_db_vc_vibe_game_instance_nd](#ods_db_vc_vibe_game_instance_nd) - 无描述
354. [ods_db_vc_vibe_game_log_nd](#ods_db_vc_vibe_game_log_nd) - 无描述
355. [ods_db_vc_visual_novel_script_nd](#ods_db_vc_visual_novel_script_nd) - 无描述
356. [ods_db_vc_visual_novel_unlock_nd](#ods_db_vc_visual_novel_unlock_nd) - 无描述
357. [ods_db_vc_voice_copy_nd](#ods_db_vc_voice_copy_nd) - 无描述
358. [ods_db_vc_zone_nd](#ods_db_vc_zone_nd) - 无描述
359. [ods_log_ab_platform_sdk_log_di](#ods_log_ab_platform_sdk_log_di) - 无描述
360. [ods_log_ad_new_linkup_di](#ods_log_ad_new_linkup_di) - 无描述
361. [ods_mda_app_di](#ods_mda_app_di) - 无描述
362. [ods_mda_app_raw_di](#ods_mda_app_raw_di) - 无描述
363. [ods_mda_vc_game_web_di](#ods_mda_vc_game_web_di) - 无描述
364. [ods_mda_wap_di](#ods_mda_wap_di) - 无描述
365. [ods_vc_enum_code_a_d](#ods_vc_enum_code_a_d) - 无描述
366. [ods_vc_h5_ectypal_chat_i_d](#ods_vc_h5_ectypal_chat_i_d) - ectypal_chat 消息数据表(扁平结构)
367. [ods_vc_log_ai_chat_di](#ods_vc_log_ai_chat_di) - 无描述
368. [ods_vc_log_ai_scene_i_d](#ods_vc_log_ai_scene_i_d) - 无描述
369. [ods_vc_log_antispam_audit_trace_di](#ods_vc_log_antispam_audit_trace_di) - 无描述
370. [ods_vc_log_antispam_difference_di](#ods_vc_log_antispam_difference_di) - 无描述
371. [ods_vc_log_board_game_message_di](#ods_vc_log_board_game_message_di) - 无描述
372. [ods_vc_log_common_ai_invoke_i_d](#ods_vc_log_common_ai_invoke_i_d) - 无描述
373. [ods_vc_log_low_quality_reply_di](#ods_vc_log_low_quality_reply_di) - 无描述
374. [ods_vc_log_message_type_i_d](#ods_vc_log_message_type_i_d) - 无描述
375. [ods_vc_log_order_i_d](#ods_vc_log_order_i_d) - 无描述
376. [ods_vc_log_user_activity_i_d](#ods_vc_log_user_activity_i_d) - 无描述
377. [ods_vc_log_vc_enum_i_d](#ods_vc_log_vc_enum_i_d) - 无描述
378. [ods_vc_log_vgame_workshop_chat_di](#ods_vc_log_vgame_workshop_chat_di) - 无描述
379. [ods_vc_rec_pool_daily_i_d](#ods_vc_rec_pool_daily_i_d) - avg推荐后端日志 - vc
380. [ods_vc_rec_pool_daily_i_h](#ods_vc_rec_pool_daily_i_h) - avg推荐后端日志 - vc
381. [ods_vc_rec_pool_result_i_d](#ods_vc_rec_pool_result_i_d) - avg推荐后端日志结果
382. [ods_vc_rec_pool_result_i_h](#ods_vc_rec_pool_result_i_h) - avg推荐后端日志结果
383. [rec_vc_data_l2_rec_pool_daily](#rec_vc_data_l2_rec_pool_daily) - 无描述
384. [rec_vc_data_l2_rec_pool_daily_hour_tmp](#rec_vc_data_l2_rec_pool_daily_hour_tmp) - 无描述
385. [rec_vc_data_l2_rec_pool_daily_tmp](#rec_vc_data_l2_rec_pool_daily_tmp) - 无描述

---

## ads_pv_character_gmv_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_pv_character_gmv_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 583.5M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `gmv` | `double` |  |  |
| 3 | `channel` | `string` |  |  |
| 4 | `dt` | `string` |  |  |

---

## ads_pv_character_talk_cnt_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_pv_character_talk_cnt_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 623.6M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  |  |
| 2 | `role_id` | `bigint` |  |  |
| 3 | `character_type` | `string` |  |  |
| 4 | `vc_total_talk_cnt` | `bigint` |  |  |
| 5 | `pve_total_talk_cnt` | `bigint` |  |  |
| 6 | `total_talk_cnt` | `bigint` |  |  |
| 7 | `front_tags` | `array<string>` |  |  |
| 8 | `backend_tags` | `array<string>` |  |  |
| 9 | `tags` | `array<string>` |  |  |
| 10 | `character_tags` | `array<string>` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ads_pv_uc_action_info_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_pv_uc_action_info_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 5.5G |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_type` | `string` |  |  |
| 4 | `chat_num_3` | `bigint` |  |  |
| 5 | `chat_num_7` | `bigint` |  |  |
| 6 | `chat_num_15` | `bigint` |  |  |
| 7 | `chat_num_30` | `bigint` |  |  |
| 8 | `fetter_nums` | `int` |  |  |
| 9 | `shield_status` | `int` |  | 状态：1.正常 2.删除 |
| 10 | `disturb_status` | `int` |  | 免打扰状态 1:正常 -1:免打扰 |
| 11 | `channel` | `string` |  |  |
| 12 | `dt` | `string` |  |  |

---

## ads_pv_user_behavior_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_pv_user_behavior_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.6G |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  |  |
| 2 | `user_code` | `bigint` |  |  |
| 3 | `user_type` | `bigint` |  |  |
| 4 | `last_talk_date` | `string` |  |  |
| 5 | `talk_intervals` | `int` |  |  |
| 6 | `register_date` | `string` |  |  |
| 7 | `last_active_date` | `string` |  |  |
| 8 | `active_intervals` | `int` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ads_rec_dispatch_human_characters_index_general

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_rec_dispatch_human_characters_index_general` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | internal |
| **表大小** | 2.0B |
| **是否分区表** | 否 |

### 字段详情

共 1 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `value` | `string` |  |  |

---

## ads_rec_theater_feature

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_rec_theater_feature` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.lifeng08 |
| **表类型** | internal |
| **表大小** | 2.4M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `item_id` | `string` |  |  |
| 2 | `category` | `int` |  |  |
| 3 | `type` | `int` |  |  |
| 4 | `weight` | `bigint` |  |  |
| 5 | `status` | `bigint` |  |  |
| 6 | `is_trial` | `int` |  |  |
| 7 | `create_by` | `string` |  |  |
| 8 | `update_by` | `string` |  |  |
| 9 | `effective_time` | `bigint` |  |  |
| 10 | `expiration_time` | `bigint` |  |  |
| 11 | `content_type` | `int` |  |  |
| 12 | `channel` | `int` |  |  |
| 13 | `public_scope` | `int` |  |  |
| 14 | `front_tags` | `string` |  |  |
| 15 | `backend_tags` | `string` |  |  |
| 16 | `rec_valid_tag` | `int` |  |  |
| 17 | `gmv` | `float` |  |  |
| 18 | `day` | `string` |  |  |

---

## ads_rec_theater_pool_l1

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_rec_theater_pool_l1` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.lifeng08 |
| **表类型** | internal |
| **表大小** | 44.1K |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `item_id` | `string` |  |  |
| 2 | `item_type` | `string` |  |  |
| 3 | `day` | `string` |  |  |

---

## ads_uc_ctype_talk_cnt_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_uc_ctype_talk_cnt_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 21.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 21.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `statistical_period` | `string` |  |  |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `category` | `string` |  | 品类名称 |
| 5 | `backend_tags` | `string` |  | 后台类目 |
| 6 | `talk_cnt` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ads_underage_user_easy_fetch_verify_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_underage_user_easy_fetch_verify_dd` |
| **描述** | 未成年命中及认证结果 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 12.7M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `user_code` | `bigint` |  | userCode |
| 3 | `create_date` | `date` |  | 创建日期 |
| 4 | `is_verified` | `string` |  | 是否认证 Y, N |
| 5 | `age` | `int` |  | 年龄 |
| 6 | `dt` | `string` |  |  |

---

## ads_vc_ab_platform_experiment_metric_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_ab_platform_experiment_metric_dd` |
| **描述** | 破次元恋人AB实验指标展示表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 50.9M |
| **是否分区表** | 是 |

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_date` | `string` |  | 实验日期 |
| 2 | `appid` | `bigint` |  | 应用id |
| 3 | `sceneid` | `bigint` |  | 场景id |
| 4 | `exp_id` | `bigint` |  | 实验id |
| 5 | `bucket_id` | `bigint` |  | 实验分组 |
| 6 | `dimension` | `string` |  | 维度 |
| 7 | `dimension_value` | `string` |  | 维度值 |
| 8 | `group_name` | `string` |  | 实验分组名称 |
| 9 | `appname` | `string` |  | 应用名称 |
| 10 | `scenename` | `string` |  | 场景名称 |
| 11 | `exp_uv_sum` | `double` |  | 实验uv |
| 12 | `exp_pv_avg` | `double` |  | 人均请求数 |
| 13 | `exp_pv_sum` | `double` |  | 实验pv 请求数 |
| 14 | `vc_gmv_sum` | `double` |  | 总营收 |
| 15 | `vc_arpu` | `double` |  | ARPU |
| 16 | `vc_arppu` | `double` |  | ARPPU |
| 17 | `vc_pay_money_avg` | `double` |  | 平均消费金额 |
| 18 | `vc_pay_uv_avg` | `double` |  | 付费人数占比 |
| 19 | `vc_pay_uv_sum` | `double` |  | 付费人数 |
| 20 | `vc_chat_uv_sum` | `double` |  | 聊天人数 |
| 21 | `vc_chat_rounds_avg` | `double` |  | 平均聊天轮数 |
| 22 | `vc_chat_rounds_sum` | `double` |  | 总聊天轮数 |
| 23 | `vc_roles_cnt_avg` | `double` |  | 平均聊天角色数 |
| 24 | `vc_roles_cnt_sum` | `double` |  | 总聊天角色数(未去重) |
| 25 | `vc_visit_uv_avg` | `double` |  | 页面曝光uv覆盖率 |
| 26 | `vc_visit_uv_sum` | `double` |  | 页面曝光uv |
| 27 | `vc_visit_retain_6d_avg` | `double` |  | 七日留存率 |
| 28 | `vc_visit_retain_1d_avg` | `double` |  | 次日留存率 |
| 29 | `vc_visit2pay_rate` | `double` |  | 访问到付费率 |
| 30 | `vc_visit2chat_rate` | `double` |  | 访问到聊天率 |
| 31 | `vc_chat2pay_rate` | `double` |  | 聊天到付费率 |
| 32 | `dt` | `string` |  |  |

---

## ads_vc_ab_platform_experiment_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_ab_platform_experiment_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 119.8M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_date` | `string` |  | 实验日期 |
| 2 | `appid` | `bigint` |  | 应用id |
| 3 | `sceneid` | `bigint` |  | 场景id |
| 4 | `exp_id` | `bigint` |  | 实验id |
| 5 | `bucket_id` | `bigint` |  | 实验分组 |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `dimension` | `string` |  | 维度 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `metric_value` | `double` |  | 指标值 |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_character_choose_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_character_choose_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 22.0M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_type` | `string` |  |  |
| 3 | `total_talk_cnt` | `bigint` |  |  |
| 4 | `total_peoples` | `bigint` |  |  |
| 5 | `status` | `int` |  | 状态,1:上架,0:下架 |
| 6 | `index_feed_status` | `int` |  | 是否加入发现页瀑布流,1:是,0:否 |
| 7 | `stranger_msg_status` | `int` |  | 是否加入陌生人消息,1:是,0:否 |
| 8 | `chat_recommend_status` | `int` |  | 是否加入聊天页推荐,1:是,0:否 |
| 9 | `channel` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ads_vc_character_package_rank_ct_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_character_package_rank_ct_a_d` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | internal |
| **表大小** | 11.6K |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `target_type` | `int` |  |  |
| 2 | `rank_id` | `int` |  |  |
| 3 | `pack_id` | `bigint` |  | 角色包id |
| 4 | `target_id` | `bigint` |  | 角色id |
| 5 | `nums` | `bigint` |  |  |
| 6 | `rk` | `int` |  |  |
| 7 | `version` | `int` |  |  |
| 8 | `_count` | `bigint` |  |  |

---

## ads_vc_character_period_top100_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_character_period_top100_n_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 59.5K |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `period` | `string` |  |  |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_name` | `string` |  | 角色名称 |
| 4 | `character_type` | `string` |  |  |
| 5 | `talk_cnt` | `bigint` |  |  |
| 6 | `t_rk` | `int` |  |  |
| 7 | `peoples` | `bigint` |  |  |
| 8 | `p_rk` | `int` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_growth_ad_period_cvr_tmp

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_growth_ad_period_cvr_tmp` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 39.0M |
| **是否分区表** | 是 |

### 字段详情

共 36 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_type` | `string` |  | 设备类型: new return_30 |
| 2 | `deviceos` | `string` |  |  |
| 3 | `advertiserid` | `string` |  |  |
| 4 | `appchannel` | `string` |  |  |
| 5 | `campaignid` | `string` |  |  |
| 6 | `aid` | `string` |  |  |
| 7 | `cid` | `string` |  |  |
| 8 | `media` | `string` |  |  |
| 9 | `proxy` | `string` |  |  |
| 10 | `custom_ouid` | `string` |  |  |
| 11 | `photoid` | `string` |  |  |
| 12 | `newuv` | `bigint` |  |  |
| 13 | `loguv` | `bigint` |  |  |
| 14 | `reguv` | `bigint` |  |  |
| 15 | `per_activedays` | `double` |  |  |
| 16 | `active_2days_uv` | `bigint` |  |  |
| 17 | `n_day_uv` | `bigint` |  |  |
| 18 | `per_duration_minutes` | `double` |  |  |
| 19 | `interactionuv` | `bigint` |  |  |
| 20 | `hotuv` | `bigint` |  |  |
| 21 | `hotpv` | `bigint` |  |  |
| 22 | `commenduv` | `bigint` |  |  |
| 23 | `commendpv` | `bigint` |  |  |
| 24 | `postuv` | `bigint` |  |  |
| 25 | `postpv` | `bigint` |  |  |
| 26 | `duration_uv` | `bigint` |  |  |
| 27 | `excellent_uv` | `bigint` |  |  |
| 28 | `impounding_uv` | `bigint` |  |  |
| 29 | `whiteboard_uv` | `bigint` |  | 白板用户数 |
| 30 | `photo_url` | `string` |  |  |
| 31 | `photo_caption` | `string` |  |  |
| 32 | `star_user_id` | `bigint` |  |  |
| 33 | `star_name` | `string` |  |  |
| 34 | `invest_amount` | `double` |  |  |
| 35 | `period` | `int` |  |  |
| 36 | `dt` | `string` |  |  |

---

## ads_vc_hive2kafka_202501_i_h

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_hive2kafka_202501_i_h` |
| **描述** | 两小时未回复 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 552.4M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `value` | `string` |  | 值 |
| 4 | `sign` | `string` |  | 标识 |
| 5 | `dt` | `string` |  |  |
| 6 | `h` | `string` |  |  |

---

## ads_vc_hive2kafka_202501_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_hive2kafka_202501_n_d` |
| **描述** | 春节版本---人群包 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 482.0M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `value` | `string` |  | 值 |
| 4 | `sign` | `string` |  | 标识 |
| 5 | `dt` | `string` |  |  |

---

## ads_vc_hive2redis_202501_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_hive2redis_202501_n_d` |
| **描述** | 春节版本---人群包 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 165.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 165.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `key` | `string` |  | redis-key |
| 2 | `value` | `string` |  | redis-value |
| 3 | `dt` | `string` |  |  |

---

## ads_vc_hive2redis_202503_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_hive2redis_202503_n_d` |
| **描述** | 自动化短信---人群包，数据有效期：36h |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 8.4G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `key` | `string` |  | redis-key |
| 2 | `value` | `string` |  | redis-value |
| 3 | `dt` | `string` |  |  |

---

## ads_vc_hot_characters_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_hot_characters_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 35.5M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名称 |
| 3 | `character_type` | `string` |  |  |
| 4 | `total_talk_cnt` | `bigint` |  |  |
| 5 | `total_peoples` | `bigint` |  |  |
| 6 | `dt` | `string` |  |  |

---

## ads_vc_lose_user_talk_period_cnt_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_lose_user_talk_period_cnt_n_d` |
| **描述** | 流失用户 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1020.5M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `statistical_period` | `string` |  | 统计周期 |
| 3 | `not_talk_period` | `string` |  | 未聊天日期 |
| 4 | `total_talks` | `int` |  | 聊天轮次 |
| 5 | `dt` | `string` |  |  |

---

## ads_vc_om_22605_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_om_22605_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  |  |
| 2 | `character_id` | `bigint` |  |  |
| 3 | `model` | `string` |  |  |
| 4 | `turnid` | `int` |  |  |
| 5 | `response` | `string` |  |  |
| 6 | `reason` | `string` |  |  |
| 7 | `label` | `string` |  |  |
| 8 | `confidence` | `string` |  |  |
| 9 | `charatcer_name` | `string` |  |  |
| 10 | `yesterday_cnt` | `string` |  |  |
| 11 | `today_cnt` | `string` |  |  |
| 12 | `dt` | `string` |  |  |

---

## ads_vc_om_25313_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_om_25313_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 41.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 41.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  |  |
| 2 | `character_type` | `string` |  |  |
| 3 | `characters` | `string` |  |  |
| 4 | `dt` | `string` |  |  |

---

## ads_vc_om_3116_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_om_3116_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 639.7K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `platform` | `string` |  | 平台 |
| 2 | `channel` | `string` |  | 渠道 |
| 3 | `ad_id` | `string` |  | 广告id |
| 4 | `value` | `string` |  |  |
| 5 | `dt` | `string` |  |  |

---

## ads_vc_om_3704_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_om_3704_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 24.9M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  |  |
| 2 | `data` | `array<row<int,int,string>('character_id','sort','message_id')>` |  |  |
| 3 | `dt` | `string` |  |  |

---

## ads_vc_om_3827_2h_v2_i_h

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_om_3827_2h_v2_i_h` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 3.4G |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  |  |
| 2 | `data` | `array<row<int,double>('character_id','talk_interval')>` |  |  |
| 3 | `dt` | `string` |  |  |
| 4 | `hour` | `string` |  |  |

---

## ads_vc_om_3827_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_om_3827_a_d` |
| **描述** | 用户活跃明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 51.1M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## ads_vc_tags_data_bitmap_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_tags_data_bitmap_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 13.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 13.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag_name` | `string` |  | 标签名 |
| 2 | `dim1` | `string` |  | 维度1 |
| 3 | `dim2` | `string` |  | 维度2 |
| 4 | `dim3` | `string` |  | 维度3 |
| 5 | `dim4` | `string` |  | 维度4 |
| 6 | `grp` | `string` |  |  |
| 7 | `bitmap` | `varbinary(2147483647)` |  |  |
| 8 | `dt` | `string` |  |  |
| 9 | `tag` | `string` |  |  |

---

## ads_vc_tags_data_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_tags_data_dd` |
| **描述** | 标签数据存储表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 279.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 279.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `tag_name` | `string` |  | 标签名 |
| 3 | `dim1` | `string` |  | 维度1 |
| 4 | `dim2` | `string` |  | 维度2 |
| 5 | `dim3` | `string` |  | 维度3 |
| 6 | `dim4` | `string` |  | 维度4 |
| 7 | `grp` | `string` |  | 维度5 |
| 8 | `value` | `double` |  | 指标 |
| 9 | `dt` | `string` |  |  |
| 10 | `tag` | `string` |  |  |

---

## ads_vc_uc_talk_period_cnt_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_uc_talk_period_cnt_n_d` |
| **描述** | 用户角色聊天统计汇总表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 88.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 88.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `statistical_period` | `string` |  | 统计周期 |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `character_name` | `string` |  | 角色名 |
| 5 | `talk_cnt` | `bigint` |  | 聊天轮次 |
| 6 | `talk_days` | `bigint` |  | 周期内聊天天数 |
| 7 | `last_talk_intervals` | `int` |  | 最后一次聊天时间如今多少天 |
| 8 | `energy_consume` | `int` |  | 能量消耗 |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_uc_talk_top20_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_uc_talk_top20_n_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 6.0G |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `statistical_period` | `string` |  | 统计周期 |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `character_name` | `string` |  | 角色名称 |
| 5 | `total_talks` | `bigint` |  |  |
| 6 | `rk` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ads_vc_uct_persons_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_uct_persons_n_d` |
| **描述** | 用户角色聊天轮次人群包圈定表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 28.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 28.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `sign` | `string` |  | 时间周期标识 |
| 2 | `dimensionality` | `string` |  | 统计维度 |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `character_id` | `bigint` |  | 角色id |
| 5 | `talk_cnt` | `bigint` |  | 首次聊天时间 |
| 6 | `is_merge` | `string` |  |  |
| 7 | `pve_talk_cnt` | `int` |  |  |
| 8 | `vc_talk_cnt` | `int` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ads_vc_user_consume_period_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_user_consume_period_n_d` |
| **描述** | 用户活跃明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.5G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `pay_item_code` | `string` |  | 商品code |
| 3 | `order_cnt` | `int` |  | 订单数 |
| 4 | `amount` | `double` |  | 订单金额 |
| 5 | `statistical_period` | `string` |  | 统计周期 |
| 6 | `dt` | `string` |  |  |

---

## ads_vc_user_expire_assets_push_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_user_expire_assets_push_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | internal |
| **表大小** | 65.3M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `asset` | `string` |  |  |
| 3 | `props` | `string` |  |  |
| 4 | `benefit` | `string` |  |  |
| 5 | `dt` | `string` |  |  |

---

## ads_vc_user_period_data_summary_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_user_period_data_summary_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 479.9M |
| **是否分区表** | 是 |

### 字段详情

共 34 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `is_test_user` | `string` |  |  |
| 3 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 4 | `active_days_1d` | `bigint` |  |  |
| 5 | `order_cnt_1d` | `bigint` |  |  |
| 6 | `amount_1d` | `double` |  |  |
| 7 | `uc_charaters_1d` | `bigint` |  |  |
| 8 | `uc_cnt_1d` | `bigint` |  |  |
| 9 | `active_days_3d` | `bigint` |  |  |
| 10 | `order_cnt_3d` | `bigint` |  |  |
| 11 | `amount_3d` | `double` |  |  |
| 12 | `uc_charaters_3d` | `bigint` |  |  |
| 13 | `uc_cnt_3d` | `bigint` |  |  |
| 14 | `active_days_7d` | `bigint` |  |  |
| 15 | `order_cnt_7d` | `bigint` |  |  |
| 16 | `amount_7d` | `double` |  |  |
| 17 | `uc_charaters_7d` | `bigint` |  |  |
| 18 | `uc_cnt_7d` | `bigint` |  |  |
| 19 | `active_days_15d` | `bigint` |  |  |
| 20 | `order_cnt_15d` | `bigint` |  |  |
| 21 | `amount_15d` | `double` |  |  |
| 22 | `uc_charaters_15d` | `bigint` |  |  |
| 23 | `uc_cnt_15d` | `bigint` |  |  |
| 24 | `active_days_30d` | `bigint` |  |  |
| 25 | `order_cnt_30d` | `bigint` |  |  |
| 26 | `amount_30d` | `double` |  |  |
| 27 | `uc_charaters_30d` | `bigint` |  |  |
| 28 | `uc_cnt_30d` | `bigint` |  |  |
| 29 | `active_days_90d` | `bigint` |  |  |
| 30 | `order_cnt_90d` | `bigint` |  |  |
| 31 | `amount_90d` | `double` |  |  |
| 32 | `uc_charaters_90d` | `bigint` |  |  |
| 33 | `uc_cnt_90d` | `bigint` |  |  |
| 34 | `dt` | `string` |  |  |

---

## ads_vc_user_talk_period_cnt_n_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_user_talk_period_cnt_n_d` |
| **描述** | 新老注册用户聊天统计表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 10.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 10.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `register_date` | `date` |  | 注册日期 |
| 3 | `statistical_period` | `string` |  | 统计周期 |
| 4 | `user_type` | `string` |  | 用户类型 |
| 5 | `talk_cnt` | `int` |  | 聊天轮次 |
| 6 | `dt` | `string` |  |  |

---

## ads_vc_yesterdayregister_todaynotactive_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ads_vc_yesterdayregister_todaynotactive_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 2.7M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## bridge_exp_metric

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `bridge_exp_metric` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `int` |  | 应用id： 1 lofter |
| 2 | `exp_id` | `bigint` |  | 实验id |
| 3 | `metric` | `string` |  | 实验指标 |
| 4 | `dimension` | `string` |  | 实验指标维度 |
| 5 | `dimension_value` | `string` |  | 实验指标维度值 |
| 6 | `dt` | `string` |  |  |

---

## bridge_exp_metric_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `bridge_exp_metric_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6.3M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `int` |  | 应用id： 1 lofter |
| 2 | `exp_id` | `bigint` |  | 实验id |
| 3 | `metric` | `string` |  | 实验指标 |
| 4 | `dimension` | `string` |  | 实验指标维度 |
| 5 | `dimension_value` | `string` |  | 实验指标维度值 |
| 6 | `dt` | `string` |  |  |

---

## dim_user

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dim_user` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 用户id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `create_date` | `string` |  | 创建日期 |
| 4 | `phone` | `string` |  |  |
| 5 | `user_code` | `bigint` |  | lofter账号 |
| 6 | `is_anonymous` | `int` |  | 是否匿名 1匿名 0非匿名 |
| 7 | `dt` | `string` |  |  |

---

## dim_user_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dim_user_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 57.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 57.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 用户id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `create_date` | `string` |  | 创建日期 |
| 4 | `phone` | `string` |  |  |
| 5 | `user_code` | `bigint` |  | lofter账号 |
| 6 | `is_anonymous` | `int` |  | 是否匿名 1匿名 0非匿名 |
| 7 | `dt` | `string` |  |  |

---

## dim_vc_character_info_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dim_vc_character_info_a_d` |
| **描述** | 角色信息明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 1302.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1302.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 35 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `c_user_id` | `bigint` |  | 关联用户账号ID |
| 3 | `creator_uid` | `bigint` |  | 创建者用户id |
| 4 | `character_name` | `string` |  | 角色名称 |
| 5 | `description` | `string` |  | 角色描述 |
| 6 | `character_avatar` | `string` |  | 角色头像 |
| 7 | `character_back_img` | `string` |  | 角色背景图 |
| 8 | `residence` | `string` |  | 居住地 |
| 9 | `birthday` | `string` |  | 生日 |
| 10 | `birthday_year` | `string` |  | 生日-年 |
| 11 | `birthday_month` | `string` |  | 生日-月 |
| 12 | `birthday_day` | `string` |  | 生日-日 |
| 13 | `bot_setting` | `string` |  | 角色人设 |
| 14 | `ext_prompt` | `string` |  | 角色额外的prompt |
| 15 | `front_tags` | `string` |  | 前台类目 |
| 16 | `backend_tags` | `string` |  | 后台类目 |
| 17 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 18 | `type` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 19 | `weights` | `int` |  | 角色权重 |
| 20 | `score` | `int` |  | 后台评分 |
| 21 | `audit_time` | `bigint` |  | 角色审核时间 |
| 22 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 23 | `ext` | `string` |  | 额外配置 |
| 24 | `create_time` | `timestamp` |  | 创建时间 |
| 25 | `update_time` | `timestamp` |  | 更新时间 |
| 26 | `valid_start_time` | `timestamp` |  | 有效开始时间（status=2时有效） |
| 27 | `character_dyn_back_img` | `string` |  | 角色动态背景图 |
| 28 | `audience_type` | `int` |  | 受众类型：1所有人（默认），2成年人，3未成年人，4审核人员 |
| 29 | `public_scope` | `int` |  | 1.公开,2.私密 |
| 30 | `sex` | `int` |  |  |
| 31 | `audit_status` | `int` |  |  |
| 32 | `character_type` | `string` |  |  |
| 33 | `is_valid` | `string` |  |  |
| 34 | `role_id` | `bigint` |  |  |
| 35 | `dt` | `string` |  |  |

---

## dim_vc_discardted_users_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dim_vc_discardted_users_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 20.0K |
| **是否分区表** | 否 |

### 字段详情

共 1 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 主键id |

---

## dim_vc_user_info_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dim_vc_user_info_a_d` |
| **描述** | 用户信息明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 57.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 57.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `user_name` | `string` |  | 昵称 |
| 3 | `sex` | `bigint` |  | 性别 |
| 4 | `out_id` | `string` |  | 外部唯一标识 |
| 5 | `phone` | `string` |  | 手机号 |
| 6 | `status` | `bigint` |  | 状态 |
| 7 | `register_date` | `date` |  | 注册日期 |
| 8 | `register_time` | `timestamp` |  | 注册时间 |
| 9 | `is_new_register` | `string` |  | 是否新注册用户 |
| 10 | `update_time` | `timestamp` |  | 数据更新时间 |
| 11 | `user_code` | `string` |  |  |
| 12 | `channel` | `int` |  | 渠道来源：1-->app,2-->lofter |
| 13 | `register_days` | `int` |  | 注册天数 |
| 14 | `login_type` | `string` |  | 登录类型 |
| 15 | `rest_energy` | `int` |  | 剩余能量 |
| 16 | `is_notified` | `string` |  | 是否开启通知 |
| 17 | `device_os` | `string` |  | 最后记录的设备os |
| 18 | `is_test_user` | `string` |  | 是否测试用户 |
| 19 | `appversion` | `string` |  | app版本 |
| 20 | `first_active_deviceudid` | `string` |  | 首次访问设备id |
| 21 | `last_active_deviceudid` | `string` |  | 最后访问设备id |
| 22 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 23 | `free_rest_energy` | `int` |  |  |
| 24 | `buy_rest_energy` | `int` |  |  |
| 25 | `reward_rest_energy` | `int` |  |  |
| 26 | `expired_energy` | `int` |  | 当日将过期能量 |
| 27 | `dt` | `string` |  |  |

---

## dwd_ab_platform_exp_user_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_ab_platform_exp_user_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 119.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 119.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `appid` | `bigint` |  | 应用id: 1 lofter |
| 3 | `appver` | `string` |  | app 版本号 |
| 4 | `os` | `string` |  | 设备系统 |
| 5 | `deviceid` | `string` |  | 设备号 |
| 6 | `traceid` | `string` |  | 请求追踪id |
| 7 | `sceneid` | `bigint` |  | 场景id |
| 8 | `exp_id` | `bigint` |  | 实验id |
| 9 | `bucket_id` | `bigint` |  | 命中实验分组id |
| 10 | `modify_time` | `bigint` |  | 实验配置最新时间 |
| 11 | `exp_ver` | `int` |  | 实验配置版本号 |
| 12 | `exp_type` | `int` |  | 实验类型(哪个流量域) |
| 13 | `version` | `string` |  | ABTLog 版本号 |
| 14 | `timestamp` | `bigint` |  | 打点时间 |
| 15 | `backend_deviceid` | `string` |  | 后端接口传来的设备ID |
| 16 | `dt` | `string` |  |  |

---

## dwd_ad_growth_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_ad_growth_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 59.7M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `deviceos` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `appchannel` | `string` |  |  |
| 5 | `source` | `string` |  |  |
| 6 | `matchtype` | `string` |  |  |
| 7 | `proxy` | `string` |  |  |
| 8 | `media` | `string` |  |  |
| 9 | `advertiserid` | `string` |  |  |
| 10 | `campaignid` | `string` |  |  |
| 11 | `aid` | `string` |  |  |
| 12 | `cid` | `string` |  |  |
| 13 | `is_ad_attributed` | `int` |  | 是否全局归因到广告（1-成功，0-失败） |
| 14 | `customudid` | `string` |  | 后台设备id |
| 15 | `custom_ouid` | `string` |  | 助推账号 |
| 16 | `photoid` | `string` |  | 视频素材id |
| 17 | `dt` | `string` |  |  |

---

## dwd_ad_growth_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_ad_growth_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.7M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `deviceos` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `appchannel` | `string` |  |  |
| 5 | `source` | `string` |  |  |
| 6 | `matchtype` | `string` |  |  |
| 7 | `proxy` | `string` |  |  |
| 8 | `media` | `string` |  |  |
| 9 | `advertiserid` | `string` |  |  |
| 10 | `campaignid` | `string` |  |  |
| 11 | `aid` | `string` |  |  |
| 12 | `cid` | `string` |  |  |
| 13 | `is_ad_attributed` | `int` |  | 是否全局归因到广告（1-成功，0-失败） |
| 14 | `customudid` | `string` |  | 后台设备id |
| 15 | `custom_ouid` | `string` |  | 助推账号 |
| 16 | `photoid` | `string` |  | 视频素材id |
| 17 | `dt` | `string` |  |  |

---

## dwd_device_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_device_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.1G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `devicemodel` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `firstaccesstime` | `bigint` |  |  |
| 5 | `userids` | `array<bigint>` |  |  |
| 6 | `appchannels` | `array<string>` |  |  |
| 7 | `appversions` | `array<string>` |  |  |
| 8 | `firstaccessip` | `string` |  | 设备当日首次访问IP |
| 9 | `returnoccurtime` | `bigint` |  | 首次当日活跃时间， 过滤昨日延迟发送数据 |
| 10 | `appversionchannel` | `map<string, string>` |  |  |
| 11 | `last_appchannel` | `string` |  | 当天最后渠道 |
| 12 | `last_appversion` | `string` |  | 当天最后版本 |
| 13 | `dt` | `string` |  |  |

---

## dwd_device_all_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_device_all_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 42.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 42.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `devicemodel` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `firstaccesstime` | `bigint` |  |  |
| 5 | `userid` | `bigint` |  |  |
| 6 | `appchannel` | `string` |  |  |
| 7 | `appversion` | `string` |  |  |
| 8 | `customudid` | `string` |  |  |
| 9 | `imei` | `string` |  |  |
| 10 | `idfa` | `string` |  |  |
| 11 | `oaid` | `string` |  |  |
| 12 | `androidid` | `string` |  |  |
| 13 | `finaluserid` | `bigint` |  |  |
| 14 | `new_date` | `string` |  |  |
| 15 | `deviceid` | `bigint` |  | deviceUdid对应数字id |
| 16 | `dt` | `string` |  |  |

---

## dwd_device_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_device_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 138.2M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `devicemodel` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `firstaccesstime` | `bigint` |  |  |
| 5 | `userid` | `bigint` |  |  |
| 6 | `appchannel` | `string` |  |  |
| 7 | `appversion` | `string` |  |  |
| 8 | `customudid` | `string` |  |  |
| 9 | `imei` | `string` |  |  |
| 10 | `idfa` | `string` |  |  |
| 11 | `oaid` | `string` |  |  |
| 12 | `androidid` | `string` |  |  |
| 13 | `finaluserid` | `bigint` |  |  |
| 14 | `dt` | `string` |  |  |

---

## dwd_device_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_device_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 18.5M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `devicemodel` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `firstaccesstime` | `bigint` |  |  |
| 5 | `userids` | `array<bigint>` |  |  |
| 6 | `appchannels` | `array<string>` |  |  |
| 7 | `appversions` | `array<string>` |  |  |
| 8 | `returnoccurtime` | `bigint` |  | 首次当日活跃时间， 过滤昨日延迟发送数据 |
| 9 | `last_appchannel` | `string` |  | 当天最后渠道 |
| 10 | `last_appversion` | `string` |  | 当天最后版本 |
| 11 | `dt` | `string` |  |  |

---

## dwd_pv_simulator_summary_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_pv_simulator_summary_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 688.4K |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `simulator_id` | `string` |  | 模拟器ID |
| 2 | `simulator_category` | `int` |  |  |
| 3 | `simulator_type` | `int` |  |  |
| 4 | `people_nums` | `int` |  |  |
| 5 | `talk_cnt` | `int` |  |  |
| 6 | `dt` | `string` |  |  |

---

## dwd_pv_simulator_talk_cnt_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_pv_simulator_talk_cnt_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 64.7M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `simulator_id` | `string` |  | 模拟器ID |
| 2 | `name` | `string` |  | 名称 |
| 3 | `init_user_identity` | `string` |  |  |
| 4 | `user_id` | `bigint` |  | 用户ID |
| 5 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 6 | `source` | `string` |  | 消息来源，可选值：1-vc,2-lofter |
| 7 | `talk_cnt` | `bigint` |  |  |
| 8 | `dt` | `string` |  |  |

---

## dwd_pv_simulator_talk_info_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_pv_simulator_talk_info_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 6.0G |
| **是否分区表** | 是 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `simulator_id` | `string` |  | 模拟器ID |
| 2 | `name` | `string` |  | 名称 |
| 3 | `init_param` | `string` |  | 初始化内容 |
| 4 | `init_user_identity` | `string` |  |  |
| 5 | `biz_id` | `string` |  | 业务ID，不存在默认为空字符串 |
| 6 | `message_id` | `bigint` |  | 主键ID |
| 7 | `scene` | `string` |  | 玩法场景，每个场景单独定义 |
| 8 | `user_id` | `bigint` |  | 用户ID |
| 9 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 10 | `sender` | `int` |  | 发话类型 1-角色回复 2-用户发话 6-助攻 |
| 11 | `source` | `string` |  | 消息来源，可选值：1-vc,2-lofter |
| 12 | `asset_type` | `int` |  |  |
| 13 | `asset_type_desc` | `string` |  |  |
| 14 | `total_consume` | `bigint` |  |  |
| 15 | `free_nums` | `bigint` |  |  |
| 16 | `buy_nums` | `bigint` |  |  |
| 17 | `reward_nums` | `bigint` |  |  |
| 18 | `lofter_nums` | `bigint` |  |  |
| 19 | `scene_code` | `int` |  |  |
| 20 | `scene_desc` | `string` |  |  |
| 21 | `create_time` | `bigint` |  | 创建时间 |
| 22 | `create_time_r` | `string` |  |  |
| 23 | `update_time` | `bigint` |  | 更新时间 |
| 24 | `update_time_r` | `string` |  |  |
| 25 | `simulator_category` | `int` |  |  |
| 26 | `simulator_type` | `int` |  |  |
| 27 | `dt` | `string` |  |  |

---

## dwd_vc_activity_detail_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_activity_detail_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 62.2K |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `activity_id` | `bigint` |  | 主键id |
| 2 | `activity_name` | `string` |  | 活动名称 |
| 3 | `activity_type` | `int` |  | 活动类型：1签到领取活动，2连续签到奖励活动 |
| 4 | `is_valid` | `string` |  |  |
| 5 | `period` | `string` |  | 活动周期：day1-dayX表示1-x天任务，week表示每自然周任务 |
| 6 | `module_id` | `bigint` |  | 主键id |
| 7 | `module_type` | `int` |  | 模块类型：1每日免费；2每日额外；3补签；4签到奖励 |
| 8 | `module_title` | `string` |  |  |
| 9 | `module_sub_title` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## dwd_vc_app_event_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_app_event_i_d` |
| **描述** | 事件按天划分表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 396.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 396.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `event_id` | `string` |  | 事件id |
| 4 | `occur_time` | `timestamp` |  | 事件发生时间 |
| 5 | `user_code` | `bigint` |  | 用户code |
| 6 | `app_key` | `string` |  | app包标识 |
| 7 | `app_channel` | `string` |  | 包来源 |
| 8 | `app_version` | `string` |  | app版本 |
| 9 | `device_os` | `string` |  | 操作系统 |
| 10 | `device_model` | `string` |  |  |
| 11 | `ip` | `string` |  |  |
| 12 | `params` | `map<string, string>` |  | 附加信息 |
| 13 | `dt` | `string` |  |  |

---

## dwd_vc_asset_consume_info_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_asset_consume_info_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 11.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 账号ID |
| 2 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 3 | `asset_type_desc` | `string` |  |  |
| 4 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 5 | `scene_desc` | `string` |  |  |
| 6 | `total_consume` | `bigint` |  |  |
| 7 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 8 | `free_nums` | `bigint` |  |  |
| 9 | `buy_nums` | `bigint` |  |  |
| 10 | `reward_nums` | `bigint` |  |  |
| 11 | `lofter_nums` | `bigint` |  |  |
| 12 | `dt` | `string` |  |  |

---

## dwd_vc_character_package_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_character_package_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 18.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 18.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `package_id` | `bigint` |  | 角色包id |
| 2 | `character_type` | `string` |  | 角色类型 |
| 3 | `characters` | `array<int>` |  | 角色id集合 |
| 4 | `character_nums` | `bigint` |  | 数量 |
| 5 | `version` | `int` |  | 版本 |
| 6 | `total_talk_cnt` | `bigint` |  |  |
| 7 | `total_peoples` | `bigint` |  |  |
| 8 | `dt` | `string` |  |  |
| 9 | `h` | `string` |  |  |

---

## dwd_vc_character_tags_1h_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_character_tags_1h_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 234.4M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 主键id |
| 2 | `front_tags_list` | `array<string>` |  |  |
| 3 | `backend_tags_list` | `array<string>` |  |  |
| 4 | `category_list` | `array<string>` |  |  |
| 5 | `character_type` | `string` |  |  |
| 6 | `character_name` | `string` |  |  |
| 7 | `total_talk_cnt` | `bigint` |  |  |
| 8 | `total_peoples` | `bigint` |  |  |
| 9 | `pack_id` | `bigint` |  |  |
| 10 | `l2_name_set_str` | `string` |  |  |
| 11 | `dt` | `string` |  |  |
| 12 | `h` | `string` |  |  |

---

## dwd_vc_character_tags_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_character_tags_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.6M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `l1_id` | `bigint` |  | 主键id |
| 2 | `l1_name` | `string` |  | 类别名称字段(例如:标签类型名称、标签名称) |
| 3 | `l1_code` | `int` |  | 类别:1类型、2标签 |
| 4 | `l1_code_type` | `string` |  |  |
| 5 | `l2_id` | `bigint` |  | 主键id |
| 6 | `l2_name` | `string` |  | 类别名称字段(例如:标签类型名称、标签名称) |
| 7 | `l2_code` | `int` |  | 类别:1类型、2标签 |
| 8 | `l2_code_type` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## dwd_vc_character_tags_v2_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_character_tags_v2_dd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | internal |
| **表大小** | 43.6M |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  |  |
| 2 | `l1_name_set_str` | `string` |  |  |
| 3 | `l2_name_set_str` | `string` |  |  |
| 4 | `l1l2json` | `string` |  |  |

---

## dwd_vc_daily_message_users_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_daily_message_users_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 75.1M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `scene` | `string` |  |  |
| 3 | `task_type` | `int` |  | 任务类型：1短信任务（与主任务一致） |
| 4 | `is_anonymous` | `string` |  |  |
| 5 | `trigger_time` | `string` |  |  |
| 6 | `send_time` | `string` |  |  |
| 7 | `source_code` | `string` |  | 溯源码信息 |
| 8 | `dt` | `string` |  |  |

---

## dwd_vc_device_growth_attribution_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_device_growth_attribution_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 101.5M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备Id |
| 2 | `occur_time` | `date` |  | 新增或回流时间 |
| 3 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 4 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 5 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 6 | `deviceos` | `string` |  | 操作系统 |
| 7 | `proxy` | `string` |  | 渠道代理 |
| 8 | `user_id` | `bigint` |  |  |
| 9 | `user_code` | `bigint` |  |  |
| 10 | `appchannel` | `string` |  | 当天最后渠道 |
| 11 | `appversion` | `string` |  | 当天最后版本 |
| 12 | `dt` | `string` |  |  |

---

## dwd_vc_device_user_correlation_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_device_user_correlation_a_d` |
| **描述** | user_id,device_udid,user_code关联表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 24.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 24.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `user_code` | `bigint` |  | lofter站内id |
| 4 | `deviceos` | `string` |  | 操作系统 |
| 5 | `dt` | `string` |  |  |

---

## dwd_vc_free_character_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_free_character_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 411.5K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  |  |
| 3 | `free_type` | `int` |  | 限免类型 1.每日限免 2.新人限免 |
| 4 | `valid_start_time` | `string` |  |  |
| 5 | `valid_end_time` | `string` |  |  |
| 6 | `free_for_role` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## dwd_vc_lofter_real_users_mapping_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_lofter_real_users_mapping_a_d` |
| **描述** | 用户活跃明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 5.0G |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | lofter用户id，vc数仓里的user_code |
| 2 | `vc_user_code` | `bigint` |  | vc_user_account里记录的user_code |
| 3 | `lofter_user_id` | `bigint` |  | lofter用户id |
| 4 | `is_lofter` | `string` |  | 是否lofter用户 |
| 5 | `is_pve` | `string` |  | 是否pve用户 |
| 6 | `phone` | `string` |  | lofter认证手机号 |
| 7 | `dt` | `string` |  |  |

---

## dwd_vc_order_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_order_i_d` |
| **描述** | 订单表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 25.2M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `order_id` | `bigint` |  | 订单id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `item_id` | `bigint` |  | 商品id |
| 4 | `reward_type` | `int` |  | 奖励类型：0未知，1能量，2道具，3权益 |
| 5 | `reward_id` | `bigint` |  | 奖励ID，rewardType为道具，则此id为道具id，为能量，则此id为能量code |
| 6 | `pay_time` | `timestamp` |  | 支付时间 |
| 7 | `order_status` | `int` |  | 订单状态 |
| 8 | `order_time` | `timestamp` |  | 订单创建时间 |
| 9 | `order_date` | `date` |  | 订单创建日期 |
| 10 | `order_price` | `double` |  | 订单金额 |
| 11 | `is_test_user` | `string` |  | 是否测试用户 |
| 12 | `pay_item_code` | `string` |  | 支付商品code |
| 13 | `platform` | `string` |  |  |
| 14 | `category_level1` | `string` |  |  |
| 15 | `product_name` | `string` |  |  |
| 16 | `dt` | `string` |  |  |

---

## dwd_vc_pmessage_info_7d_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_pmessage_info_7d_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.2G |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dt1` | `string` |  |  |
| 2 | `source_code` | `string` |  | 溯源码信息 |
| 3 | `user_id` | `string` |  |  |
| 4 | `h5_source_code` | `string` |  |  |
| 5 | `device_udid` | `string` |  |  |
| 6 | `type` | `string` |  |  |
| 7 | `servise_source_code` | `string` |  |  |
| 8 | `scene` | `string` |  |  |
| 9 | `task_type` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## dwd_vc_special_event_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_special_event_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 628.2M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `event_id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `event_type` | `int` |  | 事件类型 1-次元事件 2-日常事件 3-情绪事件 |
| 5 | `play_type` | `int` |  | 玩法类型 0-不支持玩法 1-任务玩法 2-数值玩法 |
| 6 | `sub_event_type` | `int` |  | 事件子类型，0-默认值无实际含义，1-预埋事件，2-推送事件，3-日常事件 |
| 7 | `title` | `string` |  |  |
| 8 | `sub_type_set` | `array<string>` |  |  |
| 9 | `status` | `int` |  | 状态信息 1-待处理 2-处理中 3-已关闭 4-已完成 5-已失败 -1-已删除 |
| 10 | `create_time` | `timestamp` |  |  |
| 11 | `dt` | `string` |  |  |

---

## dwd_vc_uc_talk_info_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_uc_talk_info_i_d` |
| **描述** | 用户角色聊天明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 85.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 85.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_name` | `string` |  | 角色名称 |
| 4 | `message_id` | `string` |  | 消息id |
| 5 | `message_send_time` | `timestamp` |  | 消息发送时间 |
| 6 | `asset_type` | `bigint` |  | 资产类型： 1能量 |
| 7 | `consume_nums` | `bigint` |  | 支出数量 |
| 8 | `sender` | `bigint` |  | 对话方式：1.回复 2.主动对话 |
| 9 | `content_type` | `bigint` |  | 内容类型：1.文本 2.音频 3.图片 |
| 10 | `type` | `bigint` |  | 聊天类型：1.单聊 2.群聊 |
| 11 | `status` | `bigint` |  | 状态：1.初始 2.已回复 3.名字敏感词 4.ai异常 |
| 12 | `fetter_nums` | `bigint` |  | 羁绊值 |
| 13 | `content` | `string` |  | 聊天内容 |
| 14 | `free_energy` | `bigint` |  | 免费能量 |
| 15 | `buy_energy` | `bigint` |  | 购买能量 |
| 16 | `reward_energy` | `bigint` |  | 奖励能量 |
| 17 | `character_type` | `int` |  |  |
| 18 | `character_status` | `int` |  |  |
| 19 | `lofter_energy` | `int` |  | 扣除lofter能量 |
| 20 | `message_consume_type` | `string` |  | 消息付费类型 |
| 21 | `sender_subtype_code` | `int` |  | sender = 4,5时的子类型 |
| 22 | `sender_subtype` | `string` |  | 子类型描述 |
| 23 | `dt` | `string` |  |  |

---

## dwd_vc_uc_talk_message_energy_info_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_uc_talk_message_energy_info_i_d` |
| **描述** | 聊天能量明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 6.5G |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `asset_type` | `int` |  | 聊天消耗类似，1：能量 |
| 3 | `total_consume` | `bigint` |  | 总能量 |
| 4 | `message_id` | `bigint` |  | 消息id |
| 5 | `free_energy` | `bigint` |  | 免费能量 |
| 6 | `buy_energy` | `bigint` |  | 购买能量 |
| 7 | `reward_energy` | `bigint` |  | 奖励能量 |
| 8 | `lofter_energy` | `int` |  | pve能量 |
| 9 | `dt` | `string` |  |  |

---

## dwd_vc_ucr_order_amount_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_ucr_order_amount_i_d` |
| **描述** | 订单归因到角色表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 16.3M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_name` | `string` |  | 角色名称 |
| 4 | `order_id` | `bigint` |  | 订单id |
| 5 | `order_price` | `double` |  | 订单金额 |
| 6 | `type` | `int` |  |  |
| 7 | `status` | `int` |  |  |
| 8 | `audience_type` | `int` |  |  |
| 9 | `audit_status` | `int` |  |  |
| 10 | `dt` | `string` |  |  |

---

## dwd_vc_unregistered_deviceid_userid_mapping_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_unregistered_deviceid_userid_mapping_dd` |
| **描述** | 未注册的设备id和生成的userid的对应关系 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 7.9G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备号,截止到当天未注册的 |
| 2 | `hash_userid` | `bigint` |  | 由设备id哈希得来 |
| 3 | `dt` | `string` |  |  |

---

## dwd_vc_user_active_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_active_i_d` |
| **描述** | 用户每日活跃明细表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 750.9M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `last_active_time` | `timestamp` |  | 当天最后活跃时间 |
| 4 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 5 | `dt` | `string` |  |  |

---

## dwd_vc_user_activity_schedule_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_activity_schedule_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 727.7M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `activity_id` | `int` |  | from deserializer |
| 2 | `activity_name` | `string` |  |  |
| 3 | `activity_type` | `int` |  |  |
| 4 | `is_valid` | `string` |  |  |
| 5 | `period` | `string` |  |  |
| 6 | `module_id` | `int` |  | from deserializer |
| 7 | `module_type` | `int` |  | 模块类型：1每日免费；2每日额外；3补签；4签到奖励 |
| 8 | `user_id` | `bigint` |  | from deserializer |
| 9 | `desc` | `string` |  |  |
| 10 | `module_title` | `string` |  |  |
| 11 | `module_sub_title` | `string` |  |  |
| 12 | `dt` | `string` |  |  |

---

## dwd_vc_user_asset_income_detail_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_asset_income_detail_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.6G |
| **是否分区表** | 是 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `income_id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `order_id` | `bigint` |  | 订单ID，关联订单表 |
| 4 | `item_id` | `bigint` |  | 商品ID，关联vc_item表 |
| 5 | `item_scene` | `string` |  |  |
| 6 | `income_scene` | `string` |  |  |
| 7 | `income_desc` | `string` |  | from deserializer |
| 8 | `reward_type` | `int` |  | 类型：1资产，2道具，3权益 |
| 9 | `reward_desc` | `string` |  | from deserializer |
| 10 | `reward_id` | `bigint` |  | 收入分类细分 |
| 11 | `reward_sub_desc` | `string` |  |  |
| 12 | `num` | `bigint` |  | 收入数量 |
| 13 | `left_num` | `bigint` |  | 剩余数量：剩余可用数量 |
| 14 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 15 | `possession_id` | `bigint` |  | 持有财产id（若reward_type为1则为能量记录表id；reward_type为2则为 vc_user_props 的id；reward_type为3则为用户权益表的id） |
| 16 | `income_record_id` | `bigint` |  | 收入记录表id，有些类型可能不存收入记录表 |
| 17 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 18 | `valid_start_time_r` | `string` |  |  |
| 19 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 20 | `valid_end_time_r` | `string` |  |  |
| 21 | `create_time` | `bigint` |  | 数据创建时间 |
| 22 | `create_time_r` | `string` |  |  |
| 23 | `update_time` | `bigint` |  | 数据更新时间 |
| 24 | `update_time_r` | `string` |  |  |
| 25 | `dt` | `string` |  |  |

---

## dwd_vc_user_consecutive_active_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_consecutive_active_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.2G |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `max_start_date` | `string` |  |  |
| 3 | `max_end_date` | `string` |  |  |
| 4 | `max_consecutive_days` | `bigint` |  |  |
| 5 | `last_start_date` | `string` |  |  |
| 6 | `last_end_date` | `string` |  |  |
| 7 | `last_consecutive_days` | `int` |  |  |
| 8 | `now_consecutive` | `int` |  |  |
| 9 | `dt` | `string` |  |  |

---

## dwd_vc_user_income_record_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_income_record_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.1G |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `reward_type` | `int` |  | 奖励类型：1资产，2道具，3权益 |
| 4 | `reward_id` | `bigint` |  | 收入分类细分 |
| 5 | `scene` | `int` |  | 获取来源：1免费领取；2购买；3奖励发放 |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `income_time` | `string` |  |  |
| 8 | `amount` | `bigint` |  | 收入数量 |
| 9 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 10 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 11 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 12 | `income_detail` | `string` |  | 收入详情 |
| 13 | `type` | `string` |  |  |
| 14 | `date` | `string` |  |  |
| 15 | `activity_id` | `string` |  |  |
| 16 | `module_id` | `string` |  |  |
| 17 | `activity_name` | `string` |  | 活动名称 |
| 18 | `title` | `string` |  | 模块标题 |
| 19 | `sub_title` | `string` |  | 模块副标题 |
| 20 | `pay_item_code` | `string` |  | 支付商品code |
| 21 | `asset_type_desc` | `string` |  | 资产描述 |
| 22 | `dt` | `string` |  |  |

---

## dwd_vc_user_simulator_asset_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_simulator_asset_consume_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9.8G |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `amount` | `bigint` |  | 支出数量 |
| 5 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `consume_status` | `int` |  | 消耗状态： 1=预扣除；2=扣除完成；3=回滚完成 |
| 8 | `detail` | `string` |  | 支出详情 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |
| 11 | `ext_amount` | `bigint` |  | 从lofter消耗的能量 |
| 12 | `partition_date` | `bigint` |  | 分区 |
| 13 | `ext` | `string` |  | 额外参数 |
| 14 | `dt` | `string` |  |  |

---

## dwd_vc_user_simulator_chats_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dwd_vc_user_simulator_chats_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 4.1G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `message_id` | `bigint` |  | 主键ID |
| 2 | `scene` | `string` |  | 玩法场景，每个场景单独定义 |
| 3 | `user_id` | `bigint` |  | 用户ID |
| 4 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 5 | `biz_id` | `string` |  | 业务ID，不存在默认为空字符串 |
| 6 | `sender` | `int` |  | 发话类型 1-角色回复 2-用户发话 6-助攻 |
| 7 | `source` | `string` |  | 消息来源，可选值：1-vc,2-lofter |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `create_date` | `string` |  | 创建日期 |
| 11 | `update_date` | `string` |  | 更新日期 |
| 12 | `simulator_id` | `string` |  | 模拟器ID |
| 13 | `init_param` | `string` |  | 初始化内容 |
| 14 | `init_user_identity` | `string` |  | 初始化用户身份 |
| 15 | `simulator_category` | `int` |  | 模拟器分类：1-官方模拟器 |
| 16 | `simulator_type` | `int` |  | 模拟器类型：1-单角色聊天数值报告玩法 |
| 17 | `simulator_name` | `string` |  | 模拟器名称 |
| 18 | `dt` | `string` |  |  |

---

## dws_ab_platform_experiment_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_ab_platform_experiment_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 228.8M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_date` | `string` |  | 实验日期 |
| 2 | `appid` | `bigint` |  | 应用id |
| 3 | `sceneid` | `bigint` |  | 场景id |
| 4 | `exp_id` | `bigint` |  | 实验id |
| 5 | `bucket_id` | `bigint` |  | 实验分组 |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `dimension` | `string` |  | 维度 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `metric_value` | `double` |  | 指标值 |
| 10 | `metricdetailid` | `bigint` |  | 实验指标id |
| 11 | `metricdimid` | `bigint` |  | 实验指标维度id |
| 12 | `dt` | `string` |  |  |

---

## dws_ab_platform_user_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_ab_platform_user_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 837.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 837.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `exp_date` | `string` |  | 实验日期 |
| 3 | `appid` | `bigint` |  | 应用id |
| 4 | `sceneid` | `bigint` |  | 场景id |
| 5 | `exp_id` | `bigint` |  | 实验id |
| 6 | `bucket_id` | `bigint` |  | 实验分组 |
| 7 | `metric` | `string` |  | 指标名 |
| 8 | `dimension` | `string` |  | 维度 |
| 9 | `dimension_value` | `string` |  | 维度值 |
| 10 | `metric_value` | `double` |  | 指标值 |
| 11 | `dt` | `string` |  |  |

---

## dws_ab_platform_vc_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_ab_platform_vc_metric_di` |
| **描述** | ab实验vc相关原子指标表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 38.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 38.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 指标的基准日期 |
| 2 | `period` | `int` |  | 相对基准日期的间隔 |
| 3 | `user_id` | `bigint` |  | 用户ID |
| 4 | `chat_rounds` | `bigint` |  | 聊天轮数 |
| 5 | `chat_30_uv` | `int` |  | 聊天超过30轮uv |
| 6 | `chat_uv` | `int` |  | 聊天超过0轮uv |
| 7 | `pay_uv` | `int` |  | 付费uv |
| 8 | `pay_money` | `double` |  | 付费金额 |
| 9 | `gmv` | `double` |  | GMV |
| 10 | `roles_cnt` | `bigint` |  | 聊天角色数 |
| 11 | `retain_uv` | `int` |  | 留存uv |
| 12 | `visit_chat_uv` | `int` |  | 访问聊天界面uv |
| 13 | `visit_uv` | `int` |  | 登录uv |
| 14 | `retain_visit_uv` | `int` |  | 留存访问UV |
| 15 | `chat_10_uv` | `int` |  | 超过10轮的聊天人数 |
| 16 | `exp_id` | `bigint` |  | 实验ID |
| 17 | `bucket_id` | `bigint` |  | 分桶ID |
| 18 | `order_cnt` | `bigint` |  | 订单数 |
| 19 | `new_gift_package_pay_uv` | `bigint` |  | 新人礼包付费人数 |
| 20 | `new_gift_package_amount` | `double` |  | 新人礼包付费金额 |
| 21 | `new_gift_package_order_cnt` | `bigint` |  | 新人礼包订单数 |
| 22 | `old_gift_package_pay_uv` | `bigint` |  | 老用户礼包付费人数 |
| 23 | `old_gift_package_amount` | `double` |  | 老用户礼包付费金额 |
| 24 | `old_gift_package_order_cnt` | `bigint` |  | 老用户礼包订单数 |
| 25 | `dt` | `string` |  |  |

---

## dws_ab_platform_vc_metric_expand_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_ab_platform_vc_metric_expand_di` |
| **描述** | ab实验平台vc的指标汇总展开表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 526.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 526.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 基准日期 |
| 2 | `type` | `string` |  | 指标名前缀,vc |
| 3 | `period` | `int` |  | 距基准日期间隔,向后数 |
| 4 | `user_id` | `bigint` |  | 用户ID |
| 5 | `metric` | `string` |  | 指标名 |
| 6 | `metric_value` | `double` |  | 指标值 |
| 7 | `exp_id` | `bigint` |  | 实验ID |
| 8 | `bucket_id` | `bigint` |  | 分桶ID |
| 9 | `dt` | `string` |  |  |

---

## dws_par_device_session_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_par_device_session_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 581.4M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `appversion` | `string` |  |  |
| 3 | `appchannel` | `string` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `sessioncount` | `bigint` |  |  |
| 6 | `sessiontime` | `bigint` |  | ms |
| 7 | `dt` | `string` |  |  |

---

## dws_rec_character_tags_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_rec_character_tags_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 801.5M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  |  |
| 2 | `tags_relationship` | `string` |  |  |
| 3 | `tags_identity` | `string` |  |  |
| 4 | `role_art_style` | `string` |  |  |
| 5 | `tags_ip` | `string` |  |  |
| 6 | `tags_category` | `string` |  |  |
| 7 | `tags_settings` | `string` |  |  |
| 8 | `dt` | `string` |  |  |

---

## dws_rec_pool_l0

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_rec_pool_l0` |
| **描述** | 无描述 |
| **Owner** | bdms_hzzhuyangping |
| **表类型** | internal |
| **表大小** | 1.4M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `item_id` | `string` |  |  |
| 2 | `item_type` | `string` |  |  |
| 3 | `day` | `string` |  |  |

---

## dws_rec_pool_ugc_l0

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_rec_pool_ugc_l0` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 351.2K |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `item_id` | `string` |  |  |
| 2 | `item_type` | `string` |  |  |
| 3 | `day` | `string` |  |  |

---

## dws_rec_role_features

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_rec_role_features` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 393.1M |
| **是否分区表** | 是 |

### 字段详情

共 24 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `item_id` | `string` |  |  |
| 2 | `item_type` | `string` |  |  |
| 3 | `user_id` | `bigint` |  |  |
| 4 | `character_name` | `string` |  |  |
| 5 | `front_tags` | `string` |  |  |
| 6 | `type` | `int` |  |  |
| 7 | `weights` | `bigint` |  |  |
| 8 | `audience_type` | `int` |  |  |
| 9 | `create_time` | `bigint` |  |  |
| 10 | `valid_start_time` | `bigint` |  |  |
| 11 | `role_update_time` | `bigint` |  |  |
| 12 | `gmv` | `float` |  |  |
| 13 | `hot` | `float` |  |  |
| 14 | `worldview` | `string` |  |  |
| 15 | `tags_personality` | `string` |  |  |
| 16 | `tags_xp` | `string` |  |  |
| 17 | `paid` | `float` |  |  |
| 18 | `tags_relationship` | `string` |  |  |
| 19 | `tags_identity` | `string` |  |  |
| 20 | `role_art_style` | `string` |  |  |
| 21 | `status` | `int` |  |  |
| 22 | `role_gender` | `string` |  |  |
| 23 | `tag_v2` | `string` |  |  |
| 24 | `day` | `string` |  |  |

---

## dws_rec_user_features_syn

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_rec_user_features_syn` |
| **描述** | 无描述 |
| **Owner** | bdms_hzzhuyangping |
| **表类型** | internal |
| **表大小** | 558.7M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `bottom_characters` | `string` |  | 分发策略中需要沉底的角色，用户近7日聊过30轮以上的角色 |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_art_style_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_art_style_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.lifeng08 |
| **表类型** | internal |
| **表大小** | 103.1M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_art_style` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_category_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_category_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.lifeng08 |
| **表类型** | internal |
| **表大小** | 259.2M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_category` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_identity_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_identity_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.6G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_identity` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_ip_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_ip_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.lifeng08 |
| **表类型** | internal |
| **表大小** | 79.7M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_ip` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_personality_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_personality_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzzhuyangping |
| **表类型** | internal |
| **表大小** | 2.4G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_personality` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_relationship_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_relationship_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.9G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_relationship` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_settings_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_settings_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.lifeng08 |
| **表类型** | internal |
| **表大小** | 1.8G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_settings` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_tags_xp_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_tags_xp_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzzhuyangping |
| **表类型** | internal |
| **表大小** | 2.2G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_tags_xp` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_prefer_worldviews_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_prefer_worldviews_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzzhuyangping |
| **表类型** | internal |
| **表大小** | 420.5M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `prefer_worldviews` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_show_3d_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_show_3d_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 11.6M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `is_show_3d` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_user_profile_user_talk_cnt_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_user_profile_user_talk_cnt_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzzhuyangping |
| **表类型** | internal |
| **表大小** | 165.4M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `character_id` | `string` |  |  |
| 3 | `character_name` | `string` |  |  |
| 4 | `talkcnt` | `float` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dws_vc_character_behavior_info_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_character_behavior_info_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 4.1G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名称 |
| 3 | `creator_uid` | `bigint` |  | 创建者用户id |
| 4 | `birthday` | `string` |  | 生日 |
| 5 | `type` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 6 | `weights` | `int` |  | 角色权重 |
| 7 | `score` | `int` |  | 后台评分 |
| 8 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 9 | `audience_type` | `int` |  | 受众类型：1所有人（默认），2成年人，3未成年人，4审核人员 |
| 10 | `valid_start_time` | `timestamp` |  | 有效开始时间（status=2时有效） |
| 11 | `free_for_role` | `string` |  |  |
| 12 | `character_type` | `string` |  |  |
| 13 | `is_valid` | `string` |  |  |
| 14 | `free_type` | `int` |  |  |
| 15 | `public_scope` | `int` |  |  |
| 16 | `c_user_id` | `bigint` |  |  |
| 17 | `role_id` | `bigint` |  |  |
| 18 | `dt` | `string` |  |  |

---

## dws_vc_character_behavior_info_view

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_character_behavior_info_view` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名称 |
| 3 | `creator_uid` | `bigint` |  | 创建者用户id |
| 4 | `birthday` | `string` |  | 生日 |
| 5 | `type` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 6 | `weights` | `int` |  | 角色权重 |
| 7 | `score` | `int` |  | 后台评分 |
| 8 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 9 | `audience_type` | `int` |  | 受众类型：1所有人（默认），2成年人，3未成年人，4审核人员 |
| 10 | `valid_start_time` | `timestamp` |  | 有效开始时间（status=2时有效） |
| 11 | `free_for_role` | `string` |  |  |
| 12 | `character_type` | `string` |  |  |
| 13 | `is_valid` | `string` |  |  |
| 14 | `free_type` | `int` |  |  |
| 15 | `public_scope` | `int` |  |  |
| 16 | `c_user_id` | `bigint` |  |  |
| 17 | `role_id` | `bigint` |  |  |
| 18 | `dt` | `string` |  |  |

---

## dws_vc_character_energy_consume_top100_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_character_energy_consume_top100_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.8M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名称 |
| 3 | `character_type` | `string` |  |  |
| 4 | `energy_consume` | `bigint` |  |  |
| 5 | `rk` | `int` |  |  |
| 6 | `total_talk_cnt` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## dws_vc_character_tabulate_data_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_character_tabulate_data_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 77.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 77.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名称 |
| 3 | `character_type` | `string` |  |  |
| 4 | `total_talk_cnt` | `bigint` |  |  |
| 5 | `total_peoples` | `bigint` |  |  |
| 6 | `status` | `int` |  |  |
| 7 | `audience_type` | `int` |  |  |
| 8 | `role_id` | `bigint` |  |  |
| 9 | `pve_total_talk_cnt` | `bigint` |  |  |
| 10 | `public_scope` | `int` |  |  |
| 11 | `dt` | `string` |  |  |

---

## dws_vc_character_tabulate_data_view

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_character_tabulate_data_view` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名称 |
| 3 | `character_type` | `string` |  |  |
| 4 | `total_talk_cnt` | `bigint` |  |  |
| 5 | `total_peoples` | `bigint` |  |  |
| 6 | `status` | `int` |  |  |
| 7 | `audience_type` | `int` |  |  |
| 8 | `role_id` | `bigint` |  |  |
| 9 | `pve_total_talk_cnt` | `bigint` |  |  |
| 10 | `public_scope` | `int` |  |  |
| 11 | `dt` | `string` |  |  |

---

## dws_vc_character_tc_info_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_character_tc_info_i_d` |
| **描述** | 角色维度汇总表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 48.6M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `character_id` | `bigint` |  | 角色id |
| 2 | `character_name` | `string` |  | 角色名 |
| 3 | `talk_cnt` | `int` |  | 聊天总轮数 |
| 4 | `talk_users` | `int` |  | 聊天总人数 |
| 5 | `energy_consume` | `int` |  | 能量消耗 |
| 6 | `consume_users` | `int` |  | 为其充值的人数 |
| 7 | `order_cnt` | `int` |  | 充值的订单数 |
| 8 | `amount` | `double` |  | 充值总金额 |
| 9 | `dt` | `string` |  |  |

---

## dws_vc_daily_hot_tags_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_daily_hot_tags_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.3M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  |  |
| 2 | `nums` | `bigint` |  |  |
| 3 | `rk` | `int` |  |  |
| 4 | `dt` | `string` |  |  |

---

## dws_vc_daily_hot_words_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_daily_hot_words_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 1.4M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `word` | `string` |  |  |
| 2 | `nums` | `bigint` |  |  |
| 3 | `rk` | `int` |  |  |
| 4 | `dt` | `string` |  |  |

---

## dws_vc_device_active_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_device_active_a_d` |
| **描述** | 设备活跃累计表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 24.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 24.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备id |
| 2 | `device_os` | `string` |  | 系统 |
| 3 | `userids` | `array<bigint>` |  | 用户id组 |
| 4 | `first_active_time` | `timestamp` |  | 首次活跃时间 |
| 5 | `first_active_date` | `date` |  | 首次活跃日期 |
| 6 | `last_active_date` | `date` |  | 最后一次活跃日期 |
| 7 | `active_days_1d` | `bigint` |  | 今一日活跃次数 |
| 8 | `active_days_7d` | `bigint` |  | 今七日活跃次数 |
| 9 | `active_days_15d` | `bigint` |  | 今十五日活跃次数 |
| 10 | `active_days_30d` | `bigint` |  | 今三十日活跃次数 |
| 11 | `dt` | `string` |  |  |

---

## dws_vc_device_growth_dau_stratify_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_device_growth_dau_stratify_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 711.4M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备id |
| 2 | `userids` | `array<bigint>` |  | 用户id组 |
| 3 | `device_type` | `string` |  | 设备类型 |
| 4 | `origin_type` | `string` |  | 来源 |
| 5 | `active_days_30d` | `bigint` |  | 过去30天活跃天数 |
| 6 | `origin_channel` | `string` |  | 渠道来源 |
| 7 | `dt` | `string` |  |  |

---

## dws_vc_feed_v2_part1_rank_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_feed_v2_part1_rank_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 43.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 43.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `package_id` | `bigint` |  | 角色包id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `talk_cnt` | `int` |  | 聊天轮次 |
| 4 | `rk` | `int` |  |  |
| 5 | `version` | `int` |  |  |
| 6 | `dt` | `string` |  |  |
| 7 | `h` | `string` |  |  |

---

## dws_vc_feed_v2_part2_rank_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_feed_v2_part2_rank_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 40.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 40.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `package_id` | `bigint` |  | 角色包id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `talk_cnt` | `int` |  | 聊天轮次 |
| 4 | `rk` | `int` |  |  |
| 5 | `version` | `int` |  |  |
| 6 | `dt` | `string` |  |  |
| 7 | `h` | `string` |  |  |

---

## dws_vc_feed_v2_part3_rank_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_feed_v2_part3_rank_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 39.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 39.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `package_id` | `bigint` |  | 角色包id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `talk_cnt` | `int` |  | 聊天轮次 |
| 4 | `rk` | `int` |  |  |
| 5 | `version` | `int` |  |  |
| 6 | `dt` | `string` |  |  |
| 7 | `h` | `string` |  |  |

---

## dws_vc_feed_v2_part_all_rank_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_feed_v2_part_all_rank_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 220.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 220.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `package_id` | `bigint` |  |  |
| 2 | `rk` | `string` |  |  |
| 3 | `version` | `int` |  |  |
| 4 | `characters` | `array<string>` |  |  |
| 5 | `nums` | `bigint` |  |  |
| 6 | `characters_data` | `array<row<string,array<string>>('source','characterids')>` |  |  |
| 7 | `dt` | `string` |  |  |
| 8 | `h` | `string` |  |  |

---

## dws_vc_uc_daily_action_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_uc_daily_action_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 168.9M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `gmv` | `double` |  |  |
| 4 | `order_cnt` | `bigint` |  |  |
| 5 | `energy_consume` | `bigint` |  |  |
| 6 | `talk_cnt` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## dws_vc_uc_talk_day_cnt_single_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_uc_talk_day_cnt_single_i_d` |
| **描述** | 用户角色聊天信息，按天汇总表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 542.9M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_name` | `string` |  | 角色名称 |
| 4 | `fetter_nums` | `int` |  | 羁绊数值 |
| 5 | `talk_cnt` | `int` |  | 聊天轮次总数 |
| 6 | `energy_consume` | `bigint` |  | 聊天消耗的能量 |
| 7 | `message_cnt` | `int` |  | 消息数量 |
| 8 | `mmt_cnt` | `int` |  | 摸摸头数量 |
| 9 | `character_type` | `string` |  |  |
| 10 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 11 | `free_energy` | `bigint` |  |  |
| 12 | `buy_energy` | `bigint` |  |  |
| 13 | `reward_energy` | `bigint` |  |  |
| 14 | `message_consume` | `int` |  |  |
| 15 | `interaction_consume` | `int` |  |  |
| 16 | `free_message_cnt` | `int` |  |  |
| 17 | `free_interaction_cnt` | `int` |  |  |
| 18 | `heart_cnt` | `int` |  | 心跳 |
| 19 | `dt` | `string` |  |  |

---

## dws_vc_uc_talk_summary_single_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_uc_talk_summary_single_a_d` |
| **描述** | 用户角色聊天信息汇总 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 50.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 50.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `character_name` | `string` |  | 角色名字 |
| 4 | `fetter_nums` | `int` |  | 羁绊值 |
| 5 | `first_talk_date` | `date` |  | 首次聊天 |
| 6 | `last_talk_date` | `date` |  | 末次聊天 |
| 7 | `talk_cnt` | `int` |  | 聊天总轮数 |
| 8 | `energy_consume` | `int` |  | 消耗能量总数 |
| 9 | `first_talk_intervals` | `int` |  | 首次聊天距今时间间隔 |
| 10 | `last_talk_intervals` | `int` |  | 末次聊天距今时间间隔 |
| 11 | `character_type` | `string` |  |  |
| 12 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 13 | `dt` | `string` |  |  |

---

## dws_vc_user_active_retain_point_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_active_retain_point_i_d` |
| **描述** | 用户活跃留存表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 692.6M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_udid` | `string` |  | 设备id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `last1day` | `string` |  | 次日留存 |
| 4 | `last3day` | `string` |  | 近三日留存 |
| 5 | `last7day` | `string` |  | 近七日留存 |
| 6 | `last30day` | `string` |  | 近30日留存 |
| 7 | `dt` | `string` |  |  |

---

## dws_vc_user_behavior_info_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_behavior_info_a_d` |
| **描述** | 用户消费汇总表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 4.9G |
| **是否分区表** | 是 |

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `sex` | `bigint` |  | 性别 |
| 3 | `phone` | `string` |  | 手机号 |
| 4 | `register_date` | `date` |  | 注册日期 |
| 5 | `first_order_date` | `date` |  | 首笔订单日期 |
| 6 | `last_order_date` | `date` |  | 末笔订单日期 |
| 7 | `purchase_intervals` | `int` |  | 最后一笔订单距今时间差（day） |
| 8 | `total_orders` | `int` |  | 用户总订单数 |
| 9 | `amounts` | `double` |  | 用户总订单金额 |
| 10 | `last_active_date` | `date` |  | 最后活跃日期 |
| 11 | `active_intervals` | `int` |  | 最后活跃距今时间差（day） |
| 12 | `first_talk_date` | `date` |  | 首次聊天日期 |
| 13 | `last_talk_date` | `date` |  | 末次聊天日期 |
| 14 | `talk_intervals` | `int` |  | 最后一次聊天距今时间差（day） |
| 15 | `register_days` | `int` |  | 注册天数 |
| 16 | `login_type` | `string` |  | 登录类型 |
| 17 | `total_talks` | `int` |  | 聊天总数 |
| 18 | `rest_energy` | `int` |  | 剩余能量 |
| 19 | `origin_type` | `string` |  | 来源类型 |
| 20 | `is_notified` | `string` |  | 是否开启通知 |
| 21 | `device_os` | `string` |  | 最后记录的设备os |
| 22 | `appversion` | `string` |  |  |
| 23 | `is_test_user` | `string` |  |  |
| 24 | `user_code` | `string` |  |  |
| 25 | `first_active_deviceudid` | `string` |  | 首次访问设备id |
| 26 | `last_active_deviceudid` | `string` |  | 最后访问设备id |
| 27 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 28 | `free_rest_energy` | `int` |  |  |
| 29 | `buy_rest_energy` | `int` |  |  |
| 30 | `reward_rest_energy` | `int` |  |  |
| 31 | `max_cst_start_date` | `date` |  | 最大连续活跃开始日期 |
| 32 | `max_cst_end_date` | `date` |  | 最大连续活跃结束日期 |
| 33 | `max_consecutive_days` | `int` |  | 最大连续活跃天数 |
| 34 | `last_cst_start_date` | `date` |  | 最近连续活跃开始日期 |
| 35 | `last_cst_end_date` | `date` |  | 最近连续活跃结束日期 |
| 36 | `last_consecutive_days` | `int` |  | 最近连续活跃天数 |
| 37 | `is_lofter` | `string` |  |  |
| 38 | `is_pve` | `string` |  |  |
| 39 | `expired_energy` | `int` |  |  |
| 40 | `dt` | `string` |  |  |

---

## dws_vc_user_behavior_info_view

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_behavior_info_view` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户id |
| 2 | `sex` | `bigint` |  | 性别 |
| 3 | `phone` | `string` |  | 手机号 |
| 4 | `register_date` | `date` |  | 注册日期 |
| 5 | `first_order_date` | `date` |  | 首笔订单日期 |
| 6 | `last_order_date` | `date` |  | 末笔订单日期 |
| 7 | `purchase_intervals` | `int` |  | 最后一笔订单距今时间差（day） |
| 8 | `total_orders` | `int` |  | 用户总订单数 |
| 9 | `amounts` | `double` |  | 用户总订单金额 |
| 10 | `last_active_date` | `date` |  | 最后活跃日期 |
| 11 | `active_intervals` | `int` |  | 最后活跃距今时间差（day） |
| 12 | `first_talk_date` | `date` |  | 首次聊天日期 |
| 13 | `last_talk_date` | `date` |  | 末次聊天日期 |
| 14 | `talk_intervals` | `int` |  | 最后一次聊天距今时间差（day） |
| 15 | `register_days` | `int` |  | 注册天数 |
| 16 | `login_type` | `string` |  | 登录类型 |
| 17 | `total_talks` | `int` |  | 聊天总数 |
| 18 | `rest_energy` | `int` |  | 剩余能量 |
| 19 | `origin_type` | `string` |  | 来源类型 |
| 20 | `is_notified` | `string` |  | 是否开启通知 |
| 21 | `device_os` | `string` |  | 最后记录的设备os |
| 22 | `appversion` | `string` |  |  |
| 23 | `is_test_user` | `string` |  |  |
| 24 | `user_code` | `string` |  |  |
| 25 | `first_active_deviceudid` | `string` |  | 首次访问设备id |
| 26 | `last_active_deviceudid` | `string` |  | 最后访问设备id |
| 27 | `is_anonymity` | `string` |  | 是否匿名用户 |
| 28 | `free_rest_energy` | `int` |  |  |
| 29 | `buy_rest_energy` | `int` |  |  |
| 30 | `reward_rest_energy` | `int` |  |  |
| 31 | `max_cst_start_date` | `date` |  | 最大连续活跃开始日期 |
| 32 | `max_cst_end_date` | `date` |  | 最大连续活跃结束日期 |
| 33 | `max_consecutive_days` | `int` |  | 最大连续活跃天数 |
| 34 | `last_cst_start_date` | `date` |  | 最近连续活跃开始日期 |
| 35 | `last_cst_end_date` | `date` |  | 最近连续活跃结束日期 |
| 36 | `last_consecutive_days` | `int` |  | 最近连续活跃天数 |
| 37 | `is_lofter` | `string` |  |  |
| 38 | `is_pve` | `string` |  |  |
| 39 | `expired_energy` | `int` |  |  |
| 40 | `dt` | `string` |  |  |

---

## dws_vc_user_simulator_chats_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_simulator_chats_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 36.7M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `simulator_id` | `string` |  | 模拟器ID |
| 3 | `simulator_def` | `string` |  | 模拟器定义：app_simulator,lofter_simulator,theater_simulator |
| 4 | `total_chats` | `bigint` |  | 聊天轮次 |
| 5 | `dt` | `string` |  |  |

---

## dws_vc_user_simulator_energy_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_simulator_energy_consume_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9.3M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `simulator_id` | `string` |  | 模拟器Id |
| 3 | `simulator_def` | `string` |  | 模拟器定义：vc,lofter,theater |
| 4 | `total_consume` | `bigint` |  | 总消耗能量 |
| 5 | `dt` | `string` |  |  |

---

## dws_vc_user_tc_info_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_tc_info_i_d` |
| **描述** | 用户维度汇总表 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 148.4M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 角色id |
| 2 | `talk_cnt` | `int` |  | 聊天总轮数 |
| 3 | `talk_characters_nums` | `int` |  | 聊天总人数 |
| 4 | `energy_consume` | `int` |  | 能量消耗 |
| 5 | `consume_characters` | `int` |  | 为其充值的人数 |
| 6 | `order_cnt` | `int` |  | 充值的订单数 |
| 7 | `amount` | `double` |  | 充值总金额 |
| 8 | `talk_characters` | `array<int>` |  |  |
| 9 | `dt` | `string` |  |  |

---

## dws_vc_user_ugc_ogc_talk_cnt_30d_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_ugc_ogc_talk_cnt_30d_di` |
| **描述** | 用户近30天聊天轮数汇总日报表 |
| **Owner** | bdms_huanglvdian |
| **表类型** | internal |
| **表大小** | 329.0M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户唯一标识ID |
| 2 | `total_talk_cnt_30d` | `bigint` |  | 滚动30天（含当天）的总聊天轮数 |
| 3 | `ugc_talk_cnt_30d` | `bigint` |  | 滚动30天（含当天）与UGC角色（用户创建）的聊天轮数 |
| 4 | `ogc_talk_cnt_30d` | `bigint` |  | 滚动30天（含当天）与OGC角色（官方创建）的聊天轮数 |
| 5 | `dt` | `string` |  | 数据计算日期, 格式: yyyy-MM-dd |

---

## dws_vc_user_ugc_type_talk_cnt_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `dws_vc_user_ugc_type_talk_cnt_di` |
| **描述** | 每日不同人群聊天类型轮数统计中间表 |
| **Owner** | bdms_huanglvdian |
| **表类型** | internal |
| **表大小** | 205.5M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `bigint` |  | 用户ID |
| 2 | `chat_type` | `string` |  | 分人群聊天类型, 例如: 原创（所有角色）, 原创（公开角色） |
| 3 | `talk_cnt` | `bigint` |  | 该用户在该聊天类型下的总聊天轮数 |
| 4 | `dt` | `string` |  | 日期分区, 格式: yyyy-MM-dd |

---

## ods_binlog_vc_character_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_character_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 1.9G |
| **是否分区表** | 是 |

### 字段详情

共 43 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 关联用户账号ID |
| 3 | `creator_uid` | `bigint` |  | 创建者用户id |
| 4 | `character_name` | `string` |  | 角色名称 |
| 5 | `description` | `string` |  | 角色描述 |
| 6 | `character_avatar` | `string` |  | 角色头像 |
| 7 | `character_back_img` | `string` |  | 角色背景图 |
| 8 | `residence` | `string` |  | 居住地 |
| 9 | `birthday` | `string` |  | 生日 |
| 10 | `bot_setting` | `string` |  |  |
| 11 | `ext_prompt` | `string` |  | 角色额外的prompt |
| 12 | `front_tags` | `string` |  | 前台类目 |
| 13 | `backend_tags` | `string` |  | 后台类目 |
| 14 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 15 | `type` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 16 | `weights` | `bigint` |  | 角色权重 |
| 17 | `score` | `bigint` |  | 后台评分 |
| 18 | `audit_time` | `bigint` |  | 角色审核时间 |
| 19 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 20 | `ext` | `string` |  | 额外配置 |
| 21 | `create_time` | `bigint` |  | 创建时间 |
| 22 | `update_time` | `bigint` |  | 更新时间 |
| 23 | `valid_start_time` | `bigint` |  | 有效开始时间（status=2时有效） |
| 24 | `character_dyn_back_img` | `string` |  | 角色动态背景图 |
| 25 | `audience_type` | `int` |  | 受众类型：1所有人（默认），2成年人，3未成年人，4审核人员 |
| 26 | `about_he` | `string` |  | 关于ta |
| 27 | `dyn_back_first_frame_img` | `string` |  | 动态背景图第一帧图片 |
| 28 | `drafts_info` | `string` |  | 草稿箱 |
| 29 | `public_scope` | `int` |  | 公开状态 0 初始值 1:公开 2:私密 |
| 30 | `sex` | `int` |  | 角色性别 0:未设置 1:男 2:女 3:无性别 |
| 31 | `audit_status` | `int` |  | UGC审核结果 0:初始值 1:审核成功 2:审核中 3.审核失败 |
| 32 | `stage_settings` | `string` |  | 阶段人设 |
| 33 | `voice_pack_id` | `bigint` |  | 音色包id |
| 34 | `lofter_id` | `bigint` |  | 站内角色id |
| 35 | `category` | `string` |  | 品类名称 |
| 36 | `detail_setting` | `string` |  | 角色拆解详细人设 |
| 37 | `model_item_id` | `bigint` |  | 默认模式模型id |
| 38 | `pack_id` | `bigint` |  | 角色包id |
| 39 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 40 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 41 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 42 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 43 | `dt` | `string` |  |  |

---

## ods_binlog_vc_event_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_event_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 52.9M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `gmt_create` | `bigint` |  | 创建时间 |
| 3 | `gmt_modify` | `bigint` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `character_id` | `bigint` |  | 虚拟人id |
| 6 | `type` | `string` |  | 事件类型，邀约事件、思念事件、破冰事件...... |
| 7 | `trigger_time` | `bigint` |  | 事件触发时间 |
| 8 | `ext` | `string` |  | 扩展信息 |
| 9 | `_bin_op` | `int` |  | 操作类型：0插入，1删除，2更新 |
| 10 | `_bin_op_time` | `bigint` |  | 操作时间 |
| 11 | `_bin_op_seqno` | `bigint` |  | 操作顺序 |
| 12 | `_bin_old` | `map<string, string>` |  | 旧数据 |
| 13 | `dt` | `string` |  |  |

---

## ods_binlog_vc_push_message_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_push_message_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 24.4M |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `uq_id` | `bigint` |  | 记录表唯一ID |
| 5 | `biz_id` | `string` |  | 业务唯一ID |
| 6 | `scene` | `string` |  | 业务场景 |
| 7 | `task_id` | `bigint` |  | 任务ID -1默认没有任务ID |
| 8 | `sub_task_id` | `bigint` |  | 子任务ID -1默认值，默认无该值 |
| 9 | `user_id` | `bigint` |  | 用户ID |
| 10 | `content` | `string` |  | 推送内容，JSON格式 |
| 11 | `send_status` | `int` |  | 发送状态 10：待发送 20：发送中  30：发送完成 40：发送失败-取消发送 41：发送失败-紧急撤销 42：发送失败-业务异常  -1：已删除 |
| 12 | `read_status` | `int` |  | 是否已读 0-未读 1-已读 |
| 13 | `receive_status` | `int` |  | 是否已到达 0-未到达，1-已到达 |
| 14 | `send_time` | `bigint` |  | 发送时间 |
| 15 | `ext` | `string` |  | 扩展信息 |
| 16 | `_bin_op` | `int` |  | 操作类型：0插入，1删除，2更新 |
| 17 | `_bin_op_time` | `bigint` |  | 操作时间 |
| 18 | `_bin_op_seqno` | `bigint` |  | 操作顺序 |
| 19 | `_bin_old` | `map<string, string>` |  | 旧数据 |
| 20 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_account_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_account_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 71.3M |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_name` | `string` |  | 用户昵称 |
| 3 | `user_profile` | `string` |  | 用户简介 |
| 4 | `user_head_img` | `string` |  | 用户头像 |
| 5 | `sex` | `bigint` |  | 性别 |
| 6 | `out_id` | `string` |  | 外部唯一标识 |
| 7 | `phone` | `string` |  | 用户手机号 |
| 8 | `status` | `bigint` |  | 状态 |
| 9 | `ext` | `string` |  | 扩展信息 |
| 10 | `create_time` | `bigint` |  | 创建时间 |
| 11 | `update_time` | `string` |  | 更新时间 |
| 12 | `user_code` | `string` |  |  |
| 13 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 14 | `account_init_process` | `string` |  | 账号初始化进度 |
| 15 | `device_id` | `string` |  | 设备ID |
| 16 | `account_type` | `int` |  | 账号类型: 1-默认账号，2-虚拟账号 |
| 17 | `device_uid` | `string` |  | 设备UID信息，非业务场景使用 |
| 18 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 19 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 20 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 21 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 22 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_asset_consume_flow_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_asset_consume_flow_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 20.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 20.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `amount` | `bigint` |  | 支出数量 |
| 5 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `consume_status` | `int` |  | 消耗状态： 1=预扣除；2=扣除完成；3=回滚完成 |
| 8 | `detail` | `string` |  | 支出详情 |
| 9 | `create_time` | `string` |  | 创建时间 |
| 10 | `update_time` | `string` |  | 更新时间 |
| 11 | `ext_amount` | `bigint` |  | 额外数量 |
| 12 | `partition_date` | `bigint` |  | 分区 |
| 13 | `ext` | `string` |  | 额外参数 |
| 14 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 15 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 16 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 17 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 18 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_character_memory_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_character_memory_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 58.0M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `long_memory_code` | `string` |  | 长期记忆code |
| 3 | `content` | `string` |  | 记忆摘要 |
| 4 | `type` | `int` |  | 类型：1.用户-虚拟人 2.虚拟人-虚拟人 |
| 5 | `user_id` | `bigint` |  | 用户id |
| 6 | `source_id` | `bigint` |  | 源目标 |
| 7 | `target_id` | `bigint` |  | 关联目标 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 11 | `ext` | `string` |  | 扩展信息 |
| 12 | `status` | `bigint` |  | 状态 |
| 13 | `sentence_dense_embedding` | `string` |  | 密集向量embedding |
| 14 | `sentence_sparse_embedding` | `string` |  | 稀疏向量embedding |
| 15 | `_bin_op` | `int` |  | 操作类型：0插入，1删除，2更新 |
| 16 | `_bin_op_time` | `bigint` |  | 操作时间 |
| 17 | `_bin_op_seqno` | `bigint` |  | 操作顺序 |
| 18 | `_bin_old` | `map<string, string>` |  | 旧数据 |
| 19 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_character_memory_relation_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_character_memory_relation_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 156.5M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `long_memory_code` | `string` |  | 长期记忆code |
| 3 | `message_id` | `string` |  | 消息id |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 7 | `ext` | `string` |  | 扩展信息 |
| 8 | `status` | `bigint` |  | 状态 |
| 9 | `_bin_op` | `int` |  | 操作类型：0插入，1删除，2更新 |
| 10 | `_bin_op_time` | `bigint` |  | 操作时间 |
| 11 | `_bin_op_seqno` | `bigint` |  | 操作顺序 |
| 12 | `_bin_old` | `map<string, string>` |  | 旧数据 |
| 13 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_deduction_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_deduction_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 203.1M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 扣除记录ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `record_id` | `bigint` |  | 购买发放记录明细表ID |
| 4 | `deduction_num` | `bigint` |  | 当前扣减数量 |
| 5 | `scene` | `int` |  | 支出场景 |
| 6 | `ext` | `string` |  | 扩展信息 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |
| 9 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 10 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 11 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 12 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 13 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_dungeon_chat_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_dungeon_chat_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 987.1M |
| **是否分区表** | 是 |

### 字段详情

共 24 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | from deserializer |
| 2 | `scene` | `string` |  | from deserializer |
| 3 | `user_id` | `bigint` |  | from deserializer |
| 4 | `character_id` | `bigint` |  | from deserializer |
| 5 | `create_time` | `bigint` |  | from deserializer |
| 6 | `update_time` | `bigint` |  | from deserializer |
| 7 | `req_id` | `string` |  | from deserializer |
| 8 | `db_update_time` | `bigint` |  | from deserializer |
| 9 | `biz_id` | `string` |  | from deserializer |
| 10 | `sender` | `int` |  | from deserializer |
| 11 | `status` | `int` |  | from deserializer |
| 12 | `content` | `string` |  | from deserializer |
| 13 | `content_type` | `int` |  | from deserializer |
| 14 | `msg_type` | `bigint` |  | from deserializer |
| 15 | `app_header` | `string` |  | from deserializer |
| 16 | `chat_seq` | `bigint` |  | from deserializer |
| 17 | `ext` | `string` |  | from deserializer |
| 18 | `_bin_op` | `int` |  | from deserializer |
| 19 | `_bin_op_time` | `bigint` |  | from deserializer |
| 20 | `_bin_op_seqno` | `bigint` |  | from deserializer |
| 21 | `_bin_old` | `map<string, string>` |  | from deserializer |
| 22 | `source` | `string` |  |  |
| 23 | `branch_id` | `bigint` |  | 存档id |
| 24 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_event_task_detail_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_event_task_detail_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 29.3M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `string` |  | 创建时间 |
| 3 | `update_time` | `string` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 用户ID |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `event_id` | `bigint` |  | 事件ID |
| 7 | `type` | `int` |  | 类型 1-卡片推送 2-点击选项 3-触发目标玩法 |
| 8 | `status` | `int` |  | 状态信息 2-处理中 4-已完成 5-已失败 |
| 9 | `ext` | `string` |  | 业务扩展信息 |
| 10 | `message_id` | `string` |  | 消息ID |
| 11 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 12 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 13 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 14 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 15 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_event_task_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_event_task_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 145.3M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `string` |  | 创建时间 |
| 3 | `update_time` | `string` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 用户ID |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `event_type` | `int` |  | 事件类型 1-次元事件 2-日常事件 3-情绪事件 |
| 7 | `status` | `int` |  | 状态信息 1-待处理 2-处理中 3-已关闭 4-已完成 5-已失败 -1-已删除 |
| 8 | `init_source` | `int` |  | 初始化来源 1-存量初始化 2-实时初始化 |
| 9 | `is_close_front_show` | `int` |  | 前台是否关闭 0-否 1-是 |
| 10 | `config_id` | `bigint` |  | 配置ID event_type=1会有值 |
| 11 | `content` | `string` |  | 内容信息 {"title" : "","description" : "","content" : "","jumpType" : "","jumpUrl" : ""} |
| 12 | `option_reply_config` | `string` |  | 选项配置信息 |
| 13 | `play_type` | `int` |  | 玩法类型 0-不支持玩法 1-任务玩法 2-数值玩法 |
| 14 | `play_config` | `string` |  | 玩法描述 |
| 15 | `ext` | `string` |  | 业务扩展信息 |
| 16 | `sub_event_type` | `int` |  | 事件子类型，0-默认值无实际含义，1-预埋事件，2-推送事件，3-日常事件 |
| 17 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 18 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 19 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 20 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 21 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_message_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_message_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 16.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 16.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | from deserializer |
| 2 | `user_id` | `bigint` |  | from deserializer |
| 3 | `character_id` | `bigint` |  | from deserializer |
| 4 | `conversation_id` | `string` |  | from deserializer |
| 5 | `message_id` | `string` |  | from deserializer |
| 6 | `sender` | `int` |  | from deserializer |
| 7 | `content` | `string` |  | from deserializer |
| 8 | `content_type` | `int` |  | from deserializer |
| 9 | `type` | `int` |  | from deserializer |
| 10 | `status` | `int` |  | from deserializer |
| 11 | `ai_model` | `int` |  | from deserializer |
| 12 | `create_time` | `bigint` |  | from deserializer |
| 13 | `update_time` | `bigint` |  | from deserializer |
| 14 | `channel` | `int` |  | from deserializer |
| 15 | `ext` | `string` |  | from deserializer |
| 16 | `reply_id` | `bigint` |  | from deserializer |
| 17 | `lofter_message_id` | `bigint` |  | from deserializer |
| 18 | `_bin_op` | `int` |  | from deserializer |
| 19 | `_bin_op_time` | `bigint` |  | from deserializer |
| 20 | `_bin_op_seqno` | `bigint` |  | from deserializer |
| 21 | `_bin_old` | `map<string, string>` |  | from deserializer |
| 22 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_message_record_partition_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_message_record_partition_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9.1G |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | from deserializer |
| 2 | `user_id` | `bigint` |  | from deserializer |
| 3 | `character_id` | `bigint` |  | from deserializer |
| 4 | `conversation_id` | `string` |  | from deserializer |
| 5 | `message_id` | `string` |  | from deserializer |
| 6 | `sender` | `int` |  | from deserializer |
| 7 | `content` | `string` |  | from deserializer |
| 8 | `content_type` | `int` |  | from deserializer |
| 9 | `type` | `int` |  | from deserializer |
| 10 | `status` | `int` |  | from deserializer |
| 11 | `ai_model` | `int` |  | from deserializer |
| 12 | `create_time` | `bigint` |  | from deserializer |
| 13 | `update_time` | `bigint` |  | from deserializer |
| 14 | `channel` | `int` |  | from deserializer |
| 15 | `ext` | `string` |  | from deserializer |
| 16 | `reply_id` | `bigint` |  | from deserializer |
| 17 | `lofter_message_id` | `bigint` |  | from deserializer |
| 18 | `_bin_op` | `int` |  | from deserializer |
| 19 | `_bin_op_time` | `bigint` |  | from deserializer |
| 20 | `_bin_op_seqno` | `bigint` |  | from deserializer |
| 21 | `_bin_old` | `map<string, string>` |  | from deserializer |
| 22 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_purchase_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_purchase_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 428.3M |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `order_id` | `bigint` |  | 订单ID，关联订单表 |
| 4 | `item_id` | `bigint` |  | 商品ID，关联vc_item表 |
| 5 | `item_scene` | `int` |  | 商品场景 |
| 6 | `reward_type` | `int` |  | 类型：1资产，2道具，3权益 |
| 7 | `reward_id` | `bigint` |  | 收入分类细分 |
| 8 | `num` | `bigint` |  | 收入数量 |
| 9 | `left_num` | `bigint` |  | 剩余数量：剩余可用数量 |
| 10 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 11 | `possession_id` | `bigint` |  | 持有财产id（若reward_type为1则为能量记录表id；reward_type为2则为vc_user_props的id；reward_type为3则为用户权益表的id） |
| 12 | `income_record_id` | `bigint` |  | 收入记录表id，有些类型可能不存收入记录表 |
| 13 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 14 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 15 | `create_time` | `bigint` |  | 数据创建时间 |
| 16 | `update_time` | `bigint` |  | 数据更新时间 |
| 17 | `ext` | `string` |  | 扩展信息 |
| 18 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 19 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 20 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 21 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 22 | `dt` | `string` |  |  |

---

## ods_binlog_vc_user_simulator_detail_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_user_simulator_detail_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 1.6G |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `db_update_time` | `bigint` |  | 更新时间 |
| 7 | `simulator_id` | `string` |  | 模拟器ID |
| 8 | `init_id` | `bigint` |  | 实例表ID |
| 9 | `type` | `int` |  | 操作类型1-初始化 3-任务判定 |
| 10 | `ext` | `string` |  | 业务扩展信息 |
| 11 | `chapter_id` | `string` |  | 章节ID |
| 12 | `controller_id` | `string` |  | 控制器ID |
| 13 | `result_config_id` | `string` |  | 结果配置ID |
| 14 | `result_ext` | `string` |  | 结果执行信息 |
| 15 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 16 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 17 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 18 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 19 | `branch_id` | `bigint` |  | 存档id，无默认0 |
| 20 | `dt` | `string` |  |  |

---

## ods_binlog_vc_vibe_game_instance_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_vibe_game_instance_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 322.9K |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id，无角色默认-1 |
| 4 | `game_id` | `string` |  | 游戏id（外部配置id） |
| 5 | `game_name` | `string` |  | 游戏名称 |
| 6 | `type` | `int` |  | 游戏类型，预留字段 |
| 7 | `status` | `int` |  | 实例状态：0-进行中 1-已结束 |
| 8 | `result` | `string` |  | 游戏结果（JSON格式） |
| 9 | `data` | `string` |  | 初始化信息（JSON格式） |
| 10 | `ext` | `string` |  | 扩展信息（JSON格式） |
| 11 | `create_time` | `string` |  | 创建时间 |
| 12 | `update_time` | `string` |  | 更新时间 |
| 13 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 14 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 15 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 16 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 17 | `dt` | `string` |  |  |

---

## ods_binlog_vc_vibe_game_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_binlog_vc_vibe_game_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 156.7K |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `instance_id` | `bigint` |  | 游戏实例id |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `character_id` | `bigint` |  | 角色id，无角色默认-1 |
| 5 | `game_id` | `string` |  | 游戏id（外部配置id） |
| 6 | `game_name` | `string` |  | 游戏名称 |
| 7 | `type` | `int` |  | 游戏类型，预留字段 |
| 8 | `action` | `string` |  | 上报行为标识 |
| 9 | `data` | `string` |  | 上报数据（JSON格式） |
| 10 | `ext` | `string` |  | 扩展信息（JSON格式） |
| 11 | `create_time` | `string` |  | 创建时间 |
| 12 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 13 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 14 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 15 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 16 | `dt` | `string` |  |  |

---

## ods_db_ab_app_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_app_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 业务id |
| 2 | `appname` | `string` |  | 业务名称 |
| 3 | `dbinserttime` | `bigint` |  |  |
| 4 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_exp_event_group_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_exp_event_group_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 唯一id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `eventgroupname` | `string` |  | 事件组名称 |
| 4 | `eventinfo` | `string` |  | 事件信息,kv结构 |
| 5 | `dbinserttime` | `bigint` |  |  |
| 6 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_exp_group_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_exp_group_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 实验分组id |
| 2 | `expid` | `bigint` |  | 实验id |
| 3 | `name` | `string` |  | 分组名称 |
| 4 | `flowrate` | `bigint` |  | 流量占比 |
| 5 | `bucketrange` | `string` |  | 分桶范围 |
| 6 | `whitelistid` | `string` |  | 白名单id，逗号拼接 |
| 7 | `paramkv` | `string` |  | 策略key和val，k1:v1,k2:v2 |
| 8 | `state` | `int` |  | 状态，0 - 正常，-1 - 删除 |
| 9 | `dbinserttime` | `bigint` |  |  |
| 10 | `dbupdatetime` | `bigint` |  |  |
| 11 | `showrate` | `bigint` |  | 展示流量占比 |

---

## ods_db_ab_exp_metric_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_exp_metric_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 实验指标id |
| 2 | `expid` | `bigint` |  | 实验id |
| 3 | `metricid` | `bigint` |  | 指标id |
| 4 | `dimid` | `bigint` |  | 维度id |
| 5 | `dbinserttime` | `bigint` |  |  |
| 6 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_exp_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_exp_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 实验id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `sceneid` | `bigint` |  | 流量场景id |
| 4 | `domainid` | `bigint` |  | 流量领域id |
| 5 | `layerid` | `bigint` |  | 流量层id |
| 6 | `name` | `string` |  | 实验名称 |
| 7 | `desctext` | `string` |  | 实验描述 |
| 8 | `starttime` | `bigint` |  | 开始时间，毫秒时间戳 |
| 9 | `endtime` | `bigint` |  | 结束时间，毫秒时间戳 |
| 10 | `crowd` | `string` |  | 人群包id，逗号拼接 |
| 11 | `exptype` | `int` |  | 实验类型，0 - 服务端，1 - 客户端 |
| 12 | `appversion` | `string` |  | 实验app版本，逗号拼接 |
| 13 | `state` | `int` |  | 实验状态 |
| 14 | `createor` | `string` |  | 创建人 |
| 15 | `flowrate` | `bigint` |  | 流量层流量占比 |
| 16 | `bucketrange` | `string` |  | 流量层分桶范围 |
| 17 | `paramkey` | `string` |  | 实验参数名列表 |
| 18 | `dbinserttime` | `bigint` |  |  |
| 19 | `dbupdatetime` | `bigint` |  |  |
| 20 | `dashboarduid` | `string` |  | dashboard唯一id |
| 21 | `dashboardurl` | `string` |  | dashboard地址 |
| 22 | `dashboardversion` | `string` |  | dashboard版本号 |
| 23 | `fileurl` | `string` |  | NOS文件链接 |
| 24 | `exptag` | `string` |  | 实验唯一标识 |
| 25 | `eventgroupids` | `string` |  | 事件组id,多个以逗号隔开 |
| 26 | `endversion` | `string` |  | 实验失效版本 |
| 27 | `startversion` | `string` |  | 实验生效版本 |
| 28 | `flowers` | `string` |  | 关注人 |

---

## ods_db_ab_exp_result_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_exp_result_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 唯一id |
| 2 | `expid` | `bigint` |  | 实验id |
| 3 | `expgroupid` | `bigint` |  | 实验组id |
| 4 | `expgroupname` | `string` |  | 实验组名称 |
| 5 | `metricid` | `bigint` |  | 指标id |
| 6 | `metricname` | `string` |  | 指标名称 |
| 7 | `metricdimid` | `bigint` |  | 指标维度id |
| 8 | `metricdimname` | `string` |  | 指标维度名称 |
| 9 | `day` | `bigint` |  | 结果时间 |
| 10 | `value` | `double` |  | 数值 |
| 11 | `dbinserttime` | `bigint` |  |  |
| 12 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_flow_domain_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_flow_domain_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 流量领域id |
| 2 | `sceneid` | `bigint` |  | 所属场景名称 |
| 3 | `domainname` | `string` |  | 领域名称 |
| 4 | `flowrate` | `bigint` |  | 流量比例, 43.3%-433 |
| 5 | `bucketrange` | `string` |  | 分桶范围，逗号拼接，范围横杠表示 |
| 6 | `whitelistid` | `string` |  | 白名单id，逗号拼接 |
| 7 | `dbinserttime` | `bigint` |  |  |
| 8 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_flow_layer_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_flow_layer_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 流量层id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `domainid` | `bigint` |  | 流量领域id |
| 4 | `layername` | `string` |  | 流量层名称 |
| 5 | `shuffletype` | `int` |  | 打散方式 0 - 固定，1 - 每周一打散 |
| 6 | `hashtype` | `int` |  | hash字段，0 - userId，1 - deviceId |
| 7 | `hashseed` | `string` |  | hash种子 |
| 8 | `dbinserttime` | `bigint` |  |  |
| 9 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_flow_scene_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_flow_scene_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 流量场景id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `scenename` | `string` |  | 场景名称 |
| 4 | `scenealias` | `string` |  | 场景名别称 |
| 5 | `hashtype` | `int` |  | hash 类型，0 - userId，1 - deviceId |
| 6 | `hashseed` | `string` |  | hash种子 |
| 7 | `dbinserttime` | `bigint` |  |  |
| 8 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_metric_business_line_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_metric_business_line_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 指标业务线id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `businesslinename` | `string` |  | 业务线名称 |
| 4 | `dbinserttime` | `bigint` |  |  |
| 5 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_metric_detail_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_metric_detail_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 指标id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `metriclineid` | `bigint` |  | 指标业务线id |
| 4 | `metricsceneid` | `bigint` |  | 指标场景id |
| 5 | `metricname` | `string` |  | 指标名称 |
| 6 | `metriccode` | `string` |  | 指标监控项代码 |
| 7 | `status` | `int` |  | 指标状态，0 - 开发中，1 - 上线，-1 下线 |
| 8 | `dbinserttime` | `bigint` |  |  |
| 9 | `dbupdatetime` | `bigint` |  |  |
| 10 | `metrictype` | `int` |  | 1:单一指标,2:复合指标 |
| 11 | `basemetricinfo` | `string` |  | 复合的指标信息,kv结构 |
| 12 | `calrules` | `string` |  | 复合指标计算规则 |
| 13 | `datatype` | `int` |  | 数据格式 1：数字，2：百分比 |
| 14 | `scales` | `int` |  | 小数点位数 |

---

## ods_db_ab_metric_dim_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_metric_dim_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 维度id |
| 2 | `metricid` | `bigint` |  | 指标id |
| 3 | `dimname` | `string` |  | 维度名称 |
| 4 | `dimcode` | `string` |  | 维度代码 |
| 5 | `dbinserttime` | `bigint` |  |  |
| 6 | `dbupdatetime` | `bigint` |  |  |
| 7 | `multidiminfo` | `string` |  | 复合维度信息,kv结构 |

---

## ods_db_ab_metric_scene_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_metric_scene_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 指标场景id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `scenename` | `string` |  | 场景名称 |
| 4 | `dbinserttime` | `bigint` |  |  |
| 5 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ab_trace_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_trace_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 唯一id |
| 2 | `appid` | `bigint` |  | 业务id |
| 3 | `traceid` | `string` |  | 追踪id |
| 4 | `operatorname` | `string` |  | 操作人 |
| 5 | `operateip` | `string` |  | 操作ip |
| 6 | `operatetime` | `bigint` |  | 操作时间 |
| 7 | `operatetablename` | `string` |  | 操作表名 |
| 8 | `operatetype` | `string` |  | 操作类型 |
| 9 | `sqltext` | `string` |  | 执行sql |
| 10 | `sqlparam` | `string` |  | sql参数 |
| 11 | `primarykey` | `string` |  | 主键名 |
| 12 | `affectrows` | `bigint` |  | 影响行数 |
| 13 | `beforedata` | `string` |  | 执行前数据 |
| 14 | `afterdata` | `string` |  | 执行后数据 |
| 15 | `diffdata` | `string` |  | 差异点 |
| 16 | `ext` | `string` |  | 备注 |
| 17 | `uri` | `string` |  | 请求路径 |

---

## ods_db_ab_white_list_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ab_white_list_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 白名单包id |
| 2 | `appid` | `bigint` |  | 业务Id |
| 3 | `name` | `string` |  | 包名称 |
| 4 | `desctext` | `string` |  | 包描述 |
| 5 | `whitelist` | `string` |  | userId或deviceId列表 |
| 6 | `dbinserttime` | `bigint` |  |  |
| 7 | `dbupdatetime` | `bigint` |  |  |

---

## ods_db_ad_channel_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_ad_channel_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | ID |
| 2 | `appid` | `string` |  | 产品id |
| 3 | `media` | `string` |  | 投放媒体名称 |
| 4 | `advertiserid` | `string` |  | 广告主ID |
| 5 | `proxy` | `string` |  | 代理名称 |
| 6 | `channelpackage` | `string` |  | 渠道包名称 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `updatetime` | `bigint` |  | 更新时间 |

---

## ods_db_rt1h_vc_character_package_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_rt1h_vc_character_package_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | internal |
| **表大小** | 21.4K |
| **是否分区表** | 否 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `name` | `string` |  |  |
| 3 | `ugc_categories` | `string` |  |  |
| 4 | `ugc_front_tags` | `string` |  |  |
| 5 | `ugc_backend_tags` | `string` |  |  |
| 6 | `ogc_categories` | `string` |  |  |
| 7 | `ogc_backend_tags` | `string` |  |  |
| 8 | `character_file_url` | `string` |  |  |
| 9 | `black_list` | `string` |  |  |
| 10 | `ext` | `string` |  |  |
| 11 | `status` | `int` |  |  |
| 12 | `create_time` | `timestamp` |  |  |
| 13 | `update_time` | `timestamp` |  |  |
| 14 | `version` | `int` |  |  |
| 15 | `ogc_front_tags` | `string` |  |  |
| 16 | `ogc_tag_type` | `int` |  |  |
| 17 | `ugc_tag_type` | `int` |  |  |

---

## ods_db_rt1h_vc_rank_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_rt1h_vc_rank_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | internal |
| **表大小** | 21.1K |
| **是否分区表** | 否 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `name` | `string` |  |  |
| 3 | `pack_id` | `bigint` |  |  |
| 4 | `simulator_id` | `string` |  |  |
| 5 | `type` | `int` |  |  |
| 6 | `target_type` | `int` |  |  |
| 7 | `icon` | `string` |  |  |
| 8 | `sub_title` | `string` |  |  |
| 9 | `order_field` | `int` |  |  |
| 10 | `order_type` | `int` |  |  |
| 11 | `sql_clause` | `string` |  |  |
| 12 | `value_render_config` | `string` |  |  |
| 13 | `img_url` | `string` |  |  |
| 14 | `status` | `int` |  |  |
| 15 | `start_time` | `timestamp` |  |  |
| 16 | `end_time` | `timestamp` |  |  |
| 17 | `create_time` | `timestamp` |  |  |
| 18 | `update_time` | `timestamp` |  |  |
| 19 | `rule_route` | `string` |  |  |
| 20 | `version` | `bigint` |  |  |
| 21 | `ext` | `string` |  |  |
| 22 | `limit_cnt` | `int` |  |  |

---

## ods_db_rt1h_vc_user_simulator_init_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_rt1h_vc_user_simulator_init_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | internal |
| **表大小** | 228.7M |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `user_id` | `bigint` |  |  |
| 3 | `character_id` | `bigint` |  |  |
| 4 | `create_time` | `bigint` |  |  |
| 5 | `update_time` | `bigint` |  |  |
| 6 | `db_update_time` | `timestamp` |  |  |
| 7 | `simulator_id` | `string` |  |  |
| 8 | `status` | `int` |  |  |
| 9 | `init_param` | `string` |  |  |
| 10 | `ext` | `string` |  |  |

---

## ods_db_vc_account_access_token_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_account_access_token_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `phone` | `string` |  | 用户手机号 |
| 4 | `out_id` | `string` |  | 外部唯一标识 |
| 5 | `client_type` | `string` |  | 客户端类型 |
| 6 | `token_expire_time` | `bigint` |  | token失效时间 |
| 7 | `token_check_time` | `bigint` |  | token验证时间 |
| 8 | `ext` | `string` |  | 扩展信息 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |
| 11 | `token` | `string` |  |  |
| 12 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 13 | `device_id` | `string` |  | 设备ID |

---

## ods_db_vc_account_bind_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_account_bind_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `share_platform` | `int` |  | 发布平台 |
| 4 | `account` | `string` |  | 平台账号 |
| 5 | `unbind` | `int` |  | 是否解绑 1.绑定 2.解绑 |
| 6 | `create_time` | `bigint` |  | DB创建时间 |
| 7 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_account_map_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_account_map_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `phone` | `string` |  | 用户手机号 |
| 4 | `phone_unique_id` | `string` |  | 手机号码 ssn |
| 5 | `channel_unique_id` | `string` |  | 外部账号 ssn |
| 6 | `status` | `bigint` |  | 状态 |
| 7 | `ext` | `string` |  | 扩展信息 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `channel` | `string` |  | 渠道 |
| 11 | `app_channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |

---

## ods_db_vc_activity_module_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_activity_module_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 6.9M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `activity_id` | `bigint` |  | 关联的活动id |
| 3 | `module_icon` | `string` |  | 模块图标 |
| 4 | `top_label` | `string` |  | 顶部标签 |
| 5 | `title` | `string` |  | 模块标题 |
| 6 | `sub_title` | `string` |  | 模块副标题 |
| 7 | `module_type` | `int` |  | 模块类型：1每日免费；2每日额外；3补签；4签到奖励 |
| 8 | `module_config` | `string` |  | 模块配置（按钮、奖励配置、领取限制等） |
| 9 | `created_by` | `string` |  | 创建人 |
| 10 | `updated_by` | `string` |  | 更新人 |
| 11 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 更新时间 |
| 14 | `sort_order` | `bigint` |  |  |
| 15 | `title_icon` | `string` |  |  |
| 16 | `dt` | `string` |  |  |

---

## ods_db_vc_activity_module_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_activity_module_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `activity_id` | `bigint` |  | 关联的活动id |
| 3 | `module_icon` | `string` |  | 模块图标 |
| 4 | `top_label` | `string` |  | 顶部标签 |
| 5 | `title` | `string` |  | 模块标题 |
| 6 | `sub_title` | `string` |  | 模块副标题 |
| 7 | `module_type` | `int` |  | 模块类型：1每日免费；2每日额外；3补签；4签到奖励 |
| 8 | `module_config` | `string` |  | 模块配置（按钮、奖励配置、领取限制等） |
| 9 | `created_by` | `string` |  | 创建人 |
| 10 | `updated_by` | `string` |  | 更新人 |
| 11 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 更新时间 |
| 14 | `sort_order` | `bigint` |  | 排序字段，数值越小排序越靠前 |
| 15 | `title_icon` | `string` |  | 模块标题图标 |

---

## ods_db_vc_activity_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_activity_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `activity_name` | `string` |  | 活动名称 |
| 3 | `description` | `string` |  | 描述 |
| 4 | `activity_type` | `int` |  | 活动类型：1签到领取活动，2连续签到奖励活动 |
| 5 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 6 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 7 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 8 | `created_by` | `string` |  | 创建人 |
| 9 | `updated_by` | `string` |  | 更新人 |
| 10 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 11 | `status` | `int` |  | 活动状态：1=初始化，2=上架，3=下架 |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 更新时间 |
| 14 | `period` | `string` |  | 活动周期：day1-dayX表示1-x天任务，week表示每自然周任务 |
| 15 | `config` | `string` |  | 活动配置 |

---

## ods_db_vc_ad_watch_order_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_ad_watch_order_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键，即orderId |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `module_id` | `bigint` |  | 活动模块ID |
| 4 | `category` | `string` |  | 广告分类 |
| 5 | `location` | `bigint` |  | 广告位置 |
| 6 | `status` | `int` |  | 0=待观看, 1=已完成 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `channel` | `string` |  | 渠道 |

---

## ods_db_vc_admin_auth_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_auth_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_code` | `string` |  | 账号ID |
| 3 | `create_time` | `bigint` |  | 数据创建时间 |
| 4 | `update_time` | `bigint` |  | 数据更新时间 |
| 5 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_admin_menu_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_menu_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 菜单名 |
| 3 | `code` | `string` |  | 菜单code |
| 4 | `parent_id` | `bigint` |  | 父id |
| 5 | `type` | `bigint` |  | 类型 1.页面 2.按钮 |
| 6 | `url` | `string` |  | 接口地址 |
| 7 | `icon_url` | `string` |  | 图标路径 |
| 8 | `router` | `string` |  | 路由 |
| 9 | `path` | `string` |  | 树路径 |
| 10 | `level` | `bigint` |  | 层级 |
| 11 | `sort` | `double` |  |  |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 更新时间 |
| 14 | `created_by` | `string` |  | 创建人邮箱 |
| 15 | `updated_by` | `string` |  | 更新人邮箱 |
| 16 | `status` | `int` |  | 是否删除：1正常，-1删除 |

---

## ods_db_vc_admin_operation_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_operation_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `email` | `string` |  | 邮箱 |
| 3 | `url` | `string` |  | 请求路径 |
| 4 | `type` | `string` |  | 请求类型 |
| 5 | `ext` | `string` |  | 拓展信息 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_admin_review_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_review_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `type` | `int` |  | 审核类型，1 礼包审核 |
| 3 | `reason` | `string` |  | 理由 |
| 4 | `apply_by` | `string` |  | 申请人 |
| 5 | `review_by` | `string` |  | 审核人 |
| 6 | `related_id` | `bigint` |  | 关联id |
| 7 | `status` | `int` |  | 审核状态 1审核中 2审核成功 3审核失败 |
| 8 | `ext` | `string` |  | 扩展字段 |
| 9 | `created_by` | `string` |  | 创建人邮箱 |
| 10 | `updated_by` | `string` |  | 更新人邮箱 |
| 11 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 12 | `create_time` | `bigint` |  | 数据创建时间 |
| 13 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_admin_role_group_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_role_group_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `group_name` | `string` |  | 角色分组名 |
| 3 | `create_time` | `bigint` |  | 创建时间 |
| 4 | `update_time` | `bigint` |  | 更新时间 |
| 5 | `created_by` | `string` |  | 创建人邮箱 |
| 6 | `updated_by` | `string` |  | 更新人邮箱 |
| 7 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 8 | `status` | `int` |  | 是否删除：1正常，-1删除 |

---

## ods_db_vc_admin_role_menu_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_role_menu_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `role_group_id` | `bigint` |  | 角色id |
| 3 | `menu_id` | `string` |  | 菜单id |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `created_by` | `string` |  | 创建人邮箱 |
| 7 | `updated_by` | `string` |  | 更新人邮箱 |
| 8 | `status` | `int` |  | 是否删除：1正常，-1删除 |

---

## ods_db_vc_admin_user_role_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_admin_user_role_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `email` | `string` |  | 用户邮箱 |
| 3 | `role_group_id` | `bigint` |  | 角色id |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `created_by` | `string` |  | 创建人邮箱 |
| 7 | `updated_by` | `string` |  | 更新人邮箱 |
| 8 | `status` | `int` |  | 是否删除：1正常，-1删除 |

---

## ods_db_vc_adornment_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_adornment_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `name` | `string` |  | 名称 |
| 5 | `image_url` | `string` |  | 图片地址 |
| 6 | `type` | `bigint` |  | 类型：1.头饰 2.气泡 |
| 7 | `created_by` | `string` |  | 创建人邮箱 |
| 8 | `updated_by` | `string` |  | 更新人邮箱 |
| 9 | `config` | `string` |  | 配置信息json结构 |
| 10 | `ext` | `string` |  | 额外配置 |
| 11 | `status` | `int` |  | 状态：1.待上架 2.已上架 4.删除 |
| 12 | `weight` | `bigint` |  | 权重，数值越大优先级越高 |

---

## ods_db_vc_ai_experiment_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_ai_experiment_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `experiment_id` | `bigint` |  | 实验id |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `character_id` | `bigint` |  | 角色id |
| 5 | `business_type` | `string` |  | 业务类型 |
| 6 | `create_time` | `bigint` |  | 数据创建时间 |
| 7 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_ai_scene_code_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_ai_scene_code_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `scene_code` | `string` |  | 场景code |
| 3 | `scene_type` | `int` |  | 1.聊天类型 2.任务类型 3.灵感提示 |
| 4 | `model_type` | `int` |  | 模型厂商 |
| 5 | `model_name` | `string` |  | 模型名 |
| 6 | `system_prompt` | `string` |  | 系统提示词 |
| 7 | `temperature` | `double` |  | 较高的值将使输出更加随机，而较低的值将使输出更加集中和确定，默认0.7 |
| 8 | `top_p` | `double` |  | 采样方法，数值越小结果确定性越强；数值越大，结果越随机，默认1 |
| 9 | `customized_param` | `string` |  | 自定义参数 |
| 10 | `status` | `int` |  | 1.有效 2.无效 |
| 11 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 更新时间 |
| 14 | `ext` | `string` |  | 额外配置 |

---

## ods_db_vc_ai_scene_rule_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_ai_scene_rule_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `rule_code` | `string` |  | 规则code |
| 3 | `type` | `int` |  | 1.业务规则 2.流量规则 |
| 4 | `rule_config` | `string` |  | 规则配置 |
| 5 | `status` | `int` |  | 1.有效 2.无效 |
| 6 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `ext` | `string` |  | 额外配置 |

---

## ods_db_vc_annual_gift_character_rank_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_annual_gift_character_rank_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `character_id` | `bigint` |  | app角色ID |
| 3 | `lofter_id` | `bigint` |  | lofterid |
| 4 | `total_energy` | `bigint` |  | 总能量值 |
| 5 | `last_time` | `bigint` |  | 最后一次更新时间 |
| 6 | `activity_id` | `bigint` |  | 榜单活动ID，0表示历史数据（迁移前） |

---

## ods_db_vc_annual_gift_character_stats_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_annual_gift_character_stats_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `character_id` | `bigint` |  |  |
| 3 | `channel` | `bigint` |  |  |
| 4 | `total_gifts` | `bigint` |  |  |
| 5 | `total_users` | `bigint` |  |  |
| 6 | `last_time` | `bigint` |  |  |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |
| 9 | `total_energy` | `bigint` |  | 能量值 |
| 10 | `activity_id` | `bigint` |  | 榜单活动ID，0表示历史数据（迁移前） |

---

## ods_db_vc_annual_gift_detail_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_annual_gift_detail_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `user_id` | `bigint` |  |  |
| 3 | `character_id` | `bigint` |  |  |
| 4 | `gift_id` | `bigint` |  |  |
| 5 | `gift_count` | `bigint` |  |  |
| 6 | `channel` | `bigint` |  |  |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |
| 9 | `ext` | `string` |  |  |
| 10 | `total_energy` | `bigint` |  | 能量值 |
| 11 | `activity_id` | `bigint` |  | 榜单活动ID，0表示历史数据（迁移前） |

---

## ods_db_vc_annual_gift_user_stats_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_annual_gift_user_stats_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `user_id` | `bigint` |  |  |
| 3 | `channel` | `bigint` |  |  |
| 4 | `character_id` | `bigint` |  |  |
| 5 | `total_count` | `bigint` |  |  |
| 6 | `create_time` | `bigint` |  | 数据创建时间 |
| 7 | `update_time` | `bigint` |  | 数据更新时间 |
| 8 | `last_time` | `bigint` |  |  |
| 9 | `total_energy` | `bigint` |  | 能量值 |
| 10 | `activity_id` | `bigint` |  | 榜单活动ID，0表示历史数据（迁移前） |

---

## ods_db_vc_backend_tag_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_backend_tag_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 名称 |
| 3 | `status` | `int` |  | 1：上线；0：下线 |
| 4 | `create_time` | `bigint` |  | 数据创建时间 |
| 5 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_batch_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_batch_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `task_code` | `string` |  | 任务场景 |
| 3 | `record_code` | `string` |  | 纪录唯一code |
| 4 | `status` | `int` |  | 状态，0-初始化，1-执行中，2-成功，3-失败 |
| 5 | `ext` | `string` |  | 额外配置 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_batch_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_batch_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `task_scene` | `string` |  | 任务场景 |
| 3 | `task_code` | `string` |  | 任务唯一code |
| 4 | `status` | `int` |  | 状态，0-初始化，1-执行中，2-成功，3-失败 |
| 5 | `ext` | `string` |  | 额外配置 |
| 6 | `start_time` | `bigint` |  | 任务开始时间 |
| 7 | `end_time` | `bigint` |  | 任务结束时间 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `user_group_type` | `int` |  | 用户类型 1=uid多个逗号隔开；2=人群包，3全量 |
| 11 | `group_id` | `string` |  | 分组id |
| 12 | `search_key1` | `string` |  | 搜索关键词 |
| 13 | `priority` | `bigint` |  | 任务优先级，数值越大优先级越高 |
| 14 | `created_by` | `string` |  | 创建人邮箱 |
| 15 | `updated_by` | `string` |  | 更新人邮箱 |
| 16 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认） |
| 17 | `success_count` | `bigint` |  | 成功数 |
| 18 | `lose_count` | `bigint` |  | 失败数 |
| 19 | `task_count` | `bigint` |  | 任务总数 |

---

## ods_db_vc_birthday_daily_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_birthday_daily_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `event_id` | `bigint` |  | 活动id |
| 3 | `user_id` | `bigint` |  | 用户ID |
| 4 | `character_id` | `bigint` |  | 角色ID |
| 5 | `reset_type` | `bigint` |  | 任务类型 1.按日刷新 2.只能完成一次 |
| 6 | `task_type` | `bigint` |  | 任务类型 1.按日刷新 2.只能完成一次 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_birthday_question_bank_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_birthday_question_bank_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `question_bank` | `string` |  | 题目内容 |
| 3 | `create_time` | `bigint` |  | 数据创建时间 |
| 4 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_birthday_support_gift_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_birthday_support_gift_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `event_id` | `bigint` |  | 活动id |
| 4 | `gift_count` | `bigint` |  | 礼物数量 |
| 5 | `support_gift_id` | `bigint` |  | 礼物id |
| 6 | `create_time` | `bigint` |  | 数据创建时间 |
| 7 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_birthday_support_gift_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_birthday_support_gift_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `event_id` | `bigint` |  | 活动id |
| 4 | `character_id` | `bigint` |  | 角色id |
| 5 | `support_gift_id` | `bigint` |  | 应援礼物id |
| 6 | `support_value` | `bigint` |  | 应援值 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |
| 9 | `gift_count` | `bigint` |  | 礼物数量 |

---

## ods_db_vc_birthday_support_ranking_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_birthday_support_ranking_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `event_id` | `bigint` |  | 活动id |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `total_support_value` | `bigint` |  | 热度总应援值 |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_birthday_user_answer_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_birthday_user_answer_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `event_id` | `bigint` |  | 活动id |
| 3 | `user_id` | `bigint` |  | 用户ID |
| 4 | `question_id` | `bigint` |  | 问题ID |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_board_game_message_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_board_game_message_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `game_record_id` | `bigint` |  | 游戏记录ID |
| 3 | `scene` | `string` |  | 玩法场景，每个场景单独定义 |
| 4 | `part` | `bigint` |  | 轮次 |
| 5 | `user_id` | `bigint` |  | 用户ID |
| 6 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 7 | `source_player_id` | `bigint` |  | 玩家ID，无角色默认值-1 |
| 8 | `create_time` | `bigint` |  | 创建时间（时间戳，毫秒） |
| 9 | `update_time` | `bigint` |  | 更新时间（时间戳，毫秒） |
| 10 | `req_id` | `string` |  | 请求id |
| 11 | `db_update_time` | `bigint` |  | 记录更新时间 |
| 12 | `biz_id` | `string` |  | 业务ID，不存在默认为空字符串 |
| 13 | `sender` | `bigint` |  | 发话类型 1-角色发送 2-用户发送，3-系统发送 |
| 14 | `status` | `bigint` |  | 消息状态 1-正常态，3-命中敏感词，4-ai异常，5-ai敏感词异常，6-遗忘，7-已删除 |
| 15 | `content` | `string` |  | 发话内容 |
| 16 | `content_type` | `bigint` |  | 内容类型 |
| 17 | `msg_type` | `bigint` |  | 消息类型 |
| 18 | `app_header` | `string` |  | 用户header信息 |
| 19 | `msg_seq` | `bigint` |  | 聊天轮次 |
| 20 | `ext` | `string` |  | 扩展信息（JSON格式） |
| 21 | `source` | `string` |  | 来源信息 |

---

## ods_db_vc_call_back_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_call_back_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 数据创建时间 |
| 3 | `update_time` | `bigint` |  | 数据更新时间 |
| 4 | `call_back_type` | `bigint` |  | 召回类型 |
| 5 | `task_id` | `string` |  | 任务ID -1默认没有任务ID |
| 6 | `sub_task_id` | `string` |  | 子任务ID -1默认值，默认无该值 |
| 7 | `user_id` | `bigint` |  | 用户ID |
| 8 | `msg_body` | `string` |  | kafka消息体 |
| 9 | `status` | `bigint` |  | 0成功，1失败 |
| 10 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_category_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_category_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 名称 |
| 3 | `status` | `int` |  | 1：上线；0：下线 |
| 4 | `create_time` | `bigint` |  | 数据创建时间 |
| 5 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_character_ai_content_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_ai_content_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `scene` | `bigint` |  | 场景值 |
| 3 | `content` | `string` |  | 消息内容 |
| 4 | `character_id` | `bigint` |  | 角色id |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_character_choose_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_choose_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `status` | `int` |  | 状态,1:上架,0:下架 |
| 4 | `index_feed_status` | `int` |  | 是否加入发现页瀑布流,1:是,0:否 |
| 5 | `stranger_msg_status` | `int` |  | 是否加入陌生人消息,1:是,0:否 |
| 6 | `chat_recommend_status` | `int` |  | 是否加入聊天页推荐,1:是,0:否 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `type` | `int` |  | 0官方角色，1用户自由创建 |

---

## ods_db_vc_character_corpus_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_corpus_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 角色 |
| 3 | `emotional_status` | `int` |  | 状态0邀约,1普通,2吃醋,3冷落 |
| 4 | `content_setting` | `string` |  | 语料集合、顺序、是否音频 |
| 5 | `group_num` | `bigint` |  | 组号 |
| 6 | `stage` | `bigint` |  | 聊天阶段 |
| 7 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `ext` | `string` |  | 扩展字段 |
| 11 | `created_by` | `string` |  | 创建人邮箱 |
| 12 | `updated_by` | `string` |  | 更新人邮箱 |
| 13 | `status` | `int` |  | 语料状态:1.未生效，2.生效，-1.删除 |
| 14 | `content_type` | `int` |  | 内容类型,1普通语料2prompt |

---

## ods_db_vc_character_free_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_free_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `weight` | `int` |  | 权重 |
| 4 | `free_type` | `int` |  | 限免类型 1.每日限免 2.新人限免 |
| 5 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 6 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 7 | `created_by` | `string` |  | 创建人邮箱 |
| 8 | `updated_by` | `string` |  | 更新人邮箱 |
| 9 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 10 | `create_time` | `bigint` |  | DB创建时间 |
| 11 | `update_time` | `bigint` |  | DB更新时间 |
| 12 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |

---

## ods_db_vc_character_hot_top_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_hot_top_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `period` | `string` |  | 周期 |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `character_name` | `string` |  | 角色名称 |
| 5 | `character_type` | `int` |  | 角色类型：0官方 1自由捏 |
| 6 | `talk_rank` | `bigint` |  | 轮次排名 |
| 7 | `talk_count` | `bigint` |  | 具体轮次 |
| 8 | `people_count_rank` | `bigint` |  | 人数排名 |
| 9 | `people_count` | `bigint` |  | 具体人数 |
| 10 | `dt` | `bigint` |  | dt |
| 11 | `create_time` | `bigint` |  | 数据创建时间 |
| 12 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_character_impression_like_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_impression_like_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `impression_id` | `bigint` |  | 印象词id |
| 4 | `character_id` | `bigint` |  | 关联的角色id |
| 5 | `liked` | `int` |  | 状态：1：已赞，0：未赞，-1：删除 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_character_impression_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_impression_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 印象词关联的角色id |
| 3 | `user_id` | `bigint` |  | 词创建用户id |
| 4 | `like_count` | `bigint` |  | 点赞量 |
| 5 | `impression` | `string` |  | 印象词 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_character_moment_backup_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_moment_backup_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 虚拟角色ID |
| 3 | `content` | `string` |  | 角色动态内容 |
| 4 | `location` | `string` |  | 角色位置信息 |
| 5 | `ext` | `string` |  | 额外信息，如发布视频、图片链接等 |
| 6 | `public_type` | `int` |  | 公开类型，1表示公开；2表示私密；3部分可见 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `release_time` | `bigint` |  | 发布时间 |
| 10 | `comment_count` | `bigint` |  | 评论数 |
| 11 | `like_count` | `bigint` |  | 点赞数 |
| 12 | `created_by` | `string` |  | 创建人邮箱 |
| 13 | `updated_by` | `string` |  | 更新人邮箱 |
| 14 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 15 | `status` | `int` |  | 状态：1生效（默认）-1=失效（删除） |
| 16 | `send_msg_flag` | `int` |  | 消息发送状态：0未发送 1=已发送 |
| 17 | `content_md5` | `string` |  | 动态内容md5值 |
| 18 | `post_id` | `bigint` |  | lofter文章id |
| 19 | `author_type` | `int` |  | 发布者类型：1=NPC，2=用户，3=用户分身 |
| 20 | `author_id` | `bigint` |  | 具体发布者ID：author_type=1时为NPC的character_id；author_type=2时为userId；author_type=3时为分身ID |
| 21 | `post_type` | `int` |  | 动态内容类型：1=文字，2=语音 |
| 22 | `user_id` | `bigint` |  | 归属用户ID：author_type=1时为NULL；author_type=2/3时均为用户自身userId |
| 23 | `circle_source` | `int` |  | 圈子来源：0=非圈子(原有)，1=top50全局自动，2=用户兴趣池，3=用户手动发布，4=运营手动配置 |
| 24 | `script_json` | `string` |  | 预计算剧本JSON：NPC发帖预计算时存储完整回复队列，执行后可清空 |
| 25 | `tags` | `string` |  | 标签列表JSON数组，如 ["标签1","标签2"]，最多5个 |

---

## ods_db_vc_character_moment_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_moment_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 虚拟角色ID |
| 3 | `content` | `string` |  | 角色动态内容 |
| 4 | `location` | `string` |  | 角色位置信息 |
| 5 | `ext` | `string` |  | 额外信息 |
| 6 | `public_type` | `int` |  | 公开类型，1表示公开；2表示私密；3部分可见 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `release_time` | `bigint` |  | 发布时间 |
| 10 | `comment_count` | `bigint` |  | 评论数 |
| 11 | `like_count` | `bigint` |  | 点赞数 |
| 12 | `created_by` | `string` |  | 创建人邮箱 |
| 13 | `updated_by` | `string` |  | 更新人邮箱 |
| 14 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 15 | `status` | `int` |  | 状态：1生效（默认）-1=失效（删除） |
| 16 | `send_msg_flag` | `int` |  | 消息发送状态：0未发送 1=已发送 |
| 17 | `content_md5` | `string` |  | 动态内容md5值 |
| 18 | `post_id` | `bigint` |  | lofter文章id |
| 19 | `author_type` | `int` |  | 发布者类型：1=NPC，2=用户，3=用户分身 |
| 20 | `author_id` | `bigint` |  | 具体发布者ID：author_type=1时为NPC的character_id；author_type=2时为userId；author_type=3时为分身ID |
| 21 | `post_type` | `int` |  | 动态内容类型：1=文字，2=语音 |
| 22 | `user_id` | `bigint` |  | 归属用户ID：author_type=1时为NULL；author_type=2/3时均为用户自身userId |
| 23 | `circle_source` | `int` |  | 圈子来源：0=非圈子(原有)，1=top50全局自动，2=用户兴趣池，3=用户手动发布，4=运营手动配置 |
| 24 | `script_json` | `string` |  | 预计算剧本JSON：NPC发帖预计算时存储完整回复队列，执行后可清空 |
| 25 | `tags` | `string` |  | 标签列表JSON数组，如 ["标签1","标签2"]，最多5个 |
| 26 | `target_user_id` | `bigint` |  | 专属动态目标用户ID，publicType=4时表示只对该用户可见 |
| 27 | `simulator_id` | `string` |  | 模拟器评论区文章绑定的 simulatorId, NULL 表示原圈子动态 |

---

## ods_db_vc_character_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 关联用户账号ID |
| 3 | `creator_uid` | `bigint` |  | 创建者用户id |
| 4 | `character_name` | `string` |  | 角色名称 |
| 5 | `description` | `string` |  | 角色描述 |
| 6 | `character_avatar` | `string` |  | 角色头像 |
| 7 | `character_back_img` | `string` |  | 角色背景图 |
| 8 | `residence` | `string` |  | 居住地 |
| 9 | `birthday` | `string` |  | 生日 |
| 10 | `bot_setting` | `string` |  |  |
| 11 | `ext_prompt` | `string` |  | 角色额外的prompt |
| 12 | `front_tags` | `string` |  | 前台类目 |
| 13 | `backend_tags` | `string` |  | 后台类目 |
| 14 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 15 | `type` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 16 | `weights` | `bigint` |  | 角色权重 |
| 17 | `score` | `bigint` |  | 后台评分 |
| 18 | `audit_time` | `bigint` |  | 角色审核时间 |
| 19 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 20 | `ext` | `string` |  | 额外配置 |
| 21 | `create_time` | `bigint` |  | 创建时间 |
| 22 | `update_time` | `bigint` |  | 更新时间 |
| 23 | `valid_start_time` | `bigint` |  | 有效开始时间（status=2时有效） |
| 24 | `character_dyn_back_img` | `string` |  | 角色动态背景图 |
| 25 | `audience_type` | `int` |  | 受众类型：1所有人（默认），2成年人，3未成年人，4审核人员 |
| 26 | `about_he` | `string` |  | 关于ta |
| 27 | `dyn_back_first_frame_img` | `string` |  | 动态背景图第一帧图片 |
| 28 | `drafts_info` | `string` |  | 草稿箱 |
| 29 | `public_scope` | `int` |  | 公开状态 0 初始值 1:公开 2:私密 |
| 30 | `sex` | `int` |  | 角色性别 0:未设置 1:男 2:女 3:无性别 |
| 31 | `audit_status` | `int` |  | UGC审核结果 0:初始值 1:审核成功 2:审核中 3.审核失败 |
| 32 | `stage_settings` | `string` |  | 阶段人设 |
| 33 | `voice_pack_id` | `bigint` |  | 音色包id |
| 34 | `lofter_id` | `bigint` |  | 站内角色id |
| 35 | `category` | `string` |  | 品类名称 |
| 36 | `detail_setting` | `string` |  | 角色拆解详细人设 |
| 37 | `model_item_id` | `bigint` |  | 默认模式模型id |
| 38 | `pack_id` | `bigint` |  | 角色包id |
| 39 | `detail_setting2` | `string` |  | 角色拆解详细人设2 |

---

## ods_db_vc_character_ogc_convert_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_ogc_convert_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | VC角色ID |
| 3 | `lofter_role_id` | `bigint` |  | Lofter角色ID(paperman侧反查原作者用) |
| 4 | `channel` | `bigint` |  | 角色来源渠道:1=APP来源 2=LOFTER来源 |
| 5 | `origin_user_id` | `bigint` |  | 转换前VC内部userId |
| 6 | `origin_creator_uid` | `bigint` |  | 转换前原作者lofter userCode |
| 7 | `ogc_blog_id` | `bigint` |  | mockRobotForPVE返回的官方账号blogId |
| 8 | `before_snapshot` | `string` |  | 转换前快照JSON(type/userId/creatorUid/auditStatus/publicScope/status/audienceType) |
| 9 | `operator` | `string` |  | 操作人admin email |
| 10 | `convert_status` | `int` |  | 转换状态:0=处理中 1=成功 |
| 11 | `fail_reason` | `string` |  | 失败原因 |
| 12 | `create_time` | `bigint` |  | 数据创建时间 |
| 13 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_character_package_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_package_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 角色包名称 |
| 3 | `ugc_categories` | `string` |  | ugc分类 |
| 4 | `ugc_front_tags` | `string` |  | ugc前台标签 |
| 5 | `ugc_backend_tags` | `string` |  | ugc后台标签 |
| 6 | `ogc_categories` | `string` |  | ogc分类 |
| 7 | `ogc_backend_tags` | `string` |  | ogc后台标签 |
| 8 | `character_file_url` | `string` |  | 上传角色文件地址 |
| 9 | `black_list` | `string` |  | 黑名单角色ID，多个用英文逗号分割 |
| 10 | `ext` | `string` |  | 额外配置 |
| 11 | `status` | `int` |  | 1:初始化；2:处理中；3:处理完成；-1:删除 |
| 12 | `create_time` | `bigint` |  | 数据创建时间 |
| 13 | `update_time` | `bigint` |  | 数据更新时间 |
| 14 | `version` | `bigint` |  | 版本号 |
| 15 | `ogc_front_tags` | `string` |  | ogc前台标签 |
| 16 | `ogc_tag_type` | `int` |  | ogc后台标签类型1: 后台标签，2：二级关系标签 |
| 17 | `ugc_tag_type` | `int` |  | ogc后台标签类型1: 后台标签，2：二级关系标签 |

---

## ods_db_vc_character_secret_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_secret_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 角色ID |
| 3 | `title` | `string` |  | 标题 |
| 4 | `content` | `string` |  | 内容 |
| 5 | `status` | `int` |  | 状态类型：1未生效，2生效，-1删除 |
| 6 | `sort_no` | `bigint` |  | 解锁顺序，从1开始 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `start_time` | `bigint` |  | 创建时间 |
| 9 | `end_time` | `bigint` |  | 创建时间 |
| 10 | `created_by` | `string` |  | 创建人邮箱 |
| 11 | `updated_by` | `string` |  | 更新人邮箱 |
| 12 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）含义见DataChannelEnum |
| 13 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_character_skin_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_skin_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 皮肤id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `name` | `string` |  | 皮肤名 |
| 4 | `img_url` | `string` |  | 皮肤图片(NOS) |
| 5 | `acquire_channel_infos` | `string` |  | 获取渠道列表 JSON |
| 6 | `sort` | `bigint` |  | 排序，默认与 id 一致，升序 |
| 7 | `is_deleted` | `int` |  | 0-未删除 1-已删除 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_character_tag_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_tag_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `tag_id` | `bigint` |  | 标签id |
| 4 | `status` | `int` |  | 关系表状态,1:未生效,2:生效中,-1:已删除 |
| 5 | `created_by` | `string` |  | 创建人邮箱 |
| 6 | `updated_by` | `string` |  | 更新人邮箱 |
| 7 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 8 | `ext` | `string` |  | 扩展字段 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_character_voice_pack_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_character_voice_pack_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `model_manufacturer` | `string` |  | 模型厂商 |
| 3 | `voice_type` | `int` |  | 声音模式：1-普通 2-混音 |
| 4 | `voice_config` | `string` |  | 声音配置：语速等 |
| 5 | `imbre_config` | `string` |  | 音色配置：混音配置等 |
| 6 | `status` | `int` |  | 状态，0: 未生效, 1: 生效, -1: 删除 |
| 7 | `ext` | `string` |  | 额外配置 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `voice_pack_name` | `string` |  | 音色包名称 |
| 11 | `txt` | `string` |  | 声音预览文本 |
| 12 | `remark` | `string` |  | 备注 |
| 13 | `voice_pack_sex` | `int` |  | 语音包性别 1男 2女 |
| 14 | `user_id` | `bigint` |  | 用户id |
| 15 | `app_config` | `string` |  | app配置 |
| 16 | `imbre_config2` | `string` |  | 2.0音色配置 |

---

## ods_db_vc_comment_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_comment_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `target_type` | `int` |  | 评论目标类型：1=角色动态评论 |
| 3 | `target_id` | `bigint` |  | 评论目标ID（如角色动态ID） |
| 4 | `actor_type` | `int` |  | 评论者类型：1=用户；2=角色 |
| 5 | `actor_id` | `bigint` |  | 评论者ID |
| 6 | `parent_id` | `bigint` |  | 父评论ID（0表示一级评论） |
| 7 | `content` | `string` |  | 评论内容 |
| 8 | `url` | `string` |  | 评论链接，多个用逗号隔开 |
| 9 | `like_count` | `bigint` |  | 点赞数 |
| 10 | `status` | `int` |  | 评论状态：1=仅自己可见；2=全体可见；-1=删除；-2=下架 |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 数据库更新时间 |
| 13 | `child_amount` | `bigint` |  | 子评论数量 |
| 14 | `data_from` | `int` |  | 数据来源 1app 2admin后台 |
| 15 | `reply_comment_id` | `bigint` |  | 回复评论id |

---

## ods_db_vc_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `config_type` | `int` |  | 配置类型：1:限免 |
| 3 | `content` | `string` |  | 配置内容 |
| 4 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 5 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 6 | `created_by` | `string` |  | 创建人邮箱 |
| 7 | `updated_by` | `string` |  | 更新人邮箱 |
| 8 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 9 | `create_time` | `bigint` |  | DB创建时间 |
| 10 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_content_analyze_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_content_analyze_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `model` | `string` |  | 模型名称 |
| 3 | `user_id` | `bigint` |  | 用户ID |
| 4 | `character_id` | `bigint` |  | 角色ID |
| 5 | `content` | `string` |  | json信息 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |
| 8 | `req_body` | `string` |  | 请求内容 |

---

## ods_db_vc_content_audit_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_content_audit_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `user_code` | `string` |  | 用户code |
| 4 | `link` | `string` |  | 分享链接 |
| 5 | `share_platform` | `int` |  | 平台名称 |
| 6 | `bind_account` | `string` |  | 绑定账号 |
| 7 | `activity_id` | `bigint` |  | 活动ID |
| 8 | `content_type` | `int` |  | 投稿类型 |
| 9 | `report_type` | `int` |  | 申报类型 |
| 10 | `audit_type` | `int` |  | 审核类型 |
| 11 | `audit_status` | `int` |  | 审核状态 |
| 12 | `audit_time` | `bigint` |  | 审核时间 |
| 13 | `audit_remark` | `string` |  | 审核备注 |
| 14 | `create_time` | `bigint` |  | DB创建时间 |
| 15 | `update_time` | `bigint` |  | DB更新时间 |
| 16 | `qq_account` | `string` |  | QQ账号 |
| 17 | `audit_submit_type` | `int` |  | 偶像审核提交类型 1.聊天内容分享 2.自创角色分享 3.优质投稿 |
| 18 | `coop_code` | `string` |  | 合作码 |
| 19 | `task_type` | `int` |  | 偶像任务类型 2.聊天内容分享 3.自创角色分享 4.聊天内容分享 |

---

## ods_db_vc_data_character_choose_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_data_character_choose_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 角色id |
| 3 | `status` | `int` |  | 状态,1:上架,0:下架 |
| 4 | `index_feed_status` | `int` |  | 是否加入发现页瀑布流,1:是,0:否 |
| 5 | `stranger_msg_status` | `int` |  | 是否加入陌生人消息,1:是,0:否 |
| 6 | `chat_recommend_status` | `int` |  | 是否加入聊天页推荐,1:是,0:否 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `type` | `int` |  | 0官方角色，1用户自由创建 |

---

## ods_db_vc_date_card_lottery_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_date_card_lottery_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `act_id` | `bigint` |  | 活动id/奖池id |
| 4 | `draw_type` | `int` |  | 抽奖类型: 1-1抽, 2-10抽 |
| 5 | `benefit_type` | `int` |  | 权益（优惠）类型: 1-新用户首抽免费, 2-新用户首10抽半价 |
| 6 | `draw_count` | `bigint` |  | 抽取次数 |
| 7 | `cost_cy_coin` | `bigint` |  | 消耗的虚拟币 |
| 8 | `sort_no` | `bigint` |  | 抽奖结束轮次 |
| 9 | `wait_guarantee_sort_no` | `bigint` |  | 未抽到保底的轮数 |
| 10 | `status` | `int` |  | 抽奖记录状态 |
| 11 | `create_time` | `bigint` |  | 数据创建时间 |
| 12 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_date_card_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_date_card_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 关联的角色id |
| 3 | `name` | `string` |  | 卡片名 |
| 4 | `card_desc` | `string` |  | 描述 |
| 5 | `prompt` | `string` |  | 卡片对应的prompt |
| 6 | `prompt_type` | `bigint` |  | prompt类型: 1-附加, 2-替换 |
| 7 | `img_url` | `string` |  | 原图链接 |
| 8 | `thumbnail` | `string` |  | 缩略图链接 |
| 9 | `level` | `string` |  | 等级: R、SR、SSR、SP |
| 10 | `max_grade` | `bigint` |  | 最大等级 |
| 11 | `acquire_channels` | `string` |  | 获取渠道（JSON） |
| 12 | `grade_infos` | `string` |  | 等级信息（JSON） |
| 13 | `skin_id` | `bigint` |  | 皮肤id |
| 14 | `open_remark` | `string` |  | 开场白 |
| 15 | `chat_tip` | `string` |  | 剧情转场文案 |
| 16 | `create_time` | `bigint` |  | 数据创建时间 |
| 17 | `update_time` | `bigint` |  | 数据更新时间 |
| 18 | `online` | `int` |  | 是否线上，1-线上，0-离线 |

---

## ods_db_vc_date_card_physical_exchange_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_date_card_physical_exchange_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `shipping_address` | `string` |  | 收货地址 |
| 3 | `recipient` | `string` |  | 收件人 |
| 4 | `contact_phone` | `string` |  | 联系电话 |
| 5 | `user_id` | `bigint` |  | 用户ID |
| 6 | `reward_id` | `bigint` |  | 实物奖励ID |
| 7 | `reward_name` | `string` |  | 实物名称 |
| 8 | `status` | `int` |  | 状态：1-待填写地址 2-待导出 3-已导出 |
| 9 | `create_time` | `bigint` |  | 数据创建时间 |
| 10 | `update_time` | `bigint` |  | 数据更新时间 |
| 11 | `type` | `int` |  | 状态：1-抽奖兑换 2.周边直售 3.周边抽奖 |
| 12 | `select_status` | `int` |  | 来源:1。不默认 2.默认 |

---

## ods_db_vc_date_card_reward_acquire_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_date_card_reward_acquire_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `act_id` | `bigint` |  | 活动id/奖池id |
| 4 | `lottery_record_id` | `bigint` |  | 抽奖记录id |
| 5 | `acquire_channel` | `int` |  | 获取渠道,1-抽奖,2-兑换 |
| 6 | `reward_id` | `bigint` |  | 奖品id |
| 7 | `reward_type` | `int` |  | 奖品类型:1-约会卡,2-实物 |
| 8 | `sort_no` | `bigint` |  | 该抽奖记录轮次 |
| 9 | `converted_fragment_count` | `bigint` |  | 转化成碎片的数量 |
| 10 | `create_time` | `bigint` |  | 数据创建时间 |
| 11 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_date_card_story_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_date_card_story_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `card_id` | `bigint` |  | 卡片id |
| 3 | `grade` | `bigint` |  | 卡片等级 |
| 4 | `story` | `string` |  | 故事内容 |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_delay_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_delay_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 任务id主键 |
| 2 | `task_type` | `int` |  | 任务类型：1 @角色回复任务 |
| 3 | `business_id` | `string` |  | 业务id |
| 4 | `trigger_time` | `bigint` |  | 定时的触发时间 |
| 5 | `status` | `int` |  | 任务执行状态 1:未发送,2:发送中,3:发送完成,4发送失败,-1删除 |
| 6 | `ext` | `string` |  | 扩展字段 |
| 7 | `create_time` | `bigint` |  | DB创建时间 |
| 8 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_device_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_device_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `device_id` | `string` |  | 设备ID |
| 3 | `device_uid` | `string` |  | 设备UID信息，非业务场景使用 |
| 4 | `user_id` | `bigint` |  | 账号ID |
| 5 | `client_type` | `string` |  | 客户端类型 |
| 6 | `app_version_name` | `string` |  | app版本名 |
| 7 | `app_version_code` | `string` |  | app版本code |
| 8 | `ext` | `string` |  | 扩展信息 |
| 9 | `create_time` | `bigint` |  | 数据创建时间 |
| 10 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_device_report_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_device_report_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `idfa` | `string` |  | ios投流唯一id |
| 4 | `imei` | `string` |  | imei |
| 5 | `android_id` | `string` |  | android_id |
| 6 | `oaid` | `string` |  | android投流唯一id |
| 7 | `platform` | `string` |  | 客户端类型 |
| 8 | `device_id` | `string` |  | 设备id |
| 9 | `user_agent` | `string` |  | 用户代理 |
| 10 | `accept_language` | `string` |  | 系统语言 |
| 11 | `device_manufacturer` | `string` |  | 设备制造商 |
| 12 | `device_model` | `string` |  | 设备型号 |
| 13 | `os_version` | `string` |  | 系统版本 |
| 14 | `app_version_name` | `string` |  | 应用名称 |
| 15 | `app_version_code` | `string` |  | 应用版本号 |
| 16 | `network_type` | `string` |  | 网络类型 |
| 17 | `timezone` | `string` |  | 时区 |
| 18 | `status` | `bigint` |  | 记录状态 |
| 19 | `create_time` | `bigint` |  | 创建时间 |
| 20 | `update_time` | `bigint` |  | 修改时间 |
| 21 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_dungeon_chat_backtrace_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_dungeon_chat_backtrace_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `prev_record_id` | `bigint` |  | 回溯锚点消息ID（用户回溯到的那条消息） |
| 3 | `backtrace_time` | `bigint` |  | 回溯时间点（毫秒时间戳） |
| 4 | `next_record_id` | `bigint` |  | 回溯后第一条新消息ID，0=尚无新对话 |
| 5 | `create_time` | `bigint` |  | 回溯操作发生时间（毫秒时间戳） |
| 6 | `user_id` | `bigint` |  | 用户ID |
| 7 | `biz_id` | `string` |  | 业务ID |
| 8 | `branch_id` | `bigint` |  | 存档ID |
| 9 | `from_record_id` | `bigint` |  | 回溯起点消息ID（回溯操作发生时最新的一条消息ID，即被丢弃区间的起点） |
| 10 | `character_id` | `bigint` |  | 角色ID |

---

## ods_db_vc_event_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_event_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 30 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `event_type` | `int` |  | 事件类型(1次元事件) |
| 3 | `sub_event_type` | `int` |  | 事件子类型，例次元事件分为(1预埋事件、2推送事件)，仅event_type为1时有效 |
| 4 | `event_form` | `int` |  | 子事件的推送形式 (1角色推送2链接推送) |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `title` | `string` |  | 事件标题 |
| 7 | `description` | `string` |  | 事件描述(即副标题) |
| 8 | `content` | `string` |  | 事件内容(1000字以内) |
| 9 | `option_reply_config` | `string` |  | 选项回复配置(选项+用户输出+角色输出) |
| 10 | `jump_url` | `string` |  | 跳转链接 |
| 11 | `jump_type` | `int` |  | 跳转类型 (1h5 2rnpage 3nativeChat) |
| 12 | `trigger_condition_type` | `int` |  | 关联触发条件1关联角色2关联开场白 @EventTriggerTypeEnum |
| 13 | `trigger_condition_config` | `string` |  | 触发条件(仅关联触发条件为1时有效) |
| 14 | `play_type` | `int` |  | 任务玩法类型 (0不支持 1任务玩法 2数值玩法) |
| 15 | `play_config` | `string` |  | 玩法配置 |
| 16 | `send_obj_type` | `int` |  | 发放类型 0.未指定 1.全局用户 2.人群包用户(仅sub_event_type 为2推送事件有效) |
| 17 | `obj_id` | `string` |  | 发放的对象id(仅sub_event_type 为2推送事件有效) |
| 18 | `effective_start_time` | `bigint` |  | 生效开始时间,(仅推送事件有效) |
| 19 | `created_by` | `string` |  | 创建人 |
| 20 | `updated_by` | `string` |  | 更新人 |
| 21 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 22 | `create_time` | `bigint` |  | 创建时间 |
| 23 | `update_time` | `bigint` |  | 更新时间 |
| 24 | `status` | `int` |  | 活动状态：1未生效，2已生效，3下架，-1删除 |
| 25 | `ext` | `string` |  | 扩展字段 |
| 26 | `activity_type` | `int` |  | 任务玩法类型 (0不支持活动 1支持活动) |
| 27 | `activity_config` | `string` |  | 玩法配置 |
| 28 | `tag` | `string` |  | 标签 |
| 29 | `tag_type` | `int` |  | 事件标签类型 详见@EventTagTypeEnum |
| 30 | `sign` | `int` |  | 测试标识1正式 2测试 |

---

## ods_db_vc_event_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_event_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 9.1G |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `gmt_create` | `bigint` |  | 创建时间 |
| 3 | `gmt_modify` | `bigint` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `character_id` | `bigint` |  | 虚拟人id |
| 6 | `type` | `string` |  | 事件类型，邀约事件、思念事件、破冰事件...... |
| 7 | `trigger_time` | `bigint` |  | 事件触发时间 |
| 8 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_front_tag_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_front_tag_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 名称 |
| 3 | `status` | `int` |  | 1：上线；0：下线 |
| 4 | `create_time` | `bigint` |  | 数据创建时间 |
| 5 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_game_banner_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_banner_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `title` | `string` |  | 标题 |
| 3 | `description` | `string` |  | 描述（最多2行） |
| 4 | `cover` | `string` |  | 封面图 NOS objectName（横向大图） |
| 5 | `start_time` | `bigint` |  | 开始时间（毫秒时间戳） |
| 6 | `end_time` | `bigint` |  | 结束时间（毫秒时间戳） |
| 7 | `status` | `bigint` |  | 状态：0=下线 10=上线 |
| 8 | `top_order` | `bigint` |  | 置顶权重（值越大越靠前） |
| 9 | `link_url` | `string` |  | 跳转链接（目标URL或页面路由） |
| 10 | `scene` | `bigint` |  | 场景：1=首页banner 2=活动位 |
| 11 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 12 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 13 | `db_update_time` | `bigint` |  | 数据库更新时间 |

---

## ods_db_vc_game_favorite_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_favorite_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `game_id` | `bigint` |  | 作品ID |
| 4 | `create_time` | `bigint` |  | 创建时间（毫秒） |
| 5 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_game_follow_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_follow_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 关注者ID |
| 3 | `follow_user_id` | `bigint` |  | 被关注者ID |
| 4 | `create_time` | `bigint` |  | 创建时间（毫秒） |
| 5 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_game_hot_recommend_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_hot_recommend_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `position` | `bigint` |  | 坑位序号 1-20 |
| 3 | `game_id` | `bigint` |  | 关联游戏 ID |
| 4 | `status` | `int` |  | 状态：1=上架 0=下架 -1=已删除 |
| 5 | `start_time` | `bigint` |  | 生效时间（毫秒时间戳） |
| 6 | `end_time` | `bigint` |  | 失效时间（毫秒时间戳） |
| 7 | `operator` | `string` |  | 操作人 |
| 8 | `create_time` | `bigint` |  | NULL |
| 9 | `update_time` | `bigint` |  | NULL |
| 10 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_game_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID（业务 gameId） |
| 2 | `user_id` | `bigint` |  | 创作者 userId |
| 3 | `status` | `int` |  | 状态：0=草稿 10=已发布 20=已下线 |
| 4 | `audit_status` | `int` |  | 审核状态：0=待审核 10=审核通过 20=审核不通过 |
| 5 | `title` | `string` |  | 游戏名称（最长50字符） |
| 6 | `tags` | `string` |  | 游戏标签（JSON数组，最多5个），如：["太空","冒险"] |
| 7 | `summary` | `string` |  | 一句话简介（≤300字） |
| 8 | `description` | `string` |  | 关于此游戏（Markdown，≤1000字） |
| 9 | `controls` | `string` |  | 操作说明（≤500字） |
| 10 | `cover` | `string` |  | 封面图 NOS objectName |
| 11 | `game_file` | `string` |  | 游戏文件 NOS objectName |
| 12 | `assets` | `string` |  | 游戏资源文件地址 JSON 数组，客户端透传 |
| 13 | `screen` | `int` |  | 屏幕方向：1=横屏 2=竖屏 |
| 14 | `price_type` | `int` |  | 付费类型：1=免费 2=购买后可玩 |
| 15 | `price` | `bigint` |  | 购买所需积分（price_type=2 时有效） |
| 16 | `play_count` | `bigint` |  | 游玩次数 |
| 17 | `collect_count` | `bigint` |  | 收藏数 |
| 18 | `like_count` | `bigint` |  | 点赞数 |
| 19 | `score` | `bigint` |  | 评分（×10，如 49 表示 4.9） |
| 20 | `top_time` | `bigint` |  | 置顶时间（毫秒时间戳，NULL=未置顶） |
| 21 | `publish_time` | `bigint` |  | 发布时间（毫秒时间戳） |
| 22 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 23 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 24 | `db_update_time` | `bigint` |  | DB自动更新时间 |
| 25 | `visibility` | `int` |  | 可见性：1=公开 2=私有 |

---

## ods_db_vc_game_like_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_like_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 点赞用户ID |
| 3 | `game_id` | `bigint` |  | 被点赞作品ID |
| 4 | `create_time` | `bigint` |  | 创建时间（毫秒） |
| 5 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_game_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_ids` | `string` |  | 用户ID列表 |
| 3 | `character_ids` | `string` |  | 角色ID列表 |
| 4 | `status` | `int` |  | 状态信息 0-初始化中 1-已开启-进行中 2-已完成-已结束 |
| 5 | `type` | `string` |  | 类型，游戏记录类型 |
| 6 | `target_id` | `string` |  | 目标id，关联其他记录用的 |
| 7 | `start_time` | `bigint` |  | 开始时间（时间戳） |
| 8 | `end_time` | `bigint` |  | 结束时间（时间戳） |
| 9 | `create_time` | `bigint` |  | 数据创建时间 |
| 10 | `update_time` | `bigint` |  | 数据更新时间 |
| 11 | `ext` | `string` |  | 扩展信息 |
| 12 | `version` | `bigint` |  | 版本号，主要用于ext |
| 13 | `game_type` | `string` |  | 游戏类型 |

---

## ods_db_vc_game_relation_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_relation_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `ori_id` | `bigint` |  | 原id，n:1中的n |
| 3 | `target_id` | `bigint` |  | 目标id，n:1中的1 |
| 4 | `type` | `bigint` |  | 类型 |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_game_room_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_room_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_ids` | `string` |  | 用户ID列表，逗号分隔，例如：123,456,789 |
| 3 | `character_ids` | `string` |  | 角色ID列表，逗号分隔，例如：1,2,3 |
| 4 | `game_type` | `string` |  | 游戏类型，例如：guess_multi（蒙面猜） |
| 5 | `public_status` | `int` |  | 公开状态：1-公开，2-私有 |
| 6 | `password` | `string` |  | 房间密码（私有房间使用） |
| 7 | `status` | `int` |  | 房间状态：0-关闭，1-正常 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `ext` | `string` |  | 扩展字段，JSON格式 |
| 11 | `creator_user_id` | `bigint` |  | 创建者id |
| 12 | `master_id` | `bigint` |  | 房主id |

---

## ods_db_vc_game_user_account_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_user_account_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `phone` | `string` |  | 手机号 |
| 3 | `yd_account` | `string` |  | URS账号全称，格式：{phone}@yd.163.com |
| 4 | `status` | `int` |  | 账号状态：1=正常 2=封禁 3=注销 |
| 5 | `channel` | `int` |  | 渠道：1=App 2=Lofter |
| 6 | `device_id` | `string` |  | 注册来源设备ID |
| 7 | `user_nick` | `string` |  | 用户昵称 |
| 8 | `avatar` | `string` |  | 用户头像 |
| 9 | `signature` | `string` |  | 个性签名 |
| 10 | `gender` | `int` |  | 性别：1=男 2=女 3=保密 |
| 11 | `banner_image` | `string` |  | 封面图 NOS objectName |
| 12 | `social_links` | `string` |  | 社交媒体链接（JSON，key=平台名，value=链接） |
| 13 | `ext` | `string` |  | 扩展信息（JSON） |
| 14 | `follow_count` | `bigint` |  | 关注数 |
| 15 | `fans_count` | `bigint` |  | 粉丝数 |
| 16 | `total_play_count` | `bigint` |  | 总游玩次数（名下所有作品 play_count 之和） |
| 17 | `last_login_time` | `bigint` |  | 最后登录时间（毫秒时间戳） |
| 18 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 19 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 20 | `db_update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_game_user_lofter_bind_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_user_lofter_bind_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 本系统用户ID（均衡字段 BF） |
| 3 | `lofter_id` | `bigint` |  | Lofter 用户唯一标识（blogId） |
| 4 | `status` | `int` |  | 状态：1=已绑定，0=已解绑 |
| 5 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 6 | `db_update_time` | `bigint` |  | 数据库更新时间 |

---

## ods_db_vc_game_workshop_draft_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_game_workshop_draft_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 创作者 userId（BF 分片字段） |
| 3 | `game_type` | `string` |  | 游戏类型：interactive/management/casual |
| 4 | `name` | `string` |  | 草稿名称 |
| 5 | `content_key` | `string` |  | NOS object key |
| 6 | `has_code` | `int` |  | 是否含生成代码：0=否 1=是 |
| 7 | `is_deleted` | `int` |  | 软删除：0=正常 1=已删除 |
| 8 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 9 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 10 | `db_update_time` | `bigint` |  | 数据库更新时间 |

---

## ods_db_vc_gift_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_gift_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `name` | `string` |  | 名称 |
| 5 | `image_url` | `string` |  | 图片地址 |
| 6 | `default_message` | `string` |  | AI默认消息 |
| 7 | `description` | `string` |  | 简介 |
| 8 | `character_ids` | `string` |  | 礼物适用角色 |
| 9 | `prompt` | `string` |  | prompt |
| 10 | `ext` | `string` |  | 扩展字段 |
| 11 | `scene` | `bigint` |  | 类型：1.普通场景 2.生日月活动礼物 |
| 12 | `weight` | `bigint` |  | 权重，数值越大优先级越高 |
| 13 | `status` | `int` |  | 状态：1.待上架 2.已上架 3.禁止购买 4.删除 |

---

## ods_db_vc_guide_card_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_guide_card_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `content` | `string` |  | 消息内容 |
| 4 | `character_id` | `bigint` |  | 角色id |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |
| 7 | `device_id` | `string` |  | 设备id |
| 8 | `status` | `int` |  | 0:未读，1：已读 |

---

## ods_db_vc_id_auth_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_id_auth_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `card_no` | `string` |  | 身份证号（加密） |
| 3 | `name` | `string` |  | 姓名 |
| 4 | `ext` | `string` |  | 扩展信息 |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_invitation_reward_records_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_invitation_reward_records_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `invited_user_id` | `bigint` |  | 受邀请人id |
| 4 | `type` | `bigint` |  | 邀请奖励类型 1.等级奖励 2.返利 |
| 5 | `grade` | `bigint` |  | 邀请等级奖励 |
| 6 | `rebate` | `bigint` |  | 返利奖励内容 |
| 7 | `status` | `bigint` |  | 领取状态 2.待领取 3.已领取 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_invite_code_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_invite_code_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `user_id` | `bigint` |  | BF，邀请人 userId |
| 3 | `code` | `string` |  | 8位大写字母+数字邀请码 |
| 4 | `invite_count` | `bigint` |  | 累计拉新人数 |
| 5 | `create_time` | `bigint` |  | NULL |
| 6 | `update_time` | `bigint` |  | NULL |
| 7 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_invite_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_invite_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `inviter_user_id` | `bigint` |  | 邀请人 userId |
| 3 | `invitee_user_id` | `bigint` |  | BF，受邀人 userId |
| 4 | `code` | `string` |  | 使用的邀请码 |
| 5 | `bind_time` | `bigint` |  | 绑定时间戳 |
| 6 | `create_time` | `bigint` |  | NULL |
| 7 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_item_group_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_item_group_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `type` | `int` |  | 分组类型，1 聊天礼包 ，2 聊天档位，3 琉光礼包，4 琉光档位，5 VIP礼包 |
| 3 | `platform` | `int` |  | 平台：1=ios、2=android |
| 4 | `item_id` | `bigint` |  | 礼包id |
| 5 | `first_item_id` | `bigint` |  | 首次购买替换礼包id |
| 6 | `weight` | `bigint` |  | 权重 |
| 7 | `config` | `string` |  | 配置信息 |
| 8 | `status` | `int` |  | 状态 |
| 9 | `ext` | `string` |  | 扩展字段 |
| 10 | `created_by` | `string` |  | 创建人邮箱 |
| 11 | `updated_by` | `string` |  | 更新人邮箱 |
| 12 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 13 | `create_time` | `bigint` |  | 数据创建时间 |
| 14 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_item_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_item_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 商品ID |
| 2 | `platform` | `int` |  | 平台：1=ios、2=android |
| 3 | `pay_item_code` | `string` |  | 支付商品code |
| 4 | `item_name` | `string` |  | 商品名称 |
| 5 | `item_title` | `string` |  | 商品title |
| 6 | `use_note` | `string` |  | 使用说明 |
| 7 | `buy_note` | `string` |  | 购买说明 |
| 8 | `limit_type` | `int` |  | 购买限制：1无限制；2新人限购次数；3天限制；4自然周；5自然月 |
| 9 | `limit_count` | `bigint` |  | 购买限制次数 |
| 10 | `original_price` | `double` |  | 原价 |
| 11 | `discount_price` | `double` |  | 折扣价 |
| 12 | `currency_code` | `string` |  | 货币code，默认CNY |
| 13 | `image` | `string` |  | 商品图 |
| 14 | `display_quantity` | `bigint` |  | 显示数量 |
| 15 | `strike_quantity` | `bigint` |  | 划线数量 |
| 16 | `item_config` | `string` |  | 商品配置 |
| 17 | `status` | `int` |  | 商品状态：-1-删除，1-上架，2=下架 |
| 18 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 19 | `create_time` | `bigint` |  | 创建时间 |
| 20 | `update_time` | `bigint` |  | 更新时间 |
| 21 | `pay_title` | `string` |  | 支付标题 |
| 22 | `pay_icon` | `string` |  | 商品支付图标 |
| 23 | `internal_name` | `string` |  | 商品内部标识名 |
| 24 | `sub_type` | `int` |  | 商品订阅类型：0=普通消耗商品，1=月，2=季度，3=年 |
| 25 | `created_by` | `string` |  | 创建人邮箱 |
| 26 | `updated_by` | `string` |  | 更新人邮箱 |
| 27 | `item_type` | `int` |  | 礼包类型：1.聊天礼包2.VIP礼包3.能量奖励4.琉光奖励5.忘忧草奖励6.琉光礼包 |

---

## ods_db_vc_jump_link_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_jump_link_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `title` | `string` |  | 标题 |
| 3 | `sub_title` | `string` |  | 子标题 |
| 4 | `image_url` | `string` |  | 图片URL |
| 5 | `jump_url` | `string` |  | 跳转链接 |
| 6 | `jump_type` | `int` |  | 跳转类型 1.h5 2.rnpage 3.nativeChat |
| 7 | `scene_type` | `int` |  | 业务场景型 1banner位 2金刚位 |
| 8 | `weight` | `bigint` |  | 权重 |
| 9 | `start_time` | `bigint` |  | 开始时间 |
| 10 | `end_time` | `bigint` |  | 结束时间 |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 更新时间 |
| 13 | `created_by` | `string` |  | 创建人邮箱 |
| 14 | `updated_by` | `string` |  | 更新人邮箱 |
| 15 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=h5 |
| 16 | `status` | `int` |  | 状态：1生效（默认）-1=失效（删除） |
| 17 | `platform` | `int` |  | 客户端类型 1.ios 2.android  99.all |
| 18 | `version` | `string` |  | 版本号 |
| 19 | `ext` | `string` |  | 扩展信息 |
| 20 | `pack_id` | `bigint` |  | 人群包ID |
| 21 | `description` | `string` |  | 描述 |
| 22 | `channel_id` | `bigint` |  | 频道 |

---

## ods_db_vc_like_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_like_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `actor_type` | `int` |  | 点赞者类型：1=用户；2=虚拟角色 |
| 3 | `actor_id` | `bigint` |  | 点赞者ID |
| 4 | `target_type` | `int` |  | 点赞目标类型：1=动态；2=评论 |
| 5 | `target_id` | `bigint` |  | 点赞目标ID |
| 6 | `status` | `int` |  | 点赞状态：1=喜欢；0=不喜欢 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 记录更新时间 |

---

## ods_db_vc_lofter_group_member_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_lofter_group_member_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `team_id` | `bigint` |  | 群Id |
| 4 | `create_time` | `bigint` |  | 数据创建时间 |
| 5 | `update_time` | `bigint` |  | 数据更新时间 |
| 6 | `status` | `int` |  | 状态，0：失效，1：生效 |
| 7 | `index_key` | `string` |  | 索引字段 |

---

## ods_db_vc_lofter_group_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_lofter_group_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 创建者ID |
| 3 | `team_id` | `bigint` |  | 群Id |
| 4 | `seq` | `bigint` |  | 创建的第几个群 |
| 5 | `index_key` | `string` |  | 索引字段 |
| 6 | `scene` | `bigint` |  | 建群场景：0：模拟器官方大群，1: 模拟器小群 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |
| 9 | `status` | `int` |  | 状态，0：失效1：生效 |

---

## ods_db_vc_log_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_log_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `status` | `int` |  | 上报状态 |
| 4 | `device_id` | `string` |  | 设备ID |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |
| 7 | `start_date` | `string` |  | 开始日期 |
| 8 | `end_date` | `string` |  | 结束日期 |

---

## ods_db_vc_log_upload_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_log_upload_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `task_id` | `bigint` |  | 任务ID |
| 4 | `version_code` | `bigint` |  | 版本号 |
| 5 | `device_id` | `string` |  | 设备ID |
| 6 | `platform` | `string` |  | 平台 |
| 7 | `url` | `string` |  | 文件地址 |
| 8 | `create_time` | `bigint` |  | 数据创建时间 |
| 9 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_main_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_main_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 任务id主键 |
| 2 | `send_object_type` | `int` |  | 用户类型：2:表示人群包 |
| 3 | `send_object_id` | `string` |  | 用户id/人群包id |
| 4 | `task_type` | `int` |  | 任务类型：1短信任务 |
| 5 | `content` | `string` |  | 任务内容 |
| 6 | `status` | `int` |  | 奖励的发放状态,1:未发送,2:发送中,3:发送完成,4发送失败,5终止发送,-1删除 |
| 7 | `trigger_type` | `int` |  | 任务类型：1:立即发送，2:定时单次发送 |
| 8 | `trigger_time` | `bigint` |  | 定时的触发时间(只有trigger_type=2时生效) |
| 9 | `success_count` | `bigint` |  | 成功数 |
| 10 | `lose_count` | `bigint` |  | 失败数 |
| 11 | `task_count` | `bigint` |  | 任务总数 |
| 12 | `send_start_time` | `bigint` |  | 任务开始执行时间 |
| 13 | `send_end_time` | `bigint` |  | 任务执行结束生效的时间 |
| 14 | `ext` | `string` |  | 扩展字段 |
| 15 | `created_by` | `string` |  | 创建人邮箱 |
| 16 | `updated_by` | `string` |  | 更新人邮箱 |
| 17 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 18 | `create_time` | `bigint` |  | DB创建时间 |
| 19 | `update_time` | `bigint` |  | DB更新时间 |
| 20 | `business_id` | `string` |  | 业务id |
| 21 | `character_id` | `bigint` |  | 角色id |

---

## ods_db_vc_moment_read_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_moment_read_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `character_id` | `bigint` |  | 虚拟角色ID |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `moment_id` | `bigint` |  | 动态id |
| 5 | `ext` | `string` |  | 扩展字段，存JSON |
| 6 | `create_time` | `bigint` |  | 数据创建时间 |
| 7 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_order_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_order_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 订单ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `item_id` | `bigint` |  | 商品ID |
| 4 | `pay_order_code` | `string` |  | 支付订单code |
| 5 | `reward_type` | `int` |  | 奖励类型：0未知，1能量，2道具，3权益 |
| 6 | `reward_id` | `bigint` |  | 奖励ID，rewardType为道具，则此id为道具id，为能量，则此id为能量code |
| 7 | `pay_time` | `bigint` |  | 订单支付完成时间 |
| 8 | `order_price` | `double` |  | 订单价格 |
| 9 | `order_status` | `int` |  | 订单状态：1-待支付（初始态）；2-已支付；3-支付取消；4-已发货； |
| 10 | `ext` | `string` |  | 订单扩展信息 |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 更新时间 |
| 13 | `pay_channel` | `string` |  | 支付渠道：如微信、支付宝、银行卡等 |

---

## ods_db_vc_periphery_act_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_periphery_act_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `act_id` | `bigint` |  | 活动ID（业务主键，与 PeripheryModel.actId 对齐） |
| 3 | `act_name` | `string` |  | 活动名称（运营标识用） |
| 4 | `act_type` | `int` |  | 1=周边抽奖(福袋) 2=周边直售（与 PeripheryModel.type 完全对齐） |
| 5 | `channel` | `int` |  | 1=APP 2=站内 |
| 6 | `gift_id` | `bigint` |  | 首购赠品ID，福袋类型可空 |
| 7 | `gift_name` | `string` |  | GiftModel.giftName |
| 8 | `gift_total_num` | `bigint` |  | GiftModel.totalNum |
| 9 | `gift_give_type` | `int` |  | GiftModel.giveType: 1=随便购买都赠送 2=指定商品赠送 |
| 10 | `gift_reward_content` | `string` |  | GiftModel.rewardContent |
| 11 | `status` | `int` |  | 0=禁用 1=启用 |
| 12 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 13 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 14 | `current_period_no` | `bigint` |  | 当前对外的期号（C 端按此过滤；运营手动切换） |
| 15 | `gift_enabled` | `int` |  | 首购赠品开关：1=开启发放(默认) 0=关闭 |

---

## ods_db_vc_periphery_lottery_records_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_periphery_lottery_records_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `product_id` | `string` |  | 商品id |
| 3 | `order_id` | `bigint` |  | 订单id |
| 4 | `status` | `bigint` |  | 状态 1.抽奖中，2.抽奖完成 3.抽奖失败 |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |
| 7 | `user_id` | `bigint` |  | 用户id |

---

## ods_db_vc_periphery_order_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_periphery_order_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `product_id` | `bigint` |  | 商品id |
| 3 | `product_name` | `string` |  | 商品名称 |
| 4 | `order_id` | `bigint` |  | 订单id |
| 5 | `payment_amount` | `double` |  | 支付金额 |
| 6 | `product_count` | `bigint` |  | 商品数量 |
| 7 | `address_id` | `bigint` |  | 地址id |
| 8 | `user_id` | `bigint` |  | 用户id |
| 9 | `source` | `int` |  | 来源: 1.app 2.lofter |
| 10 | `type` | `int` |  | 类型 1.抽奖 2.周边直售 |
| 11 | `gift_id` | `bigint` |  | 赠送奖品id |
| 12 | `create_time` | `bigint` |  | 数据创建时间 |
| 13 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_periphery_product_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_periphery_product_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `act_id` | `bigint` |  | 关联 vc_periphery_act.act_id |
| 3 | `product_id` | `bigint` |  | 商品业务ID（新建时由系统分配 = id；历史 Apollo 数据保留原值） |
| 4 | `pool_id` | `string` |  | 福袋奖池ID（如 periphery1030），直售为 NULL |
| 5 | `product_name` | `string` |  | PeripheryProductModel.productName |
| 6 | `product_img` | `string` |  | PeripheryProductModel.productImg（详情大图） |
| 7 | `product_price` | `double` |  | PeripheryProductModel.productPrice（元，展示用） |
| 8 | `product_desc` | `string` |  | PeripheryProductModel.productDesc |
| 9 | `pay_icon` | `string` |  | paperman.icon（支付列表小图） |
| 10 | `pay_amount` | `double` |  | 金额，单位：元 |
| 11 | `pay_type` | `int` |  | paperman.type，当前固定 0 |
| 12 | `stamina` | `bigint` |  | paperman.stamina（福袋扣体力，直售为 NULL） |
| 13 | `start_time` | `bigint` |  | 开售时间毫秒时间戳 |
| 14 | `end_time` | `bigint` |  | 下架时间毫秒时间戳 |
| 15 | `sort` | `bigint` |  | 展示排序，升序 |
| 16 | `status` | `int` |  | 0=下架 1=上架 |
| 17 | `create_time` | `bigint` |  |  |
| 18 | `update_time` | `bigint` |  |  |
| 19 | `period_no` | `bigint` |  | 所属期号（与 act.current_period_no 配合过滤） |

---

## ods_db_vc_plot_summary_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_plot_summary_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `record_time` | `bigint` |  | 触发总结的聊天记录创建时间（毫秒时间戳） |
| 4 | `summary` | `string` |  | AI 生成的摘要内容 |
| 5 | `create_time` | `bigint` |  | 总结创建时间（毫秒时间戳） |
| 6 | `character_id` | `bigint` |  | 角色ID |
| 7 | `branch_id` | `bigint` |  | 存档ID |
| 8 | `scene` | `string` |  | 场景枚举名（PLOT_SIMULATOR_CHAT / PLOT_SIMULATOR_DUPLICATE_CHAT） |
| 9 | `record_id` | `bigint` |  | 记录Id |

---

## ods_db_vc_prompt_test_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_prompt_test_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `task_name` | `string` |  | 任务名称 |
| 3 | `scene_type` | `int` |  | 1.任务类型 2.聊天类型 |
| 4 | `model_param` | `string` |  | 模型参数 |
| 5 | `system_prompt` | `string` |  | 系统提示词 |
| 6 | `put_template` | `string` |  | 输出模板 |
| 7 | `user_prompt` | `string` |  | 用户提示词 |
| 8 | `customized_prompt` | `string` |  | 自定义提示词 |
| 9 | `prompt_param` | `string` |  | 提示词参数 |
| 10 | `customized_model` | `string` |  | 自定义模型 |
| 11 | `customized_memory` | `string` |  | 自定义记忆 |
| 12 | `memory_param` | `string` |  | 记忆参数 |
| 13 | `sample_param` | `string` |  | 样本参数 |
| 14 | `sample_date` | `string` |  | 样本数据 |
| 15 | `status` | `int` |  | 1.有效 2.无效 |
| 16 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 17 | `create_time` | `bigint` |  | 创建时间 |
| 18 | `update_time` | `bigint` |  | 更新时间 |
| 19 | `created_by` | `string` |  | 创建人邮箱 |
| 20 | `updated_by` | `string` |  | 更新人邮箱 |
| 21 | `ext` | `string` |  | 额外配置 |

---

## ods_db_vc_prompt_test_task_result_detail_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_prompt_test_task_result_detail_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `task_result_id` | `bigint` |  | 任务结果id |
| 3 | `model_param` | `string` |  | 模型参数 |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `character_id` | `bigint` |  | 角色id |
| 6 | `chat_history` | `string` |  | 聊天记录 |
| 7 | `model_result` | `string` |  | 模型测试结果 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `created_by` | `string` |  | 创建人邮箱 |
| 11 | `updated_by` | `string` |  | 更新人邮箱 |
| 12 | `ext` | `string` |  | 额外配置 |

---

## ods_db_vc_prompt_test_task_result_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_prompt_test_task_result_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `task_id` | `bigint` |  | 任务id |
| 3 | `task_batch` | `bigint` |  | 任务批次 |
| 4 | `model_param` | `string` |  | 模型参数 |
| 5 | `prompt_param` | `string` |  | 提示词参数 |
| 6 | `system_prompt` | `string` |  | 系统提示词 |
| 7 | `put_template` | `string` |  | 输出模板 |
| 8 | `user_prompt` | `string` |  | 用户提示词 |
| 9 | `customized_memory` | `string` |  | 自定义记忆 |
| 10 | `memory_param` | `string` |  | 记忆参数 |
| 11 | `sample_param` | `string` |  | 样本参数 |
| 12 | `sample_date` | `string` |  | 样本数据 |
| 13 | `create_time` | `bigint` |  | 创建时间 |
| 14 | `update_time` | `bigint` |  | 更新时间 |
| 15 | `created_by` | `string` |  | 创建人邮箱 |
| 16 | `updated_by` | `string` |  | 更新人邮箱 |
| 17 | `ext` | `string` |  | 额外配置 |

---

## ods_db_vc_props_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_props_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 道具名称 |
| 3 | `props_type` | `bigint` |  | 道具类型 |
| 4 | `description` | `string` |  | 道具描述 |
| 5 | `setting` | `string` |  | 道具配置 |
| 6 | `status` | `int` |  | 数据状态，1有效，-1删除 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_push_message_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_push_message_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 46.5G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 46.5G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `uq_id` | `bigint` |  | 记录表唯一ID |
| 5 | `biz_id` | `string` |  | 业务唯一ID |
| 6 | `scene` | `string` |  | 业务场景 |
| 7 | `task_id` | `bigint` |  | 任务ID -1默认没有任务ID |
| 8 | `sub_task_id` | `bigint` |  | 子任务ID -1默认值，默认无该值 |
| 9 | `user_id` | `bigint` |  | 用户ID |
| 10 | `content` | `string` |  | 推送内容，JSON格式 |
| 11 | `send_status` | `int` |  | 发送状态 10：待发送 20：发送中  30：发送完成 40：发送失败-取消发送 41：发送失败-紧急撤销 42：发送失败-业务异常  -1：已删除 |
| 12 | `read_status` | `int` |  | 是否已读 0-未读 1-已读 |
| 13 | `receive_status` | `int` |  | 是否已到达 0-未到达，1-已到达 |
| 14 | `send_time` | `bigint` |  | 发送时间 |
| 15 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_rank_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_rank_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 榜单名称 |
| 3 | `pack_id` | `bigint` |  | 角色包id |
| 4 | `simulator_id` | `string` |  | 模拟器id |
| 5 | `type` | `int` |  | 榜单类型 见:RankTypeEnum |
| 6 | `target_type` | `int` |  | 作用的对象类型 1:角色榜；2:用户榜 |
| 7 | `icon` | `string` |  | 艺术字 |
| 8 | `sub_title` | `string` |  | 副标题 |
| 9 | `order_field` | `int` |  | 排序字段 1:GMV；2聊天轮数 3消耗能量值 |
| 10 | `order_type` | `int` |  | 排序方式 1:累加型；2最高值 |
| 11 | `sql_clause` | `string` |  | sql语句 |
| 12 | `value_render_config` | `string` |  | 榜单数值配置 |
| 13 | `img_url` | `string` |  | 榜单底图 |
| 14 | `status` | `int` |  | 1:生效；2：失效 |
| 15 | `start_time` | `bigint` |  | 周期开始时间 |
| 16 | `end_time` | `bigint` |  | 周期结束时间 |
| 17 | `create_time` | `bigint` |  | 数据创建时间 |
| 18 | `update_time` | `bigint` |  | 数据更新时间 |
| 19 | `rule_route` | `string` |  | 规则链接地址 |
| 20 | `version` | `bigint` |  | 版本 |
| 21 | `ext` | `string` |  | 额外配置 |
| 22 | `limit_cnt` | `bigint` |  | 榜单排名限额 |

---

## ods_db_vc_rank_data_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_rank_data_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `rank_id` | `bigint` |  | 榜单ID |
| 3 | `rank_value` | `bigint` |  | 榜单排名 |
| 4 | `target_id` | `bigint` |  | 对象ID |
| 5 | `target_type` | `bigint` |  | 对象类型 |
| 6 | `create_time` | `bigint` |  | 数据创建时间 |
| 7 | `update_time` | `bigint` |  | 数据更新时间 |
| 8 | `status` | `int` |  | 状态，0：失效1：生效 |
| 9 | `version` | `bigint` |  | 版本 |
| 10 | `rank_order` | `bigint` |  | 排序值 |

---

## ods_db_vc_rank_list_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_rank_list_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `first_id` | `bigint` |  | 第一维度ID |
| 5 | `second_id` | `bigint` |  | 第二维度ID |
| 6 | `scene` | `string` |  | 场景类型 |
| 7 | `target_value` | `bigint` |  | 目标值 |
| 8 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_redeem_code_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_redeem_code_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `type` | `int` |  | 1兑换码2邀请码 |
| 3 | `code` | `string` |  | 兑换码字符 |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `single_receive_counts` | `int` |  | 单人限领次数(默认1次) |
| 6 | `total_receive_counts` | `bigint` |  | 总限领次数(默认无上限) |
| 7 | `require_new_user` | `int` |  | 是否要求新用户(1需要，2不需要) |
| 8 | `reward` | `string` |  | 奖励规则: 奖励类型、奖励id、数量关系、有效期单位、有效期数值 |
| 9 | `status` | `int` |  | 状态：1草稿,2已保存,3取消生效,-1删除 |
| 10 | `effective_start_time` | `bigint` |  | 兑换码生效开始时间 |
| 11 | `effective_end_time` | `bigint` |  | 兑换码失效时间 |
| 12 | `ext` | `string` |  | 扩展字段 |
| 13 | `created_by` | `string` |  | 创建人邮箱 |
| 14 | `updated_by` | `string` |  | 更新人邮箱 |
| 15 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 16 | `create_time` | `bigint` |  | DB创建时间 |
| 17 | `update_time` | `bigint` |  | DB更新时间 |
| 18 | `invite_total` | `bigint` |  | type=2时有用，作为邀请码绑定总人数 |
| 19 | `reward_count` | `bigint` |  | type=2时有用，该用户领取邀请码奖励的人数(应为10的倍数) |
| 20 | `jump_url` | `string` |  | 跳转链接 |
| 21 | `jump_type` | `int` |  | 跳转类型 1.h5 2.RNPage 3.nativeChat |
| 22 | `share_channel` | `string` |  | 渠道标识 (例如 光点、用户) |

---

## ods_db_vc_redeem_code_receive_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_redeem_code_receive_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 任务id |
| 2 | `code_id` | `bigint` |  | 所属兑换码id |
| 3 | `code` | `string` |  | 兑换码字符 |
| 4 | `receive_time` | `bigint` |  | 领取时间 |
| 5 | `user_id` | `bigint` |  | 用户id |
| 6 | `ext` | `string` |  | 扩展字段 |
| 7 | `device_id` | `string` |  | 设备id |
| 8 | `create_time` | `bigint` |  | DB创建时间 |
| 9 | `update_time` | `bigint` |  | DB更新时间 |
| 10 | `type` | `bigint` |  | 兑换码类型,1兑换码 2邀请码 |
| 11 | `code_user_id` | `bigint` |  | code所属用户id |

---

## ods_db_vc_reward_task_detail_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_reward_task_detail_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 明细id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `type_count_json` | `string` |  | 奖励类型、奖励id、数量关系:例如,{"type":0,"rewardId":0,"count":10} |
| 4 | `status` | `int` |  | 奖励的发放状态,1:发送完成,2:发送失败 |
| 5 | `send_start_time` | `bigint` |  | 奖励发放时间 |
| 6 | `valid_start_time` | `bigint` |  | 奖励生效时间,用户的同发放时间 |
| 7 | `valid_time` | `bigint` |  | 奖励的有效期 |
| 8 | `reward_task_id` | `bigint` |  | 所属任务id |
| 9 | `ext` | `string` |  | 扩展字段 |
| 10 | `created_by` | `string` |  | 创建人邮箱 |
| 11 | `updated_by` | `string` |  | 更新人邮箱 |
| 12 | `create_time` | `bigint` |  | DB创建时间 |
| 13 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_reward_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_reward_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 任务id |
| 2 | `obj_id` | `string` |  |  |
| 3 | `obj_counts` | `bigint` |  | 用户数量或人群包的用户数量 |
| 4 | `type_count_json` | `string` |  | 奖励类型、奖励id、数量关系:例如{"type":0,"rewardId":0,"count":10} |
| 5 | `status` | `int` |  | 奖励的发放状态,1:未发送,2:发送中,3:发送完成,4发送失败 |
| 6 | `send_type` | `int` |  | 任务类型：1:实时，2:定时 |
| 7 | `trigger_time` | `bigint` |  | 定时的触发时间(只有send_type=2时生效) |
| 8 | `send_start_time` | `bigint` |  | 奖励发放时间 |
| 9 | `valid_start_time` | `bigint` |  | 奖励生效时间(发送结束时间) |
| 10 | `valid_time` | `bigint` |  | 奖励的有效期 |
| 11 | `user_package_mark` | `int` |  | 用户或人群包标志,1:表示用户,2:表示人群包 |
| 12 | `ext` | `string` |  | 扩展字段 |
| 13 | `created_by` | `string` |  | 创建人邮箱 |
| 14 | `updated_by` | `string` |  | 更新人邮箱 |
| 15 | `create_time` | `bigint` |  | DB创建时间 |
| 16 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_rule_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_rule_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `obj_type` | `int` |  | 主体类型 1-角色 2-用户 |
| 5 | `obj_id` | `bigint` |  | 主体ID 1->角色ID 2->用户ID |
| 6 | `metric_id` | `bigint` |  | 指标ID |
| 7 | `metric_param` | `string` |  | 指标参数 {"selector" : ">=","value" : "10"} |
| 8 | `config_id` | `bigint` |  | 配置ID 规则关联的配置表ID |
| 9 | `is_deleted` | `int` |  | 是否删除 0-否 1-是 |

---

## ods_db_vc_simulator_attr_lib_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_attr_lib_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `attr_id` | `string` |  | 属性业务ID，对外暴露的唯一标识，如 ATTR_001 |
| 3 | `name` | `string` |  | 属性名称，如"好感度""黑化值""剧情进度" |
| 4 | `icon` | `string` |  | 属性图标REPORT 场景可为空 |
| 5 | `type` | `string` |  | 属性类型：数字 / 文本 |
| 6 | `rule` | `string` |  | 规则说明：SIMULATOR 场景描述变化规则，REPORT 场景描述生成规则 |
| 7 | `attr_scope` | `string` |  | 属性归属场景：SIMULATOR（模拟器属性）/ REPORT（报告属性） |
| 8 | `category` | `string` |  | 属性分类（仅 SIMULATOR 场景有效）：panel（聊天室面板）/ identity（身份）/ both（两者皆有），REPORT 场景为 NULL |
| 9 | `report_id` | `bigint` |  | 关联的控制器ID（仅 REPORT 场景有效），关联 vc_simulator_controller_lib.id，控制器删除时业务层级联清理 |
| 10 | `sort_order` | `bigint` |  | 排序权重，DESC 排序 |
| 11 | `status` | `int` |  | 状态 1启用 0禁用（软删，永远不物理删） |
| 12 | `create_by` | `string` |  | 创建运营 ID |
| 13 | `create_time` | `bigint` |  | 创建时间（毫秒） |
| 14 | `update_time` | `bigint` |  | 更新时间（毫秒） |
| 15 | `ext` | `string` |  | 扩展字段，JSON 格式存储业务自定义信息 |

---

## ods_db_vc_simulator_controller_lib_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_controller_lib_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `name` | `string` |  | 控制器名称，如"剧情走向""氛围渲染" |
| 3 | `controller_type` | `string` |  | 控制器类型：UI（界面输出）/ VALUE（数值控制） |
| 4 | `output_type` | `string` |  | 输出形态（仅 UI 类型有效）：REPORT（结果报告）/ BUBBLE（旁白气泡），VALUE 类型时为 NULL |
| 5 | `rule` | `string` |  | SP 信息，旁白气泡等场景填写 |
| 6 | `trigger_condition` | `string` |  | 触发条件JSON，支持多条件递归组合（AND/OR + 条件树），详见文件头注释 |
| 7 | `sort_order` | `bigint` |  | 排序权重，DESC 排序 |
| 8 | `status` | `int` |  | 状态 1启用 0禁用（软删，永远不物理删） |
| 9 | `create_by` | `string` |  | 创建运营 ID |
| 10 | `create_time` | `bigint` |  | 创建时间（毫秒） |
| 11 | `update_time` | `bigint` |  | 更新时间（毫秒） |
| 12 | `ext` | `string` |  | 扩展字段，JSON 格式存储业务自定义信息 |
| 13 | `output_mode` | `string` |  | 输出来源：AI（AI根据SP+属性生成）/ FIXED（直接输出固定内容） |
| 14 | `fixed_content` | `string` |  | 固定文本内容，output_mode=FIXED 时使用 |

---

## ods_db_vc_simulator_editorial_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_editorial_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `title` | `string` |  | 标题（与头图二选一） |
| 3 | `head_img` | `string` |  | 头图 URL（与标题二选一，有图优先） |
| 4 | `content_blocks` | `string` |  | 内容块列表 |
| 5 | `status` | `int` |  | 状态 0=下线 1=上线 |
| 6 | `operator` | `string` |  | 操作人 |
| 7 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 8 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 9 | `channel` | `int` |  | 渠道 1-APP/里世界 2-LOFTER |

---

## ods_db_vc_simulator_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `name` | `string` |  | 名称 |
| 5 | `description` | `string` |  | 简介 |
| 6 | `cover_image_url` | `string` |  | 入口封面图URL |
| 7 | `category` | `int` |  | 模拟器分类：1-官方模拟器 |
| 8 | `type` | `int` |  | 模拟器类型：1-单角色聊天数值报告玩法 |
| 9 | `weight` | `bigint` |  | 模拟器曝光权重 |
| 10 | `status` | `bigint` |  | 状态 1:已保存 2:待生效 3:生效 4:下架 |
| 11 | `config_ext` | `string` |  |  |
| 12 | `is_trial` | `int` |  | 是否试玩：0-否，1-是 |
| 13 | `simulator_id` | `string` |  | 模拟器id |
| 14 | `version` | `bigint` |  | 版本号 |
| 15 | `business_ext` | `string` |  | 业务ext |
| 16 | `create_by` | `string` |  | 创建人 |
| 17 | `update_by` | `string` |  | 更新人 |
| 18 | `effective_time` | `bigint` |  | 生效时间 |
| 19 | `expiration_time` | `bigint` |  | 失效时间 |
| 20 | `content_type` | `int` |  | 内容类型，1-原创 2-IP |
| 21 | `front_tags` | `string` |  | 前台标签，英文逗号分隔 |
| 22 | `backend_tags` | `string` |  | 后台标签，英文逗号分隔 |
| 23 | `channel` | `int` |  | 渠道信息，可选值：1-vc 3-LOFTER非里世界 |
| 24 | `public_scope` | `int` |  | 可见域 1:公开 2:私密 3:已屏蔽 |
| 25 | `user_id` | `bigint` |  | 用户ID |
| 26 | `user_commit` | `string` |  |  |
| 27 | `push_start_time` | `bigint` |  | 推送开始时间 |
| 28 | `push_end_time` | `bigint` |  | 推送结束时间, |
| 29 | `game_user_id` | `bigint` |  | 游戏平台用户ID |
| 30 | `block_terminals` | `string` |  | 禁止透出端，逗号分隔，空=全可见 |
| 31 | `lofter_audit_status` | `int` |  | lofter推荐审核 0-未送审 1-审核中 2-未通过 3-已通过 |

---

## ods_db_vc_simulator_pick_list_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_pick_list_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `title` | `string` |  | 标题（必填） |
| 3 | `description` | `string` |  | 描述（必填） |
| 4 | `image_url` | `string` |  | 封面图（选填） |
| 5 | `jump_url` | `string` |  | 跳转链接（H5，必填） |
| 6 | `sort_order` | `bigint` |  | 排序号，数字越小越靠前 |
| 7 | `status` | `int` |  | 状态 0=下线 1=上线 |
| 8 | `operator` | `string` |  | 操作人 |
| 9 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 10 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 11 | `channel` | `int` |  | 渠道 1-APP/里世界 2-LOFTER |

---

## ods_db_vc_simulator_room_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_room_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID（roomId） |
| 2 | `room_code` | `string` |  | 房间号（展示用，如 #8826，8位随机码） |
| 3 | `simulator_id` | `string` |  | 模拟器业务ID |
| 4 | `owner_user_id` | `bigint` |  | 房主用户ID（创建者） |
| 5 | `init_id` | `bigint` |  | 关联的初始化实例ID（1对1） |
| 6 | `status` | `int` |  | 房间状态：0-准备中 1-游戏中 |
| 7 | `identity_config` | `string` |  | 身份快照JSON，记录游戏开始时各身份的角色分配情况 |
| 8 | `mute_config` | `string` |  | 禁言配置JSON，房间维度永久禁言记录 |
| 9 | `min_player_num` | `int` |  | 最小开始人数（冗余自 config_ext.minPlayerNum） |
| 10 | `max_player_num` | `int` |  | 最大人数（冗余自 config_ext.maxPlayerNum） |
| 11 | `ext` | `string` |  | 扩展字段（JSON） |
| 12 | `create_time` | `bigint` |  | 创建时间（毫秒戳） |
| 13 | `update_time` | `bigint` |  | 更新时间（毫秒戳） |

---

## ods_db_vc_simulator_room_participant_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_room_participant_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `room_id` | `bigint` |  | 房间ID |
| 3 | `simulator_id` | `string` |  | 模拟器业务ID |
| 4 | `user_id` | `bigint` |  | 参与者用户ID |
| 5 | `cos_role_id` | `string` |  | 该用户扮演的身份ID（cosRoleId） |
| 6 | `role` | `int` |  | 角色：0-房主 1-普通参与者 |
| 7 | `join_time` | `bigint` |  | 加入时间（毫秒戳） |
| 8 | `last_active_time` | `bigint` |  | 用户最近一次进入该房间的时间（毫秒戳），用于查询最近在玩的房间 |
| 9 | `status` | `int` |  | 状态：0-游戏中 1-已完成 -1-中途退出 |
| 10 | `ext` | `string` |  | 扩展字段（JSON） |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_simulator_template_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_template_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键，作为业务 templateId 直接对外使用 |
| 2 | `scene_code` | `string` |  | 关联 vc_ai_scene_code.scene_code，模板对应的创作助手 SP/模型/温度配置由 vc_ai_scene_code 表维护 |
| 3 | `name` | `string` |  | 模板展示名称，如"豪门联姻" |
| 4 | `cover_image_url` | `string` |  | 模板封面图 URL |
| 5 | `description` | `string` |  | 模板简介（C 端展示） |
| 6 | `weight` | `bigint` |  | 排序权重，DESC 排序 |
| 7 | `is_pinned` | `int` |  | 是否置顶外显（活动入口） 1是 0否 |
| 8 | `status` | `int` |  | 状态 1启用 0禁用（软删，永远不物理删） |
| 9 | `create_by` | `string` |  | 创建运营 ID |
| 10 | `create_time` | `bigint` |  | 创建时间（毫秒） |
| 11 | `update_time` | `bigint` |  | 更新时间（毫秒） |

---

## ods_db_vc_simulator_zone_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_simulator_zone_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `title` | `string` |  | 专区标题 |
| 3 | `head_img` | `string` |  | 头图 URL |
| 4 | `banner_config` | `string` |  | banner 列表 |
| 5 | `description` | `string` |  | 专区描述 |
| 6 | `tag_names` | `string` |  | 关联标签，多个逗号分隔 |
| 7 | `jump_url` | `string` |  | 跳转链接 |
| 8 | `create_btn_config` | `string` |  | 创建按钮配置 |
| 9 | `status` | `int` |  | 状态 0=下线 1=上线 |
| 10 | `operator` | `string` |  | 操作人 |
| 11 | `create_time` | `bigint` |  | 创建时间（毫秒时间戳） |
| 12 | `update_time` | `bigint` |  | 更新时间（毫秒时间戳） |
| 13 | `channel` | `int` |  | 渠道 1-APP/里世界 2-LOFTER |

---

## ods_db_vc_sub_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_sub_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 任务id主键 |
| 2 | `main_task_id` | `bigint` |  | 关联的主任务id |
| 3 | `task_type` | `int` |  | 任务类型：1短信任务（与主任务一致） |
| 4 | `business_id` | `string` |  | 业务id |
| 5 | `user_ids` | `string` |  | 子任务对应的用户ID列表（JSON数组格式，如["user1", "user2"]） |
| 6 | `status` | `int` |  | 奖励的发放状态,1:未发送,2:发送中,3:发送完成,4发送失败,5终止发送,-1删除 |
| 7 | `content` | `string` |  | 任务内容 |
| 8 | `ext` | `string` |  | 扩展字段 |
| 9 | `create_time` | `bigint` |  | DB创建时间 |
| 10 | `update_time` | `bigint` |  | DB更新时间 |
| 11 | `success_count` | `bigint` |  | 成功数 |
| 12 | `lose_count` | `bigint` |  | 失败数 |

---

## ods_db_vc_suggestion_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_suggestion_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `suggestion` | `string` |  | 联想词 |
| 3 | `create_time` | `bigint` |  | DB创建时间 |
| 4 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_suicide_prevention_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_suicide_prevention_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `record_type` | `int` |  | 1=文本审核触发 2=易盾人审回调 |
| 3 | `user_id` | `bigint` |  | 用户内部 ID |
| 4 | `user_code` | `string` |  | 用户外部 code（lofter userId） |
| 5 | `channel` | `bigint` |  | 渠道 |
| 6 | `hit_sub_labels` | `string` |  | 命中的 sublabel JSON |
| 7 | `business_code` | `string` |  | 业务场景码 |
| 8 | `tip_send_time` | `bigint` |  | 温馨提示发送时间（毫秒），null=未发送，type=1 |
| 9 | `escalate_count` | `bigint` |  | 命中 699 时当前累计次数，type=1 有值 |
| 10 | `escalated` | `int` |  | 是否触发升级人审：0=否 1=是，type=1 有值 |
| 11 | `hit_content` | `string` |  | 触发命中的内容，type=1 有值 |
| 12 | `censor_labels` | `string` |  | 人审结论标签 JSON 数组，type=2 有值 |
| 13 | `popo_notify_time` | `bigint` |  | popo |
| 14 | `create_time` | `bigint` |  | 记录时间（毫秒时间戳） |

---

## ods_db_vc_support_detail_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_support_detail_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `votes` | `bigint` |  | 票数 |
| 3 | `activity_id` | `bigint` |  | 活动id |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `create_time` | `bigint` |  | DB创建时间 |
| 6 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_support_plan_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_support_plan_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `votes` | `bigint` |  | 票数 |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `activity_id` | `bigint` |  | 活动id |
| 5 | `create_time` | `bigint` |  | DB创建时间 |
| 6 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_tag_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_tag_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 类别名称字段(例如:标签类型名称、标签名称) |
| 3 | `pid` | `bigint` |  | 所属父级id，顶级为0 |
| 4 | `category` | `int` |  | 类别:1类型、2标签 |
| 5 | `scense` | `int` |  | 场景:1角色后台标签 |
| 6 | `status` | `int` |  | 标签状态,1:未生效,2:生效中,-1:已删除 |
| 7 | `created_by` | `string` |  | 创建人邮箱 |
| 8 | `updated_by` | `string` |  | 更新人邮箱 |
| 9 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 10 | `ext` | `string` |  | 扩展字段 |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_url_source_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_url_source_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `source_code` | `string` |  | 溯源码信息 |
| 5 | `scene` | `int` |  | 业务场景 |
| 6 | `source_content` | `string` |  | 溯源信息，业务参数透传 |
| 7 | `ext` | `string` |  | 业务扩展信息 |

---

## ods_db_vc_urs_callback_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_urs_callback_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `create_time` | `bigint` |  | 数据创建时间 |
| 4 | `update_time` | `bigint` |  | 数据更新时间 |
| 5 | `type` | `int` |  | 操作类型 |
| 6 | `urs_id` | `string` |  | urs账号ID |
| 7 | `phone` | `string` |  | 老手机号 |
| 8 | `tid` | `string` |  | 链路ID，同一批次tid一样 |
| 9 | `callback` | `string` |  | 回调内容 |

---

## ods_db_vc_user_account_config_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_account_config_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `type` | `bigint` |  | 业务类型 |
| 4 | `status` | `bigint` |  | 状态 |
| 5 | `ext` | `string` |  | 扩展信息 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |
| 8 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |

---

## ods_db_vc_user_account_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_account_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 1.8G |
| **是否分区表** | 否 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_name` | `string` |  | 用户昵称 |
| 3 | `user_profile` | `string` |  | 用户简介 |
| 4 | `user_head_img` | `string` |  | 用户头像 |
| 5 | `sex` | `bigint` |  | 性别 |
| 6 | `out_id` | `string` |  | 外部唯一标识 |
| 7 | `phone` | `string` |  | 用户手机号 |
| 8 | `status` | `bigint` |  | 状态 |
| 9 | `ext` | `string` |  | 扩展信息 |
| 10 | `create_time` | `bigint` |  | 创建时间 |
| 11 | `update_time` | `bigint` |  | 更新时间 |
| 12 | `user_code` | `string` |  |  |
| 13 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 14 | `account_init_process` | `string` |  | 账号初始化进度 |
| 15 | `device_id` | `string` |  | 设备ID |
| 16 | `account_type` | `int` |  | 账号类型: 1-默认账号，2-虚拟账号 |
| 17 | `device_uid` | `string` |  | 设备UID信息，非业务场景使用 |
| 18 | `real_auth_id` | `bigint` |  | 认证id |

---

## ods_db_vc_user_activity_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_activity_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `activity_id` | `bigint` |  | 活动ID |
| 4 | `activity_type` | `int` |  | 活动类型：1签到领取活动，2连续签到奖励活动 |
| 5 | `behavior_record` | `string` |  | 用户行为记录 |
| 6 | `version` | `bigint` |  | 更新版本 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `start_time` | `bigint` |  |  |
| 10 | `end_time` | `bigint` |  |  |

---

## ods_db_vc_user_app_small_module_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_app_small_module_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `size_type` | `int` |  | 组件尺寸：1小2中3大 |
| 5 | `config` | `string` |  | 组件配置 |
| 6 | `status` | `int` |  | 状态，-1: 删除, 1: 生效 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_asset_consume_flow_all_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_asset_consume_flow_all_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 122.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 122.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `amount` | `bigint` |  | 支出数量 |
| 5 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `consume_status` | `int` |  | 消耗状态： 1=预扣除；2=扣除完成；3=回滚完成 |
| 8 | `detail` | `string` |  | 支出详情 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |
| 11 | `ext_amount` | `bigint` |  | 额外数量 |
| 12 | `partition_date` | `bigint` |  | 分区日期字段(yyyymmdd格式) |
| 13 | `ext` | `string` |  | 额外参数 |
| 14 | `dt` | `string` |  |  |

---

## ods_db_vc_user_asset_consume_flow_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_asset_consume_flow_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 19.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 19.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `amount` | `bigint` |  | 支出数量 |
| 5 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `consume_status` | `int` |  | 消耗状态： 1=预扣除；2=扣除完成；3=回滚完成 |
| 8 | `detail` | `string` |  | 支出详情 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |
| 11 | `ext_amount` | `bigint` |  | 从lofter消耗的能量 |
| 12 | `partition_date` | `bigint` |  | 分区 |
| 13 | `ext` | `string` |  | 额外参数 |
| 14 | `dt` | `string` |  |  |

---

## ods_db_vc_user_asset_consume_flow_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_asset_consume_flow_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `amount` | `bigint` |  | 支出数量 |
| 5 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `consume_status` | `int` |  | 消耗状态： 1=预扣除；2=扣除完成；3=回滚完成 |
| 8 | `detail` | `string` |  | 支出详情 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |
| 11 | `ext_amount` | `bigint` |  | 额外数量 |
| 12 | `partition_date` | `bigint` |  | 分区日期字段(yyyymmdd格式) |
| 13 | `ext` | `string` |  |  |

---

## ods_db_vc_user_asset_left_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_asset_left_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 1.0G |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `total_amount` | `bigint` |  | 总数量 |
| 5 | `left_amount` | `bigint` |  | 剩余数量 |
| 6 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 7 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 8 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 9 | `scene` | `int` |  | 收入场景： 0未知场景； |
| 10 | `create_time` | `bigint` |  | 创建时间 |
| 11 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_asset_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_asset_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `asset_type` | `int` |  | 资产类型： 1能量 |
| 4 | `amount` | `bigint` |  | 数量 |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_backpack_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_backpack_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 账号ID |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `prop_id` | `bigint` |  | 道具ID |
| 7 | `prop_type` | `bigint` |  | 1-礼物 2-装扮 |
| 8 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 9 | `valid_start_time` | `bigint` |  | 道具有效开始时间 |
| 10 | `valid_end_time` | `bigint` |  | 道具有效截止时间 |
| 11 | `source` | `int` |  | 获取来源：0未知；1免费领取；2.购买 |
| 12 | `ext` | `string` |  | 额外配置 |
| 13 | `num` | `bigint` |  | 道具数量 |

---

## ods_db_vc_user_benefits_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_benefits_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 168.9M |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `benefit_type` | `int` |  | 权益类型，如：充能卡等 |
| 4 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 5 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 6 | `scene` | `int` |  | 获取来源：1免费领取；2购买；3奖励发放 |
| 7 | `version` | `bigint` |  | 更新版本 |
| 8 | `status` | `int` |  | 权益状态：1有效，-1失效 |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | DB更新时间 |
| 11 | `character_id` | `bigint` |  | 角色ID |

---

## ods_db_vc_user_birthday_support_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_birthday_support_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | id主键 |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `event_id` | `bigint` |  | 生日活动ID |
| 4 | `is_read` | `bigint` |  | 是否阅读 1:未阅读， 2:已阅读 |
| 5 | `character_id` | `bigint` |  | 角色id |
| 6 | `support_value` | `bigint` |  | 用户应援值 |
| 7 | `received_level_rewards` | `string` |  | 领取的奖励等级 |
| 8 | `create_time` | `bigint` |  | 数据创建时间 |
| 9 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_character_ai_experiment_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_ai_experiment_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `experiment_id` | `bigint` |  | 实验id |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `character_id` | `bigint` |  | 角色id |
| 5 | `business_type` | `string` |  | 业务类型 |
| 6 | `ext` | `string` |  | 扩展字段 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_character_choose_model_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_choose_model_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `model_id` | `bigint` |  | 模型id |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |
| 7 | `ext` | `string` |  | 额外信息 |
| 8 | `op_type` | `int` |  | 操作类型 |

---

## ods_db_vc_user_character_choose_model_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_choose_model_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `model_id` | `bigint` |  | 模型id |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |
| 7 | `ext` | `string` |  | 额外信息 |

---

## ods_db_vc_user_character_group_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_group_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `group_type` | `bigint` |  | 分组类型：1羁绊录 |
| 5 | `group_code` | `bigint` |  | 分组标签code |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_character_memory_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_memory_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 25.7G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 25.7G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `long_memory_code` | `string` |  | 长期记忆code |
| 3 | `content` | `string` |  | 记忆摘要 |
| 4 | `type` | `int` |  | 类型：1.用户-虚拟人 2.虚拟人-虚拟人 |
| 5 | `user_id` | `bigint` |  | 用户id |
| 6 | `source_id` | `bigint` |  | 源目标 |
| 7 | `target_id` | `bigint` |  | 关联目标 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 11 | `ext` | `string` |  | 扩展信息 |
| 12 | `status` | `bigint` |  | 状态 |
| 13 | `sentence_dense_embedding` | `string` |  | 密集向量embedding |
| 14 | `sentence_sparse_embedding` | `string` |  | 稀疏向量embedding |

---

## ods_db_vc_user_character_memory_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_memory_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 46.3G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 46.3G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `long_memory_code` | `string` |  | 长期记忆code |
| 3 | `message_id` | `string` |  | 消息id |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 7 | `ext` | `string` |  | 扩展信息 |
| 8 | `status` | `bigint` |  | 状态 |

---

## ods_db_vc_user_character_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 虚拟人id |
| 4 | `character_emotional_status` | `int` |  | 虚拟人情绪状态：1.普通 2.呢嘛 3.冷落 4.恩恩 |
| 5 | `status` | `int` |  | 状态：1.正常 2.删除 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 修改时间 |
| 8 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 9 | `ext` | `string` |  | 扩展信息 |
| 10 | `heart_value` | `bigint` |  | 心动值 |

---

## ods_db_vc_user_character_simulator_chat_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_simulator_chat_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `simulator_id` | `string` |  | 模拟器ID |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |
| 7 | `latest_send_msg_time` | `bigint` |  | 最近一次发送消息时间（毫秒时间戳） |
| 8 | `ext` | `string` |  | 业务扩展信息 |
| 9 | `channel` | `int` |  | 渠道信息，可选值：1-vc客户端 3-LOFTER客户端 |
| 10 | `status` | `int` |  | 状态，1-正常，-1 -已删除 |

---

## ods_db_vc_user_character_skin_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_character_skin_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id |
| 4 | `skin_id` | `bigint` |  | 皮肤id |
| 5 | `acquire_channel` | `int` |  | 获取渠道,1-约会卡解锁 |
| 6 | `record_id` | `string` |  | 获取对应记录id |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_date_card_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_date_card_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `card_id` | `bigint` |  | 卡片id |
| 4 | `character_id` | `bigint` |  | 关联的角色id |
| 5 | `grade` | `int` |  | 用户持有该卡片等级: 1-1星, 2-2星 |
| 6 | `chat_unlocked` | `int` |  | 自由聊天状态: 0-未解锁, 1-已解锁 |
| 7 | `viewed` | `int` |  | 查看状态: 0-未查看, 1-已查看 |
| 8 | `create_time` | `bigint` |  | 数据创建时间 |
| 9 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_deduction_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_deduction_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 16.3G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 16.3G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 扣除记录ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `record_id` | `bigint` |  | 购买发放记录明细表ID |
| 4 | `deduction_num` | `bigint` |  | 当前扣减数量 |
| 5 | `scene` | `int` |  | 支出场景 |
| 6 | `ext` | `string` |  | 扩展信息 |
| 7 | `create_time` | `bigint` |  | 数据创建时间 |
| 8 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_diary_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_diary_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `type` | `int` |  | 值类型：1印象，2日记 |
| 5 | `title` | `string` |  | 标题 |
| 6 | `content` | `string` |  | 内容 |
| 7 | `status` | `int` |  | 状态类型：1正常，-1删除 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_user_diary_visit_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_diary_visit_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `type` | `int` |  | 值类型：1日记 |
| 4 | `target_id` | `bigint` |  | 业务ID |
| 5 | `last_visit_time` | `bigint` |  | 最近一次访问日记的时间 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |
| 8 | `character_id` | `bigint` |  | 角色ID |

---

## ods_db_vc_user_dungeon_chat_branch_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_dungeon_chat_branch_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `character_id` | `bigint` |  | 虚拟角色ID |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `simulator_id` | `string` |  | 模拟器id |
| 5 | `init_id` | `bigint` |  | 实例化id |
| 6 | `parent_branch_id` | `bigint` |  | 父分支id |
| 7 | `has_child` | `int` |  | 是否存在子节点 0不存在 1存在 |
| 8 | `branch_path` | `string` |  | 分支根路径 |
| 9 | `forget_message` | `string` |  | 遗忘消息 |
| 10 | `status` | `int` |  | 状态 |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 更新时间 |
| 13 | `branch_name` | `string` |  | 存档名称 |
| 14 | `use_status` | `string` |  | 是否当前使用 0否 1是 |
| 15 | `branch_num` | `bigint` |  | 存档轮数 |
| 16 | `init_status` | `int` |  | 0-待初始化 , 1-初始化中, 2-初始化已完成待开启，3-已开启 |
| 17 | `get_ways` | `int` |  | 获取途径 1购买 2免费 3复制 |

---

## ods_db_vc_user_dungeon_chat_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_dungeon_chat_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 64.2G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 64.2G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `scene` | `string` |  | 玩法场景，每个场景单独定义 |
| 3 | `user_id` | `bigint` |  | 用户ID |
| 4 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |
| 7 | `req_id` | `string` |  | 请求id |
| 8 | `db_update_time` | `bigint` |  | 更新时间 |
| 9 | `biz_id` | `string` |  | 业务ID，不存在默认为空字符串 |
| 10 | `sender` | `int` |  | 发话类型 1-角色回复 2-用户发话 6-助攻 |
| 11 | `status` | `int` |  | 消息状态 1-正常态，3-命中敏感词，4-ai异常，5-ai敏感词异常，6-遗忘，7-已删除 |
| 12 | `content` | `string` |  | 发话内容 |
| 13 | `content_type` | `int` |  | 内容类型 |
| 14 | `msg_type` | `bigint` |  | 消息类型 |
| 15 | `app_header` | `string` |  | 用户header信息 |
| 16 | `chat_seq` | `bigint` |  | 聊天轮次 |
| 17 | `ext` | `string` |  | 扩展消息聊天扩展信息 |
| 18 | `source` | `string` |  | 消息来源，可选值：1-vc,2-lofter |
| 19 | `branch_id` | `bigint` |  | 存档id |

---

## ods_db_vc_user_event_count_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_event_count_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `user_id` | `bigint` |  | BF |
| 3 | `event_type` | `string` |  | 事件类型，对应 UserFirstEventEnum.code |
| 4 | `count` | `bigint` |  | NULL |
| 5 | `extra` | `string` |  | 扩展数据 JSON，如来源页面、版本号等 |
| 6 | `create_time` | `bigint` |  | NULL |
| 7 | `update_time` | `bigint` |  | NULL |
| 8 | `db_update_time` | `bigint` |  | NULL |

---

## ods_db_vc_user_event_task_detail_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_event_task_detail_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 用户ID |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `event_id` | `bigint` |  | 事件ID |
| 7 | `type` | `int` |  | 类型 1-卡片推送 2-点击选项 3-触发目标玩法 |
| 8 | `status` | `int` |  | 状态信息 2-处理中 4-已完成 5-已失败 |
| 9 | `ext` | `string` |  | 业务扩展信息 |
| 10 | `message_id` | `string` |  | 消息ID |

---

## ods_db_vc_user_event_task_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_event_task_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 用户ID |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `event_type` | `int` |  | 事件类型 1-次元事件 2-日常事件 3-情绪事件 |
| 7 | `status` | `int` |  | 状态信息 1-待处理 2-处理中 3-已关闭 4-已完成 5-已失败 -1-已删除 |
| 8 | `init_source` | `int` |  | 初始化来源 1-存量初始化 2-实时初始化 |
| 9 | `is_close_front_show` | `int` |  | 前台是否关闭 0-否 1-是 |
| 10 | `config_id` | `bigint` |  | 配置ID event_type=1会有值 |
| 11 | `content` | `string` |  | 内容信息 {"title" : "","description" : "","content" : "","jumpType" : "","jumpUrl" : ""} |
| 12 | `option_reply_config` | `string` |  | 选项配置信息 |
| 13 | `play_type` | `int` |  | 玩法类型 0-不支持玩法 1-任务玩法 2-数值玩法 |
| 14 | `play_config` | `string` |  | 玩法描述 |
| 15 | `ext` | `string` |  | 业务扩展信息 |
| 16 | `sub_event_type` | `int` |  | 事件子类型，0-默认值无实际含义，1-预埋事件，2-推送事件，3-日常事件 |

---

## ods_db_vc_user_feedback_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_feedback_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `type` | `int` |  | 1.聊天记录反馈 |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `target_id` | `string` |  | 关联id:消息id等 |
| 5 | `reason` | `string` |  | 自定义参数 |
| 6 | `opt_type` | `int` |  | 0.初始值 1.点赞 2.点踩 |
| 7 | `status` | `int` |  | 1.有效 2.无效 |
| 8 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter |
| 9 | `create_time` | `bigint` |  | 创建时间 |
| 10 | `update_time` | `bigint` |  | 更新时间 |
| 11 | `ext` | `string` |  | 额外配置 |
| 12 | `character_id` | `bigint` |  | 角色id |

---

## ods_db_vc_user_fetter_notice_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_fetter_notice_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `type` | `int` |  | 0：日常，1：固定，2：人工 |
| 5 | `status` | `int` |  | 0：未关闭，1：已关闭 |
| 6 | `route` | `string` |  | 路由地址 |
| 7 | `content` | `string` |  | 消息内容 |
| 8 | `user_code` | `string` |  | userCode |
| 9 | `create_time` | `bigint` |  | 数据创建时间 |
| 10 | `update_time` | `bigint` |  | 数据更新时间 |
| 11 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_user_gift_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_gift_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `create_time` | `bigint` |  | 创建时间 |
| 3 | `update_time` | `bigint` |  | 更新时间 |
| 4 | `user_id` | `bigint` |  | 账号ID |
| 5 | `character_id` | `bigint` |  | 角色ID |
| 6 | `gift_id` | `bigint` |  | 礼物ID |
| 7 | `message` | `string` |  | ai生成消息 |
| 8 | `status` | `int` |  | 0 初始化，未领取，1：已曝光，2：已领取 |
| 9 | `scene` | `bigint` |  | 场景：1.普通场景 2.生日月活动礼物 |
| 10 | `channel` | `bigint` |  | 用户礼物渠道 1.app 2.lofter |
| 11 | `give_nums` | `bigint` |  | 赠送数量 |

---

## ods_db_vc_user_income_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_income_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `reward_type` | `int` |  | 奖励类型：1资产，2道具，3权益 |
| 4 | `reward_id` | `bigint` |  | 收入分类细分 |
| 5 | `scene` | `int` |  | 获取来源：1免费领取；2购买；3奖励发放 |
| 6 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 7 | `income_time` | `bigint` |  | 收入时间 |
| 8 | `amount` | `bigint` |  | 收入数量 |
| 9 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 10 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 11 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 12 | `income_detail` | `string` |  | 收入详情 |
| 13 | `create_time` | `bigint` |  | 创建时间 |
| 14 | `update_time` | `bigint` |  | DB更新时间 |
| 15 | `display` | `int` |  | 是否展示：0-不展示，1-展示 |
| 16 | `channel` | `int` |  | 来源渠道，1-APP，2-lofter |

---

## ods_db_vc_user_interact_message_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_interact_message_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `interact_type` | `int` |  | 互动类型：1=评论；2=点赞 |
| 4 | `join_id` | `bigint` |  | 关联ID |
| 5 | `read_status` | `int` |  | 是否已读 2未读 1已读 |
| 6 | `status` | `int` |  | 状态：1生效（默认）-1=失效（删除） |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 记录更新时间 |
| 9 | `ext` | `string` |  | 扩展字段，JSON格式，COMMON类型存jumpUrl |

---

## ods_db_vc_user_item_subscription_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_item_subscription_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `user_sub_type` | `int` |  | 用户订阅类型： 1=VIP |
| 4 | `item_id` | `bigint` |  | 当前订阅的订阅商品id |
| 5 | `pay_method` | `string` |  | 支付平台(alipay/weixinpay) |
| 6 | `matrix_orderid` | `string` |  | SDK订单号(支付平台的) |
| 7 | `status` | `int` |  | 协议状态:0初始化,1未生效,2正常,3暂停,4已解约 |
| 8 | `expiration_time` | `bigint` |  | 本次订阅周期过期时间(续费时更新) |
| 9 | `original_transaction_id` | `string` |  | 支付宝签约号/微信签约号 |
| 10 | `ext` | `string` |  | 订阅记录扩展信息 |
| 11 | `create_time` | `bigint` |  | 数据创建时间 |
| 12 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_like_op_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_like_op_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `type` | `int` |  | 类型：LikeTypeEnum |
| 5 | `target_id` | `bigint` |  | 点赞目标的记录ID |
| 6 | `liked` | `int` |  | 0不喜欢，1喜欢 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_message_list_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_message_list_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 虚拟人id |
| 4 | `conversation_id` | `string` |  | 会话id |
| 5 | `status` | `int` |  | 状态：1.正常 2.删除 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 修改时间 |
| 8 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 9 | `ext` | `string` |  | 扩展信息 |
| 10 | `last_encounter_time` | `bigint` |  | 最近邂逅时间 |
| 11 | `disturb_status` | `int` |  | 免打扰状态 1:正常 -1:免打扰 |

---

## ods_db_vc_user_message_record_partition_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_message_record_partition_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 992.2G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 992.2G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 虚拟人id |
| 4 | `conversation_id` | `string` |  | 会话id |
| 5 | `message_id` | `string` |  | 消息id |
| 6 | `sender` | `int` |  | 对话方式：1.回复 2.主动对话 |
| 7 | `content` | `string` |  | 对话内容 |
| 8 | `content_type` | `int` |  | 内容类型：1.文本 2.音频 3.图片 |
| 9 | `type` | `int` |  | 聊天类型：1.单聊 2.群聊 |
| 10 | `status` | `int` |  | 状态：1.初始 2.已回复 3.名字敏感词 4.ai异常 |
| 11 | `ai_model` | `int` |  | ai模型类型：1.豆包 2.minimax 3.伏羲 |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 修改时间 |
| 14 | `channel` | `int` |  | 渠道：1:虚拟人app（默认）2:lofter |
| 15 | `ext` | `string` |  | 扩展信息 |
| 16 | `reply_id` | `bigint` |  | 回复消息id |
| 17 | `lofter_message_id` | `bigint` |  | Lofter消息D |

---

## ods_db_vc_user_mystery_gift_prize_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_mystery_gift_prize_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `message_id` | `string` |  | 消息id |
| 4 | `gift_id` | `bigint` |  | 礼物id |
| 5 | `channel` | `bigint` |  | 渠道，参考DataChannelEnum |
| 6 | `reward_type` | `bigint` |  | 中奖类型，对应GiftRewardTypeEnum的code |
| 7 | `reward_sub_type` | `bigint` |  | 中奖子类型，对应ImRewardTaskTypeEnum的code |
| 8 | `scene_id` | `string` |  | 发奖场景ID，格式：ImRewardTaskTypeEnum枚举名_时间戳ms |
| 9 | `reward_amount` | `bigint` |  | 奖励数量 |
| 10 | `status` | `bigint` |  | 1:发放成功，0:未发放 |
| 11 | `ext` | `string` |  | 扩展信息（json结构） |
| 12 | `create_time` | `bigint` |  | 创建时间 |
| 13 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_numeric_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_numeric_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `value_type` | `int` |  | 值类型：1羁绊值 |
| 5 | `value` | `bigint` |  | 值 |
| 6 | `version` | `bigint` |  | 更新版本 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |
| 9 | `character_type` | `int` |  | 角色类型：0官方 1自由捏 |
| 10 | `manual_code` | `int` |  | 手动设置关系 |
| 11 | `status` | `int` |  | 数据状态：1有效，0无效 |

---

## ods_db_vc_user_op_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_op_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `operation_type` | `bigint` |  | 1-销户 2-第三方解绑 3-手机解绑 |
| 4 | `status` | `bigint` |  | 1-有效  -1:已注销 2-待注销 -2-无效 |
| 5 | `op_time` | `bigint` |  | 操作时间 |
| 6 | `ext` | `string` |  | 扩展信息 |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_preference_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_preference_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `gender_options` | `string` |  | 性别偏好 |
| 4 | `relation_options` | `string` |  | 关系偏好 |
| 5 | `character_options` | `string` |  | 角色偏好 |
| 6 | `create_time` | `bigint` |  | 数据创建时间 |
| 7 | `update_time` | `bigint` |  | 数据更新时间 |

---

## ods_db_vc_user_props_consume_flow_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_props_consume_flow_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `amount` | `bigint` |  | 支出数量 |
| 4 | `scene` | `int` |  | 支出场景： 0未知场景； |
| 5 | `scene_uniq_id` | `string` |  | 场景内唯一id |
| 6 | `consume_status` | `int` |  | 消耗状态： 1=预扣除；2=扣除完成；3=回滚完成 |
| 7 | `detail` | `string` |  | 支出详情 |
| 8 | `create_time` | `bigint` |  | 创建时间 |
| 9 | `update_time` | `bigint` |  | 更新时间 |
| 10 | `ext_info` | `string` |  | 扩展信息 |

---

## ods_db_vc_user_props_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_props_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 316.6M |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `props_id` | `bigint` |  | 道具id |
| 4 | `props_type` | `bigint` |  | 道具类型：2忘忧草 |
| 5 | `amount` | `bigint` |  | 道具数量 |
| 6 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 7 | `valid_start_time` | `bigint` |  | 道具有效开始时间 |
| 8 | `valid_end_time` | `bigint` |  | 道具有效截止时间 |
| 9 | `source` | `int` |  | 获取来源：0未知；1免费领取；2购买 |
| 10 | `create_time` | `bigint` |  | 创建时间 |
| 11 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_user_purchase_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_purchase_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 2.1G |
| **是否分区表** | 否 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `order_id` | `bigint` |  | 订单ID，关联订单表 |
| 4 | `item_id` | `bigint` |  | 商品ID，关联vc_item表 |
| 5 | `item_scene` | `int` |  | 商品场景 |
| 6 | `reward_type` | `int` |  | 类型：1资产，2道具，3权益 |
| 7 | `reward_id` | `bigint` |  | 收入分类细分 |
| 8 | `num` | `bigint` |  | 收入数量 |
| 9 | `left_num` | `bigint` |  | 剩余数量：剩余可用数量 |
| 10 | `valid_type` | `int` |  | 生效类型：1永久（默认）；2=区间有效 |
| 11 | `possession_id` | `bigint` |  | 持有财产id（若reward_type为1则为能量记录表id；reward_type为2则为 vc_user_props 的id；reward_type为3则为用户权益表的id） |
| 12 | `income_record_id` | `bigint` |  | 收入记录表id，有些类型可能不存收入记录表 |
| 13 | `valid_start_time` | `bigint` |  | 有效开始时间 |
| 14 | `valid_end_time` | `bigint` |  | 有效截止时间 |
| 15 | `create_time` | `bigint` |  | 数据创建时间 |
| 16 | `update_time` | `bigint` |  | 数据更新时间 |
| 17 | `ext` | `string` |  | 扩展信息 |

---

## ods_db_vc_user_real_auth_notice_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_real_auth_notice_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `content` | `string` |  | 消息内容 |
| 4 | `user_code` | `string` |  | userCode |
| 5 | `create_time` | `bigint` |  | 数据创建时间 |
| 6 | `update_time` | `bigint` |  | 数据更新时间 |
| 7 | `ext` | `string` |  | 扩展信息 |
| 8 | `source` | `int` |  | 来源: 1=被动识别, 2=主动选择 |
| 9 | `channel` | `int` |  | 渠道: 1=虚拟人App, 2=Lofter站内, 3=Lofter App |

---

## ods_db_vc_user_secret_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_secret_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 账号ID |
| 3 | `character_id` | `bigint` |  | 角色ID |
| 4 | `secret_id` | `bigint` |  | 秘密ID |
| 5 | `status` | `int` |  | 状态类型：1正常，-1删除 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_share_record_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_share_record_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `type` | `int` |  | 分享type:1聊天分享，2角色分享，见UrlSourceShareSceneEnum |
| 3 | `content` | `string` |  | 分享内容 |
| 4 | `create_time` | `bigint` |  | DB创建时间 |
| 5 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_user_simulator_detail_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_simulator_detail_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | 163.0G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 163.0G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `db_update_time` | `bigint` |  | 更新时间 |
| 7 | `simulator_id` | `string` |  | 模拟器ID |
| 8 | `init_id` | `bigint` |  | 实例表ID |
| 9 | `type` | `int` |  | 操作类型1-初始化 3-任务判定 |
| 10 | `ext` | `string` |  | 业务扩展信息 |
| 11 | `chapter_id` | `string` |  | 章节ID |
| 12 | `controller_id` | `string` |  | 控制器ID |
| 13 | `result_config_id` | `string` |  | 结果配置ID |
| 14 | `result_ext` | `string` |  | 结果执行信息 |
| 15 | `branch_id` | `bigint` |  | 存档id，无默认0 |

---

## ods_db_vc_user_simulator_group_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_simulator_group_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 创建者ID |
| 3 | `simulator_id` | `string` |  | 模拟器ID |
| 4 | `create_time` | `bigint` |  | 数据创建时间 |

---

## ods_db_vc_user_simulator_init_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_simulator_init_info_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `character_id` | `bigint` |  | 角色ID，无角色默认值-1 |
| 4 | `create_time` | `bigint` |  | 创建时间 |
| 5 | `update_time` | `bigint` |  | 更新时间 |
| 6 | `db_update_time` | `bigint` |  | 更新时间 |
| 7 | `simulator_id` | `string` |  | 模拟器ID |
| 8 | `status` | `int` |  | 状态信息 0-初始化中 1-初始化完成-待开启 2-已开启-进行中 3-已关闭  4-已完成 5-已失败  -1-已删除 |
| 9 | `init_param` | `string` |  | 初始化内容 |
| 10 | `ext` | `string` |  | 业务扩展信息 |

---

## ods_db_vc_user_simulator_play_relation_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_simulator_play_relation_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `simulator_id` | `string` |  | 模拟器ID |
| 4 | `type` | `int` |  | 模拟器类型：1-单角色聊天数值报告玩法 |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |
| 7 | `message_id` | `string` |  | 最新消息id |
| 8 | `ext` | `string` |  | 业务扩展信息 |
| 9 | `status` | `int` |  | 状态，1-正常，-1 -已删除 |
| 10 | `room_id` | `bigint` |  | 房间ID，单人模拟器为0，多人模拟器为具体roomId |

---

## ods_db_vc_user_simulator_result_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_simulator_result_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `simulator_id` | `string` |  | 模拟器id |
| 4 | `init_id` | `bigint` |  | 实例化id |
| 5 | `branch_id` | `bigint` |  | 存档id |
| 6 | `result_def_id` | `string` |  | 结局/成就id |
| 7 | `forget_status` | `int` |  | 回溯状态 -1未回溯 1已回溯 |
| 8 | `ext` | `string` |  | 拓展字段 |
| 9 | `status` | `int` |  | 状态 |
| 10 | `create_time` | `bigint` |  | 创建时间 |
| 11 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_simulator_result_unlock_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_simulator_result_unlock_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `simulator_id` | `string` |  | 模拟器id |
| 4 | `result_def_id` | `string` |  | 结局/成就id |
| 5 | `status` | `int` |  | 状态 |
| 6 | `create_time` | `bigint` |  | 创建时间 |
| 7 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_support_task_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_support_task_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `task_type` | `int` |  | 任务类型 |
| 3 | `activity_id` | `bigint` |  | 活动id |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `source` | `int` |  | 来源 |
| 6 | `progress` | `bigint` |  | 进度 |
| 7 | `complete_status` | `int` |  | 完成状态 |
| 8 | `task_reward` | `int` |  | 任务奖励 |
| 9 | `create_time` | `bigint` |  | DB创建时间 |
| 10 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_user_topic_box_like_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_topic_box_like_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID |
| 3 | `topic_box_id` | `bigint` |  | 目标ID（话题ID或回答ID） |
| 4 | `like_status` | `int` |  | 喜欢状态: 1喜欢, 0不喜欢(默认) |
| 5 | `create_time` | `bigint` |  | 创建时间 |
| 6 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_topic_box_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_topic_box_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 创建者用户ID |
| 3 | `title` | `string` |  | 话题标题 |
| 4 | `content` | `string` |  | 话题内容 |
| 5 | `category_type` | `bigint` |  | 分类类型（0-其他,1-情侣问答，2-模拟辩论，3-吃醋，4-事件） |
| 6 | `public_type` | `int` |  | 是否公开（1-公开2-私密） |
| 7 | `fetter_relation_code` | `int` |  | 羁绊关系(分组)（1,2,3,4） |
| 8 | `first_publish` | `int` |  | 是否用户首次公开（-1-非首次1-首次） |
| 9 | `publish_time` | `bigint` |  | 发布时间 |
| 10 | `view_count` | `bigint` |  | 浏览次数 |
| 11 | `use_count` | `bigint` |  | 使用次数 |
| 12 | `share_count` | `bigint` |  | 分享次数 |
| 13 | `like_count` | `bigint` |  | 点赞次数 |
| 14 | `weight` | `bigint` |  | 权重值 |
| 15 | `show_index` | `bigint` |  | 展示坐标. 默认为0 |
| 16 | `status` | `int` |  | 状态（-1-删除，1-正常）见枚举:DataStatusEnum |
| 17 | `create_time` | `bigint` |  | 创建时间 |
| 18 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_user_votes_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_user_votes_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `votes` | `bigint` |  | 票数 |
| 3 | `activity_id` | `bigint` |  | 活动id |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `source` | `int` |  | 来源 |
| 6 | `create_time` | `bigint` |  | DB创建时间 |
| 7 | `update_time` | `bigint` |  | DB更新时间 |

---

## ods_db_vc_vibe_game_instance_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_vibe_game_instance_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户id |
| 3 | `character_id` | `bigint` |  | 角色id，无角色默认-1 |
| 4 | `game_id` | `string` |  | 游戏id（外部配置id） |
| 5 | `game_name` | `string` |  | 游戏名称 |
| 6 | `type` | `bigint` |  | 游戏类型，预留字段 |
| 7 | `status` | `bigint` |  | 实例状态：0-进行中 1-已结束 |
| 8 | `result` | `string` |  | 游戏结果（JSON格式） |
| 9 | `data` | `string` |  | 初始化信息（JSON格式） |
| 10 | `ext` | `string` |  | 扩展信息（JSON格式） |
| 11 | `create_time` | `bigint` |  | 创建时间 |
| 12 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_vibe_game_log_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_vibe_game_log_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `instance_id` | `bigint` |  | 游戏实例id |
| 3 | `user_id` | `bigint` |  | 用户id |
| 4 | `character_id` | `bigint` |  | 角色id，无角色默认-1 |
| 5 | `game_id` | `string` |  | 游戏id（外部配置id） |
| 6 | `game_name` | `string` |  | 游戏名称 |
| 7 | `type` | `bigint` |  | 游戏类型，预留字段 |
| 8 | `action` | `string` |  | 上报行为标识 |
| 9 | `data` | `string` |  | 上报数据（JSON格式） |
| 10 | `ext` | `string` |  | 扩展信息（JSON格式） |
| 11 | `create_time` | `bigint` |  | 创建时间 |

---

## ods_db_vc_visual_novel_script_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_visual_novel_script_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `story_id` | `string` |  | 剧本业务ID（vn_xxx） |
| 3 | `version` | `string` |  | 剧本版本号（meta.version） |
| 4 | `dsl_version` | `string` |  | DSL 协议版本 |
| 5 | `dsl_json` | `string` |  | 完整 DSL JSON（不压缩，≤16MB） |
| 6 | `dsl_digest` | `string` |  | DSL JSON 摘要（sha256/md5，用于版本比对） |
| 7 | `title` | `string` |  | 剧本标题 |
| 8 | `subtitle` | `string` |  | 副标题 |
| 9 | `cover` | `string` |  | 封面图 URL |
| 10 | `category` | `string` |  | 分类（古风/现代/悬疑等） |
| 11 | `rating` | `string` |  | 评级（全年龄/12+/16+） |
| 12 | `tags` | `string` |  | 标签（逗号分隔） |
| 13 | `summary` | `string` |  | 简介 |
| 14 | `original_url` | `string` |  | 原作链接 |
| 15 | `original_author` | `string` |  | 原作者 |
| 16 | `estimated_duration_min` | `bigint` |  | 预计阅读时长（分钟） |
| 17 | `total_beats` | `bigint` |  | beat 总数 |
| 18 | `total_endings` | `bigint` |  | 结局数 |
| 19 | `paid_beats_count` | `bigint` |  | 付费 beat 数（启动构图统计） |
| 20 | `status` | `bigint` |  | 状态：0=草稿 1=审核 2=已发布 3=下架 |
| 21 | `is_current` | `bigint` |  | 是否当前线上版本：1=是（同 story_id 仅 1 条） |
| 22 | `display_order` | `bigint` |  | 列表排序权重，DESC |
| 23 | `published_at` | `bigint` |  | 发布时间 |
| 24 | `create_time` | `bigint` |  | 创建时间 |
| 25 | `update_time` | `bigint` |  | 更新时间 |

---

## ods_db_vc_visual_novel_unlock_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_visual_novel_unlock_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `user_id` | `bigint` |  | 用户ID（DDB 分库键） |
| 3 | `story_id` | `string` |  | 剧本ID |
| 4 | `unlock_type` | `bigint` |  | 解锁粒度：1=beat 2=story_full（一期仅 1） |
| 5 | `target_id` | `string` |  | beat_id 或 story_id（按 unlock_type） |
| 6 | `unlock_source` | `string` |  | 来源：PAYMENT/SHARE/VIP/GIFT/ADMIN |
| 7 | `cost_inspiration` | `bigint` |  | 消耗灵感数量（SHARE=0） |
| 8 | `transaction_id` | `string` |  | 钱包流水号（SHARE=NULL） |
| 9 | `idempotency_key` | `string` |  | 幂等键（客户端生成，长度 16~64） |
| 10 | `create_time` | `bigint` |  | 创建时间 |

---

## ods_db_vc_voice_copy_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_voice_copy_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `voice_id` | `string` |  | 声音id |
| 3 | `voice_name` | `string` |  | 声音名称 |
| 4 | `voice_sex` | `int` |  | 声音性别 1男 2女 |
| 5 | `txt` | `string` |  | 声音预览文本 |
| 6 | `remark` | `string` |  | 备注 |
| 7 | `copy_status` | `int` |  | 是否复刻 -1否 1是 |
| 8 | `model_type` | `int` |  | 模型厂商 1豆包 |
| 9 | `created_by` | `string` |  | 创建人邮箱 |
| 10 | `updated_by` | `string` |  | 更新人邮箱 |
| 11 | `channel` | `int` |  | 数据渠道：1=虚拟人app（默认）2=lofter含义见DataChannelEnum |
| 12 | `status` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 13 | `create_time` | `bigint` |  | DB创建时间 |
| 14 | `update_time` | `bigint` |  | DB更新时间 |
| 15 | `file_name` | `string` |  | 音频文件名 |
| 16 | `type` | `int` |  | 音色类型 1付费 2公版 |
| 17 | `user_id` | `bigint` |  | 用户id |
| 18 | `tag` | `string` |  | 标签 |
| 19 | `original_id` | `string` |  | 原始id |

---

## ods_db_vc_zone_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_db_vc_zone_nd` |
| **描述** | 无描述 |
| **Owner** | virtual_character |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `name` | `string` |  | 名称 |
| 3 | `description` | `string` |  | 描述 |
| 4 | `head_img` | `string` |  | 头图 |
| 5 | `long_img` | `string` |  | 长图 |
| 6 | `square_img` | `string` |  | 方图 |
| 7 | `hand_pick_config` | `string` |  | 精选配置 |
| 8 | `feed_config` | `string` |  | feed配置 |
| 9 | `banner_config` | `string` |  | banner配置 |
| 10 | `popup_config` | `string` |  | 弹窗配置 |
| 11 | `rule_config` | `string` |  | 规则配置 |
| 12 | `share_config` | `string` |  | 分享配置 |
| 13 | `discover_config` | `string` |  | 发现配置 |
| 14 | `create_time` | `bigint` |  | 数据创建时间 |
| 15 | `update_time` | `bigint` |  | 数据更新时间 |
| 16 | `status` | `int` |  | 状态，1: 有效，0：无效 |

---

## ods_log_ab_platform_sdk_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_log_ab_platform_sdk_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6.8M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ablogversion` | `string` |  | 实验日志版本 |
| 2 | `appversion` | `string` |  | app版本 |
| 3 | `deviceid` | `string` |  | 设备id |
| 4 | `expid` | `bigint` |  | 实验id |
| 5 | `groupid` | `bigint` |  | 实验分组id |
| 6 | `os` | `string` |  | 系统 |
| 7 | `sceneid` | `int` |  | 场景id |
| 8 | `time` | `bigint` |  |  |
| 9 | `traceid` | `string` |  | 实验跟踪串 |
| 10 | `userid` | `string` |  | 实验用户id |
| 11 | `dt` | `string` |  |  |

---

## ods_log_ad_new_linkup_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_log_ad_new_linkup_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 25.5M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  |  |
| 2 | `channel` | `string` |  |  |
| 3 | `deviceid` | `string` |  |  |
| 4 | `platform` | `string` |  |  |
| 5 | `time` | `bigint` |  |  |
| 6 | `subchannel` | `string` |  |  |
| 7 | `adid` | `string` |  |  |
| 8 | `idfaimei` | `string` |  |  |
| 9 | `dadeviceid` | `string` |  |  |
| 10 | `newuserflag` | `string` |  |  |
| 11 | `matchtype` | `string` |  |  |
| 12 | `advertiserid` | `string` |  |  |
| 13 | `campaignid` | `string` |  |  |
| 14 | `aid` | `string` |  |  |
| 15 | `cid` | `string` |  |  |
| 16 | `actionid` | `string` |  |  |
| 17 | `custom_ouid` | `string` |  | 助推账号 |
| 18 | `photoid` | `string` |  | 视频素材id |
| 19 | `dt` | `string` |  | date partition field |

---

## ods_mda_app_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_mda_app_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1055.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1055.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `eventid` | `string` |  |  |
| 3 | `occurtime` | `bigint` |  |  |
| 4 | `user_id` | `bigint` |  | 虚拟人账号 |
| 5 | `user_code` | `bigint` |  | 对应lofter账号 |
| 6 | `appkey` | `string` |  |  |
| 7 | `appversion` | `string` |  |  |
| 8 | `appchannel` | `string` |  |  |
| 9 | `sessionuuid` | `string` |  |  |
| 10 | `deviceos` | `string` |  |  |
| 11 | `devicemodel` | `string` |  |  |
| 12 | `deviceadid` | `string` |  |  |
| 13 | `deviceidfv` | `string` |  |  |
| 14 | `deviceimei` | `string` |  |  |
| 15 | `deviceandroidid` | `string` |  |  |
| 16 | `oaid` | `string` |  | 安卓设备标识 oaid |
| 17 | `customudid` | `string` |  |  |
| 18 | `costtime` | `bigint` |  |  |
| 19 | `recid` | `string` |  |  |
| 20 | `itemid` | `string` |  |  |
| 21 | `itemtype` | `string` |  |  |
| 22 | `alginfo` | `string` |  |  |
| 23 | `scene` | `string` |  |  |
| 24 | `source` | `string` |  |  |
| 25 | `ip` | `string` |  |  |
| 26 | `kafkatime` | `bigint` |  |  |
| 27 | `action` | `string` |  |  |
| 28 | `actiontype` | `string` |  | 事件对应的行为类型:cell_exposure,cell_click,subscribe,search,like etc |
| 29 | `params` | `map<string, string>` |  |  |
| 30 | `deviceosversion` | `string` |  | 系统版本 |
| 31 | `dt` | `string` |  |  |

---

## ods_mda_app_raw_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_mda_app_raw_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 11.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 52 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `userid` | `string` |  |  |
| 4 | `username` | `string` |  |  |
| 5 | `appversion` | `string` |  |  |
| 6 | `appchannel` | `string` |  |  |
| 7 | `occurtime` | `bigint` |  |  |
| 8 | `category` | `string` |  |  |
| 9 | `label` | `string` |  |  |
| 10 | `customudid` | `string` |  |  |
| 11 | `sessionuuid` | `string` |  |  |
| 12 | `sdkversion` | `string` |  |  |
| 13 | `ip` | `string` |  |  |
| 14 | `deviceplatform` | `string` |  |  |
| 15 | `deviceos` | `string` |  |  |
| 16 | `deviceosversion` | `string` |  |  |
| 17 | `devicemodel` | `string` |  |  |
| 18 | `deviceresolution` | `string` |  |  |
| 19 | `deviceoldmacaddr` | `string` |  |  |
| 20 | `devicemacaddr` | `string` |  |  |
| 21 | `deviceadid` | `string` |  |  |
| 22 | `deviceidfv` | `string` |  |  |
| 23 | `deviceimei` | `string` |  |  |
| 24 | `devicenetwork` | `string` |  |  |
| 25 | `devicecarrier` | `string` |  |  |
| 26 | `appkey` | `string` |  |  |
| 27 | `city` | `string` |  |  |
| 28 | `timezone` | `string` |  |  |
| 29 | `localelanguage` | `string` |  |  |
| 30 | `localecountry` | `string` |  |  |
| 31 | `wifibssid` | `string` |  |  |
| 32 | `wifissid` | `string` |  |  |
| 33 | `costtime` | `bigint` |  |  |
| 34 | `source` | `string` |  |  |
| 35 | `deviceandroidid` | `string` |  |  |
| 36 | `sdktype` | `string` |  |  |
| 37 | `itemid` | `string` |  |  |
| 38 | `itemtype` | `string` |  |  |
| 39 | `recid` | `string` |  |  |
| 40 | `scene` | `string` |  |  |
| 41 | `action` | `string` |  |  |
| 42 | `tagname` | `string` |  |  |
| 43 | `layout` | `string` |  |  |
| 44 | `alginfo` | `string` |  |  |
| 45 | `attributes` | `map<string, string>` |  |  |
| 46 | `kafkatime` | `bigint` |  |  |
| 47 | `_source` | `string` |  |  |
| 48 | `region` | `string` |  |  |
| 49 | `country` | `string` |  |  |
| 50 | `oaid` | `string` |  |  |
| 51 | `datatype` | `string` |  |  |
| 52 | `dt` | `string` |  |  |

---

## ods_mda_vc_game_web_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_mda_vc_game_web_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3.5M |
| **是否分区表** | 是 |

### 字段详情

共 47 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | from deserializer |
| 2 | `eventid` | `string` |  | from deserializer |
| 3 | `urlpath` | `string` |  | from deserializer |
| 4 | `userid` | `string` |  | from deserializer |
| 5 | `region` | `string` |  | from deserializer |
| 6 | `screenheight` | `string` |  | from deserializer |
| 7 | `browserversion` | `string` |  | from deserializer |
| 8 | `referrerdomain` | `string` |  | from deserializer |
| 9 | `kafkatime` | `string` |  | from deserializer |
| 10 | `secondlevelsource` | `string` |  | from deserializer |
| 11 | `data` | `string` |  | from deserializer |
| 12 | `devicemodel` | `string` |  | from deserializer |
| 13 | `deviceresolution` | `string` |  | from deserializer |
| 14 | `deviceplatform` | `string` |  | from deserializer |
| 15 | `ipcity` | `string` |  | from deserializer |
| 16 | `hubbleid` | `string` |  | from deserializer |
| 17 | `sdkversion` | `string` |  | from deserializer |
| 18 | `city` | `string` |  | from deserializer |
| 19 | `timestamp` | `string` |  | from deserializer |
| 20 | `occurtime` | `string` |  | from deserializer |
| 21 | `activationtime` | `string` |  | from deserializer |
| 22 | `processtime` | `string` |  | from deserializer |
| 23 | `servertime` | `string` |  | from deserializer |
| 24 | `browser` | `string` |  | from deserializer |
| 25 | `deviceos` | `string` |  | from deserializer |
| 26 | `ipcountry` | `string` |  | from deserializer |
| 27 | `currenturl` | `string` |  | from deserializer |
| 28 | `deviceosversion` | `string` |  | from deserializer |
| 29 | `currentdomain` | `string` |  | from deserializer |
| 30 | `sdktype` | `string` |  | from deserializer |
| 31 | `country` | `string` |  | from deserializer |
| 32 | `ip` | `string` |  | from deserializer |
| 33 | `referrer` | `string` |  | from deserializer |
| 34 | `datatype` | `string` |  | from deserializer |
| 35 | `productkey` | `string` |  | from deserializer |
| 36 | `sessionuuid` | `string` |  | from deserializer |
| 37 | `screenwidth` | `string` |  | from deserializer |
| 38 | `pagetitle` | `string` |  | from deserializer |
| 39 | `appchannel` | `string` |  | from deserializer |
| 40 | `pageopenscene` | `string` |  | from deserializer |
| 41 | `firstlevelsource` | `string` |  | from deserializer |
| 42 | `ipprovince` | `string` |  | from deserializer |
| 43 | `appkey` | `string` |  | from deserializer |
| 44 | `costtime` | `string` |  | from deserializer |
| 45 | `attributes` | `map<string, string>` |  |  |
| 46 | `_source` | `string` |  |  |
| 47 | `dt` | `string` |  | date partition field |

---

## ods_mda_wap_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_mda_wap_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 302.9M |
| **是否分区表** | 是 |

### 字段详情

共 48 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | from deserializer |
| 2 | `eventid` | `string` |  | from deserializer |
| 3 | `urlpath` | `string` |  | from deserializer |
| 4 | `userid` | `string` |  | from deserializer |
| 5 | `region` | `string` |  | from deserializer |
| 6 | `screenheight` | `string` |  | from deserializer |
| 7 | `browserversion` | `string` |  | from deserializer |
| 8 | `referrerdomain` | `string` |  | from deserializer |
| 9 | `kafkatime` | `string` |  | from deserializer |
| 10 | `secondlevelsource` | `string` |  | from deserializer |
| 11 | `data` | `string` |  | from deserializer |
| 12 | `devicemodel` | `string` |  | from deserializer |
| 13 | `deviceresolution` | `string` |  | from deserializer |
| 14 | `deviceplatform` | `string` |  | from deserializer |
| 15 | `ipcity` | `string` |  | from deserializer |
| 16 | `hubbleid` | `string` |  | from deserializer |
| 17 | `sdkversion` | `string` |  | from deserializer |
| 18 | `city` | `string` |  | from deserializer |
| 19 | `timestamp` | `string` |  | from deserializer |
| 20 | `occurtime` | `string` |  | from deserializer |
| 21 | `activationtime` | `string` |  | from deserializer |
| 22 | `processtime` | `string` |  | from deserializer |
| 23 | `servertime` | `string` |  | from deserializer |
| 24 | `browser` | `string` |  | from deserializer |
| 25 | `deviceos` | `string` |  | from deserializer |
| 26 | `ipcountry` | `string` |  | from deserializer |
| 27 | `currenturl` | `string` |  | from deserializer |
| 28 | `deviceosversion` | `string` |  | from deserializer |
| 29 | `currentdomain` | `string` |  | from deserializer |
| 30 | `sdktype` | `string` |  | from deserializer |
| 31 | `country` | `string` |  | from deserializer |
| 32 | `ip` | `string` |  | from deserializer |
| 33 | `referrer` | `string` |  | from deserializer |
| 34 | `datatype` | `string` |  | from deserializer |
| 35 | `productkey` | `string` |  | from deserializer |
| 36 | `sessionuuid` | `string` |  | from deserializer |
| 37 | `screenwidth` | `string` |  | from deserializer |
| 38 | `pagetitle` | `string` |  | from deserializer |
| 39 | `appchannel` | `string` |  | from deserializer |
| 40 | `pageopenscene` | `string` |  | from deserializer |
| 41 | `firstlevelsource` | `string` |  | from deserializer |
| 42 | `ipprovince` | `string` |  | from deserializer |
| 43 | `appkey` | `string` |  | from deserializer |
| 44 | `costtime` | `string` |  | from deserializer |
| 45 | `attributes` | `map<string, string>` |  |  |
| 46 | `_source` | `string` |  |  |
| 47 | `useragent` | `string` |  | 浏览器ua |
| 48 | `dt` | `string` |  | date partition field |

---

## ods_vc_enum_code_a_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_enum_code_a_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 3.5K |
| **是否分区表** | 否 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `code` | `int` |  | from deserializer |
| 2 | `description` | `string` |  | from deserializer |
| 3 | `enumname` | `string` |  | from deserializer |

---

## ods_vc_h5_ectypal_chat_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_h5_ectypal_chat_i_d` |
| **描述** | ectypal_chat 消息数据表(扁平结构) |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 96.7M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `type` | `string` |  | 消息类型 |
| 2 | `data` | `row<bigint,bigint,bigint,int,int,string,int,bigint,int,bigint,int,int,int,bigint>('id','userid','userdupid','userduptype','sender','requestid','sortno','roleid','messagetype','sendmessagetime','costappstamina','costlofterstamina','coststatus','duplicateid')` |  | 消息数据主体 |
| 3 | `dt` | `string` |  |  |

---

## ods_vc_log_ai_chat_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_ai_chat_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 1060.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1060.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `model` | `int` |  | from deserializer |
| 5 | `modelname` | `string` |  | from deserializer |
| 6 | `requestjson` | `string` |  | from deserializer |
| 7 | `responsejson` | `string` |  | from deserializer |
| 8 | `rt` | `bigint` |  | from deserializer |
| 9 | `requesttime` | `string` |  | from deserializer |
| 10 | `userid` | `bigint` |  | from deserializer |
| 11 | `characterid` | `bigint` |  | from deserializer |
| 12 | `desc` | `string` |  | from deserializer |
| 13 | `airequestid` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_vc_log_ai_scene_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_ai_scene_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 556.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 556.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 33 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `model` | `int` |  | from deserializer |
| 5 | `modelname` | `string` |  | from deserializer |
| 6 | `aiscenecode` | `string` |  | from deserializer |
| 7 | `aiscenecodedesc` | `string` |  | from deserializer |
| 8 | `requestjson` | `string` |  | from deserializer |
| 9 | `responsejson` | `string` |  | from deserializer |
| 10 | `rt` | `int` |  | from deserializer |
| 11 | `requesttime` | `string` |  | from deserializer |
| 12 | `inputtokencount` | `int` |  | from deserializer |
| 13 | `outputtokencount` | `int` |  | from deserializer |
| 14 | `totaltokencount` | `int` |  | from deserializer |
| 15 | `finishreason` | `string` |  | from deserializer |
| 16 | `content` | `string` |  | from deserializer |
| 17 | `reasoningcontent` | `string` |  | from deserializer |
| 18 | `auditresult` | `string` |  | from deserializer |
| 19 | `auditmsg` | `string` |  | from deserializer |
| 20 | `outdataid` | `string` |  | from deserializer |
| 21 | `hitnonage` | `boolean` |  | from deserializer |
| 22 | `userid` | `bigint` |  | from deserializer |
| 23 | `characterid` | `bigint` |  | from deserializer |
| 24 | `desc` | `string` |  | from deserializer |
| 25 | `code` | `int` |  | from deserializer |
| 26 | `codedesc` | `string` |  | from deserializer |
| 27 | `errormsg` | `string` |  | from deserializer |
| 28 | `inputcost` | `double` |  | from deserializer |
| 29 | `outputcost` | `double` |  | from deserializer |
| 30 | `cachecost` | `double` |  | from deserializer |
| 31 | `cachetokencount` | `double` |  | from deserializer |
| 32 | `airequestid` | `string` |  | from deserializer |
| 33 | `dt` | `string` |  |  |

---

## ods_vc_log_antispam_audit_trace_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_antispam_audit_trace_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 485.4M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间，ISO-8601 带毫秒，如 2026-05-22T16:34:19.552 |
| 3 | `loghost` | `string` |  | 产生日志的主机名/容器名 |
| 4 | `usercode` | `string` |  | 用户编码（字符串，避免大整数精度丢失） |
| 5 | `channel` | `string` |  | 渠道 |
| 6 | `content` | `string` |  | 审核文本内容 |
| 7 | `auditresult` | `int` |  | 审核结果：0=通过 等 |
| 8 | `dataid` | `string` |  | 数据唯一ID（UUID） |
| 9 | `hitsublabels` | `string` |  | 命中的子标签，逗号分隔 |
| 10 | `hitnonage` | `boolean` |  | 是否命中未成年 |
| 11 | `hitsuicidetip` | `boolean` |  | 是否命中自杀提示 |
| 12 | `hitsuicideescalate` | `boolean` |  | 是否命中自杀升级 |
| 13 | `rt` | `int` |  | 审核耗时(ms) |
| 14 | `scenecode` | `string` |  | 场景编码，如 search |
| 15 | `businesscode` | `string` |  | 业务编码 |
| 16 | `businessdesc` | `string` |  | 业务描述 |
| 17 | `requesttime` | `string` |  | 请求时间，格式 yyyy-MM-dd HH:mm:ss |
| 18 | `userid` | `bigint` |  | 用户ID |
| 19 | `dt` | `string` |  |  |

---

## ods_vc_log_antispam_difference_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_antispam_difference_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.4G |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `aliservicename` | `string` |  |  |
| 5 | `alisuggestion` | `string` |  |  |
| 6 | `alimappingauditresult` | `string` |  |  |
| 7 | `alioriauditresult` | `string` |  |  |
| 8 | `alirt` | `string` |  |  |
| 9 | `yidunbusinessid` | `string` |  |  |
| 10 | `yidunmappingauditresult` | `string` |  |  |
| 11 | `yidunoriauditresult` | `string` |  |  |
| 12 | `yidunrt` | `string` |  |  |
| 13 | `content` | `string` |  |  |
| 14 | `usercode` | `string` |  |  |
| 15 | `requesttime` | `string` |  |  |
| 16 | `desc` | `string` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ods_vc_log_board_game_message_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_board_game_message_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 20.1M |
| **是否分区表** | 是 |

### 字段详情

共 24 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `userid` | `bigint` |  |  |
| 5 | `characterid` | `bigint` |  |  |
| 6 | `roomid` | `bigint` |  |  |
| 7 | `desc` | `string` |  |  |
| 8 | `ext` | `string` |  |  |
| 9 | `gamerecordid` | `bigint` |  |  |
| 10 | `messagetype` | `string` |  |  |
| 11 | `messagedirection` | `string` |  |  |
| 12 | `content` | `string` |  |  |
| 13 | `sender` | `int` |  |  |
| 14 | `sourceplayerid` | `bigint` |  |  |
| 15 | `gameroomid` | `bigint` |  |  |
| 16 | `status` | `int` |  |  |
| 17 | `oldstatus` | `int` |  |  |
| 18 | `newstatus` | `int` |  |  |
| 19 | `errorcode` | `int` |  |  |
| 20 | `errormsg` | `string` |  |  |
| 21 | `requestid` | `string` |  |  |
| 22 | `rt` | `int` |  |  |
| 23 | `requesttime` | `string` |  |  |
| 24 | `dt` | `string` |  |  |

---

## ods_vc_log_common_ai_invoke_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_common_ai_invoke_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 1267.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1267.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 37 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `airequestscenecode` | `string` |  | from deserializer |
| 5 | `needoutputaudit` | `boolean` |  | from deserializer |
| 6 | `needinputaudit` | `boolean` |  | from deserializer |
| 7 | `inputauditscene` | `string` |  | from deserializer |
| 8 | `outputauditscene` | `string` |  | from deserializer |
| 9 | `businessscene` | `string` |  | from deserializer |
| 10 | `modeltype` | `int` |  | from deserializer |
| 11 | `modelname` | `string` |  | from deserializer |
| 12 | `isyouth` | `boolean` |  | from deserializer |
| 13 | `airt` | `int` |  | from deserializer |
| 14 | `inputantirt` | `int` |  | from deserializer |
| 15 | `outputantirt` | `int` |  | from deserializer |
| 16 | `aiscenert` | `int` |  | from deserializer |
| 17 | `code` | `int` |  | from deserializer |
| 18 | `codedesc` | `string` |  | from deserializer |
| 19 | `errormsg` | `string` |  | from deserializer |
| 20 | `requestjson` | `string` |  | from deserializer |
| 21 | `responsejson` | `string` |  | from deserializer |
| 22 | `requesttime` | `string` |  | from deserializer |
| 23 | `inputtokencount` | `int` |  | from deserializer |
| 24 | `outputtokencount` | `int` |  | from deserializer |
| 25 | `totaltokencount` | `int` |  | from deserializer |
| 26 | `cachedtokencount` | `int` |  | from deserializer |
| 27 | `finishreason` | `string` |  | from deserializer |
| 28 | `content` | `string` |  | from deserializer |
| 29 | `reasoningcontent` | `string` |  | from deserializer |
| 30 | `auditresult` | `int` |  | from deserializer |
| 31 | `auditmsg` | `string` |  | from deserializer |
| 32 | `outdataid` | `string` |  | from deserializer |
| 33 | `hitnonage` | `boolean` |  | from deserializer |
| 34 | `userid` | `bigint` |  | from deserializer |
| 35 | `characterid` | `bigint` |  | from deserializer |
| 36 | `desc` | `string` |  | from deserializer |
| 37 | `dt` | `string` |  |  |

---

## ods_vc_log_low_quality_reply_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_low_quality_reply_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 646.5K |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `airequest` | `string` |  |  |
| 5 | `originalreply` | `string` |  |  |
| 6 | `lowqualitytypes` | `string` |  |  |
| 7 | `reason` | `string` |  |  |
| 8 | `retried` | `boolean` |  |  |
| 9 | `rt` | `int` |  |  |
| 10 | `checkcode` | `int` |  |  |
| 11 | `userid` | `bigint` |  |  |
| 12 | `characterid` | `bigint` |  |  |
| 13 | `dt` | `string` |  |  |

---

## ods_vc_log_message_type_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_message_type_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 1.4G |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `assettype` | `string` |  | from deserializer |
| 5 | `assetscene` | `string` |  | from deserializer |
| 6 | `sceneuniqid` | `string` |  | from deserializer |
| 7 | `talkcount` | `int` |  | from deserializer |
| 8 | `userid` | `bigint` |  | from deserializer |
| 9 | `characterid` | `bigint` |  | from deserializer |
| 10 | `desc` | `string` |  | from deserializer |
| 11 | `allowfreecount` | `int` |  | from deserializer |
| 12 | `dt` | `string` |  |  |

---

## ods_vc_log_order_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_order_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 17.0M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `orderid` | `bigint` |  | from deserializer |
| 5 | `itemid` | `int` |  | from deserializer |
| 6 | `userid` | `bigint` |  | from deserializer |
| 7 | `desc` | `string` |  | from deserializer |
| 8 | `dt` | `string` |  |  |

---

## ods_vc_log_user_activity_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_user_activity_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 52.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 52.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `activityid` | `int` |  | from deserializer |
| 5 | `moduleid` | `int` |  | from deserializer |
| 6 | `userid` | `bigint` |  | from deserializer |
| 7 | `desc` | `string` |  | from deserializer |
| 8 | `ext` | `string` |  | from deserializer |
| 9 | `dt` | `string` |  |  |

---

## ods_vc_log_vc_enum_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_vc_enum_i_d` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 8.8M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | from deserializer |
| 2 | `logtime` | `string` |  | from deserializer |
| 3 | `loghost` | `string` |  | from deserializer |
| 4 | `code` | `int` |  | from deserializer |
| 5 | `description` | `string` |  | from deserializer |
| 6 | `enumname` | `string` |  | from deserializer |
| 7 | `dt` | `string` |  |  |

---

## ods_vc_log_vgame_workshop_chat_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_log_vgame_workshop_chat_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.7M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型，如 vgame_workshop_chat |
| 2 | `logtime` | `string` |  | 日志时间，ISO-8601 带毫秒，如 2026-05-28T12:12:07.651 |
| 3 | `loghost` | `string` |  | 产生日志的主机名/容器名 |
| 4 | `userid` | `bigint` |  | 用户ID |
| 5 | `model` | `string` |  | 调用的模型名，如 deepseek-v4-flash |
| 6 | `tier` | `string` |  | 模型区分档位：basic，premium |
| 7 | `statuscode` | `int` |  | HTTP/业务状态码，200=成功 |
| 8 | `latencyms` | `bigint` |  | 端到端调用耗时(ms) |
| 9 | `truncated` | `boolean` |  | 响应是否被截断 |
| 10 | `request` | `string` |  | 原始请求 JSON 字符串，含 stream/messages/model/tools 等，按需   get_json_object 解析 |
| 11 | `output` | `string` |  | 模型输出 JSON 字符串，含 response/reasoningContent/toolCalls，按需   get_json_object 解析 |
| 12 | `usage` | `string` |  | Token 使用统计 JSON 字符串，含   promptTokens/completionTokens/totalTokens/cacheHitTokens/cacheMissTokens/reasoningTokens |
| 13 | `dt` | `string` |  |  |

---

## ods_vc_rec_pool_daily_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_rec_pool_daily_i_d` |
| **描述** | avg推荐后端日志 - vc |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 896.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 896.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ab` | `array<string>` |  |  |
| 2 | `cost` | `int` |  |  |
| 3 | `ext` | `row<string,string,string,string>('multiAbTestLayer','times','multiAbTestBucket','deviceInfo')` |  |  |
| 4 | `lf` | `boolean` |  |  |
| 5 | `multiab` | `row<string,string,string>('AGG','RECALL','MODEL')` |  |  |
| 6 | `placeholderplan` | `array<string>` |  |  |
| 7 | `recid` | `string` |  |  |
| 8 | `recitems` | `array<row<string,row<int,string,int,int>('adjustplan','algname','newpool','upflag'),string,string,string,map<string,string>>('alg','alginfoext','bi','ii','it','rs')>` |  |  |
| 9 | `recstarttime` | `bigint` |  |  |
| 10 | `req` | `row<string,string,string,boolean,int,int,string,boolean,int>('account','accountType','deviceInfo','mock','num','reback','sceneName','simu','times')` |  |  |
| 11 | `usemultiab` | `boolean` |  |  |
| 12 | `userid` | `string` |  |  |
| 13 | `dt` | `string` |  | 天 |

---

## ods_vc_rec_pool_daily_i_h

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_rec_pool_daily_i_h` |
| **描述** | avg推荐后端日志 - vc |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 50.6M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ab` | `array<string>` |  |  |
| 2 | `cost` | `int` |  |  |
| 3 | `ext` | `row<string,string,string,string>('multiAbTestLayer','times','multiAbTestBucket','deviceInfo')` |  |  |
| 4 | `lf` | `boolean` |  |  |
| 5 | `multiab` | `row<string,string,string>('AGG','RECALL','MODEL')` |  |  |
| 6 | `placeholderplan` | `array<string>` |  |  |
| 7 | `recid` | `string` |  |  |
| 8 | `recitems` | `array<row<string,row<int,string,int,int>('adjustplan','algname','newpool','upflag'),string,string,string,map<string,string>>('alg','alginfoext','bi','ii','it','rs')>` |  |  |
| 9 | `recstarttime` | `bigint` |  |  |
| 10 | `req` | `row<string,string,string,boolean,int,int,string,boolean,int>('account','accountType','deviceInfo','mock','num','reback','sceneName','simu','times')` |  |  |
| 11 | `usemultiab` | `boolean` |  |  |
| 12 | `userid` | `string` |  |  |
| 13 | `dt` | `string` |  | 天 |
| 14 | `h` | `string` |  |  |

---

## ods_vc_rec_pool_result_i_d

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_rec_pool_result_i_d` |
| **描述** | avg推荐后端日志结果 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 128.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 128.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `string` |  | 用户ID |
| 2 | `deviceid` | `string` |  | 设备信息 |
| 3 | `recstarttime` | `bigint` |  | 推荐时间 |
| 4 | `times` | `int` |  | 请求时间 |
| 5 | `scenename` | `string` |  | 场景名称 |
| 6 | `recid` | `string` |  | 推荐ID |
| 7 | `itemid` | `string` |  | 物品ID |
| 8 | `itemtype` | `string` |  | 物品类型 |
| 9 | `algname` | `string` |  | 算法名称 |
| 10 | `cost` | `int` |  | 消耗时间 |
| 11 | `lf` | `boolean` |  | 是否需要进入数据仓库 |
| 12 | `agg` | `string` |  | AB测试拓展信息-AGG |
| 13 | `recall` | `string` |  | AB测试拓展信息-RECALL |
| 14 | `model` | `string` |  | AB测试拓展信息-MODEL |
| 15 | `bi` | `string` |  | BI信息 |
| 16 | `rs` | `map<string, string>` |  | 推荐原因 |
| 17 | `usemultiab` | `boolean` |  | 是否使用多AB测试 |
| 18 | `req` | `row<string,string,string,boolean,int,int,string,boolean,int>('account','accountType','deviceInfo','mock','num','reback','sceneName','simu','times')` |  | 请求信息 |
| 19 | `recitem` | `row<string,row<int,string,int,int>('adjustplan','algname','newpool','upflag'),string,string,string,map<string,string>>('alg','algInfoExt','bi','ii','it','rs')` |  | 推荐物品信息 |
| 20 | `multiab` | `row<string,string,string>('AGG','RECALL','MODEL')` |  | AB测试拓展信息 |
| 21 | `ext` | `row<string,string,string,string>('multiAbTestLayer','times','multiAbTestBucket','deviceInfo')` |  | 扩展信息 |
| 22 | `placeholderplan` | `array<string>` |  | 占位计划 |
| 23 | `flow` | `string` |  | flow |
| 24 | `pos` | `int` |  | 下发列表中的相对位置 |
| 25 | `s_ctr` | `double` |  |  |
| 26 | `s_play` | `double` |  |  |
| 27 | `s_duration` | `double` |  |  |
| 28 | `s_pay` | `double` |  |  |
| 29 | `s_comment` | `double` |  |  |
| 30 | `s_default` | `double` |  |  |
| 31 | `dt` | `string` |  | 天 |

---

## ods_vc_rec_pool_result_i_h

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `ods_vc_rec_pool_result_i_h` |
| **描述** | avg推荐后端日志结果 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `string` |  | 用户ID |
| 2 | `deviceid` | `string` |  | 设备信息 |
| 3 | `recstarttime` | `bigint` |  | 推荐时间 |
| 4 | `times` | `int` |  | 请求时间 |
| 5 | `scenename` | `string` |  | 场景名称 |
| 6 | `recid` | `string` |  | 推荐ID |
| 7 | `itemid` | `string` |  | 物品ID |
| 8 | `itemtype` | `string` |  | 物品类型 |
| 9 | `algname` | `string` |  | 算法名称 |
| 10 | `cost` | `int` |  | 消耗时间 |
| 11 | `lf` | `boolean` |  | 是否需要进入数据仓库 |
| 12 | `agg` | `string` |  | AB测试拓展信息-AGG |
| 13 | `recall` | `string` |  | AB测试拓展信息-RECALL |
| 14 | `model` | `string` |  | AB测试拓展信息-MODEL |
| 15 | `bi` | `string` |  | BI信息 |
| 16 | `rs` | `map<string, string>` |  | 推荐原因 |
| 17 | `usemultiab` | `boolean` |  | 是否使用多AB测试 |
| 18 | `req` | `row<string,string,string,boolean,int,int,string,boolean,int>('account','accountType','deviceInfo','mock','num','reback','sceneName','simu','times')` |  | 请求信息 |
| 19 | `recitem` | `row<string,row<int,string,int,int>('adjustplan','algname','newpool','upflag'),string,string,string,map<string,string>>('alg','algInfoExt','bi','ii','it','rs')` |  | 推荐物品信息 |
| 20 | `multiab` | `row<string,string,string>('AGG','RECALL','MODEL')` |  | AB测试拓展信息 |
| 21 | `ext` | `row<string,string,string,string>('multiAbTestLayer','times','multiAbTestBucket','deviceInfo')` |  | 扩展信息 |
| 22 | `placeholderplan` | `array<string>` |  | 占位计划 |
| 23 | `flow` | `string` |  | flow |
| 24 | `pos` | `int` |  | 下发列表中的相对位置 |
| 25 | `s_ctr` | `double` |  |  |
| 26 | `s_play` | `double` |  |  |
| 27 | `s_duration` | `double` |  |  |
| 28 | `s_pay` | `double` |  |  |
| 29 | `s_comment` | `double` |  |  |
| 30 | `s_default` | `double` |  |  |
| 31 | `dt` | `string` |  | 天 |
| 32 | `h` | `string` |  |  |

---

## rec_vc_data_l2_rec_pool_daily

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `rec_vc_data_l2_rec_pool_daily` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 10.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 10.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `content` | `string` |  |  |
| 2 | `day` | `string` |  |  |

---

## rec_vc_data_l2_rec_pool_daily_hour_tmp

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `rec_vc_data_l2_rec_pool_daily_hour_tmp` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 50.6M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `data` | `string` |  |  |
| 2 | `dt` | `string` |  |  |
| 3 | `h` | `string` |  |  |

---

## rec_vc_data_l2_rec_pool_daily_tmp

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `vc` |
| **表名** | `rec_vc_data_l2_rec_pool_daily_tmp` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 896.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 896.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `content` | `string` |  |  |
| 2 | `dt` | `string` |  |  |

---

