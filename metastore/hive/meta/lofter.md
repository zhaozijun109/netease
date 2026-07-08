# 数据库: lofter

> 本文档包含数据库 `lofter` 中**最近 30 天内有更新**的 665 张表的元数据信息，用于 AI 训练学习。
> （库内共 842 张表，177 张表因超过 30 天未更新已被过滤）

## 表目录

1. [ads_iad_lofter_exp_click_outer_di](#ads_iad_lofter_exp_click_outer_di) - 和LOFTER对数表
2. [ads_iad_lofter_info_flow_di](#ads_iad_lofter_info_flow_di) - 和LOFTER对数表
3. [ads_lofter_exp_click_checkout_di](#ads_lofter_exp_click_checkout_di) - 麦穗lofter数据备份
4. [bridge_ad_content_collection](#bridge_ad_content_collection) - 无描述
5. [bridge_ad_content_collection_dd](#bridge_ad_content_collection_dd) - 内容广告付费合集
6. [bridge_ad_position_user_group_dd](#bridge_ad_position_user_group_dd) - 无描述
7. [bridge_c2c_product_post_dd](#bridge_c2c_product_post_dd) - 无描述
8. [bridge_calendar_date](#bridge_calendar_date) - 无描述
9. [bridge_calendar_date_dd](#bridge_calendar_date_dd) - 无描述
10. [bridge_collection_post](#bridge_collection_post) - 无描述
11. [bridge_collection_post_dd](#bridge_collection_post_dd) - 无描述
12. [bridge_exp_metric](#bridge_exp_metric) - 无描述
13. [bridge_exp_metric_dd](#bridge_exp_metric_dd) - 无描述
14. [category_domain_mapping](#category_domain_mapping) - 无描述
15. [dep175_drpf_mplus_api_data_day](#dep175_drpf_mplus_api_data_day) - 无描述
16. [device_active](#device_active) - 无描述
17. [device_new](#device_new) - 无描述
18. [device_retain](#device_retain) - 无描述
19. [device_return](#device_return) - 无描述
20. [dim_act_accompany_tag](#dim_act_accompany_tag) - 无描述
21. [dim_act_premium_grain_tag](#dim_act_premium_grain_tag) - 无描述
22. [dim_actpwd_dd](#dim_actpwd_dd) - 无描述
23. [dim_ad_dsp_dd](#dim_ad_dsp_dd) - 无描述
24. [dim_ad_dsp_slot_dd](#dim_ad_dsp_slot_dd) - 无描述
25. [dim_ad_position_dd](#dim_ad_position_dd) - 无描述
26. [dim_ad_request_dd](#dim_ad_request_dd) - 无描述
27. [dim_benefit_product](#dim_benefit_product) - 无描述
28. [dim_benefit_product_category](#dim_benefit_product_category) - 无描述
29. [dim_benefit_sku](#dim_benefit_sku) - 无描述
30. [dim_black_tag](#dim_black_tag) - 无描述
31. [dim_blog](#dim_blog) - 无描述
32. [dim_bookstore_post](#dim_bookstore_post) - 无描述
33. [dim_bookstore_post_dd](#dim_bookstore_post_dd) - 无描述
34. [dim_c2c_product_dd](#dim_c2c_product_dd) - 无描述
35. [dim_daren](#dim_daren) - 无描述
36. [dim_date](#dim_date) - 无描述
37. [dim_date_rolling](#dim_date_rolling) - 无描述
38. [dim_domain](#dim_domain) - 无描述
39. [dim_game_post_dd](#dim_game_post_dd) - 无描述
40. [dim_gift](#dim_gift) - 无描述
41. [dim_gift_dd](#dim_gift_dd) - 无描述
42. [dim_gift_post](#dim_gift_post) - 无描述
43. [dim_gift_post_dd](#dim_gift_post_dd) - 无描述
44. [dim_gift_post_return](#dim_gift_post_return) - 无描述
45. [dim_gift_post_return_dd](#dim_gift_post_return_dd) - 无描述
46. [dim_grain_creator_dd](#dim_grain_creator_dd) - 无描述
47. [dim_ip_dd](#dim_ip_dd) - 无描述
48. [dim_ip_extend](#dim_ip_extend) - 无描述
49. [dim_ip_extend_dd](#dim_ip_extend_dd) - 无描述
50. [dim_kol_channel_dd](#dim_kol_channel_dd) - 无描述
51. [dim_membership_collection_dd](#dim_membership_collection_dd) - 会员合集
52. [dim_membership_post_dd](#dim_membership_post_dd) - 会员文章池
53. [dim_membership_post_vip_dd](#dim_membership_post_vip_dd) - 会员vip免费文章池
54. [dim_miniprogram_post](#dim_miniprogram_post) - 无描述
55. [dim_miniprogram_post_dd](#dim_miniprogram_post_dd) - 无描述
56. [dim_post](#dim_post) - 文章维表
57. [dim_post_article](#dim_post_article) - 无描述
58. [dim_post_category_dd](#dim_post_category_dd) - 文章类目：文章对应唯一主类目
59. [dim_post_category_set_dd](#dim_post_category_set_dd) - 文章类目： 单篇文章可能对应多条类目
60. [dim_post_talk](#dim_post_talk) - 无描述
61. [dim_pve_user](#dim_pve_user) - 无描述
62. [dim_pve_user_dd](#dim_pve_user_dd) - 无描述
63. [dim_seven_group_ip_category_res_dd](#dim_seven_group_ip_category_res_dd) - 无描述
64. [dim_tag](#dim_tag) - 无描述
65. [dim_tag_dd](#dim_tag_dd) - 无描述
66. [dim_user](#dim_user) - 无描述
67. [dim_video](#dim_video) - 无描述
68. [dim_video_dd](#dim_video_dd) - 视频域唯一新建 DIM 表 · dim_post(视频帖) LEFT JOIN ods_db_video_post_nd + embed JSON 解析
69. [dwb_par_lofter_device_tag_wd](#dwb_par_lofter_device_tag_wd) - 无描述
70. [dwb_par_lofter_music_user_label_di](#dwb_par_lofter_music_user_label_di) - lofter&音乐常态化活动，对接人肖乃同
71. [dwb_par_lofter_tag_wd](#dwb_par_lofter_tag_wd) - lofter用户标签表
72. [dwd_ab_platform_exp_user_di](#dwd_ab_platform_exp_user_di) - 无描述
73. [dwd_act_card_action_di](#dwd_act_card_action_di) - 无描述
74. [dwd_act_music_user_label_pool_di](#dwd_act_music_user_label_pool_di) - 无描述
75. [dwd_act_paper_man_action_di](#dwd_act_paper_man_action_di) - 无描述
76. [dwd_act_pve_action_di](#dwd_act_pve_action_di) - 无描述
77. [dwd_act_tag_big_event_detail_di](#dwd_act_tag_big_event_detail_di) - 圈层大事件底表
78. [dwd_activity_action_di](#dwd_activity_action_di) - 无描述
79. [dwd_ad_action_click_di](#dwd_ad_action_click_di) - 无描述
80. [dwd_ad_action_dsp_request_di](#dwd_ad_action_dsp_request_di) - 无描述
81. [dwd_ad_action_expose_di](#dwd_ad_action_expose_di) - 无描述
82. [dwd_ad_action_fill_di](#dwd_ad_action_fill_di) - 无描述
83. [dwd_ad_action_stock_di](#dwd_ad_action_stock_di) - 无描述
84. [dwd_ad_action_win_di](#dwd_ad_action_win_di) - 无描述
85. [dwd_ad_actions_di](#dwd_ad_actions_di) - 无描述
86. [dwd_ad_actions_v2_di](#dwd_ad_actions_v2_di) - 无描述
87. [dwd_ad_amount_per_user_di](#dwd_ad_amount_per_user_di) - 每天每个用户为不同广告位带来的的广告营收
88. [dwd_ad_content_unlock_di](#dwd_ad_content_unlock_di) - 内容广告业务解锁
89. [dwd_ad_content_unlock_sdk_di](#dwd_ad_content_unlock_sdk_di) - 内容广告sdk解锁
90. [dwd_ad_dsp_win_fill_di](#dwd_ad_dsp_win_fill_di) - 无描述
91. [dwd_ad_growth_device_di](#dwd_ad_growth_device_di) - 无描述
92. [dwd_ad_growth_new_di](#dwd_ad_growth_new_di) - 无描述
93. [dwd_ad_growth_order_activate_di](#dwd_ad_growth_order_activate_di) - 无描述
94. [dwd_ad_growth_return_di](#dwd_ad_growth_return_di) - 无描述
95. [dwd_ad_req_di](#dwd_ad_req_di) - 无描述
96. [dwd_ad_resource_action_di](#dwd_ad_resource_action_di) - 无描述
97. [dwd_ad_resource_monitor_close_di](#dwd_ad_resource_monitor_close_di) - 无描述
98. [dwd_ad_reward_score_log_di](#dwd_ad_reward_score_log_di) - 无描述
99. [dwd_ad_reward_score_product_exchange_di](#dwd_ad_reward_score_product_exchange_di) - 无描述
100. [dwd_ad_reward_task_complete_di](#dwd_ad_reward_task_complete_di) - 无描述
101. [dwd_antispam_copy_and_callback_di](#dwd_antispam_copy_and_callback_di) - 无描述
102. [dwd_ask_publish_di](#dwd_ask_publish_di) - 无描述
103. [dwd_beginner_guide_page_events_di](#dwd_beginner_guide_page_events_di) - 用户与AB实验相关的行为统计
104. [dwd_benefit_trade_order_product_dd](#dwd_benefit_trade_order_product_dd) - 无描述
105. [dwd_blog_follow_di](#dwd_blog_follow_di) - 无描述
106. [dwd_blog_intro_sensitive_word_di](#dwd_blog_intro_sensitive_word_di) - 无描述
107. [dwd_blog_nickname_sensitive_word_di](#dwd_blog_nickname_sensitive_word_di) - 无描述
108. [dwd_cc_module_query_di](#dwd_cc_module_query_di) - 无描述
109. [dwd_cold_ip_dd](#dwd_cold_ip_dd) - 冷圈ip
110. [dwd_collection_detail_di](#dwd_collection_detail_di) - 无描述
111. [dwd_collection_user_subscribe_dd](#dwd_collection_user_subscribe_dd) - 无描述
112. [dwd_content_browse_di](#dwd_content_browse_di) - 无描述
113. [dwd_coupon_order_di](#dwd_coupon_order_di) - 无描述
114. [dwd_device_all_dd](#dwd_device_all_dd) - 无描述
115. [dwd_device_apk_install_dd](#dwd_device_apk_install_dd) - 无描述
116. [dwd_device_collect_apk_di](#dwd_device_collect_apk_di) - 无描述
117. [dwd_device_growth_attribution_di](#dwd_device_growth_attribution_di) - 无描述
118. [dwd_device_growth_attribution_v2_di](#dwd_device_growth_attribution_v2_di) - 无描述
119. [dwd_device_growth_content_di](#dwd_device_growth_content_di) - 无描述
120. [dwd_device_mapping](#dwd_device_mapping) - 无描述
121. [dwd_device_mapping_detail_di](#dwd_device_mapping_detail_di) - 无描述
122. [dwd_dstr_flow_task_action_di](#dwd_dstr_flow_task_action_di) - 无描述
123. [dwd_dstr_flow_task_post_dd](#dwd_dstr_flow_task_post_dd) - 无描述
124. [dwd_ec_add_cart_di](#dwd_ec_add_cart_di) - 无描述
125. [dwd_ec_derivate_gmv_di](#dwd_ec_derivate_gmv_di) - 电商衍生品订单GMV明细表
126. [dwd_ec_derivate_ipseries_category_refund_di](#dwd_ec_derivate_ipseries_category_refund_di) - 电商衍生品ip系列退收明细表
127. [dwd_ec_derivate_ipseries_category_revenue_di](#dwd_ec_derivate_ipseries_category_revenue_di) - 电商衍生品ip系列收入明细表
128. [dwd_ec_derivate_refund_di](#dwd_ec_derivate_refund_di) - 电商衍生品订单退收明细表
129. [dwd_ec_derivate_revenue_di](#dwd_ec_derivate_revenue_di) - 电商衍生品订单收入明细表
130. [dwd_ec_product_expose_di](#dwd_ec_product_expose_di) - 无描述
131. [dwd_ec_product_order_di](#dwd_ec_product_order_di) - 无描述
132. [dwd_ec_trace_product_expose_di](#dwd_ec_trace_product_expose_di) - 无描述
133. [dwd_ec_trace_product_view_di](#dwd_ec_trace_product_view_di) - 无描述
134. [dwd_ec_yanxuan_ad_click_di](#dwd_ec_yanxuan_ad_click_di) - 无描述
135. [dwd_ec_yanxuan_order_di](#dwd_ec_yanxuan_order_di) - 无描述
136. [dwd_ecology_ai_infringe_post_dd](#dwd_ecology_ai_infringe_post_dd) - 无描述
137. [dwd_emote_dun_ti_record_di](#dwd_emote_dun_ti_record_di) - 无描述
138. [dwd_evt_avatar_box_access_di](#dwd_evt_avatar_box_access_di) - 无描述
139. [dwd_evt_benefit_page_view_di](#dwd_evt_benefit_page_view_di) - 无描述
140. [dwd_evt_post_paid_detail_dd](#dwd_evt_post_paid_detail_dd) - 无描述
141. [dwd_evt_user_login_di](#dwd_evt_user_login_di) - 无描述
142. [dwd_evt_webview_index_di](#dwd_evt_webview_index_di) - 无描述
143. [dwd_gift_ai_blogs_human_review_di](#dwd_gift_ai_blogs_human_review_di) - 无描述
144. [dwd_gift_post_order_dd](#dwd_gift_post_order_dd) - 无描述
145. [dwd_gift_post_unlock_dd](#dwd_gift_post_unlock_dd) - 无描述
146. [dwd_gift_return_post_dd](#dwd_gift_return_post_dd) - 无描述
147. [dwd_growth_actpwd_access_di](#dwd_growth_actpwd_access_di) - 无描述
148. [dwd_growth_harmony_device_di](#dwd_growth_harmony_device_di) - 无描述
149. [dwd_growth_vertical_category_crowd_di](#dwd_growth_vertical_category_crowd_di) - 无描述
150. [dwd_home_top_resource_visit_di](#dwd_home_top_resource_visit_di) - 首页吸顶资源访问明细 - 按天分区
151. [dwd_issue_with_status_change_for_lofter](#dwd_issue_with_status_change_for_lofter) - 无描述
152. [dwd_issue_with_status_change_for_vc](#dwd_issue_with_status_change_for_vc) - 无描述
153. [dwd_liaoliao_mda_base](#dwd_liaoliao_mda_base) - 无描述
154. [dwd_lucky_boy_record_di](#dwd_lucky_boy_record_di) - 无描述
155. [dwd_lucky_boy_result_di](#dwd_lucky_boy_result_di) - 无描述
156. [dwd_membership_order_di](#dwd_membership_order_di) - 无描述
157. [dwd_membership_vip_post_browse_di](#dwd_membership_vip_post_browse_di) - 无描述
158. [dwd_miniprogram_order_di](#dwd_miniprogram_order_di) - 无描述
159. [dwd_paid_gift_postid_info_nd](#dwd_paid_gift_postid_info_nd) - 无描述
160. [dwd_paid_post_detail_dd](#dwd_paid_post_detail_dd) - 无描述
161. [dwd_paid_subscribe_device_cpa_deduplicate_di](#dwd_paid_subscribe_device_cpa_deduplicate_di) - 无描述
162. [dwd_paid_subscribe_order_di](#dwd_paid_subscribe_order_di) - 无描述
163. [dwd_paid_subscribe_silent_user_activate_di](#dwd_paid_subscribe_silent_user_activate_di) - 无描述
164. [dwd_par_creator_first_publish_di](#dwd_par_creator_first_publish_di) - 无描述
165. [dwd_par_device_all_dd](#dwd_par_device_all_dd) - 无描述
166. [dwd_post_audio_di](#dwd_post_audio_di) - 无描述
167. [dwd_post_audit_di](#dwd_post_audit_di) - 无描述
168. [dwd_post_browse_di](#dwd_post_browse_di) - 无描述
169. [dwd_post_collection_di](#dwd_post_collection_di) - 无描述
170. [dwd_post_expose_di](#dwd_post_expose_di) - 无描述
171. [dwd_post_group_post_list_di](#dwd_post_group_post_list_di) - 无描述
172. [dwd_post_hot_di](#dwd_post_hot_di) - 无描述
173. [dwd_post_length_dd](#dwd_post_length_dd) - 无描述
174. [dwd_post_publish_di](#dwd_post_publish_di) - 无描述
175. [dwd_post_response_di](#dwd_post_response_di) - 无描述
176. [dwd_post_share_di](#dwd_post_share_di) - 无描述
177. [dwd_post_status](#dwd_post_status) - 无描述
178. [dwd_post_status_dd](#dwd_post_status_dd) - 无描述
179. [dwd_post_talk_browse_di](#dwd_post_talk_browse_di) - 无描述
180. [dwd_post_talk_discuss_score_di](#dwd_post_talk_discuss_score_di) - 无描述
181. [dwd_post_talk_expose_di](#dwd_post_talk_expose_di) - 无描述
182. [dwd_post_talk_hot_di](#dwd_post_talk_hot_di) - 无描述
183. [dwd_post_talk_page_view_di](#dwd_post_talk_page_view_di) - 短内容功能页面曝光明细表
184. [dwd_post_talk_publish_di](#dwd_post_talk_publish_di) - 无描述
185. [dwd_post_talk_response_di](#dwd_post_talk_response_di) - 无描述
186. [dwd_post_talk_share_di](#dwd_post_talk_share_di) - 无描述
187. [dwd_post_text_length_di](#dwd_post_text_length_di) - 无描述
188. [dwd_push_action_di](#dwd_push_action_di) - 无描述
189. [dwd_pve_ml_order_di](#dwd_pve_ml_order_di) - 无描述
190. [dwd_pve_music_page_access_di](#dwd_pve_music_page_access_di) - 无描述
191. [dwd_pve_music_user_dialogue_di](#dwd_pve_music_user_dialogue_di) - 无描述
192. [dwd_pve_user_amount_info_di](#dwd_pve_user_amount_info_di) - 无描述
193. [dwd_pve_user_chats_active_di](#dwd_pve_user_chats_active_di) - 无描述
194. [dwd_pve_user_chats_group_info_di](#dwd_pve_user_chats_group_info_di) - 无描述
195. [dwd_pve_user_chats_info_di](#dwd_pve_user_chats_info_di) - 无描述
196. [dwd_pve_user_chats_low_active_di](#dwd_pve_user_chats_low_active_di) - 无描述
197. [dwd_pve_user_chats_new_di](#dwd_pve_user_chats_new_di) - 无描述
198. [dwd_pve_user_chats_return_di](#dwd_pve_user_chats_return_di) - 无描述
199. [dwd_pve_user_grass_stamina_log_di](#dwd_pve_user_grass_stamina_log_di) - 无描述
200. [dwd_pve_user_interview_active_di](#dwd_pve_user_interview_active_di) - 无描述
201. [dwd_pve_user_interview_low_active_di](#dwd_pve_user_interview_low_active_di) - 无描述
202. [dwd_pve_user_interview_new_di](#dwd_pve_user_interview_new_di) - 无描述
203. [dwd_pve_user_interview_return_di](#dwd_pve_user_interview_return_di) - 无描述
204. [dwd_pve_user_props_stamina_log_di](#dwd_pve_user_props_stamina_log_di) - 无描述
205. [dwd_pve_user_stamina_log_di](#dwd_pve_user_stamina_log_di) - 无描述
206. [dwd_pve_user_sweet_stamina_log_di](#dwd_pve_user_sweet_stamina_log_di) - 无描述
207. [dwd_pve_user_timed_stamina_log_di](#dwd_pve_user_timed_stamina_log_di) - 无描述
208. [dwd_rec_content_understand_dd](#dwd_rec_content_understand_dd) - 无描述
209. [dwd_rec_post_review_di](#dwd_rec_post_review_di) - 无描述
210. [dwd_rec_reason_scene_di](#dwd_rec_reason_scene_di) - 推荐侧推荐理由及埋点数据
211. [dwd_return_user_push_publish_success_di](#dwd_return_user_push_publish_success_di) - 无描述
212. [dwd_rewardcenter_user_di](#dwd_rewardcenter_user_di) - 权益中心用户日活及新增标记
213. [dwd_rewardcenter_visit_di](#dwd_rewardcenter_visit_di) - 权益中心埋点访问明细
214. [dwd_risk_brush_hot_suspect_post_rank_ip_posts_di](#dwd_risk_brush_hot_suspect_post_rank_ip_posts_di) - 无描述
215. [dwd_risk_brush_hot_suspect_post_rank_ip_users_di](#dwd_risk_brush_hot_suspect_post_rank_ip_users_di) - 无描述
216. [dwd_risk_brush_hot_suspect_post_rank_posts_di](#dwd_risk_brush_hot_suspect_post_rank_posts_di) - 无描述
217. [dwd_risk_brush_hot_suspect_post_rank_users_di](#dwd_risk_brush_hot_suspect_post_rank_users_di) - 无描述
218. [dwd_risk_brush_hot_suspect_users_di](#dwd_risk_brush_hot_suspect_users_di) - 无描述
219. [dwd_risk_offsite_induction_return_gift_di](#dwd_risk_offsite_induction_return_gift_di) - 无描述
220. [dwd_risk_shuare_model_di](#dwd_risk_shuare_model_di) - 无描述
221. [dwd_risk_shuare_post_model_di](#dwd_risk_shuare_post_model_di) - 文章刷热模型基础指标， 计算7日内热度行为文章数据
222. [dwd_risk_user_level_dd](#dwd_risk_user_level_dd) - 无描述
223. [dwd_search_action_di](#dwd_search_action_di) - 无描述
224. [dwd_subject_bubble_action_di](#dwd_subject_bubble_action_di) - 无描述
225. [dwd_suspect_shuare_model_di](#dwd_suspect_shuare_model_di) - 无描述
226. [dwd_suspect_shuare_model_post_rank_di](#dwd_suspect_shuare_model_post_rank_di) - 无描述
227. [dwd_tag_browse_di](#dwd_tag_browse_di) - 无描述
228. [dwd_tag_ip_mapping_nd](#dwd_tag_ip_mapping_nd) - 无描述
229. [dwd_tag_subscribe_di](#dwd_tag_subscribe_di) - 无描述
230. [dwd_ue_report_di](#dwd_ue_report_di) - 无描述
231. [dwd_user_active_di](#dwd_user_active_di) - 无描述
232. [dwd_user_ad_revenue_di](#dwd_user_ad_revenue_di) - 无描述
233. [dwd_user_black_hit_rule_di](#dwd_user_black_hit_rule_di) - 无描述
234. [dwd_user_events_di](#dwd_user_events_di) - 无描述
235. [dwd_user_group_user_list_di](#dwd_user_group_user_list_di) - 无描述
236. [dwd_user_low_active_di](#dwd_user_low_active_di) - 无描述
237. [dwd_user_new_di](#dwd_user_new_di) - 无描述
238. [dwd_user_order_dd](#dwd_user_order_dd) - 无描述
239. [dwd_user_retention_di](#dwd_user_retention_di) - 无描述
240. [dwd_user_return_di](#dwd_user_return_di) - 无描述
241. [dwd_user_white_list_dd](#dwd_user_white_list_dd) - 无描述
242. [dwd_userfolder_action_di](#dwd_userfolder_action_di) - 无描述
243. [dwd_vc_device_new_di](#dwd_vc_device_new_di) - 无描述
244. [dwd_vc_device_return_di](#dwd_vc_device_return_di) - 无描述
245. [dwd_video_cover_edit_di](#dwd_video_cover_edit_di) - 视频封面编辑事件明细 · 字段定义待客户端 schema 确认后细化
246. [dwd_video_icloud_download_di](#dwd_video_icloud_download_di) - 视频源 iCloud 下载事件明细 · 仅 iOS · 当前埋点仅上报失败/取消, 成功事件标识待客户端补充
247. [dwd_video_play_di](#dwd_video_play_di) - 视频播放会话明细 · 按 reqId 聚合一次完整播放的全量事件
248. [dwd_video_publish_di](#dwd_video_publish_di) - 视频发布明细 · dwd_post_publish_di (视频帖) JOIN dim_video_dd
249. [dwd_video_publish_funnel_di](#dwd_video_publish_funnel_di) - 视频上传发布漏斗明细 · 按 req_id 聚合从点击发布到接口成功的全流程
250. [dwd_video_quality_event_di](#dwd_video_quality_event_di) - 视频清晰度切换事件明细 · 一行 = 一次切换
251. [dwd_vote_record_di](#dwd_vote_record_di) - 无描述
252. [dwd_ycy_ad_user_device_actions_di](#dwd_ycy_ad_user_device_actions_di) - 易次元广告数据
253. [dws_ab_platform_active_user_metric_di](#dws_ab_platform_active_user_metric_di) - 活跃用户的指标(当日活跃-回流-新用户)
254. [dws_ab_platform_ad_metric_di](#dws_ab_platform_ad_metric_di) - AB实验平台_广告指标明细表
255. [dws_ab_platform_client_metric_di](#dws_ab_platform_client_metric_di) - ab实验客户端指标
256. [dws_ab_platform_device_metric_di](#dws_ab_platform_device_metric_di) - 无描述
257. [dws_ab_platform_ecology_collection_metric_di](#dws_ab_platform_ecology_collection_metric_di) - ab实验合集指标
258. [dws_ab_platform_ecology_creator_metric_di](#dws_ab_platform_ecology_creator_metric_di) - ab实验指标
259. [dws_ab_platform_ecology_fullsite_metric_di](#dws_ab_platform_ecology_fullsite_metric_di) - ab实验指标
260. [dws_ab_platform_ecology_scene_metric_di](#dws_ab_platform_ecology_scene_metric_di) - ab实验合集指标
261. [dws_ab_platform_exp10_metric_di](#dws_ab_platform_exp10_metric_di) - 无描述
262. [dws_ab_platform_exp8_metric_di](#dws_ab_platform_exp8_metric_di) - AB实验8原子指标表
263. [dws_ab_platform_experiment_metric_di](#dws_ab_platform_experiment_metric_di) - 无描述
264. [dws_ab_platform_new_user_metric_di](#dws_ab_platform_new_user_metric_di) - 新用户的指标
265. [dws_ab_platform_paycontent_membership_metric_di](#dws_ab_platform_paycontent_membership_metric_di) - AB实验平台_内容付费_会员指标明细表
266. [dws_ab_platform_paycontent_metric_di](#dws_ab_platform_paycontent_metric_di) - ab实验平台内容付费扩展总表
267. [dws_ab_platform_paycontent_metric_v2_di](#dws_ab_platform_paycontent_metric_v2_di) - AB实验原子指标表
268. [dws_ab_platform_paycontent_scene_metric_di](#dws_ab_platform_paycontent_scene_metric_di) - ab实验平台内容付费-场景原子指标表
269. [dws_ab_platform_push_user_device_metric_di](#dws_ab_platform_push_user_device_metric_di) - ab实验Push用户和设备指标
270. [dws_ab_platform_pve_metric_di](#dws_ab_platform_pve_metric_di) - ab实验平pve原子指标表
271. [dws_ab_platform_pve_metric_expand_di](#dws_ab_platform_pve_metric_expand_di) - ab实验平台pve的指标汇总展开表
272. [dws_ab_platform_return_user_metric_di](#dws_ab_platform_return_user_metric_di) - 回流用户的指标
273. [dws_ab_platform_rewardcenter_metric_di](#dws_ab_platform_rewardcenter_metric_di) - ab实验指标-权益中心
274. [dws_ab_platform_user_metric_di](#dws_ab_platform_user_metric_di) - 不区分新用户
275. [dws_act_card_cvr_di](#dws_act_card_cvr_di) - 无描述
276. [dws_act_tag_big_event_di](#dws_act_tag_big_event_di) - 圈层大事件
277. [dws_act_tag_honor_name_di](#dws_act_tag_honor_name_di) - 圈层荣誉
278. [dws_act_tag_ship_score_di](#dws_act_tag_ship_score_di) - tag嗑力值T+1增量表
279. [dws_anti_spam_user_behavior_nd](#dws_anti_spam_user_behavior_nd) - 无描述
280. [dws_c2c_product_dd](#dws_c2c_product_dd) - 无描述
281. [dws_category_user_consume_dd](#dws_category_user_consume_dd) - 无描述
282. [dws_category_user_consume_di](#dws_category_user_consume_di) - 无描述
283. [dws_collection_dd](#dws_collection_dd) - 无描述
284. [dws_collection_revisit_di](#dws_collection_revisit_di) - 无描述
285. [dws_collection_scene_agg_bm_di](#dws_collection_scene_agg_bm_di) - 无描述
286. [dws_collection_scene_bm_di](#dws_collection_scene_bm_di) - 无描述
287. [dws_collection_subscribe_user_browse_latest_di](#dws_collection_subscribe_user_browse_latest_di) - 无描述
288. [dws_creator_browse_users_di](#dws_creator_browse_users_di) - 无描述
289. [dws_creator_gift_users_di](#dws_creator_gift_users_di) - 无描述
290. [dws_creator_valid_detail_di](#dws_creator_valid_detail_di) - 无描述
291. [dws_device_growth_dau_stratify_di](#dws_device_growth_dau_stratify_di) - 无描述
292. [dws_device_ip_interest_di](#dws_device_ip_interest_di) - 无描述
293. [dws_device_tag_interest_di](#dws_device_tag_interest_di) - 无描述
294. [dws_deviceudid_new_sum_ratio_dd](#dws_deviceudid_new_sum_ratio_dd) - 无描述
295. [dws_deviceudid_postid_core_act_di](#dws_deviceudid_postid_core_act_di) - 无描述
296. [dws_ecology_ai_infringe_blog_dd](#dws_ecology_ai_infringe_blog_dd) - 无描述
297. [dws_ecology_ai_infringe_post_dd](#dws_ecology_ai_infringe_post_dd) - 无描述
298. [dws_evt_login_user_last_dd](#dws_evt_login_user_last_dd) - 无描述
299. [dws_gift_post_di](#dws_gift_post_di) - 无描述
300. [dws_gift_post_premium_ip_scoring_dd](#dws_gift_post_premium_ip_scoring_dd) - 无描述
301. [dws_gift_post_premium_post_score_di](#dws_gift_post_premium_post_score_di) - 无描述
302. [dws_gift_post_premium_scoring_dd](#dws_gift_post_premium_scoring_dd) - 无描述
303. [dws_gift_post_premium_scoring_detail_dd](#dws_gift_post_premium_scoring_detail_dd) - 无描述
304. [dws_gift_post_premium_scoring_di](#dws_gift_post_premium_scoring_di) - 无描述
305. [dws_gift_post_premium_scoring_v2_dd](#dws_gift_post_premium_scoring_v2_dd) - 无描述
306. [dws_gift_post_premium_scoring_v2_di](#dws_gift_post_premium_scoring_v2_di) - 无描述
307. [dws_gift_post_return_dd](#dws_gift_post_return_dd) - 无描述
308. [dws_gift_unlock_tag_interests_di](#dws_gift_unlock_tag_interests_di) - 无描述
309. [dws_gift_user_revenue_dd](#dws_gift_user_revenue_dd) - 无描述
310. [dws_grain_post_creator_follower_dd](#dws_grain_post_creator_follower_dd) - 无描述
311. [dws_growth_content_new_level_di](#dws_growth_content_new_level_di) - 无描述
312. [dws_growth_device_ip_di](#dws_growth_device_ip_di) - 无描述
313. [dws_growth_vertical_category_crowd_dd](#dws_growth_vertical_category_crowd_dd) - 无描述
314. [dws_hot_article_stat_di](#dws_hot_article_stat_di) - 无描述
315. [dws_ip_consume_di](#dws_ip_consume_di) - 无描述
316. [dws_ip_creator_produce_dd](#dws_ip_creator_produce_dd) - 无描述
317. [dws_ip_di](#dws_ip_di) - 无描述
318. [dws_ip_growth_dd](#dws_ip_growth_dd) - 无描述
319. [dws_ip_interaction_di](#dws_ip_interaction_di) - 无描述
320. [dws_ip_life_cycle_dd](#dws_ip_life_cycle_dd) - 无描述
321. [dws_ip_life_cycle_type_info_dd](#dws_ip_life_cycle_type_info_dd) - ip圈层生命周期
322. [dws_ip_supply_di](#dws_ip_supply_di) - 无描述
323. [dws_ip_top20_info_dd](#dws_ip_top20_info_dd) - 无描述
324. [dws_ip_user_consume_dd](#dws_ip_user_consume_dd) - 无描述
325. [dws_ip_user_consume_di](#dws_ip_user_consume_di) - 无描述
326. [dws_ip_user_life_cycle_dd](#dws_ip_user_life_cycle_dd) - 无描述
327. [dws_ip_uv_nd](#dws_ip_uv_nd) - 无描述
328. [dws_ip_valid_pv_rank_info_di](#dws_ip_valid_pv_rank_info_di) - 无描述
329. [dws_land_nonrec_itemid_retain_di](#dws_land_nonrec_itemid_retain_di) - 无描述
330. [dws_membership_cp_dd](#dws_membership_cp_dd) - 会员cp池
331. [dws_membership_ip_dd](#dws_membership_ip_dd) - 会员ip池
332. [dws_membership_post_score_dd](#dws_membership_post_score_dd) - 会员文章综合分
333. [dws_miniprogram_post_order_dd](#dws_miniprogram_post_order_dd) - 无描述
334. [dws_page_note_di](#dws_page_note_di) - 无描述
335. [dws_page_note_source_scene_di](#dws_page_note_source_scene_di) - 无描述
336. [dws_page_scene_di](#dws_page_scene_di) - 无描述
337. [dws_page_source_scene_di](#dws_page_source_scene_di) - 无描述
338. [dws_par_creator_dd](#dws_par_creator_dd) - 无描述
339. [dws_par_creator_di](#dws_par_creator_di) - 无描述
340. [dws_par_creator_gift_level_scoring_dd](#dws_par_creator_gift_level_scoring_dd) - 无描述
341. [dws_par_creator_gift_level_scoring_detail_dd](#dws_par_creator_gift_level_scoring_detail_dd) - 无描述
342. [dws_par_creator_interaction_dd](#dws_par_creator_interaction_dd) - 无描述
343. [dws_par_creator_interaction_di](#dws_par_creator_interaction_di) - 无描述
344. [dws_par_creator_level_scoring_dd](#dws_par_creator_level_scoring_dd) - 无描述
345. [dws_par_creator_paid_post_income_dd](#dws_par_creator_paid_post_income_dd) - 无描述
346. [dws_par_creator_pay_di](#dws_par_creator_pay_di) - 无描述
347. [dws_par_creator_premium_staging_dd](#dws_par_creator_premium_staging_dd) - 无描述
348. [dws_par_creator_traffic_dd](#dws_par_creator_traffic_dd) - 无描述
349. [dws_par_creator_user_support_score_dd](#dws_par_creator_user_support_score_dd) - 无描述
350. [dws_par_device_active_dd](#dws_par_device_active_dd) - 无描述
351. [dws_par_device_interaction_di](#dws_par_device_interaction_di) - 无描述
352. [dws_par_device_session_di](#dws_par_device_session_di) - 无描述
353. [dws_par_device_session_v2_di](#dws_par_device_session_v2_di) - 无描述
354. [dws_par_user_1v1_di](#dws_par_user_1v1_di) - 无描述
355. [dws_par_user_active_dd](#dws_par_user_active_dd) - 无描述
356. [dws_par_user_active_di](#dws_par_user_active_di) - 无描述
357. [dws_par_user_ad_dd](#dws_par_user_ad_dd) - 无描述
358. [dws_par_user_ad_di](#dws_par_user_ad_di) - 无描述
359. [dws_par_user_appversion_dd](#dws_par_user_appversion_dd) - 无描述
360. [dws_par_user_base_dd](#dws_par_user_base_dd) - 无描述
361. [dws_par_user_content_di](#dws_par_user_content_di) - 无描述
362. [dws_par_user_core_staging_dd](#dws_par_user_core_staging_dd) - 无描述
363. [dws_par_user_coupon_exchange_dd](#dws_par_user_coupon_exchange_dd) - 无描述
364. [dws_par_user_discuss_di](#dws_par_user_discuss_di) - 无描述
365. [dws_par_user_ec_dd](#dws_par_user_ec_dd) - 无描述
366. [dws_par_user_fans_dd](#dws_par_user_fans_dd) - 无描述
367. [dws_par_user_home_top_resource_dd](#dws_par_user_home_top_resource_dd) - 首页吸顶资源(权益中心/破次元)用户人群包指标 - 按天聚合
368. [dws_par_user_home_visit_di](#dws_par_user_home_visit_di) - 无描述
369. [dws_par_user_interaction_dd](#dws_par_user_interaction_dd) - 无描述
370. [dws_par_user_interaction_di](#dws_par_user_interaction_di) - 无描述
371. [dws_par_user_ip_create_dd](#dws_par_user_ip_create_dd) - 无描述
372. [dws_par_user_ip_prefer_dd](#dws_par_user_ip_prefer_dd) - 无描述
373. [dws_par_user_misc_di](#dws_par_user_misc_di) - 无描述
374. [dws_par_user_pay_dd](#dws_par_user_pay_dd) - 无描述
375. [dws_par_user_post_dd](#dws_par_user_post_dd) - 无描述
376. [dws_par_user_push_dd](#dws_par_user_push_dd) - 无描述
377. [dws_par_user_revenue_dd](#dws_par_user_revenue_dd) - 无描述
378. [dws_par_user_reward_center_active_dd](#dws_par_user_reward_center_active_dd) - 权益中心用户活跃度汇总表 - 按天聚合
379. [dws_par_user_reward_center_ad_watch_dd](#dws_par_user_reward_center_ad_watch_dd) - 权益中心用户看广告次数汇总表 - 按天聚合
380. [dws_par_user_reward_center_exchange_dd](#dws_par_user_reward_center_exchange_dd) - 权益中心用户商品兑换汇总表 - 累计数据
381. [dws_par_user_session_di](#dws_par_user_session_di) - 无描述
382. [dws_par_user_stratify_di](#dws_par_user_stratify_di) - 无描述
383. [dws_par_user_tag_create_dd](#dws_par_user_tag_create_dd) - 无描述
384. [dws_par_user_traffic_dd](#dws_par_user_traffic_dd) - 无描述
385. [dws_post_base_stats_dd](#dws_post_base_stats_dd) - 无描述
386. [dws_post_base_stats_di](#dws_post_base_stats_di) - 无描述
387. [dws_post_highlight_comment_dd](#dws_post_highlight_comment_dd) - 无描述
388. [dws_post_interaction_dd](#dws_post_interaction_dd) - 无描述
389. [dws_post_interaction_di](#dws_post_interaction_di) - 无描述
390. [dws_post_misc_dd](#dws_post_misc_dd) - 无描述
391. [dws_post_pay_di](#dws_post_pay_di) - 无描述
392. [dws_post_premium_di](#dws_post_premium_di) - 优质文章
393. [dws_post_risk_di](#dws_post_risk_di) - 无描述
394. [dws_post_support_di](#dws_post_support_di) - 无描述
395. [dws_post_talk_answer_interaction_dd](#dws_post_talk_answer_interaction_dd) - 无描述
396. [dws_post_talk_question_interaction_dd](#dws_post_talk_question_interaction_dd) - 无描述
397. [dws_post_talk_user_crowd_dd](#dws_post_talk_user_crowd_dd) - 无描述
398. [dws_post_traffic_dd](#dws_post_traffic_dd) - 无描述
399. [dws_post_traffic_di](#dws_post_traffic_di) - 无描述
400. [dws_post_valid_publish_dd](#dws_post_valid_publish_dd) - 无描述
401. [dws_publish_ip_rank_info_di](#dws_publish_ip_rank_info_di) - 无描述
402. [dws_pve_roles_amount_info_dd](#dws_pve_roles_amount_info_dd) - 无描述
403. [dws_pve_user_action_di](#dws_pve_user_action_di) - 无描述
404. [dws_pve_user_coststamina_role_di](#dws_pve_user_coststamina_role_di) - 无描述
405. [dws_pve_user_role_amount_di](#dws_pve_user_role_amount_di) - 无描述
406. [dws_pve_user_role_chats_di](#dws_pve_user_role_chats_di) - 无描述
407. [dws_recommend_blog_review_status_di](#dws_recommend_blog_review_status_di) - 无描述
408. [dws_tag_consume_di](#dws_tag_consume_di) - 无描述
409. [dws_tag_dd](#dws_tag_dd) - 无描述
410. [dws_tag_di](#dws_tag_di) - 无描述
411. [dws_tag_fetch_di](#dws_tag_fetch_di) - 无描述
412. [dws_tag_interaction_di](#dws_tag_interaction_di) - 无描述
413. [dws_tag_supply_di](#dws_tag_supply_di) - 无描述
414. [dws_tag_user_consume_dd](#dws_tag_user_consume_dd) - 无描述
415. [dws_tag_user_consume_di](#dws_tag_user_consume_di) - 无描述
416. [dws_talk_publish_ip_rank_info_di](#dws_talk_publish_ip_rank_info_di) - 无描述
417. [dws_user_first_interaction_dd](#dws_user_first_interaction_dd) - 用户首次的热度+评论行为
418. [dws_user_life_circle_index_dd](#dws_user_life_circle_index_dd) - 无描述
419. [dws_user_life_circle_judge_dd](#dws_user_life_circle_judge_dd) - 无描述
420. [dws_user_pay_type_info_dd](#dws_user_pay_type_info_dd) - 无描述
421. [dws_user_post_interaction_di](#dws_user_post_interaction_di) - 无描述
422. [dws_user_post_other_interaction_di](#dws_user_post_other_interaction_di) - 无描述
423. [dws_user_post_talk_interaction_di](#dws_user_post_talk_interaction_di) - 无描述
424. [dws_user_security_level_di](#dws_user_security_level_di) - 无描述
425. [dws_user_tag_interaction_di](#dws_user_tag_interaction_di) - 无描述
426. [dws_user_talkcontent_interaction_di](#dws_user_talkcontent_interaction_di) - 无描述
427. [dws_video_creator_dd](#dws_video_creator_dd) - 创作者视频画像 · 仅因子
428. [dws_video_post_dd](#dws_video_post_dd) - 视频粒度日汇总 · 仅输出因子 (下游求商得率)
429. [dws_video_post_general_di](#dws_video_post_general_di) - 无描述
430. [dws_video_quality_dd](#dws_video_quality_dd) - 视频质量大盘因子 · 按 deviceOs × device_tier × appVersion × dominant_quality 切片
431. [dws_video_speedrate_dd](#dws_video_speedrate_dd) - 倍速功能专项因子 · 按 deviceOs × speedrate_tier 切片
432. [dws_video_user_consume_di](#dws_video_user_consume_di) - 用户视频消费日汇总 · 仅因子
433. [excellent_new_device_info_di](#excellent_new_device_info_di) - 无描述
434. [hubble_events](#hubble_events) - 无描述
435. [lofter_photopost_feature](#lofter_photopost_feature) - 无描述
436. [mda_common_deviceid_nd](#mda_common_deviceid_nd) - 无描述
437. [meta_worker_hdfs_meta](#meta_worker_hdfs_meta) - 无描述
438. [ods_binlog_anonymity_login_di](#ods_binlog_anonymity_login_di) - 无描述
439. [ods_binlog_benefit_order_product_di](#ods_binlog_benefit_order_product_di) - 无描述
440. [ods_binlog_blog_info_di](#ods_binlog_blog_info_di) - 无描述
441. [ods_binlog_comment_hot_di](#ods_binlog_comment_hot_di) - 无描述
442. [ods_binlog_dressing_user_suit_di](#ods_binlog_dressing_user_suit_di) - 无描述
443. [ods_binlog_message_di](#ods_binlog_message_di) - 无描述
444. [ods_binlog_photo_post_di](#ods_binlog_photo_post_di) - 无描述
445. [ods_binlog_post_collection_record_di](#ods_binlog_post_collection_record_di) - 无描述
446. [ods_binlog_post_di](#ods_binlog_post_di) - 无描述
447. [ods_binlog_post_hot_di](#ods_binlog_post_hot_di) - 无描述
448. [ods_binlog_post_response_di](#ods_binlog_post_response_di) - 无描述
449. [ods_binlog_profile_di](#ods_binlog_profile_di) - 无描述
450. [ods_binlog_pve_role_group_dialogue_di](#ods_binlog_pve_role_group_dialogue_di) - 无描述
451. [ods_binlog_pve_user_dialogue_di](#ods_binlog_pve_user_dialogue_di) - 无描述
452. [ods_binlog_pve_user_dialogue_partition_di](#ods_binlog_pve_user_dialogue_partition_di) - 无描述
453. [ods_binlog_pve_user_info_di](#ods_binlog_pve_user_info_di) - 无描述
454. [ods_binlog_pve_user_male_virtue_prison_log_di](#ods_binlog_pve_user_male_virtue_prison_log_di) - 无描述
455. [ods_binlog_recommend_post_review_log_di](#ods_binlog_recommend_post_review_log_di) - 无描述
456. [ods_binlog_recommend_review_post_di](#ods_binlog_recommend_review_post_di) - 无描述
457. [ods_binlog_reward_user_benefit_period_di](#ods_binlog_reward_user_benefit_period_di) - 无描述
458. [ods_binlog_risk_antispam_callback_record_di](#ods_binlog_risk_antispam_callback_record_di) - 无描述
459. [ods_binlog_risk_antispam_post_image_di](#ods_binlog_risk_antispam_post_image_di) - 无描述
460. [ods_binlog_risk_antispam_post_tmp2_di](#ods_binlog_risk_antispam_post_tmp2_di) - 无描述
461. [ods_binlog_risk_antispam_response_di](#ods_binlog_risk_antispam_response_di) - 无描述
462. [ods_binlog_rta_creator_ad_switch_di](#ods_binlog_rta_creator_ad_switch_di) - 无描述
463. [ods_binlog_share_post_di](#ods_binlog_share_post_di) - 无描述
464. [ods_binlog_tag_resource_di](#ods_binlog_tag_resource_di) - 无描述
465. [ods_binlog_text_post_di](#ods_binlog_text_post_di) - 无描述
466. [ods_binlog_trade_fans_vip_order_di](#ods_binlog_trade_fans_vip_order_di) - 无描述
467. [ods_binlog_trade_gift_pay_account_di](#ods_binlog_trade_gift_pay_account_di) - 无描述
468. [ods_binlog_trade_gift_present_record_di](#ods_binlog_trade_gift_present_record_di) - 无描述
469. [ods_binlog_trade_post_pack_order_di](#ods_binlog_trade_post_pack_order_di) - 无描述
470. [ods_binlog_trade_return_gift_exchange_record_di](#ods_binlog_trade_return_gift_exchange_record_di) - 无描述
471. [ods_binlog_trade_return_gift_plan_di](#ods_binlog_trade_return_gift_plan_di) - 无描述
472. [ods_binlog_trade_store_vip_order_di](#ods_binlog_trade_store_vip_order_di) - 无描述
473. [ods_binlog_trade_support_record_di](#ods_binlog_trade_support_record_di) - 无描述
474. [ods_binlog_trade_user_exchange_coupon_di](#ods_binlog_trade_user_exchange_coupon_di) - 无描述
475. [ods_binlog_trade_user_free_gift_di](#ods_binlog_trade_user_free_gift_di) - 无描述
476. [ods_binlog_user_close_account_data_bak_di](#ods_binlog_user_close_account_data_bak_di) - 无描述
477. [ods_binlog_user_close_account_log_di](#ods_binlog_user_close_account_log_di) - 无描述
478. [ods_binlog_user_following_di](#ods_binlog_user_following_di) - 无描述
479. [ods_binlog_user_statistic_di](#ods_binlog_user_statistic_di) - 无描述
480. [ods_db_text_post_di](#ods_db_text_post_di) - 无描述
481. [ods_kafka_search_di](#ods_kafka_search_di) - 无描述
482. [ods_log_ab_platform_sdk_log_di](#ods_log_ab_platform_sdk_log_di) - 无描述
483. [ods_log_ab_test_di](#ods_log_ab_test_di) - lofter backend abTest log
484. [ods_log_access_shortlink_di](#ods_log_access_shortlink_di) - 无描述
485. [ods_log_activity_signin_di](#ods_log_activity_signin_di) - 无描述
486. [ods_log_ad_ab_test_di](#ods_log_ad_ab_test_di) - lofter backend adAbTest log
487. [ods_log_ad_attribution_register_di](#ods_log_ad_attribution_register_di) - 无描述
488. [ods_log_ad_attribution_retain_di](#ods_log_ad_attribution_retain_di) - 无描述
489. [ods_log_ad_attribution_trade_di](#ods_log_ad_attribution_trade_di) - 无描述
490. [ods_log_ad_click_di](#ods_log_ad_click_di) - 无描述
491. [ods_log_ad_client_di](#ods_log_ad_client_di) - 无描述
492. [ods_log_ad_deeplink_di](#ods_log_ad_deeplink_di) - 无描述
493. [ods_log_ad_device_info_upload_di](#ods_log_ad_device_info_upload_di) - 无描述
494. [ods_log_ad_dsp_di](#ods_log_ad_dsp_di) - 无描述
495. [ods_log_ad_dsp_raw_di](#ods_log_ad_dsp_raw_di) - 无描述
496. [ods_log_ad_linkup_ks_stat_di](#ods_log_ad_linkup_ks_stat_di) - 无描述
497. [ods_log_ad_material_crawl_di](#ods_log_ad_material_crawl_di) - 广告投放素材抓取
498. [ods_log_ad_new_linkup_di](#ods_log_ad_new_linkup_di) - 无描述
499. [ods_log_ad_outer_linkup_di](#ods_log_ad_outer_linkup_di) - 无描述
500. [ods_log_ad_post_detail_candy_di](#ods_log_ad_post_detail_candy_di) - 无描述
501. [ods_log_ad_post_detail_free_di](#ods_log_ad_post_detail_free_di) - 无描述
502. [ods_log_ad_post_like_di](#ods_log_ad_post_like_di) - 广告文赞解锁
503. [ods_log_ad_resource_action_di](#ods_log_ad_resource_action_di) - 无描述
504. [ods_log_ad_resource_monitor_close_di](#ods_log_ad_resource_monitor_close_di) - 无描述
505. [ods_log_ad_yidun_callback_di](#ods_log_ad_yidun_callback_di) - 无描述
506. [ods_log_ad_yidun_request_di](#ods_log_ad_yidun_request_di) - 无描述
507. [ods_log_anti_addiction_di](#ods_log_anti_addiction_di) - 无描述
508. [ods_log_anti_forbid_di](#ods_log_anti_forbid_di) - 无描述
509. [ods_log_anti_risk_comment_di](#ods_log_anti_risk_comment_di) - 无描述
510. [ods_log_anti_risk_message_di](#ods_log_anti_risk_message_di) - 无描述
511. [ods_log_anti_risk_post_di](#ods_log_anti_risk_post_di) - 无描述
512. [ods_log_anti_risk_shuare_di](#ods_log_anti_risk_shuare_di) - 无描述
513. [ods_log_anti_spam_copy_di](#ods_log_anti_spam_copy_di) - 无描述
514. [ods_log_antispam_brush_dispose_di](#ods_log_antispam_brush_dispose_di) - 无描述
515. [ods_log_antispam_callback_article_di](#ods_log_antispam_callback_article_di) - 无描述
516. [ods_log_antispam_callback_comment_di](#ods_log_antispam_callback_comment_di) - 无描述
517. [ods_log_antispam_user_report_di](#ods_log_antispam_user_report_di) - 用户投诉上报
518. [ods_log_antispam_webapp_di](#ods_log_antispam_webapp_di) - 无描述
519. [ods_log_appregister_di](#ods_log_appregister_di) - lofter tomcat appregister
520. [ods_log_artificial_import_tag_di](#ods_log_artificial_import_tag_di) - 无描述
521. [ods_log_authenticate_di](#ods_log_authenticate_di) - lofter tomcat authenticate
522. [ods_log_behavior_di](#ods_log_behavior_di) - 无描述
523. [ods_log_bindphone_di](#ods_log_bindphone_di) - lofter tomcat bindphone log
524. [ods_log_bookstore_coupon_di](#ods_log_bookstore_coupon_di) - lofter backend bookstore_coupon log
525. [ods_log_browsepage_di](#ods_log_browsepage_di) - lofter browse page
526. [ods_log_bund_di](#ods_log_bund_di) - lofter tomcat bund log
527. [ods_log_cheat_header_info_di](#ods_log_cheat_header_info_di) - 无描述
528. [ods_log_check_public_post_md5_consumer_di](#ods_log_check_public_post_md5_consumer_di) - 无描述
529. [ods_log_commend_di](#ods_log_commend_di) - lofter tomcat commend log
530. [ods_log_common_abtest_di](#ods_log_common_abtest_di) - lofter backend commonAbTest log
531. [ods_log_content_acw_di](#ods_log_content_acw_di) - 无描述
532. [ods_log_content_acw_kafka_di](#ods_log_content_acw_kafka_di) - 无描述
533. [ods_log_content_incantation_di](#ods_log_content_incantation_di) - 无描述
534. [ods_log_coupon_trade_acquisition_log_di](#ods_log_coupon_trade_acquisition_log_di) - 无描述
535. [ods_log_creator_stimulus_pm_di](#ods_log_creator_stimulus_pm_di) - 无描述
536. [ods_log_darenrecpage_di](#ods_log_darenrecpage_di) - lofter daren rec page
537. [ods_log_domaindarenpage_di](#ods_log_domaindarenpage_di) - lofter domain daren page
538. [ods_log_domainset_di](#ods_log_domainset_di) - lofter tomcat domainset
539. [ods_log_double11neteaseprofile_di](#ods_log_double11neteaseprofile_di) - lofter tomcat lofter_ds_double11neteaseprofile
540. [ods_log_dstr_flow_post_push_config_di](#ods_log_dstr_flow_post_push_config_di) - 无描述
541. [ods_log_dynamicpage_di](#ods_log_dynamicpage_di) - lofter dynamic page
542. [ods_log_exposinterestman_di](#ods_log_exposinterestman_di) - lofter tomcat exposinterestman log
543. [ods_log_findpage_di](#ods_log_findpage_di) - lofter find page
544. [ods_log_flpayfailed_di](#ods_log_flpayfailed_di) - lofter tomcat flpayfailed log
545. [ods_log_flpaysucess_di](#ods_log_flpaysucess_di) - lofter tomcat flpaysucess log
546. [ods_log_follow_di](#ods_log_follow_di) - lofter tomcat follow log
547. [ods_log_followcancel_di](#ods_log_followcancel_di) - lofter tomcat followcancel log
548. [ods_log_followpage_di](#ods_log_followpage_di) - lofter follow page
549. [ods_log_forbid_phone_reg_di](#ods_log_forbid_phone_reg_di) - 安全整改封禁手机注册信息
550. [ods_log_homepage_di](#ods_log_homepage_di) - lofter home page
551. [ods_log_hongbao_data_di](#ods_log_hongbao_data_di) - 无描述
552. [ods_log_hongbao_token_di](#ods_log_hongbao_token_di) - 无描述
553. [ods_log_ios_coin_sms_di](#ods_log_ios_coin_sms_di) - lofter ios CoinSms log
554. [ods_log_labeldarenpage_di](#ods_log_labeldarenpage_di) - lofter label daren page
555. [ods_log_labeldetailpage_di](#ods_log_labeldetailpage_di) - lofter label detail page
556. [ods_log_labelpagepc_di](#ods_log_labelpagepc_di) - lofter label page pc
557. [ods_log_labelrecommend_di](#ods_log_labelrecommend_di) - lofter tomcat labelrecommend log
558. [ods_log_larecpage_di](#ods_log_larecpage_di) - lofter la rec page
559. [ods_log_launchtopic_di](#ods_log_launchtopic_di) - lofter tomcat launchtopic log
560. [ods_log_login401_di](#ods_log_login401_di) - 无描述
561. [ods_log_message_di](#ods_log_message_di) - lofter tomcat message log
562. [ods_log_monitor_crab_suspect_di](#ods_log_monitor_crab_suspect_di) - 无描述
563. [ods_log_new_outerlinkup_di](#ods_log_new_outerlinkup_di) - 无描述
564. [ods_log_nickname_di](#ods_log_nickname_di) - lofter tomcat nickname
565. [ods_log_participatetopic_di](#ods_log_participatetopic_di) - lofter tomcat participatetopic log
566. [ods_log_personalpage_di](#ods_log_personalpage_di) - lofter tomcat  personalpage
567. [ods_log_popupabtest_di](#ods_log_popupabtest_di) - lofter backend popUpAbTest log
568. [ods_log_post_audit_chain_di](#ods_log_post_audit_chain_di) - 无描述
569. [ods_log_post_change_di](#ods_log_post_change_di) - 无描述
570. [ods_log_praise_di](#ods_log_praise_di) - lofter tomcat praise log
571. [ods_log_praisecancel_di](#ods_log_praisecancel_di) - lofter tomcat praisecancel
572. [ods_log_publishpost_di](#ods_log_publishpost_di) - lofter tomcat publishpost log
573. [ods_log_push_after_accord_di](#ods_log_push_after_accord_di) - 无描述
574. [ods_log_push_before_accord_di](#ods_log_push_before_accord_di) - 无描述
575. [ods_log_push_callback_details_di](#ods_log_push_callback_details_di) - 无描述
576. [ods_log_push_callback_info_di](#ods_log_push_callback_info_di) - 无描述
577. [ods_log_push_log_di](#ods_log_push_log_di) - 无描述
578. [ods_log_pushnotice_di](#ods_log_pushnotice_di) - lofter tomcat pushnotice log
579. [ods_log_pve_ai_req_log_di](#ods_log_pve_ai_req_log_di) - lofter backend pve AI_REQ_LOG log
580. [ods_log_pve_log_antispam_audit_trace_di](#ods_log_pve_log_antispam_audit_trace_di) - 无描述
581. [ods_log_pve_model_match_log_di](#ods_log_pve_model_match_log_di) - lofter backend pve MODEL_MATCH_LOG log
582. [ods_log_pve_monitor_access_di](#ods_log_pve_monitor_access_di) - lofter backend PVE_MONITOR_ACCESS log
583. [ods_log_radar_risk_di](#ods_log_radar_risk_di) - 后台风控引擎日志明细表
584. [ods_log_rec_scene_ctr_di](#ods_log_rec_scene_ctr_di) - 无描述
585. [ods_log_rec_text_identification_di](#ods_log_rec_text_identification_di) - 无描述
586. [ods_log_rec_text_identification_test_di](#ods_log_rec_text_identification_test_di) - 无描述
587. [ods_log_recommend_di](#ods_log_recommend_di) - lofter tomcat recommend log
588. [ods_log_register_di](#ods_log_register_di) - lofter tomcat register log
589. [ods_log_registerback_di](#ods_log_registerback_di) - lofter tomcat registerback
590. [ods_log_reproduce_di](#ods_log_reproduce_di) - lofter tomcat lofter_ds_reproduce
591. [ods_log_risk_api_access_di](#ods_log_risk_api_access_di) - 无描述
592. [ods_log_risk_operation_di](#ods_log_risk_operation_di) - 无描述
593. [ods_log_risk_post_digest_audit_di](#ods_log_risk_post_digest_audit_di) - 无描述
594. [ods_log_risk_radar_di](#ods_log_risk_radar_di) - 无描述
595. [ods_log_risk_slider_di](#ods_log_risk_slider_di) - 无描述
596. [ods_log_risk_slider_result_di](#ods_log_risk_slider_result_di) - 无描述
597. [ods_log_risk_yidun_audit_di](#ods_log_risk_yidun_audit_di) - 无描述
598. [ods_log_role_anti_media_di](#ods_log_role_anti_media_di) - 无描述
599. [ods_log_searchmob_di](#ods_log_searchmob_di) - lofter tomcat searchmob log
600. [ods_log_searchpchome_di](#ods_log_searchpchome_di) - lofter tomcat searchpchome log
601. [ods_log_searchpcperson_di](#ods_log_searchpcperson_di) - lofter tomcat searchpcperson log
602. [ods_log_send_sms_task_by_package_di](#ods_log_send_sms_task_by_package_di) - 无描述
603. [ods_log_serecpage_di](#ods_log_serecpage_di) - lofter se rec page
604. [ods_log_share_di](#ods_log_share_di) - lofter tomcat share log
605. [ods_log_shareblog_di](#ods_log_shareblog_di) - lofter tomcat shareblog
606. [ods_log_sharelabel_di](#ods_log_sharelabel_di) - lofter tomcat sharelabel
607. [ods_log_sharepost_di](#ods_log_sharepost_di) - lofter tomcat sharepost
608. [ods_log_shieldrecommend_di](#ods_log_shieldrecommend_di) - lofter tomcat shieldrecommend
609. [ods_log_singlelogpage_di](#ods_log_singlelogpage_di) - lofter tomcat singlelogpage
610. [ods_log_skyeye_faas_data_di](#ods_log_skyeye_faas_data_di) - 音乐站外爬取数据
611. [ods_log_specialdetailpage_di](#ods_log_specialdetailpage_di) - lofter tomcat specialdetailpage
612. [ods_log_specialpage_di](#ods_log_specialpage_di) - lofter special page
613. [ods_log_spm_di](#ods_log_spm_di) - 无描述
614. [ods_log_stranger_message_daily_di](#ods_log_stranger_message_daily_di) - 无描述
615. [ods_log_subscribe_di](#ods_log_subscribe_di) - lofter tomcat subscribe log
616. [ods_log_subscribecancel_di](#ods_log_subscribecancel_di) - lofter tomcat subscribecancel log
617. [ods_log_subscribecollection_di](#ods_log_subscribecollection_di) - lofter ds subscribe colletion
618. [ods_log_subscribepage_di](#ods_log_subscribepage_di) - lofter subscribe page
619. [ods_log_subscribepost_di](#ods_log_subscribepost_di) - lofter ds subscribe post
620. [ods_log_superwoman_di](#ods_log_superwoman_di) - lofter supewoman log
621. [ods_log_tag_protected_detail_di](#ods_log_tag_protected_detail_di) - 无描述
622. [ods_log_tag_protected_log_di](#ods_log_tag_protected_log_di) - 无描述
623. [ods_log_tag_resource_empty_di](#ods_log_tag_resource_empty_di) - 无描述
624. [ods_log_taxiuhomepage_di](#ods_log_taxiuhomepage_di) - lofter tomcat taxiuhomepage
625. [ods_log_taxiupublish_di](#ods_log_taxiupublish_di) - lofter tomcat taxiupublish
626. [ods_log_tomcat_di](#ods_log_tomcat_di) - lofter tomcat log
627. [ods_log_topicpage_di](#ods_log_topicpage_di) - lofter topic page
628. [ods_log_trace_request_meta_di](#ods_log_trace_request_meta_di) - 无描述
629. [ods_log_traffic_sensing_push_attribution_di](#ods_log_traffic_sensing_push_attribution_di) - 无描述
630. [ods_log_traffic_sensing_push_potential_di](#ods_log_traffic_sensing_push_potential_di) - 无描述
631. [ods_log_unbindphone_di](#ods_log_unbindphone_di) - lofter tomcat unbindphone log
632. [ods_log_unbund_di](#ods_log_unbund_di) - lofter tomcat unbund log
633. [ods_log_unsubscribecollection_di](#ods_log_unsubscribecollection_di) - lofter ds subscribe colletion
634. [ods_log_user_phone_unbind_di](#ods_log_user_phone_unbind_di) - 无描述
635. [ods_log_user_push_token_info_di](#ods_log_user_push_token_info_di) - 无描述
636. [ods_log_user_session_time_di](#ods_log_user_session_time_di) - 无描述
637. [ods_log_user_stat_count_fix_di](#ods_log_user_stat_count_fix_di) - 无描述
638. [ods_log_user_wgt_di](#ods_log_user_wgt_di) - 无描述
639. [ods_log_videopublishfaile_di](#ods_log_videopublishfaile_di) - lofter ds video publish failed
640. [ods_log_videopublishreguest_di](#ods_log_videopublishreguest_di) - lofter ds video publish reguest
641. [ods_log_videopublishsuccess_di](#ods_log_videopublishsuccess_di) - lofter ds video publish success
642. [ods_log_viewabtest_di](#ods_log_viewabtest_di) - lofter tomcat viewabtest log
643. [ods_log_visitdomain_di](#ods_log_visitdomain_di) - lofter tomcat visitdomain log
644. [ods_mda_app_di](#ods_mda_app_di) - 无描述
645. [ods_mda_app_raw_di](#ods_mda_app_raw_di) - 无描述
646. [ods_mda_bookstore_miniprogram_di](#ods_mda_bookstore_miniprogram_di) - 无描述
647. [ods_mda_miniprogram_di](#ods_mda_miniprogram_di) - 无描述
648. [ods_mda_push_reach_di](#ods_mda_push_reach_di) - 无描述
649. [ods_mda_ruyuan_miniprogram_di](#ods_mda_ruyuan_miniprogram_di) - 无描述
650. [ods_mda_wap_di](#ods_mda_wap_di) - 无描述
651. [ods_mda_web_di](#ods_mda_web_di) - 无描述
652. [ods_pve_log_ai_metric_di](#ods_pve_log_ai_metric_di) - 无描述
653. [ods_rec_content_understand_log_di](#ods_rec_content_understand_log_di) - 无描述
654. [ods_risk_limit](#ods_risk_limit) - 无描述
655. [ods_risk_limit_dd](#ods_risk_limit_dd) - 无描述
656. [ods_risk_mark_return_gift_nd](#ods_risk_mark_return_gift_nd) - 无描述
657. [rec_fea_user_long_profile_tags_v3](#rec_fea_user_long_profile_tags_v3) - 无描述
658. [stg_par_creator_interaction_in_130d_dd](#stg_par_creator_interaction_in_130d_dd) - 无描述
659. [stg_par_creator_interaction_out_130d_wd](#stg_par_creator_interaction_out_130d_wd) - 无描述
660. [stg_post_content_feature_dd](#stg_post_content_feature_dd) - 无描述
661. [stg_post_hot_dynamic_in_130d_dd](#stg_post_hot_dynamic_in_130d_dd) - 无描述
662. [stg_post_hot_static_out_130d_wd](#stg_post_hot_static_out_130d_wd) - 无描述
663. [stg_post_interaction_in_130d_dd](#stg_post_interaction_in_130d_dd) - 无描述
664. [stg_post_interaction_out_130d_wd](#stg_post_interaction_out_130d_wd) - 无描述
665. [zd_lofter_post_human_no_pass_info_dd](#zd_lofter_post_human_no_pass_info_dd) - 近俩年过安全人审但未进推荐池的内容

---

## ads_iad_lofter_exp_click_outer_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ads_iad_lofter_exp_click_outer_di` |
| **描述** | 和LOFTER对数表 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.8M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dsp_id` | `string` |  | dsp id |
| 2 | `dsp_name` | `string` |  | dsp 名称 |
| 3 | `flight_id` | `string` |  | 广告位id |
| 4 | `flight_name` | `string` |  | 广告位名称 |
| 5 | `ad_type` | `string` |  | 广告类型 |
| 6 | `media_flight_request` | `bigint` |  | 媒体广告位请求数 |
| 7 | `dsp_resp_ads_win` | `bigint` |  | 胜出量 |
| 8 | `price` | `bigint` |  | 消耗 |
| 9 | `exposure` | `bigint` |  | adx曝光量 |
| 10 | `click` | `bigint` |  | adx点击量 |
| 11 | `anti_exposure` | `bigint` |  | 计费曝光量 |
| 12 | `anti_click` | `bigint` |  | 计费点击量 |
| 13 | `maisui_settle_money` | `int` |  | 麦穗结算价格 |
| 14 | `category` | `string` |  |  |
| 15 | `new_settle_money` | `int` |  | 调整后结算价格 |
| 16 | `dt` | `string` |  | 日期分区 |

---

## ads_iad_lofter_info_flow_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ads_iad_lofter_info_flow_di` |
| **描述** | 和LOFTER对数表 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.6M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dsp_id` | `string` |  | dsp id |
| 2 | `dsp_name` | `string` |  | dsp 名称 |
| 3 | `flight_id` | `string` |  | 广告位id |
| 4 | `flight_name` | `string` |  | 广告位名称 |
| 5 | `ad_type` | `string` |  | 广告类型 |
| 6 | `category` | `string` |  |  |
| 7 | `specific_sponsors` | `string` |  | lofter广告主 |
| 8 | `media_flight_request` | `bigint` |  | 媒体广告位请求数 |
| 9 | `dsp_resp_ads_win` | `bigint` |  | 胜出量 |
| 10 | `price` | `bigint` |  | 消耗 |
| 11 | `exposure` | `bigint` |  | adx曝光量 |
| 12 | `click` | `bigint` |  | adx点击量 |
| 13 | `anti_exposure` | `bigint` |  | 计费曝光量 |
| 14 | `anti_click` | `bigint` |  | 计费点击量 |
| 15 | `maisui_settle_money` | `bigint` |  | 麦穗结算价格 |
| 16 | `activationvalue` | `bigint` |  | 转化-激活数 |
| 17 | `registervalue` | `bigint` |  | 转化-注册数 |
| 18 | `cut_price_money` | `decimal(20,8)` |  | 扣点之后的外部dsp结算价 |
| 19 | `cut_maisui_settle_money` | `decimal(20,8)` |  | 扣点之后的麦穗结算价 |
| 20 | `cash` | `decimal(20,8)` |  |  |
| 21 | `dt` | `string` |  | 日期分区 |

---

## ads_lofter_exp_click_checkout_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ads_lofter_exp_click_checkout_di` |
| **描述** | 麦穗lofter数据备份 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 9.8M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dsp_id` | `string` |  | dsp id |
| 2 | `dsp_name` | `string` |  | dsp 名称 |
| 3 | `flight_id` | `string` |  | 广告位id |
| 4 | `flight_name` | `string` |  | 广告位名称 |
| 5 | `ad_type` | `string` |  | 广告类型 |
| 6 | `media_flight_request` | `bigint` |  | 媒体广告位请求数 |
| 7 | `dsp_resp_ads_win` | `bigint` |  | 胜出量 |
| 8 | `price` | `bigint` |  | 消耗 |
| 9 | `exposure` | `bigint` |  | adx曝光量 |
| 10 | `click` | `bigint` |  | adx点击量 |
| 11 | `anti_exposure` | `bigint` |  | 计费曝光量 |
| 12 | `anti_click` | `bigint` |  | 计费点击量 |
| 13 | `maisui_settle_money` | `int` |  | 麦穗结算价格 |
| 14 | `category` | `string` |  |  |
| 15 | `appid` | `string` |  | 应用id |
| 16 | `dt` | `string` |  | day |

---

## bridge_ad_content_collection

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_ad_content_collection` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `collectionid` | `bigint` |  | 合集id |
| 4 | `is_pay` | `int` |  | 是否付费解锁: 0免费，1付费(有广告锁) |
| 5 | `dt` | `string` |  |  |

---

## bridge_ad_content_collection_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_ad_content_collection_dd` |
| **描述** | 内容广告付费合集 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 714.9K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `collectionid` | `bigint` |  | 合集id |
| 4 | `is_pay` | `int` |  | 是否付费解锁: 0免费，1付费(有广告锁) |
| 5 | `dt` | `string` |  |  |

---

## bridge_ad_position_user_group_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_ad_position_user_group_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 85.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 85.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `positionid` | `bigint` |  | 广告位id |
| 2 | `userid` | `bigint` |  | 人群包用户id |
| 3 | `is_shield` | `int` |  | 屏蔽人群包 1是 0否 |
| 4 | `is_delivery` | `int` |  | 投放人群包 1是 0否 |
| 5 | `dt` | `string` |  |  |

---

## bridge_c2c_product_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_c2c_product_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.0M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `productid` | `bigint` |  | 小黄车商品id |
| 2 | `postid` | `bigint` |  | 挂载文章id |
| 3 | `blogid` | `bigint` |  | 创作者id |
| 4 | `create_time` | `bigint` |  | 挂载创建时间 |
| 5 | `dt` | `string` |  |  |

---

## bridge_calendar_date

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_calendar_date` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `calendar_date` | `string` |  | 日历基准日期 |
| 2 | `period` | `string` |  | 关联周期： 滚动时间窗口7 15 30 90 180 365 自然周期 week month quarter year |
| 3 | `relate_date` | `string` |  | 关联日期 |
| 4 | `dt` | `string` |  |  |

---

## bridge_calendar_date_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_calendar_date_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 25.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 25.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `calendar_date` | `string` |  | 日历基准日期 |
| 2 | `period` | `string` |  | 关联周期： 滚动时间窗口7 15 30 90 180 365 自然周期 week month quarter year |
| 3 | `relate_date` | `string` |  | 关联日期 |
| 4 | `dt` | `string` |  |  |

---

## bridge_collection_post

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_collection_post` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 合集文章桥接关系id 见表lofter_db_dump.ods_db_post_collection_record_nd |
| 2 | `collectionid` | `bigint` |  | 合集id |
| 3 | `postid` | `bigint` |  | 文章id |
| 4 | `blogid` | `bigint` |  | 博客id |
| 5 | `collection_name` | `string` |  | 合集名称 |
| 6 | `dt` | `string` |  |  |

---

## bridge_collection_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_collection_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 89.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 89.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 合集文章桥接关系id 见表lofter_db_dump.ods_db_post_collection_record_nd |
| 2 | `collectionid` | `bigint` |  | 合集id |
| 3 | `postid` | `bigint` |  | 文章id |
| 4 | `blogid` | `bigint` |  | 博客id |
| 5 | `collection_name` | `string` |  | 合集名称 |
| 6 | `dt` | `string` |  |  |

---

## bridge_exp_metric

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `bridge_exp_metric` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
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
| **数据库** | `lofter` |
| **表名** | `bridge_exp_metric_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6.2M |
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

## category_domain_mapping

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `category_domain_mapping` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 1.4K |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `category` | `string` |  |  |
| 2 | `category2` | `string` |  |  |
| 3 | `category3` | `string` |  |  |
| 4 | `domainname` | `string` |  |  |

---

## dep175_drpf_mplus_api_data_day

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dep175_drpf_mplus_api_data_day` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 159.8K |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `json_info` | `string` |  |  |
| 2 | `ds` | `string` |  |  |

---

## device_active

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `device_active` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 499.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 499.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `devicemodel` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `firstaccesstime` | `bigint` |  |  |
| 5 | `userids` | `array<bigint>` |  | 匿名和非匿名都存，放在最前面的是最晚创建的非匿名账号 |
| 6 | `appchannels` | `array<string>` |  |  |
| 7 | `appversions` | `array<string>` |  |  |
| 8 | `firstaccessip` | `string` |  | 设备当日首次访问IP |
| 9 | `returnoccurtime` | `bigint` |  | 首次当日活跃时间， 过滤昨日延迟发送数据 |
| 10 | `appversionchannel` | `map<string, string>` |  |  |
| 11 | `dt` | `string` |  |  |

---

## device_new

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `device_new` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 9.8G |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

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
| 11 | `finaluserid` | `bigint` |  |  |
| 12 | `dt` | `string` |  |  |

---

## device_retain

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `device_retain` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1040.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1040.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `deviceos` | `string` |  | 操作系统 |
| 3 | `base_date` | `string` |  | 基准日期 |
| 4 | `is_new` | `int` |  | 基准日期是否新设备 |
| 5 | `is_retain` | `int` |  | 是否留存 1是 0否 |
| 6 | `dt` | `string` |  |  |
| 7 | `period` | `int` |  | 留存周期 1 2 6 14 29 |

---

## device_return

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `device_return` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 57.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 57.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `devicemodel` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `firstaccesstime` | `bigint` |  | 首次回流时间 |
| 5 | `userids` | `array<bigint>` |  |  |
| 6 | `appchannels` | `array<string>` |  |  |
| 7 | `appversions` | `array<string>` |  |  |
| 8 | `returnoccurtime` | `bigint` |  | 回流时间 |
| 9 | `dt` | `string` |  |  |
| 10 | `period` | `int` |  |  |

---

## dim_act_accompany_tag

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_act_accompany_tag` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 30.2K |
| **是否分区表** | 否 |

### 字段详情

共 1 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  |  |

---

## dim_act_premium_grain_tag

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_act_premium_grain_tag` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.1K |
| **是否分区表** | 否 |

### 字段详情

共 1 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  |  |

---

## dim_actpwd_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_actpwd_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3166.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3166.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `actpwd` | `string` |  | 口令 |
| 2 | `channel` | `string` |  | 口令渠道 |
| 3 | `itemid` | `bigint` |  | 口令内容id |
| 4 | `content_type` | `string` |  | 口令内容类型 |
| 5 | `tags` | `string` |  | 内容标签， 多个逗号分隔 |
| 6 | `title` | `string` |  | 内容标题 |
| 7 | `url` | `string` |  | 内容链接 |
| 8 | `actpwd_type` | `string` |  | 口令类型: 搜索 剪贴板 搜索置顶 |
| 9 | `starttime` | `bigint` |  | 口令创建时间 |
| 10 | `settlement_type` | `string` |  | 分成模式 CPA CPS FANS（高粉） UGC（UGC内容，单篇或券包） |
| 11 | `is_first_level_channel` | `int` |  | 是否一级渠道 |
| 12 | `is_paid_subscribe` | `int` |  | 是否付费内容拉新： 0否 1是 2高粉拉新 |
| 13 | `endtime` | `bigint` |  | 口令失效时间 |
| 14 | `promote_channels` | `array<string>` |  | 拉新口令站外推广渠道： 微博 抖音 快手 |
| 15 | `kol_nickname` | `string` |  | kol昵称 |
| 16 | `dt` | `string` |  |  |

---

## dim_ad_dsp_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ad_dsp_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 438.8K |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | 广告应用id |
| 2 | `dspid` | `bigint` |  | dsp id |
| 3 | `app_name` | `string` |  | 应用名称： LOFTER AVG AVG_HUAWEI |
| 4 | `is_client_biding` | `int` |  | 是否客户端竞价dsp: 1是 0否 |
| 5 | `is_internal_settle` | `int` |  | 是否内部结算dsp：1是 0否 |
| 6 | `rebate` | `decimal(10,2)` |  | 扣点后结算比例 |
| 7 | `is_lofter_ad` | `int` |  | 是否lofter站内广告: 1是 0否 |
| 8 | `dt` | `string` |  |  |

---

## dim_ad_dsp_slot_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ad_dsp_slot_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 7.8M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | 应用id |
| 2 | `slotid` | `string` |  | 代码位id |
| 3 | `dspid` | `bigint` |  | dspId |
| 4 | `positionid` | `bigint` |  | 广告位id |
| 5 | `price_type` | `string` |  | 出价类型: bidding waterfall |
| 6 | `dt` | `string` |  |  |

---

## dim_ad_position_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ad_position_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.3M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `positionid` | `bigint` |  | 广告位id |
| 2 | `position_name` | `string` |  | 广告名称 |
| 3 | `appid` | `string` |  | 应用id |
| 4 | `app_name` | `string` |  | 应用名称 |
| 5 | `category` | `string` |  | 广告位类目 |
| 6 | `location` | `string` |  | 广告位类目位置 |
| 7 | `is_under_delivery_user_group` | `int` |  | 是否在人群包下下发 1是 0否 |
| 8 | `revenue_position_id` | `bigint` |  | 对应收入上报数据广告位id |
| 9 | `dt` | `string` |  |  |

---

## dim_ad_request_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ad_request_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2295.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2295.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `req_id` | `string` |  | 请求id |
| 2 | `appid` | `string` |  | appId |
| 3 | `os` | `string` |  | 操作系统 |
| 4 | `version` | `string` |  | 系统版本 |
| 5 | `ip` | `string` |  | 请求ip |
| 6 | `userid` | `bigint` |  | 用户id |
| 7 | `request_date` | `string` |  | 请求日期 |
| 8 | `request_time` | `bigint` |  | 请求时间 |
| 9 | `dt` | `string` |  |  |

---

## dim_benefit_product

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_benefit_product` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.9M |
| **是否分区表** | 否 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `productid` | `bigint` |  | 商品ID |
| 2 | `productname` | `string` |  | 商品名称 |
| 3 | `supplyname` | `string` |  | 供应商名称 |
| 4 | `producttype` | `bigint` |  | 商品类型0券码类商品，1兑换类实物，2特权类实物，3现金类实物，5抽奖机会类商品，6抽取卡片类商品，7外链跳转类商品，8碎卡拼图类商品，9抽赏类实物商品，10卡牌赠品，11换赏商品 |
| 5 | `ips` | `array<string>` |  | 商品对应ip列表 |
| 6 | `category1` | `bigint` |  | 一级类目Id |
| 7 | `category1_name` | `string` |  | 一级类目名称 |
| 8 | `category2` | `bigint` |  | 二级类目Id |
| 9 | `category2_name` | `string` |  | 二级类目名称 |
| 10 | `category3` | `bigint` |  | 三级类目Id |
| 11 | `category3_name` | `string` |  | 三级类目名称 |
| 12 | `activitycode` | `string` |  | 活动code |
| 13 | `supplierid` | `bigint` |  | 供应商ID |
| 14 | `slottype` | `int` |  | 抽赏活动类型 |
| 15 | `presaletype` | `int` |  | 预售类型 |
| 16 | `cardpoolname` | `string` |  | 卡池名称 |
| 17 | `createtime` | `bigint` |  | 创建时间 |
| 18 | `updatetime` | `bigint` |  | 更新时间 |
| 19 | `onsaletime` | `bigint` |  | 上架时间 |
| 20 | `offsaletime` | `bigint` |  | 下架时间 |

---

## dim_benefit_product_category

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_benefit_product_category` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 37.2K |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 前后台分类ID |
| 2 | `name` | `string` |  | 分类名称 |
| 3 | `level` | `int` |  | 分类级别，不做单独设置，在创建修改分类时，自动依据上级分类ID设置，用于显示 |
| 4 | `parentid` | `bigint` |  | 上级分类ID, 默认0， 0表示没有上级分类 |
| 5 | `categorytype` | `int` |  | 分类类型，0 后台分类； 1 前台分类 |
| 6 | `category1` | `bigint` |  | 一级分类ID |
| 7 | `category1_name` | `string` |  | 一级分类名称 |
| 8 | `category2` | `bigint` |  | 二级分类ID |
| 9 | `category2_name` | `string` |  | 三级分类名称 |
| 10 | `category3` | `bigint` |  | 三级分类ID |
| 11 | `category3_name` | `string` |  | 三级分类名称 |

---

## dim_benefit_sku

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_benefit_sku` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 7.7M |
| **是否分区表** | 否 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `skuid` | `bigint` |  | skuID |
| 2 | `sku_name` | `string` |  | sku名称 |
| 3 | `sku_createtime` | `bigint` |  | sku创建时间 |
| 4 | `stock` | `bigint` |  | 库存数量 |
| 5 | `storeprice` | `double` |  | 市集或抽赏价格 |
| 6 | `costprice` | `double` |  | 成本价格 |
| 7 | `productid` | `bigint` |  | 商品ID |
| 8 | `productname` | `string` |  | 商品名称 |
| 9 | `supplyname` | `string` |  | 供应商名称 |
| 10 | `producttype` | `bigint` |  | 商品类型0券码类商品，1兑换类实物，2特权类实物，3现金类实物，5抽奖机会类商品，6抽取卡片类商品，7外链跳转类商品，8碎卡拼图类商品，9抽赏类实物商品，10卡牌赠品，11换赏商品 |
| 11 | `ips` | `array<string>` |  | 商品对应ip列表 |
| 12 | `category1` | `bigint` |  | 一级类目Id |
| 13 | `category1_name` | `string` |  | 一级类目名称 |
| 14 | `category2` | `bigint` |  | 二级类目Id |
| 15 | `category2_name` | `string` |  | 二级类目名称 |
| 16 | `category3` | `bigint` |  | 三级类目Id |
| 17 | `category3_name` | `string` |  | 三级类目名称 |
| 18 | `activitycode` | `string` |  | 活动code |
| 19 | `supplierid` | `bigint` |  | 供应商ID |
| 20 | `slottype` | `int` |  | 抽赏活动类型 |
| 21 | `presaletype` | `int` |  | 预售类型 |
| 22 | `cardpoolname` | `string` |  | 卡池名称 |
| 23 | `createtime` | `bigint` |  | 创建时间 |
| 24 | `updatetime` | `bigint` |  | 更新时间 |
| 25 | `onsaletime` | `bigint` |  | 上架时间 |
| 26 | `offsaletime` | `bigint` |  | 下架时间 |
| 27 | `sale_mode` | `string` |  | 销售模式： 采销-平销,采销-抽赏,代销-平销,代销-抽赏， 自研-卡牌 |

---

## dim_black_tag

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_black_tag` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 19.2M |
| **是否分区表** | 否 |

### 字段详情

共 1 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签名称 |

---

## dim_blog

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_blog` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 11.8G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 11.8G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `blogname` | `string` |  |  |
| 3 | `isauthenticated` | `boolean` |  |  |
| 4 | `isvalid` | `boolean` |  |  |
| 5 | `blognickname` | `string` |  |  |
| 6 | `authdomainids` | `array<bigint>` |  |  |
| 7 | `authdomainnames` | `array<string>` |  |  |
| 8 | `istest` | `int` |  | 是否匿名测试账号 1是 0否 |
| 9 | `createtime` | `bigint` |  | 博客创建时间 |
| 10 | `isofficial` | `int` |  | 是否官方账号: 1是 0否  |
| 11 | `authtime` | `bigint` |  | 博客首次认证时间 |

---

## dim_bookstore_post

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_bookstore_post` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id， 维表主键 |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `title` | `string` |  | 文章标题 |
| 5 | `publish_time` | `bigint` |  | 发布时间 |
| 6 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步) |
| 7 | `allowview` | `int` |  | 文章能见状态(0:公开可见,50:待审核,100:仅自己可见) |
| 8 | `bookstore_content_id` | `bigint` |  | 书城内容id |
| 9 | `bookstore_style_tags` | `array<string>` |  | 书城风格标签列表 |
| 10 | `bookstore_content_status` | `int` |  | 书城内容状态：0正常，-1删除 |
| 11 | `dt` | `string` |  |  |

---

## dim_bookstore_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_bookstore_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 23.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 23.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id， 维表主键 |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `title` | `string` |  | 文章标题 |
| 5 | `publish_time` | `bigint` |  | 发布时间 |
| 6 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步) |
| 7 | `allowview` | `int` |  | 文章能见状态(0:公开可见,50:待审核,100:仅自己可见) |
| 8 | `bookstore_content_id` | `bigint` |  | 书城内容id |
| 9 | `bookstore_style_tags` | `array<string>` |  | 书城风格标签列表 |
| 10 | `bookstore_content_status` | `int` |  | 书城内容状态：0正常，-1删除 |
| 11 | `dt` | `string` |  |  |

---

## dim_c2c_product_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_c2c_product_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 55.7M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `productid` | `bigint` |  | 小黄车商品id |
| 2 | `blogid` | `bigint` |  | 创作者/卖家id |
| 3 | `name` | `string` |  | 商品名称/标题 |
| 4 | `description` | `string` |  | 商品描述 |
| 5 | `product_tags` | `array<string>` |  | 商品标签 |
| 6 | `ip_tags` | `array<string>` |  | ip标签 |
| 7 | `product_type` | `string` |  | 商品类型 |
| 8 | `product_status` | `string` |  | 商品状态: 下架 正常 |
| 9 | `dt` | `string` |  |  |

---

## dim_daren

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_daren` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 632.5K |
| **是否分区表** | 否 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `blogid` | `bigint` |  |  |
| 3 | `authinfo` | `array<row<bigint,bigint,string>('authtime','authdomainid','authdomainname')>` |  |  |

---

## dim_date

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_date` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | internal |
| **表大小** | 38.3K |
| **是否分区表** | 否 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `string` |  |  |
| 2 | `week` | `string` |  |  |
| 3 | `month` | `string` |  |  |
| 4 | `year` | `string` |  |  |
| 5 | `ts` | `bigint` |  |  |
| 6 | `dayofyear` | `int` |  |  |
| 7 | `weekofyear` | `int` |  |  |
| 8 | `monthofmonth` | `int` |  |  |

---

## dim_date_rolling

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_date_rolling` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | internal |
| **表大小** | 97.1K |
| **是否分区表** | 否 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `string` |  |  |
| 2 | `rollingdate` | `string` |  |  |
| 3 | `period` | `int` |  |  |

---

## dim_domain

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_domain` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 188.8K |
| **是否分区表** | 否 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `domainname` | `string` |  |  |
| 3 | `tags` | `array<string>` |  |  |
| 4 | `toptags` | `array<string>` |  |  |
| 5 | `topictags` | `array<string>` |  |  |

---

## dim_game_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_game_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 38.5M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id， 维表主键 |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `publishdate` | `string` |  |  |
| 4 | `tags` | `string` |  | 文章标签，逗号分隔 |
| 5 | `title` | `string` |  | 文章标题 |
| 6 | `post_url` | `string` |  | 文章链接 |
| 7 | `hyperlink_urls` | `array<string>` |  | 游戏链接 |
| 8 | `iscoop` | `int` |  | 是否lofter合作游戏，1是，0非 |
| 9 | `dt` | `string` |  |  |

---

## dim_gift

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_gift` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `name` | `string` |  | 礼物名 |
| 3 | `is_pay` | `int` |  | 是否付费 |
| 4 | `coin` | `bigint` |  | 乐乎币价格 |
| 5 | `price` | `double` |  | 礼物价格 |
| 6 | `dt` | `string` |  |  |

---

## dim_gift_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_gift_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 133.2K |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  |  |
| 2 | `name` | `string` |  | 礼物名 |
| 3 | `is_pay` | `int` |  | 是否付费 |
| 4 | `coin` | `bigint` |  | 乐乎币价格 |
| 5 | `price` | `double` |  | 礼物价格 |
| 6 | `dt` | `string` |  |  |

---

## dim_gift_post

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_gift_post` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 该用户下的文章 |
| 2 | `title` | `string` |  | 文章标题 |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `url` | `string` |  | 文章url |
| 5 | `contenttype` | `string` |  | 文章类型 |
| 6 | `publishdate` | `string` |  | 文章发布时间 |
| 7 | `userid` | `bigint` |  | 开通付费用户id |
| 8 | `accept_gift_flag` | `int` |  | 接受礼物flag |
| 9 | `agree_day` | `string` |  | 同意开通礼物的时间 |
| 10 | `blogname` | `string` |  | 博客名称 |
| 11 | `blognickname` | `string` |  | 博客昵称 |
| 12 | `return_gift_ids` | `string` |  | 文章回礼礼物ids |
| 13 | `cp_type` | `string` |  | 是否为cp |
| 14 | `platform_type` | `string` |  | 平台类型 |
| 15 | `is_pay_return_gift` | `string` |  | 回礼类型标识 |
| 16 | `dt` | `string` |  |  |

---

## dim_gift_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_gift_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 363.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 363.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 礼物文章id |
| 2 | `title` | `string` |  | 文章标题 |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `url` | `string` |  | 文章url |
| 5 | `contenttype` | `string` |  | 文章类型 |
| 6 | `publishdate` | `string` |  | 文章发布时间 |
| 7 | `userid` | `bigint` |  | 开通付费用户id |
| 8 | `accept_gift_flag` | `int` |  | 接受礼物flag: 1 接受 0 不接受 |
| 9 | `agree_day` | `string` |  | 同意开通礼物日期 |
| 10 | `blogname` | `string` |  | 博客名称 |
| 11 | `blognickname` | `string` |  | 博客昵称 |
| 12 | `return_gift_ids` | `string` |  | 文章回礼礼物ids |
| 13 | `cp_type` | `string` |  | 是否为cp |
| 14 | `platform_type` | `string` |  | 平台类型 |
| 15 | `is_pay_return_gift` | `string` |  | 回礼类型标识:0，无回礼；1，仅免费；2，仅付费；3，仅付费+仅免费；4.付费免费；5，付费免费+仅免费；6，付费免费+仅付费；7，付费免费+仅付费+仅免费 |
| 16 | `dt` | `string` |  |  |

---

## dim_gift_post_return

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_gift_post_return` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 回礼ID |
| 2 | `plantype` | `bigint` |  | 回礼类型Id |
| 3 | `status` | `int` |  | 状态（-1：删除；0：未生效；1：生效；） |
| 4 | `auditstatus` | `int` |  | 审核状态（0：待审核；1：通过；2：不通过；） |
| 5 | `postid` | `bigint` |  | 文章id |
| 6 | `createdate` | `string` |  | 回礼创建日期 |
| 7 | `text_length` | `int` |  | 回礼文字数 |
| 8 | `image_count` | `int` |  | 回礼图片数 |
| 9 | `drainage_flag` | `int` |  | 引流嫌疑标志 |
| 10 | `giftname` | `string` |  | 回礼礼物名称 |
| 11 | `ispaynum` | `bigint` |  | 付费礼物数 |
| 12 | `nopaynum` | `bigint` |  | 免费礼物数 |
| 13 | `blogid` | `bigint` |  | 博客id |
| 14 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 15 | `blogname` | `string` |  | 博客名称 |
| 16 | `publishdate` | `string` |  | 文章发布日期 |
| 17 | `return_gift_plantype` | `string` |  | 回礼类型名称 |
| 18 | `agreeday` | `string` |  | 礼物功能开通日期 |
| 19 | `gift_ability_type` | `int` |  | 礼物功能类型 0:无,1:收费权限,2:激励计划 |
| 20 | `gift_ability_status` | `bigint` |  | 礼物功能状态 0:无,1:未开通,2:开通 |
| 21 | `blog_channel` | `int` |  | 博客来源 0:UGC, 1:PGC |
| 22 | `sign_flag` | `int` |  | 单文签约标识 |
| 23 | `ugc_channel` | `int` |  | UGC来源标识 |
| 24 | `ispay_returngift` | `int` |  | 回礼付费类型 0:付费免费,1:仅付费,2:仅免费 |
| 25 | `fans_vip_status` | `int` |  | 粉丝会员的状态 |
| 26 | `fans_vip_agree_day` | `string` |  | 粉丝会员开通日期 |
| 27 | `fans_vip_close_day` | `string` |  | 粉丝会员关闭日期 |
| 28 | `is_authority` | `int` |  | 粉丝会员是否为官方 |
| 29 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 30 | `collectionid` | `bigint` |  | 文章所属合集Id |
| 31 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 32 | `accept_gift_flag` | `int` |  | 是否接受回礼 |
| 33 | `show_support_flag` | `int` |  | 是否支持 |
| 34 | `hide_return_gift_preview_img` | `int` |  | 是否隐藏回礼预览图片 |
| 35 | `emote_package_id` | `bigint` |  | 表情包id |
| 36 | `dress_id` | `bigint` |  | 装扮id |
| 37 | `preview_image_count` | `bigint` |  | 预览图片数 |
| 38 | `review_status` | `int` |  | 0/未标记；1/良好；-1/低质；-2/负面 |
| 39 | `mark_tags` | `array<string>` |  | 回礼打标标签列表 |
| 40 | `dt` | `string` |  |  |

---

## dim_gift_post_return_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_gift_post_return_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 121.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 121.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 回礼ID |
| 2 | `plantype` | `bigint` |  | 回礼类型Id |
| 3 | `status` | `int` |  | 状态（-1：删除；0：未生效；1：生效；） |
| 4 | `auditstatus` | `int` |  | 审核状态（0：待审核；1：通过；2：不通过；） |
| 5 | `postid` | `bigint` |  | 文章id |
| 6 | `createdate` | `string` |  | 回礼创建日期 |
| 7 | `text_length` | `int` |  | 回礼文字数 |
| 8 | `image_count` | `int` |  | 回礼图片数 |
| 9 | `drainage_flag` | `int` |  | 引流嫌疑标志 |
| 10 | `giftname` | `string` |  | 回礼礼物名称 |
| 11 | `ispaynum` | `bigint` |  | 付费礼物数 |
| 12 | `nopaynum` | `bigint` |  | 免费礼物数 |
| 13 | `blogid` | `bigint` |  | 博客id |
| 14 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 15 | `blogname` | `string` |  | 博客名称 |
| 16 | `publishdate` | `string` |  | 文章发布日期 |
| 17 | `return_gift_plantype` | `string` |  | 回礼类型名称 |
| 18 | `agreeday` | `string` |  | 礼物功能开通日期 |
| 19 | `gift_ability_type` | `int` |  | 礼物功能类型 0:无,1:收费权限,2:激励计划 |
| 20 | `gift_ability_status` | `bigint` |  | 礼物功能状态 0:无,1:未开通,2:开通 |
| 21 | `blog_channel` | `int` |  | 博客来源 0:UGC, 1:PGC, 2=高粉，3=书城及小程序 |
| 22 | `sign_flag` | `int` |  | 单文签约标识 |
| 23 | `ugc_channel` | `int` |  | UGC来源标识 |
| 24 | `ispay_returngift` | `int` |  | 回礼付费类型 0:付费免费,1:仅付费,2:仅免费 |
| 25 | `fans_vip_status` | `int` |  | 粉丝会员的状态 |
| 26 | `fans_vip_agree_day` | `string` |  | 粉丝会员开通日期 |
| 27 | `fans_vip_close_day` | `string` |  | 粉丝会员关闭日期 |
| 28 | `is_authority` | `int` |  | 粉丝会员是否为官方 |
| 29 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 30 | `collectionid` | `bigint` |  | 文章所属合集Id |
| 31 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 32 | `accept_gift_flag` | `int` |  | 是否接受回礼 |
| 33 | `show_support_flag` | `int` |  | 是否支持 |
| 34 | `hide_return_gift_preview_img` | `int` |  | 是否隐藏回礼预览图片 |
| 35 | `emote_package_id` | `bigint` |  | 表情包id |
| 36 | `dress_id` | `bigint` |  | 装扮id |
| 37 | `preview_image_count` | `bigint` |  | 预览图片数 |
| 38 | `review_status` | `int` |  | 0/未标记；1/良好；-1/低质；-2/负面 |
| 39 | `mark_tags` | `array<string>` |  | 回礼打标标签列表 |
| 40 | `dt` | `string` |  |  |

---

## dim_grain_creator_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_grain_creator_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 299.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 299.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `grainid` | `bigint` |  | 粮单Id |
| 2 | `tags` | `string` |  | 标签 |
| 3 | `userid` | `bigint` |  | 粮单创建者id |
| 4 | `cover_url` | `string` |  | 粮单封面链接 |
| 5 | `name` | `string` |  | 粮单名称 |
| 6 | `createtime` | `bigint` |  | 粮单创建时间 |
| 7 | `lastpublishtime` | `bigint` |  | 粮单最近更新时间 |
| 8 | `graintype` | `bigint` |  | 粮单类型：0普通，1企划，2榜单 |
| 9 | `status` | `int` |  | 状态，0：正常， -1删除 |
| 10 | `sourcetype` | `bigint` |  | 来源类型，0：用户创建，1：运营创建 |
| 11 | `hotplantype` | `bigint` |  | 是否为热门企划：0否，1是 |
| 12 | `level` | `string` |  | 粮单的博客等级 |
| 13 | `blog_url` | `string` |  | 粮单创建者博客url |
| 14 | `content_type` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 15 | `dt` | `string` |  |  |

---

## dim_ip_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ip_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 165.6M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  |  |
| 2 | `derivedflag` | `int` |  | 是否衍生 1是 0否 |
| 3 | `categories` | `array<string>` |  | ip下包含的类目，多级类目用-分隔, 可能包含一、二、三级类目情况 |
| 4 | `domains` | `array<string>` |  | ip归属领域： 通过类目关联的领域得到, 见lofter.category_domain_mapping |
| 5 | `dt` | `string` |  |  |

---

## dim_ip_extend

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ip_extend` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | ip 相关衍生属性 |
| 2 | `is_core` | `int` |  | 是否核心ip |
| 3 | `is_new` | `int` |  | 是否新ip |
| 4 | `is_film_tv_star` | `int` |  | 是否影视ip |
| 5 | `is_game_cartoon` | `int` |  | 是否游戏ip |
| 6 | `dt` | `string` |  |  |

---

## dim_ip_extend_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_ip_extend_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 49.7M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | ip 相关衍生属性 |
| 2 | `is_core` | `int` |  | 是否核心ip |
| 3 | `is_new` | `int` |  | 是否新ip |
| 4 | `is_film_tv_star` | `int` |  | 是否影视ip |
| 5 | `is_game_cartoon` | `int` |  | 是否游戏ip |
| 6 | `dt` | `string` |  |  |

---

## dim_kol_channel_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_kol_channel_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.1M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | kol平台渠道id |
| 2 | `channel` | `string` |  | 渠道名称 |
| 3 | `dt` | `string` |  |  |

---

## dim_membership_collection_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_membership_collection_dd` |
| **描述** | 会员合集 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 484.8M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `collectionid` | `bigint` |  | 会员合集 |
| 2 | `collection_name` | `string` |  | 合集名称 |
| 3 | `blogid` | `bigint` |  | 合集作者 |
| 4 | `is_vip` | `int` |  | 是否会员免费: 1是 0否 |
| 5 | `collection_post_count` | `bigint` |  | 合集文章数 |
| 6 | `membership_post_count` | `bigint` |  | 会员内容池文章数 |
| 7 | `membership_vip_post_count` | `bigint` |  | 会员免费文章数 |
| 8 | `dt` | `string` |  |  |

---

## dim_membership_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_membership_post_dd` |
| **描述** | 会员文章池 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 82.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 82.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `ips` | `array<string>` |  | 文章ip |
| 5 | `join_time` | `bigint` |  | 文章入池时间 |
| 6 | `join_date` | `string` |  | 文章入池日期 |
| 7 | `pay_gift_type` | `string` |  | 文章付费类型：0 -文章没有设置回礼* 1 -文章设置回礼且只有一种，仅免费* 2 -文章设置回礼且只有一种，仅付费* 3 -文章设置回礼且有两种，仅付费+仅免费* 4 -文章设置回礼且只有一种，付费免费* 5 -文章设置回礼且有两种，付费免费+仅免费* 6 -文章设置回礼且有两种，付费免费+仅付费* 7 -文章设置回礼且有三种，付费免费+仅付费+仅免费 |
| 8 | `is_vip` | `int` |  | 是否会员免费: 1是 0否 |
| 9 | `dt` | `string` |  |  |

---

## dim_membership_post_vip_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_membership_post_vip_dd` |
| **描述** | 会员vip免费文章池 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.9G |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `ips` | `array<string>` |  | 文章ip |
| 5 | `join_time` | `bigint` |  | 文章入池时间 |
| 6 | `join_date` | `string` |  | 文章入池日期 |
| 7 | `dt` | `string` |  |  |

---

## dim_miniprogram_post

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_miniprogram_post` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `miniid` | `int` |  | 小程序id |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `blogid` | `bigint` |  | 博客id |
| 4 | `list_time` | `bigint` |  | 进入小程序内容池时间 |
| 5 | `blog_name` | `string` |  | 博客名称 |
| 6 | `blog_nickname` | `string` |  | 博客昵称 |
| 7 | `title` | `string` |  | 文章标题 |
| 8 | `url` | `string` |  | 文章url |
| 9 | `dt` | `string` |  |  |

---

## dim_miniprogram_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_miniprogram_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 20.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 20.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `miniid` | `int` |  | 小程序id |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `blogid` | `bigint` |  | 博客id |
| 4 | `list_time` | `bigint` |  | 进入小程序内容池时间 |
| 5 | `blog_name` | `string` |  | 博客名称 |
| 6 | `blog_nickname` | `string` |  | 博客昵称 |
| 7 | `title` | `string` |  | 文章标题 |
| 8 | `url` | `string` |  | 文章url |
| 9 | `dt` | `string` |  |  |

---

## dim_post

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_post` |
| **描述** | 文章维表 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 89.7G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 89.7G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 文章ID |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `blogid` | `bigint` |  |  |
| 4 | `blogname` | `string` |  |  |
| 5 | `title` | `string` |  |  |
| 6 | `publishtime` | `bigint` |  |  |
| 7 | `publishdate` | `string` |  |  |
| 8 | `tags` | `array<string>` |  |  |
| 9 | `domains` | `array<bigint>` |  | 前端领域（不是类目领域） |
| 10 | `ispublished` | `boolean` |  | 是否发布状态， 单向过程 |
| 11 | `isforbidden` | `boolean` |  | 是否封禁 等同于valid=25 |
| 12 | `isblogauthenticated` | `boolean` |  | 是否达人博客 |
| 13 | `userpostindex` | `bigint` |  | 该篇文章是作者的第几篇文章，按时间顺序排列，第一篇该值为0。 |
| 14 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 15 | `citedparentpostid` | `bigint` |  | 转载原文章id |
| 16 | `iscitedpost` | `boolean` |  | 是否转载 |
| 17 | `movefrom` | `string` |  |  |
| 18 | `usercreatefrom` | `string` |  |  |
| 19 | `usercreatedate` | `string` |  | 用户创建日期 |
| 20 | `blognickname` | `string` |  | 博客昵称 |
| 21 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步, 32: 用户删除) |
| 22 | `allowview` | `int` |  | 文章能见状态(0:公开可见,50:待审核,100:仅自己可见) |
| 23 | `ischat` | `int` |  | 是否聊聊 1是 0否 |
| 24 | `ismoved` | `int` |  | 是否导入 1是 0否 |
| 25 | `isimported` | `int` |  | 是否引入 1是 0否 |
| 26 | `isactivityautopost` | `int` |  | 是否活动自动发文 1是 0否 |
| 27 | `ips` | `array<string>` |  | 文章ip： 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 28 | `url` | `string` |  | 文章链接 |
| 29 | `is_book_store` | `int` |  | 是否书城文章 |
| 30 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 31 | `importplatformtype` | `string` |  | 引入文章渠道类型 |

---

## dim_post_article

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_post_article` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 85.9G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 85.9G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 文章ID |
| 2 | `userid` | `bigint` |  |  |
| 3 | `blogid` | `bigint` |  |  |
| 4 | `blogname` | `string` |  |  |
| 5 | `title` | `string` |  |  |
| 6 | `publishtime` | `bigint` |  |  |
| 7 | `publishdate` | `string` |  |  |
| 8 | `tags` | `array<string>` |  |  |
| 9 | `domains` | `array<bigint>` |  | 前端领域（不是类目领域） |
| 10 | `ispublished` | `boolean` |  | 是否发布状态， 单向过程 |
| 11 | `isforbidden` | `boolean` |  | 是否封禁 等同于valid=25 |
| 12 | `isblogauthenticated` | `boolean` |  | 是否达人博客 |
| 13 | `userpostindex` | `bigint` |  | 该篇文章是作者的第几篇文章，按时间顺序排列，第一篇该值为0。 |
| 14 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，6长文章) |
| 15 | `citedparentpostid` | `bigint` |  | 转载原文章id |
| 16 | `iscitedpost` | `boolean` |  | 是否转载 |
| 17 | `movefrom` | `string` |  |  |
| 18 | `usercreatefrom` | `string` |  |  |
| 19 | `usercreatedate` | `string` |  | 用户创建日期 |
| 20 | `blognickname` | `string` |  | 博客昵称 |
| 21 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步, 32: 用户删除) |
| 22 | `allowview` | `int` |  | 文章能见状态(0:公开可见,50:待审核,100:仅自己可见) |
| 23 | `ischat` | `int` |  | 是否聊聊 1是 0否 |
| 24 | `ismoved` | `int` |  | 是否导入 1是 0否 |
| 25 | `isimported` | `int` |  | 是否引入 1是 0否 |
| 26 | `isactivityautopost` | `int` |  | 是否活动自动发文 1是 0否 |
| 27 | `ips` | `array<string>` |  | 文章ip： 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 28 | `url` | `string` |  | 文章链接 |
| 29 | `is_book_store` | `int` |  | 是否书城文章 |
| 30 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 31 | `importplatformtype` | `string` |  | 引入文章渠道类型 |

---

## dim_post_category_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_post_category_dd` |
| **描述** | 文章类目：文章对应唯一主类目 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 613.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 613.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 博客ID |
| 3 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 4 | `category` | `string` |  | 一级类目 |
| 5 | `category2` | `string` |  | 二级类目 |
| 6 | `category3` | `string` |  | 三级类目 |
| 7 | `domain` | `string` |  | 类目领域 |
| 8 | `dt` | `string` |  |  |

---

## dim_post_category_set_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_post_category_set_dd` |
| **描述** | 文章类目： 单篇文章可能对应多条类目 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 613.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 613.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 博客ID |
| 3 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 4 | `category` | `string` |  | 一级类目 |
| 5 | `category2` | `string` |  | 二级类目 |
| 6 | `category3` | `string` |  | 三级类目 |
| 7 | `domain` | `string` |  | 类目领域 |
| 8 | `dt` | `string` |  |  |

---

## dim_post_talk

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_post_talk` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 7.5G |
| **是否分区表** | 否 |

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `string` |  | 唯一键,拼接talkId+talkType |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 动态类型：QUESTION,COS_QUESTION,SCORE_QUESTION,NOMINATESIGN_QUESTION,ANSWER,COS_ANSWER,NORMAL_COMMENT_ANSWER,LINE_COMMENT_ANSWER,PHOTO_COMMENT_ANSWER,JOIN_NOMINATESIGN_ANSWER,JOIN_SCORE_ANSWER |
| 4 | `blogid` | `bigint` |  | 日志所属博客Id |
| 5 | `questionid` | `bigint` |  | 话题Id(特殊:如果本身是说说并且没挂话题则为0,如果本身是话题则为nul) |
| 6 | `questionuserid` | `bigint` |  | 话题所属用户Id(特殊:如果本身是说说并且没挂话题则为0,如果本身是话题则为null) |
| 7 | `talkcontent` | `string` |  | 讨论内容 |
| 8 | `recomstatus` | `int` |  | 推荐状态:0初始,1推荐,-1不推荐 |
| 9 | `forbidstatus` | `int` |  | 屏蔽状态:0未屏蔽,1被屏蔽,2申请解屏中(仅话题有) |
| 10 | `valid` | `int` |  | 同post中的valid |
| 11 | `allowview` | `int` |  | 同post中的allowview |
| 12 | `status` | `int` |  | 同question的status |
| 13 | `auditstatus` | `int` |  | 同question的auditstatus |
| 14 | `createtime` | `bigint` |  | 短内容(question,answer)创建时间 |
| 15 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 16 | `tags` | `array<string>` |  | 标签 |
| 17 | `questiontype` | `int` |  | 话题类型: null-非话题,0-1对1提问,1-1对N投稿,2-运营创建,3-聊聊 |
| 18 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 19 | `ext` | `string` |  | 扩展字段,answer为提名子话题的回复时,包含关联文章信息 |
| 20 | `discusscount` | `bigint` |  | 讨论数(后台),answer类型为null |
| 21 | `scorecount` | `bigint` |  | 打分人数,answer类型为null |
| 22 | `parentquestionid` | `bigint` |  | 父问题Id,answer类型为null |
| 23 | `answercount` | `bigint` |  | 回答数 |
| 24 | `cosplay` | `int` |  | 话题类型: 0-普通,1-角色说,2-打分父话题,3-打分子话题,4-印象词父话题,5-印象词子话题 |
| 25 | `answertype` | `int` |  | 回答类型: 0-普通,1-角色说,2-普通评论,3-划线评,4-圈评,5-提名子话题回复,6-打分父话题回复,7-印象词动态 |
| 26 | `relatedblogid` | `bigint` |  | 关联文章的blogId |
| 27 | `relatedpostid` | `bigint` |  | 关联文章的postId |
| 28 | `contenttype` | `string` |  | 关联文章的类型(1文字,2图片,3音乐,4视频,5问答,6长文章) |
| 29 | `topicchat` | `int` |  | 0-普通，1-主题聊天话题 |
| 30 | `topicid` | `bigint` |  | 主题id |
| 31 | `is_fans_question` | `int` |  | 是否是私享话题：0 否，1 是 |

---

## dim_pve_user

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_pve_user` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `first_interview_date` | `string` |  | 第一次访问时间 |
| 3 | `last_interview_date` | `string` |  | 上一次访问时间 |
| 4 | `first_chats_date` | `string` |  | 第一次聊天时间 |
| 5 | `last_chats_date` | `string` |  | 上一次聊天时间 |
| 6 | `first_trade_date` | `string` |  | 第一次支付时间 |
| 7 | `last_trade_date` | `string` |  | 上一次支付时间 |
| 8 | `usertype` | `int` |  | 用户类型：0聊天用户，1访问用户，2邀约及其他用户 |
| 9 | `userchannel` | `int` |  | 用户来源渠道: 10:音乐; 20:广告; 30:自然 |
| 10 | `dt` | `string` |  |  |

---

## dim_pve_user_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_pve_user_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 140.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 140.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `first_interview_date` | `string` |  | 第一次访问时间 |
| 3 | `last_interview_date` | `string` |  | 上一次访问时间 |
| 4 | `first_chats_date` | `string` |  | 第一次聊天时间 |
| 5 | `last_chats_date` | `string` |  | 上一次聊天时间 |
| 6 | `first_trade_date` | `string` |  | 第一次支付时间 |
| 7 | `last_trade_date` | `string` |  | 上一次支付时间 |
| 8 | `usertype` | `int` |  | 用户类型：0聊天用户，1访问用户，2邀约及其他用户 |
| 9 | `userchannel` | `int` |  | 用户来源渠道: 10:音乐; 20:广告; 30:自然 |
| 10 | `dt` | `string` |  |  |

---

## dim_seven_group_ip_category_res_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_seven_group_ip_category_res_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_yaomengyuan01 |
| **表类型** | external |
| **表大小** | 720.1K |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  |  |
| 2 | `category` | `string` |  |  |
| 3 | `category_res` | `string` |  |  |
| 4 | `dt` | `string` |  |  |

---

## dim_tag

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_tag` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签名称 |
| 2 | `is_cmb_tag` | `int` |  | 是否为标签库中的标签 |
| 3 | `derivedflag` | `int` |  | 是否衍生 -1:未设置,0:否,1:是 |
| 4 | `ipflag` | `int` |  | 是否IP -1:未设置,0:否,1:是 |
| 5 | `fanflag` | `int` |  | 是否同人 -1:未设置,0:否,1:是 |
| 6 | `cpflag` | `int` |  | 是否CP -1:未设置,0:否,1:是 |
| 7 | `entity` | `string` |  | 实体 |
| 8 | `entityspecific` | `string` |  | 具体实体，分号分隔 |
| 9 | `cpprops` | `string` |  | CP属性 |
| 10 | `otherprops` | `string` |  | 其他属性 |
| 11 | `specialsettingflag` | `int` |  | 是否特殊设定 -1:未设置,0:否,1:是 |
| 12 | `ips` | `string` |  | 所属IP ","分隔 |
| 13 | `categories` | `string` |  | 所属类目 ","分隔 |
| 14 | `is_activity_tag` | `int` |  | 是否活动标签 |
| 15 | `domainid` | `bigint` |  | 标签所属领域id |
| 16 | `domain_name` | `string` |  | 标签所属领域名称 |
| 17 | `dbcreatetime` | `bigint` |  | db入库时间 |
| 18 | `dbupdatetime` | `bigint` |  | db更新时间 |
| 19 | `dt` | `string` |  |  |

---

## dim_tag_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_tag_dd` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | external |
| **表大小** | 235.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 235.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签名称 |
| 2 | `is_cmb_tag` | `int` |  | 是否为标签库中的标签 |
| 3 | `derivedflag` | `int` |  | 是否衍生 -1:未设置,0:否,1:是 |
| 4 | `ipflag` | `int` |  | 是否IP -1:未设置,0:否,1:是 |
| 5 | `fanflag` | `int` |  | 是否同人 -1:未设置,0:否,1:是 |
| 6 | `cpflag` | `int` |  | 是否CP -1:未设置,0:否,1:是 |
| 7 | `entity` | `string` |  | 实体 |
| 8 | `entityspecific` | `string` |  | 具体实体，分号分隔 |
| 9 | `cpprops` | `string` |  | CP属性 |
| 10 | `otherprops` | `string` |  | 其他属性 |
| 11 | `specialsettingflag` | `int` |  | 是否特殊设定 -1:未设置,0:否,1:是 |
| 12 | `ips` | `string` |  | 所属IP ","分隔 |
| 13 | `categories` | `string` |  | 所属类目 ","分隔 |
| 14 | `is_activity_tag` | `int` |  | 是否活动标签 |
| 15 | `domainid` | `bigint` |  | 标签所属领域id |
| 16 | `domain_name` | `string` |  | 标签所属领域名称 |
| 17 | `dbcreatetime` | `bigint` |  | db入库时间 |
| 18 | `dbupdatetime` | `bigint` |  | db更新时间 |
| 19 | `dt` | `string` |  |  |

---

## dim_user

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_user` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 30.4G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 30.4G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 用户ID |
| 2 | `createtime` | `bigint` |  | 注册时间 |
| 3 | `email` | `string` |  | 注册邮箱 |
| 4 | `mainblogid` | `bigint` |  | 主博客ID |
| 5 | `createfrom` | `string` |  | 用户来源 |
| 6 | `createdate` | `string` |  | 用户注册日期 |
| 7 | `isanonymous` | `int` |  | 是否匿名用户 1是 0否 |
| 8 | `istest` | `int` |  | 是否匿名测试账号 1是 0否 |
| 9 | `isrobot` | `int` |  | 是否机器人用户 1是 0否 |
| 10 | `sourcetype` | `string` |  | 用户来源类型：官方账号，PGC，UGC |
| 11 | `is_miniprogram` | `int` |  | 是否小程序用户： 1是 0否 |

---

## dim_video

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_video` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 36 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章 ID |
| 2 | `userid` | `bigint` |  | 发布者 userId |
| 3 | `blogid` | `bigint` |  | 创作者 blogId |
| 4 | `blog_name` | `string` |  | 博客标识 |
| 5 | `blog_nickname` | `string` |  | 博客昵称 |
| 6 | `post_title` | `string` |  | 标题 |
| 7 | `publish_date` | `string` |  | 发布日期 yyyy-MM-dd |
| 8 | `publish_time` | `bigint` |  | 发布时间戳(ms) |
| 9 | `post_tags` | `array<string>` |  | 标签数组 |
| 10 | `post_ips` | `array<string>` |  | IP 圈层数组 |
| 11 | `post_domains` | `array<bigint>` |  | 一级领域数组 |
| 12 | `is_imported` | `int` |  | 是否导入: 0=否 / 1=是 |
| 13 | `import_platform_type` | `string` |  | 导入平台 (抖音/快手/B站等) |
| 14 | `allow_view` | `int` |  | 可见范围: 0=公开 / 50=审核中 / 100=仅自己可见 |
| 15 | `valid` | `int` |  | 审核状态: 0=正常 / 25=屏蔽 |
| 16 | `is_published` | `boolean` |  | 是否已发布: true=已发布 / false=未发布 |
| 17 | `is_forbidden` | `boolean` |  | 是否被屏蔽: true=被屏蔽 / false=未屏蔽 |
| 18 | `post_url` | `string` |  | 文章 URL |
| 19 | `movefrom` | `string` |  | 规整后的客户端: ios / android / web |
| 20 | `caption` | `string` |  | 富文本内容 (HTML) |
| 21 | `video_type` | `int` |  | 视频类型: 3=站内原生 / 非3=站外/导入 |
| 22 | `origin_url` | `string` |  | 原始视频 URL |
| 23 | `hls_url` | `string` |  | HLS 流 URL (m3u8) |
| 24 | `h265_url` | `string` |  | H265 编码 URL |
| 25 | `flash_url` | `string` |  | Flash URL |
| 26 | `video_down_url` | `string` |  | 下载 URL |
| 27 | `video_first_img` | `string` |  | 首帧封面图 |
| 28 | `video_img_url` | `string` |  | 用户自定义封面 URL |
| 29 | `vid` | `bigint` |  | 视频中台 vid (VOD 资源 ID) |
| 30 | `duration_sec` | `bigint` |  | 视频时长 (秒) - 注意单位与 dwd_video_play_di 的毫秒不同 |
| 31 | `size_bytes` | `bigint` |  | 文件大小 (字节) |
| 32 | `img_width` | `int` |  | 画幅宽 (像素) |
| 33 | `img_height` | `int` |  | 画幅高 (像素) |
| 34 | `aspect_ratio` | `string` |  | 宽高比: landscape / portrait / square / unknown |
| 35 | `embed_type` | `string` |  | embed 类型, 如 uservideo |
| 36 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dim_video_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dim_video_dd` |
| **描述** | 视频域唯一新建 DIM 表 · dim_post(视频帖) LEFT JOIN ods_db_video_post_nd + embed JSON 解析 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 158.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 158.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 36 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章 ID |
| 2 | `userid` | `bigint` |  | 发布者 userId |
| 3 | `blogid` | `bigint` |  | 创作者 blogId |
| 4 | `blog_name` | `string` |  | 博客标识 |
| 5 | `blog_nickname` | `string` |  | 博客昵称 |
| 6 | `post_title` | `string` |  | 标题 |
| 7 | `publish_date` | `string` |  | 发布日期 yyyy-MM-dd |
| 8 | `publish_time` | `bigint` |  | 发布时间戳(ms) |
| 9 | `post_tags` | `array<string>` |  | 标签数组 |
| 10 | `post_ips` | `array<string>` |  | IP 圈层数组 |
| 11 | `post_domains` | `array<bigint>` |  | 一级领域数组 |
| 12 | `is_imported` | `int` |  | 是否导入: 0=否 / 1=是 |
| 13 | `import_platform_type` | `string` |  | 导入平台 (抖音/快手/B站等) |
| 14 | `allow_view` | `int` |  | 可见范围: 0=公开 / 50=审核中 / 100=仅自己可见 |
| 15 | `valid` | `int` |  | 审核状态: 0=正常 / 25=屏蔽 |
| 16 | `is_published` | `boolean` |  | 是否已发布: true=已发布 / false=未发布 |
| 17 | `is_forbidden` | `boolean` |  | 是否被屏蔽: true=被屏蔽 / false=未屏蔽 |
| 18 | `post_url` | `string` |  | 文章 URL |
| 19 | `movefrom` | `string` |  | 规整后的客户端: ios / android / web |
| 20 | `caption` | `string` |  | 富文本内容 (HTML) |
| 21 | `video_type` | `int` |  | 视频类型: 3=站内原生 / 非3=站外/导入 |
| 22 | `origin_url` | `string` |  | 原始视频 URL |
| 23 | `hls_url` | `string` |  | HLS 流 URL (m3u8) |
| 24 | `h265_url` | `string` |  | H265 编码 URL |
| 25 | `flash_url` | `string` |  | Flash URL |
| 26 | `video_down_url` | `string` |  | 下载 URL |
| 27 | `video_first_img` | `string` |  | 首帧封面图 |
| 28 | `video_img_url` | `string` |  | 用户自定义封面 URL |
| 29 | `vid` | `bigint` |  | 视频中台 vid (VOD 资源 ID) |
| 30 | `duration_sec` | `bigint` |  | 视频时长 (秒) - 注意单位与 dwd_video_play_di 的毫秒不同 |
| 31 | `size_bytes` | `bigint` |  | 文件大小 (字节) |
| 32 | `img_width` | `int` |  | 画幅宽 (像素) |
| 33 | `img_height` | `int` |  | 画幅高 (像素) |
| 34 | `aspect_ratio` | `string` |  | 宽高比: landscape / portrait / square / unknown |
| 35 | `embed_type` | `string` |  | embed 类型, 如 uservideo |
| 36 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwb_par_lofter_device_tag_wd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwb_par_lofter_device_tag_wd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 288.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 288.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_id` | `string` |  | 设备标识 |
| 2 | `device_type` | `string` |  | 设备标识类型: idfa, imei, idfv, androidid, oaid |
| 3 | `tag_value` | `string` |  | 标签值，json格式 |
| 4 | `user_id` | `bigint` |  | 用户id |
| 5 | `pt_d` | `string` |  | 日期分区 |
| 6 | `pt_tag` | `string` |  | 标签分区 |

---

## dwb_par_lofter_music_user_label_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwb_par_lofter_music_user_label_di` |
| **描述** | lofter&音乐常态化活动，对接人肖乃同 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 45.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 45.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | Lofter用户id |
| 2 | `phone` | `string` |  | 注册手机号 |
| 3 | `verify_phone` | `string` |  | 绑定手机号 |
| 4 | `email` | `string` |  | 邮箱 |
| 5 | `user_type` | `string` |  | 云音乐状态（音乐新客、音乐召回、其他） |
| 6 | `pt_d` | `string` |  |  |

---

## dwb_par_lofter_tag_wd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwb_par_lofter_tag_wd` |
| **描述** | lofter用户标签表 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3465.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3465.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  | 用户id |
| 2 | `tag_value` | `string` |  | 标签值，json格式 |
| 3 | `tag_value_wt` | `string` |  | 标签置信度(对应eid标签json中的WT) |
| 4 | `pt_d` | `string` |  | 日期分区 |
| 5 | `pt_tag` | `string` |  | 标签分区 |

---

## dwd_ab_platform_exp_user_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ab_platform_exp_user_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 905.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 905.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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
| 15 | `backend_deviceid` | `string` |  | 后端传来的deviceid |
| 16 | `dt` | `string` |  |  |

---

## dwd_act_card_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_act_card_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 36.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 36.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `activityid` | `string` |  | 活动id |
| 4 | `deviceos` | `string` |  | 系统 |
| 5 | `eventid` | `string` |  | 事件id |
| 6 | `scene` | `string` |  | 场景 |
| 7 | `occurtime` | `bigint` |  | 发生时间 |
| 8 | `attributes` | `map<string, string>` |  | 事件属性 |
| 9 | `dt` | `string` |  |  |

---

## dwd_act_music_user_label_pool_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_act_music_user_label_pool_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 98.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 98.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | Lofter用户id |
| 2 | `phone` | `string` |  | 绑定手机号 |
| 3 | `email` | `string` |  | 注册邮箱 |
| 4 | `verify_phone` | `string` |  | 认证手机号 |
| 5 | `dt` | `string` |  |  |

---

## dwd_act_paper_man_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_act_paper_man_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `activityid` | `string` |  | 活动id |
| 4 | `deviceos` | `string` |  | 系统 |
| 5 | `eventid` | `string` |  | 事件id |
| 6 | `scene` | `string` |  | 场景 |
| 7 | `occurtime` | `bigint` |  | 发生时间 |
| 8 | `attributes` | `map<string, string>` |  | 事件属性 |
| 9 | `dt` | `string` |  |  |

---

## dwd_act_pve_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_act_pve_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 377.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 377.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `activityid` | `string` |  | 活动id |
| 4 | `deviceos` | `string` |  | 系统 |
| 5 | `eventid` | `string` |  | 事件id |
| 6 | `scene` | `string` |  | 场景 |
| 7 | `occurtime` | `bigint` |  | 发生时间 |
| 8 | `attributes` | `map<string, string>` |  | 事件属性 |
| 9 | `roleid` | `bigint` |  | 角色id |
| 10 | `url` | `string` |  | 页面url |
| 11 | `dt` | `string` |  |  |

---

## dwd_act_tag_big_event_detail_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_act_tag_big_event_detail_di` |
| **描述** | 圈层大事件底表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 31.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 31.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `tag` | `string` |  | tag名称 |
| 3 | `cnt` | `int` |  | 数量 |
| 4 | `type` | `string` |  | 事件类型 |
| 5 | `dt` | `string` |  | 日期分区 |

---

## dwd_activity_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_activity_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 174.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 174.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `activityid` | `string` |  | 活动id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `deviceudid` | `string` |  | 哈勃设备id |
| 4 | `occurtime` | `bigint` |  | 操作时间 |
| 5 | `eventid` | `string` |  | 操作 |
| 6 | `dt` | `string` |  |  |

---

## dwd_ad_action_click_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_action_click_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 19.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 19.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | appId |
| 2 | `app_name` | `string` |  | 应用名称 |
| 3 | `req_id` | `string` |  | 广告请求id |
| 4 | `req_uid` | `string` |  | dsp请求id |
| 5 | `dspid` | `bigint` |  | dspId |
| 6 | `deviceudid` | `string` |  | 设备id |
| 7 | `positionid` | `string` |  | 广告位id |
| 8 | `position_name` | `string` |  | 广告位名称 |
| 9 | `adtrace` | `string` |  | adTace跟踪串 |
| 10 | `adtracename` | `string` |  | adTrace名称 |
| 11 | `time` | `bigint` |  | 点击时间戳 |
| 12 | `deviceos` | `string` |  | 系统 |
| 13 | `appversion` | `string` |  | 版本 |
| 14 | `userid` | `bigint` |  | 广告请求用户id |
| 15 | `is_dp` | `int` |  | 是否唤起dp 1是 0否 |
| 16 | `is_dp_has_app` | `int` |  | 唤起dp是否安装app 1是 0否 |
| 17 | `dt` | `string` |  |  |

---

## dwd_ad_action_dsp_request_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_action_dsp_request_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 11499.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11499.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | appid Lofer-6ED29071 |
| 2 | `positionname` | `string` |  | 广告位名字 |
| 3 | `positionid` | `bigint` |  | 广告位ID |
| 4 | `os` | `string` |  |  |
| 5 | `adid` | `string` |  |  |
| 6 | `dspid` | `bigint` |  |  |
| 7 | `slotid` | `string` |  | site 格式形如: 6ED29071:STARTUP:1 |
| 8 | `advertiser_type` | `string` |  | 传媒客户类型（直客:0 ,非直客:1 ,品牌:2） |
| 9 | `uuid` | `string` |  | dsp请求uuid |
| 10 | `req_id` | `string` |  | 请求id |
| 11 | `version` | `string` |  |  |
| 12 | `wakeupboot` | `int` |  | 开机是否为热启动 |
| 13 | `userid` | `bigint` |  | 请求用户id |
| 14 | `is_request` | `bigint` |  | 是否请求DPS 0或1 |
| 15 | `is_fill` | `bigint` |  | DSP服务端是否填充；0或1 |
| 16 | `is_timeout` | `bigint` |  | DSP服务端超时；0或1 |
| 17 | `is_win` | `bigint` |  | DSP服务端竟得；0或1 |
| 18 | `is_forbidden` | `bigint` |  | DSP服务端被封禁；0或1 |
| 19 | `bid_amount` | `double` |  | DSP价格=DSP出价*出价系数，单位为0.01元 |
| 20 | `is_client_biding` | `int` |  | 是否客户端竞价dsp: 1是 0否 |
| 21 | `is_internal_settle` | `int` |  | 是否内部结算dsp：1是 0否 |
| 22 | `is_lofter_ad` | `int` |  | 是否lofter站内广告: 1是 0否 |
| 23 | `user_slot` | `bigint` |  | userslot |
| 24 | `request_time` | `bigint` |  | 请求时间 |
| 25 | `postid` | `bigint` |  | 广告关联文章id |
| 26 | `rta_sponsorid` | `string` |  | 联运广告主 |
| 27 | `is_maisui_external_dsp` | `int` |  | 是否来源麦穗外部dsp： 1是 0否 |
| 28 | `ad_type` | `string` |  | 广告类型：INNER 内部运营广告 BIDDING 竞价广告 FEE 付费合约 PDB 程序化PDB |
| 29 | `dt` | `string` |  |  |

---

## dwd_ad_action_expose_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_action_expose_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 156.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 156.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | appId |
| 2 | `app_name` | `string` |  | 应用名称 |
| 3 | `req_id` | `string` |  | 广告请求id |
| 4 | `req_uid` | `string` |  | dsp请求id |
| 5 | `dspid` | `bigint` |  | dspId |
| 6 | `deviceudid` | `string` |  | 设备id |
| 7 | `positionid` | `string` |  | 广告位id |
| 8 | `position_name` | `string` |  | 广告位名称 |
| 9 | `adtrace` | `string` |  | adTace跟踪串 |
| 10 | `adtracename` | `string` |  | adTrace名称 |
| 11 | `time` | `bigint` |  | 曝光时间戳 |
| 12 | `deviceos` | `string` |  | 系统 |
| 13 | `appversion` | `string` |  | 版本 |
| 14 | `userid` | `bigint` |  | 广告请求用户id |
| 15 | `dt` | `string` |  |  |

---

## dwd_ad_action_fill_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_action_fill_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3687.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3687.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `adid` | `string` |  | 广告id |
| 4 | `dspid` | `bigint` |  | dspid |
| 5 | `slotid` | `string` |  | 代码位 |
| 6 | `req_uid` | `string` |  | dsp请求uid |
| 7 | `req_id` | `string` |  | 广告请求id |
| 8 | `bid_floor` | `double` |  | 竞价底价 |
| 9 | `bid_price` | `double` |  | 竞价报价 |
| 10 | `bid_factor` | `double` |  | 竞价报价系数 |
| 11 | `time` | `bigint` |  | 填充时间 |
| 12 | `deviceos` | `string` |  | 系统 |
| 13 | `appversion` | `string` |  | app版本号 |
| 14 | `appid` | `string` |  | 应用id |
| 15 | `is_valid` | `int` |  | 是否有效竞得 1是 0否 |
| 16 | `dt` | `string` |  |  |

---

## dwd_ad_action_stock_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_action_stock_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 806.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 806.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `deviceudid` | `string` |  | 设备id |
| 3 | `req_id` | `string` |  | 广告请求id |
| 4 | `category` | `string` |  | 广告位类目 |
| 5 | `location` | `string` |  | 广告位类目位置 |
| 6 | `deviceos` | `string` |  | 系统类型 |
| 7 | `appversion` | `string` |  | 版本号 |
| 8 | `ex_info1` | `string` |  | 扩展信息1 |
| 9 | `ex_info2` | `string` |  | 扩展信息2 |
| 10 | `time` | `bigint` |  | 库存时间 |
| 11 | `positionid` | `bigint` |  | 广告位id |
| 12 | `position_name` | `string` |  | 广告位名称 |
| 13 | `appid` | `string` |  | 应用id |
| 14 | `is_deliver` | `int` |  | 是否在下发人群 1是 0否（屏蔽人群 或者 不在下发人群包)  |
| 15 | `is_valid` | `int` |  | 是否有效库存 |
| 16 | `dt` | `string` |  |  |

---

## dwd_ad_action_win_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_action_win_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2846.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2846.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `adid` | `string` |  | 广告id |
| 4 | `dspid` | `bigint` |  | dspid |
| 5 | `slotid` | `string` |  | 代码位 |
| 6 | `req_uid` | `string` |  | dsp请求uid |
| 7 | `req_id` | `string` |  | 广告请求id |
| 8 | `bid_floor` | `double` |  | 竞价底价 |
| 9 | `bid_price` | `double` |  | 竞价报价 |
| 10 | `bid_factor` | `double` |  | 竞价报价系数 |
| 11 | `time` | `bigint` |  | 填充时间 |
| 12 | `deviceos` | `string` |  | 系统 |
| 13 | `appversion` | `string` |  | app版本号 |
| 14 | `appid` | `string` |  | 应用id |
| 15 | `is_valid` | `int` |  | 是否有效竞得 1是 0否 |
| 16 | `dt` | `string` |  |  |

---

## dwd_ad_actions_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_actions_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 23480.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 23480.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | appid Lofer-6ED29071 |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `positionname` | `string` |  | 广告位名字 |
| 4 | `positionid` | `string` |  | 广告位ID |
| 5 | `os` | `string` |  |  |
| 6 | `adid` | `string` |  |  |
| 7 | `dspid` | `string` |  |  |
| 8 | `slotid` | `string` |  | site 格式形如: 6ED29071:STARTUP:1 |
| 9 | `advertiser_type` | `string` |  | 传媒客户类型（直客:0 ,非直客:1 ,品牌:2） |
| 10 | `req_id` | `string` |  | 请求id |
| 11 | `version` | `string` |  |  |
| 12 | `adtrace` | `string` |  |  |
| 13 | `adtracename` | `string` |  |  |
| 14 | `requestcount` | `bigint` |  | 是否请求DPS 0或1 |
| 15 | `fillcount` | `bigint` |  | DSP服务端是否填充；0或1 |
| 16 | `timeoutcount` | `bigint` |  | DSP服务端超时；0或1 |
| 17 | `winpv` | `bigint` |  | DSP服务端竟得；0或1 |
| 18 | `forbiddencount` | `bigint` |  | DSP服务端被封禁；0或1 |
| 19 | `bid_amount` | `double` |  | DSP价格=DSP出价*出价系数，单位为0.01元 |
| 20 | `client_fill_count` | `int` |  | DSP客户端SDK填充，只有穿山甲和优量汇；0或1 |
| 21 | `client_win_count` | `int` |  | DSP客户端竞得；0或1 |
| 22 | `bgpv` | `bigint` |  | 曝光；0或1 |
| 23 | `clickpv` | `bigint` |  | 广告是否被点击；0或1 |
| 24 | `wakeupboot` | `int` |  | 开机是否为热启动 --2022.11.25添加 |
| 25 | `ext` | `map<string, string>` |  | 额外信息 |
| 26 | `userid` | `bigint` |  | 用户id |
| 27 | `client_bg_count` | `int` |  | 客户端曝光统计 |
| 28 | `client_click_count` | `int` |  | 客户端点击统计 |
| 29 | `dp_count` | `int` |  | 客户端dp-link唤起统计 |
| 30 | `uuid` | `string` |  | 广告uuid，对应埋点为req_uid |
| 31 | `client_win_price` | `float` |  | 客户端统计的竞得报价，部分场景没有值 |
| 32 | `client_win_factor` | `float` |  | 客户端统计的竞得报价系数，部分场景没有值 |
| 33 | `dp_has_app_count` | `int` |  | dp调起时用户手机是否安装对应App |
| 34 | `ex_info1` | `string` |  | 额外信息1 |
| 35 | `ex_info2` | `string` |  | 额外信息2 |
| 36 | `store` | `bigint` |  | 库存 |
| 37 | `cache_req_id` | `string` |  | 请求池-新请求id |
| 38 | `cache_position_id` | `string` |  | 请求池-新广告位id |
| 39 | `cache_position_name` | `string` |  | 请求池-新广告位名称 |
| 40 | `dt` | `string` |  |  |

---

## dwd_ad_actions_v2_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_actions_v2_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 23208.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 23208.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 36 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | appid Lofer-6ED29071 |
| 2 | `req_id` | `string` |  | 请求id |
| 3 | `req_uid` | `string` |  | dsp请求uid |
| 4 | `positionid` | `bigint` |  | 广告位ID |
| 5 | `position_name` | `string` |  | 广告位名字 |
| 6 | `os` | `string` |  |  |
| 7 | `version` | `string` |  |  |
| 8 | `adid` | `string` |  |  |
| 9 | `dspid` | `bigint` |  |  |
| 10 | `slotid` | `string` |  | 广告代码位 |
| 11 | `advertiser_type` | `string` |  | 传媒客户类型（直客:0 ,非直客:1 ,品牌:2） |
| 12 | `wakeupboot` | `int` |  | 开机是否为热启动 |
| 13 | `userid` | `bigint` |  | 用户id |
| 14 | `adtrace` | `string` |  |  |
| 15 | `adtracename` | `string` |  |  |
| 16 | `bid_amount` | `double` |  | DSP价格=竞价出价*出价系数，单位为0.01元 |
| 17 | `is_valid_stock` | `int` |  | 是否有效广告库存 |
| 18 | `is_request` | `int` |  | 是否成功发起请求DPS: 0或1 |
| 19 | `is_timeout` | `int` |  | 是否请求超时: 0或1 |
| 20 | `is_forbidden` | `int` |  | 是否被封禁；0或1 |
| 21 | `is_fill` | `int` |  | 是否填充: 0或1 |
| 22 | `is_win` | `int` |  | 是否竟得: 0或1 |
| 23 | `is_bg` | `int` |  | 是否曝光；0或1 |
| 24 | `is_click` | `int` |  | 是否被点击；0或1 |
| 25 | `category` | `string` |  | 广告类目 |
| 26 | `price_type` | `string` |  | 出价类型: bidding waterfall |
| 27 | `bid_type` | `int` |  | 竞价类型（服务端：0，客户端：1） |
| 28 | `user_slot` | `bigint` |  | userslot |
| 29 | `is_download` | `int` |  | 是否下载广告素材 1是 0否 |
| 30 | `is_dp` | `int` |  | 是否触发dp请求 1是 0否 |
| 31 | `is_dp_has_app` | `int` |  | 唤起dp是否安装app 1是 0否 |
| 32 | `request_time` | `bigint` |  | 请求时间 |
| 33 | `postid` | `bigint` |  | 广告关联文章id |
| 34 | `rta_sponsorid` | `string` |  | 联运广告主 |
| 35 | `ad_type` | `string` |  | 广告类型：INNER 内部运营广告 BIDDING 竞价广告 FEE 付费合约 PDB 程序化PDB |
| 36 | `dt` | `string` |  |  |

---

## dwd_ad_amount_per_user_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_amount_per_user_di` |
| **描述** | 每天每个用户为不同广告位带来的的广告营收 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 49.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 49.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `positionid` | `string` |  | 广告位ID |
| 3 | `category` | `string` |  | 广告类别 |
| 4 | `bgpv` | `bigint` |  | 曝光PV |
| 5 | `clickpv` | `bigint` |  | 点击PV |
| 6 | `amount` | `double` |  | 广告营收,单位:元 |
| 7 | `is_canreward` | `string` |  | 信息流激励广告,1是 0否 |
| 8 | `dt` | `string` |  |  |

---

## dwd_ad_content_unlock_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_content_unlock_di` |
| **描述** | 内容广告业务解锁 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 481.4M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `module` | `string` |  | 业务类型： 激励视频、广告文、视频合集 |
| 2 | `userid` | `bigint` |  | 解锁用户 |
| 3 | `postid` | `bigint` |  | 解锁文章 |
| 4 | `blogid` | `bigint` |  |  |
| 5 | `unlock_time` | `bigint` |  | 解锁时间 毫秒时间戳 |
| 6 | `dt` | `string` |  |  |

---

## dwd_ad_content_unlock_sdk_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_content_unlock_sdk_di` |
| **描述** | 内容广告sdk解锁 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 326.3M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `module` | `string` |  | 业务类型： 激励视频、广告文、视频合集 |
| 2 | `userid` | `bigint` |  | 解锁用户 |
| 3 | `postid` | `bigint` |  | 解锁文章 |
| 4 | `blogid` | `bigint` |  | 解锁博客id |
| 5 | `bid_amount` | `double` |  | 竞价金额 单位元 |
| 6 | `dt` | `string` |  |  |

---

## dwd_ad_dsp_win_fill_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_dsp_win_fill_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2386.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2386.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `positionid` | `bigint` |  | 广告位: 1000 开屏 |
| 2 | `os` | `string` |  | 客户端系统 |
| 3 | `userid` | `bigint` |  | 用户id |
| 4 | `requestid` | `string` |  | 请求id |
| 5 | `dspid` | `string` |  | dspId |
| 6 | `client_win_price` | `float` |  | 客户端竞的价格 |
| 7 | `request_time` | `bigint` |  | 客户端请求时间 |
| 8 | `response_time` | `bigint` |  | 客户端收到响应时间 |
| 9 | `deviceudid` | `string` |  | 哈勃设备id |
| 10 | `dt` | `string` |  |  |

---

## dwd_ad_growth_device_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_growth_device_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.yangshi |
| **表类型** | external |
| **表大小** | 649.7M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

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
| 13 | `appid` | `string` |  | 业务区分 |
| 14 | `device_type` | `string` |  | 设备类别 |
| 15 | `is_ad_attributed` | `int` |  | 是否全局归因到广告（1-成功，0-失败） |
| 16 | `customudid` | `string` |  | 后台设备id |
| 17 | `custom_ouid` | `string` |  | 助推账号 |
| 18 | `photoid` | `string` |  | 视频素材id |
| 19 | `dt` | `string` |  |  |

---

## dwd_ad_growth_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_growth_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.1G |
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

## dwd_ad_growth_order_activate_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_growth_order_activate_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5.4M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单号 |
| 2 | `order_type` | `int` |  | 订单类型： 3 书城订单 |
| 3 | `actionid` | `string` |  |  |
| 4 | `customudid` | `string` |  |  |
| 5 | `deviceudid` | `string` |  |  |
| 6 | `amount` | `double` |  | 金额 |
| 7 | `media` | `string` |  |  |
| 8 | `proxy` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## dwd_ad_growth_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_growth_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.0G |
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

## dwd_ad_req_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_req_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hznieyuan |
| **表类型** | external |
| **表大小** | 484.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 484.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `req_id` | `string` |  | 请求ID |
| 2 | `appid` | `string` |  | appid Lofer-6ED29071 |
| 3 | `deviceudid` | `string` |  |  |
| 4 | `userid` | `string` |  | 用户id |
| 5 | `adcategory` | `string` |  | 广告位名字 |
| 6 | `location` | `string` |  | 广告位置 |
| 7 | `os` | `string` |  |  |
| 8 | `version` | `string` |  |  |
| 9 | `ext` | `map<string, string>` |  | 额外信息 |
| 10 | `storecount` | `int` |  | 有库存 0或1 |
| 11 | `requestcount` | `int` |  | 是否请求DPS 0或1 |
| 12 | `nonfreefillcount` | `int` |  | 非免费填充 0或1 |
| 13 | `server_return_size` | `int` |  | 服务端返回数量 |
| 14 | `client_win_count` | `int` |  | 是否客户端竞得且广告素材下载成功 0或1 |
| 15 | `client_win_adsource` | `string` |  |  |
| 16 | `client_win_slotid` | `string` |  | site 格式形如: 6ED29071:STARTUP:1 |
| 17 | `client_win_advertiser_type` | `string` |  | 传媒客户类型（直客:0 ,非直客:1 ,品牌:2） |
| 18 | `bgpv` | `int` |  | 是否有曝光监测链接 0或1 |
| 19 | `clickpv` | `int` |  | 是否有点击监测链接上报 0或1 |
| 20 | `client_bg_count` | `int` |  | 是否有客户端曝光上报 0或1 |
| 21 | `client_click_count` | `int` |  | 是否有客户端点击上报 0或1 |
| 22 | `bg_bid_amount` | `int` |  | 曝光内容出价 |
| 23 | `dp_count` | `int` |  | 曝光内容是否dp-link 0或1 |
| 24 | `dp_success_count` | `int` |  | dp-link唤起成功 0或1 |
| 25 | `nonfilteredcount` | `float` |  | 底价过滤后剩余数量 |
| 26 | `dt` | `string` |  |  |

---

## dwd_ad_resource_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_resource_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 581.5M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `groupid` | `string` |  | 资源分组: A 绘旅人1 B虚拟恋人 C 是绘旅人2 |
| 4 | `deviceid` | `string` |  |  |
| 5 | `os` | `string` |  |  |
| 6 | `optime` | `bigint` |  | 操作时间 |
| 7 | `optype` | `string` |  | 操作类型:  expose 曝光 click 点击 downopen downstart downfinish installstart installfinish |
| 8 | `materialid` | `string` |  | 素材id |
| 9 | `dt` | `string` |  |  |

---

## dwd_ad_resource_monitor_close_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_resource_monitor_close_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 44.8M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `groupid` | `string` |  | 资源分组: A 绘旅人 B虚拟恋人 |
| 3 | `materialid` | `string` |  | 素材id |
| 4 | `dt` | `string` |  |  |

---

## dwd_ad_reward_score_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_reward_score_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 444.5M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `action` | `int` |  | 行为: 1 看广告得乐乎米, 2 邀请新用户乐乎米奖励, 3 掉落乐乎米看广告膨胀, 4 连续完成任务奖励, 5 逛街任务奖励, 6 破次元恋人注册奖励, 7 换量任务奖励, 8 签到奖励, 9 签到看广告奖励, 10 看弹窗广告得乐乎米, 11 开启通知奖励, 12 站内任务奖励, 50 提现失败退回, 98 补发, 99 测试入账, -1 兑换, -2 乐乎米失效, -3 未活跃失效 |
| 3 | `action_type` | `string` |  | 积分行为类型： 发放 兑换 过期 其他 |
| 4 | `op_time` | `bigint` |  | 操作时间 |
| 5 | `score` | `bigint` |  | 操作的积分： 正值为获得， 负值为消耗 |
| 6 | `dt` | `string` |  |  |

---

## dwd_ad_reward_score_product_exchange_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_reward_score_product_exchange_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 79.9M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exchange_recordid` | `bigint` |  | 兑换记录ID |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `score` | `bigint` |  | 兑换积分 |
| 4 | `exchange_time` | `bigint` |  | 兑换时间 |
| 5 | `product_name` | `string` |  | 商品名称 |
| 6 | `productid` | `bigint` |  | 商品ID |
| 7 | `category` | `string` |  | 商品一级类目编码: 1内容 2装扮 3市集 4其他 5外部合作 |
| 8 | `subcategory` | `string` |  | 商品二级类目编码: 1-1  粮票 1-2  糖果券 1-3  虚拟人体力 1-4  IP糖果券 2-0  表情包 2-1  头像框 2-3  评论气泡 2-6  主题装扮 3-1  谷票（红包） 3-2  后悔卡 4-1  定制开屏 4-2  印鸽 4-3  现金 5-1  影视音会员 5-2  云音乐会员 |
| 9 | `product_count` | `bigint` |  | 兑换商品数量 |
| 10 | `dt` | `string` |  |  |

---

## dwd_ad_reward_task_complete_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ad_reward_task_complete_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 544.0M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `taskid` | `bigint` |  | 任务id |
| 3 | `period_key` | `string` |  | 任务周期键 |
| 4 | `task_name` | `string` |  | 任务名称 |
| 5 | `task_pay_type` | `int` |  | 任务类型： 0免费任务 1付费任务 |
| 6 | `task_stage` | `bigint` |  | 任务阶段序号 |
| 7 | `score` | `bigint` |  | 获得积分 |
| 8 | `reward_time` | `bigint` |  | 发放时间 |
| 9 | `dt` | `string` |  |  |

---

## dwd_antispam_copy_and_callback_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_antispam_copy_and_callback_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 27.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 27.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `optype` | `string` |  | 抄送回调类型,comment,message,post_version etc |
| 2 | `userid` | `bigint` |  | 博客ID |
| 3 | `bussinessid` | `bigint` |  | 内容ID |
| 4 | `bizinfo` | `string` |  | 抄送类型 |
| 5 | `uuid` | `string` |  | 抄送id |
| 6 | `status` | `int` |  | 状态 |
| 7 | `version` | `bigint` |  | 版本 |
| 8 | `machine` | `int` |  | 机审或人审标识 |
| 9 | `flag` | `string` |  | normal or mask,代表内容正常还是被屏蔽 |
| 10 | `dt` | `string` |  |  |

---

## dwd_ask_publish_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ask_publish_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | internal |
| **表大小** | 150.3M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 问答文章id |
| 2 | `userid` | `bigint` |  | 创作者用户id |
| 3 | `blog_name` | `string` |  | 博客名称 |
| 4 | `post_title` | `string` |  | 文章标题 |
| 5 | `post_tags` | `array<string>` |  | 文章标签 |
| 6 | `post_ips` | `array<string>` |  | 文章ip |
| 7 | `post_domains` | `array<string>` |  | 文章领域 |
| 8 | `post_content_type` | `string` |  | 文章类型： 问答 |
| 9 | `post_publish_date` | `string` |  | 发文日期： 业务上发文日期（文章展示发文日期） |
| 10 | `post_publish_time` | `string` |  | 发文时间 |
| 11 | `blog_nickname` | `string` |  | 博客昵称 |
| 12 | `is_user_first_post` | `int` |  | 是否创作者首发文 |
| 13 | `platform` | `string` |  | 端类型 |
| 14 | `dt` | `string` |  |  |

---

## dwd_beginner_guide_page_events_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_beginner_guide_page_events_di` |
| **描述** | 用户与AB实验相关的行为统计 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 96.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 96.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `customudid` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `deviceudid` | `string` |  |  |
| 5 | `category_expose_cnt` | `bigint` |  | category曝光次数 |
| 6 | `ip_expose_cnt` | `bigint` |  | ip曝光次数 |
| 7 | `tag_expose_cnt` | `bigint` |  | tag曝光次数 |
| 8 | `click_cancel_category_cnt` | `bigint` |  | 点击取消选择category的次数 |
| 9 | `click_cancel_ip_cnt` | `bigint` |  | 点击取消选择ip的次数 |
| 10 | `click_cancel_tag_cnt` | `bigint` |  | 点击取消选择ip的次数 |
| 11 | `click_choose_category_cnt` | `bigint` |  | 点击选择category的次数 |
| 12 | `click_choose_ip_cnt` | `bigint` |  | 点击选择Ip的次数 |
| 13 | `click_choose_tag_cnt` | `bigint` |  | 点击选择Tag的次数 |
| 14 | `click_skip_cnt` | `bigint` |  | 点击跳过的次数 |
| 15 | `click_rename_cnt` | `bigint` |  | 点击修改名字的次数 |
| 16 | `deviceos` | `string` |  | 系统 |
| 17 | `app_front2backend_cnts` | `int` |  | APP切至后台次数数 |
| 18 | `app_backend2front_cnts` | `int` |  | APP从后台切至前台次数 |
| 19 | `slide_left_cnts` | `int` |  | 单日志页向左滑次数 |
| 20 | `slide_right_cnts` | `int` |  | 单日志页向右滑次数 |
| 21 | `exposed_comment_cnts` | `int` |  | 评论曝光条数 |
| 22 | `underscore_window_exposed_cnt` | `bigint` |  | 划线评弹窗曝光数 |
| 23 | `article_post_cover_exposed_cnt` | `bigint` |  | 文字文章带封面曝光数 |
| 24 | `article_post_cover_clicked_cnt` | `bigint` |  | 文字文章带封面点击数 |
| 25 | `watch_later_func_usage_cnt` | `bigint` |  | 稍后再看功能使用次数 |
| 26 | `discover_refresh_tip_exposed_cnt` | `bigint` |  | 发现页刷新提示曝光数 |
| 27 | `discover_refresh_tip_click_cnt` | `bigint` |  | 发现页刷新提示点击数 |
| 28 | `dt` | `string` |  |  |

---

## dwd_benefit_trade_order_product_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_benefit_trade_order_product_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 637.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 637.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tradeid` | `bigint` |  | 交易ID |
| 2 | `thirdpartynum` | `string` |  | 第三方标志 |
| 3 | `userid` | `bigint` |  | 用户ID |
| 4 | `producttype` | `int` |  | 商品类型 |
| 5 | `status` | `int` |  | 交易状态 |
| 6 | `orderfrom` | `string` |  | 交易来源 |
| 7 | `paymethod` | `string` |  | 支付方式 |
| 8 | `trade_money` | `double` |  | 交易实付金额 |
| 9 | `tradetime` | `bigint` |  | 交易时间 |
| 10 | `paytime` | `bigint` |  | 支付时间 |
| 11 | `orderid` | `bigint` |  | 订单ID |
| 12 | `ordertime` | `bigint` |  | 订单时间 |
| 13 | `order_money` | `double` |  | 订单实付金额 |
| 14 | `deliveryprice` | `double` |  | 运费 |
| 15 | `deliverytime` | `bigint` |  | 发货时间 |
| 16 | `walfare` | `double` |  | 福利币 |
| 17 | `newcouponid` | `bigint` |  | 优惠券ID |
| 18 | `supplierid` | `bigint` |  | 供应商ID |
| 19 | `productid` | `bigint` |  | 商品ID |
| 20 | `productname` | `string` |  | 商品名称 |
| 21 | `producttime` | `bigint` |  | 商品创建时间 |
| 22 | `productgroupid` | `bigint` |  | 商品组ID，可关联skuid |
| 23 | `presaletype` | `int` |  | 商品售卖类型 |
| 24 | `productnum` | `bigint` |  | 购买商品数量 |
| 25 | `storeprice` | `double` |  | 福利市集价格 |
| 26 | `marketprice` | `double` |  | 商品市场价格 |
| 27 | `newcouponpreferential` | `double` |  | 平台优惠券金额 |
| 28 | `bountypreferential` | `double` |  | 商家津贴金额 |
| 29 | `product_money` | `double` |  | 购买该商品的实付金额 |
| 30 | `slotnum` | `int` |  | 抽赏次数 |
| 31 | `dt` | `string` |  |  |

---

## dwd_blog_follow_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_blog_follow_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 17.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 17.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `blogid` | `bigint` |  | 关注博客id |
| 3 | `follow_time` | `bigint` |  | 关注时间 |
| 4 | `platform` | `string` |  | 关注操作平台 |
| 5 | `deviceudid` | `string` |  | 客户端哈勃id |
| 6 | `deviceos` | `string` |  | 客户端系统 |
| 7 | `appversion` | `string` |  | 客户端版本 |
| 8 | `scene` | `string` |  | 场景 |
| 9 | `source1_scene` | `string` |  |  |
| 10 | `source2_scene` | `string` |  |  |
| 11 | `module` | `string` |  |  |
| 12 | `source1_module` | `string` |  |  |
| 13 | `source2_module` | `string` |  |  |
| 14 | `source_postid` | `bigint` |  | 关注来源文章Id |
| 15 | `dt` | `string` |  |  |

---

## dwd_blog_intro_sensitive_word_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_blog_intro_sensitive_word_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 17.1M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客ID |
| 2 | `sensitive_word` | `string` |  | 敏感词 |
| 3 | `dt` | `string` |  |  |

---

## dwd_blog_nickname_sensitive_word_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_blog_nickname_sensitive_word_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 40.8M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客ID |
| 2 | `sensitive_word` | `string` |  | 敏感词 |
| 3 | `dt` | `string` |  |  |

---

## dwd_cc_module_query_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_cc_module_query_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 175.1M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `eventid` | `string` |  | ka1-10 客服搜索查询 ka1-11客服搜索结果点击， 其他见moduleName |
| 4 | `name` | `string` |  | 客服查询名称 |
| 5 | `modulename` | `string` |  | 客服查询模块名称 |
| 6 | `occurtime` | `bigint` |  | 查询时间 |
| 7 | `search_keyword` | `string` |  | 客服搜索查询-搜索词 |
| 8 | `search_result_url` | `string` |  | 客服搜索查询-结果链接 |
| 9 | `dt` | `string` |  |  |

---

## dwd_cold_ip_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_cold_ip_dd` |
| **描述** | 冷圈ip |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.1M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 冷圈ip |
| 2 | `post_cnt_30d` | `bigint` |  | 30日发文量 |
| 3 | `text_post_cnt_30d` | `bigint` |  | 文字文章30日发文量 |
| 4 | `text_pay_only_post_cnt_30d` | `bigint` |  | 文字文章仅付费30日发文量 |
| 5 | `dt` | `string` |  |  |

---

## dwd_collection_detail_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_collection_detail_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 139.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 139.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `collectionid` | `bigint` |  | 合集id |
| 2 | `deviceudid` | `string` |  | 设备id |
| 3 | `userid` | `bigint` |  | 用户id |
| 4 | `occurtime` | `bigint` |  | 发生时间 |
| 5 | `deviceos` | `string` |  | 操作系统 |
| 6 | `appversion` | `string` |  | app版本 |
| 7 | `dt` | `string` |  |  |

---

## dwd_collection_user_subscribe_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_collection_user_subscribe_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 445.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 445.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `subscribe_userid` | `bigint` |  | 合集订阅用户 |
| 2 | `subscribetime` | `bigint` |  | 订阅时间 |
| 3 | `lastfetchtime` | `bigint` |  | 上次拉取时间 |
| 4 | `collectionid` | `bigint` |  | 合集Id |
| 5 | `tags` | `string` |  | 合集所带标签 |
| 6 | `blogid` | `bigint` |  | 合集的博客Id |
| 7 | `cover_url` | `string` |  | 合集封面链接 |
| 8 | `name` | `string` |  | 合集名称 |
| 9 | `createtime` | `bigint` |  | 合集创建时间 |
| 10 | `lastpublishtime` | `bigint` |  | 合集最近更新时间 |
| 11 | `status` | `int` |  | 合集状态，0：正常，-1：被封禁 |
| 12 | `collectiontype` | `int` |  | 合集类型，0：普通合集，1：共创合集 |
| 13 | `postcount` | `bigint` |  | 合集文章数 |
| 14 | `postcollectionhot` | `bigint` |  | 合集热度 |
| 15 | `viewcount` | `bigint` |  | 合集浏览数 |
| 16 | `dt` | `string` |  |  |

---

## dwd_content_browse_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_content_browse_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 282.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 282.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `occurtime` | `bigint` |  | 浏览时间 |
| 4 | `source` | `string` |  | 来源 |
| 5 | `scene` | `string` |  | 场景 |
| 6 | `eventid` | `string` |  | 事件id |
| 7 | `deviceos` | `string` |  | 操作系统 |
| 8 | `appversion` | `string` |  | app版本 |
| 9 | `itemid` | `bigint` |  | 浏览物品id |
| 10 | `item_type` | `string` |  | 浏览物品类型 |
| 11 | `is_rec` | `int` |  | 是否推荐场景 |
| 12 | `dt` | `string` |  |  |

---

## dwd_coupon_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_coupon_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 158.7M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `pay_time` | `bigint` |  | 支付时间 |
| 4 | `amount` | `double` |  | 金额 |
| 5 | `postid` | `bigint` |  | 文章id |
| 6 | `actpwd` | `string` |  | 口令 |
| 7 | `deviceudid` | `string` |  | 设备id |
| 8 | `actpwd_channel` | `string` |  | 口令渠道 |
| 9 | `actpwd_search_time` | `bigint` |  | 口令搜索时间 |
| 10 | `blogid` | `bigint` |  | 文章博客id |
| 11 | `activity_name` | `string` |  | 券包活动名称 |
| 12 | `platform` | `int` |  | 1安卓, 2苹果 |
| 13 | `dt` | `string` |  |  |

---

## dwd_device_all_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_all_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 3040.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3040.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备Udid |
| 2 | `deviceos` | `string` |  | 设备对应的操作系统 |
| 3 | `first_active_time` | `bigint` |  | 设备首次激活时间 |
| 4 | `userids` | `array<bigint>` |  | 该设备上使用的userId |
| 5 | `last_active_date` | `string` |  | 最近一次活跃日期 |
| 6 | `is_active_today` | `int` |  | 当日是否活跃：1活跃，0否 |
| 7 | `last_return_date` | `string` |  | 最近一次回流日期 |
| 8 | `device_type` | `string` |  | 设备来源类型：new,return_30,remain,lost |
| 9 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：搜索/广告/自然增长 |
| 10 | `origin_channel` | `string` |  | 来源渠道 |
| 11 | `origin_actpwd` | `string` |  | 搜索口令 |
| 12 | `last_growth_date` | `string` |  | 最近一次增长记录日期 |
| 13 | `dt` | `string` |  |  |

---

## dwd_device_apk_install_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_apk_install_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 703.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 703.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `apk_name` | `string` |  | apk名称 |
| 3 | `is_installed` | `int` |  | 是否安装 |
| 4 | `occurtime` | `bigint` |  | 最近一次检测时间 |
| 5 | `dt` | `string` |  |  |

---

## dwd_device_collect_apk_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_collect_apk_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1734.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1734.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `occurtime` | `bigint` |  | 采集时间 |
| 4 | `apk_name` | `string` |  | 采集应用包名 |
| 5 | `is_installed` | `int` |  | 是否安装 |
| 6 | `idfa` | `string` |  |  |
| 7 | `imei` | `string` |  |  |
| 8 | `androidid` | `string` |  |  |
| 9 | `oaid` | `string` |  |  |
| 10 | `idfv` | `string` |  |  |
| 11 | `deviceos` | `string` |  | 操作系统: Android iOS |
| 12 | `caid` | `string` |  |  |
| 13 | `dt` | `string` |  |  |

---

## dwd_device_growth_attribution_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_growth_attribution_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 15.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 15.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 30 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 2 | `deviceudid` | `string` |  | 设备Id |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：广告/口令/自然增长 |
| 4 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 5 | `origin_actpwd` | `string` |  | 设备来源归因-口令：　搜索口令 |
| 6 | `land_nonrec_userid` | `bigint` |  | 承接非推荐内容归因-用户id |
| 7 | `land_nonrec_itemid` | `bigint` |  | 承接非推荐内容归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 8 | `land_nonrec_content_type` | `string` |  | 承接非推荐内容归因-内容类型: 单日志 合集 粮单 |
| 9 | `land_nonrec_link` | `string` |  | 承接非推荐内容归因-口令链接 |
| 10 | `land_nonrec_occurtime` | `bigint` |  | 承接非推荐内容归因-浏览时间 |
| 11 | `land_rec_userid` | `bigint` |  | 承接推荐内容归因-用户id |
| 12 | `land_rec_itemid` | `bigint` |  | 承接推荐内容归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 13 | `land_rec_content_type` | `string` |  | 承接推荐内容归因-内容类型: 单日志 合集 粮单 |
| 14 | `land_rec_occurtime` | `bigint` |  | 承接推荐内容归因-浏览时间 |
| 15 | `is_whiteboard` | `int` |  | 是否白板用户 |
| 16 | `is_paid_subscribe` | `int` |  | 是否付费订阅口令拉新 |
| 17 | `occurtime` | `bigint` |  | 新增或回流时间 |
| 18 | `deviceos` | `string` |  | 操作系统 |
| 19 | `origin_type_level2` | `string` |  | 二级来源类型： 分享 |
| 20 | `land_nonrec_loose_userid` | `bigint` |  | 承接非推荐内容1小时归因-用户id |
| 21 | `land_nonrec_loose_itemid` | `bigint` |  | 承接非推荐内容1小时归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 22 | `land_nonrec_loose_content_type` | `string` |  | 承接非推荐内容1小时归因-内容类型: 单日志 合集 粮单 |
| 23 | `land_nonrec_loose_occurtime` | `bigint` |  | 承接非推荐内容1小时归因-浏览时间 |
| 24 | `land_nonrec_loose_link` | `string` |  | 承接非推荐内容1小时归因-口令链接 |
| 25 | `land_rec_loose_userid` | `bigint` |  | 承接推荐内容1小时归因-用户id |
| 26 | `land_rec_loose_itemid` | `bigint` |  | 承接推荐内容1小时归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 27 | `land_rec_loose_content_type` | `string` |  | 承接推荐内容1小时归因-内容类型: 单日志 合集 粮单 |
| 28 | `land_rec_loose_occurtime` | `bigint` |  | 承接推荐内容1小时归因-浏览时间 |
| 29 | `proxy` | `string` |  | 渠道代理 |
| 30 | `dt` | `string` |  |  |

---

## dwd_device_growth_attribution_v2_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_growth_attribution_v2_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 9.8G |
| **是否分区表** | 是 |

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 2 | `deviceudid` | `string` |  | 设备Id |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 4 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 5 | `origin_actpwd` | `string` |  | 设备来源归因-口令：　搜索口令 |
| 6 | `land_nonrec_userid` | `bigint` |  | 承接非推荐内容归因-用户id |
| 7 | `land_nonrec_itemid` | `bigint` |  | 承接非推荐内容归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 8 | `land_nonrec_content_type` | `string` |  | 承接非推荐内容归因-内容类型: 单日志 合集 粮单 |
| 9 | `land_nonrec_link` | `string` |  | 承接非推荐内容归因-口令链接 |
| 10 | `land_nonrec_occurtime` | `bigint` |  | 承接非推荐内容归因-浏览时间 |
| 11 | `land_rec_userid` | `bigint` |  | 承接推荐内容归因-用户id |
| 12 | `land_rec_itemid` | `bigint` |  | 承接推荐内容归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 13 | `land_rec_content_type` | `string` |  | 承接推荐内容归因-内容类型: 单日志 合集 粮单 |
| 14 | `land_rec_occurtime` | `bigint` |  | 承接推荐内容归因-浏览时间 |
| 15 | `is_whiteboard` | `int` |  | 是否白板用户 |
| 16 | `is_paid_subscribe` | `int` |  | 是否付费订阅口令拉新 |
| 17 | `occurtime` | `bigint` |  | 新增或回流时间 |
| 18 | `deviceos` | `string` |  | 操作系统 |
| 19 | `origin_type_level2` | `string` |  | 二级来源类型： 分享 |
| 20 | `land_nonrec_loose_userid` | `bigint` |  | 承接非推荐内容1小时归因-用户id |
| 21 | `land_nonrec_loose_itemid` | `bigint` |  | 承接非推荐内容1小时归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 22 | `land_nonrec_loose_content_type` | `string` |  | 承接非推荐内容1小时归因-内容类型: 单日志 合集 粮单 |
| 23 | `land_nonrec_loose_occurtime` | `bigint` |  | 承接非推荐内容1小时归因-浏览时间 |
| 24 | `land_nonrec_loose_link` | `string` |  | 承接非推荐内容1小时归因-口令链接 |
| 25 | `land_rec_loose_userid` | `bigint` |  | 承接推荐内容1小时归因-用户id |
| 26 | `land_rec_loose_itemid` | `bigint` |  | 承接推荐内容1小时归因-内容id 根据content_type分别对应单日志/合集/粮单的id/活动code |
| 27 | `land_rec_loose_content_type` | `string` |  | 承接推荐内容1小时归因-内容类型: 单日志 合集 粮单 |
| 28 | `land_rec_loose_occurtime` | `bigint` |  | 承接推荐内容1小时归因-浏览时间 |
| 29 | `dt` | `string` |  |  |

---

## dwd_device_growth_content_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_growth_content_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 9.4G |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备Id |
| 2 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 4 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 5 | `origin_actpwd` | `string` |  | 设备来源归因-口令：　搜索口令 |
| 6 | `is_paid_subscribe` | `int` |  | 是否付费订阅口令拉新 |
| 7 | `content_type` | `string` |  | 内容类型: 图片 文字 视频 合集 粮单 |
| 8 | `itemid` | `bigint` |  | 内容id |
| 9 | `blogid` | `bigint` |  | 拉新内容归属的用户id |
| 10 | `publish_date` | `bigint` |  | 拉新内容发布日期 |
| 11 | `tags` | `string` |  | 标签名称","分隔 |
| 12 | `ips` | `string` |  | 所属IP ","分隔 |
| 13 | `title` | `string` |  | 内容标题 |
| 14 | `url` | `string` |  | 内容链接 |
| 15 | `is_book_store` | `int` |  | 是否书城内容 |
| 16 | `dt` | `string` |  |  |

---

## dwd_device_mapping

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_mapping` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 211.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 211.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `sid` | `string` |  | 源id |
| 2 | `tid` | `string` |  | 目标id |
| 3 | `firsttime` | `bigint` |  | 首次关联时间 |
| 4 | `lasttime` | `bigint` |  | 最后关联时间 |
| 5 | `firstno` | `bigint` |  | 最早关联设备序号 |
| 6 | `lastno` | `bigint` |  | 最新关联设备序号 |
| 7 | `sid_tp` | `string` |  |  |
| 8 | `tid_tp` | `string` |  |  |

---

## dwd_device_mapping_detail_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_device_mapping_detail_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 2095.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2095.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `ip` | `string` |  |  |
| 3 | `userid` | `bigint` |  |  |
| 4 | `customudid` | `string` |  |  |
| 5 | `idfa` | `string` |  |  |
| 6 | `imei` | `string` |  |  |
| 7 | `firsttime` | `bigint` |  |  |
| 8 | `lasttime` | `bigint` |  |  |
| 9 | `androidid` | `string` |  |  |
| 10 | `oaid` | `string` |  |  |
| 11 | `idfv` | `string` |  |  |
| 12 | `isanonymous` | `int` |  | 是否匿名用户 1是 0否 20230511添加 |
| 13 | `dt` | `string` |  |  |

---

## dwd_dstr_flow_task_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_dstr_flow_task_action_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 162.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 162.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `flowtaskid` | `bigint` |  |  |
| 2 | `flowtasktype` | `bigint` |  |  |
| 3 | `userid` | `bigint` |  |  |
| 4 | `postid` | `bigint` |  |  |
| 5 | `postblogid` | `bigint` |  |  |
| 6 | `occurtime` | `bigint` |  |  |
| 7 | `is_exposure` | `int` |  |  |
| 8 | `is_click` | `int` |  |  |
| 9 | `hot` | `bigint` |  |  |
| 10 | `dt` | `string` |  |  |

---

## dwd_dstr_flow_task_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_dstr_flow_task_post_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 39.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 39.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  |  |
| 2 | `userid` | `bigint` |  |  |
| 3 | `flowtaskid` | `bigint` |  |  |
| 4 | `flowtasktype` | `bigint` |  |  |
| 5 | `flowtaskname` | `string` |  |  |
| 6 | `createtime` | `bigint` |  |  |
| 7 | `starttime` | `bigint` |  |  |
| 8 | `endtime` | `bigint` |  |  |
| 9 | `status` | `int` |  |  |
| 10 | `creator` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## dwd_ec_add_cart_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_add_cart_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 277.3M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `occurtime` | `bigint` |  |  |
| 4 | `productid` | `bigint` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dwd_ec_derivate_gmv_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_derivate_gmv_di` |
| **描述** | 电商衍生品订单GMV明细表 |
| **Owner** | bdms_wb.yangshi |
| **表类型** | external |
| **表大小** | 4.8M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `tradeid` | `bigint` |  | 交易id |
| 3 | `product_num` | `int` |  | 商品数量 |
| 4 | `channel` | `string` |  | 渠道 |
| 5 | `suppliertype` | `string` |  | 供应商类型 |
| 6 | `postage` | `double` |  | 订单邮费 |
| 7 | `payamount` | `double` |  | 实付金额 |
| 8 | `dt` | `string` |  |  |

---

## dwd_ec_derivate_ipseries_category_refund_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_derivate_ipseries_category_refund_di` |
| **描述** | 电商衍生品ip系列退收明细表 |
| **Owner** | bdms_wb.yangshi |
| **表类型** | external |
| **表大小** | 774.7K |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `productid` | `bigint` |  | 商品id |
| 3 | `productname` | `string` |  | 商品名称 |
| 4 | `channel` | `string` |  | 渠道 |
| 5 | `suppliertype` | `string` |  | 供应商类型 |
| 6 | `ipname` | `string` |  | ip名称 |
| 7 | `seriesname` | `string` |  | 系列名称 |
| 8 | `categoryname` | `string` |  | 品类名称 |
| 9 | `refund_qty` | `bigint` |  | 商品数量 |
| 10 | `amount` | `double` |  | 金额 |
| 11 | `dt` | `string` |  |  |

---

## dwd_ec_derivate_ipseries_category_revenue_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_derivate_ipseries_category_revenue_di` |
| **描述** | 电商衍生品ip系列收入明细表 |
| **Owner** | bdms_wb.yangshi |
| **表类型** | external |
| **表大小** | 2.2M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `productid` | `bigint` |  | 商品id |
| 3 | `productname` | `string` |  | 商品名称 |
| 4 | `channel` | `string` |  | 渠道 |
| 5 | `suppliertype` | `string` |  | 供应商类型 |
| 6 | `ipname` | `string` |  | ip名称 |
| 7 | `seriesname` | `string` |  | 系列名称 |
| 8 | `categoryname` | `string` |  | 品类名称 |
| 9 | `ship_qty` | `bigint` |  | 商品数量 |
| 10 | `amount` | `double` |  | 金额 |
| 11 | `dt` | `string` |  |  |

---

## dwd_ec_derivate_refund_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_derivate_refund_di` |
| **描述** | 电商衍生品订单退收明细表 |
| **Owner** | bdms_wb.yangshi |
| **表类型** | external |
| **表大小** | 392.7K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `product_num` | `bigint` |  | 退收商品数量 |
| 3 | `channel` | `string` |  | 渠道 |
| 4 | `suppliertype` | `string` |  | 供应商类型 |
| 5 | `refundtype` | `int` |  | 退款类型0现金商品退款单,1抽赏退款订单,2抽赏无需退款单,3退订,4退货 |
| 6 | `refund` | `double` |  | 退收金额 |
| 7 | `dt` | `string` |  |  |

---

## dwd_ec_derivate_revenue_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_derivate_revenue_di` |
| **描述** | 电商衍生品订单收入明细表 |
| **Owner** | bdms_wb.yangshi |
| **表类型** | external |
| **表大小** | 1.2M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id(发货单id) |
| 2 | `ship_qty` | `bigint` |  | 发货数量 |
| 3 | `deliverymode` | `int` |  | 发货方式1部分发货 2整单发货 |
| 4 | `channel` | `string` |  | 渠道 |
| 5 | `suppliertype` | `string` |  | 供应商类型 |
| 6 | `revenue` | `double` |  | 收入金额(不包含运费)) |
| 7 | `freight_revenue` | `double` |  | 运费收入 |
| 8 | `dt` | `string` |  |  |

---

## dwd_ec_product_expose_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_product_expose_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 54.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 54.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  | 事件Id |
| 2 | `deviceudid` | `string` |  | 哈勃设备Id |
| 3 | `userid` | `bigint` |  | 用户Id |
| 4 | `occurtime` | `bigint` |  | 发生时间 |
| 5 | `deviceos` | `string` |  | 系统 |
| 6 | `appkey` | `string` |  | app key |
| 7 | `costtime` | `bigint` |  | 消耗时间 |
| 8 | `source` | `string` |  | 来源 |
| 9 | `itemid` | `string` |  | 物料Id |
| 10 | `itemtype` | `string` |  | 物料类型 |
| 11 | `recid` | `string` |  | 推荐Id |
| 12 | `scene` | `string` |  | 场景 |
| 13 | `params` | `map<string, string>` |  | 参数 |
| 14 | `dt` | `string` |  |  |

---

## dwd_ec_product_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_product_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 39.6M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单Id |
| 2 | `status` | `bigint` |  | 状态 |
| 3 | `createtime` | `bigint` |  | 创建时间 |
| 4 | `buyerid` | `string` |  | 应用中的用户 |
| 5 | `amount` | `double` |  | 付款 |
| 6 | `paytime` | `bigint` |  | 支付时间 |
| 7 | `tradeid` | `bigint` |  | 交易id |
| 8 | `origintype` | `int` |  | 订单来源，0正常支付订单 1活动生成订单 |
| 9 | `productid` | `bigint` |  | 商品id |
| 10 | `producttype` | `bigint` |  | 商品类型 |
| 11 | `storeprice` | `double` |  | 市集价或福利币价格 |
| 12 | `marketprice` | `double` |  | 市场价 |
| 13 | `deductprice` | `double` |  | 福利币最高抵扣价格 |
| 14 | `pricetype` | `bigint` |  | 价格类型 |
| 15 | `productnum` | `bigint` |  | 商品数目 |
| 16 | `newcouponpreferential` | `double` |  | 商品平摊的订单优惠金额 |
| 17 | `adtrace` | `string` |  | 广告跟踪标识 |
| 18 | `cardactivitycode` | `string` |  | 抽卡活动code |
| 19 | `dt` | `string` |  |  |

---

## dwd_ec_trace_product_expose_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_trace_product_expose_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 64.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 64.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `deviceudid` | `string` |  | 设备id |
| 3 | `url` | `string` |  | url: 商品/房间/cms链接 |
| 4 | `adtrace` | `string` |  | adTrace跟踪串 |
| 5 | `activitycode` | `string` |  | 房间号 |
| 6 | `productid` | `string` |  | 商品id |
| 7 | `occurtime` | `bigint` |  | 发生时间 |
| 8 | `dt` | `string` |  |  |

---

## dwd_ec_trace_product_view_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_trace_product_view_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.7G |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `deviceudid` | `string` |  | 设备id |
| 3 | `url` | `string` |  | url: 商品/房间/cms链接 |
| 4 | `adtrace` | `string` |  | adTrace跟踪串 |
| 5 | `activitycode` | `string` |  | 房间号 |
| 6 | `productid` | `string` |  | 商品id |
| 7 | `occurtime` | `bigint` |  | 发生时间 |
| 8 | `dt` | `string` |  |  |

---

## dwd_ec_yanxuan_ad_click_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_yanxuan_ad_click_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 804.8K |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | lofter用户ID |
| 2 | `occurtime` | `bigint` |  | 点击时间 |
| 3 | `deviceid` | `string` |  | idfa/imei md5 |
| 4 | `dt` | `string` |  | 日期分区 |

---

## dwd_ec_yanxuan_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ec_yanxuan_order_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.5M |
| **是否分区表** | 是 |

### 字段详情

共 37 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `createtime` | `bigint` |  | 创建时间 |
| 3 | `status` | `bigint` |  | 状态 |
| 4 | `buyerid` | `string` |  | 应用中的用户 |
| 5 | `productid` | `bigint` |  | 商品id |
| 6 | `type` | `bigint` |  | 商品类型 |
| 7 | `productname` | `string` |  | 商品名称 |
| 8 | `productdetail` | `string` |  | 商品详情 |
| 9 | `storeprice` | `double` |  | 市集价或福利币价格 |
| 10 | `marketprice` | `double` |  | 市场价 |
| 11 | `deductprice` | `double` |  | 福利币最高抵扣价格 |
| 12 | `pricetype` | `bigint` |  | 价格类型 |
| 13 | `freepost` | `bigint` |  | 是否免邮 |
| 14 | `productcontent` | `string` |  | 商品内容 |
| 15 | `productext` | `string` |  | 商品额外信息 |
| 16 | `productnum` | `bigint` |  | 商品数目 |
| 17 | `attrgroupid` | `bigint` |  | 属性组id |
| 18 | `attrgroupext` | `string` |  | 属性组额外信息 |
| 19 | `newcouponpreferential` | `double` |  | 商品平摊的订单优惠金额 |
| 20 | `supplierassumeamount` | `double` |  | 商家承担优惠券金额 |
| 21 | `supplierassumebountyamount` | `double` |  | 商家承担的津贴金额 |
| 22 | `bountypreferential` | `double` |  | 津贴的优惠金额， 从 benefit_order 中分拆 |
| 23 | `receivername` | `string` |  | 收货人姓名 |
| 24 | `receiverphone` | `string` |  | 收货人电话 |
| 25 | `receiveraddress` | `string` |  | 收货人地址 |
| 26 | `receiveremail` | `string` |  | 收货人邮箱 |
| 27 | `receiverzip` | `string` |  | 收货人邮编 |
| 28 | `receiverpro` | `string` |  | 省 |
| 29 | `receivercity` | `string` |  | 市 |
| 30 | `receiverreg` | `string` |  | 区 |
| 31 | `receiverext` | `string` |  | 收货人其他信息 |
| 32 | `amount` | `double` |  | 付款 |
| 33 | `paytime` | `bigint` |  | 支付时间 |
| 34 | `paymethod` | `string` |  | 支付方式 |
| 35 | `skuamount` | `double` |  | 分摊金额 |
| 36 | `deviceid` | `string` |  | 设备标识 md5(idfa/imei) |
| 37 | `dt` | `string` |  | 日期分区 |

---

## dwd_ecology_ai_infringe_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ecology_ai_infringe_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 487.8M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章Id |
| 2 | `blogid` | `bigint` |  | 创作者Id |
| 3 | `userid` | `bigint` |  | 举报用户Id |
| 4 | `source` | `int` |  | 1: 负反馈卡片，2: 侵权后台，3: 算法AI文识别，4: 合伙人众裁 |
| 5 | `createtime` | `bigint` |  | 创建时间 |
| 6 | `createdate` | `string` |  | 创建时间(yyyy-MM-dd格式) |
| 7 | `dt` | `string` |  |  |

---

## dwd_emote_dun_ti_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_emote_dun_ti_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 4.1G |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `commentid` | `bigint` |  | 评论Id |
| 2 | `postid` | `bigint` |  | 文章Id |
| 3 | `blogid` | `bigint` |  | 博客Id |
| 4 | `dunuserid` | `bigint` |  | 求蹲的用户Id |
| 5 | `tistatus` | `int` |  | 踢的状态：0未踢，1已踢 |
| 6 | `paicount` | `bigint` |  | 拍的数量 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `dbupdatetime` | `bigint` |  |  |
| 9 | `titime` | `bigint` |  | 踢时间 |
| 10 | `dt` | `string` |  |  |

---

## dwd_evt_avatar_box_access_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_evt_avatar_box_access_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 575.7M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `occurtime` | `bigint` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dwd_evt_benefit_page_view_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_evt_benefit_page_view_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 813.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 813.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `userid` | `bigint` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `productid` | `bigint` |  |  |
| 6 | `params` | `map<string, string>` |  |  |
| 7 | `occurtime` | `bigint` |  | 事件发生时间 |
| 8 | `url` | `string` |  | 事件发生页面链接 |
| 9 | `adtrace` | `string` |  | 资源/广告位跟踪串 |
| 10 | `activitycode` | `string` |  | 抽赏房间号 |
| 11 | `itemid` | `string` |  | 物料id |
| 12 | `itemtype` | `string` |  | 物料类型 |
| 13 | `scene` | `string` |  | 场景 |
| 14 | `appkey` | `string` |  |  |
| 15 | `costtime` | `bigint` |  |  |
| 16 | `source` | `string` |  |  |
| 17 | `dt` | `string` |  |  |

---

## dwd_evt_post_paid_detail_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_evt_post_paid_detail_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 5685.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5685.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 付费文章 |
| 2 | `blogid` | `bigint` |  | 文章对应的博客id |
| 3 | `money` | `double` |  | 消费金额，元 |
| 4 | `userid` | `bigint` |  | 消费用户ID |
| 5 | `createtime` | `bigint` |  | 创建时间 |
| 6 | `pay_type` | `string` |  | 消费类型：付费礼物，博客订阅，粉丝会员 |
| 7 | `dt` | `string` |  |  |

---

## dwd_evt_user_login_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_evt_user_login_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 10825.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 10825.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `accountid` | `bigint` |  | 用户Id |
| 2 | `deviceudid` | `string` |  | 设备Udid |
| 3 | `clienttype` | `string` |  | 客户端类型 |
| 4 | `actiontype` | `string` |  | 动作类型 |
| 5 | `ip` | `string` |  | 设备IP |
| 6 | `logintype` | `string` |  |  |
| 7 | `appversion` | `string` |  |  |
| 8 | `userremoteport` | `bigint` |  |  |
| 9 | `actiontime` | `bigint` |  |  |
| 10 | `uri` | `string` |  | uri |
| 11 | `login_country` | `string` |  | 登陆IP国家 |
| 12 | `login_province` | `string` |  | 登陆IP省份 |
| 13 | `login_city` | `string` |  | 登陆IP城市 |
| 14 | `dt` | `string` |  |  |

---

## dwd_evt_webview_index_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_evt_webview_index_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1002.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1002.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `occurtime` | `bigint` |  | 事件时间 |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `deviceudid` | `string` |  | 设备ID |
| 4 | `deviceos` | `string` |  | 操作系统 |
| 5 | `deviceosversion` | `string` |  | 系统版本 |
| 6 | `appversion` | `string` |  | APP版本 |
| 7 | `ip` | `string` |  | 用户IP |
| 8 | `devicenetwork` | `string` |  | 设备网络 |
| 9 | `devicecarrier` | `string` |  | 设备供应商 |
| 10 | `country` | `string` |  | IP所属国家 |
| 11 | `province` | `string` |  | IP所属省份 |
| 12 | `city` | `string` |  | IP所属城市 |
| 13 | `sessionid` | `string` |  | 页面标识 |
| 14 | `websessionid` | `string` |  | 页面完整路径 |
| 15 | `isfirst` | `string` |  | 容器是否首次打开 |
| 16 | `ismp` | `string` |  | 是否小程序 |
| 17 | `mpid` | `string` |  | 小程序ID |
| 18 | `mpversion` | `string` |  | 小程序版本 |
| 19 | `useragent` | `string` |  | UA |
| 20 | `createendtime` | `bigint` |  | 容器初始化结束时间 |
| 21 | `createstarttime` | `bigint` |  | 容器初始化开始时间 |
| 22 | `loadmpresendtime` | `bigint` |  | 加载小程序结束时间 |
| 23 | `loadmpresstarttime` | `bigint` |  | 加载小程序开始时间 |
| 24 | `pageurl` | `string` |  | 页面链接 |
| 25 | `webviewcreatetime` | `bigint` |  | 容器创建耗时 |
| 26 | `webviewloadmprestime` | `bigint` |  | 容器加载小程序耗时 |
| 27 | `collectscene` | `string` |  |  |
| 28 | `notescene` | `string` |  |  |
| 29 | `presetdatareadytime` | `bigint` |  |  |
| 30 | `datareturntime` | `bigint` |  |  |
| 31 | `completetime` | `bigint` |  |  |
| 32 | `dt` | `string` |  |  |

---

## dwd_gift_ai_blogs_human_review_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_gift_ai_blogs_human_review_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.7K |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `dt` | `string` |  |  |

---

## dwd_gift_post_order_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_gift_post_order_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6464.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 6464.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id, 参见ods_db_trade_gift_present_record_nd.id |
| 2 | `postid` | `bigint` |  | 付费文章 |
| 3 | `sender` | `bigint` |  | 送礼用户id |
| 4 | `createtime` | `bigint` |  | 创建时间 |
| 5 | `gift_num` | `bigint` |  | 送礼个数 |
| 6 | `coin` | `bigint` |  | 乐乎币数量 |
| 7 | `return_gift_id` | `bigint` |  | 回礼礼物id |
| 8 | `blogid` | `bigint` |  | 文章对应的博客id |
| 9 | `blogname` | `string` |  | 文章对应的博客名称 |
| 10 | `publishdate` | `string` |  | 文章发布日期 |
| 11 | `content_type` | `string` |  | 文章内容类型 |
| 12 | `follow_blogid` | `bigint` |  | 关注博客id |
| 13 | `praise_postid` | `bigint` |  | 点赞博客id |
| 14 | `platform_type` | `string` |  | 平台类型 |
| 15 | `return_gift_type` | `string` |  | 回礼礼物类型 |
| 16 | `is_first_pay` | `string` |  | 是否文章首次付费 |
| 17 | `is_cp` | `string` |  | 是否为cp |
| 18 | `dt` | `string` |  |  |

---

## dwd_gift_post_unlock_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_gift_post_unlock_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 17439.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 17439.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `postid` | `bigint` |  | 回礼文章id |
| 3 | `giftid` | `bigint` |  | 礼物id |
| 4 | `return_gift_id` | `bigint` |  | 回礼id |
| 5 | `coin` | `double` |  | 消耗乐乎币 |
| 6 | `money` | `double` |  | 消耗金额, 单位人民币 |
| 7 | `unlock_time` | `string` |  | 解锁时间 |
| 8 | `unlock_unit_id` | `bigint` |  | 解锁单位id: 文章id 合集id 粮单id |
| 9 | `unlock_unit` | `string` |  | 解锁单位: 文章 合集 粮单 |
| 10 | `blogid` | `bigint` |  | 博客id |
| 11 | `unlock_method` | `string` |  | 解锁方式: 礼物 粮票 券包 广告 |
| 12 | `content_type` | `string` |  | 内容类型 |
| 13 | `return_gift_plantype` | `string` |  | 回礼类型 |
| 14 | `grain_tickets` | `bigint` |  | 粮票数量 |
| 15 | `is_settle` | `int` |  | 是否结算 0不结算 1结算 |
| 16 | `dt` | `string` |  |  |

---

## dwd_gift_return_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_gift_return_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 469.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 469.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 回礼ID |
| 2 | `plantype` | `bigint` |  | 回礼类型Id |
| 3 | `status` | `int` |  | 状态（-1：删除；0：未生效；1：生效；） |
| 4 | `auditstatus` | `int` |  | 审核状态（0：待审核；1：通过；2：不通过；） |
| 5 | `postid` | `bigint` |  | 文章id |
| 6 | `createdate` | `string` |  | 回礼创建日期 |
| 7 | `text_length` | `int` |  | 回礼文字数 |
| 8 | `image_count` | `int` |  | 回礼图片数 |
| 9 | `drainage_flag` | `int` |  | 引流嫌疑标志 |
| 10 | `giftname` | `string` |  | 回礼礼物名称 |
| 11 | `ispaynum` | `bigint` |  | 付费礼物数 |
| 12 | `nopaynum` | `bigint` |  | 免费礼物数 |
| 13 | `blogid` | `bigint` |  | 博客id |
| 14 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 15 | `blogname` | `string` |  | 博客名称 |
| 16 | `publishdate` | `string` |  | 文章发布日期 |
| 17 | `return_gift_plantype` | `string` |  | 回礼类型名称 |
| 18 | `agreeday` | `string` |  | 礼物功能开通日期 |
| 19 | `gift_ability_type` | `int` |  | 礼物功能类型 0:无,1:收费权限,2:激励计划 |
| 20 | `gift_ability_status` | `bigint` |  | 礼物功能状态 0:无,1:未开通,2:开通 |
| 21 | `blog_channel` | `int` |  | 博客来源 0:UGC, 1:PGC, 2=高粉，3=书城及小程序  |
| 22 | `sign_flag` | `int` |  | 单文签约标识 |
| 23 | `ugc_channel` | `int` |  | UGC来源标识 |
| 24 | `ispay_returngift` | `int` |  | 回礼付费类型 0:付费免费,1:仅付费,2:仅免费 |
| 25 | `fans_vip_status` | `int` |  | 粉丝会员的状态 |
| 26 | `fans_vip_agree_day` | `string` |  | 粉丝会员开通日期 |
| 27 | `fans_vip_close_day` | `string` |  | 粉丝会员关闭日期 |
| 28 | `is_authority` | `int` |  | 粉丝会员是否为官方 |
| 29 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 30 | `collectionid` | `bigint` |  | 文章所属合集Id |
| 31 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 32 | `accept_gift_flag` | `int` |  | 是否接受回礼 |
| 33 | `show_support_flag` | `int` |  | 是否支持 |
| 34 | `hide_return_gift_preview_img` | `int` |  | 是否隐藏回礼预览图片 |
| 35 | `emote_package_id` | `bigint` |  | 表情包id |
| 36 | `dress_id` | `bigint` |  | 装扮id |
| 37 | `preview_image_count` | `bigint` |  | 预览图片数 |
| 38 | `review_status` | `int` |  | 0/未标记；1/良好；-1/低质；-2/负面 |
| 39 | `mark_tags` | `array<string>` |  | 回礼打标标签列表 |
| 40 | `dt` | `string` |  |  |

---

## dwd_growth_actpwd_access_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_growth_actpwd_access_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 32.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 32.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `actpwd` | `string` |  | 口令 |
| 2 | `channel` | `string` |  | 口令渠道 |
| 3 | `deviceudid` | `string` |  | 设备id |
| 4 | `content_type` | `string` |  | 口令关联内容类型 |
| 5 | `itemid` | `bigint` |  | 口令关联内容id， 内容类型见content_type |
| 6 | `item_userid` | `bigint` |  | 口令关联内容用户id |
| 7 | `time` | `bigint` |  | 访问时间 |
| 8 | `userid` | `bigint` |  | 访问用户id |
| 9 | `customuuid` | `string` |  | 后台设备id |
| 10 | `is_new` | `int` |  | 是否新增设备 1是 0否 |
| 11 | `is_paid_subscribe` | `int` |  | 是否付费内容拉新： 0否 1是 2高粉拉新 |
| 12 | `link` | `string` |  | 口令关联内容链接 |
| 13 | `actpwd_type` | `bigint` |  | 口令类型，0-搜索口令，1-剪切口令, 2-搜索置顶口令 |
| 14 | `item_tag` | `string` |  | 口令内容标签 |
| 15 | `actpwd_start_time` | `bigint` |  | 口令有效期开始时间 |
| 16 | `actpwd_end_time` | `bigint` |  | 口令有效期结束时间 |
| 17 | `settlement_type` | `string` |  | 结算方式 |
| 18 | `is_membership` | `int` |  | 是否礼物会员口令 |
| 19 | `is_pve` | `int` |  | 是否pve跳转口令 1是 0否 |
| 20 | `dt` | `string` |  |  |

---

## dwd_growth_harmony_device_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_growth_harmony_device_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 25.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 25.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `userid` | `bigint` |  |  |
| 3 | `harmony` | `int` |  | 0:鸿蒙, 1:卓易通 |
| 4 | `dt` | `string` |  |  |

---

## dwd_growth_vertical_category_crowd_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_growth_vertical_category_crowd_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 923.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 923.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 文章IP归属 |
| 2 | `deviceudid` | `string` |  | 设备udid |
| 3 | `vertical_category` | `string` |  | 类目 |
| 4 | `valid_browse_pv` | `bigint` |  | 有效浏览pv |
| 5 | `dt` | `string` |  |  |

---

## dwd_home_top_resource_visit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_home_top_resource_visit_di` |
| **描述** | 首页吸顶资源访问明细 - 按天分区 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 11.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `deviceudid` | `string` |  | 设备ID |
| 3 | `eventid` | `string` |  | 事件ID a1-37=曝光 a1-36=点击 |
| 4 | `action_type` | `string` |  | 行为类型 expose=曝光 click=点击 |
| 5 | `resource_type` | `string` |  | 资源类型 reward_center=权益中心 pve_boyfriends=破次元 |
| 6 | `url` | `string` |  | 埋点上报的URL |
| 7 | `occurtime` | `bigint` |  | 事件发生时间(毫秒) |
| 8 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_issue_with_status_change_for_lofter

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_issue_with_status_change_for_lofter` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 5.6M |
| **是否分区表** | 否 |

### 字段详情

共 83 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `decimal(38,0)` |  |  |
| 2 | `created_at` | `timestamp` |  |  |
| 3 | `updated_at` | `timestamp` |  |  |
| 4 | `title` | `string` |  |  |
| 5 | `jira_id` | `string` |  |  |
| 6 | `jira_key` | `string` |  |  |
| 7 | `create_time` | `timestamp` |  |  |
| 8 | `resolve_time` | `timestamp` |  |  |
| 9 | `close_time` | `timestamp` |  |  |
| 10 | `due_date` | `timestamp` |  |  |
| 11 | `plan_submit_test_time` | `string` |  |  |
| 12 | `plan_release_time` | `string` |  |  |
| 13 | `plan_start_time` | `string` |  |  |
| 14 | `creator` | `string` |  |  |
| 15 | `assignee` | `string` |  |  |
| 16 | `reporter` | `string` |  |  |
| 17 | `verifier` | `string` |  |  |
| 18 | `priority` | `string` |  |  |
| 19 | `severity` | `string` |  |  |
| 20 | `status` | `string` |  |  |
| 21 | `online_bug` | `int` |  |  |
| 22 | `original_estimate` | `int` |  |  |
| 23 | `remaining_estimate` | `int` |  |  |
| 24 | `time_spent` | `int` |  |  |
| 25 | `issue_type` | `string` |  |  |
| 26 | `parent` | `string` |  |  |
| 27 | `sprint` | `string` |  |  |
| 28 | `project_key` | `string` |  |  |
| 29 | `team` | `string` |  |  |
| 30 | `component` | `string` |  |  |
| 31 | `requirement_source` | `string` |  |  |
| 32 | `finished_estimate` | `int` |  |  |
| 33 | `finished_time_spent` | `int` |  |  |
| 34 | `man_month_estimate` | `string` |  |  |
| 35 | `labels` | `string` |  |  |
| 36 | `update_time` | `timestamp` |  |  |
| 37 | `ticket_link` | `string` |  |  |
| 38 | `advise_link` | `string` |  |  |
| 39 | `incident_link` | `string` |  |  |
| 40 | `project_link` | `string` |  |  |
| 41 | `module_id` | `int` |  |  |
| 42 | `module_manual_updated` | `int` |  |  |
| 43 | `schedule_plan_time` | `timestamp` |  |  |
| 44 | `project_category` | `string` |  |  |
| 45 | `watchers` | `string` |  |  |
| 46 | `source` | `string` |  |  |
| 47 | `bug_reason` | `string` |  |  |
| 48 | `bug_type` | `string` |  |  |
| 49 | `deleted` | `int` |  |  |
| 50 | `business_target` | `string` |  |  |
| 51 | `demand_target` | `string` |  |  |
| 52 | `review` | `string` |  |  |
| 53 | `check_evaluate` | `string` |  |  |
| 54 | `plan_check_time` | `string` |  |  |
| 55 | `updator` | `string` |  |  |
| 56 | `has_risk` | `int` |  |  |
| 57 | `feedback_source` | `string` |  |  |
| 58 | `actual_working_hours` | `string` |  |  |
| 59 | `actual_working_hours_sum` | `string` |  |  |
| 60 | `recheck_link` | `string` |  |  |
| 61 | `man_day_estimate` | `string` |  |  |
| 62 | `delay_rate` | `string` |  |  |
| 63 | `project_detail_link` | `string` |  |  |
| 64 | `label` | `string` |  |  |
| 65 | `value_close_loop` | `string` |  |  |
| 66 | `fix_result` | `string` |  |  |
| 67 | `estimate_cost` | `string` |  |  |
| 68 | `issue_key` | `string` |  |  |
| 69 | `story_req_time` | `timestamp` |  |  |
| 70 | `story_vision_time` | `timestamp` |  |  |
| 71 | `story_clarified_time` | `timestamp` |  |  |
| 72 | `story_rd_time` | `timestamp` |  |  |
| 73 | `story_ready_time` | `timestamp` |  |  |
| 74 | `t_close` | `timestamp` |  |  |
| 75 | `t_cancel` | `timestamp` |  |  |
| 76 | `task_scheduled_time` | `timestamp` |  |  |
| 77 | `t_resolve` | `timestamp` |  |  |
| 78 | `bug_response_time` | `timestamp` |  |  |
| 79 | `bug_close_time` | `timestamp` |  |  |
| 80 | `bug_reopen_time` | `timestamp` |  |  |
| 81 | `team_id` | `string` |  |  |
| 82 | `component_id` | `string` |  |  |
| 83 | `custom_ext` | `string` |  |  |

---

## dwd_issue_with_status_change_for_vc

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_issue_with_status_change_for_vc` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 873.3K |
| **是否分区表** | 否 |

### 字段详情

共 83 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `decimal(38,0)` |  |  |
| 2 | `created_at` | `timestamp` |  |  |
| 3 | `updated_at` | `timestamp` |  |  |
| 4 | `title` | `string` |  |  |
| 5 | `jira_id` | `string` |  |  |
| 6 | `jira_key` | `string` |  |  |
| 7 | `create_time` | `timestamp` |  |  |
| 8 | `resolve_time` | `timestamp` |  |  |
| 9 | `close_time` | `timestamp` |  |  |
| 10 | `due_date` | `timestamp` |  |  |
| 11 | `plan_submit_test_time` | `string` |  |  |
| 12 | `plan_release_time` | `string` |  |  |
| 13 | `plan_start_time` | `string` |  |  |
| 14 | `creator` | `string` |  |  |
| 15 | `assignee` | `string` |  |  |
| 16 | `reporter` | `string` |  |  |
| 17 | `verifier` | `string` |  |  |
| 18 | `priority` | `string` |  |  |
| 19 | `severity` | `string` |  |  |
| 20 | `status` | `string` |  |  |
| 21 | `online_bug` | `int` |  |  |
| 22 | `original_estimate` | `int` |  |  |
| 23 | `remaining_estimate` | `int` |  |  |
| 24 | `time_spent` | `int` |  |  |
| 25 | `issue_type` | `string` |  |  |
| 26 | `parent` | `string` |  |  |
| 27 | `sprint` | `string` |  |  |
| 28 | `project_key` | `string` |  |  |
| 29 | `team` | `string` |  |  |
| 30 | `component` | `string` |  |  |
| 31 | `requirement_source` | `string` |  |  |
| 32 | `finished_estimate` | `int` |  |  |
| 33 | `finished_time_spent` | `int` |  |  |
| 34 | `man_month_estimate` | `string` |  |  |
| 35 | `labels` | `string` |  |  |
| 36 | `update_time` | `timestamp` |  |  |
| 37 | `ticket_link` | `string` |  |  |
| 38 | `advise_link` | `string` |  |  |
| 39 | `incident_link` | `string` |  |  |
| 40 | `project_link` | `string` |  |  |
| 41 | `module_id` | `int` |  |  |
| 42 | `module_manual_updated` | `int` |  |  |
| 43 | `schedule_plan_time` | `timestamp` |  |  |
| 44 | `project_category` | `string` |  |  |
| 45 | `watchers` | `string` |  |  |
| 46 | `source` | `string` |  |  |
| 47 | `bug_reason` | `string` |  |  |
| 48 | `bug_type` | `string` |  |  |
| 49 | `deleted` | `int` |  |  |
| 50 | `business_target` | `string` |  |  |
| 51 | `demand_target` | `string` |  |  |
| 52 | `review` | `string` |  |  |
| 53 | `check_evaluate` | `string` |  |  |
| 54 | `plan_check_time` | `string` |  |  |
| 55 | `updator` | `string` |  |  |
| 56 | `has_risk` | `int` |  |  |
| 57 | `feedback_source` | `string` |  |  |
| 58 | `actual_working_hours` | `string` |  |  |
| 59 | `actual_working_hours_sum` | `string` |  |  |
| 60 | `recheck_link` | `string` |  |  |
| 61 | `man_day_estimate` | `string` |  |  |
| 62 | `delay_rate` | `string` |  |  |
| 63 | `project_detail_link` | `string` |  |  |
| 64 | `label` | `string` |  |  |
| 65 | `value_close_loop` | `string` |  |  |
| 66 | `fix_result` | `string` |  |  |
| 67 | `estimate_cost` | `string` |  |  |
| 68 | `issue_key` | `string` |  |  |
| 69 | `story_req_time` | `timestamp` |  |  |
| 70 | `story_vision_time` | `timestamp` |  |  |
| 71 | `story_clarified_time` | `timestamp` |  |  |
| 72 | `story_rd_time` | `timestamp` |  |  |
| 73 | `story_ready_time` | `timestamp` |  |  |
| 74 | `t_close` | `timestamp` |  |  |
| 75 | `t_cancel` | `timestamp` |  |  |
| 76 | `task_scheduled_time` | `timestamp` |  |  |
| 77 | `t_resolve` | `timestamp` |  |  |
| 78 | `bug_response_time` | `timestamp` |  |  |
| 79 | `bug_close_time` | `timestamp` |  |  |
| 80 | `bug_reopen_time` | `timestamp` |  |  |
| 81 | `team_id` | `string` |  |  |
| 82 | `component_id` | `string` |  |  |
| 83 | `custom_ext` | `string` |  |  |

---

## dwd_liaoliao_mda_base

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_liaoliao_mda_base` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 11.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备UID |
| 2 | `eventid` | `string` |  | 事件ID |
| 3 | `dt` | `string` |  |  |

---

## dwd_lucky_boy_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_lucky_boy_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 382.8M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `luckyboyid` | `bigint` |  | 抓人Id |
| 2 | `postid` | `bigint` |  | 文章ID |
| 3 | `join_blogid` | `bigint` |  | 参与人BlogId |
| 4 | `joinaction` | `bigint` |  | 参与动作 |
| 5 | `sequence` | `bigint` |  | 序列 仅仅在满足条件时更新值 |
| 6 | `notifyonpublish` | `int` |  | 是否通知 0-不通知； 1-通知 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `publish_blogid` | `bigint` |  | 发布人BlogId |
| 9 | `publish_joinaction` | `string` |  | 发布参与动作：1-喜欢；2-评论；3-赠礼；4-推荐。多个行为英文逗号分隔 |
| 10 | `needfollow` | `int` |  | 是否需要关注 |
| 11 | `publishtime` | `bigint` |  | 公布时间 |
| 12 | `publishtype` | `int` |  | 公布方式：0-手动公布；1-自动公布 |
| 13 | `status` | `int` |  | 抓人状态：0-初始状态；1-待公布；2-已经公布；-1-抓人失败 |
| 14 | `count` | `bigint` |  | 抓人人数 |
| 15 | `ispublished` | `int` |  | 是否发布或草稿 |
| 16 | `resettimes` | `bigint` |  | 剩余重设抓人结果次数 |
| 17 | `type` | `int` |  | 抓人配置类型：0-用户配置；1-官方配置 |
| 18 | `dt` | `string` |  |  |

---

## dwd_lucky_boy_result_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_lucky_boy_result_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 15.1M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `luckyboyid` | `bigint` |  | 抓人Id |
| 2 | `join_blogid` | `bigint` |  | 参与人BlogId |
| 3 | `sequence` | `bigint` |  | 序列 仅仅在满足条件时更新值 |
| 4 | `lucky_status` | `int` |  | 抓人状态：0-中奖了；-1-被作者黑了 |
| 5 | `createtime` | `bigint` |  | 创建时间 |
| 6 | `publish_blogid` | `bigint` |  | 发布人BlogId |
| 7 | `postid` | `bigint` |  | 文章ID |
| 8 | `publish_joinaction` | `string` |  | 发布参与动作：1-喜欢；2-评论；3-赠礼；4-推荐。多个行为英文逗号分隔 |
| 9 | `needfollow` | `int` |  | 是否需要关注 |
| 10 | `publishtime` | `bigint` |  | 公布时间 |
| 11 | `publishtype` | `int` |  | 公布方式：0-手动公布；1-自动公布 |
| 12 | `status` | `int` |  | 抓人状态：0-初始状态；1-待公布；2-已经公布；-1-抓人失败 |
| 13 | `count` | `bigint` |  | 抓人人数 |
| 14 | `ispublished` | `int` |  | 是否发布或草稿 |
| 15 | `resettimes` | `bigint` |  | 剩余重设抓人结果次数 |
| 16 | `type` | `int` |  | 抓人配置类型：0-用户配置；1-官方配置 |
| 17 | `dt` | `string` |  |  |

---

## dwd_membership_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_membership_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 24.1M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `order_id` | `bigint` |  | 订单id, 对应表Trade_BlogVipOrder |
| 2 | `userid` | `bigint` |  | 支付用户id |
| 3 | `blogid` | `bigint` |  | 订阅博客id |
| 4 | `postid` | `bigint` |  | 订单文章id |
| 5 | `amount` | `double` |  | 订单金额 |
| 6 | `pay_time` | `bigint` |  | 支付时间 |
| 7 | `deviceudid` | `string` |  | 搜索口令哈勃设备id |
| 8 | `customuuid` | `string` |  | 搜索口令后台设备id |
| 9 | `actpwd` | `string` |  | 搜索口令 |
| 10 | `actpwd_channel` | `string` |  | 搜索口令渠道 |
| 11 | `actpwd_search_time` | `string` |  | 搜索口令时间 |
| 12 | `actpwd_search_list` | `array<string>` |  | 交易前当天搜索口令 |
| 13 | `order_type` | `int` |  | 订单类型 1博客订阅 2高级粉丝订阅 3书城订阅 4书城小程序 |
| 14 | `is_internal_attribute_order` | `int` |  | 是否系统订单 0 用户主动下单  1（包含优惠券、包月自动下单以及其他系统自动下单）2 米良免结算订单 （外部投放 星图渠道) |
| 15 | `vip_days` | `bigint` |  | 购买会员天数 |
| 16 | `platform` | `int` |  | 1安卓, 2苹果 |
| 17 | `dt` | `string` |  |  |

---

## dwd_membership_vip_post_browse_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_membership_vip_post_browse_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 149.4M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 会员文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `userid` | `bigint` |  | 浏览用户 |
| 4 | `start_time` | `bigint` |  | 当天最早浏览时间 |
| 5 | `end_time` | `bigint` |  | 当天最晚浏览时间 |
| 6 | `duration_seconds` | `double` |  | 文章浏览总时长 单位秒 |
| 7 | `dt` | `string` |  |  |

---

## dwd_miniprogram_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_miniprogram_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.4M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `miniid` | `int` |  | 所属的小程序标识 |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `platform` | `string` |  | 支付平台: Android iOS |
| 4 | `postid` | `bigint` |  | 付时关联的文章id |
| 5 | `pay_time` | `bigint` |  | 支付时间 |
| 6 | `amount` | `double` |  | 支付金额 |
| 7 | `dt` | `string` |  |  |

---

## dwd_paid_gift_postid_info_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_paid_gift_postid_info_nd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 11.8G |
| **是否分区表** | 否 |

> ⚠️ **【性能提示】** 该表大小为 11.8G，属于大表但非分区表。查询该表时请注意性能，建议增加合理的 WHERE 条件以减少扫描的数据量，避免全表扫描。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 开通付费用户id |
| 2 | `accept_gift_flag` | `int` |  | 接受礼物flag |
| 3 | `agree_day` | `string` |  | 同意开通礼物的时间 |
| 4 | `postid` | `bigint` |  | 该用户下的文章 |
| 5 | `blogname` | `string` |  | 博客名称 |
| 6 | `blognickname` | `string` |  | 博客昵称 |
| 7 | `title` | `string` |  | 文章标题 |
| 8 | `tags` | `array<string>` |  | 文章标签 |
| 9 | `url` | `string` |  | 文章url |
| 10 | `contenttype` | `string` |  | 文章类型 |
| 11 | `publishdate` | `string` |  | 文章发布时间 |
| 12 | `return_gift_ids` | `string` |  | 文章回礼礼物ids |
| 13 | `cp_type` | `string` |  | 是否为cp |
| 14 | `platform_type` | `string` |  | 平台类型 |
| 15 | `is_pay_return_gift` | `string` |  | 回礼类型标识 |

---

## dwd_paid_post_detail_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_paid_post_detail_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 3160.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3160.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 付费文章 |
| 2 | `sender` | `bigint` |  | 送礼用户id |
| 3 | `createtime` | `bigint` |  | 创建时间 |
| 4 | `gift_num` | `bigint` |  | 送礼个数 |
| 5 | `coin` | `bigint` |  | 乐乎币数量 |
| 6 | `return_gift_id` | `bigint` |  | 回礼礼物id |
| 7 | `blogid` | `bigint` |  | 文章对应的博客id |
| 8 | `blogname` | `string` |  | 文章对应的博客名称 |
| 9 | `publishdate` | `string` |  | 文章发布日期 |
| 10 | `content_type` | `string` |  | 文章内容类型 |
| 11 | `follow_blogid` | `bigint` |  | 关注博客id |
| 12 | `praise_postid` | `bigint` |  | 点赞博客id |
| 13 | `platform_type` | `string` |  | 平台类型 |
| 14 | `return_gift_type` | `string` |  | 回礼礼物类型 |
| 15 | `is_first_pay` | `string` |  | 是否文章首次付费 |
| 16 | `is_cp` | `string` |  | 是否为cp |
| 17 | `dt` | `string` |  |  |

---

## dwd_paid_subscribe_device_cpa_deduplicate_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_paid_subscribe_device_cpa_deduplicate_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 411.7K |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `actpwd` | `string` |  | 口令 |
| 3 | `actpwd_channel` | `string` |  | 口令渠道 |
| 4 | `dt` | `string` |  | 日期 |

---

## dwd_paid_subscribe_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_paid_subscribe_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 999.6M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `order_id` | `bigint` |  | 订单id, 对应表Trade_BlogVipOrder |
| 2 | `userid` | `bigint` |  | 支付用户id |
| 3 | `blogid` | `bigint` |  | 订阅博客id |
| 4 | `postid` | `bigint` |  | 订单文章id |
| 5 | `amount` | `double` |  | 订单金额 |
| 6 | `pay_time` | `bigint` |  | 支付时间 |
| 7 | `deviceudid` | `string` |  | 搜索口令哈勃设备id |
| 8 | `customuuid` | `string` |  | 搜索口令后台设备id |
| 9 | `actpwd` | `string` |  | 搜索口令 |
| 10 | `actpwd_channel` | `string` |  | 搜索口令渠道 |
| 11 | `actpwd_search_time` | `string` |  | 搜索口令时间 |
| 12 | `actpwd_search_list` | `array<string>` |  | 交易前当天搜索口令 |
| 13 | `order_type` | `int` |  | 订单类型 1博客订阅 2高级粉丝订阅 3书城订阅 4书城小程序 |
| 14 | `is_official` | `int` |  | 是否官方推广账号订单 |
| 15 | `is_internal_attribute_order` | `int` |  | 是否系统订单 0 用户主动下单  1（包含优惠券、包月自动下单以及其他系统自动下单）2 米良免结算订单 （外部投放 星图渠道) |
| 16 | `is_vip_fans` | `int` |  | 是否高级粉丝口令归因订单 |
| 17 | `platform` | `int` |  | 1安卓, 2苹果 |
| 18 | `dt` | `string` |  |  |

---

## dwd_paid_subscribe_silent_user_activate_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_paid_subscribe_silent_user_activate_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 45.4M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `actpwd` | `string` |  | 口令 |
| 2 | `actpwd_channel` | `string` |  | 口令渠道 |
| 3 | `userid` | `bigint` |  | 激活休眠用户id |
| 4 | `dt` | `string` |  |  |

---

## dwd_par_creator_first_publish_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_par_creator_first_publish_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 391.8M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 用户博客 |
| 2 | `blogname` | `string` |  | 博客名称 |
| 3 | `postid` | `bigint` |  | 文章ID |
| 4 | `contenttype` | `string` |  | 文章类型(1文字，2图片，3音乐，4视频，5问答，6长文章) |
| 5 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步) |
| 6 | `isforbidden` | `boolean` |  | 是否被封禁 |
| 7 | `publishdate` | `string` |  | 文章发布日期 |
| 8 | `publishtime` | `bigint` |  | 文章发布时间 |
| 9 | `usercreatedate` | `string` |  | 用户注册时间 |
| 10 | `url` | `string` |  | 文章链接 |
| 11 | `tags` | `array<string>` |  | 文章所带标签 |
| 12 | `dt` | `string` |  |  |

---

## dwd_par_device_all_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_par_device_all_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 5783.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5783.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备Udid |
| 2 | `deviceos` | `string` |  | 操作系统 |
| 3 | `devicemodel` | `string` |  | 设备Model |
| 4 | `firstaccesstime` | `bigint` |  | 初次访问时间 |
| 5 | `userid` | `bigint` |  | 用户ID |
| 6 | `appchannel` | `string` |  | app渠道 |
| 7 | `appversion` | `string` |  | app版本 |
| 8 | `deviceid` | `bigint` |  | deviceUdid对应的唯一Id |
| 9 | `dt` | `string` |  | 日期 |

---

## dwd_post_audio_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_audio_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.8G |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceos` | `string` |  | 设备系统 |
| 2 | `deviceudid` | `string` |  | 设备Id |
| 3 | `userid` | `bigint` |  | 用户Id |
| 4 | `collectionid` | `bigint` |  | 合集Id |
| 5 | `postid` | `bigint` |  | 文章Id |
| 6 | `maxprogress` | `double` |  | 播放进度 |
| 7 | `playedtime` | `double` |  | 播放时长 |
| 8 | `is_valid` | `int` |  | 是否有效 |
| 9 | `is_finish` | `int` |  | 是否完成 |
| 10 | `dt` | `string` |  |  |

---

## dwd_post_audit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_audit_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 13.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 13.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 用户博客 |
| 3 | `version` | `int` |  | 审核版本 |
| 4 | `uuid` | `string` |  | 抄送uuid |
| 5 | `level` | `int` |  | 优先级 |
| 6 | `post_type` | `int` |  | 文章类型 |
| 7 | `machine_operator` | `string` |  | 机审操作者 |
| 8 | `person_operator` | `string` |  | 人审操作者 |
| 9 | `machine_status` | `int` |  | 机审状态 |
| 10 | `person_status` | `int` |  | 人审状态 |
| 11 | `machine_forbidtype` | `bigint` |  | 机审屏蔽类型 |
| 12 | `machine_hint` | `string` |  | 机审屏蔽理由 |
| 13 | `person_forbidtype` | `bigint` |  | 人审屏蔽类型 |
| 14 | `person_hint` | `string` |  | 人审屏蔽理由 |
| 15 | `copy_time` | `bigint` |  | 文章抄送时间 |
| 16 | `machine_audit_time` | `bigint` |  | 文章机审时间 |
| 17 | `person_audit_time` | `bigint` |  | 文章人审时间 |
| 18 | `dt` | `string` |  |  |

---

## dwd_post_browse_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_browse_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 26295.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 26295.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 浏览用户id |
| 2 | `action_type` | `string` |  | 浏览类型: page_view page_duration |
| 3 | `deviceudid` | `string` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `postid` | `bigint` |  |  |
| 6 | `post_userid` | `bigint` |  | 文章创作者用户id |
| 7 | `post_content_type` | `string` |  | 文章类型 |
| 8 | `post_publish_date` | `string` |  | 文章发布日期 |
| 9 | `post_tags` | `array<string>` |  | 文章标签 |
| 10 | `post_ips` | `array<string>` |  | 文章ip归属 |
| 11 | `post_domains` | `array<string>` |  | 文章领域归属 |
| 12 | `scene` | `string` |  | 场景 |
| 13 | `module` | `string` |  | 模块 |
| 14 | `source_module` | `string` |  | 来源 |
| 15 | `occurtime` | `bigint` |  |  |
| 16 | `duration` | `bigint` |  | 停留时长, 单位毫秒 |
| 17 | `is_real` | `int` |  | 是否有效浏览 口径：视频浏览时长大于5秒 其他大于3秒 |
| 18 | `is_fans` | `int` |  | 用户是否博主粉丝 |
| 19 | `is_from_rec` | `int` |  | 是否来源推荐场景 |
| 20 | `is_support_induced` | `int` |  | 是否流量扶持引导浏览 1是 0否 |
| 21 | `is_book_store` | `int` |  | 浏览文章是否来源于书城 1是，0否 |
| 22 | `is_video_finish` | `int` |  | 视频是否完播, 口径： 播放进度大于90% |
| 23 | `is_new_user` | `int` |  | 是否当天注册新用户 |
| 24 | `source1_scene` | `string` |  | 前一步场景 |
| 25 | `source2_scene` | `string` |  | 前两步场景 |
| 26 | `source2_module` | `string` |  | 前两步模块 |
| 27 | `is_collection` | `int` |  | 是否合集 |
| 28 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 29 | `appversion` | `string` |  | app版本信息,230814号加入 |
| 30 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 31 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 32 | `dt` | `string` |  |  |

---

## dwd_post_collection_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_collection_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 11342.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11342.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 日志ID |
| 2 | `userid` | `bigint` |  | 操作用户ID |
| 3 | `action_type` | `string` |  | 操作类型：praise，reproduce,recommend,subscribe,expose,browse,share,comment |
| 4 | `post_content_type` | `string` |  | 日志内容类型 |
| 5 | `deviceudid` | `string` |  | 客户端设备ID |
| 6 | `is_real` | `int` |  | 是否真实浏览 |
| 7 | `scene` | `string` |  | 场景 |
| 8 | `source1_scene` | `string` |  | 前一步场景 |
| 9 | `source2_scene` | `string` |  | 前二步场景 |
| 10 | `module` | `string` |  | 模块 |
| 11 | `source1_module` | `string` |  | 前一步模块 |
| 12 | `source2_module` | `string` |  | 前二步模块 |
| 13 | `commentid` | `bigint` |  | 评论ID |
| 14 | `costtime` | `bigint` |  | 事件耗时 |
| 15 | `occurtime` | `bigint` |  | 事件发生时间 |
| 16 | `collectionid` | `bigint` |  | 文章对应的合集ID |
| 17 | `is_collection_new_user` | `int` |  | 是否为合集拉新新用户 |
| 18 | `post_userid` | `bigint` |  | 文章所属的博客ID |
| 19 | `is_collection_return_user` | `int` |  | 是否为合集回流用户 |
| 20 | `dt` | `string` |  |  |

---

## dwd_post_expose_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_expose_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 18186.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 18186.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 曝光用户id |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `deviceos` | `string` |  |  |
| 4 | `postid` | `bigint` |  |  |
| 5 | `post_userid` | `bigint` |  | 文章创作者用户id |
| 6 | `post_content_type` | `string` |  | 文章类型 |
| 7 | `is_fans` | `int` |  | 用户是否博主粉丝 |
| 8 | `scene` | `string` |  | 场景 |
| 9 | `module` | `string` |  | 模块 |
| 10 | `is_rec` | `int` |  | 是否推荐场景 |
| 11 | `occurtime` | `bigint` |  |  |
| 12 | `post_tags` | `array<string>` |  | 文章标签 |
| 13 | `reaction` | `string` |  | 曝光后续反馈操作： click dislike |
| 14 | `is_support` | `int` |  | 是否扶持流量 |
| 15 | `is_new_user` | `int` |  | 是否当天注册新用户 |
| 16 | `source1_scene` | `string` |  | 前一步场景 |
| 17 | `source2_scene` | `string` |  | 前两步场景 |
| 18 | `source1_module` | `string` |  | 前一步模块 |
| 19 | `source2_module` | `string` |  | 前两步模块 |
| 20 | `is_collection` | `int` |  | 是否合集 |
| 21 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 22 | `appversion` | `string` |  | app版本信息,230814号加入 |
| 23 | `detail_scene_level_1` | `string` |  | 当前场景的详细一级列 |
| 24 | `detail_scene_level_2` | `string` |  | 当前场景的详细一级列 |
| 25 | `ips` | `array<string>` |  | 文章ip： 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 26 | `dt` | `string` |  |  |

---

## dwd_post_group_post_list_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_group_post_list_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 10.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 10.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  |  |
| 2 | `dt` | `string` |  |  |
| 3 | `job_id` | `string` |  |  |

---

## dwd_post_hot_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_hot_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 2691.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2691.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 34 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 日志ID |
| 2 | `userid` | `bigint` |  | 热度操作用户ID |
| 3 | `optype` | `string` |  | 热度操作类型：praise,recommend,subscribe,reproduce |
| 4 | `optime` | `bigint` |  | 热度操作时间 |
| 5 | `ip` | `string` |  | 热度操作IP |
| 6 | `blogid` | `bigint` |  | 博客ID |
| 7 | `is_blog_authenticated` | `int` |  | 是否认证博客(达人) 1认证 0 非认证 |
| 8 | `post_userid` | `bigint` |  | 日志作者用户ID |
| 9 | `post_tags` | `array<string>` |  | 日志标签列表 |
| 10 | `post_domains` | `array<bigint>` |  | 日志领域列表 |
| 11 | `post_publish_date` | `string` |  | 日志发布日期 |
| 12 | `post_content_type` | `string` |  | 日志内容类型 |
| 13 | `platform` | `string` |  | 平台类型 |
| 14 | `deviceudid` | `string` |  | 客户端设备ID |
| 15 | `eventid` | `string` |  | 事件ID |
| 16 | `scene` | `string` |  | 场景 |
| 17 | `source1_scene` | `string` |  | 前一步场景 |
| 18 | `source2_scene` | `string` |  | 前二步场景 |
| 19 | `module` | `string` |  | 模块 |
| 20 | `source1_module` | `string` |  | 前一步模块 |
| 21 | `source2_module` | `string` |  | 前二步模块 |
| 22 | `params_add_item_id` | `string` |  | 参数add_itemId |
| 23 | `params_add_item_type` | `string` |  | 参数add_itemType |
| 24 | `occurtime` | `bigint` |  | 事件发生事件 |
| 25 | `kafkatime` | `bigint` |  | 事件上报kafka时间 |
| 26 | `rec_alg` | `string` |  | 推荐算法 |
| 27 | `is_new_user` | `int` |  | 热度操作是否新用户 |
| 28 | `is_collection` | `int` |  | 是否合集 |
| 29 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 30 | `is_fans` | `int` |  | 是否博主粉丝 |
| 31 | `post_ips` | `array<string>` |  | 日志所属ip列表 |
| 32 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 33 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 34 | `dt` | `string` |  |  |

---

## dwd_post_length_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_length_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 39.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 39.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `words_count` | `bigint` |  | 文章内容字数 |
| 3 | `photo_count` | `bigint` |  | 文章包含图片数 |
| 4 | `contain_link` | `int` |  | 文章包含链接 |
| 5 | `contain_text` | `int` |  | 文章包含富文本 |
| 6 | `contain_image` | `int` |  | 文章包含图片标签 |
| 7 | `dt` | `string` |  | 统计日期 |

---

## dwd_post_publish_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_publish_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 12.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 12.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `userid` | `bigint` |  | 创作者用户id |
| 3 | `blog_name` | `string` |  | 博客名称 |
| 4 | `post_title` | `string` |  | 文章标题 |
| 5 | `post_tags` | `array<string>` |  | 文章标签 |
| 6 | `post_ips` | `array<string>` |  | 文章ip |
| 7 | `post_domains` | `array<string>` |  | 文章领域 |
| 8 | `post_content_type` | `string` |  | 文章类型： 图片 文字 视频 |
| 9 | `post_publish_date` | `string` |  | 发文日期： 业务上发文日期（文章展示发文日期） |
| 10 | `post_publish_time` | `string` |  | 发文时间 |
| 11 | `blog_nickname` | `string` |  | 博客昵称 |
| 12 | `is_user_first_post` | `int` |  | 是否创作者首发文 |
| 13 | `platform` | `string` |  | 端类型 |
| 14 | `is_pay_gift` | `int` |  | 是否设置付费回礼 |
| 15 | `dt` | `string` |  |  |

---

## dwd_post_response_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_response_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 89.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 89.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 日志ID |
| 2 | `userid` | `bigint` |  | 发布评论用户ID |
| 3 | `commentid` | `bigint` |  | 评论ID |
| 4 | `replyl1_commentid` | `bigint` |  | 回复一级评论ID |
| 5 | `replyl2_commentid` | `bigint` |  | 回复二级评论ID |
| 6 | `content` | `string` |  | 评论内容 |
| 7 | `optime` | `bigint` |  | 评论操作时间 |
| 8 | `ip` | `string` |  | 评论发布IP |
| 9 | `blogid` | `bigint` |  | 文章所属的博客ID |
| 10 | `is_blog_authenticated` | `int` |  | 是否认证博客(达人) 1认证 0 非认证 |
| 11 | `post_userid` | `bigint` |  | 日志作者用户ID |
| 12 | `post_tags` | `array<string>` |  | 日志标签列表 |
| 13 | `post_domains` | `array<bigint>` |  | 日志领域列表 |
| 14 | `post_publish_date` | `string` |  | 日志发布日期 |
| 15 | `post_content_type` | `string` |  | 日志内容类型 |
| 16 | `platform` | `string` |  | 端类型 |
| 17 | `deviceudid` | `string` |  | 客户端设备ID |
| 18 | `eventid` | `string` |  | 事件ID |
| 19 | `scene` | `string` |  | 场景 |
| 20 | `source1_scene` | `string` |  | 前一步场景 |
| 21 | `source2_scene` | `string` |  | 前二步场景 |
| 22 | `module` | `string` |  | 模块 |
| 23 | `source1_module` | `string` |  | 前一步模块 |
| 24 | `source2_module` | `string` |  | 前二步模块 |
| 25 | `occurtime` | `bigint` |  | 事件发生事件 |
| 26 | `kafkatime` | `bigint` |  | 事件上报kafka时间 |
| 27 | `rec_alg` | `string` |  | 推荐算法 |
| 28 | `is_new_user` | `int` |  | 评论者是否为新用户 |
| 29 | `is_collection` | `int` |  | 是否合集 |
| 30 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 31 | `is_valid` | `int` |  | 是否有效评论 |
| 32 | `is_fans` | `int` |  | 是否博主粉丝 |
| 33 | `is_underscore` | `int` |  | 是否划线评 |
| 34 | `is_underscore_reply` | `int` |  | 是否划线评回复 1是 0否 |
| 35 | `post_ips` | `array<string>` |  | 日志所属ip列表 |
| 36 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 37 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 38 | `is_circle_comment` | `int` |  | 是否圈评 |
| 39 | `is_circle_comment_reply` | `int` |  | 是否圈评回复 |
| 40 | `dt` | `string` |  |  |

---

## dwd_post_share_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_share_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 17.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 17.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 33 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 日志ID |
| 2 | `userid` | `bigint` |  | 操作用户ID |
| 3 | `blogid` | `bigint` |  | 文章所属的博客ID |
| 4 | `is_blog_authenticated` | `int` |  | 是否认证博客(达人) 1认证 0 非认证 |
| 5 | `post_userid` | `bigint` |  | 日志作者用户ID |
| 6 | `post_tags` | `array<string>` |  | 日志标签列表 |
| 7 | `post_domains` | `array<bigint>` |  | 日志领域列表 |
| 8 | `post_publish_date` | `string` |  | 日志发布日期 |
| 9 | `post_content_type` | `string` |  | 日志内容类型 |
| 10 | `platform` | `string` |  | 端类型 |
| 11 | `deviceudid` | `string` |  | 客户端设备ID |
| 12 | `eventid` | `string` |  | 事件ID |
| 13 | `scene` | `string` |  | 场景 |
| 14 | `source1_scene` | `string` |  | 前一步场景 |
| 15 | `source2_scene` | `string` |  | 前二步场景 |
| 16 | `module` | `string` |  | 模块 |
| 17 | `source1_module` | `string` |  | 前一步模块 |
| 18 | `source2_module` | `string` |  | 前二步模块 |
| 19 | `occurtime` | `bigint` |  | 事件发生事件 |
| 20 | `kafkatime` | `bigint` |  | 事件上报kafka时间 |
| 21 | `rec_alg` | `string` |  | 推荐算法 |
| 22 | `ip` | `string` |  | 操作IP |
| 23 | `is_new_user` | `int` |  | 操作者是否为新用户 |
| 24 | `is_collection` | `int` |  | 是否合集 |
| 25 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 26 | `appversion` | `string` |  | app版本信息,230814号加入 |
| 27 | `post_ips` | `array<string>` |  | 日志所属ip列表 |
| 28 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 29 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 30 | `share_method` | `string` |  | 分享方式 |
| 31 | `share_channel` | `string` |  | 分享渠道: 新浪微博 微信好友 微信朋友圈 QQ好友 QQ空间 小红书等 |
| 32 | `is_fans` | `int` |  | 用户是否博主粉丝 |
| 33 | `dt` | `string` |  |  |

---

## dwd_post_status

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_status` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 用户博客 |
| 3 | `ispublished` | `int` |  | 是否发布或草稿 |
| 4 | `allowview` | `int` |  | 文章能见状态(0:公开可见,50:待审核,100:仅自己可见) |
| 5 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步, 32: 用户删除) |
| 6 | `post_status` | `string` |  | 文章状态(草稿,屏蔽,仅自己可见,公开,审核中,审核不通过) |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `publish_time` | `bigint` |  | 发布时间 |
| 9 | `start_date` | `string` |  | 状态起始时间 |
| 10 | `end_date` | `string` |  | 状态结束时间 |
| 11 | `dt` | `string` |  |  |

---

## dwd_post_status_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_status_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 202.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 202.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 用户博客 |
| 3 | `ispublished` | `int` |  | 是否发布或草稿 |
| 4 | `allowview` | `int` |  | 文章能见状态(0:公开可见,50:待审核,100:仅自己可见) |
| 5 | `valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步, 32: 用户删除) |
| 6 | `post_status` | `string` |  | 文章状态(草稿,屏蔽,仅自己可见,公开,审核中,审核不通过) |
| 7 | `create_time` | `bigint` |  | 创建时间 |
| 8 | `publish_time` | `bigint` |  | 发布时间 |
| 9 | `start_date` | `string` |  | 状态起始时间 |
| 10 | `end_date` | `string` |  | 状态结束时间 |
| 11 | `dt` | `string` |  |  |

---

## dwd_post_talk_browse_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_browse_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 8.2G |
| **是否分区表** | 是 |

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 浏览用户id |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 讨论类型 |
| 4 | `action_type` | `string` |  | 浏览类型: page_view,page_duration |
| 5 | `deviceudid` | `string` |  |  |
| 6 | `deviceos` | `string` |  |  |
| 7 | `appversion` | `string` |  | app版本信息,230814号加入 |
| 8 | `scene` | `string` |  | 场景 |
| 9 | `module` | `string` |  | 模块 |
| 10 | `source_module` | `string` |  | 来源 |
| 11 | `occurtime` | `bigint` |  |  |
| 12 | `is_from_rec` | `int` |  | 是否来源推荐场景 |
| 13 | `source1_scene` | `string` |  | 前一步场景 |
| 14 | `source2_scene` | `string` |  | 前两步场景 |
| 15 | `source2_module` | `string` |  | 前两步模块 |
| 16 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 17 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 18 | `blogid` | `bigint` |  | 日志所属博客Id |
| 19 | `questionid` | `bigint` |  | 话题Id |
| 20 | `questionuserid` | `bigint` |  | 话题所属用户Id |
| 21 | `talkcontent` | `string` |  | 讨论内容 |
| 22 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 23 | `tags` | `array<string>` |  | 文章标签 |
| 24 | `is_fans` | `int` |  | 用户是否博主粉丝 |
| 25 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 26 | `is_new_user` | `int` |  | 是否当天注册新用户 |
| 27 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 28 | `dt` | `string` |  |  |

---

## dwd_post_talk_discuss_score_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_discuss_score_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2167.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2167.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 37 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `string` |  | 唯一键,拼接talkId+talkType |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 动态类型 |
| 4 | `blogid` | `bigint` |  | 日志所属博客Id |
| 5 | `questionid` | `bigint` |  | 话题Id(特殊:如果本身是说说并且没挂话题则为0,如果本身是话题则为nul) |
| 6 | `questionuserid` | `bigint` |  | 话题所属用户Id(特殊:如果本身是说说并且没挂话题则为0,如果本身是话题则为null) |
| 7 | `talkcontent` | `string` |  | 讨论内容 |
| 8 | `recomstatus` | `int` |  | 推荐状态:0初始,1推荐,-1不推荐 |
| 9 | `forbidstatus` | `int` |  | 屏蔽状态:0未屏蔽,1被屏蔽,2申请解屏中(仅话题有) |
| 10 | `valid` | `int` |  | 同post中的valid |
| 11 | `allowview` | `int` |  | 同post中的allowview |
| 12 | `status` | `int` |  | 同question的status |
| 13 | `auditstatus` | `int` |  | 同question的auditstatus |
| 14 | `tags` | `array<string>` |  | 标签 |
| 15 | `questiontype` | `int` |  | 话题类型: null-非话题,0-1对1提问,1-1对N投稿,2-运营创建,3-聊聊 |
| 16 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 17 | `ext` | `string` |  | 扩展字段,answer为提名子话题的回复时,包含关联文章信息 |
| 18 | `discusscount` | `bigint` |  | 讨论数(后台),answer类型为null |
| 19 | `scorecount` | `bigint` |  | 打分人数,answer类型为null |
| 20 | `parentquestionid` | `bigint` |  | 父问题Id,answer类型为null |
| 21 | `answercount` | `bigint` |  | 回答数 |
| 22 | `cosplay` | `int` |  | 话题类型: 0-普通,1-角色说,2-打分父话题,3-打分子话题,4-印象词父话题,5-印象词子话题 |
| 23 | `answertype` | `int` |  | 回答类型: 0-普通,1-角色说,2-普通评论,3-划线评,4-圈评,5-提名子话题回复,6-打分父话题回复,7-印象词动态 |
| 24 | `relatedblogid` | `bigint` |  | 关联文章的blogId |
| 25 | `relatedpostid` | `bigint` |  | 关联文章的postId |
| 26 | `contenttype` | `string` |  | 关联文章的类型(1文字,2图片,3音乐,4视频,5问答,6长文章) |
| 27 | `topicchat` | `int` |  | 0-普通，1-主题聊天话题 |
| 28 | `topicid` | `bigint` |  | 主题id |
| 29 | `userid` | `bigint` |  | 回应者id |
| 30 | `content` | `string` |  | 回应的内容，里面包括回应的博客信息 |
| 31 | `replyl1commentid` | `bigint` |  | 回复一级评论id |
| 32 | `replyl2commentid` | `bigint` |  | 回复二级评论id |
| 33 | `commenthot` | `bigint` |  | 评论点赞数 |
| 34 | `replyl2count` | `bigint` |  | 2级评论数 |
| 35 | `score` | `bigint` |  | 分数 |
| 36 | `words` | `string` |  | 印象词 |
| 37 | `dt` | `string` |  |  |

---

## dwd_post_talk_expose_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_expose_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 88.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 88.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 浏览用户id |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 讨论类型 |
| 4 | `action_type` | `string` |  | 曝光类型: cell_exposure,cell_click,negative_feedback |
| 5 | `deviceudid` | `string` |  |  |
| 6 | `deviceos` | `string` |  |  |
| 7 | `appversion` | `string` |  | app版本信息,230814号加入 |
| 8 | `scene` | `string` |  | 场景 |
| 9 | `module` | `string` |  | 模块 |
| 10 | `source_module` | `string` |  | 来源 |
| 11 | `occurtime` | `bigint` |  |  |
| 12 | `is_from_rec` | `int` |  | 是否来源推荐场景 |
| 13 | `source1_scene` | `string` |  | 前一步场景 |
| 14 | `source2_scene` | `string` |  | 前两步场景 |
| 15 | `source2_module` | `string` |  | 前两步模块 |
| 16 | `detail_scene_level_1` | `string` |  | 当前场景的详细一级列 |
| 17 | `detail_scene_level_2` | `string` |  | 当前场景的详细二级列 |
| 18 | `reaction` | `string` |  | 曝光后续反馈操作： click dislike |
| 19 | `blogid` | `bigint` |  | 日志所属博客Id |
| 20 | `questionid` | `bigint` |  | 话题Id |
| 21 | `questionuserid` | `bigint` |  | 话题所属用户Id |
| 22 | `talkcontent` | `string` |  | 讨论内容 |
| 23 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 24 | `tags` | `array<string>` |  | 文章标签 |
| 25 | `is_fans` | `int` |  | 用户是否博主粉丝 |
| 26 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 27 | `is_new_user` | `int` |  | 是否当天注册新用户 |
| 28 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 29 | `dt` | `string` |  |  |

---

## dwd_post_talk_hot_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_hot_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.6G |
| **是否分区表** | 是 |

### 字段详情

共 34 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 浏览用户id |
| 2 | `optype` | `string` |  | 热度操作类型：praise,recommend,subscribe,reproduce |
| 3 | `optime` | `bigint` |  | 热度操作时间 |
| 4 | `ip` | `string` |  | 热度操作IP |
| 5 | `talkid` | `bigint` |  | 讨论id |
| 6 | `talktype` | `string` |  | 讨论类型 |
| 7 | `blogid` | `bigint` |  | 日志所属博客Id |
| 8 | `questionid` | `bigint` |  | 话题Id |
| 9 | `questionuserid` | `bigint` |  | 话题所属用户Id |
| 10 | `talkcontent` | `string` |  | 讨论内容 |
| 11 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 12 | `tags` | `array<string>` |  | 文章标签 |
| 13 | `platform` | `string` |  | 平台类型 |
| 14 | `action_type` | `string` |  | 热度类型: praise |
| 15 | `deviceudid` | `string` |  | 客户端设备ID |
| 16 | `eventid` | `string` |  | 事件ID |
| 17 | `scene` | `string` |  | 场景 |
| 18 | `source1_scene` | `string` |  | 前一步场景 |
| 19 | `source2_scene` | `string` |  | 前二步场景 |
| 20 | `module` | `string` |  | 模块 |
| 21 | `source1_module` | `string` |  | 前一步模块 |
| 22 | `source2_module` | `string` |  | 前二步模块 |
| 23 | `params_add_item_id` | `string` |  | 参数add_itemId |
| 24 | `params_add_item_type` | `string` |  | 参数add_itemType |
| 25 | `occurtime` | `bigint` |  | 事件发生事件 |
| 26 | `kafkatime` | `bigint` |  | 事件上报kafka时间 |
| 27 | `rec_alg` | `string` |  | 推荐算法 |
| 28 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 29 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 30 | `is_new_user` | `int` |  | 热度操作是否新用户 |
| 31 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 32 | `is_fans` | `int` |  | 是否博主粉丝 |
| 33 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 34 | `dt` | `string` |  |  |

---

## dwd_post_talk_page_view_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_page_view_di` |
| **描述** | 短内容功能页面曝光明细表 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 18.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 18.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `userid` | `bigint` |  |  |
| 4 | `itemid` | `string` |  |  |
| 5 | `itemtype` | `string` |  |  |
| 6 | `scene` | `string` |  |  |
| 7 | `params` | `map<string, string>` |  |  |
| 8 | `dt` | `string` |  |  |

---

## dwd_post_talk_publish_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_publish_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.9G |
| **是否分区表** | 是 |

### 字段详情

共 30 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `string` |  | 唯一键,拼接talkId+talkType |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 动态类型 |
| 4 | `blogid` | `bigint` |  | 日志所属博客Id |
| 5 | `questionid` | `bigint` |  | 话题Id(特殊:如果本身是说说并且没挂话题则为0,如果本身是话题则为nul) |
| 6 | `questionuserid` | `bigint` |  | 话题所属用户Id(特殊:如果本身是说说并且没挂话题则为0,如果本身是话题则为null) |
| 7 | `talkcontent` | `string` |  | 讨论内容 |
| 8 | `recomstatus` | `int` |  | 推荐状态:0初始,1推荐,-1不推荐 |
| 9 | `forbidstatus` | `int` |  | 屏蔽状态:0未屏蔽,1被屏蔽,2申请解屏中(仅话题有) |
| 10 | `valid` | `int` |  | 同post中的valid |
| 11 | `allowview` | `int` |  | 同post中的allowview |
| 12 | `status` | `int` |  | 同question的status |
| 13 | `auditstatus` | `int` |  | 同question的auditstatus |
| 14 | `tags` | `array<string>` |  | 标签 |
| 15 | `questiontype` | `int` |  | 话题类型: null-非话题,0-1对1提问,1-1对N投稿,2-运营创建,3-聊聊 |
| 16 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 17 | `ext` | `string` |  | 扩展字段,answer为提名子话题的回复时,包含关联文章信息 |
| 18 | `discusscount` | `bigint` |  | 讨论数(后台),answer类型为null |
| 19 | `scorecount` | `bigint` |  | 打分人数,answer类型为null |
| 20 | `parentquestionid` | `bigint` |  | 父问题Id,answer类型为null |
| 21 | `answercount` | `bigint` |  | 回答数 |
| 22 | `cosplay` | `int` |  | 话题类型: 0-普通,1-角色说,2-打分父话题,3-打分子话题,4-印象词父话题,5-印象词子话题 |
| 23 | `answertype` | `int` |  | 回答类型: 0-普通,1-角色说,2-普通评论,3-划线评,4-圈评,5-提名子话题回复,6-打分父话题回复,7-印象词动态 |
| 24 | `relatedblogid` | `bigint` |  | 关联文章的blogId |
| 25 | `relatedpostid` | `bigint` |  | 关联文章的postId |
| 26 | `contenttype` | `string` |  | 关联文章的类型(1文字,2图片,3音乐,4视频,5问答,6长文章) |
| 27 | `topicchat` | `int` |  | 0-普通，1-主题聊天话题 |
| 28 | `topicid` | `bigint` |  | 主题id |
| 29 | `is_user_first_post_talk` | `int` |  | 是否是用户当日首发 |
| 30 | `dt` | `string` |  |  |

---

## dwd_post_talk_response_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_response_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 351.4M |
| **是否分区表** | 是 |

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 浏览用户id |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 讨论类型 |
| 4 | `commentid` | `bigint` |  | 评论ID |
| 5 | `replyl1_commentid` | `bigint` |  | 回复一级评论ID |
| 6 | `replyl2_commentid` | `bigint` |  | 回复二级评论ID |
| 7 | `content` | `string` |  | 评论内容 |
| 8 | `optime` | `bigint` |  | 评论操作时间 |
| 9 | `ip` | `string` |  | 评论发布IP |
| 10 | `is_valid` | `int` |  | 是否有效评论 |
| 11 | `is_underscore` | `int` |  | 是否划线评 |
| 12 | `is_circle_comment` | `int` |  | 是否圈评 |
| 13 | `blogid` | `bigint` |  | 日志所属博客Id |
| 14 | `questionid` | `bigint` |  | 话题Id |
| 15 | `questionuserid` | `bigint` |  | 话题所属用户Id |
| 16 | `talkcontent` | `string` |  | 讨论内容 |
| 17 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 18 | `tags` | `array<string>` |  | 文章标签 |
| 19 | `platform` | `string` |  | 平台类型 |
| 20 | `action_type` | `string` |  | 回复类型: comment |
| 21 | `deviceudid` | `string` |  | 客户端设备ID |
| 22 | `eventid` | `string` |  | 事件ID |
| 23 | `scene` | `string` |  | 场景 |
| 24 | `source1_scene` | `string` |  | 前一步场景 |
| 25 | `source2_scene` | `string` |  | 前二步场景 |
| 26 | `module` | `string` |  | 模块 |
| 27 | `source1_module` | `string` |  | 前一步模块 |
| 28 | `source2_module` | `string` |  | 前二步模块 |
| 29 | `occurtime` | `bigint` |  | 事件发生事件 |
| 30 | `kafkatime` | `bigint` |  | 事件上报kafka时间 |
| 31 | `rec_alg` | `string` |  | 推荐算法 |
| 32 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 33 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 34 | `is_new_user` | `int` |  | 热度操作是否新用户 |
| 35 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 36 | `is_fans` | `int` |  | 是否博主粉丝 |
| 37 | `is_underscore_reply` | `int` |  | 是否划线评回复 1是 0否 |
| 38 | `is_circle_comment_reply` | `int` |  | 是否圈评回复 |
| 39 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 40 | `dt` | `string` |  |  |

---

## dwd_post_talk_share_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_talk_share_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 6.6M |
| **是否分区表** | 是 |

### 字段详情

共 33 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 浏览用户id |
| 2 | `talkid` | `bigint` |  | 讨论id |
| 3 | `talktype` | `string` |  | 讨论类型 |
| 4 | `platform` | `string` |  | 平台类型 |
| 5 | `action_type` | `string` |  | 回复类型: share |
| 6 | `deviceudid` | `string` |  | 客户端设备ID |
| 7 | `eventid` | `string` |  | 事件ID |
| 8 | `scene` | `string` |  | 场景 |
| 9 | `source1_scene` | `string` |  | 前一步场景 |
| 10 | `source2_scene` | `string` |  | 前二步场景 |
| 11 | `module` | `string` |  | 模块 |
| 12 | `source1_module` | `string` |  | 前一步模块 |
| 13 | `source2_module` | `string` |  | 前二步模块 |
| 14 | `occurtime` | `bigint` |  | 事件发生事件 |
| 15 | `kafkatime` | `bigint` |  | 事件上报kafka时间 |
| 16 | `rec_alg` | `string` |  | 推荐算法 |
| 17 | `ip` | `string` |  | 操作IP |
| 18 | `appversion` | `string` |  | app版本信息,230814号加入 |
| 19 | `detail_source1_scene_level_1` | `string` |  | 前一步详细场景的一级列 |
| 20 | `detail_source1_scene_level_2` | `string` |  | 前一步详细场景的二级列 |
| 21 | `blogid` | `bigint` |  | 日志所属博客Id |
| 22 | `questionid` | `bigint` |  | 话题Id |
| 23 | `questionuserid` | `bigint` |  | 话题所属用户Id |
| 24 | `talkcontent` | `string` |  | 讨论内容 |
| 25 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 26 | `tags` | `array<string>` |  | 文章标签 |
| 27 | `is_new_user` | `int` |  | 操作者是否为新用户 |
| 28 | `deviceid` | `bigint` |  | deviceUdid映射数字id， 参见lofter.dwd_par_device_all_dd |
| 29 | `share_method` | `string` |  | 分享方式 |
| 30 | `share_channel` | `string` |  | 分享渠道: 新浪微博 微信好友 微信朋友圈 QQ好友 QQ空间 小红书等 |
| 31 | `is_fans` | `int` |  | 用户是否博主粉丝 |
| 32 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 33 | `dt` | `string` |  |  |

---

## dwd_post_text_length_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_post_text_length_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 16.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 16.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  |  |
| 2 | `blogid` | `bigint` |  |  |
| 3 | `contentsize` | `bigint` |  | 内容长度， 基于html内容文本 (去掉标签属性链接等)) |
| 4 | `dt` | `string` |  |  |

---

## dwd_push_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_push_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 592.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 592.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `action` | `int` |  | push行为类型 1 推送 2 触达 3点击 4上报push开关 5 有效推送 |
| 3 | `occurtime` | `bigint` |  | 行为时间 推送为服务端时间 其他基于客户端时间 |
| 4 | `pushgroupid` | `string` |  | push消息groupId |
| 5 | `is_push_option_on` | `int` |  | 上报push开关是否打开， 1打开 0 关闭 |
| 6 | `deviceudid` | `string` |  | 行为关联客户端设备id， 推送没有关联设备 |
| 7 | `channeltype` | `int` |  | 渠道类型： 0 杭研推送 1小米通知栏 2华为 3小米透传 4 oppo通知栏 6魅族通知栏 7 文漫websocket 8vivo 10小米ios推送 11华为新版本 |
| 8 | `pushgroup` | `string` |  |  |
| 9 | `pushmessageid` | `bigint` |  |  |
| 10 | `title` | `string` |  |  |
| 11 | `content` | `string` |  |  |
| 12 | `dt` | `string` |  |  |

---

## dwd_pve_ml_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_ml_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | internal |
| **表大小** | 39.3M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `order_id` | `bigint` |  | NULL |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `amount` | `double` |  | 交易总金额，未减去渠道分层（channelDivision）和手续费（fee) |
| 4 | `customuuid` | `string` |  | 下单设备id |
| 5 | `paytime` | `bigint` |  | 第三方支付时间 |
| 6 | `deviceudid` | `string` |  | 设备id |
| 7 | `actpwd` | `string` |  | 口令 |
| 8 | `channel` | `string` |  | 口令渠道 |
| 9 | `actpwd_search_time` | `bigint` |  | 访问时间 |
| 10 | `actpwd_search_list` | `array<string>` |  |  |
| 11 | `platform` | `int` |  | 1安卓, 2苹果 |
| 12 | `dt` | `string` |  |  |

---

## dwd_pve_music_page_access_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_music_page_access_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 210.5M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `device_os` | `string` |  | 系统 |
| 3 | `scene` | `string` |  | 场景 |
| 4 | `url` | `string` |  | 页面url |
| 5 | `occur_time` | `bigint` |  | 发生时间 |
| 6 | `roleid` | `bigint` |  | 角色id |
| 7 | `role_name` | `string` |  | 角色名称 |
| 8 | `music_uid` | `bigint` |  | 云音乐用户ID |
| 9 | `dt` | `string` |  |  |

---

## dwd_pve_music_user_dialogue_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_music_user_dialogue_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 718.2M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `roleid` | `bigint` |  | 角色id |
| 3 | `role_name` | `string` |  | 角色名称 |
| 4 | `occur_time` | `bigint` |  | 发生时间 |
| 5 | `music_uid` | `bigint` |  | 云音乐用户ID |
| 6 | `content` | `string` |  |  |
| 7 | `sender` | `int` |  | 对话类型： 1 用户发送， 0 ai回复 |
| 8 | `dt` | `string` |  |  |

---

## dwd_pve_user_amount_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_amount_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 276.0M |
| **是否分区表** | 是 |

### 字段详情

共 24 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tradeid` | `bigint` |  | 对应trade_order表时的id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `stamina` | `bigint` |  | 购买的体力值 |
| 4 | `platform` | `int` |  | 1安卓，2苹果 |
| 5 | `paytype` | `int` |  | 支付渠道，1支付宝，2苹果支付 |
| 6 | `amount` | `double` |  | 交易总金额，未减去渠道分层（channelDivision）和手续费（fee） |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `finishtime` | `bigint` |  | 修改时间 |
| 9 | `productid` | `bigint` |  | 体力值商品id |
| 10 | `bankordersn` | `string` |  | 第三方支付流水号 |
| 11 | `bankordertime` | `bigint` |  | 第三方支付时间 |
| 12 | `sandbox` | `int` |  | 是否沙盒购买 0-不是 1-是 |
| 13 | `producttype` | `int` |  | 商品类型，0能量，1忘忧草 |
| 14 | `roleid` | `bigint` |  | 角色id |
| 15 | `pveuserid` | `bigint` |  | 角色对应的官方账号 |
| 16 | `rolename` | `string` |  | 角色名称 |
| 17 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 18 | `rolestatus` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 19 | `rolepublicview` | `int` |  | 是否公开可见，在聚合页分发 |
| 20 | `rolecreatoruid` | `bigint` |  | 创建者id |
| 21 | `groupid` | `bigint` |  | 组ID |
| 22 | `grouproletype` | `int` |  | 组内全部的角色类型,同roleType,如果组内有不同角色类型则为-1 |
| 23 | `chattype` | `int` |  | 0:单聊, 1:cp单聊, 2:群聊 |
| 24 | `dt` | `string` |  |  |

---

## dwd_pve_user_chats_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_chats_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 332.7M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_chats_group_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_chats_group_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.4G |
| **是否分区表** | 是 |

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dialogueid` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `groupid` | `bigint` |  | 组ID |
| 4 | `pveuserid` | `bigint` |  | PVE虚拟男友用户id |
| 5 | `roleid` | `bigint` |  | 角色ID |
| 6 | `roletype` | `bigint` |  | 角色类型 |
| 7 | `content` | `string` |  | 对话内容 |
| 8 | `type` | `int` |  | 对话类型，0普通, 1日常陪伴 |
| 9 | `audioflag` | `int` |  | 音频标识，1音频, 0文本 |
| 10 | `sortno` | `bigint` |  | 对话轮数 |
| 11 | `ext` | `string` |  | 扩展内容 AudioInfo |
| 12 | `createtime` | `bigint` |  | 创建时间 |
| 13 | `requestid` | `string` |  | 一组对话请求唯一ID |
| 14 | `sessionid` | `string` |  | 对话状态 |
| 15 | `status` | `int` |  | 对话状态标识，0初始，1已回答，2命中敏感词，3AI异常 |
| 16 | `aisource` | `int` |  | AI数据源标识，0minimax, 1伏羲 |
| 17 | `messagetype` | `int` |  | 消息类型，0普通消息，1道具，2副本，5动态 |
| 18 | `dtid` | `string` |  | 对话traceId |
| 19 | `grouptype` | `int` |  | 组类型，0：cp，1：群聊 |
| 20 | `sender` | `int` |  | 1:用户发送，0：ai回复 |
| 21 | `targetroleids` | `string` |  | 指定回复的角色id |
| 22 | `chattype` | `int` |  | 0:单聊, 1:cp单聊, 2:群聊 |
| 23 | `dt` | `string` |  |  |

---

## dwd_pve_user_chats_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_chats_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 252.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 252.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `dialogueid` | `bigint` |  | 聊天日志id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `content` | `string` |  | 对话内容 |
| 4 | `type` | `int` |  | 对话类型0普通 |
| 5 | `audioflag` | `int` |  | 1音频,0文本 |
| 6 | `ext` | `string` |  | 扩展内容 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `requestid` | `string` |  | 一组对话请求唯一ID |
| 9 | `status` | `int` |  | 对话状态，标识ai回复0初始，1已回答，2命中敏感词，3ai异常 |
| 10 | `aisource` | `int` |  | ai数据源:0minimax,1伏羲 |
| 11 | `sortno` | `bigint` |  | 对话序号 |
| 12 | `roleid` | `bigint` |  | 角色id |
| 13 | `pveuserid` | `bigint` |  | 角色对应的官方账号 |
| 14 | `rolename` | `string` |  | 角色名称 |
| 15 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 16 | `rolestatus` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 17 | `rolepublicview` | `int` |  | 是否公开可见，在聚合页分发 |
| 18 | `rolecreatoruid` | `bigint` |  | 创建者id |
| 19 | `messagetype` | `int` |  | 0普通消息，1道具，2混沌，3梦境，4任务，12评论，13动态 |
| 20 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 21 | `chattype` | `int` |  | 0:单聊, 1:cp单聊, 2:群聊 |
| 22 | `dt` | `string` |  |  |

---

## dwd_pve_user_chats_low_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_chats_low_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 67.1M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_chats_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_chats_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 23.3M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_chats_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_chats_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 18.0M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_grass_stamina_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_grass_stamina_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logid` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `grassdelta` | `bigint` |  | 消耗草的数量 |
| 4 | `delta` | `bigint` |  | 体力值增量可为负数 |
| 5 | `stamina` | `bigint` |  | 体力值 |
| 6 | `type` | `int` |  | 类型：0消费，1充值，2每日礼 |
| 7 | `status` | `int` |  | 状态，0正常 |
| 8 | `createtime` | `bigint` |  | 创建时间 |
| 9 | `ext` | `string` |  | 消除内容id |
| 10 | `roleid` | `bigint` |  | 角色id |
| 11 | `pveuserid` | `bigint` |  | 角色对应的官方账号 |
| 12 | `rolename` | `string` |  | 角色名称 |
| 13 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 14 | `rolestatus` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 15 | `rolepublicview` | `int` |  | 是否公开可见，在聚合页分发 |
| 16 | `rolecreatoruid` | `bigint` |  | 创建者id |
| 17 | `dt` | `string` |  |  |

---

## dwd_pve_user_interview_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_interview_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 950.4M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_interview_low_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_interview_low_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 231.6M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_interview_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_interview_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 103.9M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_interview_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_interview_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 135.5M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_pve_user_props_stamina_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_props_stamina_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.5G |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logid` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `propsid` | `bigint` |  | 道具id |
| 4 | `coststamina` | `bigint` |  | 消耗的能量值 |
| 5 | `dialogueno` | `bigint` |  | 应用的对话轮数 |
| 6 | `dialogueid` | `bigint` |  | 道具所在的对话id |
| 7 | `status` | `int` |  | 状态，0正常，99忘忧草 |
| 8 | `createtime` | `bigint` |  | 创建时间 |
| 9 | `updatetime` | `bigint` |  | 更新时间 |
| 10 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 11 | `userdupid` | `bigint` |  | 角色副本id |
| 12 | `propsdesc` | `string` |  | 道具阶段性效果 |
| 13 | `propstype` | `int` |  | 道具类型，0普通，1万能 |
| 14 | `roleid` | `bigint` |  | 角色id |
| 15 | `pveuserid` | `bigint` |  | 角色对应的官方账号 |
| 16 | `rolename` | `string` |  | 角色名称 |
| 17 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 18 | `rolestatus` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 19 | `rolepublicview` | `int` |  | 是否公开可见，在聚合页分发 |
| 20 | `rolecreatoruid` | `bigint` |  | 创建者id |
| 21 | `dt` | `string` |  |  |

---

## dwd_pve_user_stamina_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_stamina_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 56.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 56.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logid` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `newstamina` | `bigint` |  | 新的体力值 |
| 4 | `oldstamina` | `bigint` |  | 旧的体力值 |
| 5 | `dialogueid` | `bigint` |  | 对话id记录 |
| 6 | `delta` | `bigint` |  | 体力值增量可为负数 |
| 7 | `type` | `int` |  | 类型：0ai对话,11听语音,12转文字，1充值，2每日登录, 3周卡/月卡 |
| 8 | `status` | `int` |  | 状态，0正常,1已领取(周卡和月卡) |
| 9 | `createtime` | `bigint` |  | 创建时间 |
| 10 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 11 | `endtime` | `bigint` |  | 若为领取体力记录，表示失效时间 |
| 12 | `roleid` | `bigint` |  | 角色id |
| 13 | `pveuserid` | `bigint` |  | 角色对应的官方账号 |
| 14 | `rolename` | `string` |  | 角色名称 |
| 15 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 16 | `rolestatus` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 17 | `rolepublicview` | `int` |  | 是否公开可见，在聚合页分发 |
| 18 | `rolecreatoruid` | `bigint` |  | 创建者id |
| 19 | `dt` | `string` |  |  |

---

## dwd_pve_user_sweet_stamina_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_sweet_stamina_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 25.9M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logid` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `createtime` | `bigint` |  | 创建时间 |
| 4 | `stamina` | `bigint` |  | 体力值 |
| 5 | `sweetcnt` | `bigint` |  | 记忆糖数量 |
| 6 | `roleid` | `bigint` |  | 角色id |
| 7 | `pveuserid` | `bigint` |  | 角色对应的官方账号 |
| 8 | `rolename` | `string` |  | 角色名称 |
| 9 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 10 | `rolestatus` | `int` |  | 角色状态，0: 未生效, 1: 生效, -1: 删除 |
| 11 | `rolepublicview` | `int` |  | 是否公开可见，在聚合页分发 |
| 12 | `rolecreatoruid` | `bigint` |  | 创建者id |
| 13 | `dt` | `string` |  |  |

---

## dwd_pve_user_timed_stamina_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_pve_user_timed_stamina_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 31.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 31.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logid` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `timedids` | `string` |  | 限时能量来源id |
| 4 | `targetid` | `bigint` |  | 消耗记录id |
| 5 | `type` | `int` |  | 消耗类型：同PVEUserStaminaLog.type |
| 6 | `createtime` | `bigint` |  | 创建时间 |
| 7 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 8 | `sourceid` | `bigint` |  | 源id |
| 9 | `sourcetype` | `int` |  | 来源类型，0单聊，1聊天室 |
| 10 | `delta` | `bigint` |  | 消耗的能量值 |
| 11 | `newstamina` | `bigint` |  | 归因：新能量 |
| 12 | `oldstamina` | `bigint` |  | 归因：旧能量 |
| 13 | `starttime` | `bigint` |  | 开始时间 |
| 14 | `endtime` | `bigint` |  | 结束时间 |
| 15 | `dt` | `string` |  |  |

---

## dwd_rec_content_understand_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_rec_content_understand_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 37.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 37.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `itemid` | `bigint` |  | 物料id: 文章Id、评论Id等 |
| 2 | `type` | `string` |  | 识别类型: 图文类目V2,AI文识别,细分类目,作品,茶水间,新旧派识别,付费标题题材,付费标题类型,设定识别,题材识别,评论体系分类,茶水间负向标题,文章评论,文章连载,章背景摘要,文章字数,图文类目,视频类目,非作品向文章类目等 |
| 3 | `level1_tag` | `string` |  | 一级标签 |
| 4 | `level2_tag` | `string` |  | 二级标签 |
| 5 | `level3_tag` | `string` |  | 三级标签 |
| 6 | `score` | `double` |  | 计算分值 |
| 7 | `dt` | `string` |  |  |

---

## dwd_rec_post_review_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_rec_post_review_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 843.0M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章Id |
| 2 | `blogid` | `bigint` |  | 创作者Id |
| 3 | `contenttype` | `string` |  | 文章类型：文字、图片、视频 |
| 4 | `is_today_publish` | `int` |  | 是否当日发文 |
| 5 | `is_pay_gift` | `int` |  | 是否付费 |
| 6 | `machine_state` | `int` |  | 机器审核状态1：机审通过，2：试投中，3：仅规则未通过，4：仅试投未通过，5：仅AI未通过，6：AI-试投均未通过 |
| 7 | `human_state` | `int` |  | 人工审核状态1：人审通过，2：人审未通过，3：人审待审核 |
| 8 | `dt` | `string` |  |  |

---

## dwd_rec_reason_scene_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_rec_reason_scene_di` |
| **描述** | 推荐侧推荐理由及埋点数据 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 1242.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1242.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `source` | `string` |  | 来源：数据,算法 |
| 2 | `actiontype` | `string` |  | 曝光或点击 |
| 3 | `eventid` | `string` |  |  |
| 4 | `userid` | `bigint` |  | 触发事件的用户 |
| 5 | `deviceudid` | `string` |  | 设备ID,非哈勃 |
| 6 | `deviceos` | `string` |  | 系统 |
| 7 | `reason` | `string` |  | 推荐理由 |
| 8 | `scene` | `string` |  | 场景 |
| 9 | `postid` | `bigint` |  | 文章ID |
| 10 | `blogid` | `bigint` |  | 文章作者ID |
| 11 | `contenttype` | `string` |  | 物料类型 |
| 12 | `is_collection` | `int` |  | 是否合集,1是0否 |
| 13 | `detail_scene_level_1` | `string` |  | 一级场景 |
| 14 | `detail_scene_level_2` | `string` |  | 二级场景 |
| 15 | `dt` | `string` |  |  |

---

## dwd_return_user_push_publish_success_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_return_user_push_publish_success_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 293.5K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `content` | `string` |  | 发送内容 |
| 3 | `type` | `int` |  | 类型 |
| 4 | `publish_date` | `string` |  | 发送日期，理论上等于分区日期 |
| 5 | `dt` | `string` |  |  |

---

## dwd_rewardcenter_user_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_rewardcenter_user_di` |
| **描述** | 权益中心用户日活及新增标记 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 79.7M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `visit_cnt` | `bigint` |  | 当日访问PV |
| 3 | `is_new_user` | `int` |  | 是否权益中心新用户： 1是 0否 |
| 4 | `is_return_user` | `int` |  | 是否权益中心回流用户： 1是 0否 |
| 5 | `is_platform_new` | `int` |  | 是否平台新用户： 1是 0否 |
| 6 | `is_platform_return` | `int` |  | 是否平台回流用户: 1是 0否 |
| 7 | `dt` | `string` |  |  |

---

## dwd_rewardcenter_visit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_rewardcenter_visit_di` |
| **描述** | 权益中心埋点访问明细 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 9.6G |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `deviceudid` | `string` |  | 设备唯一标识 |
| 3 | `eventid` | `string` |  | 事件ID |
| 4 | `actiontype` | `string` |  | 行为类型 |
| 5 | `occurtime` | `bigint` |  | 发生时间毫秒 |
| 6 | `source_type` | `string` |  | 来源类型: fixed-常驻, attract-引流, recall-召回 |
| 7 | `source` | `string` |  | 资源位名称: mine_entrance-每日福利, mine_account_loftgrain-我的账户-乐乎米, mine_account_banner-我的账户-滚动banner, giftbag_grain_ticket-礼物背包-粮票页, home_upperleft-首页吸顶, explorefeednative_11-信息流定坑, rc_subscribe-订阅提醒, privatemessage-私信, push-push, home_float-吸边 |
| 8 | `dt` | `string` |  |  |

---

## dwd_risk_brush_hot_suspect_post_rank_ip_posts_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_brush_hot_suspect_post_rank_ip_posts_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 706.9M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `hot_uv` | `bigint` |  | 前100热度操作人数 |
| 4 | `max_op_city_uv` | `bigint` |  | 最大操作ip城市热度人数 |
| 5 | `max_register_city_uv` | `bigint` |  | 最大注册ip城市热度人数 |
| 6 | `ruleid` | `int` |  | 命中规则: 0 同ip操作占比 1 注册ip城市占比 2 操作ip城市占比 |
| 7 | `ip_city` | `string` |  | 刷热ip或者城市 |
| 8 | `dt` | `string` |  |  |

---

## dwd_risk_brush_hot_suspect_post_rank_ip_users_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_brush_hot_suspect_post_rank_ip_users_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 345.4K |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `userid` | `bigint` |  | 刷热嫌疑用户id |
| 4 | `ruleid` | `int` |  | 命中规则: 0 同ip操作占比 1 注册ip城市占比 2 操作ip城市占比 |
| 5 | `ip_city` | `string` |  | 刷热ip或者城市 |
| 6 | `dt` | `string` |  |  |

---

## dwd_risk_brush_hot_suspect_post_rank_posts_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_brush_hot_suspect_post_rank_posts_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 888.0K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `hot_uv` | `bigint` |  |  |
| 4 | `suspect_uv` | `bigint` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dwd_risk_brush_hot_suspect_post_rank_users_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_brush_hot_suspect_post_rank_users_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 13.1M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `userid` | `bigint` |  | 嫌疑用户id |
| 4 | `ruleid` | `int` |  | 嫌疑规则id |
| 5 | `dt` | `string` |  |  |

---

## dwd_risk_brush_hot_suspect_users_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_brush_hot_suspect_users_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 725.1M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 近30天嫌疑刷热用户 |
| 2 | `dt` | `string` |  |  |

---

## dwd_risk_offsite_induction_return_gift_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_offsite_induction_return_gift_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.7G |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `content` | `string` |  | 内容 |
| 4 | `dt` | `string` |  |  |

---

## dwd_risk_shuare_model_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_shuare_model_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 418.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 418.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 60 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 文章博客Id |
| 3 | `blogname` | `string` |  | 博客名称 |
| 4 | `contenttype` | `string` |  | 文章类型 |
| 5 | `publishdate` | `string` |  | 文章发布日期 |
| 6 | `tags` | `array<string>` |  | 文章标签 |
| 7 | `url` | `string` |  | 文章链接 |
| 8 | `hispraise` | `bigint` |  | 文章历史点赞数 |
| 9 | `publisheruserid` | `bigint` |  | 热度操作的用户Id |
| 10 | `ip` | `string` |  | 点赞ip |
| 11 | `praiseday` | `string` |  | 点赞日期 |
| 12 | `praisetime` | `string` |  | 点赞时间 |
| 13 | `praiseblogname` | `string` |  | 点赞用户的博客Id |
| 14 | `createip` | `string` |  | 点赞用户注册IP |
| 15 | `ipuv` | `bigint` |  | 注册IP下的注册用户数 |
| 16 | `hotuv` | `bigint` |  | 点赞IP下对该文章点赞的用户数 |
| 17 | `hottimes` | `bigint` |  | 统计周期内点赞ip下的最早和最晚点赞时间差 |
| 18 | `1minipuv` | `bigint` |  | 一分钟内点赞用户数 |
| 19 | `10minipuv` | `bigint` |  | 十分钟内点赞用户数 |
| 20 | `appuserid` | `bigint` |  | app客户端点赞用户id |
| 21 | `appdeviceudid` | `string` |  | APP客户端点赞设备id |
| 22 | `pcuserid` | `bigint` |  | pc端点赞的用户id |
| 23 | `pcdeviceid` | `string` |  | pc端点赞的设备id |
| 24 | `devicenum` | `bigint` |  | 点赞用户当天在服务端登录的设备数 |
| 25 | `ipnum` | `bigint` |  | 点赞用户当天在服务端登录IP数 |
| 26 | `citynum` | `bigint` |  | 点赞用户当天在服务端登录城市数 |
| 27 | `loginipuv` | `bigint` |  | 点赞IP当天登录用户数 |
| 28 | `loginipdeviceuv` | `bigint` |  | 点赞IP当天登录设备数 |
| 29 | `profilecreatetime` | `bigint` |  | 点赞用户注册时间 |
| 30 | `email` | `string` |  | 点赞用户注册email |
| 31 | `profiletype` | `string` |  | 点赞用户注册类型 |
| 32 | `platfrom` | `string` |  | 点赞用户注册平台 |
| 33 | `fans` | `bigint` |  | 点赞用户的粉丝数 |
| 34 | `followusers` | `bigint` |  | 点赞用户的关注人数 |
| 35 | `postnum` | `bigint` |  | 点赞用户的原创发文数 |
| 36 | `praisepv` | `bigint` |  | 点赞用户送出的点赞数 |
| 37 | `praisepostpv` | `bigint` |  | 点赞用户送出的点赞文章数 |
| 38 | `praisepostuv` | `bigint` |  | 点赞用户送出的点赞文章所属博客数(下同) |
| 39 | `reproducepv` | `bigint` |  | 转载数 |
| 40 | `reproducepostpv` | `bigint` |  | 转载文章数 |
| 41 | `reproducepostuv` | `bigint` |  | 转载博客数 |
| 42 | `recpv` | `bigint` |  | 推荐数 |
| 43 | `recpostpv` | `bigint` |  | 推荐文章数 |
| 44 | `recpostuv` | `bigint` |  | 推荐博客数 |
| 45 | `subscribepv` | `bigint` |  | 订阅数 |
| 46 | `subscribepostpv` | `bigint` |  | 订阅文章数 |
| 47 | `subscribepostuv` | `bigint` |  | 订阅博客数 |
| 48 | `commentpv` | `bigint` |  | 评论数 |
| 49 | `commentpostpv` | `bigint` |  | 评论文章数 |
| 50 | `commentpostuv` | `bigint` |  | 评论博客数 |
| 51 | `messagepv` | `bigint` |  | 发送私信数 |
| 52 | `messageuv` | `bigint` |  | 发送私信接受的博客数 |
| 53 | `userlevel` | `string` |  | 用户类型:官号，达人，普通用户 |
| 54 | `userhot` | `bigint` |  | 点赞用户获取的历史热度 |
| 55 | `isblackphoneflag` | `int` |  | 用户手机号是否是黑名单 |
| 56 | `device_province_num_1d` | `bigint` |  | 1日设备登录省份数 |
| 57 | `quick_browse_pv_30d` | `bigint` |  | 30天快速浏览次数 |
| 58 | `real_browse_pv_30d` | `bigint` |  | 30天有效浏览次数 |
| 59 | `block_tag_count` | `bigint` |  | 屏蔽标签数 |
| 60 | `dt` | `string` |  |  |

---

## dwd_risk_shuare_post_model_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_shuare_post_model_di` |
| **描述** | 文章刷热模型基础指标， 计算7日内热度行为文章数据 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 69.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 69.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `postid` | `bigint` |  |  |
| 3 | `post_title` | `string` |  | 文章标题 |
| 4 | `post_content_type` | `string` |  | 文章类型 |
| 5 | `post_url` | `string` |  | 文章链接 |
| 6 | `blog_url` | `string` |  | 作者链接 |
| 7 | `blog_nickname` | `string` |  | 作者昵称 |
| 8 | `hot` | `bigint` |  | 文章累计热度 |
| 9 | `comments` | `bigint` |  | 文章累计评论 |
| 10 | `revenue` | `double` |  | 文章累计营收金额 |
| 11 | `web_praise_no_device_info_uv` | `bigint` |  | web端注册账号点赞数量 没有任何设备信息 |
| 12 | `max_city_ip_praise_ratio` | `double` |  | 最大同一IP归属地点赞到市占比 |
| 13 | `praise2comment_ratio` | `double` |  | 评赞比 |
| 14 | `praise_no_browse_uv` | `bigint` |  | 点赞账号 最近7次活跃均无正常浏览行为 |
| 15 | `praise_quick_browse_ratio` | `double` |  | 点赞用户浏览时长小于等于10s 占比 |
| 16 | `publishdate` | `string` |  | 发文日期 |
| 17 | `dt` | `string` |  |  |

---

## dwd_risk_user_level_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_risk_user_level_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2.2G |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `usertype` | `string` |  | 用户安全等级，分为黑灰白 |
| 3 | `source` | `string` |  | 来源，分为model,robot,human |
| 4 | `dt` | `string` |  |  |

---

## dwd_search_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_search_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1896.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1896.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `app_version` | `string` |  | 版本 |
| 4 | `occur_time` | `bigint` |  | 行为发生时间 |
| 5 | `search_query` | `string` |  | 搜索词 |
| 6 | `eventid` | `string` |  | mda事件id |
| 7 | `itemid` | `string` |  | 搜索itemid |
| 8 | `itemtype` | `string` |  | 搜索item类型 |
| 9 | `tab` | `string` |  | 搜索tab |
| 10 | `deviceos` | `string` |  | 客户端操作系统 |
| 11 | `ip` | `string` |  | 搜索操作ip地址 |
| 12 | `dt` | `string` |  |  |

---

## dwd_subject_bubble_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_subject_bubble_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.2G |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `subject_id` | `bigint` |  | 话题气泡id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `show_type` | `string` |  | 展示位置： bubble(气泡) message(私信) |
| 4 | `action` | `string` |  | 行为： expose click |
| 5 | `dt` | `string` |  |  |

---

## dwd_suspect_shuare_model_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_suspect_shuare_model_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2.4G |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 文章博客Id |
| 3 | `publisheruserid` | `bigint` |  | 热度操作的用户Id |
| 4 | `ip` | `string` |  | 点赞ip |
| 5 | `dt` | `string` |  |  |

---

## dwd_suspect_shuare_model_post_rank_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_suspect_shuare_model_post_rank_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 8.8G |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 文章博客Id |
| 3 | `publisheruserid` | `bigint` |  | 热度操作的用户Id |
| 4 | `ip` | `string` |  | 点赞ip |
| 5 | `dt` | `string` |  |  |

---

## dwd_tag_browse_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_tag_browse_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2313.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2313.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `time` | `bigint` |  | 访问时间 |
| 4 | `platform` | `string` |  | 平台 |
| 5 | `ip` | `string` |  | 访问ip |
| 6 | `tab` | `string` |  | 标签页tab |
| 7 | `user_create_date` | `string` |  | 用户创建日期 |
| 8 | `dt` | `string` |  |  |

---

## dwd_tag_ip_mapping_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_tag_ip_mapping_nd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.2M |
| **是否分区表** | 否 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `ip` | `string` |  | 标签对应的IP |
| 3 | `ip_rank` | `bigint` |  | 标签下ip排序， 从1开始 |

---

## dwd_tag_subscribe_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_tag_subscribe_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 11.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `time` | `bigint` |  | 订阅时间 |
| 4 | `platform` | `string` |  | 平台 |
| 5 | `user_create_date` | `string` |  | 用户创建日期 |
| 6 | `dt` | `string` |  |  |

---

## dwd_ue_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ue_report_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 522.9M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `kafkatime` | `bigint` |  |  |
| 2 | `date` | `date` |  |  |
| 3 | `reportid` | `string` |  |  |
| 4 | `reporttype` | `string` |  |  |
| 5 | `userid` | `bigint` |  |  |
| 6 | `blogid` | `bigint` |  |  |
| 7 | `postid` | `bigint` |  |  |
| 8 | `dt` | `string` |  |  |

---

## dwd_user_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 36.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 36.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `device_os` | `string` |  | 操作平台 |
| 3 | `first_active_time` | `bigint` |  | 当日首次活跃时间 剔除延迟发送昨日数据 |
| 4 | `is_anonymous` | `int` |  | 是否匿名用户 1是 0否 |
| 5 | `dt` | `string` |  | 日期 |

---

## dwd_user_ad_revenue_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_ad_revenue_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 229.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 229.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `blogid` | `bigint` |  | 博客id |
| 4 | `money` | `double` |  | 广告竞价金额 单位元 |
| 5 | `revenue_module` | `string` |  | 收入模块：激励广告解锁 贴片广告 |
| 6 | `request_time` | `bigint` |  | 请求时间 |
| 7 | `request_date` | `string` |  | 请求日期 |
| 8 | `is_paid_post` | `int` |  | 是否内容付费 1是 0否 |
| 9 | `business` | `string` |  | 业务模块： 效果广告, 激励广告 |
| 10 | `req_id` | `string` |  | 广告请求id |
| 11 | `positionid` | `bigint` |  | 广告位id |
| 12 | `position_name` | `string` |  | 广告位名字 |
| 13 | `dt` | `string` |  |  |

---

## dwd_user_black_hit_rule_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_black_hit_rule_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 52.2M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 命中规则的黑名单用户 |
| 2 | `dt` | `string` |  |  |

---

## dwd_user_events_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_events_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 296.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 296.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `eventid` | `string` |  | 事件id |
| 3 | `dt` | `string` |  |  |

---

## dwd_user_group_user_list_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_group_user_list_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 440.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 440.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `dt` | `string` |  |  |
| 3 | `job_id` | `string` |  |  |

---

## dwd_user_low_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_low_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.2G |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 低活用户 口径：昨日活跃  昨天之前近30天活跃天数少于15天 剔除30天新注册和回流 |
| 2 | `dt` | `string` |  |  |

---

## dwd_user_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.2G |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `is_anonymous` | `int` |  | 是否匿名用户 1是 0否 |
| 3 | `dt` | `string` |  | 日期 |

---

## dwd_user_order_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_order_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6553.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 6553.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `bigint` |  | 订单id |
| 2 | `order_type` | `string` |  | 订单类型: gift_present benefit store_vip fans_vip blog_vip mini_program coupon dressing_suit avatar_box  pve_stamina paper_man_stamina reward live_gift |
| 3 | `userid` | `bigint` |  | 用户id |
| 4 | `productid` | `bigint` |  | 商品id: 市集商品id 抽赏房间id 博客id 礼物文章id 装扮id 表情包id 皮肤套餐id, 书城会员为0 |
| 5 | `product_num` | `int` |  | 商品数量 |
| 6 | `order_time` | `bigint` |  | 订单时间 |
| 7 | `order_date` | `string` |  | 订单日期 |
| 8 | `pay_time` | `bigint` |  | 支付时间 |
| 9 | `pay_date` | `string` |  | 支付日期 |
| 10 | `money` | `double` |  | 支付金额 |
| 11 | `postid` | `bigint` |  | 订单关联文章id |
| 12 | `blogid` | `bigint` |  | 订单关联博客id |
| 13 | `revenue_module` | `string` |  | 收入模块 |
| 14 | `mbo_lv1_module` | `string` |  | 日报营收一级模块 |
| 15 | `mbo_lv2_module` | `string` |  | 日报营收二级模块 |
| 16 | `is_post_pay` | `int` |  | 是否文章内容付费: 1是 0否 |
| 17 | `dt` | `string` |  |  |

---

## dwd_user_retention_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_retention_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 37.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 37.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `basedate` | `string` |  |  |
| 2 | `userid` | `bigint` |  |  |
| 3 | `dt` | `string` |  |  |
| 4 | `period` | `int` |  |  |

---

## dwd_user_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 768.0M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 回流用户id |
| 2 | `dt` | `string` |  |  |

---

## dwd_user_white_list_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_user_white_list_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 97.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 97.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 豁免用户 |
| 2 | `type` | `string` |  | 豁免用户所属类型 |
| 3 | `dt` | `string` |  |  |

---

## dwd_userfolder_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_userfolder_action_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 45.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 45.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `folderid` | `bigint` |  |  |
| 4 | `folderuserid` | `bigint` |  |  |
| 5 | `occurtime` | `bigint` |  |  |
| 6 | `isexposure` | `int` |  |  |
| 7 | `isclick` | `int` |  |  |
| 8 | `isbrowse` | `int` |  |  |
| 9 | `dt` | `string` |  |  |

---

## dwd_vc_device_new_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_vc_device_new_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 8.3M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `deviceos` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `appchannel` | `string` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dwd_vc_device_return_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_vc_device_return_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.9M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `deviceos` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `appchannel` | `string` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dwd_video_cover_edit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_video_cover_edit_di` |
| **描述** | 视频封面编辑事件明细 · 字段定义待客户端 schema 确认后细化 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 121.9K |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户 ID |
| 2 | `deviceudid` | `string` |  | 设备 ID |
| 3 | `deviceos` | `string` |  | iOS / Android |
| 4 | `appversion` | `string` |  | App 版本 |
| 5 | `eventid` | `string` |  | 埋点事件 ID |
| 6 | `occurtime` | `bigint` |  | 事件发生时间戳 (ms) |
| 7 | `postid` | `bigint` |  | 视频 ID (从顶层 itemId 取) |
| 8 | `raw_params` | `map<string, string>` |  | 原始 params, 字段定义待客户端 schema 同步后二期解析 |
| 9 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_video_icloud_download_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_video_icloud_download_di` |
| **描述** | 视频源 iCloud 下载事件明细 · 仅 iOS · 当前埋点仅上报失败/取消, 成功事件标识待客户端补充 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 19.8K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户 ID |
| 2 | `deviceudid` | `string` |  | 设备 ID |
| 3 | `appversion` | `string` |  | App 版本 |
| 4 | `event_time` | `bigint` |  | 事件时间戳 (ms) |
| 5 | `ext_type` | `int` |  | 失败类型: 1=主动取消失败 / 0=下载失败 |
| 6 | `result_status` | `string` |  | cancel / fail / null (成功或其他) |
| 7 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_video_play_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_video_play_di` |
| **描述** | 视频播放会话明细 · 按 reqId 聚合一次完整播放的全量事件 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9.3G |
| **是否分区表** | 是 |

### 字段详情

共 50 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `reqid` | `string` |  | 播放会话关联键 |
| 2 | `postid` | `bigint` |  | 视频 ID |
| 3 | `userid` | `bigint` |  | 消费用户 ID |
| 4 | `deviceudid` | `string` |  | 设备唯一标识 |
| 5 | `deviceos` | `string` |  | iOS / Android |
| 6 | `devicemodel` | `string` |  | 设备型号 |
| 7 | `appversion` | `string` |  | App 版本 |
| 8 | `appchannel` | `string` |  | App 渠道 |
| 9 | `dominant_quality` | `string` |  | 主档位, 格式 "宽x高" 如 720x1280 |
| 10 | `end_currentrate` | `string` |  | 本段视频结束的码率 |
| 11 | `player_type` | `string` |  | exoplayer / neliveplayer / AVPlayer |
| 12 | `scene` | `string` |  | 播放场景 |
| 13 | `alg_info` | `string` |  | 算法信息 |
| 14 | `rec_id` | `string` |  | 推荐 ID |
| 15 | `is_advertise` | `string` |  | 是否广告: 0=否 / 1=是 |
| 16 | `item_position` | `int` |  | 列表位置 |
| 17 | `video_url` | `string` |  | 视频链接 |
| 18 | `played_time_ms` | `bigint` |  | 实际播放时长 (毫秒) |
| 19 | `buffer_time_ms` | `bigint` |  | 累计缓冲时长 (毫秒) |
| 20 | `duration_ms` | `bigint` |  | 视频总时长 (毫秒) |
| 21 | `max_progress` | `double` |  | 最大播放进度 0~1 |
| 22 | `play_rate` | `double` |  | 本次播放倍速档位 (1.0/1.25/1.5/1.75/2.0/0.5/0.75) |
| 23 | `is_real` | `int` |  | 是否有效播放 (5 秒口径, played_time_ms > 5000): 0=否 / 1=是 · 与 dwd_post_browse_di.is_real 视频帖一致, 是视频域历史口径; 若需 3 秒口径 (effective_play_rate) 下游可基于 played_time_ms > 3000 自算 |
| 24 | `is_finished` | `int` |  | 是否完播 (max_progress>=0.9 OR played_time_ms/duration_ms>=0.9): 0=否 / 1=是 |
| 25 | `buffer_session_count` | `int` |  | 本会话发生卡顿次数 |
| 26 | `userseek_session_count` | `int` |  | 本会话用户拖动进度条次数 |
| 27 | `system_interrupt_count` | `int` |  | 本会话系统打断次数 (失去音频焦点等) |
| 28 | `inflate_error_count` | `int` |  | 本会话渲染错误次数 |
| 29 | `timer_interrupt_count` | `int` |  | 本会话定时器打断次数 |
| 30 | `start_event_time` | `bigint` |  | 起播事件时间戳 (ms) |
| 31 | `prepare_event_time` | `bigint` |  | 播放器准备完成时间戳 (ms) |
| 32 | `first_frame_event_time` | `bigint` |  | 首帧渲染完成时间戳 (ms) |
| 33 | `ttfr_ms` | `bigint` |  | TTFR 播放器准备时间 (ms) = prepare_event_time - start_event_time |
| 34 | `ttfp_ms` | `bigint` |  | TTFP 首帧渲染时间 (ms) = first_frame_event_time - start_event_time |
| 35 | `is_play_started` | `int` |  | 是否起播成功 (有首帧渲染事件): 0=否 / 1=是 |
| 36 | `is_play_failed` | `int` |  | 是否播放失败 (有错误事件): 0=否 / 1=是 |
| 37 | `error_type` | `string` |  | 错误分类: network / decode / source / unknown |
| 38 | `error_code` | `int` |  | 播放器错误码 |
| 39 | `error_retry_count` | `int` |  | 已重试次数 |
| 40 | `play_heartbeat_count` | `bigint` |  | 播放心跳次数 (播放停止/切换时上报) |
| 41 | `user_action_count` | `bigint` |  | 用户播放操作次数 (start/resume/pause/stop/seek 总和) |
| 42 | `player_state_change_count` | `bigint` |  | 播放器状态变更次数 (prepare/首帧/首次 buffer) |
| 43 | `player_error_count` | `bigint` |  | 播放器错误次数 |
| 44 | `quality_event_count` | `bigint` |  | 清晰度事件总次数 (含首次确定档位) |
| 45 | `quality_switch_count` | `bigint` |  | 清晰度真正切换次数 (不含首次确定) |
| 46 | `pause_count` | `bigint` |  | 用户暂停次数 |
| 47 | `resume_count` | `bigint` |  | 用户恢复播放次数 |
| 48 | `seek_count` | `bigint` |  | 用户拖动进度条次数 |
| 49 | `is_speedrate_used` | `int` |  | 本次会话是否使用过倍速 (play_rate > 1.0): 0=否 / 1=是 |
| 50 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_video_publish_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_video_publish_di` |
| **描述** | 视频发布明细 · dwd_post_publish_di (视频帖) JOIN dim_video_dd |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 22.1M |
| **是否分区表** | 是 |

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 视频 ID |
| 2 | `userid` | `bigint` |  | 发布者 ID |
| 3 | `blogid` | `bigint` |  | 创作者 ID |
| 4 | `publish_date` | `string` |  | 发布日期 yyyy-MM-dd |
| 5 | `publish_time` | `bigint` |  | 发布时间戳 (ms) |
| 6 | `video_type` | `int` |  | 3=站内原生 / 非3=站外 |
| 7 | `duration_sec` | `bigint` |  | 视频时长 (秒) |
| 8 | `size_bytes` | `bigint` |  | 文件大小 (字节) |
| 9 | `vid` | `bigint` |  | 视频中台 vid |
| 10 | `img_width` | `int` |  | 画幅宽 |
| 11 | `img_height` | `int` |  | 画幅高 |
| 12 | `aspect_ratio` | `string` |  | landscape / portrait / square |
| 13 | `origin_url` | `string` |  | 原始视频 URL |
| 14 | `hls_url` | `string` |  | HLS 流 URL |
| 15 | `h265_url` | `string` |  | H265 编码 URL |
| 16 | `video_first_img` | `string` |  | 首帧封面图 |
| 17 | `video_img_url` | `string` |  | 自定义封面 URL |
| 18 | `is_imported` | `int` |  | 是否导入 (从抖音/快手等外部平台搬运): 0=否 / 1=是 |
| 19 | `import_platform_type` | `string` |  | 导入平台 |
| 20 | `tags` | `array<string>` |  | 标签数组 |
| 21 | `ips` | `array<string>` |  | IP 圈层数组 |
| 22 | `domains` | `array<bigint>` |  | 一级领域数组 |
| 23 | `title` | `string` |  | 标题 |
| 24 | `caption` | `string` |  | 富文本内容 |
| 25 | `client_type` | `string` |  | 发布端类型 (iOS/Android/Web) |
| 26 | `is_user_first_post` | `int` |  | 是否当日用户首发: 0=否 / 1=是 |
| 27 | `is_pay_gift` | `int` |  | 是否礼物文章 (付费内容): 0=否 / 1=是 |
| 28 | `movefrom` | `string` |  | 规整后客户端: ios/android/web |
| 29 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_video_publish_funnel_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_video_publish_funnel_di` |
| **描述** | 视频上传发布漏斗明细 · 按 req_id 聚合从点击发布到接口成功的全流程 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 5.8M |
| **是否分区表** | 是 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `req_id` | `string` |  | 上传任务关联键 |
| 2 | `deviceudid` | `string` |  | 设备 ID |
| 3 | `userid` | `bigint` |  | 用户 ID |
| 4 | `deviceos` | `string` |  | iOS / Android |
| 5 | `devicemodel` | `string` |  | 设备型号 |
| 6 | `appversion` | `string` |  | App 版本 |
| 7 | `itemid` | `bigint` |  | 文章 ID (新建为空/0, 草稿/编辑有值) |
| 8 | `post_type` | `string` |  | 发布类型: postNew / postEdit / postReblog |
| 9 | `scene` | `string` |  | 场景 (固定 release_page) |
| 10 | `start_time` | `bigint` |  | 点击发布按钮时间戳 (ms) |
| 11 | `upload_start_time` | `bigint` |  | 开始上传素材时间戳 (ms) |
| 12 | `upload_progress_time` | `bigint` |  | 素材上传成功时间戳 (ms) |
| 13 | `success_time` | `bigint` |  | 发布成功时间戳 (ms) |
| 14 | `is_success` | `int` |  | 是否最终发布成功: 0=否 / 1=是 |
| 15 | `has_start` | `int` |  | 是否触发点击发布: 0=否 / 1=是 |
| 16 | `has_upload_start` | `int` |  | 是否触发开始上传: 0=否 / 1=是 |
| 17 | `has_progress` | `int` |  | 是否触发素材上传成功: 0=否 / 1=是 |
| 18 | `has_intercepted` | `int` |  | 是否被参数缺失拦截: 0=否 / 1=是 |
| 19 | `has_material_fail` | `int` |  | 是否素材上传失败: 0=否 / 1=是 |
| 20 | `has_api_fail` | `int` |  | 是否接口请求失败: 0=否 / 1=是 |
| 21 | `fail_stage` | `string` |  | 失败阶段: Success/ApiFail/MaterialFail/ParamIntercepted/StuckAtFinalCommit/StuckAtUploading/StuckAtInit/Unknown |
| 22 | `fail_msg` | `string` |  | 失败提示文本 |
| 23 | `fail_ext_code` | `string` |  | 扩展错误码 |
| 24 | `fail_top_code` | `string` |  | 顶层错误码 |
| 25 | `fail_category` | `string` |  | 失败分类: UserInteraction_or_Network / ServerError / ClientError |
| 26 | `total_duration_ms` | `bigint` |  | 总耗时 (ms) = success_time - start_time |
| 27 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_video_quality_event_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_video_quality_event_di` |
| **描述** | 视频清晰度切换事件明细 · 一行 = 一次切换 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 7.4G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `reqid` | `string` |  | 会话关联键 |
| 2 | `postid` | `bigint` |  | 视频 ID |
| 3 | `userid` | `bigint` |  | 消费用户 |
| 4 | `deviceudid` | `string` |  | 设备 ID |
| 5 | `deviceos` | `string` |  | iOS / Android |
| 6 | `devicemodel` | `string` |  | 设备型号 |
| 7 | `appversion` | `string` |  | App 版本 |
| 8 | `event_time` | `bigint` |  | 切换发生时间戳 (ms) |
| 9 | `scene` | `string` |  | 播放场景 |
| 10 | `switch_type` | `string` |  | 切换类型: init / manual / auto |
| 11 | `from_quality` | `string` |  | 切换前档位 "宽x高", 首次确定时为空 |
| 12 | `to_quality` | `string` |  | 切换后档位 "宽x高" |
| 13 | `direction` | `string` |  | 方向 (按高度比较): up / down / same / none |
| 14 | `current_position_ms` | `bigint` |  | 切换时播放进度 (ms) |
| 15 | `duration_in_from_quality_ms` | `bigint` |  | 在前一档位停留时长 (ms) |
| 16 | `estimated_bandwidth_kbps` | `bigint` |  | 估计下行带宽 (Kbps) |
| 17 | `buffer_duration_ms` | `bigint` |  | 切换时 buffer 余量 (ms) |
| 18 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dwd_vote_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_vote_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.6G |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `voteid` | `bigint` |  | 投票Id |
| 2 | `voteoptionid` | `bigint` |  | 投票选项Id |
| 3 | `userid` | `bigint` |  | 用户Id |
| 4 | `votecreatetime` | `bigint` |  | 投票记录创建时间 |
| 5 | `votedbupdatetime` | `bigint` |  |  |
| 6 | `blogid` | `bigint` |  | 博客Id |
| 7 | `optioncontent` | `string` |  | 投票选项 |
| 8 | `count` | `bigint` |  | 投票数量 |
| 9 | `number` | `bigint` |  | 序号 |
| 10 | `voteoptioncreatetime` | `bigint` |  | 投票选项的创建时间 |
| 11 | `voteoptiondbupdatetime` | `bigint` |  |  |
| 12 | `dt` | `string` |  |  |

---

## dwd_ycy_ad_user_device_actions_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dwd_ycy_ad_user_device_actions_di` |
| **描述** | 易次元广告数据 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 120.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 120.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  | 易次元:1L9KQGDL,Y8KJDFYR |
| 2 | `category` | `string` |  | 广告位,激励视频:REWARDVIDEO |
| 3 | `deviceudid` | `string` |  | 哈勃设备ID |
| 4 | `userid` | `bigint` |  |  |
| 5 | `positionid` | `string` |  | 广告位ID |
| 6 | `os` | `string` |  | 操作系统 |
| 7 | `adid` | `string` |  | 广告ID |
| 8 | `req_id` | `string` |  | 请求ID |
| 9 | `version` | `string` |  | 版本号 |
| 10 | `bgpv` | `bigint` |  | 曝光:0或1 |
| 11 | `clickpv` | `bigint` |  | 广告是否被点击:0或1 |
| 12 | `uuid` | `string` |  | 广告uuid:对应埋点req_uid |
| 13 | `bid_amount` | `double` |  | DSP价格=DSP出价*出价系数,单位为0.01元 |
| 14 | `fillcount` | `bigint` |  | DSP服务端是否填充 0或1 |
| 15 | `winpv` | `bigint` |  | DSP服务端竞得 0或1 |
| 16 | `positionname` | `string` |  | 广告位名字 |
| 17 | `location` | `string` |  |  |
| 18 | `dt` | `string` |  |  |

---

## dws_ab_platform_active_user_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_active_user_metric_di` |
| **描述** | 活跃用户的指标(当日活跃-回流-新用户) |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 2029.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2029.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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

## dws_ab_platform_ad_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_ad_metric_di` |
| **描述** | AB实验平台_广告指标明细表 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 661.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 661.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `base_date` | `string` |  | 实验基准日期 |
| 3 | `bucket_id` | `bigint` |  | 实验分组ID |
| 4 | `metric` | `string` |  | 指标名称 |
| 5 | `dimension` | `string` |  | 维度名称 |
| 6 | `dimension_value` | `string` |  | 维度值 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `exp_id` | `bigint` |  | 实验ID |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_client_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_client_metric_di` |
| **描述** | ab实验客户端指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 164.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 164.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_device_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_device_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 314.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 314.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceid` | `string` |  | 设备ID |
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

## dws_ab_platform_ecology_collection_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_ecology_collection_metric_di` |
| **描述** | ab实验合集指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 144.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 144.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_ecology_creator_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_ecology_creator_metric_di` |
| **描述** | ab实验指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 510.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 510.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_ecology_fullsite_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_ecology_fullsite_metric_di` |
| **描述** | ab实验指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 403.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 403.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_ecology_scene_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_ecology_scene_metric_di` |
| **描述** | ab实验合集指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 1385.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1385.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `scene` | `string` |  | 场景 |
| 5 | `dimension` | `string` |  | 维度 |
| 6 | `userid` | `bigint` |  | 用户id |
| 7 | `metric` | `string` |  | 指标名 |
| 8 | `metric_value` | `double` |  | 指标值 |
| 9 | `dimension_value` | `string` |  | 维度值 |
| 10 | `dt` | `string` |  |  |

---

## dws_ab_platform_exp10_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_exp10_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 129.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 129.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_exp8_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_exp8_metric_di` |
| **描述** | AB实验8原子指标表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 948.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 948.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 实验基准日期 |
| 2 | `dimension` | `string` |  | 维度 |
| 3 | `userid` | `bigint` |  | 用户id |
| 4 | `metric` | `string` |  | 指标名 |
| 5 | `metric_value` | `double` |  | 指标值 |
| 6 | `bucket_id` | `int` |  | 实验分桶ID |
| 7 | `dimension_value` | `string` |  | 维度值 |
| 8 | `dt` | `string` |  |  |

---

## dws_ab_platform_experiment_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_experiment_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 293.7M |
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
| 10 | `metricdetailid` | `bigint` |  | 实验指标ID |
| 11 | `metricdimid` | `bigint` |  | 实验指标维度ID |
| 12 | `dt` | `string` |  |  |

---

## dws_ab_platform_new_user_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_new_user_metric_di` |
| **描述** | 新用户的指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 498.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 498.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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

## dws_ab_platform_paycontent_membership_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_paycontent_membership_metric_di` |
| **描述** | AB实验平台_内容付费_会员指标明细表 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 177.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 177.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `base_date` | `string` |  | 实验基准日期 |
| 3 | `bucket_id` | `bigint` |  | 实验分组ID |
| 4 | `metric` | `string` |  | 指标名称 |
| 5 | `dimension` | `string` |  | 维度名称 |
| 6 | `dimension_value` | `string` |  | 维度值 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `exp_id` | `bigint` |  | 实验ID |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_paycontent_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_paycontent_metric_di` |
| **描述** | ab实验平台内容付费扩展总表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 52.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 52.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `type` | `string` |  | 类型 |
| 3 | `is_fans_vip` | `string` |  | 是否是高粉 |
| 4 | `unlock_method` | `string` |  | 付费解锁方式:null,券包,礼物 |
| 5 | `groupsetid` | `int` |  | grouping sets 分组id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dt` | `string` |  |  |

---

## dws_ab_platform_paycontent_metric_v2_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_paycontent_metric_v2_di` |
| **描述** | AB实验原子指标表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 3197.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3197.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  | 实验基准日期 |
| 2 | `dim_flag` | `int` |  | 维度 |
| 3 | `userid` | `bigint` |  | 用户id |
| 4 | `metric` | `string` |  | 指标名 |
| 5 | `metric_value` | `double` |  | 指标值 |
| 6 | `bucket_id` | `int` |  | 实验分桶ID |
| 7 | `exp_id` | `bigint` |  | 实验ID |
| 8 | `dt` | `string` |  |  |

---

## dws_ab_platform_paycontent_scene_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_paycontent_scene_metric_di` |
| **描述** | ab实验平台内容付费-场景原子指标表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 154.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 154.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `scene` | `string` |  | 场景名称 |
| 3 | `is_fans_vip` | `int` |  | 是否高粉,1:是,0:否 |
| 4 | `unlock_method` | `string` |  | 付费方式,null,券包,礼物 |
| 5 | `groupsetid` | `int` |  | grouping sets 分组id |
| 6 | `pay_amount` | `double` |  | 用户付费总金额 |
| 7 | `browse_pv` | `bigint` |  | 浏览数pv |
| 8 | `browse_uv` | `bigint` |  | 浏览uv |
| 9 | `pay_pv` | `bigint` |  | 付费pv |
| 10 | `pay_uv` | `bigint` |  | 付费uv |
| 11 | `pos_comment_pv` | `bigint` |  | 好评pv |
| 12 | `pos_comment_uv` | `bigint` |  | 好评uv |
| 13 | `neg_comment_pv` | `bigint` |  | 差评pv |
| 14 | `neg_comment_uv` | `bigint` |  | 差评uv |
| 15 | `dt` | `string` |  |  |

---

## dws_ab_platform_push_user_device_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_push_user_device_metric_di` |
| **描述** | ab实验Push用户和设备指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 233.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 233.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `deviceid` | `string` |  | 设备ID |
| 7 | `metric` | `string` |  | 指标名 |
| 8 | `metric_value` | `double` |  | 指标值 |
| 9 | `dimension_value` | `string` |  | 维度值 |
| 10 | `dt` | `string` |  |  |

---

## dws_ab_platform_pve_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_pve_metric_di` |
| **描述** | ab实验平pve原子指标表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 76.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 76.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `chats_pv` | `bigint` |  | 聊天轮数 |
| 3 | `chats_uv` | `bigint` |  | 聊天人数 |
| 4 | `roles_uv` | `bigint` |  | 聊天角色数 |
| 5 | `pay_money` | `double` |  | 总付费 |
| 6 | `pay_uv` | `bigint` |  | 付费人数 |
| 7 | `visit_uv` | `bigint` |  | 访问人数 |
| 8 | `dt` | `string` |  |  |

---

## dws_ab_platform_pve_metric_expand_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_pve_metric_expand_di` |
| **描述** | ab实验平台pve的指标汇总展开表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 57.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 57.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `type` | `string` |  | 指标名前缀,pve |
| 3 | `metric` | `string` |  | 指标名 |
| 4 | `metric_value` | `double` |  | 指标值 |
| 5 | `dt` | `string` |  |  |

---

## dws_ab_platform_return_user_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_return_user_metric_di` |
| **描述** | 回流用户的指标 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 212.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 212.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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

## dws_ab_platform_rewardcenter_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_rewardcenter_metric_di` |
| **描述** | ab实验指标-权益中心 |
| **Owner** | bdms_wb.wangwei56 |
| **表类型** | external |
| **表大小** | 316.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 316.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `exp_id` | `int` |  | 实验ID |
| 2 | `bucket_id` | `int` |  | 实验分桶ID |
| 3 | `basedate` | `string` |  | 实验基准日期 |
| 4 | `dimension` | `string` |  | 维度 |
| 5 | `userid` | `bigint` |  | 用户id |
| 6 | `metric` | `string` |  | 指标名 |
| 7 | `metric_value` | `double` |  | 指标值 |
| 8 | `dimension_value` | `string` |  | 维度值 |
| 9 | `dt` | `string` |  |  |

---

## dws_ab_platform_user_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ab_platform_user_metric_di` |
| **描述** | 不区分新用户 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 8074.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 8074.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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

## dws_act_card_cvr_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_act_card_cvr_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5.5G |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `activityid` | `string` |  | 活动id |
| 4 | `deviceos` | `string` |  | 系统 |
| 5 | `scene` | `string` |  | 场景 |
| 6 | `visit_pv` | `bigint` |  | 访问次数 |
| 7 | `click_navigate_pv` | `bigint` |  | 导航栏点击次数 |
| 8 | `click_lottery_pv` | `bigint` |  | 点击抽卡次数 |
| 9 | `click_share_pv` | `bigint` |  | 点击分享次数 |
| 10 | `click_shatter_pv` | `bigint` |  | 碎卡次数 |
| 11 | `order_pv` | `bigint` |  | 提交订单次数 |
| 12 | `confirm_deliver_pv` | `bigint` |  | 确认发货次数 |
| 13 | `amount` | `double` |  | 抽赏金额 |
| 14 | `dt` | `string` |  |  |

---

## dws_act_tag_big_event_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_act_tag_big_event_di` |
| **描述** | 圈层大事件 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 12.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 12.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | tag名称 |
| 2 | `userid` | `bigint` |  | 用户或创作者ID |
| 3 | `reach_date` | `string` |  | 事件达成日期 |
| 4 | `type` | `int` |  |  |
| 5 | `dt` | `string` |  | 日期分区 |

---

## dws_act_tag_honor_name_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_act_tag_honor_name_di` |
| **描述** | 圈层荣誉 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 3.0M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | tag名称 |
| 2 | `type` | `int` |  | 类型枚举值 |
| 3 | `honor_name` | `string` |  | 荣誉名称 |
| 4 | `dt` | `string` |  | 日期分区 |

---

## dws_act_tag_ship_score_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_act_tag_ship_score_di` |
| **描述** | tag嗑力值T+1增量表 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 59.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 59.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | tag名称 |
| 2 | `userid` | `bigint` |  |  |
| 3 | `cnt` | `bigint` |  | 嗑力值 |
| 4 | `dt` | `string` |  | 日期分区 |

---

## dws_anti_spam_user_behavior_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_anti_spam_user_behavior_nd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 532.3M |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `optype` | `string` |  | 抄送回调类型,comment,message,post_version |
| 3 | `flag` | `string` |  | normal or mask, null表示该行为的汇总 |
| 4 | `count_acc` | `bigint` |  | 该行为累积量 |
| 5 | `count_1d` | `bigint` |  | 该行为当日量 |
| 6 | `count_7d` | `bigint` |  | 该行为7日量 |
| 7 | `count_30d` | `bigint` |  | 该行为30日量 |

---

## dws_c2c_product_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_c2c_product_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3.4M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `productid` | `bigint` |  | 小黄车商品id |
| 2 | `blogid` | `bigint` |  | 创作者/卖家id |
| 3 | `sale_num_std` | `bigint` |  | 商品累计销量 |
| 4 | `blog_fans_std` | `bigint` |  | 创作者累计粉丝数 |
| 5 | `dt` | `string` |  |  |

---

## dws_category_user_consume_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_category_user_consume_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 16.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 16.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `category` | `string` |  | 类目 包含一、二、三级类目 多级类目形式如:c1-c2-c3 c1-c2 |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `post_30d` | `bigint` |  | 30日消费文章数 |
| 4 | `post_15d` | `bigint` |  | 15日消费文章数 |
| 5 | `post_7d` | `bigint` |  | 7日消费文章数 |
| 6 | `dt` | `string` |  |  |

---

## dws_category_user_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_category_user_consume_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5878.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5878.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `category` | `string` |  | 类目 包含一、二、三级类目 多级类目形式如:c1-c2-c3 c1-c2 |
| 2 | `userid` | `bigint` |  | 消费用户Id |
| 3 | `pv` | `bigint` |  | 浏览内容次数 |
| 4 | `post_count` | `bigint` |  | 浏览文章数 |
| 5 | `post_bitmap` | `varbinary(2147483647)` |  | 文章id位图 |
| 6 | `dt` | `string` |  |  |

---

## dws_collection_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_collection_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 12149.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 12149.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 48 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `collectionid` | `bigint` |  | 合集Id |
| 2 | `tags` | `string` |  | 合集所带标签 |
| 3 | `blogid` | `bigint` |  | 合集的博客Id |
| 4 | `cover_url` | `string` |  | 合集封面链接 |
| 5 | `name` | `string` |  | 合集名称 |
| 6 | `createtime` | `bigint` |  | 合集创建时间 |
| 7 | `lastpublishtime` | `bigint` |  | 合集最近更新时间 |
| 8 | `status` | `int` |  | 合集状态，0：正常，-1：被封禁 |
| 9 | `collectiontype` | `int` |  | 合集类型，0：普通合集，1：共创合集 |
| 10 | `onlyvideo` | `bigint` |  | 是否小剧场合集：0否1是 |
| 11 | `creatorcount` | `bigint` |  | 合集共创者人数 |
| 12 | `postcount` | `bigint` |  | 合集文章数 |
| 13 | `postcollectionhot` | `bigint` |  | 合集热度 |
| 14 | `viewcount` | `bigint` |  | 合集浏览数 |
| 15 | `blog_level` | `string` |  | 合集创作者的博客等级 |
| 16 | `collection_post_type` | `string` |  | 合集归属的文章类型：文字，图片，视频，其他 |
| 17 | `subscribe_uv_acc` | `bigint` |  | 合集订阅累计用户 |
| 18 | `subscribe_uv_1d` | `bigint` |  | 今天订阅合集用户数 |
| 19 | `subscribe_uv_7d` | `bigint` |  | 近7天订阅合集用户数 |
| 20 | `subscribe_uv_30d` | `bigint` |  | 近30天订阅合集用户数 |
| 21 | `update_post_1d` | `bigint` |  | 今日合集更新文章数 |
| 22 | `update_post_7d` | `bigint` |  | 近7日合集更新文章数 |
| 23 | `update_post_30d` | `bigint` |  | 近30日合集更新文章数 |
| 24 | `is_recommend` | `int` |  | 合集是否过推荐审核 |
| 25 | `hot_7d` | `bigint` |  | 近7日新增热度 |
| 26 | `hot_1k_post` | `bigint` |  | 千热文章数 |
| 27 | `hot_1w_post` | `bigint` |  | 万热文章数 |
| 28 | `browse_user_bitmap` | `varbinary(2147483647)` |  | 浏览用户位图 |
| 29 | `recommend_user_bitmap` | `varbinary(2147483647)` |  | 推荐用户位图 |
| 30 | `is_stable_update` | `int` |  | 是否稳定更新 1是 0否 |
| 31 | `browse_uv_7d` | `bigint` |  | 近7日浏览用户数 |
| 32 | `underscore_comment_pv` | `bigint` |  | 划线评数量 |
| 33 | `sweet_pv` | `bigint` |  | 甜标记数 |
| 34 | `bitter_pv` | `bigint` |  | 虐标记数 |
| 35 | `subscribe_uv_3d` | `bigint` |  | 近3日订阅合集用户数 |
| 36 | `detail_browse_uv_3d` | `bigint` |  | 近3日合集详情页浏览人数 |
| 37 | `subscribe_uv_90d` | `bigint` |  | 近90天订阅合集用户数 |
| 38 | `subscribe_bitmap_7d` | `varbinary(2147483647)` |  | 近7天订阅合集用户位图 |
| 39 | `subscribe_bitmap_30d` | `varbinary(2147483647)` |  | 近30天订阅合集用户位图 |
| 40 | `subscribe_bitmap_90d` | `varbinary(2147483647)` |  | 近90天订阅合集用户位图 |
| 41 | `subscribe_real_browse_bitmap_7d` | `varbinary(2147483647)` |  | 近7天合集订阅者有效阅读用户位图 |
| 42 | `subscribe_real_browse_bitmap_30d` | `varbinary(2147483647)` |  | 近30天合集订阅者有效阅读用户位图 |
| 43 | `subscribe_real_browse_bitmap_90d` | `varbinary(2147483647)` |  | 近90天合集订阅者有效阅读用户位图 |
| 44 | `subscribe_real_hd_bitmap_7d` | `varbinary(2147483647)` |  | 近7天合集订阅者有效互动用户位图 |
| 45 | `subscribe_real_hd_bitmap_30d` | `varbinary(2147483647)` |  | 近30天合集订阅者有效互动用户位图 |
| 46 | `subscribe_real_hd_bitmap_90d` | `varbinary(2147483647)` |  | 近90天合集订阅者有效互动用户位图 |
| 47 | `subscribe_bitmap_std` | `varbinary(2147483647)` |  | 累计订阅合集用户位图 |
| 48 | `dt` | `string` |  |  |

---

## dws_collection_revisit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_collection_revisit_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 5.6G |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `baseday` | `string` |  | 基准日 |
| 2 | `collectionid` | `bigint` |  | 合集ID |
| 3 | `is_collection_new_user` | `int` |  | 是否为合集拉新新用户 |
| 4 | `realbrowse_uv` | `bigint` |  | 全场景有效浏览uv |
| 5 | `realbrowse_uv_revisit_7d` | `bigint` |  | 全场景7日内复访有效uv |
| 6 | `realbrowse_uv_mysubscription` | `bigint` |  | 我的订阅来源有效uv |
| 7 | `realbrowse_uv_mysubscription_revisit_7d` | `bigint` |  | 我的订阅来源7日内复访有效uv |
| 8 | `realbrowse_uv_subscribe` | `bigint` |  | 订阅页来源有效uv |
| 9 | `realbrowse_uv_subscribe_revisit_7d` | `bigint` |  | 订阅页来源7日内复访有效uv |
| 10 | `is_collection_return_user` | `int` |  | 是否为合集回流用户 |
| 11 | `dt` | `string` |  |  |

---

## dws_collection_scene_agg_bm_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_collection_scene_agg_bm_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 132.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 132.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 35 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blog_level` | `string` |  | 文章对应的合集ID |
| 2 | `collection_post_type` | `string` |  | 合集文章类型 |
| 3 | `scene` | `string` |  | 场景 |
| 4 | `source1_scene` | `string` |  | 前一步场景 |
| 5 | `source2_scene` | `string` |  | 前二步场景 |
| 6 | `is_collection_new_user` | `int` |  | 是否为合集拉新新用户 |
| 7 | `expose_pv` | `bigint` |  | 曝光pv |
| 8 | `realbrowse_pv` | `bigint` |  | 有效浏览pv |
| 9 | `praise_pv` | `bigint` |  | 点赞pv |
| 10 | `recommend_pv` | `bigint` |  | 推荐pv |
| 11 | `reproduce_pv` | `bigint` |  | 转载pv |
| 12 | `subscribe_pv` | `bigint` |  | 订阅pv |
| 13 | `comment_pv` | `bigint` |  | 评论pv |
| 14 | `share_pv` | `bigint` |  | 分享pv |
| 15 | `e2b_pv` | `bigint` |  | 曝光到有效浏览转化pv |
| 16 | `expose_uv_bitmap` | `varbinary(2147483647)` |  | 曝光用户位图 |
| 17 | `realbrowse_uv_bitmap` | `varbinary(2147483647)` |  | 有效浏览用户位图 |
| 18 | `praise_uv_bitmap` | `varbinary(2147483647)` |  | 点赞用户位图 |
| 19 | `recommend_uv_bitmap` | `varbinary(2147483647)` |  | 推荐用户位图 |
| 20 | `reproduce_uv_bitmap` | `varbinary(2147483647)` |  | 转载用户位图 |
| 21 | `subscribe_uv_bitmap` | `varbinary(2147483647)` |  | 订阅用户位图 |
| 22 | `comment_uv_bitmap` | `varbinary(2147483647)` |  | 评论用户位图 |
| 23 | `share_uv_bitmap` | `varbinary(2147483647)` |  | 分享用户位图 |
| 24 | `e2b_uv_bitmap` | `varbinary(2147483647)` |  | 曝光到有效浏览转化用户位图 |
| 25 | `expose_collection_bitmap` | `varbinary(2147483647)` |  | 曝光合集位图 |
| 26 | `realbrowse_collection_bitmap` | `varbinary(2147483647)` |  | 有效浏览合集位图 |
| 27 | `praise_collection_bitmap` | `varbinary(2147483647)` |  | 点赞合集位图 |
| 28 | `recommend_collection_bitmap` | `varbinary(2147483647)` |  | 推荐合集位图 |
| 29 | `reproduce_collection_bitmap` | `varbinary(2147483647)` |  | 转载合集位图 |
| 30 | `subscribe_collection_bitmap` | `varbinary(2147483647)` |  | 订阅合集位图 |
| 31 | `comment_collection_bitmap` | `varbinary(2147483647)` |  | 评论合集位图 |
| 32 | `share_collection_bitmap` | `varbinary(2147483647)` |  | 分享合集位图 |
| 33 | `e2b_collection_bitmap` | `varbinary(2147483647)` |  | 曝光到有效浏览转化合集位图 |
| 34 | `is_collection_return_user` | `int` |  | 是否为合集回流用户 |
| 35 | `dt` | `string` |  |  |

---

## dws_collection_scene_bm_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_collection_scene_bm_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 353.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 353.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `collectionid` | `bigint` |  | 文章对应的合集ID |
| 2 | `scene` | `string` |  | 场景 |
| 3 | `source1_scene` | `string` |  | 前一步场景 |
| 4 | `source2_scene` | `string` |  | 前二步场景 |
| 5 | `is_collection_new_user` | `int` |  | 是否为合集拉新新用户 |
| 6 | `expose_pv` | `bigint` |  | 曝光pv |
| 7 | `realbrowse_pv` | `bigint` |  | 有效浏览pv |
| 8 | `praise_pv` | `bigint` |  | 点赞pv |
| 9 | `recommend_pv` | `bigint` |  | 推荐pv |
| 10 | `reproduce_pv` | `bigint` |  | 转载pv |
| 11 | `subscribe_pv` | `bigint` |  | 订阅pv |
| 12 | `comment_pv` | `bigint` |  | 评论pv |
| 13 | `share_pv` | `bigint` |  | 分享pv |
| 14 | `e2b_pv` | `bigint` |  | 曝光到有效浏览转化pv |
| 15 | `expose_uv_bitmap` | `varbinary(2147483647)` |  | 曝光用户位图 |
| 16 | `realbrowse_uv_bitmap` | `varbinary(2147483647)` |  | 有效浏览用户位图 |
| 17 | `praise_uv_bitmap` | `varbinary(2147483647)` |  | 点赞用户位图 |
| 18 | `recommend_uv_bitmap` | `varbinary(2147483647)` |  | 推荐用户位图 |
| 19 | `reproduce_uv_bitmap` | `varbinary(2147483647)` |  | 转载用户位图 |
| 20 | `subscribe_uv_bitmap` | `varbinary(2147483647)` |  | 订阅用户位图 |
| 21 | `comment_uv_bitmap` | `varbinary(2147483647)` |  | 评论用户位图 |
| 22 | `share_uv_bitmap` | `varbinary(2147483647)` |  | 分享用户位图 |
| 23 | `e2b_uv_bitmap` | `varbinary(2147483647)` |  | 曝光到有效浏览转化用户位图 |
| 24 | `is_collection_return_user` | `int` |  | 是否为合集回流用户 |
| 25 | `dt` | `string` |  |  |

---

## dws_collection_subscribe_user_browse_latest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_collection_subscribe_user_browse_latest_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | internal |
| **表大小** | 1.5G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 操作用户ID |
| 2 | `collectionid` | `bigint` |  | 文章对应的合集ID |
| 3 | `postid` | `bigint` |  | 日志ID |
| 4 | `blogid` | `bigint` |  | 日志所属博客ID |
| 5 | `occurtime` | `bigint` |  | 事件发生时间 |
| 6 | `dt` | `string` |  |  |

---

## dws_creator_browse_users_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_creator_browse_users_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 596.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 596.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `userid` | `bigint` |  |  |
| 3 | `first_time` | `bigint` |  |  |
| 4 | `last_time` | `bigint` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dws_creator_gift_users_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_creator_gift_users_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 59.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 59.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `userid` | `bigint` |  |  |
| 3 | `is_first_browse` | `int` |  | 是否首次访问 |
| 4 | `is_nopay_user` | `int` |  | 是否历史非付费用户访问 |
| 5 | `browse_pv` | `bigint` |  | 有效浏览pv |
| 6 | `dt` | `string` |  |  |

---

## dws_creator_valid_detail_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_creator_valid_detail_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 7.0G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `is_valid_creator_365d` | `int` |  | 是否年度有效创作者 |
| 3 | `is_valid_creator_30d` | `int` |  | 是否月度有效创作者 |
| 4 | `post_count_365d` | `bigint` |  | 近365天发文数 |
| 5 | `sum_realpv_365d` | `bigint` |  | 近365天发文总有效pv |
| 6 | `avg_post_realpv_365d` | `bigint` |  | 近365天篇均有效pv |
| 7 | `hotpv_365d` | `bigint` |  | 近365天总热度 |
| 8 | `commentpv_365d` | `bigint` |  | 近365天总评论量 |
| 9 | `post_count_30d` | `bigint` |  | 近30天发文数 |
| 10 | `sum_realpv_30d` | `bigint` |  | 近30天发文总有效pv |
| 11 | `avg_post_realpv_30d` | `bigint` |  | 近30天篇均有效pv |
| 12 | `hotpv_30d` | `bigint` |  | 近30天总热度 |
| 13 | `commentpv_30d` | `bigint` |  | 近30天总评论量 |
| 14 | `premium_posts` | `bigint` |  | 优质内容数 |
| 15 | `dt` | `string` |  |  |

---

## dws_device_growth_dau_stratify_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_device_growth_dau_stratify_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 175.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 175.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `device_type` | `string` |  | 设备类型:近30日新增/近30日回流/持续活跃 |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然 |
| 4 | `is_paid_subscribe` | `string` |  | 是否付费订阅口令拉新 |
| 5 | `active_days_30d` | `bigint` |  | 近30日活跃天数 |
| 6 | `origin_channel` | `string` |  | 来源一级渠道 |
| 7 | `dt` | `string` |  |  |

---

## dws_device_ip_interest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_device_ip_interest_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 486.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 486.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `ip` | `string` |  | IP名称 |
| 3 | `categories` | `string` |  | 所属类目 ","分隔 |
| 4 | `device_type` | `string` |  | 设备类型:近30日新增/近30日回流/持续活跃 |
| 5 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然 |
| 6 | `is_paid_subscribe` | `int` |  | 是否付费订阅口令拉新 |
| 7 | `active_days_30d` | `int` |  | 近30日活跃天数 |
| 8 | `ip_realpv` | `bigint` |  | 当日该IP内容有效pv |
| 9 | `dt` | `string` |  |  |

---

## dws_device_tag_interest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_device_tag_interest_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1921.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1921.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `tag` | `string` |  | 标签名称 |
| 3 | `is_cmb_tag` | `int` |  | 是否为标签库中的标签 |
| 4 | `ips` | `string` |  | 所属IP ","分隔 |
| 5 | `categories` | `string` |  | 所属类目 ","分隔 |
| 6 | `device_type` | `string` |  | 设备类型:近30日新增/近30日回流/持续活跃 |
| 7 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然 |
| 8 | `is_paid_subscribe` | `int` |  | 是否付费订阅口令拉新 |
| 9 | `active_days_30d` | `int` |  | 近30日活跃天数 |
| 10 | `tag_realpv` | `bigint` |  | 当日该tag内容有效pv |
| 11 | `dt` | `string` |  |  |

---

## dws_deviceudid_new_sum_ratio_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_deviceudid_new_sum_ratio_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 16.3M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `is_paid_subscribe` | `bigint` |  | 是否付费订阅口令拉新 |
| 2 | `deviceudid_new_30d_sum` | `bigint` |  | 近30天新增设备总数 |
| 3 | `deviceudid_new_14d_sum` | `bigint` |  | 近14天新增设备总数 |
| 4 | `deviceudid_new_7d_sum` | `bigint` |  | 近7天新增设备总数 |
| 5 | `deviceudid_new_30d_act` | `bigint` |  | 近30天新增设备规模 |
| 6 | `deviceudid_new_14d_act` | `bigint` |  | 近14天拉新蓄水规模 |
| 7 | `deviceudid_new_7d_act` | `bigint` |  | 近7天拉新蓄水规模 |
| 8 | `new_30d_sum_ratio` | `double` |  | 近30天拉新蓄水率 |
| 9 | `new_14d_sum_ratio` | `double` |  | 近14天拉新蓄水率 |
| 10 | `new_7d_sum_ratio` | `double` |  | 近7天拉新蓄水率 |
| 11 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 12 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 13 | `proxy` | `string` |  | 渠道代理 |
| 14 | `device_type` | `string` |  |  |
| 15 | `is_ture_new_return` | `string` |  | 是否真新真回用户 |
| 16 | `dt` | `string` |  |  |

---

## dws_deviceudid_postid_core_act_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_deviceudid_postid_core_act_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 197.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 197.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `postid` | `bigint` |  |  |
| 3 | `post_content_type` | `string` |  | 内容类型 |
| 4 | `post_tags` | `array<string>` |  | 内容tag |
| 5 | `post_ips` | `array<string>` |  | 内容ip |
| 6 | `is_paid` | `bigint` |  | 是否付费 |
| 7 | `valid_pv` | `bigint` |  | 有效pv |
| 8 | `hot_pv` | `bigint` |  | 热度pv |
| 9 | `comment_pv` | `bigint` |  | 评论pv |
| 10 | `interact_pv` | `bigint` |  | 互动(热度/评论)pv |
| 11 | `dt` | `string` |  |  |

---

## dws_ecology_ai_infringe_blog_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ecology_ai_infringe_blog_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 64.4M |
| **是否分区表** | 是 |

### 字段详情

共 33 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客ID/创作者ID |
| 2 | `complaint_uv` | `bigint` |  | 总投诉UV |
| 3 | `complaint_user_uv` | `bigint` |  | 普通用户投诉UV（非合伙人） |
| 4 | `complaint_partner_uv` | `bigint` |  | 合伙人投诉UV |
| 5 | `complaint_uv_1d` | `bigint` |  | 近1天总投诉UV |
| 6 | `complaint_uv_3d` | `bigint` |  | 近3天总投诉UV |
| 7 | `complaint_uv_5d` | `bigint` |  | 近5天总投诉UV |
| 8 | `complaint_uv_7d` | `bigint` |  | 近7天总投诉UV |
| 9 | `complaint_uv_15d` | `bigint` |  | 近15天总投诉UV |
| 10 | `complaint_uv_30d` | `bigint` |  | 近30天总投诉UV |
| 11 | `complaint_uv_60d` | `bigint` |  | 近60天总投诉UV |
| 12 | `complaint_uv_90d` | `bigint` |  | 近90天总投诉UV |
| 13 | `complaint_user_uv_1d` | `bigint` |  | 近1天普通用户投诉UV |
| 14 | `complaint_user_uv_3d` | `bigint` |  | 近3天普通用户投诉UV |
| 15 | `complaint_user_uv_5d` | `bigint` |  | 近5天普通用户投诉UV |
| 16 | `complaint_user_uv_7d` | `bigint` |  | 近7天普通用户投诉UV |
| 17 | `complaint_user_uv_15d` | `bigint` |  | 近15天普通用户投诉UV |
| 18 | `complaint_user_uv_30d` | `bigint` |  | 近30天普通用户投诉UV |
| 19 | `complaint_user_uv_60d` | `bigint` |  | 近60天普通用户投诉UV |
| 20 | `complaint_user_uv_90d` | `bigint` |  | 近90天普通用户投诉UV |
| 21 | `complaint_partner_uv_1d` | `bigint` |  | 近1天合伙人投诉UV |
| 22 | `complaint_partner_uv_3d` | `bigint` |  | 近3天合伙人投诉UV |
| 23 | `complaint_partner_uv_5d` | `bigint` |  | 近5天合伙人投诉UV |
| 24 | `complaint_partner_uv_7d` | `bigint` |  | 近7天合伙人投诉UV |
| 25 | `complaint_partner_uv_15d` | `bigint` |  | 近15天合伙人投诉UV |
| 26 | `complaint_partner_uv_30d` | `bigint` |  | 近30天合伙人投诉UV |
| 27 | `complaint_partner_uv_60d` | `bigint` |  | 近60天合伙人投诉UV |
| 28 | `complaint_partner_uv_90d` | `bigint` |  | 近90天合伙人投诉UV |
| 29 | `first_complaint_date` | `string` |  | 创作者首次被投诉日期 |
| 30 | `last_complaint_date` | `string` |  | 创作者最新被投诉日期 |
| 31 | `rec_ai_recognition_cnt` | `bigint` |  | 创作者AI识别文章的数量 |
| 32 | `partner_judge_cnt` | `bigint` |  | 创作者被合伙人众裁的文章数量 |
| 33 | `dt` | `string` |  |  |

---

## dws_ecology_ai_infringe_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ecology_ai_infringe_post_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 373.3M |
| **是否分区表** | 是 |

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 博客ID/创作者ID |
| 3 | `complaint_uv` | `bigint` |  | 总投诉UV |
| 4 | `complaint_user_uv` | `bigint` |  | 普通用户投诉UV（非合伙人） |
| 5 | `complaint_partner_uv` | `bigint` |  | 合伙人投诉UV |
| 6 | `complaint_uv_1d` | `bigint` |  | 近1天总投诉UV |
| 7 | `complaint_uv_3d` | `bigint` |  | 近3天总投诉UV |
| 8 | `complaint_uv_5d` | `bigint` |  | 近5天总投诉UV |
| 9 | `complaint_uv_7d` | `bigint` |  | 近7天总投诉UV |
| 10 | `complaint_uv_15d` | `bigint` |  | 近15天总投诉UV |
| 11 | `complaint_uv_30d` | `bigint` |  | 近30天总投诉UV |
| 12 | `complaint_uv_60d` | `bigint` |  | 近60天总投诉UV |
| 13 | `complaint_uv_90d` | `bigint` |  | 近90天总投诉UV |
| 14 | `complaint_user_uv_1d` | `bigint` |  | 近1天普通用户投诉UV |
| 15 | `complaint_user_uv_3d` | `bigint` |  | 近3天普通用户投诉UV |
| 16 | `complaint_user_uv_5d` | `bigint` |  | 近5天普通用户投诉UV |
| 17 | `complaint_user_uv_7d` | `bigint` |  | 近7天普通用户投诉UV |
| 18 | `complaint_user_uv_15d` | `bigint` |  | 近15天普通用户投诉UV |
| 19 | `complaint_user_uv_30d` | `bigint` |  | 近30天普通用户投诉UV |
| 20 | `complaint_user_uv_60d` | `bigint` |  | 近60天普通用户投诉UV |
| 21 | `complaint_user_uv_90d` | `bigint` |  | 近90天普通用户投诉UV |
| 22 | `complaint_partner_uv_1d` | `bigint` |  | 近1天合伙人投诉UV |
| 23 | `complaint_partner_uv_3d` | `bigint` |  | 近3天合伙人投诉UV |
| 24 | `complaint_partner_uv_5d` | `bigint` |  | 近5天合伙人投诉UV |
| 25 | `complaint_partner_uv_7d` | `bigint` |  | 近7天合伙人投诉UV |
| 26 | `complaint_partner_uv_15d` | `bigint` |  | 近15天合伙人投诉UV |
| 27 | `complaint_partner_uv_30d` | `bigint` |  | 近30天合伙人投诉UV |
| 28 | `complaint_partner_uv_60d` | `bigint` |  | 近60天合伙人投诉UV |
| 29 | `complaint_partner_uv_90d` | `bigint` |  | 近90天合伙人投诉UV |
| 30 | `is_rec_ai_recognized` | `int` |  | 是否被算法AI识别(0=否, 1=是) |
| 31 | `is_partner_judged` | `int` |  | 是否被合伙人众裁(0=否, 1=是) |
| 32 | `dt` | `string` |  |  |

---

## dws_evt_login_user_last_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_evt_login_user_last_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1161.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1161.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `accountid` | `bigint` |  | 用户ID |
| 2 | `time` | `bigint` |  | 最近一次登录时间 |
| 3 | `clienttype` | `string` |  | 最近一次登录客户端类型 |
| 4 | `deviceudid` | `string` |  | 最近一次登录设备Id (customudid 服务端设备Id, 不同于哈勃设备Id) |
| 5 | `appversion` | `string` |  | 客户端版本 |
| 6 | `lasttime` | `bigint` |  | 上一次最近登陆时间 |
| 7 | `app_time` | `bigint` |  | app端最近一次登录时间 |
| 8 | `app_client_type` | `string` |  | app端最后一次登录客户端类型 |
| 9 | `dt` | `string` |  |  |

---

## dws_gift_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2546.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2546.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 31 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 该用户下的文章 |
| 2 | `title` | `string` |  | 文章标题 |
| 3 | `tags` | `array<string>` |  | 文章标签 |
| 4 | `url` | `string` |  | 文章url |
| 5 | `contenttype` | `string` |  | 文章类型 |
| 6 | `publishdate` | `string` |  | 文章发布时间 |
| 7 | `userid` | `bigint` |  | 开通付费用户id |
| 8 | `accept_gift_flag` | `int` |  | 接受礼物flag |
| 9 | `agree_day` | `string` |  | 同意开通礼物的时间 |
| 10 | `blogname` | `string` |  | 博客名称 |
| 11 | `blognickname` | `string` |  | 博客昵称 |
| 12 | `return_gift_ids` | `string` |  | 文章回礼礼物ids |
| 13 | `cp_type` | `string` |  | 是否为cp |
| 14 | `platform_type` | `string` |  | 平台类型 |
| 15 | `is_pay_return_gift` | `string` |  | 回礼类型标识 |
| 16 | `clickpv` | `bigint` |  | 文章点击量 |
| 17 | `clickuv` | `bigint` |  | 文章点击用户数 |
| 18 | `exposurepv` | `bigint` |  | 曝光数 |
| 19 | `exposureuv` | `bigint` |  | 曝光用户数 |
| 20 | `poshotpv` | `bigint` |  | 文章正向热度数 |
| 21 | `poshotuv` | `bigint` |  | 文章正向热度人数 |
| 22 | `hdpv` | `bigint` |  | 文章互动量,正向 |
| 23 | `hduv` | `bigint` |  | 文章互动人数 |
| 24 | `coin_num` | `bigint` |  |  |
| 25 | `gift_uv` | `bigint` |  |  |
| 26 | `num` | `bigint` |  |  |
| 27 | `new_sender_coin_num` | `bigint` |  |  |
| 28 | `return_gift_coin_num` | `bigint` |  |  |
| 29 | `new_sender_uv` | `bigint` |  |  |
| 30 | `return_gift_uv` | `bigint` |  |  |
| 31 | `dt` | `string` |  |  |

---

## dws_gift_post_premium_ip_scoring_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_ip_scoring_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 27.9M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | ip |
| 2 | `contenttype` | `string` |  | 内容类型 |
| 3 | `ispay_returngift` | `bigint` |  | 付费类型 |
| 4 | `days` | `string` |  | 生命周期 |
| 5 | `zonghe_score` | `double` |  | 总得分 |
| 6 | `dt` | `string` |  |  |

---

## dws_gift_post_premium_post_score_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_post_score_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6.1G |
| **是否分区表** | 是 |

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `blog_channel` | `int` |  | 博客来源渠道 0 UGC 1 PGC |
| 4 | `content_flg` | `string` |  | 文章类型 |
| 5 | `post_score_type` | `int` |  | 文章等级 1:优质；2：潜力； 3：劣质; |
| 6 | `post_score` | `double` |  | 文章分 |
| 7 | `furate` | `double` |  | 负反馈率 |
| 8 | `zhenrate` | `double` |  | 正反馈率 |
| 9 | `liulan_fufei_rate` | `double` |  | 付费转化率 |
| 10 | `hd_rate` | `double` |  | 互动率 |
| 11 | `realbrowse_rate` | `double` |  | 有效浏览转化率 |
| 12 | `jiesuo_pv` | `bigint` |  | 累计解锁次数 20241017加 |
| 13 | `jiesuo_uv` | `bigint` |  | 付费解锁人数 |
| 14 | `exposureuv` | `bigint` |  | 曝光人数 |
| 15 | `liulan_fufei_level` | `int` |  | 付费转化等级： 1:优 0:中 -1:差 |
| 16 | `hd_rate_level` | `int` |  | 互动率等级： 1:优 0:中 -1:差 |
| 17 | `realbrowse_rate_level` | `int` |  | 浏览转化率等级: 1:优 0:中 -1:差 |
| 18 | `zhenrate_level` | `int` |  | 好评率等级: 1:优 0:中 -1:差 |
| 19 | `furate_level` | `int` |  | 不满意率等级: 1:优 0:中 -1:差 |
| 20 | `jiesuo_money` | `double` |  | 累计解锁金额 |
| 21 | `fumian_flg` | `int` |  | 负面标: 0正常 1低质 2负面 |
| 22 | `fufankui_uv` | `bigint` |  | 负反馈人数 |
| 23 | `furate_ln` | `double` |  | 负反馈率指数 |
| 24 | `review_status` | `int` |  | 0/未标记；1/良好；-1/低质；-2/负面 |
| 25 | `type_dd` | `string` |  | 优质 低质 其他 |
| 26 | `model_flg` | `int` |  | 是否原模型命中：0不命中，1命中 |
| 27 | `bg_amount_flg` | `int` |  | 是否达标曝光及收益门槛：0不达标，1达标 |
| 28 | `dt` | `string` |  | 日期分区 |

---

## dws_gift_post_premium_scoring_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_scoring_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 402.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 402.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 35 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 标签 |
| 4 | `ips` | `array<string>` |  | ip圈层 |
| 5 | `url` | `string` |  | 文章链接 |
| 6 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 7 | `blogname` | `string` |  | 博客名 |
| 8 | `blognickname` | `string` |  | 博客昵称 |
| 9 | `styouzhiflg` | `int` |  | 是否生态优质文章 |
| 10 | `yinliuflg` | `int` |  | 是否回礼引流文章 |
| 11 | `review_status` | `int` |  | 回礼质量打标 |
| 12 | `contenttype` | `string` |  | 文章类型 |
| 13 | `exposureuv` | `bigint` |  | 曝光人次 |
| 14 | `qianbg_amount` | `double` |  | 千爆收益 |
| 15 | `liulan_fufei_rate` | `double` |  | 浏览付费转化 |
| 16 | `furate` | `double` |  | 负反馈转化 |
| 17 | `zhenrate` | `double` |  | 正反馈转化 |
| 18 | `publishdate` | `string` |  | 发文日期 |
| 19 | `blog_channel` | `int` |  | 博客来源类型 |
| 20 | `recommendflg` | `int` |  | 是否回礼优质打标 |
| 21 | `rec_reviewflg` | `int` |  | 是否通过推荐池人审 |
| 22 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 23 | `collectionid` | `bigint` |  | 所属合集id |
| 24 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 25 | `bg_flg` | `string` |  | 文章曝光人次档位 |
| 26 | `qianbg_amount_ln` | `double` |  | 千爆收益分值 |
| 27 | `liulan_fufei_rate_ln` | `double` |  | 浏览付费转化分值 |
| 28 | `furate_ln` | `double` |  | 负反馈转化分值 |
| 29 | `zhenrate_ln` | `double` |  | 正反馈转化分值 |
| 30 | `realbrowse_rate_ln` | `double` |  | 有效浏览转化分值 |
| 31 | `hd_rate_ln` | `double` |  | 互动转化分值 |
| 32 | `zonghe_score` | `double` |  | 综合权重分值 |
| 33 | `rk` | `bigint` |  | 排序 |
| 34 | `type` | `string` |  | 文章质量类型 |
| 35 | `dt` | `string` |  |  |

---

## dws_gift_post_premium_scoring_detail_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_scoring_detail_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 71.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 71.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 35 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 标签 |
| 4 | `ips` | `array<string>` |  | ip圈层 |
| 5 | `url` | `string` |  | 文章链接 |
| 6 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 7 | `blogname` | `string` |  | 博客名 |
| 8 | `blognickname` | `string` |  | 博客昵称 |
| 9 | `styouzhiflg` | `int` |  | 是否生态优质文章 |
| 10 | `yinliuflg` | `int` |  | 是否回礼引流文章 |
| 11 | `review_status` | `int` |  | 回礼质量打标 |
| 12 | `contenttype` | `string` |  | 文章类型 |
| 13 | `exposureuv` | `bigint` |  | 曝光人次 |
| 14 | `qianbg_amount` | `double` |  | 千爆收益 |
| 15 | `liulan_fufei_rate` | `double` |  | 浏览付费转化 |
| 16 | `furate` | `double` |  | 负反馈转化 |
| 17 | `zhenrate` | `double` |  | 正反馈转化 |
| 18 | `publishdate` | `string` |  | 发文日期 |
| 19 | `blog_channel` | `int` |  | 博客来源类型 |
| 20 | `recommendflg` | `int` |  | 是否回礼优质打标 |
| 21 | `rec_reviewflg` | `int` |  | 是否通过推荐池人审 |
| 22 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 23 | `collectionid` | `bigint` |  | 所属合集id |
| 24 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 25 | `bg_flg` | `string` |  | 文章曝光人次档位 |
| 26 | `qianbg_amount_ln` | `double` |  | 千爆收益分值 |
| 27 | `liulan_fufei_rate_ln` | `double` |  | 浏览付费转化分值 |
| 28 | `furate_ln` | `double` |  | 负反馈转化分值 |
| 29 | `zhenrate_ln` | `double` |  | 正反馈转化分值 |
| 30 | `realbrowse_rate_ln` | `double` |  | 有效浏览转化分值 |
| 31 | `hd_rate_ln` | `double` |  | 互动转化分值 |
| 32 | `zonghe_score` | `double` |  | 综合权重分值 |
| 33 | `rk` | `bigint` |  | 排序 |
| 34 | `jiesuo_uv` | `bigint` |  | 解锁人次 |
| 35 | `dt` | `string` |  |  |

---

## dws_gift_post_premium_scoring_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_scoring_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 70.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 70.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 38 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 标签 |
| 4 | `ips` | `array<string>` |  | ip圈层 |
| 5 | `url` | `string` |  | 文章链接 |
| 6 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 7 | `blogname` | `string` |  | 博客名 |
| 8 | `blognickname` | `string` |  | 博客昵称 |
| 9 | `styouzhiflg` | `int` |  | 是否生态优质文章 |
| 10 | `yinliuflg` | `int` |  | 是否回礼引流文章 |
| 11 | `review_status` | `int` |  | 回礼质量打标 |
| 12 | `contenttype` | `string` |  | 文章类型 |
| 13 | `exposureuv` | `bigint` |  | 曝光人次 |
| 14 | `qianbg_amount` | `double` |  | 千爆收益 |
| 15 | `liulan_fufei_rate` | `double` |  | 浏览付费转化 |
| 16 | `furate` | `double` |  | 负反馈转化 |
| 17 | `zhenrate` | `double` |  | 正反馈转化 |
| 18 | `publishdate` | `string` |  | 发文日期 |
| 19 | `blog_channel` | `int` |  | 博客来源类型 |
| 20 | `recommendflg` | `int` |  | 是否回礼优质打标 |
| 21 | `rec_reviewflg` | `int` |  | 是否通过推荐池人审 |
| 22 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 23 | `collectionid` | `bigint` |  | 所属合集id |
| 24 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 25 | `bg_flg` | `string` |  | 文章曝光人次档位 |
| 26 | `qianbg_amount_ln` | `double` |  | 千爆收益分值 |
| 27 | `liulan_fufei_rate_ln` | `double` |  | 浏览付费转化分值 |
| 28 | `furate_ln` | `double` |  | 负反馈转化分值 |
| 29 | `zhenrate_ln` | `double` |  | 正反馈转化分值 |
| 30 | `realbrowse_rate_ln` | `double` |  | 有效浏览转化分值 |
| 31 | `hd_rate_ln` | `double` |  | 互动转化分值 |
| 32 | `zonghe_score` | `double` |  | 综合权重分值 |
| 33 | `rk` | `bigint` |  | 排序 |
| 34 | `type` | `string` |  | 文章质量类型 |
| 35 | `fufei_zonghe_tiyan_level` | `int` |  | 付费侧综合标识，1是，0否 |
| 36 | `fufei_jieduan_level` | `int` |  | 付费侧截断文标识，1是，0否 |
| 37 | `fufei_st_level` | `int` |  | 付费侧生态标识，1是，0否 |
| 38 | `dt` | `string` |  |  |

---

## dws_gift_post_premium_scoring_v2_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_scoring_v2_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhuquan |
| **表类型** | external |
| **表大小** | 468.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 468.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 35 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 标签 |
| 4 | `ips` | `array<string>` |  | ip圈层 |
| 5 | `url` | `string` |  | 文章链接 |
| 6 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 7 | `blogname` | `string` |  | 博客名 |
| 8 | `blognickname` | `string` |  | 博客昵称 |
| 9 | `styouzhiflg` | `int` |  | 是否生态优质文章 |
| 10 | `yinliuflg` | `int` |  | 是否回礼引流文章 |
| 11 | `review_status` | `int` |  | 回礼质量打标 |
| 12 | `contenttype` | `string` |  | 文章类型 |
| 13 | `exposureuv` | `bigint` |  | 曝光人次 |
| 14 | `qianbg_amount` | `double` |  | 千爆收益 |
| 15 | `liulan_fufei_rate` | `double` |  | 浏览付费转化 |
| 16 | `furate` | `double` |  | 负反馈转化 |
| 17 | `zhenrate` | `double` |  | 正反馈转化 |
| 18 | `publishdate` | `string` |  | 发文日期 |
| 19 | `blog_channel` | `int` |  | 博客来源类型 |
| 20 | `recommendflg` | `int` |  | 是否回礼优质打标 |
| 21 | `rec_reviewflg` | `int` |  | 是否通过推荐池人审 |
| 22 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 23 | `collectionid` | `bigint` |  | 所属合集id |
| 24 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 25 | `bg_flg` | `string` |  | 文章曝光人次档位 |
| 26 | `qianbg_amount_ln` | `double` |  | 千爆收益分值 |
| 27 | `liulan_fufei_rate_ln` | `double` |  | 浏览付费转化分值 |
| 28 | `furate_ln` | `double` |  | 负反馈转化分值 |
| 29 | `zhenrate_ln` | `double` |  | 正反馈转化分值 |
| 30 | `realbrowse_rate_ln` | `double` |  | 有效浏览转化分值 |
| 31 | `hd_rate_ln` | `double` |  | 互动转化分值 |
| 32 | `zonghe_score` | `double` |  | 综合权重分值 |
| 33 | `rk` | `bigint` |  | 排序 |
| 34 | `type` | `string` |  | 文章质量类型 |
| 35 | `dt` | `string` |  |  |

---

## dws_gift_post_premium_scoring_v2_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_premium_scoring_v2_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhuquan |
| **表类型** | external |
| **表大小** | 75.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 75.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `tags` | `array<string>` |  | 标签 |
| 4 | `ips` | `array<string>` |  | ip圈层 |
| 5 | `url` | `string` |  | 文章链接 |
| 6 | `recomstatus` | `int` |  | 推荐状态：0初始 1推荐 -1不推荐 |
| 7 | `blogname` | `string` |  | 博客名 |
| 8 | `blognickname` | `string` |  | 博客昵称 |
| 9 | `styouzhiflg` | `int` |  | 是否生态优质文章 |
| 10 | `yinliuflg` | `int` |  | 是否回礼引流文章 |
| 11 | `review_status` | `int` |  | 回礼质量打标 |
| 12 | `contenttype` | `string` |  | 文章类型 |
| 13 | `exposureuv` | `bigint` |  | 曝光人次 |
| 14 | `qianbg_amount` | `double` |  | 千爆收益 |
| 15 | `liulan_fufei_rate` | `double` |  | 浏览付费转化 |
| 16 | `furate` | `double` |  | 负反馈转化 |
| 17 | `zhenrate` | `double` |  | 正反馈转化 |
| 18 | `publishdate` | `string` |  | 发文日期 |
| 19 | `blog_channel` | `int` |  | 博客来源类型 |
| 20 | `recommendflg` | `int` |  | 是否回礼优质打标 |
| 21 | `rec_reviewflg` | `int` |  | 是否通过推荐池人审 |
| 22 | `unlocktype` | `int` |  | 回礼解锁方式：0不限制，1仅高粉 |
| 23 | `collectionid` | `bigint` |  | 所属合集id |
| 24 | `provider_type` | `int` |  | 签约类型：-1未签约，0个人签约，1机构签约 |
| 25 | `bg_flg` | `string` |  | 文章曝光人次档位 |
| 26 | `qianbg_amount_ln` | `double` |  | 千爆收益分值 |
| 27 | `liulan_fufei_rate_ln` | `double` |  | 浏览付费转化分值 |
| 28 | `furate_ln` | `double` |  | 负反馈转化分值 |
| 29 | `zhenrate_ln` | `double` |  | 正反馈转化分值 |
| 30 | `realbrowse_rate_ln` | `double` |  | 有效浏览转化分值 |
| 31 | `hd_rate_ln` | `double` |  | 互动转化分值 |
| 32 | `zonghe_score` | `double` |  | 综合权重分值 |
| 33 | `rk` | `bigint` |  | 排序 |
| 34 | `type` | `string` |  | 文章质量类型 |
| 35 | `model_flg` | `int` |  | 是否原模型命中：0不命中，1命中 |
| 36 | `fufei_flg` | `int` |  | 是否高付费通道命中：0不命中，1命中 |
| 37 | `recommend_flg` | `int` |  | 是否推荐回礼优质打标命中：0不命中，1命中 |
| 38 | `bg_amount_flg` | `int` |  | 是否达标曝光及收益门槛：0不达标，1达标 |
| 39 | `dt` | `string` |  |  |

---

## dws_gift_post_return_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_post_return_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 67.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 67.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `return_gift_id` | `bigint` |  | 回礼id |
| 4 | `jiesuo_uv` | `bigint` |  | 累计解锁用户数 |
| 5 | `zhengfankui_uv` | `bigint` |  | 累计正反馈用户数 |
| 6 | `fufankui_uv` | `bigint` |  | 累计负反馈用户数 |
| 7 | `jiesuo_uv_day` | `bigint` |  | 当日解锁用户数 |
| 8 | `zhengfankui_uv_day` | `bigint` |  | 当日正反馈用户数 |
| 9 | `fufankui_uv_day` | `bigint` |  | 当日负反馈用户数 |
| 10 | `jiesuo_pv` | `bigint` |  | 累计解锁次数 20241017加 |
| 11 | `jiesuo_money` | `double` |  | 累计解锁金额 |
| 12 | `dt` | `string` |  |  |

---

## dws_gift_unlock_tag_interests_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_unlock_tag_interests_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 72.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 72.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `unlock_tag_contents` | `array<row<string,double,bigint>('tag','weight','tag_post_count')>` |  |  |
| 3 | `unlock_tag_interests` | `array<row<string,double,bigint>('tag','weight','tag_post_count')>` |  |  |
| 4 | `dt` | `string` |  |  |
| 5 | `period` | `int` |  |  |

---

## dws_gift_user_revenue_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_gift_user_revenue_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 923.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 923.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `stat_period` | `string` |  | 统计周期 |
| 3 | `gift_type` | `int` |  | 礼物类型：0免费,1付费 |
| 4 | `trade_num` | `bigint` |  | 交易次数 |
| 5 | `trade_money` | `double` |  | 交易实付金额 |
| 6 | `product_num` | `bigint` |  | 购买的商品数量 |
| 7 | `post_count` | `bigint` |  | 打赏的文章数 |
| 8 | `blog_count` | `bigint` |  | 打赏的博客数 |
| 9 | `trade_days` | `bigint` |  | 统计周期内的交易天数 |
| 10 | `dt` | `string` |  |  |

---

## dws_grain_post_creator_follower_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_grain_post_creator_follower_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 38.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 38.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `grainid` | `bigint` |  | 粮单Id |
| 2 | `postcount` | `bigint` |  | 粮单文章量 |
| 3 | `update_post_1d` | `bigint` |  | 近1日粮单更新的文章量 |
| 4 | `update_post_7d` | `bigint` |  | 近7日粮单更新的文章量 |
| 5 | `update_post_30d` | `bigint` |  | 近30日粮单更新的文章量 |
| 6 | `creator_cnt` | `bigint` |  | 粮单共创者数量 |
| 7 | `update_creator_1d` | `bigint` |  | 近1粮单更新的共创者数量 |
| 8 | `update_creator_7d` | `bigint` |  | 近7日粮单更新的共创者数量 |
| 9 | `update_creator_30d` | `bigint` |  | 近30日粮单更新的共创者数量 |
| 10 | `follower_cnt` | `bigint` |  | 粮单订阅量 |
| 11 | `update_follower_1d` | `bigint` |  | 近1日粮单更新的订阅量 |
| 12 | `update_follower_7d` | `bigint` |  | 近7日粮单更新的订阅量 |
| 13 | `update_follower_30d` | `bigint` |  | 近30日粮单更新的订阅量 |
| 14 | `dt` | `string` |  |  |

---

## dws_growth_content_new_level_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_growth_content_new_level_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 110.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 110.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  |  |
| 2 | `level` | `string` |  | 拉新&生态等级 |
| 3 | `grade` | `string` |  | 拉新等级 |
| 4 | `content_type` | `string` |  | 内容类型 |
| 5 | `title` | `string` |  | 内容标题 |
| 6 | `url` | `string` |  | 内容链接 |
| 7 | `publish_date` | `string` |  | 发布日期 |
| 8 | `ips` | `array<string>` |  | 内容ip |
| 9 | `collectionid_url` | `string` |  | 所在合集链接 |
| 10 | `is_book_store` | `int` |  | 是否书城文章 |
| 11 | `new_devices` | `bigint` |  | 当日拉新设备数 |
| 12 | `new_nature_devices` | `bigint` |  | 当日拉新自然设备数 |
| 13 | `new_devices_7d` | `bigint` |  | 周拉新设备数 |
| 14 | `new_devices_30d` | `bigint` |  | 月拉新设备数 |
| 15 | `return_devices` | `bigint` |  | 当日回流设备数 |
| 16 | `return_nature_devices` | `bigint` |  | 当日回流自然设备数 |
| 17 | `return_devices_7d` | `bigint` |  | 周回流设备数 |
| 18 | `return_devices_30d` | `bigint` |  | 月回流设备数 |
| 19 | `new_devices_std` | `bigint` |  | 累计拉新设备数(从20220101统计) |
| 20 | `return_devices_std` | `bigint` |  | 累计回流设备数(从20220101统计) |
| 21 | `post_hot` | `bigint` |  | 文章热度 |
| 22 | `current_year_new_devices_std` | `bigint` |  | 本年累计拉新设备数 |
| 23 | `current_year_return_devices_std` | `bigint` |  | 本年累计回流设备数 |
| 24 | `current_year_new_nature_devices_std` | `bigint` |  | 本年累计拉新自然设备数 |
| 25 | `current_year_return_nature_devices_std` | `bigint` |  | 本年累计回流自然设备数 |
| 26 | `dt` | `string` |  |  |

---

## dws_growth_device_ip_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_growth_device_ip_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 49.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 49.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 3 | `is_paid_subscribe` | `int` |  | 是否付费订阅拉新 |
| 4 | `post_ip` | `string` |  | 文章ip |
| 5 | `browse_post_bitmap` | `varbinary(2147483647)` |  | 浏览文章位图 |
| 6 | `dt` | `string` |  |  |

---

## dws_growth_vertical_category_crowd_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_growth_vertical_category_crowd_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 148.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 148.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `vertical_category` | `string` |  | 垂类人群 |
| 2 | `deviceudid` | `string` |  | 设备id |
| 3 | `month_valid_pv` | `bigint` |  | 月有效pv |
| 4 | `dt` | `string` |  |  |

---

## dws_hot_article_stat_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_hot_article_stat_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 45.5M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章Id |
| 2 | `blogid` | `bigint` |  | 文章对应的博客Id |
| 3 | `publishdate` | `string` |  | 文章发布日期 |
| 4 | `url` | `string` |  | 文章URL链接 |
| 5 | `source` | `string` |  | 文章来源，热门IP,非热门IP,原创 |
| 6 | `hdpv` | `bigint` |  | 互动量 |
| 7 | `score` | `double` |  | 文章得分，取互动量的对数 |
| 8 | `rk` | `int` |  | 等级内部的排名 |
| 9 | `level` | `string` |  | 等级，分为五级 |
| 10 | `flag` | `int` |  | 等级内部标识,level1-4的该字段为1,level5该字段0/1都有 |
| 11 | `dt` | `string` |  |  |

---

## dws_ip_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_consume_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 461.4M |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | IP名称 |
| 2 | `categories` | `string` |  | 所属类目 ","分隔 |
| 3 | `sum_realpv` | `string` |  | 总有效pv |
| 4 | `newuser_realpv` | `bigint` |  | 近30日新增设备有效pv |
| 5 | `returnuser_realpv` | `bigint` |  | 近30日回流设备有效pv |
| 6 | `1to7_realpv` | `bigint` |  | 月活1-7日持续活跃设备有效pv |
| 7 | `8to14_realpv` | `bigint` |  | 月活8-14日持续活跃设备有效pv |
| 8 | `15to28_realpv` | `bigint` |  | 月活15-28日持续活跃设备有效pv |
| 9 | `29to30_realpv` | `bigint` |  | 月活29-30日持续活跃设备有效pv |
| 10 | `sum_realuv` | `bigint` |  | 总有效uv |
| 11 | `newuser_realuv` | `bigint` |  | 近30日新增设备有效uv |
| 12 | `returnuser_realuv` | `bigint` |  | 近30日回流设备有效uv |
| 13 | `1to7_realuv` | `bigint` |  | 月活1-7日持续活跃设备有效uv |
| 14 | `8to14_realuv` | `bigint` |  | 月活8-14日持续活跃设备有效uv |
| 15 | `15to28_realuv` | `bigint` |  | 月活15-28日持续活跃设备有效uv |
| 16 | `29to30_realuv` | `bigint` |  | 月活29-30日持续活跃设备有效uv |
| 17 | `expose_pv` | `bigint` |  | 爆光量 |
| 18 | `pay_expose_pv` | `bigint` |  | 付费曝光量 |
| 19 | `dt` | `string` |  |  |

---

## dws_ip_creator_produce_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_creator_produce_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 14.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 14.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `interest_name` | `string` |  | 兴趣点名称 |
| 3 | `derivedflag` | `int` |  | 是否衍生 1是 0否 |
| 4 | `interest_post_count` | `bigint` |  | 兴趣点30日发文数 |
| 5 | `rk` | `bigint` |  | 兴趣点排序 |
| 6 | `dt` | `string` |  |  |

---

## dws_ip_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 998.2M |
| **是否分区表** | 是 |

### 字段详情

共 43 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  |  |
| 2 | `tags` | `array<string>` |  |  |
| 3 | `post_count` | `bigint` |  |  |
| 4 | `post_uv` | `bigint` |  |  |
| 5 | `free_post_count` | `bigint` |  |  |
| 6 | `photo_post_count` | `bigint` |  |  |
| 7 | `text_post_count` | `bigint` |  |  |
| 8 | `video_post_count` | `bigint` |  |  |
| 9 | `photo_post_uv` | `bigint` |  |  |
| 10 | `text_post_uv` | `bigint` |  |  |
| 11 | `video_post_uv` | `bigint` |  |  |
| 12 | `level_s_post_count` | `bigint` |  |  |
| 13 | `level_a_post_count` | `bigint` |  |  |
| 14 | `level_b_post_count` | `bigint` |  |  |
| 15 | `level_c_post_count` | `bigint` |  |  |
| 16 | `level_d_post_count` | `bigint` |  |  |
| 17 | `level_d_star_post_count` | `bigint` |  |  |
| 18 | `level_none_post_count` | `bigint` |  |  |
| 19 | `level_s_post_uv` | `bigint` |  |  |
| 20 | `level_a_post_uv` | `bigint` |  |  |
| 21 | `level_b_post_uv` | `bigint` |  |  |
| 22 | `level_c_post_uv` | `bigint` |  |  |
| 23 | `level_d_post_uv` | `bigint` |  |  |
| 24 | `level_d_star_post_uv` | `bigint` |  |  |
| 25 | `level_none_post_uv` | `bigint` |  |  |
| 26 | `hot` | `bigint` |  |  |
| 27 | `recommend_count` | `bigint` |  |  |
| 28 | `photo_hot` | `bigint` |  |  |
| 29 | `text_hot` | `bigint` |  |  |
| 30 | `video_hot` | `bigint` |  |  |
| 31 | `photo_recommend_count` | `bigint` |  |  |
| 32 | `text_recommend_count` | `bigint` |  |  |
| 33 | `video_recommend_count` | `bigint` |  |  |
| 34 | `expose_pv` | `bigint` |  |  |
| 35 | `real_browse_pv` | `bigint` |  |  |
| 36 | `premium_post_count` | `bigint` |  |  |
| 37 | `photo_premium_post_count` | `bigint` |  |  |
| 38 | `text_premium_post_count` | `bigint` |  |  |
| 39 | `video_premium_post_count` | `bigint` |  |  |
| 40 | `pay_post_count` | `bigint` |  | 可付费文章：礼物文章， 付费免费 或 仅付费 |
| 41 | `pay_gift_money_1d` | `double` |  | 当日礼物付费金额(消耗) |
| 42 | `pay_gift_uv_1d` | `bigint` |  | 当日礼物付费人数（消耗） |
| 43 | `dt` | `string` |  |  |

---

## dws_ip_growth_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_growth_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 圈层 |
| 2 | `new_bitmap_1d` | `varbinary(2147483647)` |  | 近1日拉新用户 |
| 3 | `new_bitmap_7d` | `varbinary(2147483647)` |  | 近7日拉新用户 |
| 4 | `new_bitmap_15d` | `varbinary(2147483647)` |  | 近15日拉新用户 |
| 5 | `new_bitmap_30d` | `varbinary(2147483647)` |  | 近30日拉新用户 |
| 6 | `dt` | `string` |  |  |

---

## dws_ip_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 122.6M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ips` | `string` |  | IP名称 |
| 2 | `categories` | `string` |  | 所属类目 ","分隔 |
| 3 | `sum_valid_interaction_uv` | `string` |  | 总有效互动数 |
| 4 | `sum_valid_interaction_pv` | `string` |  | 总有效互动pv |
| 5 | `recommend_uv` | `bigint` |  | 点击推荐uv |
| 6 | `recommend_pv` | `bigint` |  | 点击推荐pv |
| 7 | `valid_comment_uv` | `bigint` |  | 有效评论uv |
| 8 | `valid_comment_pv` | `bigint` |  | 有效评论pv |
| 9 | `share_uv` | `bigint` |  | 分享uv |
| 10 | `share_pv` | `bigint` |  | 分享pv |
| 11 | `dt` | `string` |  |  |

---

## dws_ip_life_cycle_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_life_cycle_dd` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 353.1M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | ip圈层 |
| 2 | `post_cnt_90d` | `bigint` |  | 近90日文章参与量 |
| 3 | `post_cnt_180_90d` | `bigint` |  | 近180-90日文章参与量 |
| 4 | `premium_post_cnt_90d` | `bigint` |  | 近90日优质文章参与量 |
| 5 | `premium_post_cnt_180_90d` | `bigint` |  | 近180-90日优质文章参与量 |
| 6 | `realuv_cnt_90d` | `bigint` |  | 近90日有效uv |
| 7 | `realuv_cnt_180_90d` | `bigint` |  | 近180-90日有效uv |
| 8 | `realpv_cnt_90d` | `bigint` |  | 近90日有效pv |
| 9 | `realpv_cnt_180_90d` | `bigint` |  | 近180-90日有效pv |
| 10 | `post_cnt_30d` | `bigint` |  | 近30日文章参与量 |
| 11 | `post_cnt_60_30d` | `bigint` |  | 近60-30日文章参与量 |
| 12 | `premium_post_cnt_30d` | `bigint` |  | 近30日优质文章参与量 |
| 13 | `premium_post_cnt_60_30d` | `bigint` |  | 近60-30日优质文章参与量 |
| 14 | `realuv_cnt_30d` | `bigint` |  | 近30日有效uv |
| 15 | `realuv_cnt_60_30d` | `bigint` |  | 近60-30日有效uv |
| 16 | `realpv_cnt_30d` | `bigint` |  | 近30日有效pv |
| 17 | `realpv_cnt_60_30d` | `bigint` |  | 近60-30日有效pv |
| 18 | `dt` | `string` |  |  |

---

## dws_ip_life_cycle_type_info_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_life_cycle_type_info_dd` |
| **描述** | ip圈层生命周期 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 310.2M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  |  |
| 2 | `derivedflag` | `bigint` |  | 是否衍生 |
| 3 | `ip_life_stage_30d` | `string` |  | ip圈层生命周期(近30天) |
| 4 | `ip_life_stage_90d` | `string` |  | ip圈层生命周期(近90天) |
| 5 | `ip_type` | `string` |  | 优质圈层&达标圈层(以近30天为基期) |
| 6 | `post_cnt_30d` | `bigint` |  | 近30日发文量 |
| 7 | `realuv_cnt_30d` | `bigint` |  | 近30日有效uv |
| 8 | `post_cnt_90d` | `bigint` |  | 近90日发文量 |
| 9 | `realuv_cnt_90d` | `bigint` |  | 近90日有效uv |
| 10 | `post_cnt_60_30d_ratio` | `double` |  | 近30日发文量环比增速 |
| 11 | `realuv_cnt_60_30d_ratio` | `double` |  | 近30日有效uv环比增速 |
| 12 | `premium_post_cnt_60_30d_ratio` | `double` |  | 近30日优质文章量环比增速 |
| 13 | `post_cnt_180_90d_ratio` | `double` |  | 近90日发文量环比增速 |
| 14 | `realuv_cnt_180_90d_ratio` | `double` |  | 近90日有效uv环比增速 |
| 15 | `premium_post_cnt_180_90d_ratio` | `double` |  | 近90日优质文章量环比增速 |
| 16 | `dt` | `string` |  |  |

---

## dws_ip_supply_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_supply_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 211.9M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | IP名称 |
| 2 | `categories` | `string` |  | 所属类目 ","分隔 |
| 3 | `post_cnt_1d` | `bigint` |  | 近1日发文数 |
| 4 | `post_cnt_7d` | `bigint` |  | 近7日发文数 |
| 5 | `post_cnt_15d` | `bigint` |  | 近15日发文数 |
| 6 | `post_cnt_30d` | `bigint` |  | 近30日发文数 |
| 7 | `creator_cnt_1d` | `bigint` |  | 近1日发文人数 |
| 8 | `creator_cnt_7d` | `bigint` |  | 近7日发文人数 |
| 9 | `creator_cnt_15d` | `bigint` |  | 近15日发文人数 |
| 10 | `creator_cnt_30d` | `bigint` |  | 近30日发文人数 |
| 11 | `valid_creator_cnt_30d` | `bigint` |  | 月有效创作者数 |
| 12 | `premium_post_cnt` | `bigint` |  | 优质内容数 |
| 13 | `derivedflag` | `int` |  | 1是，0否 |
| 14 | `pay_post_cnt_1d` | `bigint` |  | 当日付费内容发布量 |
| 15 | `pay_premium_post_cnt` | `bigint` |  | 付费优质内容量 |
| 16 | `dt` | `string` |  |  |

---

## dws_ip_top20_info_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_top20_info_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 541.1K |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `day` | `string` |  | 统计日期 |
| 2 | `ip` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## dws_ip_user_consume_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_user_consume_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 98.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 98.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 标签ip |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `post_30d` | `bigint` |  | 30日消费文章数 |
| 4 | `post_15d` | `bigint` |  | 15日消费文章数 |
| 5 | `post_7d` | `bigint` |  | 7日消费文章数 |
| 6 | `dt` | `string` |  |  |

---

## dws_ip_user_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_user_consume_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2119.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2119.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 标签Ip |
| 2 | `userid` | `bigint` |  | 消费用户Id |
| 3 | `pv` | `bigint` |  | 浏览内容次数 |
| 4 | `post_count` | `bigint` |  | 浏览文章数 |
| 5 | `post_bitmap` | `varbinary(2147483647)` |  | 浏览ip下文章id位图 |
| 6 | `pay_post_pv` | `bigint` |  | 付费文章浏览次数 |
| 7 | `pay_post_count` | `bigint` |  | 付费文章浏览文章数 |
| 8 | `dt` | `string` |  |  |

---

## dws_ip_user_life_cycle_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_user_life_cycle_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2672.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2672.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `ip` | `string` |  | IP圈层 |
| 3 | `user_type` | `string` |  | 圈层下用户所属类型 |
| 4 | `dt` | `string` |  |  |

---

## dws_ip_uv_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_uv_nd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 816.7M |
| **是否分区表** | 否 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 用户注册IP |
| 2 | `ipuv` | `bigint` |  | IP下的注册用户数,非匿名测试账号 |
| 3 | `ip3` | `string` |  | IP地址前三段 |
| 4 | `ip3uv` | `bigint` |  | 三段IP下的注册用户数 |

---

## dws_ip_valid_pv_rank_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_ip_valid_pv_rank_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 84.2M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  |  |
| 2 | `valid_pv` | `bigint` |  |  |
| 3 | `rn` | `bigint` |  |  |
| 4 | `dt` | `string` |  |  |

---

## dws_land_nonrec_itemid_retain_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_land_nonrec_itemid_retain_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 36.9M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `base_date` | `string` |  |  |
| 2 | `land_nonrec_itemid` | `bigint` |  |  |
| 3 | `land_nonrec_content_type` | `string` |  |  |
| 4 | `retain_new_device_count` | `bigint` |  | 新用户次留设备数 |
| 5 | `retain_return_device_count` | `bigint` |  | 回流设备次留设备数 |
| 6 | `dt` | `string` |  |  |

---

## dws_membership_cp_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_membership_cp_dd` |
| **描述** | 会员cp池 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 16.5M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | cp标签 |
| 2 | `zone` | `string` |  | 分区： vip 会员免费 pay 付费  |
| 3 | `post_count_std` | `bigint` |  | 累积文章数 |
| 4 | `post_count_7d` | `bigint` |  | 周新增文章数 |
| 5 | `hot` | `bigint` |  | 热度 |
| 6 | `dt` | `string` |  |  |

---

## dws_membership_ip_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_membership_ip_dd` |
| **描述** | 会员ip池 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 12.1M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 文章ip |
| 2 | `zone` | `string` |  | 分区： vip 会员免费 pay 付费  |
| 3 | `post_count_std` | `bigint` |  | 累积文章数 |
| 4 | `post_count_7d` | `bigint` |  | 周新增文章数 |
| 5 | `hot` | `bigint` |  | 热度 |
| 6 | `ip_type` | `string` |  | ip模块: ip 其他 |
| 7 | `dt` | `string` |  |  |

---

## dws_membership_post_score_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_membership_post_score_dd` |
| **描述** | 会员文章综合分 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 78.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 78.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `join_date` | `string` |  | 进入内容池日期 |
| 4 | `is_vip` | `int` |  | 是否会员免费: 1是 0否 |
| 5 | `is_premium` | `int` |  | 是否神作: 1是 0否 |
| 6 | `ips` | `array<string>` |  | 作品ip列表 |
| 7 | `tags` | `array<string>` |  | 作品标签 |
| 8 | `settlement_post_score` | `double` |  | 文章结算综合分 |
| 9 | `gift_post_score` | `double` |  | 文章回礼质量综合分 |
| 10 | `gift_rank_percentile` | `double` |  | 文章回礼同曝光档位排行分位 |
| 11 | `unlock_pv_30d` | `bigint` |  | 近30日解锁次数 |
| 12 | `hot_30d` | `bigint` |  | 近30日热度 |
| 13 | `hot` | `bigint` |  | 累积热度 |
| 14 | `is_vip_premium` | `int` |  | 是否会员专区神作 |
| 15 | `is_ip_premium` | `int` |  | 是否圈层神作 |
| 16 | `revenue_post_score` | `bigint` |  | 博客收入结算分 |
| 17 | `dt` | `string` |  |  |

---

## dws_miniprogram_post_order_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_miniprogram_post_order_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 39.0M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `miniid` | `int` |  | 所属小程序标识 |
| 2 | `postid` | `bigint` |  | 付时关联的文章id |
| 3 | `pay_orders` | `bigint` |  | 累计成交订单数 |
| 4 | `pay_amount` | `double` |  | 累计成交金额 |
| 5 | `pay_orders_7d` | `bigint` |  | 近7天成交订单数 |
| 6 | `pay_amount_7d` | `double` |  | 近7天成交金额 |
| 7 | `dt` | `string` |  |  |

---

## dws_page_note_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_page_note_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 245.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 245.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceos` | `string` |  | 设备Os |
| 2 | `appversion` | `string` |  | app版本 |
| 3 | `isrec` | `int` |  | 是否为推荐场景 |
| 4 | `post_content_type` | `string` |  | 文章类型 |
| 5 | `creator_level` | `string` |  | 创作者等级 |
| 6 | `browse_play_pv` | `bigint` |  | 播放PV+浏览PV |
| 7 | `browse_play_device_bitmap` | `varbinary(2147483647)` |  | 播放+浏览的设备明细 |
| 8 | `real_browse_play_pv` | `bigint` |  | 有效播放PV+有效浏览PV |
| 9 | `real_browse_play_device_bitmap` | `varbinary(2147483647)` |  | 有效播放+有效浏览的设备明细 |
| 10 | `real_browse_times` | `bigint` |  | 有效播放时长+有效浏览时长 |
| 11 | `real_browse_play_item_bitmap` | `varbinary(2147483647)` |  | 有效播放+有效浏览的文章明细 |
| 12 | `dt` | `string` |  |  |

---

## dws_page_note_source_scene_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_page_note_source_scene_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 385.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 385.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceos` | `string` |  | 设备Os |
| 2 | `appversion` | `string` |  | app版本 |
| 3 | `scene` | `string` |  | 单日志页浏览或播放的场景 |
| 4 | `post_content_type` | `string` |  | 文章类型 |
| 5 | `creator_level` | `string` |  | 创作者等级 |
| 6 | `browse_play_pv` | `bigint` |  | 播放PV+浏览PV |
| 7 | `browse_play_device_bitmap` | `varbinary(2147483647)` |  | 播放+浏览的设备明细 |
| 8 | `real_browse_play_pv` | `bigint` |  | 有效播放PV+有效浏览PV |
| 9 | `real_browse_play_device_bitmap` | `varbinary(2147483647)` |  | 有效播放+有效浏览的设备明细 |
| 10 | `real_browse_times` | `bigint` |  | 有效播放时长+有效浏览时长 |
| 11 | `real_browse_play_item_bitmap` | `varbinary(2147483647)` |  | 有效播放+有效浏览的文章明细 |
| 12 | `dt` | `string` |  |  |

---

## dws_page_scene_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_page_scene_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 7.3M |
| **是否分区表** | 是 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `scene` | `string` |  | 场景 |
| 2 | `page_pv` | `bigint` |  |  |
| 3 | `expose_pv` | `bigint` |  |  |
| 4 | `expose_uv` | `bigint` |  |  |
| 5 | `click_pv` | `bigint` |  |  |
| 6 | `click_uv` | `bigint` |  |  |
| 7 | `real_browse_pv` | `bigint` |  |  |
| 8 | `real_browse_uv` | `bigint` |  |  |
| 9 | `praise_pv` | `bigint` |  |  |
| 10 | `praise_uv` | `bigint` |  |  |
| 11 | `recommend_pv` | `bigint` |  |  |
| 12 | `recommend_uv` | `bigint` |  |  |
| 13 | `subscribe_pv` | `bigint` |  |  |
| 14 | `subscribe_uv` | `bigint` |  |  |
| 15 | `reproduce_pv` | `bigint` |  |  |
| 16 | `reproduce_uv` | `bigint` |  |  |
| 17 | `response_pv` | `bigint` |  |  |
| 18 | `response_uv` | `bigint` |  |  |
| 19 | `share_pv` | `bigint` |  |  |
| 20 | `share_uv` | `bigint` |  |  |
| 21 | `follow_pv` | `bigint` |  |  |
| 22 | `follow_uv` | `bigint` |  |  |
| 23 | `interaction_uv` | `bigint` |  |  |
| 24 | `avg_duration_seconds` | `double` |  | 平均会话时长 单位秒 |
| 25 | `dt` | `string` |  |  |

---

## dws_page_source_scene_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_page_source_scene_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.1G |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `scene` | `string` |  | 上一级来源场景 |
| 2 | `expose_pv` | `bigint` |  |  |
| 3 | `expose_uv` | `bigint` |  |  |
| 4 | `click_pv` | `bigint` |  |  |
| 5 | `click_uv` | `bigint` |  |  |
| 6 | `praise_pv` | `bigint` |  |  |
| 7 | `praise_uv` | `bigint` |  |  |
| 8 | `recommend_pv` | `bigint` |  |  |
| 9 | `recommend_uv` | `bigint` |  |  |
| 10 | `subscribe_pv` | `bigint` |  |  |
| 11 | `subscribe_uv` | `bigint` |  |  |
| 12 | `reproduce_pv` | `bigint` |  |  |
| 13 | `reproduce_uv` | `bigint` |  |  |
| 14 | `response_pv` | `bigint` |  |  |
| 15 | `response_uv` | `bigint` |  |  |
| 16 | `share_pv` | `bigint` |  |  |
| 17 | `share_uv` | `bigint` |  |  |
| 18 | `follow_pv` | `bigint` |  |  |
| 19 | `follow_uv` | `bigint` |  |  |
| 20 | `interaction_uv` | `bigint` |  |  |
| 21 | `dt` | `string` |  |  |

---

## dws_par_creator_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9020.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 9020.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 112 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `level` | `string` |  | 创作者等级 |
| 3 | `blog_url` | `string` |  | 首页url |
| 4 | `blog_nickname` | `string` |  | 昵称 |
| 5 | `is_cp` | `int` |  | 是否cp创作者 |
| 6 | `is_imported` | `int` |  | 是否引入创作者 |
| 7 | `import_platform_type` | `string` |  | 引入平台类型： 账号属性: MCN机构、知识公路、云音乐、抖音、快手等 |
| 8 | `is_in_contract` | `int` |  | 是否签约创作者 |
| 9 | `post_count_std` | `bigint` |  | 累积创作者发文数 |
| 10 | `post_count_180d` | `bigint` |  | 近180日创作者发文数 |
| 11 | `post_count_30d` | `bigint` |  | 近30日创作者发文数 |
| 12 | `post_first_date` | `string` |  | 首次发文日期 |
| 13 | `post_last_date` | `string` |  | 最近一次发文日期 |
| 14 | `post_top_categories` | `array<string>` |  | 主要发文3个类目 按占比从大到小顺序 |
| 15 | `post_top_domains` | `array<string>` |  | 主要发文3个领域 按占比从大到小顺序 |
| 16 | `post_top_ips` | `array<string>` |  | 主要发文3个ip 按占比从大到小顺序 |
| 17 | `post_top_tags` | `array<string>` |  | 主要发文3个tag 按占比从大到小顺序 |
| 18 | `fans_std` | `bigint` |  | 累积粉丝数 |
| 19 | `active_days_30d` | `bigint` |  | 近30天活跃天数 |
| 20 | `active_last_date` | `string` |  | 最近活跃日期 |
| 21 | `gift_open_time` | `bigint` |  | 首次开通礼物权限时间， 空值为未开通 |
| 22 | `fans_vip_open_time` | `bigint` |  | 高级粉丝开通时间 未开通为空 |
| 23 | `post_main_content_type` | `string` |  | 创作者主发文类型： 图片 文字 视频（按照发文量选择最大类型，相同发文量按此类型顺序选择第一个） |
| 24 | `gift_income_std` | `bigint` |  | 创作者累计礼物收益 |
| 25 | `post_count_365d` | `bigint` |  | 近365天发文 |
| 26 | `rec_post_count_30d` | `bigint` |  | 近30天发文且入推荐池文章数 |
| 27 | `rec_post_count_365d` | `bigint` |  | 近365天发文且入推荐池文章数 |
| 28 | `post_count_7d` | `bigint` |  | 近7天发文 |
| 29 | `post_count_90d` | `bigint` |  | 近90天发文 |
| 30 | `rec_post_count_7d` | `bigint` |  | 近7天发文且入推荐池文章数 |
| 31 | `rec_post_count_90d` | `bigint` |  | 近90天发文且入推荐池文章数 |
| 32 | `fans_receive_hot_cnt_7d` | `bigint` |  | 近7天粉丝贡献热度 |
| 33 | `fans_receive_hot_cnt_30d` | `bigint` |  | 近30天粉丝贡献热度 |
| 34 | `fans_receive_hot_cnt_90d` | `bigint` |  | 近90天粉丝贡献热度 |
| 35 | `fans_hd_uv_7d` | `bigint` |  | 近7天粉丝互动数 |
| 36 | `fans_hd_uv_30d` | `bigint` |  | 近30天粉丝互动数 |
| 37 | `fans_hd_uv_90d` | `bigint` |  | 近90天粉丝互动数 |
| 38 | `post_receive_hot_cnt_7d` | `bigint` |  | 近7天所有文章热度 |
| 39 | `post_receive_hot_cnt_30d` | `bigint` |  | 近30天所有文章热度 |
| 40 | `post_receive_hot_cnt_90d` | `bigint` |  | 近90天所有文章热度 |
| 41 | `post_hot_7d` | `bigint` |  | 近7天发布的文章内容热度 |
| 42 | `post_hot_30d` | `bigint` |  | 近30天发布的文章内容热度 |
| 43 | `post_hot_90d` | `bigint` |  | 近90天发布的文章内容热度 |
| 44 | `fans_real_browse_uv_7d` | `bigint` |  | 近7天粉丝有效阅读数 |
| 45 | `fans_real_browse_uv_30d` | `bigint` |  | 近30天粉丝有效阅读数 |
| 46 | `fans_real_browse_uv_90d` | `bigint` |  | 近90天粉丝有效阅读数 |
| 47 | `discovery_click_pv_7d` | `bigint` |  | 近7天发现页点击量 |
| 48 | `discovery_click_pv_30d` | `bigint` |  | 近30天发现页点击量 |
| 49 | `discovery_click_pv_90d` | `bigint` |  | 近90天发现页点击量 |
| 50 | `related_article_click_pv_7d` | `bigint` |  | 近7天相关文章点击量 |
| 51 | `related_article_click_pv_30d` | `bigint` |  | 近30天相关文章点击量 |
| 52 | `related_article_click_pv_90d` | `bigint` |  | 近90天相关文章点击量 |
| 53 | `tag_discovery_click_pv_7d` | `bigint` |  | 近7天tag发现页点击量 |
| 54 | `tag_discovery_click_pv_30d` | `bigint` |  | 近30天tag发现页点击量 |
| 55 | `tag_discovery_click_pv_90d` | `bigint` |  | 近90天tag发现页点击量 |
| 56 | `tag_new_click_pv_7d` | `bigint` |  | 近7天tag最新页点击量 |
| 57 | `tag_new_click_pv_30d` | `bigint` |  | 近30天tag最新页点击量 |
| 58 | `tag_new_click_pv_90d` | `bigint` |  | 近90天tag最新页点击量 |
| 59 | `tag_hot_click_pv_7d` | `bigint` |  | 近7天tag最热页点击量 |
| 60 | `tag_hot_click_pv_30d` | `bigint` |  | 近30天tag最热页点击量 |
| 61 | `tag_hot_click_pv_90d` | `bigint` |  | 近90天tag最热页点击量 |
| 62 | `collection_click_pv_7d` | `bigint` |  | 近7天合集点击量 |
| 63 | `collection_click_pv_30d` | `bigint` |  | 近30天合集点击量 |
| 64 | `collection_click_pv_90d` | `bigint` |  | 近90天合集点击量 |
| 65 | `attention_click_pv_7d` | `bigint` |  | 近7天attention点击量 |
| 66 | `attention_click_pv_30d` | `bigint` |  | 近30天attention点击量 |
| 67 | `attention_click_pv_90d` | `bigint` |  | 近90天attention点击量 |
| 68 | `collection_subscribe_uv_7d` | `bigint` |  | 近7天订阅合集用户数 |
| 69 | `collection_subscribe_uv_30d` | `bigint` |  | 近30天订阅合集用户数 |
| 70 | `collection_subscribe_uv_90d` | `bigint` |  | 近90天订阅合集用户数 |
| 71 | `collection_real_browse_uv_7d` | `bigint` |  | 近7天合集订阅者有效阅读数 |
| 72 | `collection_real_browse_uv_30d` | `bigint` |  | 近30天合集订阅者有效阅读数 |
| 73 | `collection_real_browse_uv_90d` | `bigint` |  | 近90天合集订阅者有效阅读数 |
| 74 | `collection_real_hd_uv_7d` | `bigint` |  | 近7天合集订阅者有效互动数 |
| 75 | `collection_real_hd_uv_30d` | `bigint` |  | 近30天合集订阅者有效互动数 |
| 76 | `collection_real_hd_uv_90d` | `bigint` |  | 近90天合集订阅者有效互动数 |
| 77 | `fans_cnt_7d` | `bigint` |  | 近7天粉丝增长数 |
| 78 | `fans_cnt_30d` | `bigint` |  | 近30天粉丝增长数 |
| 79 | `fans_cnt_90d` | `bigint` |  | 近90天粉丝增长数 |
| 80 | `infringe_post_cnt_7d` | `bigint` |  | 近7天被投诉成功数 |
| 81 | `infringe_post_cnt_30d` | `bigint` |  | 近30天被投诉成功数 |
| 82 | `infringe_post_cnt_90d` | `bigint` |  | 近90天被投诉成功数 |
| 83 | `receive_hot_std` | `bigint` |  | 累计收到的热度 |
| 84 | `receive_comment_cnt` | `bigint` |  | 累计收获评论数 |
| 85 | `receive_comment_cnt_7d` | `bigint` |  | 近7日收获评论数 |
| 86 | `receive_comment_cnt_15d` | `bigint` |  | 近15日收获评论数 |
| 87 | `receive_comment_cnt_30d` | `bigint` |  | 近30日收获评论数 |
| 88 | `collection_subscribe_uv` | `bigint` |  | 累计合集订阅用户数 |
| 89 | `fans_cnt` | `bigint` |  | 累计粉丝增长数 |
| 90 | `fans_cnt_365d` | `bigint` |  | 近365天粉丝增长数 |
| 91 | `hd_uv_7d` | `bigint` |  | 7日互动量 |
| 92 | `hd_uv_30d` | `bigint` |  | 30日互动量 |
| 93 | `hd_uv_90d` | `bigint` |  | 90日互动量 |
| 94 | `click_pv_7d` | `bigint` |  | 近7天点击量 |
| 95 | `click_pv_30d` | `bigint` |  | 近30天点击量 |
| 96 | `click_pv_90d` | `bigint` |  | 近90天点击量 |
| 97 | `post_count_1d` | `bigint` |  | 近1天发文 |
| 98 | `post_top10_ips` | `array<string>` |  | 主要发文10个ip 按占比从大到小顺序 |
| 99 | `post_top10_tags` | `array<string>` |  | 主要发文10个tag 按占比从大到小顺序 |
| 100 | `post_expose_uv_7d` | `bigint` |  | 近7d文章曝光uv |
| 101 | `post_expose_uv_30d` | `bigint` |  | 近30d文章曝光uv |
| 102 | `post_expose_uv_90d` | `bigint` |  | 近90d文章曝光uv |
| 103 | `post_click_uv_7d` | `bigint` |  | 近7d文章点击uv |
| 104 | `post_click_uv_30d` | `bigint` |  | 近30d文章点击uv |
| 105 | `post_click_uv_90d` | `bigint` |  | 近90d文章点击uv |
| 106 | `post_real_browse_uv_7d` | `bigint` |  | 近7d文章有效浏览uv |
| 107 | `post_real_browse_uv_30d` | `bigint` |  | 近30d文章有效浏览uv |
| 108 | `post_real_browse_uv_90d` | `bigint` |  | 近90d文章有效浏览uv |
| 109 | `post_support_induced_pv_7d` | `bigint` |  | 近7d文章扶持流量pv |
| 110 | `post_support_induced_pv_30d` | `bigint` |  | 近30d文章扶持流量pv |
| 111 | `post_support_induced_pv_90d` | `bigint` |  | 近90d文章扶持流量pv |
| 112 | `dt` | `string` |  |  |

---

## dws_par_creator_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 132.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 132.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 创作者用户Id |
| 2 | `post_count` | `bigint` |  | 日发文数 |
| 3 | `is_new_user` | `int` |  | 是否新注册发文用户 1是 0否 |
| 4 | `is_daren` | `int` |  | 是否达人 1是 0否 |
| 5 | `is_app_active` | `int` |  | 是否App活跃用户 1是 0否 |
| 6 | `is_first_publish` | `int` |  | 是否首次发文老用户 |
| 7 | `is_active_creator` | `int` |  | 是否持续发文用户。看上次发文是不是在30天内 是就是1 |
| 8 | `post_domains` | `array<string>` |  | 发文领域 |
| 9 | `new_post_domains` | `array<string>` |  | 新发文领域 |
| 10 | `return_post_domains` | `array<string>` |  | 回流创作领域 |
| 11 | `revenue` | `double` |  | 创作者收入 |
| 12 | `review_10post_avg_hot` | `double` |  | 近10篇入推荐池内容均热 |
| 13 | `exposure_pv` | `bigint` |  | 全部文章曝光数 |
| 14 | `click_pv` | `bigint` |  | 全部文章点击数 |
| 15 | `browse_pv` | `bigint` |  | 全部文章浏览 |
| 16 | `real_browse_play_pv` | `bigint` |  | 全部文章有效浏览播放数 |
| 17 | `real_browse_pv` | `bigint` |  | 全部文章有效浏览 |
| 18 | `play_pv` | `bigint` |  | 全部视频播放量 |
| 19 | `real_play_pv` | `bigint` |  | 全部视频有效播放量 |
| 20 | `creator_type` | `string` |  | 创作者类型 新注册发文用户/首次发文老用户/持续发文用户/回流发文用户 |
| 21 | `gift_income` | `bigint` |  | 创作者当日礼物收益 |
| 22 | `dt` | `string` |  |  |

---

## dws_par_creator_gift_level_scoring_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_gift_level_scoring_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.4G |
| **是否分区表** | 是 |

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `flg` | `string` |  | 付费创作类型： 文手/画手 |
| 3 | `level` | `int` |  | 付费创作等级 |
| 4 | `publish_activity_level` | `int` |  | 发文活跃等级：　 -1差 0中 1高 |
| 5 | `gift_conversion_level` | `int` |  | 付费表现等级：　 -1差 0中 1高 |
| 6 | `user_experience_level` | `int` |  | 消费体验等级：　 -1差 0中 1高 |
| 7 | `post_hot_level` | `int` |  | 文章表现等级：　 -1差 0中 1高 |
| 8 | `grade` | `int` |  | 付费创作者等级： 4高 3中 2低 |
| 9 | `post_score` | `double` |  | 文章综合分 |
| 10 | `post_score_percentile` | `double` |  | 文章分分位点 |
| 11 | `top5_post_count` | `bigint` |  | top5文章数 |
| 12 | `affinity_level` | `int` |  | 粘性等级 |
| 13 | `affinity_percentile` | `double` |  | 粘性分位点 |
| 14 | `top5_post_tags` | `array<string>` |  | 主要5个发文tag |
| 15 | `gift_avg_count` | `double` |  | 平均回礼数 |
| 16 | `gift_repeat_ratio` | `double` |  | 复赠占比 |
| 17 | `gift_subscribe_ratio` | `double` |  | 赠后订阅占比 |
| 18 | `gift_count` | `bigint` |  | 赠礼数量 |
| 19 | `gift_amount` | `double` |  | 赠礼金额 |
| 20 | `post_score_level` | `int` |  | 文章分等级 |
| 21 | `maneuver_flg` | `int` |  | 等级调控规则： 0: 30天负面文章>=2 文章分等级 <[正常] 1: 30天负面文章=1 文章分等级 <[优秀] 2: 30天优质文章=0 文章分等级 <[优秀] 3: 30分发布文章>0 但仅高粉订单收益>=20 文章分等级 >= [正常] 4: 30天发布文章且收益 空或者0 文章分等级 < [正常] |
| 22 | `month_settled_income` | `double` |  | 28天结算金额 |
| 23 | `dt` | `string` |  |  |

---

## dws_par_creator_gift_level_scoring_detail_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_gift_level_scoring_detail_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 478.8M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客id |
| 2 | `blackrank` | `int` |  | 小黑屋等级 |
| 3 | `flg` | `string` |  | 付费创作类型： 文手/画手 |
| 4 | `youzhi_score` | `double` |  | 优质分 |
| 5 | `qita_score` | `double` |  | 其他分 |
| 6 | `dizhi_num` | `bigint` |  | 低质文章数 |
| 7 | `youzhi_num` | `bigint` |  | 优质文章数 |
| 8 | `qita_num` | `bigint` |  | 其他文章数 |
| 9 | `fufankui_num` | `bigint` |  | 反馈文章数 |
| 10 | `zhenfankui_num` | `bigint` |  | 正反馈文章数 |
| 11 | `feijieduan_num` | `bigint` |  | 非截断文章数 |
| 12 | `dt` | `string` |  |  |

---

## dws_par_creator_interaction_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_interaction_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 357.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 357.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 45 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 创作者id |
| 2 | `receive_hot_std` | `bigint` |  | 累计热度 |
| 3 | `receive_like_cnt` | `bigint` |  | 累计点赞量 |
| 4 | `receive_reproduce_cnt` | `bigint` |  | 累计转载量 |
| 5 | `receive_recommend_cnt` | `bigint` |  | 累计推荐量 |
| 6 | `receive_collect_cnt` | `bigint` |  | 累计收藏量 |
| 7 | `receive_comment_cnt` | `bigint` |  | 累计评论量 |
| 8 | `hd_uv_std` | `bigint` |  | 累计互动量 |
| 9 | `hd_uv_1d` | `bigint` |  | 1日互动量 |
| 10 | `hd_uv_7d` | `bigint` |  | 7日互动量 |
| 11 | `hd_uv_30d` | `bigint` |  | 30日互动量 |
| 12 | `hd_uv_90d` | `bigint` |  | 90日互动量 |
| 13 | `receive_like_cnt_7d` | `bigint` |  | 7日收到的点赞量 |
| 14 | `receive_like_cnt_30d` | `bigint` |  | 30日收到的点赞量 |
| 15 | `receive_reproduce_cnt_7d` | `bigint` |  | 7日收到的转载量 |
| 16 | `receive_reproduce_cnt_30d` | `bigint` |  | 30日收到的转载量 |
| 17 | `receive_recommend_cnt_7d` | `bigint` |  | 7日收到的推荐量 |
| 18 | `receive_recommend_cnt_30d` | `bigint` |  | 30日收到的推荐量 |
| 19 | `receive_collect_cnt_7d` | `bigint` |  | 7日收到的收藏量 |
| 20 | `receive_collect_cnt_30d` | `bigint` |  | 30日收到的收藏量 |
| 21 | `receive_comment_cnt_7d` | `bigint` |  | 近7日收获评论数 |
| 22 | `receive_comment_cnt_30d` | `bigint` |  | 近30日收获评论数 |
| 23 | `receive_like_uv_30d` | `bigint` |  | 近30天收到喜欢用户数 |
| 24 | `receive_recommend_uv_30d` | `bigint` |  | 近30天收到推荐用户数 |
| 25 | `receive_reproduce_uv_30d` | `bigint` |  | 近30天收到转发用户数 |
| 26 | `receive_collect_uv_30d` | `bigint` |  | 近30天收到收藏用户数 |
| 27 | `last_like_userid_180d` | `bigint` |  | 近180天最后喜欢用户id |
| 28 | `receive_comment_cnt_15d` | `bigint` |  | 15日评论量 |
| 29 | `receive_like_cnt_15d` | `bigint` |  | 15日点赞量 |
| 30 | `receive_reproduce_cnt_15d` | `bigint` |  | 15日转载量 |
| 31 | `receive_recommend_cnt_15d` | `bigint` |  | 15日推荐量 |
| 32 | `receive_collect_cnt_15d` | `bigint` |  | 15日收藏量 |
| 33 | `fans_receive_hot_cnt_7d` | `bigint` |  | 近7天粉丝贡献热度 |
| 34 | `fans_receive_hot_cnt_30d` | `bigint` |  | 近30天粉丝贡献热度 |
| 35 | `fans_receive_hot_cnt_90d` | `bigint` |  | 近90天粉丝贡献热度 |
| 36 | `fans_hd_uv_7d` | `bigint` |  | 近7天粉丝互动量 |
| 37 | `fans_hd_uv_30d` | `bigint` |  | 近30天粉丝互动量 |
| 38 | `fans_hd_uv_90d` | `bigint` |  | 近90天粉丝互动量 |
| 39 | `post_receive_hot_cnt_7d` | `bigint` |  | 近7天所有文章热度 |
| 40 | `post_receive_hot_cnt_30d` | `bigint` |  | 近30天所有文章热度 |
| 41 | `post_receive_hot_cnt_90d` | `bigint` |  | 近90天所有文章热度 |
| 42 | `post_hot_7d` | `bigint` |  | 近7天发布的文章内容热度 |
| 43 | `post_hot_30d` | `bigint` |  | 近30天发布的文章内容热度 |
| 44 | `post_hot_90d` | `bigint` |  | 近90天发布的文章内容热度 |
| 45 | `dt` | `string` |  |  |

---

## dws_par_creator_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 10.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 10.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 38 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `text_receive_like_cnt` | `bigint` |  | 文本文章收获喜欢数 |
| 3 | `photo_receive_like_cnt` | `bigint` |  | 图片文章收获喜欢数 |
| 4 | `video_receive_like_cnt` | `bigint` |  | 视频文章收获喜欢数 |
| 5 | `article_receive_like_cnt` | `bigint` |  | 长文章收获喜欢数 |
| 6 | `music_receive_like_cnt` | `bigint` |  | 音乐文章收获喜欢数 |
| 7 | `question_receive_like_cnt` | `bigint` |  | 问答收获喜欢数 |
| 8 | `text_receive_reproduce_cnt` | `bigint` |  | 文本文章收获转载数 |
| 9 | `photo_receive_reproduce_cnt` | `bigint` |  | 图片文章收获转载数 |
| 10 | `video_receive_reproduce_cnt` | `bigint` |  | 视频文章收获转载数 |
| 11 | `article_receive_reproduce_cnt` | `bigint` |  | 长文章收获转载数 |
| 12 | `music_receive_reproduce_cnt` | `bigint` |  | 音乐文章收获转载数 |
| 13 | `question_receive_reproduce_cnt` | `bigint` |  | 问答收获转载数 |
| 14 | `text_receive_recommend_cnt` | `bigint` |  | 文本文章收获推荐数 |
| 15 | `photo_receive_recommend_cnt` | `bigint` |  | 图片文章收获推荐数 |
| 16 | `video_receive_recommend_cnt` | `bigint` |  | 视频文章收获推荐数 |
| 17 | `article_receive_recommend_cnt` | `bigint` |  | 长文章收推荐欢数 |
| 18 | `music_receive_recommend_cnt` | `bigint` |  | 音乐文章收获推荐数 |
| 19 | `question_receive_recommend_cnt` | `bigint` |  | 问答收获推荐数 |
| 20 | `text_receive_collect_cnt` | `bigint` |  | 文本文章收获收藏数 |
| 21 | `photo_receive_collect_cnt` | `bigint` |  | 图片文章收获收藏数 |
| 22 | `video_receive_collect_cnt` | `bigint` |  | 视频文章收获收藏数 |
| 23 | `article_receive_collect_cnt` | `bigint` |  | 长文章收获收藏数 |
| 24 | `music_receive_collect_cnt` | `bigint` |  | 音乐文章收获收藏数 |
| 25 | `question_receive_collect_cnt` | `bigint` |  | 问答收获收藏数 |
| 26 | `text_receive_comment_cnt` | `bigint` |  | 文本文章收获评论数 |
| 27 | `photo_receive_comment_cnt` | `bigint` |  | 图片文章收获评论数 |
| 28 | `video_receive_comment_cnt` | `bigint` |  | 视频文章收获评论数 |
| 29 | `article_receive_comment_cnt` | `bigint` |  | 长文章收获评论数 |
| 30 | `music_receive_comment_cnt` | `bigint` |  | 音乐文章收获评论数 |
| 31 | `question_receive_comment_cnt` | `bigint` |  | 问答收获评论数 |
| 32 | `receive_hot` | `bigint` |  | 获得热度 |
| 33 | `receive_like_cnt` | `bigint` |  | 获得喜欢数 |
| 34 | `receive_reproduce_cnt` | `bigint` |  | 获得转载数 |
| 35 | `receive_recommend_cnt` | `bigint` |  | 获得推荐数 |
| 36 | `receive_collect_cnt` | `bigint` |  | 获得收藏数 |
| 37 | `receive_comment_cnt` | `bigint` |  | 获得评论数 |
| 38 | `dt` | `string` |  |  |

---

## dws_par_creator_level_scoring_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_level_scoring_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2267.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2267.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `is_official` | `int` |  | 是否官方博客 1是 0否 |
| 3 | `is_imported` | `int` |  | 是否引入创作者 1是 0否 |
| 4 | `import_platform_type` | `string` |  | 引入平台类型： 账号属性: MCN机构、知识公路、云音乐、抖音、快手等 |
| 5 | `post_first_date` | `string` |  | 首次发文日期 |
| 6 | `post_last_date` | `string` |  | 最近一次发文日期 |
| 7 | `post_count_recommend_pool_180d` | `bigint` |  | 180天内进入推荐池累积文章数 |
| 8 | `fans_std` | `bigint` |  | 累计粉丝数 |
| 9 | `hd_fans_1y` | `bigint` |  | 近1年互动粉丝数 |
| 10 | `hot_avg_rec_pool_recent_posts_1y` | `bigint` |  | 近1年进入推荐池审核的最近10篇以内文章 在近1年内的热度均值 |
| 11 | `score` | `double` |  | 评分公式: log(least(greatest(post_count_recommend_pool_180d,1), 180)/log(180)*22.76 + log(least(greatest(hd_fans_1y,1),250000)/log(250000)*34.20 + log(least(greatest(hot_avg_rec_pool_recent_posts_1y,1),75000)/log(75000)*43.04 |
| 12 | `is_above_level_entry` | `int` |  | 是否达到等级评定门槛: 非官博 非引入 最近一年发过文 首次发文日期超过30天 |
| 13 | `level` | `string` |  | 创作者等级 评定门槛： 非官博 非引入 最近一年发过文 首次发文日期超过30天； 评定标准： 基于主发文类型设定不同等级阈值 |
| 14 | `post_count_recommend_pool_review_180d` | `bigint` |  | 180天内进入推荐池审核文章数 20230508添加 |
| 15 | `post_main_content_type` | `string` |  | 创作者主发文类型： 图片 文字 视频（按照发文量选择最大类型，相同发文量按此类型顺序选择第一个） |
| 16 | `post_count_video_1y` | `bigint` |  | 近一年视频发文数 |
| 17 | `post_count_text_1y` | `bigint` |  | 近一年文本发文数 |
| 18 | `post_count_photo_1y` | `bigint` |  | 近一年图片发文数 |
| 19 | `dt` | `string` |  |  |

---

## dws_par_creator_paid_post_income_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_paid_post_income_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3.2G |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 开通付费用户id |
| 2 | `gift_income_std` | `double` |  |  |
| 3 | `gift_income_1d` | `double` |  |  |
| 4 | `gift_income_7d` | `double` |  |  |
| 5 | `gift_income_30d` | `double` |  |  |
| 6 | `ad_unlock_income_std` | `double` |  |  |
| 7 | `ad_unlock_income_1d` | `double` |  |  |
| 8 | `ad_unlock_income_7d` | `double` |  |  |
| 9 | `ad_unlock_income_30d` | `double` |  |  |
| 10 | `grain_tickets_std` | `bigint` |  |  |
| 11 | `grain_tickets_1d` | `bigint` |  |  |
| 12 | `grain_tickets_7d` | `bigint` |  |  |
| 13 | `grain_tickets_30d` | `bigint` |  |  |
| 14 | `coupon_income_std` | `double` |  |  |
| 15 | `coupon_income_1d` | `double` |  |  |
| 16 | `coupon_income_7d` | `double` |  |  |
| 17 | `coupon_income_30d` | `double` |  |  |
| 18 | `fans_vip_income_std` | `double` |  |  |
| 19 | `fans_vip_income_1d` | `double` |  |  |
| 20 | `fans_vip_income_7d` | `double` |  |  |
| 21 | `fans_vip_income_30d` | `double` |  |  |
| 22 | `dt` | `string` |  |  |

---

## dws_par_creator_pay_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_pay_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.5G |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 创作者 |
| 2 | `first_unlock_uv` | `bigint` |  | 首次付费人数 |
| 3 | `first_unlock_money` | `double` |  | 首次付费金额 |
| 4 | `first_unlock_fans_uv` | `bigint` |  | 首次付费用户沉淀-关注 |
| 5 | `first_unlock_collection_subscribe_uv` | `bigint` |  | 首次付费用户沉淀-订阅合集 |
| 6 | `first_unlock_non_fans_collection_subscribe_uv` | `bigint` |  | 首次付费用户沉淀-其他 |
| 7 | `first_unlock_uv_1d` | `bigint` |  | 首次付费用户-近1日 |
| 8 | `first_unlock_money_1d` | `double` |  | 首次付费用户付费金额-近1日 |
| 9 | `dt` | `string` |  |  |

---

## dws_par_creator_premium_staging_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_premium_staging_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 618.6M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `is_premium` | `int` |  | 是否优质创作者 |
| 3 | `is_premium_high_potential` | `int` |  | 是否高潜创作者 |
| 4 | `is_premium_middle_potential` | `int` |  | 是否中潜创作者 |
| 5 | `premium_posts_30d` | `bigint` |  | 近30日优质内容量 |
| 6 | `premium_date` | `string` |  | 成为优质创作者日期 |
| 7 | `active_days_30d` | `bigint` |  | 近30日登录活跃天数 |
| 8 | `dt` | `string` |  |  |

---

## dws_par_creator_traffic_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_traffic_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 163.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 163.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 145 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `post_count_7d` | `bigint` |  | 近7天发布文章数 |
| 3 | `post_count_15d` | `bigint` |  | 近15天发布文章数 |
| 4 | `post_count_30d` | `bigint` |  | 近30天发布文章数 |
| 5 | `new_post_browse_pv_7d` | `bigint` |  | 近7天发布新文章浏览次数 |
| 6 | `new_post_browse_pv_15d` | `bigint` |  | 近15天发布新文章浏览次数 |
| 7 | `new_post_browse_pv_30d` | `bigint` |  | 近30天发布新文章浏览次数 |
| 8 | `browse_pv_7d` | `bigint` |  | 近7天浏览次数 |
| 9 | `browse_pv_15d` | `bigint` |  | 近15天浏览次数 |
| 10 | `browse_pv_30d` | `bigint` |  | 近30天浏览次数 |
| 11 | `discovery_click_pv_7d` | `bigint` |  | 近7天发现页点击量 |
| 12 | `discovery_click_pv_30d` | `bigint` |  | 近30天发现页点击量 |
| 13 | `discovery_click_pv_90d` | `bigint` |  | 近90天发现页点击量 |
| 14 | `related_article_click_pv_7d` | `bigint` |  | 近7天相关文章点击量 |
| 15 | `related_article_click_pv_30d` | `bigint` |  | 近30天相关文章点击量 |
| 16 | `related_article_click_pv_90d` | `bigint` |  | 近90天相关文章点击量 |
| 17 | `tag_discovery_click_pv_7d` | `bigint` |  | 近7天tag发现页点击量 |
| 18 | `tag_discovery_click_pv_30d` | `bigint` |  | 近30天tag发现页点击量 |
| 19 | `tag_discovery_click_pv_90d` | `bigint` |  | 近90天tag发现页点击量 |
| 20 | `tag_new_click_pv_7d` | `bigint` |  | 近7天tag最新页点击量 |
| 21 | `tag_new_click_pv_30d` | `bigint` |  | 近30天tag最新页点击量 |
| 22 | `tag_new_click_pv_90d` | `bigint` |  | 近90天tag最新页点击量 |
| 23 | `tag_hot_click_pv_7d` | `bigint` |  | 近7天tag最热页点击量 |
| 24 | `tag_hot_click_pv_30d` | `bigint` |  | 近30天tag最热页点击量 |
| 25 | `tag_hot_click_pv_90d` | `bigint` |  | 近90天tag最热页点击量 |
| 26 | `collection_click_pv_7d` | `bigint` |  | 近7天合集点击量 |
| 27 | `collection_click_pv_30d` | `bigint` |  | 近30天合集点击量 |
| 28 | `collection_click_pv_90d` | `bigint` |  | 近90天合集点击量 |
| 29 | `attention_click_pv_7d` | `bigint` |  | 近7天attention点击量 |
| 30 | `attention_click_pv_30d` | `bigint` |  | 近30天attention点击量 |
| 31 | `attention_click_pv_90d` | `bigint` |  | 近90天attention点击量 |
| 32 | `click_pv_7d` | `bigint` |  | 近7天点击量 |
| 33 | `click_pv_30d` | `bigint` |  | 近30天点击量 |
| 34 | `click_pv_90d` | `bigint` |  | 近90天点击量 |
| 35 | `post_expose_uv_7d` | `bigint` |  | 近7d文章曝光uv |
| 36 | `post_expose_uv_30d` | `bigint` |  | 近30d文章曝光uv |
| 37 | `post_expose_uv_90d` | `bigint` |  | 近90d文章曝光uv |
| 38 | `post_click_uv_7d` | `bigint` |  | 近7d文章点击uv |
| 39 | `post_click_uv_30d` | `bigint` |  | 近30d文章点击uv |
| 40 | `post_click_uv_90d` | `bigint` |  | 近90d文章点击uv |
| 41 | `post_real_browse_uv_7d` | `bigint` |  | 近7d文章有效浏览uv |
| 42 | `post_real_browse_uv_30d` | `bigint` |  | 近30d文章有效浏览uv |
| 43 | `post_real_browse_uv_90d` | `bigint` |  | 近90d文章有效浏览uv |
| 44 | `post_support_induced_pv_7d` | `bigint` |  | 近7d文章扶持流量pv |
| 45 | `post_support_induced_pv_30d` | `bigint` |  | 近30d文章扶持流量pv |
| 46 | `post_support_induced_pv_90d` | `bigint` |  | 近90d文章扶持流量pv |
| 47 | `post_support_induced_pv_1d` | `bigint` |  | 近1d文章扶持流量pv |
| 48 | `post_expose_uv_1d` | `bigint` |  | 近1d文章曝光uv |
| 49 | `post_click_uv_1d` | `bigint` |  | 近1d文章点击uv |
| 50 | `post_real_browse_uv_1d` | `bigint` |  | 近1d文章有效浏览uv |
| 51 | `expose_pv_1d` | `bigint` |  | 近1d曝光pv |
| 52 | `expose_pv_7d` | `bigint` |  | 近7d曝光pv |
| 53 | `expose_pv_30d` | `bigint` |  | 近30d曝光pv |
| 54 | `expose_pv_90d` | `bigint` |  | 近90d曝光pv |
| 55 | `browse_pv_90d` | `bigint` |  | 近90d浏览pv |
| 56 | `click_pv_1d` | `bigint` |  | 近1d点击pv |
| 57 | `browse_pv_1d` | `bigint` |  | 近1d浏览pv |
| 58 | `discovery_click_pv_1d` | `bigint` |  | 近1d发现页点击量 |
| 59 | `related_article_click_pv_1d` | `bigint` |  | 近1d相关文章点击量 |
| 60 | `tag_discovery_click_pv_1d` | `bigint` |  | 近1dtag发现页点击量 |
| 61 | `tag_new_click_pv_1d` | `bigint` |  | 近1dtag最新页点击量 |
| 62 | `tag_hot_click_pv_1d` | `bigint` |  | 近1dtag最热页点击量 |
| 63 | `collection_click_pv_1d` | `bigint` |  | 近1d合集点击量 |
| 64 | `attention_click_pv_1d` | `bigint` |  | 近1dattention点击量 |
| 65 | `discovery_expose_pv_1d` | `bigint` |  | 近1d发现页曝光pv |
| 66 | `discovery_expose_pv_7d` | `bigint` |  | 近7d发现页曝光pv |
| 67 | `discovery_expose_pv_30d` | `bigint` |  | 近30d发现页曝光pv |
| 68 | `discovery_expose_pv_90d` | `bigint` |  | 近90d发现页曝光pv |
| 69 | `related_article_expose_pv_1d` | `bigint` |  | 近1d相关文章曝光pv |
| 70 | `related_article_expose_pv_7d` | `bigint` |  | 近7d相关文章曝光pv |
| 71 | `related_article_expose_pv_30d` | `bigint` |  | 近30d相关文章曝光pv |
| 72 | `related_article_expose_pv_90d` | `bigint` |  | 近90d相关文章曝光pv |
| 73 | `tag_discovery_expose_pv_1d` | `bigint` |  | 近1dtag发现页曝光pv |
| 74 | `tag_discovery_expose_pv_7d` | `bigint` |  | 近7dtag发现页曝光pv |
| 75 | `tag_discovery_expose_pv_30d` | `bigint` |  | 近30dtag发现页曝光pv |
| 76 | `tag_discovery_expose_pv_90d` | `bigint` |  | 近90dtag发现页曝光pv |
| 77 | `tag_new_expose_pv_1d` | `bigint` |  | 近1dtag最新页曝光pv |
| 78 | `tag_new_expose_pv_7d` | `bigint` |  | 近7dtag最新页曝光pv |
| 79 | `tag_new_expose_pv_30d` | `bigint` |  | 近30dtag最新页曝光pv |
| 80 | `tag_new_expose_pv_90d` | `bigint` |  | 近90dtag最新页曝光pv |
| 81 | `tag_hot_expose_pv_1d` | `bigint` |  | 近1dtag最热页曝光pv |
| 82 | `tag_hot_expose_pv_7d` | `bigint` |  | 近7dtag最热页曝光pv |
| 83 | `tag_hot_expose_pv_30d` | `bigint` |  | 近30dtag最热页曝光pv |
| 84 | `tag_hot_expose_pv_90d` | `bigint` |  | 近90dtag最热页曝光pv |
| 85 | `collection_expose_pv_1d` | `bigint` |  | 近1d合集曝光pv |
| 86 | `collection_expose_pv_7d` | `bigint` |  | 近7d合集曝光pv |
| 87 | `collection_expose_pv_30d` | `bigint` |  | 近30d合集曝光pv |
| 88 | `collection_expose_pv_90d` | `bigint` |  | 近90d合集曝光pv |
| 89 | `attention_expose_pv_1d` | `bigint` |  | 近1dattention曝光pv |
| 90 | `attention_expose_pv_7d` | `bigint` |  | 近7dattention曝光pv |
| 91 | `attention_expose_pv_30d` | `bigint` |  | 近30dattention曝光pv |
| 92 | `attention_expose_pv_90d` | `bigint` |  | 近90dattention曝光pv |
| 93 | `discovery_browse_pv_1d` | `bigint` |  | 近1d发现页浏览pv |
| 94 | `discovery_browse_pv_7d` | `bigint` |  | 近7d发现页浏览pv |
| 95 | `discovery_browse_pv_30d` | `bigint` |  | 近30d发现页浏览pv |
| 96 | `discovery_browse_pv_90d` | `bigint` |  | 近90d发现页浏览pv |
| 97 | `related_article_browse_pv_1d` | `bigint` |  | 近1d相关文章浏览pv |
| 98 | `related_article_browse_pv_7d` | `bigint` |  | 近7d相关文章浏览pv |
| 99 | `related_article_browse_pv_30d` | `bigint` |  | 近30d相关文章浏览pv |
| 100 | `related_article_browse_pv_90d` | `bigint` |  | 近90d相关文章浏览pv |
| 101 | `tag_discovery_browse_pv_1d` | `bigint` |  | 近1dtag发现页浏览pv |
| 102 | `tag_discovery_browse_pv_7d` | `bigint` |  | 近7dtag发现页浏览pv |
| 103 | `tag_discovery_browse_pv_30d` | `bigint` |  | 近30dtag发现页浏览pv |
| 104 | `tag_discovery_browse_pv_90d` | `bigint` |  | 近90dtag发现页浏览pv |
| 105 | `tag_new_browse_pv_1d` | `bigint` |  | 近1dtag最新页浏览pv |
| 106 | `tag_new_browse_pv_7d` | `bigint` |  | 近7dtag最新页浏览pv |
| 107 | `tag_new_browse_pv_30d` | `bigint` |  | 近30dtag最新页浏览pv |
| 108 | `tag_new_browse_pv_90d` | `bigint` |  | 近90dtag最新页浏览pv |
| 109 | `tag_hot_browse_pv_1d` | `bigint` |  | 近1dtag最热页浏览pv |
| 110 | `tag_hot_browse_pv_7d` | `bigint` |  | 近7dtag最热页浏览pv |
| 111 | `tag_hot_browse_pv_30d` | `bigint` |  | 近30dtag最热页浏览pv |
| 112 | `tag_hot_browse_pv_90d` | `bigint` |  | 近90dtag最热页浏览pv |
| 113 | `collection_browse_pv_1d` | `bigint` |  | 近1d合集浏览pv |
| 114 | `collection_browse_pv_7d` | `bigint` |  | 近7d合集浏览pv |
| 115 | `collection_browse_pv_30d` | `bigint` |  | 近30d合集浏览pv |
| 116 | `collection_browse_pv_90d` | `bigint` |  | 近90d合集浏览pv |
| 117 | `attention_browse_pv_1d` | `bigint` |  | 近1dattention浏览pv |
| 118 | `attention_browse_pv_7d` | `bigint` |  | 近7dattention浏览pv |
| 119 | `attention_browse_pv_30d` | `bigint` |  | 近30dattention浏览pv |
| 120 | `attention_browse_pv_90d` | `bigint` |  | 近90dattention浏览pv |
| 121 | `search_click_pv_1d` | `bigint` |  | 近1天搜索点击量 |
| 122 | `search_click_pv_7d` | `bigint` |  | 近7天搜索点击量 |
| 123 | `search_click_pv_30d` | `bigint` |  | 近30天搜索点击量 |
| 124 | `search_click_pv_90d` | `bigint` |  | 近90天搜索点击量 |
| 125 | `videolist_click_pv_1d` | `bigint` |  | 近1天视频列表点击量 |
| 126 | `videolist_click_pv_7d` | `bigint` |  | 近7天视频列表点击量 |
| 127 | `videolist_click_pv_30d` | `bigint` |  | 近30天视频列表点击量 |
| 128 | `videolist_click_pv_90d` | `bigint` |  | 近90天视频列表点击量 |
| 129 | `search_expose_pv_1d` | `bigint` |  | 近1天搜索曝光量 |
| 130 | `search_expose_pv_7d` | `bigint` |  | 近7天搜索曝光量 |
| 131 | `search_expose_pv_30d` | `bigint` |  | 近30天搜索曝光量 |
| 132 | `search_expose_pv_90d` | `bigint` |  | 近90天搜索曝光量 |
| 133 | `videolist_expose_pv_1d` | `bigint` |  | 近1天视频列表曝光量 |
| 134 | `videolist_expose_pv_7d` | `bigint` |  | 近7天视频列表曝光量 |
| 135 | `videolist_expose_pv_30d` | `bigint` |  | 近30天视频列表曝光量 |
| 136 | `videolist_expose_pv_90d` | `bigint` |  | 近90天视频列表曝光量 |
| 137 | `search_browse_pv_1d` | `bigint` |  | 近1天搜索浏览PV |
| 138 | `search_browse_pv_7d` | `bigint` |  | 近7天搜索浏览PV |
| 139 | `search_browse_pv_30d` | `bigint` |  | 近30天搜索浏览PV |
| 140 | `search_browse_pv_90d` | `bigint` |  | 近90天搜索浏览PV |
| 141 | `videolist_browse_pv_1d` | `bigint` |  | 近1天视频列表浏览PV |
| 142 | `videolist_browse_pv_7d` | `bigint` |  | 近7天视频列表浏览PV |
| 143 | `videolist_browse_pv_30d` | `bigint` |  | 近30天视频列表浏览PV |
| 144 | `videolist_browse_pv_90d` | `bigint` |  | 近90天视频列表浏览PV |
| 145 | `dt` | `string` |  |  |

---

## dws_par_creator_user_support_score_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_creator_user_support_score_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1946.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1946.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 创作者 |
| 2 | `userid` | `bigint` |  | 内容消费用户 |
| 3 | `score` | `double` |  | 支持分 |
| 4 | `gift_score` | `double` |  |  |
| 5 | `share_score` | `double` |  |  |
| 6 | `comment_score` | `double` |  |  |
| 7 | `like_score` | `double` |  |  |
| 8 | `dt` | `string` |  |  |

---

## dws_par_device_active_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_device_active_dd` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 1028.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1028.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备id |
| 2 | `deviceos` | `string` |  | 设备对应的操作系统 |
| 3 | `first_active_time` | `bigint` |  | 设备首次激活时间 |
| 4 | `last_active_date` | `string` |  | 最后一次活跃日期 |
| 5 | `last_return_date` | `string` |  | 最后一次回流日期 |
| 6 | `active_days_1d` | `bigint` |  | 近1天活跃天数 |
| 7 | `active_days_7d` | `bigint` |  | 近7天活跃天数 |
| 8 | `active_days_15d` | `bigint` |  | 近15天活跃天数 |
| 9 | `active_days_30d` | `bigint` |  | 近30天活跃天数 |
| 10 | `dt` | `string` |  |  |

---

## dws_par_device_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_device_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 212.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 212.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `real_browse_pv` | `bigint` |  | 有效浏览pv |
| 3 | `search_cnt` | `bigint` |  | 搜索次数 |
| 4 | `post_cnt` | `bigint` |  | 点击发文次数 |
| 5 | `like_cnt` | `bigint` |  | 喜欢次数 |
| 6 | `recommend_cnt` | `bigint` |  | 推荐次数 |
| 7 | `collect_cnt` | `bigint` |  | 收藏次数 |
| 8 | `reproduce_cnt` | `bigint` |  | 转载次数 |
| 9 | `response_cnt` | `bigint` |  | 评论次数 |
| 10 | `device_pay_amount` | `double` |  | 设备当天付费金额， 关联用户当天登录过唯一设备， 用户付费金额 |
| 11 | `share_cnt` | `bigint` |  | 分享数 |
| 12 | `follow_cnt` | `bigint` |  | 关注数 |
| 13 | `collection_follow_cnt` | `bigint` |  | 合集关注数 |
| 14 | `chat_cnt` | `bigint` |  | 虚拟人聊天次数 |
| 15 | `dt` | `string` |  |  |

---

## dws_par_device_session_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_device_session_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 191.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 191.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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

## dws_par_device_session_v2_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_device_session_v2_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 256.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 256.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 哈勃设备id |
| 2 | `appversion` | `string` |  | 客户端版本 |
| 3 | `appchannel` | `string` |  | 渠道 |
| 4 | `deviceos` | `string` |  | 操作系统 |
| 5 | `sessioncount` | `bigint` |  | 会话次数 |
| 6 | `sessiontime` | `bigint` |  | 会话时长: 单位毫秒 |
| 7 | `dt` | `string` |  |  |

---

## dws_par_user_1v1_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_1v1_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 27.9M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `ask_pv` | `bigint` |  | 提问pv |
| 3 | `answer_pv` | `bigint` |  | 回答pv |
| 4 | `comment_pv` | `bigint` |  | 回答评论的pv |
| 5 | `answer_like_pv` | `bigint` |  | 回答点赞pv |
| 6 | `comment_like_pv` | `bigint` |  | 评论点赞pv |
| 7 | `dt` | `string` |  |  |

---

## dws_par_user_active_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_active_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 228.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 228.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `is_anonymous` | `int` |  | 是否匿名用户 |
| 3 | `active_days_1d` | `bigint` |  | 近1天活跃天数 |
| 4 | `active_days_7d` | `bigint` |  | 近7天活跃天数 |
| 5 | `active_days_15d` | `bigint` |  | 近15天活跃天数 |
| 6 | `active_days_30d` | `bigint` |  | 近30天活跃天数 |
| 7 | `active_days_90d` | `bigint` |  | 近90天活跃天数 |
| 8 | `active_days_3d` | `bigint` |  | 近3日活跃天数 |
| 9 | `dt` | `string` |  |  |

---

## dws_par_user_active_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_active_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 57.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 57.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `deviceos` | `string` |  | 操作平台 |
| 3 | `firstactivetime` | `bigint` |  | 当日首次活跃时间 剔除延迟发送昨日数据 |
| 4 | `is_anonymous` | `int` |  | 是否匿名用户 1是 0否 |
| 5 | `dt` | `string` |  | 日期 |

---

## dws_par_user_ad_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_ad_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 11.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `video_grain_ticket_7d` | `bigint` |  | 7日激励视频获取粮票数量 |
| 3 | `video_grain_ticket_15d` | `bigint` |  | 15日激励视频获取粮票数量 |
| 4 | `video_grain_ticket_30d` | `bigint` |  | 30日激励视频获取粮票数量 |
| 5 | `dt` | `string` |  |  |

---

## dws_par_user_ad_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_ad_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 39.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 39.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `impress_pv` | `bigint` |  | 广告曝光次数 |
| 3 | `impress_ad_count` | `bigint` |  | 广告曝光个数 |
| 4 | `click_pv` | `bigint` |  | 广告点击次数 |
| 5 | `click_ad_count` | `bigint` |  | 广告点击个数 |
| 6 | `close_ad_count` | `bigint` |  | 主动关闭广告次数 |
| 7 | `advideocount` | `bigint` |  | 激励视频数量 |
| 8 | `dt` | `string` |  |  |

---

## dws_par_user_appversion_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_appversion_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 129.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 129.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `last_appversion` | `string` |  | 最新版本 |
| 3 | `last_time` | `bigint` |  | 最新使用时间 |
| 4 | `dt` | `string` |  |  |

---

## dws_par_user_base_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_base_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1637.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1637.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 55 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `blogid` | `bigint` |  | 主博客id |
| 3 | `blogname` | `string` |  | 主博客名称 |
| 4 | `blognickname` | `string` |  | 主博客昵称 |
| 5 | `createtime` | `bigint` |  | 用户建立时间 |
| 6 | `from_platform` | `string` |  | 注册时系统平台 |
| 7 | `accounttype` | `string` |  | 账号类型: 邮箱、手机号、微信、QQ etc |
| 8 | `account` | `string` |  | 账号 |
| 9 | `name` | `string` |  | 实名制姓名 |
| 10 | `idnumber` | `string` |  | 身份证号 |
| 11 | `edu_degree` | `string` |  | 学历 |
| 12 | `profession` | `string` |  | 职业 |
| 13 | `city_reside` | `string` |  | 居住城市 |
| 14 | `phone_price_180d` | `string` |  | 手机价格 |
| 15 | `gender` | `int` |  | 性别: 0未知，1男，2女，3保密 |
| 16 | `birth_year` | `bigint` |  | 年龄（出生年份，eg：1979） |
| 17 | `is_push_on` | `int` |  | push开关状态: 1 on, 0 off |
| 18 | `total_reward_gift_amount` | `double` |  | 累计付费金额: 付费＋送付费礼物 |
| 19 | `phones` | `array<string>` |  | 用户关联手机号 |
| 20 | `last_phone` | `string` |  | 最近关联手机号 |
| 21 | `last_phone_create_time` | `bigint` |  | 最近关联手机号时间 |
| 22 | `subscribe_tags` | `array<string>` |  | 用户关注标签列表 |
| 23 | `subscribe_collections` | `array<string>` |  | 用户关注合集列表 |
| 24 | `imeis` | `array<string>` |  | 用户使用过的设备imei列表 |
| 25 | `idfas` | `array<string>` |  | 用户使用过的设备idfa列表 |
| 26 | `deviceudids` | `array<string>` |  | 用户使用过的deviceudid列表 |
| 27 | `last_login_time` | `bigint` |  | 最近一次登录时间 |
| 28 | `last_login_platform` | `string` |  | 最近一次登录客户端类型 |
| 29 | `last_login_deviceid` | `string` |  | 最近一次登录设备Id |
| 30 | `last_login_app_version` | `string` |  | 客户端版本 |
| 31 | `auth_domain_ids` | `array<bigint>` |  | 博客认证领域列表 |
| 32 | `auth_domain_names` | `array<string>` |  | 博客认证领域名称列表 |
| 33 | `is_open_reward` | `int` |  | 是否开通打赏 1是 0否 |
| 34 | `connect_sites` | `array<row<bigint,bigint,string,string>('sitetype','createtime','siteuserid','sitesource')>` |  | 关联站点信息列表 |
| 35 | `all_blog_fans` | `bigint` |  | 全部博客粉丝数 |
| 36 | `main_blog_fans` | `bigint` |  | 主博客粉丝数 |
| 37 | `follow_blogs` | `array<bigint>` |  | 关注博客数 |
| 38 | `emails` | `array<string>` |  | 用户邮箱列表 |
| 39 | `blogs` | `array<bigint>` |  | 用户全部博客列表 |
| 40 | `post_count_std` | `bigint` |  | 累计发文数 |
| 41 | `post_count_since_2020` | `bigint` |  | 自2020.01.01起累计发文数 |
| 42 | `last_login_ip` | `string` |  | 上次登录IP |
| 43 | `country` | `string` |  | 国家 |
| 44 | `province` | `string` |  | 省份 |
| 45 | `city` | `string` |  | 城市 |
| 46 | `privilegelevel` | `int` |  | 用户特权等级:0白名单,1黄V,2绿V,3普通用户 |
| 47 | `register_ip` | `string` |  | 用户注册Ip |
| 48 | `register_country` | `string` |  | 用户注册Ip所属国家 |
| 49 | `register_province` | `string` |  | 用户注册Ip所属省份 |
| 50 | `register_city` | `string` |  | 用户注册Ip所属城市 |
| 51 | `blog_status` | `string` |  | 博客状态：删除，封禁，正常 |
| 52 | `subscribe_grains` | `bigint` |  | 粮单订阅数 |
| 53 | `login_days_30d` | `bigint` |  | 30天内登录天数 |
| 54 | `is_black_phone` | `int` |  | 该用户手机是否命中风险手机号 |
| 55 | `dt` | `string` |  |  |

---

## dws_par_user_content_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_content_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1104.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1104.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `browse_pv` | `bigint` |  | 有效浏览 |
| 3 | `browse_post_count` | `bigint` |  | 浏览文章数 |
| 4 | `browse_text_photo_post_count` | `bigint` |  | 浏览图文数 |
| 5 | `browse_video_count` | `bigint` |  | 浏览视频数 |
| 6 | `browse_question_count` | `bigint` |  | 浏览问答数 |
| 7 | `session_time` | `bigint` |  | 总会话时长， 单位毫秒 |
| 8 | `browse_post_bitmap` | `varbinary(2147483647)` |  | 有效浏览文章位图 |
| 9 | `browse_note_pv` | `bigint` |  | 单日志页有效浏览pv |
| 10 | `browse_discovery_pv` | `bigint` |  | 发现页有效浏览pv |
| 11 | `browse_search_pv` | `bigint` |  | 搜索页有效浏览pv |
| 12 | `browse_tag_pv` | `bigint` |  | 标签页有效浏览pv |
| 13 | `dt` | `string` |  |  |

---

## dws_par_user_core_staging_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_core_staging_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 27.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 27.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `valid_interaction` | `bigint` |  | 近30内有效互动行为（有效评论+小蓝手+分享+发文行为）+付费行为（内容付费，商品付费）+创建粮单行为 |
| 3 | `comment_interaction` | `bigint` |  | 30天内评论被互动（被点赞、被回复） |
| 4 | `rec_cnt` | `bigint` |  | 小蓝手数 |
| 5 | `share_cnt` | `bigint` |  | 分享数 |
| 6 | `post_cnt` | `bigint` |  | 发文数 |
| 7 | `trade_cnt` | `bigint` |  | 付费次数 |
| 8 | `grain_cnt` | `bigint` |  | 付费金额 |
| 9 | `valid_response` | `bigint` |  | 有效评论数 |
| 10 | `active_days_30d` | `bigint` |  | 30天内活跃天数 |
| 11 | `response_hot` | `bigint` |  | 30天内评论被点赞次数 |
| 12 | `response_replies` | `bigint` |  | 30天内评论被回复次数 |
| 13 | `blog_nickname` | `string` |  |  |
| 14 | `blog_url` | `string` |  |  |
| 15 | `trade_money` | `double` |  |  |
| 16 | `dt` | `string` |  |  |

---

## dws_par_user_coupon_exchange_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_coupon_exchange_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 11.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `coupon_count_1d` | `bigint` |  |  |
| 3 | `coupon_count_7d` | `bigint` |  |  |
| 4 | `coupon_count_15d` | `bigint` |  |  |
| 5 | `coupon_count_30d` | `bigint` |  |  |
| 6 | `coupon_count_90d` | `bigint` |  |  |
| 7 | `coupon_count_180d` | `bigint` |  |  |
| 8 | `coupon_count_std` | `bigint` |  |  |
| 9 | `dt` | `string` |  |  |

---

## dws_par_user_discuss_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_discuss_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 137.4M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `topic_ask_pv` | `bigint` |  | 话题提问pv |
| 3 | `topic_answer_pv` | `bigint` |  | 话题回答pv |
| 4 | `notopic_answer_pv` | `bigint` |  | 无话题回答pv |
| 5 | `comment_pv` | `bigint` |  | 讨论的评论pv |
| 6 | `topic_answer_like_pv` | `bigint` |  | 讨论回答点赞pv |
| 7 | `topic_comment_like_pv` | `bigint` |  | 讨论评论点赞pv |
| 8 | `dt` | `string` |  |  |

---

## dws_par_user_ec_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_ec_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 41.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 41.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `pay_product_category_level1` | `array<string>` |  | 用户付费商品一级目录名称 |
| 3 | `pay_product_category_level2` | `array<string>` |  | 用户付费商品二级目录名称 |
| 4 | `pay_product_category_level3` | `array<string>` |  | 用户付费商品三级目录名称 |
| 5 | `pay_amount_std` | `double` |  | 累计交易金额 |
| 6 | `market_sale_amount_std` | `double` |  | 累计市集商品交易金额 |
| 7 | `card_sale_amount_std` | `double` |  | 累计抽卡商品交易金额 |
| 8 | `pay_amount_7d` | `double` |  | 近7日交易金额 |
| 9 | `market_sale_amount_7d` | `double` |  | 近7日市集商品交易金额 |
| 10 | `card_sale_amount_7d` | `double` |  | 近7日抽卡商品交易金额 |
| 11 | `pay_amount_15d` | `double` |  | 近15日交易金额 |
| 12 | `market_sale_amount_15d` | `double` |  | 近15日市集商品交易金额 |
| 13 | `card_sale_amount_15d` | `double` |  | 近15日抽卡商品交易金额 |
| 14 | `pay_amount_30d` | `double` |  | 近30日交易金额 |
| 15 | `market_sale_amount_30d` | `double` |  | 近30日市集商品交易金额 |
| 16 | `card_sale_amount_30d` | `double` |  | 近30日抽卡商品交易金额 |
| 17 | `pay_amount_90d` | `double` |  | 近90日交易金额 |
| 18 | `market_sale_amount_90d` | `double` |  | 近90日市集商品交易金额 |
| 19 | `card_sale_amount_90d` | `double` |  | 近90日抽卡商品交易金额 |
| 20 | `pay_amount_180d` | `double` |  | 近180日交易金额 |
| 21 | `market_sale_amount_180d` | `double` |  | 近180日市集商品交易金额 |
| 22 | `card_sale_amount_180d` | `double` |  | 近180日抽卡商品交易金额 |
| 23 | `add_cart_pv_7d` | `bigint` |  | 近7日添加购物车次数 |
| 24 | `add_cart_pv_15d` | `bigint` |  | 近15日添加购物车次数 |
| 25 | `add_cart_pv_30d` | `bigint` |  | 近30日添加购物车次数 |
| 26 | `add_cart_pv_90d` | `bigint` |  | 近90日添加购物车次数 |
| 27 | `add_cart_pv_180d` | `bigint` |  | 近180日添加购物车次数 |
| 28 | `last_active_date` | `string` |  | 最近活跃日期 |
| 29 | `dt` | `string` |  |  |

---

## dws_par_user_fans_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_fans_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 192.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 192.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `fans_total` | `bigint` |  |  |
| 3 | `fans_30d` | `bigint` |  |  |
| 4 | `fans_7d` | `bigint` |  |  |
| 5 | `fans_1d` | `bigint` |  |  |
| 6 | `hd_fans_1y` | `bigint` |  |  |
| 7 | `fans100dt` | `string` |  |  |
| 8 | `fans500dt` | `string` |  |  |
| 9 | `fans1kdt` | `string` |  |  |
| 10 | `fans5kdt` | `string` |  |  |
| 11 | `fans1wdt` | `string` |  |  |
| 12 | `fanstype` | `string` |  |  |
| 13 | `fans_15d` | `bigint` |  |  |
| 14 | `fans_90d` | `bigint` |  |  |
| 15 | `fans_180d` | `bigint` |  |  |
| 16 | `fans_365d` | `bigint` |  | 近365天粉丝数 |
| 17 | `dt` | `string` |  |  |

---

## dws_par_user_home_top_resource_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_home_top_resource_dd` |
| **描述** | 首页吸顶资源(权益中心/破次元)用户人群包指标 - 按天聚合 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 2.1G |
| **是否分区表** | 是 |

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `is_anonymous` | `tinyint` |  | 是否匿名用户 0-否 1-是 |
| 3 | `rc_expose_days_1d` | `int` |  | 近1天权益中心icon曝光天数 |
| 4 | `rc_expose_days_7d` | `int` |  | 近7天权益中心icon曝光天数 |
| 5 | `rc_expose_days_15d` | `int` |  | 近15天权益中心icon曝光天数 |
| 6 | `rc_expose_days_30d` | `int` |  | 近30天权益中心icon曝光天数 |
| 7 | `rc_expose_days_90d` | `int` |  | 近90天权益中心icon曝光天数 |
| 8 | `rc_expose_days_180d` | `int` |  | 近180天权益中心icon曝光天数 |
| 9 | `rc_click_count_1d` | `int` |  | 近1天权益中心icon点击次数 |
| 10 | `rc_click_count_7d` | `int` |  | 近7天权益中心icon点击次数 |
| 11 | `rc_click_count_15d` | `int` |  | 近15天权益中心icon点击次数 |
| 12 | `rc_click_count_30d` | `int` |  | 近30天权益中心icon点击次数 |
| 13 | `rc_click_count_90d` | `int` |  | 近90天权益中心icon点击次数 |
| 14 | `rc_click_count_180d` | `int` |  | 近180天权益中心icon点击次数 |
| 15 | `rc_click_days_1d` | `int` |  | 近1天权益中心icon点击天数 |
| 16 | `rc_click_days_7d` | `int` |  | 近7天权益中心icon点击天数 |
| 17 | `rc_click_days_15d` | `int` |  | 近15天权益中心icon点击天数 |
| 18 | `rc_click_days_30d` | `int` |  | 近30天权益中心icon点击天数 |
| 19 | `rc_click_days_90d` | `int` |  | 近90天权益中心icon点击天数 |
| 20 | `rc_click_days_180d` | `int` |  | 近180天权益中心icon点击天数 |
| 21 | `pve_expose_days_1d` | `int` |  | 近1天破次元icon曝光天数 |
| 22 | `pve_expose_days_7d` | `int` |  | 近7天破次元icon曝光天数 |
| 23 | `pve_expose_days_15d` | `int` |  | 近15天破次元icon曝光天数 |
| 24 | `pve_expose_days_30d` | `int` |  | 近30天破次元icon曝光天数 |
| 25 | `pve_expose_days_90d` | `int` |  | 近90天破次元icon曝光天数 |
| 26 | `pve_expose_days_180d` | `int` |  | 近180天破次元icon曝光天数 |
| 27 | `pve_click_count_1d` | `int` |  | 近1天破次元icon点击次数 |
| 28 | `pve_click_count_7d` | `int` |  | 近7天破次元icon点击次数 |
| 29 | `pve_click_count_15d` | `int` |  | 近15天破次元icon点击次数 |
| 30 | `pve_click_count_30d` | `int` |  | 近30天破次元icon点击次数 |
| 31 | `pve_click_count_90d` | `int` |  | 近90天破次元icon点击次数 |
| 32 | `pve_click_count_180d` | `int` |  | 近180天破次元icon点击次数 |
| 33 | `pve_click_days_1d` | `int` |  | 近1天破次元icon点击天数 |
| 34 | `pve_click_days_7d` | `int` |  | 近7天破次元icon点击天数 |
| 35 | `pve_click_days_15d` | `int` |  | 近15天破次元icon点击天数 |
| 36 | `pve_click_days_30d` | `int` |  | 近30天破次元icon点击天数 |
| 37 | `pve_click_days_90d` | `int` |  | 近90天破次元icon点击天数 |
| 38 | `pve_click_days_180d` | `int` |  | 近180天破次元icon点击天数 |
| 39 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_par_user_home_visit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_home_visit_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 178.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 178.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 访问用户Id |
| 2 | `blogid` | `bigint` |  | 被访问首页博客id |
| 3 | `blogname` | `string` |  | 被访问首页博客昵称 |
| 4 | `visitcount` | `bigint` |  | 访问次数 |
| 5 | `dt` | `string` |  | 日期 |

---

## dws_par_user_interaction_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_interaction_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1880.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1880.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 52 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `send_hot_std` | `bigint` |  |  |
| 3 | `send_hot_7d` | `bigint` |  |  |
| 4 | `send_hot_15d` | `bigint` |  |  |
| 5 | `send_hot_30d` | `bigint` |  |  |
| 6 | `send_hot_90d` | `bigint` |  |  |
| 7 | `send_hot_180d` | `bigint` |  |  |
| 8 | `send_like_cnt` | `bigint` |  |  |
| 9 | `send_reproduce_cnt` | `bigint` |  |  |
| 10 | `send_recommend_cnt` | `bigint` |  |  |
| 11 | `send_collect_cnt` | `bigint` |  |  |
| 12 | `send_comment_cnt_std` | `bigint` |  |  |
| 13 | `send_comment_cnt_7d` | `bigint` |  |  |
| 14 | `send_comment_cnt_15d` | `bigint` |  |  |
| 15 | `send_comment_cnt_30d` | `bigint` |  |  |
| 16 | `send_comment_cnt_90d` | `bigint` |  |  |
| 17 | `send_comment_cnt_180d` | `bigint` |  |  |
| 18 | `send_like_cnt_7d` | `bigint` |  | 7日发送的点赞量 |
| 19 | `send_like_cnt_30d` | `bigint` |  | 30日发送的点赞量 |
| 20 | `send_reproduce_cnt_7d` | `bigint` |  | 7日发送的转载量 |
| 21 | `send_reproduce_cnt_30d` | `bigint` |  | 30日发送的转载量 |
| 22 | `send_recommend_cnt_7d` | `bigint` |  | 7日发送的推荐量 |
| 23 | `send_recommend_cnt_30d` | `bigint` |  | 30日发送的推荐量 |
| 24 | `send_collect_cnt_7d` | `bigint` |  | 7日发送的收藏量 |
| 25 | `send_collect_cnt_30d` | `bigint` |  | 30日发送的收藏量 |
| 26 | `send_comment_cnt_1y` | `bigint` |  | 近一年的评论量 |
| 27 | `send_comment_interaction_cnt_30d` | `bigint` |  | 近30天评论被互动次数 |
| 28 | `send_comment_hot_30d` | `bigint` |  | 30天内评论被点赞数 |
| 29 | `send_comment_reply_cnt_30d` | `bigint` |  | 30天内评论被回复数 |
| 30 | `send_comment_post_cnt` | `bigint` |  | 送出评论的文章 |
| 31 | `send_comment_blog_cnt` | `bigint` |  | 送出评论的博客 |
| 32 | `send_like_post_cnt` | `bigint` |  | 送出喜欢的文章 |
| 33 | `send_like_blog_cnt` | `bigint` |  | 送出喜欢的博客 |
| 34 | `send_reproduce_post_cnt` | `bigint` |  | 送出装载的文章数量 |
| 35 | `send_reproduce_blog_cnt` | `bigint` |  | 送出装载的博客数量 |
| 36 | `send_recommend_post_cnt` | `bigint` |  | 送出推荐的文章数量 |
| 37 | `send_recommend_blog_cnt` | `bigint` |  | 送出推荐的博客数量 |
| 38 | `send_collect_post_cnt` | `bigint` |  | 送出收藏的文章数量 |
| 39 | `send_collect_blog_cnt` | `bigint` |  | 送出收藏的博客数量 |
| 40 | `send_like_cnt_90d` | `bigint` |  | 90日发送的点赞量 |
| 41 | `send_like_cnt_180d` | `bigint` |  | 180日发送的点赞量 |
| 42 | `send_reproduce_cnt_90d` | `bigint` |  | 90日发送的转载量 |
| 43 | `send_reproduce_cnt_180d` | `bigint` |  | 180日发送的转载量 |
| 44 | `send_recommend_cnt_90d` | `bigint` |  | 90日发送的蓝手量 |
| 45 | `send_recommend_cnt_180d` | `bigint` |  | 180日发送的蓝手量 |
| 46 | `send_collect_cnt_90d` | `bigint` |  | 90日发送的收藏量 |
| 47 | `send_collect_cnt_180d` | `bigint` |  | 180日发送的收藏量 |
| 48 | `send_like_cnt_15d` | `bigint` |  | 15日点赞量 |
| 49 | `send_reproduce_cnt_15d` | `bigint` |  | 15日转载量 |
| 50 | `send_recommend_cnt_15d` | `bigint` |  | 15日蓝手量 |
| 51 | `send_collect_cnt_15d` | `bigint` |  | 15日收藏量 |
| 52 | `dt` | `string` |  |  |

---

## dws_par_user_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 56.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 56.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 62 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `text_send_like_cnt` | `bigint` |  |  |
| 3 | `photo_send_like_cnt` | `bigint` |  |  |
| 4 | `video_send_like_cnt` | `bigint` |  |  |
| 5 | `article_send_like_cnt` | `bigint` |  |  |
| 6 | `music_send_like_cnt` | `bigint` |  |  |
| 7 | `question_send_like_cnt` | `bigint` |  |  |
| 8 | `text_send_reproduce_cnt` | `bigint` |  |  |
| 9 | `photo_send_reproduce_cnt` | `bigint` |  |  |
| 10 | `video_send_reproduce_cnt` | `bigint` |  |  |
| 11 | `article_send_reproduce_cnt` | `bigint` |  |  |
| 12 | `music_send_reproduce_cnt` | `bigint` |  |  |
| 13 | `question_send_reproduce_cnt` | `bigint` |  |  |
| 14 | `text_send_recommend_cnt` | `bigint` |  |  |
| 15 | `photo_send_recommend_cnt` | `bigint` |  |  |
| 16 | `video_send_recommend_cnt` | `bigint` |  |  |
| 17 | `article_send_recommend_cnt` | `bigint` |  |  |
| 18 | `music_send_recommend_cnt` | `bigint` |  |  |
| 19 | `question_send_recommend_cnt` | `bigint` |  |  |
| 20 | `text_send_collect_cnt` | `bigint` |  |  |
| 21 | `photo_send_collect_cnt` | `bigint` |  |  |
| 22 | `video_send_collect_cnt` | `bigint` |  |  |
| 23 | `article_send_collect_cnt` | `bigint` |  |  |
| 24 | `music_send_collect_cnt` | `bigint` |  |  |
| 25 | `question_send_collect_cnt` | `bigint` |  |  |
| 26 | `text_send_comment_cnt` | `bigint` |  |  |
| 27 | `photo_send_comment_cnt` | `bigint` |  |  |
| 28 | `video_send_comment_cnt` | `bigint` |  |  |
| 29 | `article_send_comment_cnt` | `bigint` |  |  |
| 30 | `music_send_comment_cnt` | `bigint` |  |  |
| 31 | `question_send_comment_cnt` | `bigint` |  |  |
| 32 | `send_hot` | `bigint` |  |  |
| 33 | `send_like_cnt` | `bigint` |  |  |
| 34 | `send_reproduce_cnt` | `bigint` |  |  |
| 35 | `send_recommend_cnt` | `bigint` |  |  |
| 36 | `send_collect_cnt` | `bigint` |  |  |
| 37 | `send_comment_cnt` | `bigint` |  |  |
| 38 | `send_valid_comment_count` | `bigint` |  | 有效评论数 |
| 39 | `send_novalid_comment_count` | `bigint` |  | 无效评论数 |
| 40 | `like_pv` | `bigint` |  | 排除问答的文章点赞量 |
| 41 | `reproduce_pv` | `bigint` |  | 排除问答的文章转载量 |
| 42 | `recommend_pv` | `bigint` |  | 排除问答的文章推荐量 |
| 43 | `collect_pv` | `bigint` |  | 排除问答的文章收藏量 |
| 44 | `hot_pv` | `bigint` |  | 排除问答的文章热度量 |
| 45 | `valid_comment_pv` | `bigint` |  | 排除问答的文章有效评论量 |
| 46 | `novalid_comment_pv` | `bigint` |  | 排除问答的文章无效评论量 |
| 47 | `comment_pv` | `bigint` |  | 排除问答的文章评论量 |
| 48 | `reply_comment_pv` | `bigint` |  | 排除问答的文章评论回复量 |
| 49 | `comment_like_pv` | `bigint` |  | 排除问答的文章评论点赞量 |
| 50 | `underscore_comment_pv` | `bigint` |  | 划线评数 |
| 51 | `share_pv` | `bigint` |  | 分享数 |
| 52 | `follow_uv` | `bigint` |  | 关注作者数 |
| 53 | `tag_subscribe_pv` | `bigint` |  | 订阅标签数 |
| 54 | `collection_subscribe_pv` | `bigint` |  | 订阅合集数 |
| 55 | `free_gift_present_pv` | `bigint` |  | 赠送免费礼物数 |
| 56 | `underscore_reply_pv` | `bigint` |  | 划线评回复数 |
| 57 | `sweet_marks` | `bigint` |  | 甜标记数 |
| 58 | `bitter_marks` | `bigint` |  | 虐标记数 |
| 59 | `circle_comment_pv` | `bigint` |  | 圈评pv |
| 60 | `circle_comment_reply_pv` | `bigint` |  | 圈评回复pv |
| 61 | `haha_marks` | `bigint` |  | 哈哈标记数 |
| 62 | `dt` | `string` |  |  |

---

## dws_par_user_ip_create_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_ip_create_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 150.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 150.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `ip` | `string` |  | ip |
| 3 | `post_count_acc` | `bigint` |  | 用户该ip下的累计发文数 |
| 4 | `post_count_1d` | `bigint` |  | 用户该ip下的1日发文数 |
| 5 | `post_count_7d` | `bigint` |  | 用户该ip下的7日发文数 |
| 6 | `post_count_30d` | `bigint` |  | 用户该ip下的30日发文数 |
| 7 | `post_count_365d` | `bigint` |  | 用户该ip下的一年发文数 |
| 8 | `post_count_15d` | `bigint` |  | 用户在该ip下的15日发文 |
| 9 | `post_count_90d` | `bigint` |  | 用户在该ip下的90日发文 |
| 10 | `post_count_180d` | `bigint` |  | 用户在该ip下的180日发文 |
| 11 | `dt` | `string` |  |  |

---

## dws_par_user_ip_prefer_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_ip_prefer_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 41.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 41.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `top_ips` | `array<string>` |  | 近30日偏好ip， 取前浏览文章数前5名 |
| 3 | `dt` | `string` |  |  |

---

## dws_par_user_misc_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_misc_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 24.5M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `grains` | `bigint` |  | 当日创建粮单数 |
| 3 | `dt` | `string` |  |  |

---

## dws_par_user_pay_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_pay_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 203.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 203.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 38 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 充值消费用户id |
| 2 | `coin_consume_std` | `double` |  | 累计乐乎币消费, 单位元 |
| 3 | `coin_consume_7d` | `double` |  | 近7日乐乎币消费, 单位元 |
| 4 | `coin_consume_15d` | `double` |  | 近15日乐乎币消费, 单位元 |
| 5 | `coin_consume_30d` | `double` |  | 近30日乐乎币消费, 单位元 |
| 6 | `coin_consume_90d` | `double` |  | 近90日乐乎币消费, 单位元 |
| 7 | `coin_consume_180d` | `double` |  | 近180日乐乎币消费, 单位元 |
| 8 | `grain_ticket_consume_std` | `bigint` |  | 累计粮票消费 |
| 9 | `grain_ticket_consume_7d` | `bigint` |  | 近7日粮票消费 |
| 10 | `grain_ticket_consume_15d` | `bigint` |  | 近15日粮票消费 |
| 11 | `grain_ticket_consume_30d` | `bigint` |  | 近30日粮票消费 |
| 12 | `grain_ticket_consume_90d` | `bigint` |  | 近90日粮票消费 |
| 13 | `grain_ticket_consume_180d` | `bigint` |  | 近180日粮票消费 |
| 14 | `coin_recharge_std` | `double` |  | 累计乐乎币充值金额, 单位元 |
| 15 | `coin_recharge_7d` | `double` |  | 近7日乐乎币充值金额, 单位元 |
| 16 | `coin_recharge_15d` | `double` |  | 近15日乐乎币充值金额, 单位元 |
| 17 | `coin_recharge_30d` | `double` |  | 近30日乐乎币充值金额, 单位元 |
| 18 | `coin_recharge_90d` | `double` |  | 近90日乐乎币充值金额, 单位元 |
| 19 | `coin_recharge_180d` | `double` |  | 近180日乐乎币充值金额, 单位元 |
| 20 | `fans_consume_std` | `double` |  | 累计高粉会员消费金额, 单位元 |
| 21 | `fans_consume_30d` | `double` |  | 近30日高粉会员消费金额, 单位元 |
| 22 | `coupon_consume_std` | `double` |  | 累计券包消费金额, 单位元 |
| 23 | `coupon_consume_30d` | `double` |  | 近30日券包消费金额, 单位元 |
| 24 | `coupon_unlock_money_std` | `double` |  | 累计券包解锁金额 |
| 25 | `coupon_unlock_money_30d` | `double` |  | 近30日券包解锁金额 |
| 26 | `fans_consume_7d` | `double` |  |  |
| 27 | `coupon_consume_7d` | `double` |  |  |
| 28 | `coupon_unlock_money_7d` | `double` |  |  |
| 29 | `fans_consume_1d` | `double` |  |  |
| 30 | `fans_consume_15d` | `double` |  |  |
| 31 | `fans_consume_90d` | `double` |  |  |
| 32 | `fans_consume_180d` | `double` |  |  |
| 33 | `coin_consume_1d` | `double` |  |  |
| 34 | `coupon_consume_1d` | `double` |  |  |
| 35 | `coupon_consume_15d` | `double` |  |  |
| 36 | `coupon_consume_90d` | `double` |  |  |
| 37 | `coupon_consume_180d` | `double` |  |  |
| 38 | `dt` | `string` |  |  |

---

## dws_par_user_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_post_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 23.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 23.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 85 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `post_count_std` | `bigint` |  | 累计发文数 |
| 3 | `valid_post_count_std` | `bigint` |  | 累计有效发文(包含删除) |
| 4 | `post_count_since_2020` | `bigint` |  | 2020.01.01起发文数 |
| 5 | `first_publish_time` | `bigint` |  | 首次发文时间 |
| 6 | `first_valid_publish_time` | `bigint` |  | 首次有效发文时间 |
| 7 | `last_publish_time` | `bigint` |  | 最新发文时间 |
| 8 | `last_valid_publish_time` | `bigint` |  | 最新有效发文时间 |
| 9 | `content_types` | `array<string>` |  | 发文类型 |
| 10 | `post_count_1d` | `bigint` |  | 近1日发文 |
| 11 | `post_count_7d` | `bigint` |  | 近7日发文 |
| 12 | `post_count_30d` | `bigint` |  | 近30日发文 |
| 13 | `core_post_count_std` | `bigint` |  | 核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非引入非迁移 |
| 14 | `core_post_text_std` | `bigint` |  | 文本文章核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非引入非迁移 |
| 15 | `core_post_photo_std` | `bigint` |  | 图片核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非引入非迁移 |
| 16 | `core_post_video_std` | `bigint` |  | 视频核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 17 | `core_post_longtext_std` | `bigint` |  | 长文章核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非引入非迁移 |
| 18 | `core_post_answer_std` | `bigint` |  | 问答核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非引入非迁移 |
| 19 | `core_post_chat_std` | `bigint` |  | 聊聊核心发文累计指标：  口径于日报数据一致 公开可见, 非屏蔽非转载非引入非迁移 |
| 20 | `creator_post_count_std` | `bigint` |  | 创作者发文累计指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 21 | `creator_post_count_1d` | `bigint` |  | 创作者发文1天指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 22 | `creator_post_count_7d` | `bigint` |  | 创作者发文7日指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 23 | `creator_post_count_15d` | `bigint` |  | 创作者发文15日指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 24 | `creator_post_count_30d` | `bigint` |  | 创作者发文30日指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 25 | `creator_post_count_90d` | `bigint` |  | 创作者发文90日指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 26 | `creator_post_count_180d` | `bigint` |  | 创作者发文180日指标：  公开可见, 非屏蔽非转载非问答非聊聊非引入非迁移 |
| 27 | `rec_pool_post_std` | `bigint` |  | 累计入池文章数 |
| 28 | `rec_pool_todo_post_std` | `bigint` |  | 累日推荐池待审核文章数 |
| 29 | `rec_pool_accept_post_std` | `bigint` |  | 累计推荐池审核通过文章数 |
| 30 | `rec_pool_reject_post_std` | `bigint` |  | 累计推荐池审核拒绝文章数 |
| 31 | `rec_pool_post_180d` | `bigint` |  | 近180天入池文章数 |
| 32 | `rec_pool_review_post_180d` | `bigint` |  | 近180天审核文章数 |
| 33 | `cp_post_count` | `bigint` |  |  |
| 34 | `non_cp_post_count` | `bigint` |  |  |
| 35 | `risk_normal_post_count_std` | `bigint` |  | 累计发文正常量 |
| 36 | `risk_normal_post_count_1d` | `bigint` |  | 1日发文正常量 |
| 37 | `risk_normal_post_count_7d` | `bigint` |  | 7日发文正常量 |
| 38 | `risk_normal_post_count_30d` | `bigint` |  | 30日发文正常量 |
| 39 | `risk_delete_post_count_std` | `bigint` |  | 累计发文删除量 |
| 40 | `risk_delete_post_count_1d` | `bigint` |  | 1日发文删除量 |
| 41 | `risk_delete_post_count_7d` | `bigint` |  | 7日发文删除量 |
| 42 | `risk_delete_post_count_30d` | `bigint` |  | 30日发文删除量 |
| 43 | `risk_mask_post_count_std` | `bigint` |  | 累计发文屏蔽量 |
| 44 | `risk_mask_post_count_1d` | `bigint` |  | 1日发文屏蔽量 |
| 45 | `risk_mask_post_count_7d` | `bigint` |  | 7日发文屏蔽量 |
| 46 | `risk_mask_post_count_30d` | `bigint` |  | 30日发文屏蔽量 |
| 47 | `risk_audit_view_self_post_count_std` | `bigint` |  | 累计发文处于待审核或仅自己可见的数量 |
| 48 | `risk_audit_view_self_post_count_1d` | `bigint` |  | 1日发文处于待审核或仅自己可见的数量 |
| 49 | `risk_audit_view_self_post_count_7d` | `bigint` |  | 7日发文处于待审核或仅自己可见的数量 |
| 50 | `risk_audit_view_self_post_count_30d` | `bigint` |  | 30日发文处于待审核或仅自己可见的数量 |
| 51 | `post_count_365d` | `bigint` |  | 近1年的发文数 |
| 52 | `core_post_text_7d` | `bigint` |  |  |
| 53 | `core_post_photo_7d` | `bigint` |  |  |
| 54 | `core_post_video_7d` | `bigint` |  |  |
| 55 | `core_post_longtext_7d` | `bigint` |  |  |
| 56 | `core_post_answer_7d` | `bigint` |  |  |
| 57 | `core_post_chat_7d` | `bigint` |  |  |
| 58 | `core_post_text_15d` | `bigint` |  |  |
| 59 | `core_post_photo_15d` | `bigint` |  |  |
| 60 | `core_post_video_15d` | `bigint` |  |  |
| 61 | `core_post_longtext_15d` | `bigint` |  |  |
| 62 | `core_post_answer_15d` | `bigint` |  |  |
| 63 | `core_post_chat_15d` | `bigint` |  |  |
| 64 | `core_post_text_30d` | `bigint` |  |  |
| 65 | `core_post_photo_30d` | `bigint` |  |  |
| 66 | `core_post_video_30d` | `bigint` |  |  |
| 67 | `core_post_longtext_30d` | `bigint` |  |  |
| 68 | `core_post_answer_30d` | `bigint` |  |  |
| 69 | `core_post_chat_30d` | `bigint` |  |  |
| 70 | `core_post_text_90d` | `bigint` |  |  |
| 71 | `core_post_photo_90d` | `bigint` |  |  |
| 72 | `core_post_video_90d` | `bigint` |  |  |
| 73 | `core_post_longtext_90d` | `bigint` |  |  |
| 74 | `core_post_answer_90d` | `bigint` |  |  |
| 75 | `core_post_chat_90d` | `bigint` |  |  |
| 76 | `core_post_text_180d` | `bigint` |  |  |
| 77 | `core_post_photo_180d` | `bigint` |  |  |
| 78 | `core_post_video_180d` | `bigint` |  |  |
| 79 | `core_post_longtext_180d` | `bigint` |  |  |
| 80 | `core_post_answer_180d` | `bigint` |  |  |
| 81 | `core_post_chat_180d` | `bigint` |  |  |
| 82 | `post_count_90d` | `bigint` |  | 近90天发文数 |
| 83 | `post_count_180d` | `bigint` |  | 近180天发文数 |
| 84 | `post_count_15d` | `bigint` |  | 近15天发文数 |
| 85 | `dt` | `string` |  |  |

---

## dws_par_user_push_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_push_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 40.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 40.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `is_push_on` | `int` |  | push开关状态 1开 0关 |
| 3 | `update_time` | `bigint` |  | 最近push开关状态更新时间 |
| 4 | `dt` | `string` |  |  |

---

## dws_par_user_revenue_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_revenue_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 104.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 104.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 46 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `receive_gift_amount_std` | `double` |  |  |
| 3 | `receive_gift_amount_1d` | `double` |  |  |
| 4 | `receive_gift_amount_7d` | `double` |  |  |
| 5 | `receive_gift_amount_15d` | `double` |  |  |
| 6 | `receive_gift_amount_30d` | `double` |  |  |
| 7 | `receive_gift_amount_90d` | `double` |  |  |
| 8 | `receive_gift_amount_180d` | `double` |  |  |
| 9 | `receive_reward_amount_std` | `double` |  |  |
| 10 | `receive_reward_amount_1d` | `double` |  |  |
| 11 | `receive_reward_amount_7d` | `double` |  |  |
| 12 | `receive_reward_amount_15d` | `double` |  |  |
| 13 | `receive_reward_amount_30d` | `double` |  |  |
| 14 | `receive_reward_amount_90d` | `double` |  |  |
| 15 | `receive_reward_amount_180d` | `double` |  |  |
| 16 | `send_gift_amount_std` | `double` |  |  |
| 17 | `send_gift_amount_1d` | `double` |  |  |
| 18 | `send_gift_amount_7d` | `double` |  |  |
| 19 | `send_gift_amount_15d` | `double` |  |  |
| 20 | `send_gift_amount_30d` | `double` |  |  |
| 21 | `send_gift_amount_90d` | `double` |  |  |
| 22 | `send_gift_amount_180d` | `double` |  |  |
| 23 | `send_reward_amount_std` | `double` |  |  |
| 24 | `send_reward_amount_1d` | `double` |  |  |
| 25 | `send_reward_amount_7d` | `double` |  |  |
| 26 | `send_reward_amount_15d` | `double` |  |  |
| 27 | `send_reward_amount_30d` | `double` |  |  |
| 28 | `send_reward_amount_90d` | `double` |  |  |
| 29 | `send_reward_amount_180d` | `double` |  |  |
| 30 | `receive_gift_amount_deduct_std` | `double` |  | 累计收到扣除平台分成后的礼物金额 |
| 31 | `receive_gift_amount_deduct_1d` | `double` |  | 近1日收到扣除平台分成后的礼物金额 |
| 32 | `receive_gift_amount_deduct_7d` | `double` |  | 近7日收到扣除平台分成后的礼物金额 |
| 33 | `receive_gift_amount_deduct_30d` | `double` |  | 近30日收到扣除平台分成后的礼物金额 |
| 34 | `receive_reward_amount_deduct_std` | `double` |  | 累计收到扣除平台分成后的打赏金额 |
| 35 | `receive_reward_amount_deduct_1d` | `double` |  | 近1日收到扣除平台分成后的打赏金额 |
| 36 | `receive_reward_amount_deduct_7d` | `double` |  | 近7日收到扣除平台分成后的打赏金额 |
| 37 | `receive_reward_amount_deduct_30d` | `double` |  | 近30日收到扣除平台分成后的打赏金额 |
| 38 | `send_gift_amount_deduct_std` | `double` |  | 累计送出扣除平台分成后的礼物金额 |
| 39 | `send_gift_amount_deduct_1d` | `double` |  | 近1日送出扣除平台分成后的礼物金额 |
| 40 | `send_gift_amount_deduct_7d` | `double` |  | 近7日送出扣除平台分成后的礼物金额 |
| 41 | `send_gift_amount_deduct_30d` | `double` |  | 近30日送出扣除平台分成后的礼物金额 |
| 42 | `send_reward_amount_deduct_std` | `double` |  | 累计送出扣除平台分成后的打赏金额 |
| 43 | `send_reward_amount_deduct_1d` | `double` |  | 近1日送出扣除平台分成后的打赏金额 |
| 44 | `send_reward_amount_deduct_7d` | `double` |  | 近7日送出扣除平台分成后的打赏金额 |
| 45 | `send_reward_amount_deduct_30d` | `double` |  | 近30日送出扣除平台分成后的打赏金额 |
| 46 | `dt` | `string` |  |  |

---

## dws_par_user_reward_center_active_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_reward_center_active_dd` |
| **描述** | 权益中心用户活跃度汇总表 - 按天聚合 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `is_anonymous` | `tinyint` |  | 是否匿名用户 0-否 1-是 |
| 3 | `active_days_1d` | `int` |  | 近1天活跃天数 |
| 4 | `active_days_7d` | `int` |  | 近7天活跃天数 |
| 5 | `active_days_15d` | `int` |  | 近15天活跃天数 |
| 6 | `active_days_30d` | `int` |  | 近30天活跃天数 |
| 7 | `active_days_90d` | `int` |  | 近90天活跃天数 |
| 8 | `active_days_180d` | `int` |  | 近180天活跃天数 |
| 9 | `last_active_date` | `string` |  | 最后活跃日期(yyyy-MM-dd格式) |
| 10 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_par_user_reward_center_ad_watch_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_reward_center_ad_watch_dd` |
| **描述** | 权益中心用户看广告次数汇总表 - 按天聚合 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 447.1M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `is_anonymous` | `tinyint` |  | 是否匿名用户 0-否 1-是 |
| 3 | `ad_watch_count_1d` | `int` |  | 近1天看广告次数 |
| 4 | `ad_watch_count_7d` | `int` |  | 近7天看广告次数 |
| 5 | `ad_watch_count_15d` | `int` |  | 近15天看广告次数 |
| 6 | `ad_watch_count_30d` | `int` |  | 近30天看广告次数 |
| 7 | `ad_watch_count_90d` | `int` |  | 近90天看广告次数 |
| 8 | `ad_watch_count_180d` | `int` |  | 近180天看广告次数 |
| 9 | `ad_watch_count_total` | `int` |  | 累计看广告次数 |
| 10 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_par_user_reward_center_exchange_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_reward_center_exchange_dd` |
| **描述** | 权益中心用户商品兑换汇总表 - 累计数据 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | internal |
| **表大小** | 94.1M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `grain_ticket_count_total` | `int` |  | 粮票兑换总次数 |
| 3 | `coupon_count_total` | `int` |  | 糖果券兑换总次数 |
| 4 | `pve_stamina_count_total` | `int` |  | 虚拟人体力兑换总次数 |
| 5 | `ip_coupon_count_total` | `int` |  | IP糖果券兑换总次数 |
| 6 | `emote_count_total` | `int` |  | 表情包兑换总次数 |
| 7 | `avatarbox_count_total` | `int` |  | 头像框兑换总次数 |
| 8 | `comment_count_total` | `int` |  | 评论气泡兑换总次数 |
| 9 | `skin_count_total` | `int` |  | 主题装扮兑换总次数 |
| 10 | `red_packet_count_total` | `int` |  | 谷票（红包）兑换总次数 |
| 11 | `regret_card_count_total` | `int` |  | 后悔卡兑换总次数 |
| 12 | `boot_screen_count_total` | `int` |  | 定制开屏兑换总次数 |
| 13 | `yinge_count_total` | `int` |  | 印鸽兑换总次数 |
| 14 | `cash_count_total` | `int` |  | 现金兑换总次数 |
| 15 | `media_vip_count_total` | `int` |  | 影视音会员兑换总次数 |
| 16 | `music_vip_count_total` | `int` |  | 云音乐会员兑换总次数 |
| 17 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_par_user_session_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_session_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 61.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 61.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `session_count` | `bigint` |  | 会话次数 |
| 3 | `session_total_time` | `bigint` |  | 会话总时长 单位毫秒 |
| 4 | `dt` | `string` |  |  |

---

## dws_par_user_stratify_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_stratify_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 47.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 47.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `valid_pv` | `bigint` |  |  |
| 3 | `send_hot` | `bigint` |  |  |
| 4 | `send_comment_cnt` | `bigint` |  |  |
| 5 | `follow` | `bigint` |  |  |
| 6 | `share_cnt` | `bigint` |  |  |
| 7 | `discuss` | `bigint` |  |  |
| 8 | `post_cnt` | `bigint` |  |  |
| 9 | `trade_money` | `double` |  |  |
| 10 | `dt` | `string` |  |  |

---

## dws_par_user_tag_create_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_tag_create_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 124.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 124.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `tag` | `string` |  | tag |
| 3 | `post_count_acc` | `bigint` |  | 用户该Tag下的累计发文数 |
| 4 | `post_count_1d` | `bigint` |  | 用户该Tag下的1日发文数 |
| 5 | `post_count_7d` | `bigint` |  | 用户该Tag下的7日发文数 |
| 6 | `post_count_30d` | `bigint` |  | 用户该Tag下的30日发文数 |
| 7 | `post_count_365d` | `bigint` |  | 用户该Tag下的一年发文数 |
| 8 | `post_count_15d` | `bigint` |  | 用户在该tag下的15日发文 |
| 9 | `post_count_90d` | `bigint` |  | 用户在该tag下的90日发文 |
| 10 | `post_count_180d` | `bigint` |  | 用户在该tag下的180日发文 |
| 11 | `dt` | `string` |  |  |

---

## dws_par_user_traffic_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_par_user_traffic_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 125.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 125.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `paid_post_expose_count_30d` | `bigint` |  | 近30日付费文章曝光文章数 |
| 3 | `paid_post_browse_count_30d` | `bigint` |  | 近30日付费文章浏览文章数 |
| 4 | `paid_post_expose_count_7d` | `bigint` |  |  |
| 5 | `paid_post_browse_count_7d` | `bigint` |  |  |
| 6 | `post_browse_count_30d` | `bigint` |  | 30天浏览文章数 |
| 7 | `real_post_browse_count_30d` | `bigint` |  | 30天有效浏览文章数 |
| 8 | `dt` | `string` |  |  |

---

## dws_post_base_stats_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_base_stats_dd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 5850.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5850.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 68 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章Id |
| 2 | `clickpv` | `bigint` |  | 文章累积点击量,从2021-01-01至今 |
| 3 | `clickuv` | `bigint` |  | 文章点击用户数 |
| 4 | `browsepv` | `bigint` |  | 文章浏览数 |
| 5 | `browseuv` | `bigint` |  | 文章浏览人数 |
| 6 | `exposurepv` | `bigint` |  | 曝光数 |
| 7 | `exposureuv` | `bigint` |  | 曝光用户数 |
| 8 | `realbrowsepv` | `bigint` |  | 真实浏览量 |
| 9 | `realbrowseuv` | `bigint` |  | 真实浏览用户数 |
| 10 | `browsetime` | `bigint` |  | 浏览时长 |
| 11 | `realbrowsetime` | `bigint` |  | 真实浏览时长 |
| 12 | `realbrowsenewuv` | `bigint` |  | 真实浏览新用户数,新用户的定义是注册在当日 |
| 13 | `playpv` | `bigint` |  | 视频播放量 |
| 14 | `playuv` | `bigint` |  | 视频播放人数 |
| 15 | `playtime` | `double` |  | 视频播放时长 |
| 16 | `playprogress` | `double` |  | 视频播放进度 |
| 17 | `realplaypv` | `bigint` |  | 真实播放量 |
| 18 | `realplayuv` | `bigint` |  | 真实播放人数 |
| 19 | `realplaytime` | `double` |  | 真实播放时长 |
| 20 | `realplayprogress` | `double` |  | 真实播放进度 |
| 21 | `finishplaypv` | `bigint` |  | 视频完播数 |
| 22 | `finishplayuv` | `bigint` |  | 视频完播人数 |
| 23 | `realfinishplaypv` | `bigint` |  | 视频真实完播数 废弃 |
| 24 | `realfinishplayuv` | `bigint` |  | 视频真实完播人数 废弃 |
| 25 | `negfeedbackpv` | `bigint` |  | 文章负反馈数 |
| 26 | `negfeedbackuv` | `bigint` |  | 文章负反馈人数 |
| 27 | `sharepv` | `bigint` |  | 文章分享数 |
| 28 | `shareuv` | `bigint` |  | 文章分享用户数 |
| 29 | `pospraisepv` | `bigint` |  | 文章正向喜欢数 |
| 30 | `pospraiseuv` | `bigint` |  | 文章正向喜欢人数 |
| 31 | `negpraisepv` | `bigint` |  | 文章负向喜欢数 |
| 32 | `negpraiseuv` | `bigint` |  | 文章负向喜欢人数 |
| 33 | `posrecuv` | `bigint` |  | 文章正向推荐数 |
| 34 | `posrecpv` | `bigint` |  | 文章正向推荐人数 |
| 35 | `negrecpv` | `bigint` |  | 文章负向推荐数 |
| 36 | `negrecuv` | `bigint` |  | 文章负向推荐人数 |
| 37 | `posreproducepv` | `bigint` |  | 文章正向转载数 |
| 38 | `posreproduceuv` | `bigint` |  | 文章正向转载人数 |
| 39 | `negreproducepv` | `bigint` |  | 文章负向转载数 |
| 40 | `negreproduceuv` | `bigint` |  | 文章负向转载人数 |
| 41 | `possubscribepv` | `bigint` |  | 文章正向收藏数 |
| 42 | `possubscribeuv` | `bigint` |  | 文章正向收藏人数 |
| 43 | `negsubscribepv` | `bigint` |  | 文章负向收藏数 |
| 44 | `negsubscribeuv` | `bigint` |  | 文章负向收藏人数 |
| 45 | `poshotpv` | `bigint` |  | 文章正向热度数 |
| 46 | `poshotuv` | `bigint` |  | 文章正向热度人数 |
| 47 | `neghotpv` | `bigint` |  | 文章负向热度数 |
| 48 | `neghotuv` | `bigint` |  | 文章负向热度人数 |
| 49 | `poscommentpv` | `bigint` |  | 文章正向评论数 |
| 50 | `poscommentuv` | `bigint` |  | 文章正向评论人数 |
| 51 | `negcommentpv` | `bigint` |  | 文章负向评论数 |
| 52 | `negcommentuv` | `bigint` |  | 文章负向评论人数 |
| 53 | `hdpv` | `bigint` |  | 文章互动量,正向 |
| 54 | `hduv` | `bigint` |  | 文章互动人数 |
| 55 | `rewarduv` | `bigint` |  | 文章打赏人数 |
| 56 | `rewardamount` | `double` |  | 文章打赏金额 |
| 57 | `freegiftuv` | `bigint` |  | 免费送礼用户数 |
| 58 | `freegiftamount` | `bigint` |  | 免费送礼个数 |
| 59 | `chargegiftuv` | `bigint` |  | 付费送礼用户数 |
| 60 | `chargegiftamount` | `bigint` |  | 付费送礼金额，单位是乐乎币，转成元需要*0.1 |
| 61 | `centralizedexposurepv` | `bigint` |  | 中心化流量曝光流量pv 20230509添加 下同 |
| 62 | `centralizedexposureuv` | `bigint` |  | 中心化流量曝光流量uv |
| 63 | `noncentralizedexposurepv` | `bigint` |  | 非中心化流量曝光流量pv |
| 64 | `noncentralizedexposureuv` | `bigint` |  | 非中心化流量曝光流量uv |
| 65 | `valid_response` | `bigint` |  | 有效评论量 |
| 66 | `support_exposure_pv` | `bigint` |  | 扶持曝光量 |
| 67 | `support_induced_pv` | `bigint` |  | 扶持引导有效pv |
| 68 | `dt` | `string` |  |  |

---

## dws_post_base_stats_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_base_stats_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 4706.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 4706.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 88 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章Id |
| 2 | `contenttype` | `string` |  | 文章类别 |
| 3 | `userid` | `bigint` |  | 用户Id（作者id） |
| 4 | `url` | `string` |  | 文章URL链接 |
| 5 | `publishdate` | `string` |  | 文章发布时间 |
| 6 | `tags` | `array<string>` |  | 文章标签 |
| 7 | `firsttag` | `string` |  | 一级分类 废弃 |
| 8 | `secondtag` | `string` |  | 二级分类 废弃 |
| 9 | `thirdtag` | `string` |  | 三级分类 废弃 |
| 10 | `securitylevel` | `int` |  | 文章安全等级,推荐用 |
| 11 | `recstatus` | `string` |  | 推荐状态 |
| 12 | `enterauditdate` | `string` |  | 审核入库时间 废弃 |
| 13 | `enterrecdate` | `string` |  | 入推荐池时间 废弃 |
| 14 | `userlevel` | `string` |  | 用户账号级别 |
| 15 | `qualityscore` | `double` |  | 文章质量分 废弃 |
| 16 | `wordsnum` | `int` |  | 文章字数,该字段废弃 |
| 17 | `photonum` | `int` |  | 文章图片数,该字段废弃 |
| 18 | `platformtype` | `string` |  | 文章来源渠道 |
| 19 | `duration` | `bigint` |  | 视频时长 废弃 |
| 20 | `imgheight` | `bigint` |  | 视频高度 废弃 |
| 21 | `imgwidth` | `bigint` |  | 视频宽度 废弃 |
| 22 | `clickpv` | `bigint` |  | 文章点击量 |
| 23 | `clickuv` | `bigint` |  | 文章点击用户数 |
| 24 | `browsepv` | `bigint` |  | 文章浏览数 |
| 25 | `browseuv` | `bigint` |  | 文章浏览人数 |
| 26 | `exposurepv` | `bigint` |  | 曝光数 |
| 27 | `exposureuv` | `bigint` |  | 曝光用户数 |
| 28 | `realbrowsepv` | `bigint` |  | 真实浏览量 |
| 29 | `realbrowseuv` | `bigint` |  | 真实浏览用户数 |
| 30 | `browsetime` | `bigint` |  | 浏览时长 |
| 31 | `realbrowsetime` | `bigint` |  | 真实浏览时长 |
| 32 | `realbrowsenewuv` | `bigint` |  | 真实浏览新用户数,新用户的定义是注册在当日 |
| 33 | `playpv` | `bigint` |  | 视频播放量 - 废弃字段 使用browsePv |
| 34 | `playuv` | `bigint` |  | 视频播放人数 - 废弃字段 使用browseUv |
| 35 | `playtime` | `double` |  | 视频播放时长 - 废弃字段 |
| 36 | `playprogress` | `double` |  | 视频播放进度 - 废弃字段 |
| 37 | `realplaypv` | `bigint` |  | 真实播放量 - 废弃字段 使用realBrowsePv |
| 38 | `realplayuv` | `bigint` |  | 真实播放人数 - 废弃字段 使用 realBrowseUv |
| 39 | `realplaytime` | `double` |  | 真实播放时长 - 废弃字段 |
| 40 | `realplayprogress` | `double` |  | 真实播放进度 - 废弃字段 |
| 41 | `finishplaypv` | `bigint` |  | 视频完播数 废弃字段 |
| 42 | `finishplayuv` | `bigint` |  | 视频完播人数 - 废弃字段 |
| 43 | `realfinishplaypv` | `bigint` |  | 视频真实完播数 废弃 |
| 44 | `realfinishplayuv` | `bigint` |  | 视频真实完播人数 废弃 |
| 45 | `negfeedbackpv` | `bigint` |  | 文章负反馈数 |
| 46 | `negfeedbackuv` | `bigint` |  | 文章负反馈人数 |
| 47 | `sharepv` | `bigint` |  | 文章分享数 |
| 48 | `shareuv` | `bigint` |  | 文章分享用户数 |
| 49 | `pospraisepv` | `bigint` |  | 文章正向喜欢数 |
| 50 | `pospraiseuv` | `bigint` |  | 文章正向喜欢人数 |
| 51 | `negpraisepv` | `bigint` |  | 文章负向喜欢数 |
| 52 | `negpraiseuv` | `bigint` |  | 文章负向喜欢人数 |
| 53 | `posrecuv` | `bigint` |  | 文章正向推荐数 |
| 54 | `posrecpv` | `bigint` |  | 文章正向推荐人数 |
| 55 | `negrecpv` | `bigint` |  | 文章负向推荐数 |
| 56 | `negrecuv` | `bigint` |  | 文章负向推荐人数 |
| 57 | `posreproducepv` | `bigint` |  | 文章正向转载数 |
| 58 | `posreproduceuv` | `bigint` |  | 文章正向转载人数 |
| 59 | `negreproducepv` | `bigint` |  | 文章负向转载数 |
| 60 | `negreproduceuv` | `bigint` |  | 文章负向转载人数 |
| 61 | `possubscribepv` | `bigint` |  | 文章正向收藏数 |
| 62 | `possubscribeuv` | `bigint` |  | 文章正向收藏人数 |
| 63 | `negsubscribepv` | `bigint` |  | 文章负向收藏数 |
| 64 | `negsubscribeuv` | `bigint` |  | 文章负向收藏人数 |
| 65 | `poshotpv` | `bigint` |  | 文章正向热度数 |
| 66 | `poshotuv` | `bigint` |  | 文章正向热度人数 |
| 67 | `neghotpv` | `bigint` |  | 文章负向热度数 |
| 68 | `neghotuv` | `bigint` |  | 文章负向热度人数 |
| 69 | `poscommentpv` | `bigint` |  | 文章正向评论数 |
| 70 | `poscommentuv` | `bigint` |  | 文章正向评论人数 |
| 71 | `negcommentpv` | `bigint` |  | 文章负向评论数 |
| 72 | `negcommentuv` | `bigint` |  | 文章负向评论人数 |
| 73 | `hdpv` | `bigint` |  | 文章互动量,正向 |
| 74 | `hduv` | `bigint` |  | 文章互动人数 |
| 75 | `rewarduv` | `bigint` |  | 文章打赏人数 |
| 76 | `rewardamount` | `double` |  | 文章打赏金额 |
| 77 | `freegiftuv` | `bigint` |  | 免费送礼用户数 |
| 78 | `freegiftamount` | `bigint` |  | 免费送礼个数 |
| 79 | `chargegiftuv` | `bigint` |  | 付费送礼用户数 |
| 80 | `chargegiftamount` | `bigint` |  | 付费送礼金额，单位是乐乎币，转成元需要*0.1 |
| 81 | `centralizedexposurepv` | `bigint` |  | 中心化流量曝光流量pv 20230509添加 下同 |
| 82 | `centralizedexposureuv` | `bigint` |  | 中心化流量曝光流量uv |
| 83 | `noncentralizedexposurepv` | `bigint` |  | 非中心化流量曝光流量pv |
| 84 | `noncentralizedexposureuv` | `bigint` |  | 非中心化流量曝光流量uv |
| 85 | `valid_response` | `bigint` |  | 有效评论量 |
| 86 | `support_exposure_pv` | `bigint` |  | 扶持曝光量 |
| 87 | `support_induced_pv` | `bigint` |  | 扶持引导有效pv |
| 88 | `dt` | `string` |  |  |

---

## dws_post_highlight_comment_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_highlight_comment_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3290.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3290.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `blogid` | `bigint` |  | 文章的blogId |
| 3 | `pid` | `string` |  | 划线评论的段落id或者图片的区块id |
| 4 | `interaction_pv` | `bigint` |  | 互动数： 甜+虐+划线评 |
| 5 | `ips` | `array<string>` |  | 文章圈层 |
| 6 | `is_rec` | `int` |  | 是否过推荐审核 |
| 7 | `content` | `string` |  | 划线评内容 |
| 8 | `interaction_pv_7d` | `bigint` |  | 近7日互动数 |
| 9 | `post_publish_date` | `string` |  | 发文日期 |
| 10 | `underscore_comment_pv` | `bigint` |  | 划线评数 |
| 11 | `sweet_pv` | `bigint` |  | 甜标记数 |
| 12 | `bitter_pv` | `bigint` |  | 虐标记数 |
| 13 | `tags` | `array<string>` |  | 文章标签 |
| 14 | `interaction_pv_1d` | `bigint` |  | 近1d互动数 |
| 15 | `interaction_pv_15d` | `bigint` |  | 近7d互动数 |
| 16 | `interaction_pv_30d` | `bigint` |  | 近15d互动数 |
| 17 | `interaction_pv_90d` | `bigint` |  | 近30d互动数 |
| 18 | `underscore_comment_pv_1d` | `bigint` |  | 近1d划线评pv |
| 19 | `underscore_comment_pv_7d` | `bigint` |  | 近7d划线评pv |
| 20 | `underscore_comment_pv_15d` | `bigint` |  | 近15d划线评pv |
| 21 | `underscore_comment_pv_30d` | `bigint` |  | 近30d划线评pv |
| 22 | `underscore_comment_pv_90d` | `bigint` |  | 近90d划线评pv |
| 23 | `sweet_pv_1d` | `bigint` |  | 近1d甜标记pv |
| 24 | `sweet_pv_7d` | `bigint` |  | 近7d甜标记pv |
| 25 | `sweet_pv_15d` | `bigint` |  | 近15d甜标记pv |
| 26 | `sweet_pv_30d` | `bigint` |  | 近30d甜标记pv |
| 27 | `sweet_pv_90d` | `bigint` |  | 近90d甜标记pv |
| 28 | `bitter_pv_1d` | `bigint` |  | 近1d虐标记pv |
| 29 | `bitter_pv_7d` | `bigint` |  | 近7d虐标记pv |
| 30 | `bitter_pv_15d` | `bigint` |  | 近15d虐标记pv |
| 31 | `bitter_pv_30d` | `bigint` |  | 近30d虐标记pv |
| 32 | `bitter_pv_90d` | `bigint` |  | 近90d虐标记pv |
| 33 | `haha_pv` | `bigint` |  | 哈哈标记pv |
| 34 | `haha_pv_1d` | `bigint` |  | 近1d哈哈标记pv |
| 35 | `haha_pv_7d` | `bigint` |  | 近7d哈哈标记pv |
| 36 | `haha_pv_15d` | `bigint` |  | 近15d哈哈标记pv |
| 37 | `haha_pv_30d` | `bigint` |  | 近30d哈哈标记pv |
| 38 | `haha_pv_90d` | `bigint` |  | 近90d哈哈标记pv |
| 39 | `dt` | `string` |  |  |

---

## dws_post_interaction_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_interaction_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 16804.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 16804.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 50 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `userid` | `bigint` |  | 创作者用户id |
| 3 | `blogid` | `bigint` |  | 博客id |
| 4 | `publishdate` | `string` |  | 文章发布日期 |
| 5 | `like_cnt` | `bigint` |  | 累积喜欢数 |
| 6 | `reproduce_cnt` | `bigint` |  | 累积转载数 |
| 7 | `recommend_cnt` | `bigint` |  | 累积推荐数 |
| 8 | `collect_cnt` | `bigint` |  | 累积收藏数 |
| 9 | `hot_uv` | `bigint` |  | 累积热度uv |
| 10 | `hot` | `bigint` |  | 累积热度 |
| 11 | `comment_uv` | `bigint` |  | 累积评论uv |
| 12 | `comment_cnt` | `bigint` |  | 累积评论次数 |
| 13 | `comment_uv_1d` | `bigint` |  | 当日评论uv |
| 14 | `comment_cnt_1d` | `bigint` |  | 当日新增评论数 |
| 15 | `long_comment_cnt_std` | `bigint` |  | 长评论数（有效字数大于40） |
| 16 | `long_comment_cnt_1d` | `bigint` |  | 近1天长评数量（＞40字，剔除表情） |
| 17 | `long_comment_cnt_7d` | `bigint` |  | 近7天长评数量（＞40字，剔除表情） |
| 18 | `long_comment_cnt_15d` | `bigint` |  | 近15天长评数量（＞40字，剔除表情） |
| 19 | `long_comment_cnt_30d` | `bigint` |  | 近30天长评数量（＞40字，剔除表情） |
| 20 | `hot_7d` | `bigint` |  | 近7日热度 |
| 21 | `hot_15d` | `bigint` |  | 近15日热度 |
| 22 | `hot_30d` | `bigint` |  | 近30日热度 |
| 23 | `hot_90d` | `bigint` |  | 近90日热度 |
| 24 | `hot_365d` | `bigint` |  | 近365天热度 |
| 25 | `comment_cnt_7d` | `bigint` |  | 近7日评论量 |
| 26 | `comment_cnt_15d` | `bigint` |  | 近15日评论量 |
| 27 | `comment_cnt_30d` | `bigint` |  | 近30日评论量 |
| 28 | `valid_comment_cnt` | `bigint` |  | 累计评论量 |
| 29 | `valid_comment_cnt_7d` | `bigint` |  | 近7日评论量 |
| 30 | `valid_comment_cnt_15d` | `bigint` |  | 近15日评论量 |
| 31 | `valid_comment_cnt_30d` | `bigint` |  | 近30日评论量 |
| 32 | `like_cnt_1d` | `bigint` |  | 近1日喜欢数 |
| 33 | `like_cnt_7d` | `bigint` |  | 近7日喜欢数 |
| 34 | `like_cnt_15d` | `bigint` |  | 近15日喜欢数 |
| 35 | `like_cnt_30d` | `bigint` |  | 近30日喜欢数 |
| 36 | `recommend_cnt_1d` | `bigint` |  | 近1日蓝手数 |
| 37 | `recommend_cnt_7d` | `bigint` |  | 近7日蓝手数 |
| 38 | `recommend_cnt_15d` | `bigint` |  | 近15日蓝手数 |
| 39 | `recommend_cnt_30d` | `bigint` |  | 近30日蓝手数 |
| 40 | `collect_cnt_1d` | `bigint` |  | 近1日收藏数 |
| 41 | `collect_cnt_7d` | `bigint` |  | 近7日收藏数 |
| 42 | `collect_cnt_15d` | `bigint` |  | 近15日收藏数 |
| 43 | `collect_cnt_30d` | `bigint` |  | 近30日收藏数 |
| 44 | `share_cnt_1d` | `bigint` |  | 近1日分享数 |
| 45 | `share_cnt_7d` | `bigint` |  | 近7日分享数 |
| 46 | `share_cnt_15d` | `bigint` |  | 近15日分享数 |
| 47 | `share_cnt_30d` | `bigint` |  | 近30日分享数 |
| 48 | `valid_comment_cnt_1d` | `bigint` |  | 近1日有效评论 |
| 49 | `recommend_user_bitmap` | `varbinary(2147483647)` |  | 推荐用户位图 |
| 50 | `dt` | `string` |  |  |

---

## dws_post_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 571.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 571.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `post_userid` | `bigint` |  | 文章创作者用户id |
| 3 | `post_publish_date` | `string` |  | 文章发布日期 |
| 4 | `post_content_type` | `string` |  | 文章内容类型 |
| 5 | `post_tags` | `array<string>` |  | 文章标签 |
| 6 | `hot_pv` | `bigint` |  | 日总热度 |
| 7 | `praise_pv` | `bigint` |  | 喜欢数 |
| 8 | `reproduce_pv` | `bigint` |  | 转载数 |
| 9 | `recommend_pv` | `bigint` |  | 蓝手数 |
| 10 | `subscribe_pv` | `bigint` |  | 收藏数 |
| 11 | `hot_device_bitmap` | `varbinary(2147483647)` |  | 热度操作设备位图 |
| 12 | `praise_device_bitmap` | `varbinary(2147483647)` |  | 喜欢设备位图 |
| 13 | `reproduce_device_bitmap` | `varbinary(2147483647)` |  | 转载设备位图 |
| 14 | `recommend_device_bitmap` | `varbinary(2147483647)` |  | 蓝手设备位图 |
| 15 | `subscribe_device_bitmap` | `varbinary(2147483647)` |  | 收藏设备位图 |
| 16 | `response_pv` | `bigint` |  | 评论数 |
| 17 | `response_device_bitmap` | `varbinary(2147483647)` |  | 评论设备位图 |
| 18 | `share_pv` | `bigint` |  | 分享次数 |
| 19 | `share_device_bitmap` | `varbinary(2147483647)` |  | 分享设备位图 |
| 20 | `valid_response_pv` | `bigint` |  | 有效评论次数 |
| 21 | `fans_response_pv` | `bigint` |  | 粉丝评论数 |
| 22 | `fans_response_user_bitmap` | `varbinary(2147483647)` |  | 粉丝评论用户位图 |
| 23 | `fans_hot_pv` | `bigint` |  | 粉丝热度操作次数 |
| 24 | `fans_hot_user_bitmap` | `varbinary(2147483647)` |  | 粉丝热度操作用户位图 |
| 25 | `hd_pv` | `bigint` |  | 互动次数 |
| 26 | `hd_device_bitmap` | `varbinary(2147483647)` |  | 互动设备id位图 |
| 27 | `dt` | `string` |  |  |

---

## dws_post_misc_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_misc_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `quality_comment_std` | `bigint` |  | 累计优质长评数 |
| 3 | `quality_comment_30d` | `bigint` |  | 近30天优质长评数 |
| 4 | `dt` | `string` |  |  |

---

## dws_post_pay_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_pay_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5.0G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 创作者 |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `non_public_unlock_money` | `double` |  | 私域解锁金额 |
| 4 | `non_public_unlock_pv` | `bigint` |  | 私域解锁次数 |
| 5 | `non_public_unlock_uv` | `varbinary(2147483647)` |  | 私域解锁用户数 |
| 6 | `non_public_unlock_user_bitmap` | `varbinary(2147483647)` |  | 私域解锁用户位图 |
| 7 | `public_unlock_money` | `double` |  | 公域解锁金额 |
| 8 | `public_unlock_pv` | `bigint` |  | 公域解锁次数 |
| 9 | `public_unlock_uv` | `bigint` |  | 公域解锁人数 |
| 10 | `public_unlock_user_bitmap` | `varbinary(2147483647)` |  | 公域解锁用户位图 |
| 11 | `fans_unlock_money` | `double` |  | 粉丝解锁金额 |
| 12 | `fans_unlock_pv` | `bigint` |  | 粉丝解锁次数 |
| 13 | `dt` | `string` |  |  |

---

## dws_post_premium_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_premium_di` |
| **描述** | 优质文章 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 14.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 14.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `publish_date` | `string` |  | 发布日期 |
| 4 | `real_pv` | `bigint` |  | 发布30天内有效pv |
| 5 | `exposure_pv` | `bigint` |  | 发布30天内曝光量 |
| 6 | `share_pv` | `bigint` |  | 发布30天内分享量 |
| 7 | `rec_pv` | `bigint` |  | 发布30天内推荐量 |
| 8 | `support_exposure_pv` | `bigint` |  | 发布30天内流量扶持 |
| 9 | `valid_response` | `bigint` |  | 发布30天内有效评论 |
| 10 | `click_pv` | `bigint` |  | 发文30日内点击pv |
| 11 | `hot_pv` | `bigint` |  | 发文30日内热度 |
| 12 | `response_pv` | `bigint` |  | 发文30日内评论数 |
| 13 | `premium_date` | `string` |  | 成为优质内容日期 |
| 14 | `content_type` | `string` |  | 文章类型 |
| 15 | `title` | `string` |  | 文章标题 |
| 16 | `tags` | `array<string>` |  | 文章标签 |
| 17 | `post_url` | `string` |  | 文章链接 |
| 18 | `blog_url` | `string` |  | 博客主页链接 |
| 19 | `post_valid` | `int` |  | 文章有效情况(0:正常,15:定时发布,16:自动发布,25:被封禁,26:不同步) |
| 20 | `creator_level` | `string` |  | 用户创作者等级 dws_par_creator_dd.level |
| 21 | `realpv_1d` | `bigint` |  | 近1日有效pv |
| 22 | `dt` | `string` |  |  |

---

## dws_post_risk_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_risk_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.7G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `is_recommend` | `int` |  | 是否推荐 |
| 3 | `is_recommend_machine_audit` | `int` |  | 是否推荐机器过滤 |
| 4 | `recommend_audit_duration` | `bigint` |  | 推荐审核时长 |
| 5 | `safety_audit_duration` | `bigint` |  | 安全审核时长 |
| 6 | `dt` | `string` |  |  |

---

## dws_post_support_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_support_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.9G |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `support_exposure_pv` | `bigint` |  | 扶持流量pv |
| 3 | `support_induced_pv` | `bigint` |  | 扶持引导有效pv |
| 4 | `dt` | `string` |  |  |

---

## dws_post_talk_answer_interaction_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_talk_answer_interaction_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 468.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 468.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `talkid` | `bigint` |  | 讨论id |
| 2 | `talktype` | `string` |  | 动态类型：ANSWER,COS_ANSWER,NORMAL_COMMENT_ANSWER,LINE_COMMENT_ANSWER,PHOTO_COMMENT_ANSWER,JOIN_NOMINATESIGN_ANSWER,JOIN_SCORE_ANSWER等 |
| 3 | `blogid` | `bigint` |  | 日志所属博客Id |
| 4 | `questionid` | `bigint` |  | 话题Id |
| 5 | `questiontype` | `int` |  | 话题类型: null-非话题,0-1对1提问,1-1对N投稿,2-运营创建,3-聊聊 |
| 6 | `questionstatus` | `int` |  | 同question的status |
| 7 | `recomstatus` | `int` |  | 推荐状态:0初始,1推荐,-1不推荐 |
| 8 | `forbidstatus` | `int` |  | 屏蔽状态:0未屏蔽,1被屏蔽,2申请解屏中(仅话题有) |
| 9 | `valid` | `int` |  | 同post中的valid |
| 10 | `allowview` | `int` |  | 同post中的allowview |
| 11 | `createtime` | `bigint` |  | 短内容(question,answer)创建时间 |
| 12 | `createdate` | `string` |  | 短内容(question,answer)创建日期 |
| 13 | `tags` | `array<string>` |  | 标签 |
| 14 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 15 | `answertype` | `int` |  | 回答类型: 0-普通,1-角色说,2-普通评论,3-划线评,4-圈评,5-提名子话题回复,6-打分父话题回复,7-印象词动态等 |
| 16 | `like_cnt_1d` | `bigint` |  | 近1d点赞数 |
| 17 | `like_cnt_2d` | `bigint` |  | 近2d点赞数 |
| 18 | `like_cnt_3d` | `bigint` |  | 近3d点赞数 |
| 19 | `like_cnt_7d` | `bigint` |  | 近7d点赞数 |
| 20 | `like_cnt_30d` | `bigint` |  | 近30d点赞数 |
| 21 | `like_cnt_90d` | `bigint` |  | 近90d点赞数 |
| 22 | `comment_like_cnt_1d` | `bigint` |  | 近1d评论点赞数 |
| 23 | `comment_like_cnt_2d` | `bigint` |  | 近2d评论点赞数 |
| 24 | `comment_like_cnt_3d` | `bigint` |  | 近3d评论点赞数 |
| 25 | `comment_like_cnt_7d` | `bigint` |  | 近7d评论点赞数 |
| 26 | `comment_like_cnt_30d` | `bigint` |  | 近30d评论点赞数 |
| 27 | `comment_like_cnt_90d` | `bigint` |  | 近90d评论点赞数 |
| 28 | `dt` | `string` |  |  |

---

## dws_post_talk_question_interaction_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_talk_question_interaction_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1039.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1039.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `talkid` | `bigint` |  | 讨论id |
| 2 | `talktype` | `string` |  | 动态类型：QUESTION,COS_QUESTION,SCORE_QUESTION,NOMINATESIGN_QUESTION,ANSWER,COS_ANSWER,NORMAL_COMMENT_ANSWER,LINE_COMMENT_ANSWER,PHOTO_COMMENT_ANSWER,JOIN_NOMINATESIGN_ANSWER,JOIN_SCORE_ANSWER |
| 3 | `talkcontent` | `string` |  | 讨论内容 |
| 4 | `blogid` | `bigint` |  | 日志所属博客Id |
| 5 | `cosplay` | `int` |  | 0-普通，1-角色说，2-打分父话题，3-打分子话题，4-印象词父话题，5-印象词子话题 |
| 6 | `tags` | `array<string>` |  | 标签 |
| 7 | `ips` | `array<string>` |  | 根据tag映射到ip 见lofter.dwd_tag_ip_mapping_nd |
| 8 | `createtime` | `bigint` |  | 创建时间 |
| 9 | `createdate` | `string` |  | 创建日期时间 |
| 10 | `recomstatus` | `int` |  | 推荐状态:0初始,1推荐,-1不推荐 |
| 11 | `totalanswercount` | `bigint` |  | 回答数 |
| 12 | `totaldiscusscount` | `bigint` |  | 讨论数(后台) |
| 13 | `totalscorecount` | `bigint` |  | 打分人数 |
| 14 | `1ddiscusscount` | `bigint` |  | 1d讨论数 |
| 15 | `3ddiscusscount` | `bigint` |  | 3d讨论数 |
| 16 | `7ddiscusscount` | `bigint` |  | 7d讨论数 |
| 17 | `30ddiscusscount` | `bigint` |  | 30d讨论数 |
| 18 | `90ddiscusscount` | `bigint` |  | 90d讨论数 |
| 19 | `1dscorecount` | `bigint` |  | 1d打分人数 |
| 20 | `3dscorecount` | `bigint` |  | 3d打分人数 |
| 21 | `7dscorecount` | `bigint` |  | 7d打分人数 |
| 22 | `30dscorecount` | `bigint` |  | 30d打分人数 |
| 23 | `90dscorecount` | `bigint` |  | 90d打分人数 |
| 24 | `2ddiscusscount` | `bigint` |  | 2d讨论数 |
| 25 | `2dscorecount` | `bigint` |  | 2d打分人数 |
| 26 | `questiontype` | `bigint` |  | 类型，0:1对1提问；1:1对N投稿；2:运营创建；3:聊聊； |
| 27 | `status` | `int` |  | 话题状态 |
| 28 | `is_fans_question` | `int` |  | 0 否，1 是 |
| 29 | `dt` | `string` |  |  |

---

## dws_post_talk_user_crowd_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_talk_user_crowd_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 161.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 161.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `tag` | `string` |  | 标签名称 |
| 3 | `score` | `bigint` |  | 用户得分 |
| 4 | `usertype` | `string` |  |  用户分群类型 |
| 5 | `dt` | `string` |  |  |

---

## dws_post_traffic_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_traffic_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 3504.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3504.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 153 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `expose_pv_1d` | `bigint` |  | 近1天曝光量 |
| 3 | `expose_pv_7d` | `bigint` |  | 近7天曝光量 |
| 4 | `expose_pv_15d` | `bigint` |  | 近15天曝光量 |
| 5 | `expose_pv_30d` | `bigint` |  | 近30天曝光量 |
| 6 | `expose_uv_1d` | `bigint` |  | 近1天曝光uv |
| 7 | `expose_uv_7d` | `bigint` |  | 近7天曝光uv |
| 8 | `expose_uv_15d` | `bigint` |  | 近15天曝光uv |
| 9 | `expose_uv_30d` | `bigint` |  | 近30天曝光uv |
| 10 | `browse_uv_1d` | `bigint` |  | 近1天有效浏览uv |
| 11 | `browse_uv_7d` | `bigint` |  | 近7天有效浏览uv |
| 12 | `browse_uv_15d` | `bigint` |  | 近15天有效浏览uv |
| 13 | `browse_uv_30d` | `bigint` |  | 近30天有效浏览uv |
| 14 | `browse_pv_1d` | `bigint` |  | 近1日有效pv |
| 15 | `browse_pv_7d` | `bigint` |  | 近7日有效pv |
| 16 | `browse_pv_15d` | `bigint` |  | 近15日有效pv |
| 17 | `browse_pv_30d` | `bigint` |  | 近30日有效pv |
| 18 | `discovery_click_pv_7d` | `bigint` |  | 近7天发现页点击量 |
| 19 | `discovery_click_pv_30d` | `bigint` |  | 近30天发现页点击量 |
| 20 | `discovery_click_pv_90d` | `bigint` |  | 近90天发现页点击量 |
| 21 | `related_article_click_pv_7d` | `bigint` |  | 近7天相关文章点击量 |
| 22 | `related_article_click_pv_30d` | `bigint` |  | 近30天相关文章点击量 |
| 23 | `related_article_click_pv_90d` | `bigint` |  | 近90天相关文章点击量 |
| 24 | `tag_discovery_click_pv_7d` | `bigint` |  | 近7天tag发现页点击量 |
| 25 | `tag_discovery_click_pv_30d` | `bigint` |  | 近30天tag发现页点击量 |
| 26 | `tag_discovery_click_pv_90d` | `bigint` |  | 近90天tag发现页点击量 |
| 27 | `tag_new_click_pv_7d` | `bigint` |  | 近7天tag最新页点击量 |
| 28 | `tag_new_click_pv_30d` | `bigint` |  | 近30天tag最新页点击量 |
| 29 | `tag_new_click_pv_90d` | `bigint` |  | 近90天tag最新页点击量 |
| 30 | `tag_hot_click_pv_7d` | `bigint` |  | 近7天tag最热页点击量 |
| 31 | `tag_hot_click_pv_30d` | `bigint` |  | 近30天tag最热页点击量 |
| 32 | `tag_hot_click_pv_90d` | `bigint` |  | 近90天tag最热页点击量 |
| 33 | `collection_click_pv_7d` | `bigint` |  | 近7天合集点击量 |
| 34 | `collection_click_pv_30d` | `bigint` |  | 近30天合集点击量 |
| 35 | `collection_click_pv_90d` | `bigint` |  | 近90天合集点击量 |
| 36 | `attention_click_pv_7d` | `bigint` |  | 近7天attention点击量 |
| 37 | `attention_click_pv_30d` | `bigint` |  | 近30天attention点击量 |
| 38 | `attention_click_pv_90d` | `bigint` |  | 近90天attention点击量 |
| 39 | `click_pv_7d` | `bigint` |  | 近7天点击量 |
| 40 | `click_pv_30d` | `bigint` |  | 近30天点击量 |
| 41 | `click_pv_90d` | `bigint` |  | 近90天点击量 |
| 42 | `expose_uv_90d` | `bigint` |  | 近90d曝光uv |
| 43 | `browse_uv_90d` | `bigint` |  | 近90d浏览uv |
| 44 | `click_uv_7d` | `bigint` |  | 近7d点击uv |
| 45 | `click_uv_30d` | `bigint` |  | 近30d点击uv |
| 46 | `click_uv_90d` | `bigint` |  | 近90d点击uv |
| 47 | `real_browse_duration_1d` | `bigint` |  | 近1d浏览时长 |
| 48 | `real_browse_duration_7d` | `bigint` |  | 近7d浏览时长 |
| 49 | `real_browse_duration_15d` | `bigint` |  | 近15d浏览时长 |
| 50 | `real_browse_duration_30d` | `bigint` |  | 近30d浏览时长 |
| 51 | `real_browse_duration_90d` | `bigint` |  | 近90d浏览时长 |
| 52 | `dislike_pv_1d` | `bigint` |  | 近1d不喜欢pv |
| 53 | `dislike_pv_7d` | `bigint` |  | 近7d不喜欢pv |
| 54 | `dislike_pv_15d` | `bigint` |  | 近15d不喜欢pv |
| 55 | `dislike_pv_30d` | `bigint` |  | 近30d不喜欢pv |
| 56 | `dislike_pv_90d` | `bigint` |  | 近90d不喜欢pv |
| 57 | `dislike_uv_1d` | `bigint` |  | 近1d不喜欢uv |
| 58 | `dislike_uv_7d` | `bigint` |  | 近7d不喜欢uv |
| 59 | `dislike_uv_15d` | `bigint` |  | 近15d不喜欢uv |
| 60 | `dislike_uv_30d` | `bigint` |  | 近30d不喜欢uv |
| 61 | `dislike_uv_90d` | `bigint` |  | 近90d不喜欢uv |
| 62 | `click_pv_1d` | `bigint` |  | 近1天点击量 |
| 63 | `click_uv_1d` | `bigint` |  | 近1天点击uv |
| 64 | `expose_pv_90d` | `bigint` |  | 近90天曝光量 |
| 65 | `browse_pv_90d` | `bigint` |  | 近90日有效pv |
| 66 | `discovery_click_pv_1d` | `bigint` |  | 近1天发现页点击量 |
| 67 | `related_article_click_pv_1d` | `bigint` |  | 近1天相关文章点击量 |
| 68 | `tag_discovery_click_pv_1d` | `bigint` |  | 近1天tag发现页点击量 |
| 69 | `tag_new_click_pv_1d` | `bigint` |  | 近1天tag最新页点击量 |
| 70 | `tag_hot_click_pv_1d` | `bigint` |  | 近1天tag最热页点击量 |
| 71 | `collection_click_pv_1d` | `bigint` |  | 近1天合集点击量 |
| 72 | `attention_click_pv_1d` | `bigint` |  | 近1天attention点击量 |
| 73 | `discovery_expose_pv_1d` | `bigint` |  | 近1天发现页曝光量 |
| 74 | `discovery_expose_pv_7d` | `bigint` |  | 近7天发现页曝光量 |
| 75 | `discovery_expose_pv_30d` | `bigint` |  | 近30天发现页曝光量 |
| 76 | `discovery_expose_pv_90d` | `bigint` |  | 近90天发现页曝光量 |
| 77 | `related_article_expose_pv_1d` | `bigint` |  | 近1天相关文章曝光量 |
| 78 | `related_article_expose_pv_7d` | `bigint` |  | 近7天相关文章曝光量 |
| 79 | `related_article_expose_pv_30d` | `bigint` |  | 近30天相关文章曝光量 |
| 80 | `related_article_expose_pv_90d` | `bigint` |  | 近90天相关文章曝光量 |
| 81 | `tag_discovery_expose_pv_1d` | `bigint` |  | 近1天tag发现页曝光量 |
| 82 | `tag_discovery_expose_pv_7d` | `bigint` |  | 近7天tag发现页曝光量 |
| 83 | `tag_discovery_expose_pv_30d` | `bigint` |  | 近30天tag发现页曝光量 |
| 84 | `tag_discovery_expose_pv_90d` | `bigint` |  | 近90天tag发现页曝光量 |
| 85 | `tag_new_expose_pv_1d` | `bigint` |  | 近1天tag最新页曝光量 |
| 86 | `tag_new_expose_pv_7d` | `bigint` |  | 近7天tag最新页曝光量 |
| 87 | `tag_new_expose_pv_30d` | `bigint` |  | 近30天tag最新页曝光量 |
| 88 | `tag_new_expose_pv_90d` | `bigint` |  | 近90天tag最新页曝光量 |
| 89 | `tag_hot_expose_pv_1d` | `bigint` |  | 近1天tag最热页曝光量 |
| 90 | `tag_hot_expose_pv_7d` | `bigint` |  | 近7天tag最热页曝光量 |
| 91 | `tag_hot_expose_pv_30d` | `bigint` |  | 近30天tag最热页曝光量 |
| 92 | `tag_hot_expose_pv_90d` | `bigint` |  | 近90天tag最热页曝光量 |
| 93 | `collection_expose_pv_1d` | `bigint` |  | 近1天合集曝光量 |
| 94 | `collection_expose_pv_7d` | `bigint` |  | 近7天合集曝光量 |
| 95 | `collection_expose_pv_30d` | `bigint` |  | 近30天合集曝光量 |
| 96 | `collection_expose_pv_90d` | `bigint` |  | 近90天合集曝光量 |
| 97 | `attention_expose_pv_1d` | `bigint` |  | 近1天attention曝光量 |
| 98 | `attention_expose_pv_7d` | `bigint` |  | 近7天attention曝光量 |
| 99 | `attention_expose_pv_30d` | `bigint` |  | 近30天attention曝光量 |
| 100 | `attention_expose_pv_90d` | `bigint` |  | 近90天attention曝光量 |
| 101 | `discovery_browse_pv_1d` | `bigint` |  | 近1天发现页浏览pv |
| 102 | `discovery_browse_pv_7d` | `bigint` |  | 近7天发现页浏览pv |
| 103 | `discovery_browse_pv_30d` | `bigint` |  | 近30天发现页浏览pv |
| 104 | `discovery_browse_pv_90d` | `bigint` |  | 近90天发现页浏览pv |
| 105 | `related_article_browse_pv_1d` | `bigint` |  | 近1天相关文章浏览pv |
| 106 | `related_article_browse_pv_7d` | `bigint` |  | 近7天相关文章浏览pv |
| 107 | `related_article_browse_pv_30d` | `bigint` |  | 近30天相关文章浏览pv |
| 108 | `related_article_browse_pv_90d` | `bigint` |  | 近90天相关文章浏览pv |
| 109 | `tag_discovery_browse_pv_1d` | `bigint` |  | 近1天tag发现页浏览pv |
| 110 | `tag_discovery_browse_pv_7d` | `bigint` |  | 近7天tag发现页浏览pv |
| 111 | `tag_discovery_browse_pv_30d` | `bigint` |  | 近30天tag发现页浏览pv |
| 112 | `tag_discovery_browse_pv_90d` | `bigint` |  | 近90天tag发现页浏览pv |
| 113 | `tag_new_browse_pv_1d` | `bigint` |  | 近1天tag最新页浏览pv |
| 114 | `tag_new_browse_pv_7d` | `bigint` |  | 近7天tag最新页浏览pv |
| 115 | `tag_new_browse_pv_30d` | `bigint` |  | 近30天tag最新页浏览pv |
| 116 | `tag_new_browse_pv_90d` | `bigint` |  | 近90天tag最新页浏览pv |
| 117 | `tag_hot_browse_pv_1d` | `bigint` |  | 近1天tag最热页浏览pv |
| 118 | `tag_hot_browse_pv_7d` | `bigint` |  | 近7天tag最热页浏览pv |
| 119 | `tag_hot_browse_pv_30d` | `bigint` |  | 近30天tag最热页浏览pv |
| 120 | `tag_hot_browse_pv_90d` | `bigint` |  | 近90天tag最热页浏览pv |
| 121 | `collection_browse_pv_1d` | `bigint` |  | 近1天合集浏览pv |
| 122 | `collection_browse_pv_7d` | `bigint` |  | 近7天合集浏览pv |
| 123 | `collection_browse_pv_30d` | `bigint` |  | 近30天合集浏览pv |
| 124 | `collection_browse_pv_90d` | `bigint` |  | 近90天合集浏览pv |
| 125 | `attention_browse_pv_1d` | `bigint` |  | 近1天attention浏览pv |
| 126 | `attention_browse_pv_7d` | `bigint` |  | 近7天attention浏览pv |
| 127 | `attention_browse_pv_30d` | `bigint` |  | 近30天attention浏览pv |
| 128 | `attention_browse_pv_90d` | `bigint` |  | 近90天attention浏览pv |
| 129 | `search_click_pv_1d` | `bigint` |  | 近1天搜索点击量 |
| 130 | `search_click_pv_7d` | `bigint` |  | 近7天搜索点击量 |
| 131 | `search_click_pv_30d` | `bigint` |  | 近30天搜索点击量 |
| 132 | `search_click_pv_90d` | `bigint` |  | 近90天搜索点击量 |
| 133 | `videolist_click_pv_1d` | `bigint` |  | 近1天视频列表点击量 |
| 134 | `videolist_click_pv_7d` | `bigint` |  | 近7天视频列表点击量 |
| 135 | `videolist_click_pv_30d` | `bigint` |  | 近30天视频列表点击量 |
| 136 | `videolist_click_pv_90d` | `bigint` |  | 近90天视频列表点击量 |
| 137 | `search_expose_pv_1d` | `bigint` |  | 近1天搜索曝光量 |
| 138 | `search_expose_pv_7d` | `bigint` |  | 近7天搜索曝光量 |
| 139 | `search_expose_pv_30d` | `bigint` |  | 近30天搜索曝光量 |
| 140 | `search_expose_pv_90d` | `bigint` |  | 近90天搜索曝光量 |
| 141 | `videolist_expose_pv_1d` | `bigint` |  | 近1天视频列表曝光量 |
| 142 | `videolist_expose_pv_7d` | `bigint` |  | 近7天视频列表曝光量 |
| 143 | `videolist_expose_pv_30d` | `bigint` |  | 近30天视频列表曝光量 |
| 144 | `videolist_expose_pv_90d` | `bigint` |  | 近90天视频列表曝光量 |
| 145 | `search_browse_pv_1d` | `bigint` |  | 近1天搜索浏览PV |
| 146 | `search_browse_pv_7d` | `bigint` |  | 近7天搜索浏览PV |
| 147 | `search_browse_pv_30d` | `bigint` |  | 近30天搜索浏览PV |
| 148 | `search_browse_pv_90d` | `bigint` |  | 近90天搜索浏览PV |
| 149 | `videolist_browse_pv_1d` | `bigint` |  | 近1天视频列表浏览PV |
| 150 | `videolist_browse_pv_7d` | `bigint` |  | 近7天视频列表浏览PV |
| 151 | `videolist_browse_pv_30d` | `bigint` |  | 近30天视频列表浏览PV |
| 152 | `videolist_browse_pv_90d` | `bigint` |  | 近90天视频列表浏览PV |
| 153 | `dt` | `string` |  |  |

---

## dws_post_traffic_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_traffic_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 7454.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 7454.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 78 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章ID |
| 2 | `post_userid` | `bigint` |  | 文章创作者用户id |
| 3 | `post_publish_date` | `string` |  | 文章发布日期 |
| 4 | `post_content_type` | `string` |  | 文章内容类型 |
| 5 | `post_tags` | `array<string>` |  | 文章标签 |
| 6 | `expose_pv` | `bigint` |  | 曝光pv |
| 7 | `expose_uv` | `bigint` |  | 曝光uv |
| 8 | `click_pv` | `bigint` |  | 点击pv |
| 9 | `click_uv` | `bigint` |  | 点击uv |
| 10 | `browse_pv` | `bigint` |  | 浏览pv |
| 11 | `browse_uv` | `bigint` |  | 浏览uv |
| 12 | `browse_duration` | `bigint` |  | 浏览时长 |
| 13 | `real_browse_pv` | `bigint` |  | 有效浏览pv |
| 14 | `real_browse_uv` | `bigint` |  | 有效浏览uv |
| 15 | `real_browse_duration` | `bigint` |  | 有效浏览时长 |
| 16 | `fans_expose_pv` | `bigint` |  | 粉丝曝光pv |
| 17 | `fans_expose_uv` | `bigint` |  | 粉丝曝光uv |
| 18 | `fans_click_pv` | `bigint` |  | 粉丝点击pv |
| 19 | `fans_click_uv` | `bigint` |  | 粉丝点击uv |
| 20 | `fans_real_browse_pv` | `bigint` |  | 粉丝有效浏览pv |
| 21 | `fans_real_browse_uv` | `bigint` |  | 粉丝有效浏览uv |
| 22 | `centralized_expose_pv` | `bigint` |  | 中心化流量曝光流量pv 20230614 添加 下同 |
| 23 | `centralized_expose_uv` | `bigint` |  | 中心化流量曝光流量uv |
| 24 | `non_centralized_expose_pv` | `bigint` |  | 非中心化流量曝光流量pv |
| 25 | `non_centralized_expose_uv` | `bigint` |  | 非中心化流量曝光流量uv |
| 26 | `video_finish_pv` | `bigint` |  | 视频完播pv |
| 27 | `video_finish_uv` | `bigint` |  | 视频完播uv |
| 28 | `new_user_expose_pv` | `bigint` |  | 新用户曝光pv |
| 29 | `new_user_expose_uv` | `bigint` |  | 新用户曝光uv |
| 30 | `new_user_browse_pv` | `bigint` |  | 新用户浏览pv |
| 31 | `new_user_browse_uv` | `bigint` |  | 新用户浏览uv |
| 32 | `new_user_real_browse_pv` | `bigint` |  | 新用户有效浏览pv |
| 33 | `new_user_real_browse_uv` | `bigint` |  | 新用户有效浏览uv |
| 34 | `expose_device_bitmap` | `varbinary(2147483647)` |  | 曝光设备id位图 |
| 35 | `real_browse_device_bitmap` | `varbinary(2147483647)` |  | 有效浏览设备id位图 |
| 36 | `dislike_pv` | `bigint` |  | 曝光文章不兴趣次数 |
| 37 | `dislike_uv` | `bigint` |  | 曝光文章不感兴趣人数 |
| 38 | `discovery_click_pv` | `bigint` |  | 发现页点击量 |
| 39 | `related_article_click_pv` | `bigint` |  | 相关文章点击量 |
| 40 | `tag_discovery_click_pv` | `bigint` |  | tag发现页点击量 |
| 41 | `tag_new_click_pv` | `bigint` |  | tag最新页点击量 |
| 42 | `tag_hot_click_pv` | `bigint` |  | tag最热页点击量 |
| 43 | `collection_click_pv` | `bigint` |  | 合集点击量 |
| 44 | `attention_click_pv` | `bigint` |  | attention点击量 |
| 45 | `public_expose_pv` | `bigint` |  | 公域曝光次数 |
| 46 | `public_expose_uv` | `bigint` |  | 公域曝光人数 |
| 47 | `non_public_expose_pv` | `bigint` |  | 私域曝光次数 |
| 48 | `non_public_expose_uv` | `bigint` |  | 私域曝光次数 |
| 49 | `non_public_browse_pv` | `bigint` |  | 私域浏览量 |
| 50 | `non_public_browse_uv` | `bigint` |  | 私域浏览uv |
| 51 | `non_public_browse_device_bitmap` | `varbinary(2147483647)` |  | 私域浏览设备位图 |
| 52 | `public_browse_pv` | `bigint` |  | 公域浏览pv |
| 53 | `public_browse_uv` | `bigint` |  | 公域浏览uv |
| 54 | `public_browse_device_bitmap` | `varbinary(2147483647)` |  | 公域浏览设备位图 |
| 55 | `click_device_bitmap` | `varbinary(2147483647)` |  | 点击设备位图 |
| 56 | `support_induced_pv` | `bigint` |  | 扶持流量pv |
| 57 | `dislike_uv_bitmap` | `varbinary(2147483647)` |  | dislike设备位图 |
| 58 | `discovery_expose_pv` | `bigint` |  | 发现页曝光pv |
| 59 | `related_article_expose_pv` | `bigint` |  | 相关文章曝光pv |
| 60 | `tag_discovery_expose_pv` | `bigint` |  | tag发现页曝光pv |
| 61 | `tag_new_expose_pv` | `bigint` |  | tag最新页曝光pv |
| 62 | `tag_hot_expose_pv` | `bigint` |  | tag最热页曝光pv |
| 63 | `collection_expose_pv` | `bigint` |  | 合集曝光pv |
| 64 | `attention_expose_pv` | `bigint` |  | attention曝光pv |
| 65 | `discovery_real_browse_pv` | `bigint` |  | 发现页有效浏览pv |
| 66 | `related_article_real_browse_pv` | `bigint` |  | 相关文章有效浏览pv |
| 67 | `tag_discovery_real_browse_pv` | `bigint` |  | tag发现页有效浏览pv |
| 68 | `tag_new_real_browse_pv` | `bigint` |  | tag最新页有效浏览pv |
| 69 | `tag_hot_real_browse_pv` | `bigint` |  | tag最热页有效浏览pv |
| 70 | `collection_real_browse_pv` | `bigint` |  | 合集有效浏览pv |
| 71 | `attention_real_browse_pv` | `bigint` |  | attention有效浏览pv |
| 72 | `search_click_pv` | `bigint` |  | 搜索点击pv |
| 73 | `videolist_click_pv` | `bigint` |  | 视频列表点击pv |
| 74 | `search_expose_pv` | `bigint` |  | 搜索曝光pv |
| 75 | `videolist_expose_pv` | `bigint` |  | 视频列表曝光pv |
| 76 | `search_real_browse_pv` | `bigint` |  | 搜索有效浏览pv |
| 77 | `videolist_real_browse_pv` | `bigint` |  | 视频列表有效浏览pv |
| 78 | `dt` | `string` |  |  |

---

## dws_post_valid_publish_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_post_valid_publish_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 285.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 285.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `string` |  |  |
| 2 | `blogid` | `string` |  |  |
| 3 | `is_valid` | `int` |  |  |
| 4 | `valid_rules` | `int` |  |  |
| 5 | `valid_hot` | `int` |  |  |
| 6 | `valid_pv` | `int` |  |  |
| 7 | `dt` | `string` |  |  |

---

## dws_publish_ip_rank_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_publish_ip_rank_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 31.7M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `day` | `string` |  | 统计日期 |
| 2 | `ip` | `string` |  |  |
| 3 | `postid_cnt` | `bigint` |  |  |
| 4 | `rn` | `bigint` |  |  |
| 5 | `dt` | `string` |  |  |

---

## dws_pve_roles_amount_info_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_pve_roles_amount_info_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9.7M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `roleid` | `bigint` |  | 角色ID |
| 2 | `roletype` | `int` |  | 角色类型 |
| 3 | `7d_is_new` | `int` |  | 是否7d内上新: 0: 否, 1: 是 |
| 4 | `7d_trade_convert` | `double` |  | 7d转化率 |
| 5 | `15d_amount` | `double` |  | 15日付费金额 |
| 6 | `30d_amount` | `double` |  | 30日付费金额 |
| 7 | `30d_dialogueuv` | `bigint` |  | 30d聊天uv |
| 8 | `30d_totalchats` | `bigint` |  | 30d聊天轮数 |
| 9 | `7d_amount` | `double` |  | 7天付费金额 |
| 10 | `dt` | `string` |  |  |

---

## dws_pve_user_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_pve_user_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 961.3M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `dt` | `string` |  |  |

---

## dws_pve_user_coststamina_role_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_pve_user_coststamina_role_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.5G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `roleid` | `bigint` |  | 角色id |
| 3 | `rolename` | `string` |  | 角色名称 |
| 4 | `roledef` | `string` |  | 角色定义：ogc，ugc |
| 5 | `total_coststamina` | `double` |  | 消耗的体力值 |
| 6 | `dt` | `string` |  |  |

---

## dws_pve_user_role_amount_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_pve_user_role_amount_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 50.4M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `roleid` | `bigint` |  | 角色id |
| 3 | `rolename` | `string` |  | 角色名称 |
| 4 | `roledef` | `string` |  | 角色定义：ogc，ugc |
| 5 | `total_amount` | `double` |  | 交易总金额 |
| 6 | `dt` | `string` |  |  |

---

## dws_pve_user_role_chats_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_pve_user_role_chats_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `roleid` | `bigint` |  | 角色id |
| 3 | `rolename` | `string` |  | 角色名称 |
| 4 | `roledef` | `string` |  | 角色定义：ogc，ugc |
| 5 | `total_chats` | `bigint` |  | 聊天轮数 |
| 6 | `dt` | `string` |  |  |

---

## dws_recommend_blog_review_status_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_recommend_blog_review_status_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wb.mazhihao02 |
| **表类型** | external |
| **表大小** | 6.7M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客ID |
| 2 | `published_post_cnt` | `int` |  | 发文数量 |
| 3 | `review_success_rate` | `string` |  | 过审率 |
| 4 | `dt` | `string` |  |  |

---

## dws_tag_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_consume_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 20.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 20.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签名称 |
| 2 | `is_cmb_tag` | `int` |  | 是否为标签库中的标签 |
| 3 | `ips` | `string` |  | 所属IP ","分隔 |
| 4 | `categories` | `string` |  | 所属类目 ","分隔 |
| 5 | `sum_realpv` | `bigint` |  | 总有效pv |
| 6 | `newuser_realpv` | `bigint` |  | 近30日新增设备有效pv |
| 7 | `returnuser_realpv` | `bigint` |  | 近30日回流设备有效pv |
| 8 | `1to7_realpv` | `bigint` |  | 月活1-7日持续活跃设备有效pv |
| 9 | `8to14_realpv` | `bigint` |  | 月活8-14日持续活跃设备有效pv |
| 10 | `15to28_realpv` | `bigint` |  | 月活15-28日持续活跃设备有效pv |
| 11 | `29to30_realpv` | `bigint` |  | 月活29-30日持续活跃设备有效pv |
| 12 | `sum_realuv` | `bigint` |  | 总有效uv |
| 13 | `newuser_realuv` | `bigint` |  | 近30日新增设备有效uv |
| 14 | `returnuser_realuv` | `bigint` |  | 近30日回流设备有效uv |
| 15 | `1to7_realuv` | `bigint` |  | 月活1-7日持续活跃设备有效uv |
| 16 | `8to14_realuv` | `bigint` |  | 月活8-14日持续活跃设备有效uv |
| 17 | `15to28_realuv` | `bigint` |  | 月活15-28日持续活跃设备有效uv |
| 18 | `29to30_realuv` | `bigint` |  | 月活29-30日持续活跃设备有效uv |
| 19 | `dt` | `string` |  |  |

---

## dws_tag_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 250.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 250.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `post_1k_hot_count` | `bigint` |  | 千热文章数 |
| 3 | `post_1w_hot_count` | `bigint` |  | 万热文章数 |
| 4 | `post_count_std` | `bigint` |  | 累计发文即参与量 |
| 5 | `post_count_7d` | `bigint` |  | 近7日发文 |
| 6 | `view_pv_std` | `bigint` |  | 累计标签页浏览量 |
| 7 | `dt` | `string` |  |  |

---

## dws_tag_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 14.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 14.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `browse_count` | `bigint` |  | 标签文章有效浏览量 |
| 3 | `publish_count` | `bigint` |  | 标签发文量 |
| 4 | `response_count` | `bigint` |  | 标签文章新增评论量 |
| 5 | `hot` | `bigint` |  | 标签文章新增热度 |
| 6 | `share_count` | `bigint` |  | 标签文章分享量 |
| 7 | `discuss_count` | `bigint` |  | 标签下话题讨论量 |
| 8 | `dt` | `string` |  |  |

---

## dws_tag_fetch_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_fetch_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 30.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 30.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 43 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  |  |
| 2 | `ips` | `array<string>` |  |  |
| 3 | `post_count` | `bigint` |  |  |
| 4 | `post_uv` | `bigint` |  |  |
| 5 | `free_post_count` | `bigint` |  |  |
| 6 | `free_photo_post_count` | `bigint` |  |  |
| 7 | `free_text_post_count` | `bigint` |  |  |
| 8 | `free_video_post_count` | `bigint` |  |  |
| 9 | `photo_post_count` | `bigint` |  |  |
| 10 | `text_post_count` | `bigint` |  |  |
| 11 | `video_post_count` | `bigint` |  |  |
| 12 | `photo_post_uv` | `bigint` |  |  |
| 13 | `text_post_uv` | `bigint` |  |  |
| 14 | `video_post_uv` | `bigint` |  |  |
| 15 | `level_s_post_count` | `bigint` |  |  |
| 16 | `level_a_post_count` | `bigint` |  |  |
| 17 | `level_b_post_count` | `bigint` |  |  |
| 18 | `level_c_post_count` | `bigint` |  |  |
| 19 | `level_d_post_count` | `bigint` |  |  |
| 20 | `level_d_star_post_count` | `bigint` |  |  |
| 21 | `level_none_post_count` | `bigint` |  |  |
| 22 | `level_s_post_uv` | `bigint` |  |  |
| 23 | `level_a_post_uv` | `bigint` |  |  |
| 24 | `level_b_post_uv` | `bigint` |  |  |
| 25 | `level_c_post_uv` | `bigint` |  |  |
| 26 | `level_d_post_uv` | `bigint` |  |  |
| 27 | `level_d_star_post_uv` | `bigint` |  |  |
| 28 | `level_none_post_uv` | `bigint` |  |  |
| 29 | `hot` | `bigint` |  |  |
| 30 | `recommend_count` | `bigint` |  |  |
| 31 | `photo_hot` | `bigint` |  |  |
| 32 | `text_hot` | `bigint` |  |  |
| 33 | `video_hot` | `bigint` |  |  |
| 34 | `photo_recommend_count` | `bigint` |  |  |
| 35 | `text_recommend_count` | `bigint` |  |  |
| 36 | `video_recommend_count` | `bigint` |  |  |
| 37 | `expose_pv` | `bigint` |  |  |
| 38 | `real_browse_pv` | `bigint` |  |  |
| 39 | `premium_post_count` | `bigint` |  |  |
| 40 | `photo_premium_post_count` | `bigint` |  |  |
| 41 | `text_premium_post_count` | `bigint` |  |  |
| 42 | `video_premium_post_count` | `bigint` |  |  |
| 43 | `dt` | `string` |  |  |

---

## dws_tag_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_interaction_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 3.5G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签名称 |
| 2 | `is_cmb_tag` | `int` |  | 是否为标签库中的标签 |
| 3 | `ips` | `string` |  | 所属IP ","分隔 |
| 4 | `categories` | `string` |  | 所属类目 ","分隔 |
| 5 | `sum_valid_interaction_uv` | `bigint` |  | 总有效互动uv |
| 6 | `sum_valid_interaction_pv` | `bigint` |  | 总有效互动pv |
| 7 | `recommend_uv` | `bigint` |  | 点击推荐uv |
| 8 | `recommend_pv` | `bigint` |  | 点击推荐pv |
| 9 | `valid_comment_uv` | `bigint` |  | 有效评论uv |
| 10 | `valid_comment_pv` | `bigint` |  | 有效评论pv |
| 11 | `share_uv` | `bigint` |  | 分享uv |
| 12 | `share_pv` | `bigint` |  | 分享pv |
| 13 | `dt` | `string` |  |  |

---

## dws_tag_supply_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_supply_di` |
| **描述** | 无描述 |
| **Owner** | da_lofter/dev@HADOOP.HZ.NETEASE.COM |
| **表类型** | internal |
| **表大小** | 6.0G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签名称 |
| 2 | `is_cmb_tag` | `int` |  | 是否为标签库中的标签 |
| 3 | `ips` | `string` |  | 所属IP ","分隔 |
| 4 | `categories` | `string` |  | 所属类目 ","分隔 |
| 5 | `post_cnt_1d` | `bigint` |  | 近1日发文数 |
| 6 | `post_cnt_7d` | `bigint` |  | 近7日发文数 |
| 7 | `post_cnt_15d` | `bigint` |  | 近15日发文数 |
| 8 | `post_cnt_30d` | `bigint` |  | 近30日发文数 |
| 9 | `creator_cnt_1d` | `bigint` |  | 近1日发文人数 |
| 10 | `creator_cnt_7d` | `bigint` |  | 近7日发文人数 |
| 11 | `creator_cnt_15d` | `bigint` |  | 近15日发文人数 |
| 12 | `creator_cnt_30d` | `bigint` |  | 近30日发文人数 |
| 13 | `valid_creator_cnt_30d` | `bigint` |  | 月有效创作者数 |
| 14 | `premium_post_cnt` | `bigint` |  | 优质内容数 |
| 15 | `dt` | `string` |  |  |

---

## dws_tag_user_consume_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_user_consume_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 288.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 288.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `post_30d` | `bigint` |  | 30日消费文章数 |
| 4 | `post_15d` | `bigint` |  | 15日消费文章数 |
| 5 | `post_7d` | `bigint` |  | 7日消费文章数 |
| 6 | `dt` | `string` |  |  |

---

## dws_tag_user_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_tag_user_consume_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5134.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5134.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `tag` | `string` |  | 标签 |
| 2 | `userid` | `bigint` |  | 消费用户Id |
| 3 | `pv` | `bigint` |  | 浏览内容次数 |
| 4 | `post_count` | `bigint` |  | 浏览文章数 |
| 5 | `post_bitmap` | `varbinary(2147483647)` |  | 文章id位图 |
| 6 | `is_favorite_tag` | `int` |  | 是否用户关注tag 1是 0否 |
| 7 | `dt` | `string` |  |  |

---

## dws_talk_publish_ip_rank_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_talk_publish_ip_rank_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 16.7M |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  | 动态内容ip |
| 2 | `publish_cnt` | `bigint` |  | 动态发布量 |
| 3 | `rn` | `bigint` |  | 发布量排名 |
| 4 | `dt` | `string` |  |  |

---

## dws_user_first_interaction_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_first_interaction_dd` |
| **描述** | 用户首次的热度+评论行为 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 649.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 649.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `optime` | `bigint` |  | 热度或评论操作时间 |
| 3 | `opdate` | `string` |  | 操作日期 |
| 4 | `optype` | `int` |  | 操作类型：1点赞，2转载，3推荐，4收藏，10评论 |
| 5 | `postid` | `bigint` |  | 文章ID |
| 6 | `blogid` | `bigint` |  | 文章所属博客Id |
| 7 | `dt` | `string` |  |  |

---

## dws_user_life_circle_index_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_life_circle_index_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 1509.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1509.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `create_date` | `string` |  | 用户注册日期 |
| 3 | `last_login_date` | `string` |  | 最近一次登录日期 |
| 4 | `post_cnt_1y` | `bigint` |  | 近一年发文数 |
| 5 | `discuss_cnt_1y` | `bigint` |  | 近一年发布讨论数 |
| 6 | `send_comment_cnt_1y` | `bigint` |  | 近一年评论数 |
| 7 | `trade_num_1y` | `bigint` |  | 近一年付费交易数 |
| 8 | `trade_money_1y` | `double` |  | 近一年付费金额 |
| 9 | `real_browse_pv_15d` | `bigint` |  | 近15天有效pv |
| 10 | `discuss_browse_15d` | `bigint` |  | 近15天讨论浏览pv |
| 11 | `post_cnt_15d` | `bigint` |  | 近15天发文数 |
| 12 | `discuss_cnt_15d` | `bigint` |  | 近15天发布讨论数 |
| 13 | `send_comment_cnt_15d` | `bigint` |  | 近15天评论数 |
| 14 | `trade_num_15d` | `bigint` |  | 近15天付费交易数 |
| 15 | `trade_money_15d` | `double` |  | 近15天付费金额 |
| 16 | `paid_content_real_browse_pv_15d` | `bigint` |  | 近15天付费内容有效pv |
| 17 | `product_browse_pv_15d` | `bigint` |  | 近15天商品详情页浏览pv |
| 18 | `card_browse_pv_15d` | `string` |  | 近15天卡牌详情页浏览pv |
| 19 | `dt` | `string` |  | 统计日期 |

---

## dws_user_life_circle_judge_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_life_circle_judge_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 878.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 878.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `life_cycle_type` | `string` |  | 用户生命周期 |
| 3 | `dt` | `string` |  | 统计日期 |

---

## dws_user_pay_type_info_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_pay_type_info_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 966.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 966.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `pay_type` | `string` |  | 付费类型:抽赏,市集,免费礼物,付费礼物,打赏文章,打赏博客,头像框-包月,头像框-永久,直播-免费礼物,直播-付费礼物,博客订阅 |
| 3 | `first_pay_time` | `bigint` |  | 首次付费时间 |
| 4 | `first_pay_item_id` | `bigint` |  | 首次付费对象ID |
| 5 | `first_pay_item_name` | `string` |  | 首次付费对象ID的名称 |
| 6 | `last_pay_time` | `bigint` |  | 末次付费时间 |
| 7 | `last_pay_item_id` | `bigint` |  | 末次付费对象ID |
| 8 | `last_pay_item_name` | `string` |  | 末次付费对象ID的名称 |
| 9 | `dt` | `string` |  |  |

---

## dws_user_post_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_post_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 34.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 34.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 57 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `text_send_like_cnt` | `bigint` |  |  |
| 3 | `photo_send_like_cnt` | `bigint` |  |  |
| 4 | `video_send_like_cnt` | `bigint` |  |  |
| 5 | `article_send_like_cnt` | `bigint` |  |  |
| 6 | `music_send_like_cnt` | `bigint` |  |  |
| 7 | `text_send_reproduce_cnt` | `bigint` |  |  |
| 8 | `photo_send_reproduce_cnt` | `bigint` |  |  |
| 9 | `video_send_reproduce_cnt` | `bigint` |  |  |
| 10 | `article_send_reproduce_cnt` | `bigint` |  |  |
| 11 | `music_send_reproduce_cnt` | `bigint` |  |  |
| 12 | `text_send_recommend_cnt` | `bigint` |  |  |
| 13 | `photo_send_recommend_cnt` | `bigint` |  |  |
| 14 | `video_send_recommend_cnt` | `bigint` |  |  |
| 15 | `article_send_recommend_cnt` | `bigint` |  |  |
| 16 | `music_send_recommend_cnt` | `bigint` |  |  |
| 17 | `text_send_collect_cnt` | `bigint` |  |  |
| 18 | `photo_send_collect_cnt` | `bigint` |  |  |
| 19 | `video_send_collect_cnt` | `bigint` |  |  |
| 20 | `article_send_collect_cnt` | `bigint` |  |  |
| 21 | `music_send_collect_cnt` | `bigint` |  |  |
| 22 | `text_send_comment_cnt` | `bigint` |  |  |
| 23 | `photo_send_comment_cnt` | `bigint` |  |  |
| 24 | `video_send_comment_cnt` | `bigint` |  |  |
| 25 | `article_send_comment_cnt` | `bigint` |  |  |
| 26 | `music_send_comment_cnt` | `bigint` |  |  |
| 27 | `send_hot` | `bigint` |  |  |
| 28 | `send_like_cnt` | `bigint` |  |  |
| 29 | `send_reproduce_cnt` | `bigint` |  |  |
| 30 | `send_recommend_cnt` | `bigint` |  |  |
| 31 | `send_collect_cnt` | `bigint` |  |  |
| 32 | `send_comment_cnt` | `bigint` |  |  |
| 33 | `send_valid_comment_count` | `bigint` |  | 有效评论数 |
| 34 | `send_novalid_comment_count` | `bigint` |  | 无效评论数 |
| 35 | `like_pv` | `bigint` |  | 排除问答的文章点赞量 |
| 36 | `reproduce_pv` | `bigint` |  | 排除问答的文章转载量 |
| 37 | `recommend_pv` | `bigint` |  | 排除问答的文章推荐量 |
| 38 | `collect_pv` | `bigint` |  | 排除问答的文章收藏量 |
| 39 | `hot_pv` | `bigint` |  | 排除问答的文章热度量 |
| 40 | `valid_comment_pv` | `bigint` |  | 排除问答的文章有效评论量 |
| 41 | `novalid_comment_pv` | `bigint` |  | 排除问答的文章无效评论量 |
| 42 | `comment_pv` | `bigint` |  | 排除问答的文章评论量 |
| 43 | `reply_comment_pv` | `bigint` |  | 排除问答的文章评论回复量 |
| 44 | `comment_like_pv` | `bigint` |  | 排除问答的文章评论点赞量 |
| 45 | `underscore_comment_pv` | `bigint` |  | 划线评数 |
| 46 | `share_pv` | `bigint` |  | 分享数 |
| 47 | `follow_uv` | `bigint` |  | 关注作者数 |
| 48 | `tag_subscribe_pv` | `bigint` |  | 订阅标签数 |
| 49 | `collection_subscribe_pv` | `bigint` |  | 订阅合集数 |
| 50 | `free_gift_present_pv` | `bigint` |  | 赠送免费礼物数 |
| 51 | `underscore_reply_pv` | `bigint` |  | 划线评回复数 |
| 52 | `sweet_marks` | `bigint` |  | 甜标记数 |
| 53 | `bitter_marks` | `bigint` |  | 虐标记数 |
| 54 | `circle_comment_pv` | `bigint` |  | 圈评pv |
| 55 | `circle_comment_reply_pv` | `bigint` |  | 圈评回复pv |
| 56 | `haha_marks` | `bigint` |  | 哈哈标记数 |
| 57 | `dt` | `string` |  |  |

---

## dws_user_post_other_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_post_other_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 344.3M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `ti_pv` | `bigint` |  | 踢互动数 |
| 3 | `luck_boy_pv` | `bigint` |  | 互动抓人数 |
| 4 | `vote_blog_pv` | `bigint` |  | blog投票创建数 |
| 5 | `vote_user_pv` | `bigint` |  | user投票数 |
| 6 | `dt` | `string` |  |  |

---

## dws_user_post_talk_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_post_talk_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 90.5M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `praise_pv` | `bigint` |  | 点赞数 |
| 3 | `comment_pv` | `bigint` |  | 评论数 |
| 4 | `comment_like_pv` | `bigint` |  | 评论点赞数 |
| 5 | `share_pv` | `bigint` |  | 分享数 |
| 6 | `cp_score_pv` | `bigint` |  | 打分数 |
| 7 | `dt` | `string` |  |  |

---

## dws_user_security_level_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_security_level_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 41.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 41.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客Id |
| 2 | `score` | `double` |  | 分数 |
| 3 | `forbiddentype` | `int` |  | 封禁类型 |
| 4 | `grade` | `int` |  | 等级  0 绿， 1黄， 2 红 3 高绿 |
| 5 | `details` | `array<row<string,double>('type','unpass_rate')>` |  | 不通过的详细情况 |
| 6 | `dt` | `string` |  |  |

---

## dws_user_tag_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_tag_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 210.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 210.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 32 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `tag` | `string` |  |  |
| 3 | `text_send_like_cnt` | `bigint` |  |  |
| 4 | `photo_send_like_cnt` | `bigint` |  |  |
| 5 | `video_send_like_cnt` | `bigint` |  |  |
| 6 | `article_send_like_cnt` | `bigint` |  |  |
| 7 | `music_send_like_cnt` | `bigint` |  |  |
| 8 | `text_send_reproduce_cnt` | `bigint` |  |  |
| 9 | `photo_send_reproduce_cnt` | `bigint` |  |  |
| 10 | `video_send_reproduce_cnt` | `bigint` |  |  |
| 11 | `article_send_reproduce_cnt` | `bigint` |  |  |
| 12 | `music_send_reproduce_cnt` | `bigint` |  |  |
| 13 | `text_send_recommend_cnt` | `bigint` |  |  |
| 14 | `photo_send_recommend_cnt` | `bigint` |  |  |
| 15 | `video_send_recommend_cnt` | `bigint` |  |  |
| 16 | `article_send_recommend_cnt` | `bigint` |  |  |
| 17 | `music_send_recommend_cnt` | `bigint` |  |  |
| 18 | `text_send_collect_cnt` | `bigint` |  |  |
| 19 | `photo_send_collect_cnt` | `bigint` |  |  |
| 20 | `video_send_collect_cnt` | `bigint` |  |  |
| 21 | `article_send_collect_cnt` | `bigint` |  |  |
| 22 | `music_send_collect_cnt` | `bigint` |  |  |
| 23 | `send_hot_std` | `bigint` |  |  |
| 24 | `send_like_cnt` | `bigint` |  |  |
| 25 | `send_reproduce_cnt` | `bigint` |  |  |
| 26 | `send_recommend_cnt` | `bigint` |  |  |
| 27 | `send_collect_cnt` | `bigint` |  |  |
| 28 | `like_pv` | `bigint` |  |  |
| 29 | `reproduce_pv` | `bigint` |  |  |
| 30 | `recommend_pv` | `bigint` |  |  |
| 31 | `collect_pv` | `bigint` |  |  |
| 32 | `dt` | `string` |  |  |

---

## dws_user_talkcontent_interaction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_user_talkcontent_interaction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 630.5M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `id` | `string` |  | 动态id |
| 3 | `talktype` | `string` |  | 动态类型 |
| 4 | `praise_pv` | `bigint` |  | 动态点赞次数 |
| 5 | `comment_pv` | `bigint` |  | 评论次数 |
| 6 | `comment_like_pv` | `bigint` |  | 评论点赞次数 |
| 7 | `share_pv` | `bigint` |  | 动态分享次数 |
| 8 | `cp_score_pv` | `bigint` |  | cp评分次数 |
| 9 | `interaction_pv` | `bigint` |  | 互动次数 |
| 10 | `dt` | `string` |  |  |

---

## dws_video_creator_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_video_creator_dd` |
| **描述** | 创作者视频画像 · 仅因子 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 461.0M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 创作者 ID |
| 2 | `userid` | `bigint` |  | 用户 ID |
| 3 | `total_video_count` | `bigint` |  | 历史累计视频数 |
| 4 | `publish_count_1d` | `bigint` |  | 当日发布数 |
| 5 | `expose_pv_1d` | `bigint` |  | 当日曝光 PV |
| 6 | `play_pv_1d` | `bigint` |  | 当日播放 PV |
| 7 | `real_play_pv_1d` | `bigint` |  | 当日有效播放 PV |
| 8 | `finish_play_pv_1d` | `bigint` |  | 当日完播 PV |
| 9 | `hot_pv_1d` | `bigint` |  | 当日互动 PV |
| 10 | `share_pv_1d` | `bigint` |  | 当日分享 PV |
| 11 | `response_pv_1d` | `bigint` |  | 当日评论 PV |
| 12 | `play_session_count_1d` | `bigint` |  | 当日播放会话数 |
| 13 | `buffer_session_count_1d` | `bigint` |  | 当日卡顿会话数 |
| 14 | `cover_edit_post_count_1d` | `bigint` |  | 当日发生封面编辑的视频数 |
| 15 | `cover_save_post_count_1d` | `bigint` |  | 当日发生封面保存的视频数 (二次编辑/最终保存) |
| 16 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_video_post_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_video_post_dd` |
| **描述** | 视频粒度日汇总 · 仅输出因子 (下游求商得率) |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 15.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 15.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 47 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 视频 ID |
| 2 | `userid` | `bigint` |  | 发布者 |
| 3 | `blogid` | `bigint` |  | 创作者 |
| 4 | `publish_date` | `string` |  | 发布日期 |
| 5 | `video_type` | `int` |  | 3=站内原生 / 非3=站外 |
| 6 | `duration_sec` | `bigint` |  | 视频时长(秒) |
| 7 | `size_bytes` | `bigint` |  | 文件大小(字节) |
| 8 | `aspect_ratio` | `string` |  | 宽高比 |
| 9 | `is_imported` | `int` |  | 是否导入 (从抖音/快手等外部平台搬运): 0=否 / 1=是 |
| 10 | `import_platform_type` | `string` |  | 导入平台 |
| 11 | `movefrom` | `string` |  | ios/android/web |
| 12 | `post_tags` | `array<string>` |  | 标签数组 |
| 13 | `post_ips` | `array<string>` |  | IP 数组 |
| 14 | `post_domains` | `array<bigint>` |  | 领域数组 |
| 15 | `expose_pv` | `bigint` |  | 曝光 PV (dwd_post_browse_di action_type=page_view) |
| 16 | `expose_uv` | `bigint` |  | 曝光 UV |
| 17 | `play_pv` | `bigint` |  | 播放 PV (action_type=page_duration) |
| 18 | `play_uv` | `bigint` |  | 播放 UV |
| 19 | `real_play_pv` | `bigint` |  | 有效播放 PV (is_real=1) |
| 20 | `real_play_uv` | `bigint` |  | 有效播放 UV |
| 21 | `finish_play_pv` | `bigint` |  | 完播 PV (is_video_finish=1) |
| 22 | `finish_play_uv` | `bigint` |  | 完播 UV |
| 23 | `real_play_time_ms` | `bigint` |  | 有效播放总时长 (ms) |
| 24 | `play_time_ms` | `bigint` |  | 播放总时长 (ms) |
| 25 | `play_session_count` | `bigint` |  | 播放会话数 (dwd_video_play_di) |
| 26 | `play_session_uv` | `bigint` |  | 播放会话 UV |
| 27 | `play_session_real_count` | `bigint` |  | 5s 有效会话数 |
| 28 | `play_session_finish_count` | `bigint` |  | 完播会话数 |
| 29 | `play_session_started_count` | `bigint` |  | 起播成功会话数 (有首帧渲染事件) |
| 30 | `buffer_session_count` | `bigint` |  | 卡顿会话数总和 |
| 31 | `buffer_time_ms` | `bigint` |  | 卡顿时长总和 (ms) |
| 32 | `session_played_time_ms` | `bigint` |  | 会话级累计播放时长 (来自播放心跳, 单位 ms) |
| 33 | `ttfp_ms_sum` | `bigint` |  | TTFP 时长求和 (ms, 用于求均值) |
| 34 | `ttfp_ms_count` | `bigint` |  | TTFP 有效记录数 |
| 35 | `ttfr_ms_sum` | `bigint` |  | TTFR 时长求和 (ms) |
| 36 | `ttfr_ms_count` | `bigint` |  | TTFR 有效记录数 |
| 37 | `play_fail_count` | `bigint` |  | 播放失败会话数 |
| 38 | `speedrate_session_count` | `bigint` |  | 使用倍速的会话数 |
| 39 | `speedrate_uv` | `bigint` |  | 使用倍速的 UV |
| 40 | `praise_pv` | `bigint` |  | 点赞次数 (dwd_post_hot_di praise) |
| 41 | `reproduce_pv` | `bigint` |  | 转载次数 |
| 42 | `recommend_pv` | `bigint` |  | 推荐次数 |
| 43 | `subscribe_pv` | `bigint` |  | 订阅次数 |
| 44 | `hot_pv` | `bigint` |  | hot 总次数 (praise+reproduce+recommend+subscribe) |
| 45 | `response_pv` | `bigint` |  | 评论次数 (dwd_post_response_di) |
| 46 | `share_pv` | `bigint` |  | 分享次数 (dwd_post_share_di) |
| 47 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_video_post_general_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_video_post_general_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 191.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 191.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 33 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  |  |
| 2 | `viewpv` | `int` |  |  |
| 3 | `viewuv` | `int` |  |  |
| 4 | `playuv` | `int` |  |  |
| 5 | `availableplayuv` | `int` |  | 废弃 |
| 6 | `finishplayuv` | `int` |  |  |
| 7 | `finishplayratio` | `float` |  |  |
| 8 | `availableplayratio` | `float` |  | 废弃 |
| 9 | `realplaypv` | `bigint` |  |  |
| 10 | `realplayuv` | `bigint` |  |  |
| 11 | `playpv` | `bigint` |  |  |
| 12 | `availableplaypv` | `bigint` |  | 废弃 |
| 13 | `finishplaypv` | `bigint` |  |  |
| 14 | `realplaytime` | `bigint` |  |  |
| 15 | `playtime` | `bigint` |  |  |
| 16 | `playprogress` | `float` |  | 废弃 |
| 17 | `praisepv` | `bigint` |  |  |
| 18 | `reproducepv` | `bigint` |  |  |
| 19 | `recommendpv` | `bigint` |  |  |
| 20 | `subscribepv` | `bigint` |  |  |
| 21 | `responsepv` | `bigint` |  |  |
| 22 | `hotpv` | `bigint` |  | 视频当日新增热度 |
| 23 | `videotype` | `bigint` |  | 视频类型 3外链 其他本地视频 |
| 24 | `publishdate` | `string` |  | 发布日期 |
| 25 | `tags` | `array<string>` |  | 文章标签列表 |
| 26 | `blogname` | `string` |  |  |
| 27 | `blogid` | `bigint` |  |  |
| 28 | `blognickname` | `string` |  |  |
| 29 | `originurl` | `string` |  |  |
| 30 | `duration` | `bigint` |  |  |
| 31 | `size_mb` | `double` |  |  |
| 32 | `movefrom` | `string` |  |  |
| 33 | `dt` | `string` |  |  |

---

## dws_video_quality_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_video_quality_dd` |
| **描述** | 视频质量大盘因子 · 按 deviceOs × device_tier × appVersion × dominant_quality 切片 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 4.8M |
| **是否分区表** | 是 |

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceos` | `string` |  | iOS / Android |
| 2 | `device_tier` | `string` |  | high / mid / low / unknown |
| 3 | `appversion` | `string` |  | App 版本 |
| 4 | `dominant_quality` | `string` |  | 主档位 "宽x高" |
| 5 | `play_session_count` | `bigint` |  | 播放会话数 |
| 6 | `play_session_uv` | `bigint` |  | 播放会话 UV |
| 7 | `buffer_session_count` | `bigint` |  | 卡顿会话数 (buffer>0 的会话数) |
| 8 | `buffer_session_uv` | `bigint` |  | 卡顿会话 UV |
| 9 | `buffer_time_ms` | `bigint` |  | 卡顿时长求和 (ms) |
| 10 | `played_time_ms` | `bigint` |  | 播放时长求和 (ms) |
| 11 | `ttfp_ms_sum` | `bigint` |  | TTFP 求和 (用于均值) |
| 12 | `ttfp_ms_count` | `bigint` |  | TTFP 有效记录数 |
| 13 | `ttfp_ms_p50_input_count` | `bigint` |  | 用于分位数计算的样本数 |
| 14 | `ttfr_ms_sum` | `bigint` |  | TTFR 求和 |
| 15 | `ttfr_ms_count` | `bigint` |  | TTFR 有效记录数 |
| 16 | `play_started_count` | `bigint` |  | 起播成功会话数 (有首帧渲染事件) |
| 17 | `play_failed_count` | `bigint` |  | 播放失败会话数 (有错误事件) |
| 18 | `play_heartbeat_count_sum` | `bigint` |  | 播放心跳次数求和 |
| 19 | `play_attempt_count` | `bigint` |  | 起播尝试次数 (用户点击播放) |
| 20 | `quality_switch_count_sum` | `bigint` |  | 清晰度切换次数求和 (不含首次确定) |
| 21 | `speedrate_uv` | `bigint` |  | 使用倍速的 UV (play_rate>1.0) |
| 22 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_video_speedrate_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_video_speedrate_dd` |
| **描述** | 倍速功能专项因子 · 按 deviceOs × speedrate_tier 切片 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 39.9K |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceos` | `string` |  | iOS / Android |
| 2 | `speedrate_tier` | `string` |  | 1.0x / 1.25x / 1.5x / 1.75x / 2.0x / 0.5x / 0.75x ... |
| 3 | `overall_play_uv` | `bigint` |  | 该 deviceOs 大盘有效播放 UV (各 tier 相同, 透传) |
| 4 | `overall_speedrate_uv` | `bigint` |  | 该 deviceOs 大盘倍速 UV (各 tier 相同, 透传) |
| 5 | `tier_session_count` | `bigint` |  | 本 tier 会话数 |
| 6 | `tier_session_uv` | `bigint` |  | 本 tier 会话 UV |
| 7 | `tier_finish_count` | `bigint` |  | 本 tier 完播会话数 |
| 8 | `tier_buffer_session_count` | `bigint` |  | 本 tier 卡顿会话数 |
| 9 | `tier_buffer_time_ms` | `bigint` |  | 本 tier 卡顿时长 (ms) |
| 10 | `tier_played_time_ms` | `bigint` |  | 本 tier 播放时长 (ms) |
| 11 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## dws_video_user_consume_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `dws_video_user_consume_di` |
| **描述** | 用户视频消费日汇总 · 仅因子 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 515.2M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户 ID |
| 2 | `deviceudid` | `string` |  | 设备 ID |
| 3 | `deviceos` | `string` |  | iOS / Android |
| 4 | `appversion` | `string` |  | App 版本 |
| 5 | `expose_pv` | `bigint` |  | 视频曝光 PV |
| 6 | `play_pv` | `bigint` |  | 视频播放 PV |
| 7 | `play_post_uv` | `bigint` |  | 播放过的不同视频数 |
| 8 | `real_play_pv` | `bigint` |  | 有效播放 PV |
| 9 | `finish_play_pv` | `bigint` |  | 完播 PV |
| 10 | `real_play_time_ms` | `bigint` |  | 有效播放总时长 (ms) |
| 11 | `play_session_count` | `bigint` |  | 播放会话数 |
| 12 | `play_post_distinct` | `bigint` |  | 会话粒度不同视频数 |
| 13 | `buffer_session_count` | `bigint` |  | 卡顿会话数 |
| 14 | `buffer_time_ms` | `bigint` |  | 卡顿时长 (ms) |
| 15 | `played_time_ms` | `bigint` |  | 播放时长 (ms) |
| 16 | `is_speedrate_used` | `int` |  | 当日是否用过倍速: 0=否 / 1=是 |
| 17 | `hot_pv` | `bigint` |  | 互动次数 (全内容类型) |
| 18 | `dt` | `string` |  | 分区日期 yyyy-MM-dd |

---

## excellent_new_device_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `excellent_new_device_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 2.3G |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | 设备Id |
| 2 | `device_type` | `string` |  | 增长类型: new 新设备, return_30 30天回流设备 |
| 3 | `origin_type` | `string` |  | 设备来源归因-类型: 按优先级排序为：口令/广告/自然增长 |
| 4 | `origin_channel` | `string` |  | 设备来源归因-渠道: 广告：广告渠道 口令:推广渠道 |
| 5 | `proxy` | `string` |  | 渠道代理 |
| 6 | `valid_pv` | `bigint` |  | 有效pv |
| 7 | `hdpv` | `bigint` |  | 互动次数 |
| 8 | `follow_pv` | `bigint` |  | 关注次数 |
| 9 | `subscribe_collection_pv` | `bigint` |  | 合集订阅次数 |
| 10 | `publish_pv` | `bigint` |  | 发布次数 |
| 11 | `total_chats` | `bigint` |  | 聊天轮数 |
| 12 | `dt` | `string` |  |  |

---

## hubble_events

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `hubble_events` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1342.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1342.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  | lofter内部ID |
| 2 | `deviceudid` | `string` |  | 系统ID |
| 3 | `deviceimei` | `string` |  | imei |
| 4 | `deviceandroidid` | `string` |  | 安卓id |
| 5 | `deviceadid` | `string` |  | idfa |
| 6 | `ip` | `string` |  | ip地址 |
| 7 | `country` | `string` |  | 国家 |
| 8 | `region` | `string` |  | 省份 |
| 9 | `city` | `string` |  | 城市 |
| 10 | `devicemodel` | `string` |  | 手机内部型号 |
| 11 | `deviceos` | `string` |  | 操作系统类型 |
| 12 | `networktype` | `string` |  | 用户所处的网络状态 |
| 13 | `devicecarrier` | `string` |  | 运营商 |
| 14 | `pt_appid` | `string` |  | 平台类型（android、iphone、pc、h5等） |
| 15 | `occurtime` | `bigint` |  | 事件上报时间 |
| 16 | `oaid` | `string` |  | 安卓端oaid |
| 17 | `dt` | `string` |  |  |

---

## lofter_photopost_feature

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `lofter_photopost_feature` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 67.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 67.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `itemid` | `string` |  | 日志permaillink |
| 2 | `vector` | `array<float>` |  | 文章首图向量特征 |
| 3 | `dt` | `string` |  |  |

---

## mda_common_deviceid_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `mda_common_deviceid_nd` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 456.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 456.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceos` | `string` |  |  |
| 2 | `idfa` | `string` |  |  |
| 3 | `imei` | `string` |  |  |
| 4 | `firsttime` | `bigint` |  |  |
| 5 | `dt` | `string` |  |  |

---

## meta_worker_hdfs_meta

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `meta_worker_hdfs_meta` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `path` | `string` |  | 路径 |
| 2 | `totalfilesize` | `bigint` |  | 总文件大小 |
| 3 | `type` | `string` |  | string |
| 4 | `totalinodecount` | `bigint` |  | 文件总数 |
| 5 | `blockscount` | `int` |  | 文件块数量 |
| 6 | `nsquota` | `bigint` |  | 元数据配额 |
| 7 | `dsquota` | `bigint` |  | 存储配额 |
| 8 | `totalblockscount` | `bigint` |  | 所有文件的数据块总数 |
| 9 | `modificationtime` | `string` |  | 修改时间 |
| 10 | `replication` | `int` |  | 副本数量（文件为数，目录为0） |
| 11 | `totaldirinode` | `bigint` |  | 总目录数（递归） |
| 12 | `username` | `string` |  | 用户 |
| 13 | `extrasmallfileinode` | `bigint` |  | 超小文件数量（不递归） |
| 14 | `smallfileinode` | `bigint` |  | 小文件数量（不递归） |
| 15 | `totalextrasmallfileinode` | `bigint` |  | 超小文件数量（递归） |
| 16 | `totalsmallfileinode` | `bigint` |  | 小文件数量（递归） |
| 17 | `filesize` | `bigint` |  | 文件大小（type = ‘FILE’） |
| 18 | `accesstime` | `string` |  | 最近访问时间 |
| 19 | `totalspaceconsumed` | `bigint` |  | 目录物理大小（递归） |
| 20 | `dt` | `string` |  | 日期 |

---

## ods_binlog_anonymity_login_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_anonymity_login_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 228.7M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `userid` | `bigint` |  | 对应userId |
| 3 | `deviceid` | `string` |  | 设备号 |
| 4 | `producttype` | `int` |  | 设备号类型 |
| 5 | `createtime` | `bigint` |  | NULL |
| 6 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 7 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 8 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 9 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 10 | `dt` | `string` |  |  |

---

## ods_binlog_benefit_order_product_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_benefit_order_product_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 67.6M |
| **是否分区表** | 是 |

### 字段详情

共 40 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `orderid` | `bigint` |  | 订单id |
| 3 | `createtime` | `bigint` |  | 创建时间 |
| 4 | `status` | `bigint` |  | 状态 |
| 5 | `buyerid` | `string` |  | 应用中的用户 |
| 6 | `productid` | `bigint` |  | 商品id |
| 7 | `type` | `bigint` |  | 商品类型 |
| 8 | `productname` | `string` |  | 商品名称 |
| 9 | `productdetail` | `string` |  | 商品详情 |
| 10 | `productimages` | `string` |  | 商品图片 |
| 11 | `detailimages` | `string` |  | 详情图片 |
| 12 | `storeprice` | `double` |  | 市集价或福利币价格 |
| 13 | `marketprice` | `double` |  | 市场价 |
| 14 | `deductprice` | `double` |  | 福利币最高抵扣价格 |
| 15 | `pricetype` | `bigint` |  | 价格类型 |
| 16 | `freepost` | `bigint` |  | 是否免邮 |
| 17 | `productcontent` | `string` |  | 商品内容 |
| 18 | `productext` | `string` |  | 商品额外信息 |
| 19 | `productnum` | `bigint` |  | 商品数目 |
| 20 | `attrgroupid` | `bigint` |  | 属性组id |
| 21 | `attrgroupext` | `string` |  | 属性组额外信息 |
| 22 | `coverimage` | `string` |  | 指定规格和属性的商品封面 |
| 23 | `newcouponpreferential` | `double` |  | 商品平摊的订单优惠金额 |
| 24 | `adtrace` | `string` |  | 广告跟踪标识 |
| 25 | `supplierassumeamount` | `double` |  | 商家承担优惠券金额 |
| 26 | `thirdorderid` | `string` |  | 活动平台（第三方）订单ID |
| 27 | `supplierassumebountyamount` | `double` |  | 商家承担的津贴金额 |
| 28 | `bountypreferential` | `double` |  | 津贴的优惠金额， 从 benefit_order 中分拆 |
| 29 | `sellpriceid` | `bigint` |  | 限时售价id |
| 30 | `sellprice` | `double` |  | 限时售价 |
| 31 | `presaletype` | `int` |  | 同 benefit_trade 的 preSaleType |
| 32 | `presaleid` | `bigint` |  | Benefit_ProductPreSale表的id |
| 33 | `depositprice` | `double` |  | 预售定金价格 |
| 34 | `cardactivitycode` | `string` |  | 抽卡活动code |
| 35 | `channellaunchtrace` | `string` |  | 渠道投放跟踪串 |
| 36 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 37 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 38 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 39 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 40 | `dt` | `string` |  |  |

---

## ods_binlog_blog_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_blog_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.0G |
| **是否分区表** | 是 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | ??ID |
| 2 | `blogname` | `string` |  | ????? |
| 3 | `blognickname` | `string` |  | ?????? |
| 4 | `smallavaimg` | `string` |  | ??? |
| 5 | `bigavaimg` | `string` |  | ??? |
| 6 | `keytag` | `string` |  | ?????? |
| 7 | `selfintro` | `string` |  | ???? |
| 8 | `blogcreatetime` | `bigint` |  | ?????? |
| 9 | `avaupdatetime` | `bigint` |  | ?????? |
| 10 | `rssfileid` | `bigint` |  | RSS??ID |
| 11 | `rssgentime` | `bigint` |  | RSS?????? |
| 12 | `postmodtime` | `bigint` |  | ???????? |
| 13 | `commentrank` | `int` |  | ???? |
| 14 | `imageprotected` | `int` |  | ?????? |
| 15 | `imagestamp` | `int` |  | ?????? |
| 16 | `imagedigitstamp` | `int` |  | ???????? |
| 17 | `postaddtime` | `bigint` |  | ????????????? |
| 18 | `gendar` | `int` |  | ?? |
| 19 | `birthday` | `bigint` |  | NULL |
| 20 | `avatarboxid` | `bigint` |  | 头像框ID |
| 21 | `updatetime` | `bigint` |  | 更新时间 |
| 22 | `extrabits` | `bigint` |  | 额外的bit属性 |
| 23 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 24 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 25 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 26 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 27 | `dt` | `string` |  |  |

---

## ods_binlog_comment_hot_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_comment_hot_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.4G |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `postid` | `bigint` |  | 点赞评论所属日志id |
| 4 | `commentid` | `bigint` |  | 点赞评论id |
| 5 | `optime` | `bigint` |  | 操作时间 |
| 6 | `status` | `bigint` |  | 状态 |
| 7 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 8 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 9 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 10 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 11 | `dt` | `string` |  |  |

---

## ods_binlog_dressing_user_suit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_dressing_user_suit_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6.4M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `showno` | `string` |  | 套装编号 |
| 3 | `userid` | `bigint` |  | 用户id |
| 4 | `suitid` | `bigint` |  | 套装id |
| 5 | `activestatus` | `int` |  | 套装激活状态 0-未激活 1-已激活 2-已赠送 |
| 6 | `status` | `int` |  | 套装装扮状态 0-未装扮 1-已装扮 |
| 7 | `source` | `int` |  | 套装获取来源 0-乐乎币购买 1-被赠予 2-兑换码 |
| 8 | `expiretype` | `int` |  | 有效期类型 0-基础包月 1-永久 |
| 9 | `luckyno` | `int` |  | 是否稀有编号 0-否，1-是 |
| 10 | `starttime` | `bigint` |  | 开始时间 |
| 11 | `endtime` | `bigint` |  | 截止时间 |
| 12 | `createtime` | `bigint` |  | 创建时间 |
| 13 | `db_update_time` | `bigint` |  | NULL |
| 14 | `suitcount` | `bigint` |  | 拥有的套装数量 |
| 15 | `expiredays` | `bigint` |  | 有效期天数，-1表示永久 |
| 16 | `fansvip` | `int` |  | 高粉是否已经自动获取 |
| 17 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 18 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 19 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 20 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 21 | `dt` | `string` |  |  |

---

## ods_binlog_message_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_message_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.0G |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `blogid` | `bigint` |  | 轻博客ID |
| 3 | `otherblogid` | `bigint` |  | 另一个轻博客ID |
| 4 | `publishtime` | `bigint` |  | 发送时间 |
| 5 | `content` | `string` |  | 私信内容 |
| 6 | `issender` | `int` |  | 是否为发送者 |
| 7 | `publisherblogid` | `bigint` |  | 发布者的轻博客ID |
| 8 | `type` | `bigint` |  | 私信类型 |
| 9 | `status` | `int` |  | 消息状态，1：新提交未审核不可见；2:通过 |
| 10 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 11 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 12 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 13 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 14 | `dt` | `string` |  |  |

---

## ods_binlog_photo_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_photo_post_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2.2G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 日志ID |
| 2 | `blogid` | `bigint` |  | 轻博客ID |
| 3 | `caption` | `string` |  | 照片日志的描述 |
| 4 | `photolinks` | `string` |  | 照片的链接串 |
| 5 | `photocaptions` | `string` |  | 照片的描述串 |
| 6 | `photoexifs` | `string` |  | 照片EXIF信息 |
| 7 | `phototype` | `int` |  | 照片日志类型，0：普通照片类型； 1：商品分享照片类型 |
| 8 | `embed` | `string` |  | 额外json字符串，PhotoType=1 存储商品相关信息 |
| 9 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 10 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 11 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 12 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 13 | `dt` | `string` |  |  |

---

## ods_binlog_post_collection_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_post_collection_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 111.4M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `collectionid` | `bigint` |  | 合集id |
| 4 | `postid` | `bigint` |  | 文章id |
| 5 | `status` | `int` |  | 状态，0正常/-1删除 |
| 6 | `createtime` | `bigint` |  | 文章加入合集时间 |
| 7 | `db_update_time` | `bigint` |  | NULL |
| 8 | `publishtime` | `bigint` |  | 文章发布时间 |
| 9 | `sortno` | `bigint` |  | 文章所在合集正序序号 |
| 10 | `posttype` | `int` |  | 文章类型 1-文字 2-图片 3-音乐 4-视频 5-回答 6-长文章 |
| 11 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 12 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 13 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 14 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 15 | `dt` | `string` |  |  |

---

## ods_binlog_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_post_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1346.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1346.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 日志ID |
| 2 | `blogid` | `bigint` |  | 日志所属轻博ID |
| 3 | `publisheruserid` | `bigint` |  | 作者UserID |
| 4 | `title` | `string` |  | 日志标题 |
| 5 | `publishtime` | `bigint` |  | 发布时间 |
| 6 | `modifytime` | `bigint` |  | 最近修改时间 |
| 7 | `ispublished` | `int` |  | 是否发布或草稿 |
| 8 | `allowview` | `int` |  | 日志权限： |
| 9 | `valid` | `int` |  | 标记位： |
| 10 | `rank` | `int` |  | 是否置顶，0未置顶，其余值表示置顶位置 |
| 11 | `tag` | `string` |  | 日志标签 |
| 12 | `movefrom` | `string` |  | 来自哪里 |
| 13 | `citerootpostid` | `bigint` |  | 引用的源头日志ID |
| 14 | `citerootblogid` | `bigint` |  | 引用的源头博客ID |
| 15 | `type` | `int` |  | 日志类型 |
| 16 | `iscontribute` | `int` |  | 是否为投稿 |
| 17 | `ip` | `string` |  | 发表日志的IP |
| 18 | `customlink` | `string` |  | 用户自定义日志链接 |
| 19 | `digest` | `string` |  | 日志的摘要 |
| 20 | `firstimageurl` | `string` |  | 日志的第一张图片地址 |
| 21 | `citeparentpostid` | `bigint` |  | 引用的直接来源日志ID |
| 22 | `citeparentblogid` | `bigint` |  | 引用的直接来源博客ID |
| 23 | `cctype` | `int` |  | cc知识共享类型 |
| 24 | `locationid` | `bigint` |  | 地理位置表id |
| 25 | `forbidshare` | `int` |  | 是否禁止转载至lofter |
| 26 | `allowreward` | `int` |  | 是否允许打赏 |
| 27 | `top` | `int` |  | 是否置顶 |
| 28 | `collectionid` | `bigint` |  | 合集id |
| 29 | `payview` | `int` |  | 是否付费阅读：0否 1是 |
| 30 | `createtime` | `bigint` |  | 记录创建时间 |
| 31 | `featuresetting` | `bigint` |  | 文章属性二进制位配置 |
| 32 | `iplocation` | `string` |  | ip所在地信息 |
| 33 | `sourcetype` | `int` |  | 文章分区 0-作品 1-茶水间 |
| 34 | `category` | `string` |  | 文章的类目，逗号分隔 |
| 35 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 36 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 37 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 38 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 39 | `dt` | `string` |  |  |

---

## ods_binlog_post_hot_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_post_hot_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 33.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 33.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `postid` | `bigint` |  | 热门日志ID |
| 3 | `blogid` | `bigint` |  | 热门日志所属的轻博ID |
| 4 | `publisheruserid` | `bigint` |  | 操作者的ID |
| 5 | `frompostid` | `bigint` |  | 引用日志Id |
| 6 | `fromblogid` | `bigint` |  | 引用博客Id |
| 7 | `topostid` | `bigint` |  | 被引用日志Id |
| 8 | `toblogid` | `bigint` |  | 被引用博客Id |
| 9 | `content` | `string` |  | 转载附加的内容 |
| 10 | `optime` | `bigint` |  | 操作时间 |
| 11 | `type` | `int` |  | 类型,转载或喜欢 |
| 12 | `ip` | `string` |  | IP |
| 13 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 14 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 15 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 16 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 17 | `dt` | `string` |  |  |

---

## ods_binlog_post_response_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_post_response_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 16.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 16.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `postid` | `bigint` |  | 回应的日志ID |
| 3 | `blogid` | `bigint` |  | 回应日志所属的轻博ID |
| 4 | `publisheruserid` | `bigint` |  | 回应者的ID |
| 5 | `content` | `string` |  | 回应的内容,里面包括回应的博客ID信息 |
| 6 | `publishtime` | `bigint` |  | 回应时间 |
| 7 | `valid` | `int` |  | 标记位： |
| 8 | `replytouserid` | `bigint` |  | 被回复的评论的发布者的id |
| 9 | `ip` | `string` |  | 发表评论的IP |
| 10 | `ext` | `string` |  | 额外的信息 |
| 11 | `replyl1commentid` | `bigint` |  | 回复一级评论id |
| 12 | `replyl2commentid` | `bigint` |  | 回复二级评论id |
| 13 | `commenthot` | `bigint` |  | 评论点赞数 |
| 14 | `replyl2count` | `bigint` |  | 2级评论数 |
| 15 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 16 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 17 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 18 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 19 | `pid` | `string` |  | 划线评章节id |
| 20 | `imgid` | `string` |  | 圈评的评论id |
| 21 | `top` | `int` |  | 置顶标识（0：否；1：是；） |
| 22 | `emoteids` | `string` |  | 表情id，半角逗号分隔多个 |
| 23 | `dt` | `string` |  |  |

---

## ods_binlog_profile_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_profile_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 456.7M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `email` | `string` |  | 用户邮箱名 |
| 3 | `mainblogid` | `bigint` |  | 主博客id |
| 4 | `profilecreatetime` | `bigint` |  | 用户建立时间 |
| 5 | `profilecreatefrom` | `string` |  | 用户建立来源 |
| 6 | `notifysetting` | `string` |  | 通知提醒 |
| 7 | `emailnotify` | `string` |  | 邮件提醒 |
| 8 | `blogsequence` | `string` |  | 用户博客的序列 |
| 9 | `favoritetagsequence` | `string` |  | 用户收藏的标签的序列 |
| 10 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 11 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 12 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 13 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 14 | `dt` | `string` |  |  |

---

## ods_binlog_pve_role_group_dialogue_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_pve_role_group_dialogue_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.2G |
| **是否分区表** | 是 |

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `groupid` | `bigint` |  | 组ID |
| 4 | `pveuserid` | `bigint` |  | PVE虚拟男友用户id |
| 5 | `roleid` | `bigint` |  | 角色ID |
| 6 | `roletype` | `bigint` |  | 角色类型 |
| 7 | `content` | `string` |  | 对话内容 |
| 8 | `type` | `int` |  | 对话类型，0普通, 1日常陪伴 |
| 9 | `audioflag` | `int` |  | 音频标识，1音频, 0文本 |
| 10 | `sortno` | `bigint` |  | 对话轮数 |
| 11 | `ext` | `string` |  | 扩展内容 AudioInfo |
| 12 | `createtime` | `bigint` |  | 创建时间 |
| 13 | `requestid` | `string` |  | 一组对话请求唯一ID |
| 14 | `sessionid` | `string` |  | 对话状态 |
| 15 | `status` | `int` |  | 对话状态标识，0初始，1已回答，2命中敏感词，3AI异常 |
| 16 | `aisource` | `int` |  | AI数据源标识，0minimax, 1伏羲 |
| 17 | `messagetype` | `int` |  | 消息类型，0普通消息，1道具，2副本，5动态 |
| 18 | `dtid` | `string` |  | 对话traceId |
| 19 | `grouptype` | `int` |  | 组类型，0：cp，1：群聊 |
| 20 | `sender` | `int` |  | 1:用户发送，0：ai回复 |
| 21 | `targetroleids` | `string` |  | 指定回复的角色id |
| 22 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 23 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 24 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 25 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 26 | `dt` | `string` |  |  |

---

## ods_binlog_pve_user_dialogue_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_pve_user_dialogue_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 6.1G |
| **是否分区表** | 是 |

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `pveuserid` | `bigint` |  | pve虚拟男友用户id |
| 4 | `sender` | `int` |  | 对话类型,0回复；1发送者 |
| 5 | `content` | `string` |  | 对话内容 |
| 6 | `type` | `int` |  | 对话类型0普通 |
| 7 | `audioflag` | `int` |  | 1音频,0文本 |
| 8 | `ext` | `string` |  | 扩展内容 |
| 9 | `createtime` | `bigint` |  | 创建时间 |
| 10 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 11 | `requestid` | `string` |  | 一组对话请求唯一ID |
| 12 | `status` | `int` |  | 对话状态，标识ai回复0初始，1已回答，2命中敏感词，3ai异常 |
| 13 | `aisource` | `int` |  | ai数据源:0minimax,1伏羲 |
| 14 | `sortno` | `bigint` |  | 对话序号 |
| 15 | `roleid` | `bigint` |  | 角色id |
| 16 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 17 | `rolename` | `string` |  | 角色名称, cp梦境冗余 |
| 18 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 19 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 20 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 21 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 22 | `messagetype` | `int` |  | 0普通消息，1道具，2混沌，3梦境，4任务，12评论，13动态 |
| 23 | `dt` | `string` |  |  |

---

## ods_binlog_pve_user_dialogue_partition_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_pve_user_dialogue_partition_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 46.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 46.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `pveuserid` | `bigint` |  | pve虚拟男友用户id |
| 4 | `sender` | `int` |  | 对话类型,0回复；1发送者 |
| 5 | `content` | `string` |  | 对话内容 |
| 6 | `type` | `int` |  | 对话类型0普通 |
| 7 | `audioflag` | `int` |  | 1音频,0文本 |
| 8 | `ext` | `string` |  | 扩展内容 |
| 9 | `createtime` | `bigint` |  | 创建时间 |
| 10 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 11 | `requestid` | `string` |  | 一组对话请求唯一ID |
| 12 | `status` | `int` |  | 对话状态，标识ai回复0初始，1已回答，2命中敏感词，3ai异常 |
| 13 | `aisource` | `int` |  | ai数据源:0minimax,1伏羲 |
| 14 | `sortno` | `bigint` |  | 对话序号 |
| 15 | `roleid` | `bigint` |  | 角色id |
| 16 | `roletype` | `int` |  | 0官方角色，1奇遇角色，2cp梦境，3光速匹配，4高阶定义 |
| 17 | `rolename` | `string` |  | 角色名称, cp梦境冗余 |
| 18 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 19 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 20 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 21 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 22 | `messagetype` | `int` |  | 0普通消息，1道具，2混沌，3梦境，4任务，12评论，13动态 |
| 23 | `site` | `int` |  | 站点，0：lofter，2：app |
| 24 | `sitedid` | `bigint` |  | 外部对话id |
| 25 | `dt` | `string` |  |  |

---

## ods_binlog_pve_user_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_pve_user_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 30.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 30.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `stamina` | `bigint` |  | 体力值 |
| 4 | `rechargeamount` | `double` |  | 累计充值金额 |
| 5 | `vipstarttime` | `bigint` |  | 月卡/周卡有效期 |
| 6 | `vipendtime` | `bigint` |  | 月卡/周卡有效期 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `blacklist` | `string` |  | 屏蔽设置 |
| 9 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 10 | `ext` | `string` |  | 扩展 |
| 11 | `sendmsgcount` | `bigint` |  | 用户主动发送的对话数 |
| 12 | `grasscount` | `bigint` |  | 忘忧草数量 |
| 13 | `grassamount` | `double` |  | 忘忧草充值金额 |
| 14 | `cpvipstarttime` | `bigint` |  | CP卡开始时间 |
| 15 | `cpvipendtime` | `bigint` |  | CP卡结束时间 |
| 16 | `skinbrushcount` | `bigint` |  | 贴皮笔数量 |
| 17 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 18 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 19 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 20 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 21 | `dt` | `string` |  |  |

---

## ods_binlog_pve_user_male_virtue_prison_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_pve_user_male_virtue_prison_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.5M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `roleid` | `bigint` |  | 角色id，0表示通用 |
| 3 | `userid` | `bigint` |  | 用户ID |
| 4 | `dialogueid` | `bigint` |  | 副本对话ID |
| 5 | `userdupid` | `bigint` |  | 用户副本ID |
| 6 | `label` | `string` |  | 标签 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `updatetime` | `bigint` |  | 更新时间 |
| 9 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 10 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 11 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 12 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 13 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 14 | `dt` | `string` |  |  |

---

## ods_binlog_recommend_post_review_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_recommend_post_review_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.5G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `reviewaccount` | `string` |  | 审核人 |
| 4 | `reviewtime` | `bigint` |  | 审核时间 |
| 5 | `reviewtype` | `int` |  | 审核类型 0-入库 1-不入库 |
| 6 | `createtime` | `bigint` |  | 创建时间 |
| 7 | `dbupdatetime` | `bigint` |  | NULL |
| 8 | `contentsign` | `string` |  | 内容签名 |
| 9 | `status` | `int` |  | 0/可用 -1/不可用 |
| 10 | `type` | `int` |  | 文章类型 |
| 11 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 12 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 13 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 14 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 15 | `dt` | `string` |  |  |

---

## ods_binlog_recommend_review_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_recommend_review_post_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 8.8G |
| **是否分区表** | 是 |

### 字段详情

共 24 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `blogid` | `bigint` |  | 博客ID |
| 3 | `postid` | `bigint` |  | 文章ID |
| 4 | `type` | `int` |  | 文章类型 1:TEXT 2:PHOTO 3:MUSIC 4:VIDEO 5:ASK 6:LONG |
| 5 | `recomstatus` | `tinyint` |  | 推荐状态 0初始；1推荐；-1不推荐 |
| 6 | `reviewaccount` | `string` |  | 最终审核账号 |
| 7 | `reviewtime` | `bigint` |  | 最终审核时间 |
| 8 | `createtime` | `bigint` |  | 创建时间 |
| 9 | `updatetime` | `bigint` |  | 更新时间 |
| 10 | `reviewstatus` | `tinyint` |  | 是否是二次审核，默认0 |
| 11 | `rectags` | `string` |  | 推荐标签 |
| 12 | `customtags` | `string` |  | 自定义标签 |
| 13 | `reasons` | `string` |  | 不入库原因 |
| 14 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 15 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 16 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 17 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 18 | `quality` | `int` |  | 0/normal 1/quality |
| 19 | `grade` | `int` |  | 0/normal 1/分级 |
| 20 | `lastpendingtime` | `bigint` |  | 最后一次入审核池时间 |
| 21 | `risklimit` | `int` |  | 风控限流标识 |
| 22 | `version` | `bigint` |  | 文章版本标识 |
| 23 | `dbupdatetime` | `bigint` |  |  |
| 24 | `dt` | `string` |  |  |

---

## ods_binlog_reward_user_benefit_period_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_reward_user_benefit_period_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 134.8K |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `usertype` | `int` |  | 1=新用户 2=回流低 3=回流高 |
| 4 | `startdate` | `bigint` |  | YYYYMMDD，福利期开始日 |
| 5 | `starttime` | `bigint` |  | 毫秒时间戳，24h弹窗判断 |
| 6 | `rewardchoice` | `int` |  | 0=未选 1=全额兑换 2=随机兑换 3=糖果券 |
| 7 | `lastsigndate` | `bigint` |  | YYYYMMDD，最后签到日期 |
| 8 | `status` | `int` |  | 0=正常 -1=已废弃 |
| 9 | `createtime` | `bigint` |  | 创建时间 |
| 10 | `updatetime` | `bigint` |  | 更新时间 |
| 11 | `dbcreatetime` | `bigint` |  | 数据创建时间 |
| 12 | `dbupdatetime` | `bigint` |  | 数据更新时间 |
| 13 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 14 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 15 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 16 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 17 | `dt` | `string` |  |  |

---

## ods_binlog_risk_antispam_callback_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_risk_antispam_callback_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 749.2M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `blogid` | `bigint` |  | 博客id |
| 4 | `type` | `int` |  | 类型 0文章/1图片 |
| 5 | `createtime` | `bigint` |  | 创建时间 |
| 6 | `operator` | `string` |  | 操作者 |
| 7 | `content` | `string` |  | 回调数据json |
| 8 | `status` | `int` |  | 回调状态 |
| 9 | `forbidtype` | `bigint` |  | 屏蔽类型 |
| 10 | `hint` | `string` |  | 屏蔽理由 |
| 11 | `dbupdatetime` | `bigint` |  | NULL |
| 12 | `version` | `bigint` |  | 回调文章的版本号 |
| 13 | `machine` | `int` |  | 审核类型，0人审/1机审 |
| 14 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 15 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 16 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 17 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 18 | `dt` | `string` |  |  |

---

## ods_binlog_risk_antispam_post_image_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_risk_antispam_post_image_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 248.6M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `createtime` | `bigint` |  | 创建时间 |
| 3 | `postid` | `bigint` |  | 文章id |
| 4 | `blogid` | `bigint` |  | 博客id |
| 5 | `bucket` | `string` |  | 原图桶名 |
| 6 | `objectname` | `string` |  | 原图对象名 |
| 7 | `level` | `int` |  | 审核级别 |
| 8 | `label` | `bigint` |  | 类别 |
| 9 | `rate` | `double` |  | 置信分数 |
| 10 | `forbidstatus` | `int` |  | 屏蔽状态 0正常/1屏蔽 |
| 11 | `imgurl` | `string` |  | 备份图 |
| 12 | `dbupdatetime` | `bigint` |  | NULL |
| 13 | `tag` | `string` |  | 审核标签小类别 |
| 14 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 15 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 16 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 17 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 18 | `dt` | `string` |  |  |

---

## ods_binlog_risk_antispam_post_tmp2_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_risk_antispam_post_tmp2_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 110.1K |
| **是否分区表** | 是 |

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `postid` | `bigint` |  | 文章id |
| 4 | `type` | `int` |  | 文章类型 |
| 5 | `label` | `string` |  | 审核标签 |
| 6 | `level` | `int` |  | 级别 屏蔽/嫌疑/正常 |
| 7 | `createtime` | `bigint` |  | 创建时间 |
| 8 | `dbupdatetime` | `bigint` |  | NULL |
| 9 | `tag` | `string` |  | 审核标签小类别 |
| 10 | `status` | `int` |  | 处理状态，0:未处理 1已处理 |
| 11 | `lastopuser` | `string` |  | 上次操作人 |
| 12 | `allowview` | `int` |  | 日志权限 |
| 13 | `hist` | `int` |  | 是否为历史数据 |
| 14 | `operator` | `string` |  | 操作者 |
| 15 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 16 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 17 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 18 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 19 | `dt` | `string` |  |  |

---

## ods_binlog_risk_antispam_response_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_risk_antispam_response_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `postid` | `bigint` |  | 文章id |
| 4 | `responseid` | `bigint` |  | 评论id |
| 5 | `type` | `int` |  | 文章类型 |
| 6 | `label` | `string` |  | 审核标签 |
| 7 | `level` | `int` |  | 级别 屏蔽/嫌疑/正常 |
| 8 | `createtime` | `bigint` |  | 创建时间 |
| 9 | `dbupdatetime` | `bigint` |  | NULL |
| 10 | `tag` | `string` |  | 审核标签小类别 |
| 11 | `status` | `int` |  | 处理状态，0:未处理 1已处理 |
| 12 | `lastopuser` | `string` |  | 上次操作人 |
| 13 | `allowview` | `int` |  | 日志权限 |
| 14 | `hist` | `int` |  | 是否为历史数据 |
| 15 | `operator` | `string` |  | 操作者 |
| 16 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 17 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 18 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 19 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 20 | `dt` | `string` |  |  |

---

## ods_binlog_rta_creator_ad_switch_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_rta_creator_ad_switch_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 43.5K |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 作者UserID |
| 3 | `openad` | `int` |  | 0关闭，1开启 |
| 4 | `createtime` | `bigint` |  | 创建时间 |
| 5 | `updatetime` | `bigint` |  | 更新时间 |
| 6 | `lastclosetime` | `bigint` |  | 上次关闭时间 |
| 7 | `dbcreatetime` | `bigint` |  | 创建时间 |
| 8 | `dbupdatetime` | `bigint` |  | DB更新时间 |
| 9 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 10 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 11 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 12 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 13 | `dt` | `string` |  |  |

---

## ods_binlog_share_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_share_post_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 361.9M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | ID |
| 2 | `postid` | `bigint` |  | 日志ID |
| 3 | `blogid` | `bigint` |  | 日志所属的轻博ID |
| 4 | `userid` | `bigint` |  | 分享的用户ID |
| 5 | `optime` | `bigint` |  | 分享的操作时间 |
| 6 | `status` | `bigint` |  | 状态 |
| 7 | `msg` | `string` |  | 推荐语 |
| 8 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 9 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 10 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 11 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 12 | `dt` | `string` |  |  |

---

## ods_binlog_tag_resource_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_tag_resource_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 30.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 30.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 标签ID |
| 2 | `tagname` | `string` |  | 标签名称 |
| 3 | `postid` | `bigint` |  | 所属日志ID |
| 4 | `blogid` | `bigint` |  | 所属轻博ID |
| 5 | `publishtime` | `bigint` |  | 发布时间 |
| 6 | `posthot` | `bigint` |  | 日志hot数 |
| 7 | `ispublic` | `int` |  | 是否公开 |
| 8 | `movefrom` | `string` |  | 来自哪里 |
| 9 | `createtime` | `bigint` |  | 创建时间 |
| 10 | `updatetime` | `bigint` |  | 修改时间 |
| 11 | `type` | `int` |  | 日志类型 |
| 12 | `status` | `int` |  | -1:删除 0:正常 1:刷热 |
| 13 | `latestcommenttime` | `bigint` |  | 该标签下该文章最新评论时间 |
| 14 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 15 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 16 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 17 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 18 | `dt` | `string` |  |  |

---

## ods_binlog_text_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_text_post_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 120.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 120.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 日志ID |
| 2 | `blogid` | `bigint` |  | 日志所属轻博ID |
| 3 | `content` | `string` |  | 文本日志内容 |
| 4 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 5 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 6 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 7 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 8 | `dt` | `string` |  |  |

---

## ods_binlog_trade_fans_vip_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_fans_vip_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 2.2M |
| **是否分区表** | 是 |

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `tradeid` | `bigint` |  | 对应trade_recode表时的id |
| 3 | `userid` | `bigint` |  | NULL |
| 4 | `vipblogid` | `bigint` |  | 购买目标用户的vip |
| 5 | `status` | `int` |  | 0购买失败，1购买成功 |
| 6 | `platform` | `int` |  | 1安卓，2苹果 |
| 7 | `paytype` | `int` |  | 支付渠道，1支付宝，2苹果支付 |
| 8 | `amount` | `double` |  | 交易总金额，未减去渠道分层（channelDivision）和手续费（fee) |
| 9 | `fee` | `double` |  | 手续费，这里应该没有 |
| 10 | `channeldivision` | `double` |  | 苹果渠道分成30%，只是记录一下 |
| 11 | `createtime` | `bigint` |  | 创建时间 |
| 12 | `finishtime` | `bigint` |  | 修改时间 |
| 13 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 14 | `productid` | `bigint` |  | 粉丝会员商品id |
| 15 | `bankordersn` | `string` |  | 第三方支付流水号 |
| 16 | `bankordertime` | `bigint` |  | 第三方支付时间 |
| 17 | `vipdays` | `bigint` |  | 购买会员天数 |
| 18 | `postid` | `bigint` |  | 支付时关联的文章id |
| 19 | `deviceid` | `string` |  | 设备号 |
| 20 | `effectivetime` | `bigint` |  | 该笔订单购买的会员生效开始时间 |
| 21 | `promotion` | `bigint` |  | 是否是推广订单 |
| 22 | `promotiontime` | `bigint` |  | 推广订单标志更新时间 |
| 23 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 24 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 25 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 26 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 27 | `dt` | `string` |  |  |

---

## ods_binlog_trade_gift_pay_account_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_gift_pay_account_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 10.8M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `openfrom` | `string` |  | 开通来源（INVITE=邀请；ADMIN=后端；） |
| 4 | `status` | `bigint` |  | 0=WAIT_NOTIFY,1=WAIT_AGREE,2=AGREED,10=CLOSE,11=BANNED |
| 5 | `agreetime` | `bigint` |  | 同意开通时间 |
| 6 | `acceptgiftflag` | `int` |  | 接受礼物开关（0：关闭；1：开启；） |
| 7 | `createtime` | `bigint` |  | 业务上的创建时间 |
| 8 | `dbcreatetime` | `bigint` |  | 创建时间 |
| 9 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 10 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 11 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 12 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 13 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 14 | `dt` | `string` |  |  |

---

## ods_binlog_trade_gift_present_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_gift_present_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 689.8M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `sender` | `bigint` |  | 送礼用户id |
| 3 | `postid` | `bigint` |  | 文章id |
| 4 | `blogid` | `bigint` |  | 文章博客id |
| 5 | `giftid` | `bigint` |  | 礼物id |
| 6 | `returngiftid` | `bigint` |  | 回礼礼物id |
| 7 | `gifttype` | `bigint` |  | 礼物类型 |
| 8 | `count` | `bigint` |  | 礼物数量 |
| 9 | `coin` | `bigint` |  | 消耗乐乎币数量 |
| 10 | `ip` | `string` |  | ip |
| 11 | `status` | `int` |  | 状态 |
| 12 | `createtime` | `bigint` |  | 创建时间 |
| 13 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 14 | `accounttype` | `bigint` |  | 收礼人身份类型 0-素人类型 1-激励类型 |
| 15 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 16 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 17 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 18 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 19 | `signtype` | `bigint` |  | 收礼内容类型 0-UGC 1-PGC |
| 20 | `ext` | `string` |  | 赠礼记录新增扩展字段，记录抽中的partId |
| 21 | `dt` | `string` |  |  |

---

## ods_binlog_trade_post_pack_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_post_pack_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.9M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `tradeid` | `bigint` |  | 对应trade_order表时的id |
| 3 | `userid` | `bigint` |  | NULL |
| 4 | `packid` | `bigint` |  | 购买的文包标识 |
| 5 | `status` | `int` |  | 0购买失败，1购买成功 |
| 6 | `platform` | `int` |  | 1安卓，2苹果 |
| 7 | `paytype` | `int` |  | 支付渠道，1支付宝，2苹果支付 |
| 8 | `amount` | `double` |  | 交易总金额，未减去渠道分层（channelDivision）和手续费（fee) |
| 9 | `fee` | `double` |  | 手续费，这里应该没有 |
| 10 | `channeldivision` | `double` |  | 苹果渠道分成30%，只是记录一下 |
| 11 | `createtime` | `bigint` |  | 创建时间 |
| 12 | `finishtime` | `bigint` |  | 修改时间 |
| 13 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 14 | `bankordersn` | `string` |  | 第三方支付流水号 |
| 15 | `bankordertime` | `bigint` |  | 第三方支付时间 |
| 16 | `deviceid` | `string` |  | 设备号 |
| 17 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 18 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 19 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 20 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 21 | `dt` | `string` |  |  |

---

## ods_binlog_trade_return_gift_exchange_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_return_gift_exchange_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 132.5M |
| **是否分区表** | 是 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `userid` | `bigint` |  | 用户Id |
| 3 | `couponid` | `bigint` |  | 兑换券id |
| 4 | `postid` | `bigint` |  | 文章id |
| 5 | `status` | `int` |  | 状态 0-初始状态；1-已解锁 |
| 6 | `exchangetime` | `bigint` |  | 兑换时间 |
| 7 | `startindex` | `bigint` |  | 窗口start |
| 8 | `createtime` | `bigint` |  | 创建时间 |
| 9 | `ext` | `string` |  | 扩展信息 |
| 10 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 11 | `blogid` | `bigint` |  | 文章博客id |
| 12 | `planid` | `bigint` |  | 回礼id |
| 13 | `relatedid` | `bigint` |  | 关联id,如兑换券Trade_UserExchangeCoupon表id |
| 14 | `exchangegiftid` | `bigint` |  | 抵用礼物id |
| 15 | `type` | `int` |  | 类型 0-早期兑换券；1-兑换文章；2-抽奖 |
| 16 | `parentorderid` | `bigint` |  | 父订单id |
| 17 | `parentordertype` | `bigint` |  | 0-无父订单 |
| 18 | `scene` | `int` |  | 来源，0：普通；1:新用户，2：合集聚合支付 |
| 19 | `needsettle` | `int` |  | 是否结算，0：结算；-1：不结算 |
| 20 | `signtype` | `bigint` |  | 收礼内容类型 0-UGC 1-PGC |
| 21 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 22 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 23 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 24 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 25 | `dt` | `string` |  |  |

---

## ods_binlog_trade_return_gift_plan_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_return_gift_plan_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 690.3M |
| **是否分区表** | 是 |

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键 |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `content` | `string` |  | 内容 |
| 4 | `imagejson` | `string` |  | 图片信息 |
| 5 | `plantype` | `bigint` |  | 回礼类型Id |
| 6 | `giftjson` | `string` |  | 适用的赠礼信息 |
| 7 | `auditstatus` | `int` |  | 审核状态（0：待审核；1：通过；2：不通过；） |
| 8 | `audittime` | `bigint` |  | 审核时间 |
| 9 | `status` | `int` |  | 状态（-1：删除；0：未生效；1：生效；） |
| 10 | `createtime` | `bigint` |  | 创建时间 |
| 11 | `updatetime` | `bigint` |  | 修改时间 |
| 12 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 13 | `signature` | `string` |  | 内容签名串 |
| 14 | `promotion` | `string` |  | 宣传文案 |
| 15 | `showpromotion` | `int` |  | 是否显示宣传模块 0 - 隐藏 1-显示 |
| 16 | `blogid` | `bigint` |  | 文章博客ID |
| 17 | `unlocktype` | `int` |  | 回礼解锁方式 0-不限制 1-仅高粉 |
| 18 | `screenshotflag` | `int` |  | 截屏保护开关 0-不限制 1-开启保护 |
| 19 | `reviewstatus` | `int` |  | 0/未标记；1/良好；-1/低质；-2/负面 |
| 20 | `displaytips` | `int` |  | 0/不展示提示；1/展示提示 |
| 21 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 22 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 23 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 24 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 25 | `dt` | `string` |  |  |

---

## ods_binlog_trade_store_vip_order_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_store_vip_order_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 15.1M |
| **是否分区表** | 是 |

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `tradeid` | `bigint` |  | 对应trade_order表时的id |
| 3 | `userid` | `bigint` |  | NULL |
| 4 | `bookstoreid` | `bigint` |  | 购买的目标书城标识 |
| 5 | `status` | `int` |  | 0购买失败，1购买成功 |
| 6 | `source` | `int` |  | 0用户，1系统包月自动下单 |
| 7 | `platform` | `int` |  | 1安卓，2苹果 |
| 8 | `paytype` | `int` |  | 支付渠道，1支付宝，2苹果支付 |
| 9 | `amount` | `double` |  | 交易总金额，未减去渠道分层（channelDivision）和手续费（fee) |
| 10 | `fee` | `double` |  | 手续费，这里应该没有 |
| 11 | `channeldivision` | `double` |  | 苹果渠道分成30%，只是记录一下 |
| 12 | `createtime` | `bigint` |  | 创建时间 |
| 13 | `finishtime` | `bigint` |  | 修改时间 |
| 14 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 15 | `productid` | `bigint` |  | 书城会员商品id |
| 16 | `bankordersn` | `string` |  | 第三方支付流水号 |
| 17 | `bankordertime` | `bigint` |  | 第三方支付时间 |
| 18 | `vipdays` | `bigint` |  | 购买会员天数 |
| 19 | `postid` | `bigint` |  | 支付时关联的文章id |
| 20 | `deviceid` | `string` |  | 设备号 |
| 21 | `effectivetime` | `bigint` |  | 该笔订单购买的会员生效开始时间 |
| 22 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 23 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 24 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 25 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 26 | `dt` | `string` |  |  |

---

## ods_binlog_trade_support_record_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_support_record_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.2G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | NULL |
| 2 | `time` | `bigint` |  | 产生时间点 |
| 3 | `supporterblogid` | `bigint` |  | 支持者博客的id |
| 4 | `blogid` | `bigint` |  | 被支持的博客id |
| 5 | `itemtype` | `string` |  | 被支持的内容类型 |
| 6 | `itemid` | `bigint` |  | 被支持的内容关联id |
| 7 | `score` | `bigint` |  | 支持的数量 |
| 8 | `reftype` | `string` |  | 关联的类型 |
| 9 | `refid` | `bigint` |  | 关联的id |
| 10 | `reason` | `string` |  | 产生的原因 |
| 11 | `dbcreatetime` | `bigint` |  | 创建时间 |
| 12 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 13 | `idem` | `string` |  | 幂等字段 |
| 14 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 15 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 16 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 17 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 18 | `dt` | `string` |  |  |

---

## ods_binlog_trade_user_exchange_coupon_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_user_exchange_coupon_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 194.3M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `userid` | `bigint` |  | 用户Id |
| 3 | `couponid` | `bigint` |  | 兑换券id |
| 4 | `count` | `bigint` |  | 数量 |
| 5 | `balance` | `bigint` |  | 未使用数量 |
| 6 | `createtime` | `bigint` |  | 创建时间 |
| 7 | `expiretime` | `bigint` |  | 失效时间 |
| 8 | `ext` | `string` |  | 扩展信息 |
| 9 | `tradeid` | `bigint` |  | 涉及订单时候，对应trade_order表时的id |
| 10 | `type` | `bigint` |  | 获取方式 0-充值赠送 1-累计消费赠送 2-抽奖获取 3-购买 |
| 11 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 12 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 13 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 14 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 15 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 16 | `dt` | `string` |  |  |

---

## ods_binlog_trade_user_free_gift_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_trade_user_free_gift_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 768.5M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `giftid` | `bigint` |  | 礼物id |
| 4 | `count` | `bigint` |  | 发放数量 |
| 5 | `balance` | `bigint` |  | 礼物余额 |
| 6 | `type` | `bigint` |  | 发放类型 |
| 7 | `rule` | `string` |  | 发放规则 |
| 8 | `starttime` | `bigint` |  | 开始时间 |
| 9 | `endtime` | `bigint` |  | 结束时间 |
| 10 | `status` | `int` |  | 状态 0-未使用 1-使用中 2-已使用 -1-已过期 |
| 11 | `createtime` | `bigint` |  | 创建时间 |
| 12 | `dbupdatetime` | `bigint` |  | 更新时间 |
| 13 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 14 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 15 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 16 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 17 | `dt` | `string` |  |  |

---

## ods_binlog_user_close_account_data_bak_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_user_close_account_data_bak_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 132.1M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `blogid` | `bigint` |  | 博客id |
| 4 | `target` | `string` |  | 目标表名 |
| 5 | `content` | `string` |  | 删除数据 |
| 6 | `createtime` | `bigint` |  | 创建时间 |
| 7 | `db_update_time` | `bigint` |  | NULL |
| 8 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 9 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 10 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 11 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 12 | `dt` | `string` |  |  |

---

## ods_binlog_user_close_account_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_user_close_account_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.7M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键id |
| 2 | `userid` | `bigint` |  | 用户id |
| 3 | `email` | `string` |  | 用户email |
| 4 | `status` | `int` |  | 状态: 0申请注销 1取消注销 2系统注销 |
| 5 | `ip` | `string` |  | ip记录 |
| 6 | `createtime` | `bigint` |  | 创建时间 |
| 7 | `db_update_time` | `bigint` |  | NULL |
| 8 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 9 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 10 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 11 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 12 | `dt` | `string` |  |  |

---

## ods_binlog_user_following_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_user_following_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.7G |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `blogid` | `bigint` |  | 关注的轻博客ID |
| 4 | `followtime` | `bigint` |  | 关注时间 |
| 5 | `hotcount` | `bigint` |  | 热度数 |
| 6 | `responsecount` | `bigint` |  | 评论数 |
| 7 | `score` | `bigint` |  | 总分数 |
| 8 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 9 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 10 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 11 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 12 | `lastpublishtime` | `bigint` |  |  |
| 13 | `lastvisittime` | `bigint` |  |  |
| 14 | `dt` | `string` |  |  |

---

## ods_binlog_user_statistic_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_binlog_user_statistic_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 25.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 25.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 29 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户id |
| 2 | `logincount` | `bigint` |  | 用户登录次数 |
| 3 | `lastlogintime` | `bigint` |  | 用户上次登录时间 |
| 4 | `followingcount` | `bigint` |  | 用户跟踪的博客数量 |
| 5 | `blacklistcount` | `bigint` |  | 用户黑名单数量 |
| 6 | `blogcount` | `bigint` |  | 用户拥有的博客数量 |
| 7 | `favoritepostcount` | `bigint` |  | 用户喜欢的日志数 |
| 8 | `uploaddiymusicsize` | `bigint` |  | 已上传的原创音乐的大小 |
| 9 | `recommendcount` | `bigint` |  | 被加精次数 |
| 10 | `invitecodecount` | `bigint` |  | 邀请码提醒数 |
| 11 | `publishpostcount` | `bigint` |  | 用户文章发表数 |
| 12 | `favoritetagcount` | `bigint` |  | 用户订阅标签数 |
| 13 | `bulletinloadtime` | `bigint` |  | 用户读取系统公告的时间 |
| 14 | `sharepostcount` | `bigint` |  | 用户分享的文章计数 |
| 15 | `lastloginip` | `string` |  | 最后登录IP |
| 16 | `applogincount` | `bigint` |  | 用户移动端登录次数 |
| 17 | `robotliketime` | `bigint` |  | 机器喜欢时间 |
| 18 | `subscribepostcount` | `bigint` |  | 用户收藏文章数 |
| 19 | `avatarboxid` | `bigint` |  | 头像框ID |
| 20 | `avatarboximage` | `string` |  | 头像框url |
| 21 | `subscribecollectionviewtime` | `bigint` |  | 我的收藏上次拉取时间 |
| 22 | `questionboxfetchtime` | `bigint` |  | 拉取问题箱时间 |
| 23 | `userremoteport` | `bigint` |  | 用户侧端口号 |
| 24 | `postresponsecount` | `bigint` |  | 用户发布的评论数 |
| 25 | `_bin_op` | `int` |  | binlog操作 0插入 1删除 2更新 |
| 26 | `_bin_op_time` | `bigint` |  | binlog操作时间 |
| 27 | `_bin_op_seqno` | `bigint` |  | binlog操作序号 |
| 28 | `_bin_old` | `map<string, string>` |  | binlog更新操作变更前数据 |
| 29 | `dt` | `string` |  |  |

---

## ods_db_text_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_db_text_post_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | internal |
| **表大小** | 306.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 306.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 日志ID |
| 2 | `blogid` | `bigint` |  | 日志所属轻博ID |
| 3 | `content` | `string` |  | 文本日志内容 |
| 4 | `dt` | `string` |  |  |

---

## ods_kafka_search_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_kafka_search_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 59.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 59.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceid` | `string` |  |  |
| 2 | `wk` | `string` |  |  |
| 3 | `createtime` | `bigint` |  |  |
| 4 | `isassociat` | `int` |  |  |
| 5 | `type` | `int` |  |  |
| 6 | `dt` | `string` |  |  |

---

## ods_log_ab_platform_sdk_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ab_platform_sdk_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ablogversion` | `string` |  | from deserializer |
| 2 | `appversion` | `string` |  | from deserializer |
| 3 | `deviceid` | `string` |  | from deserializer |
| 4 | `expid` | `bigint` |  | from deserializer |
| 5 | `groupid` | `bigint` |  | from deserializer |
| 6 | `os` | `string` |  | from deserializer |
| 7 | `sceneid` | `int` |  | from deserializer |
| 8 | `time` | `bigint` |  | from deserializer |
| 9 | `traceid` | `string` |  | from deserializer |
| 10 | `userid` | `string` |  | from deserializer |
| 11 | `backend_deviceid` | `string` |  | from deserializer |
| 12 | `dt` | `string` |  |  |

---

## ods_log_ab_test_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ab_test_di` |
| **描述** | lofter backend abTest log |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 187.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 187.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `abtestid` | `bigint` |  | ab实验ID |
| 2 | `abgroupid` | `bigint` |  | ab实验分组ID |
| 3 | `abname` | `string` |  | ab实验名称 |
| 4 | `abgroup` | `string` |  | ab实验分组名称 |
| 5 | `userid` | `bigint` |  | 用户ID |
| 6 | `createtime` | `bigint` |  | 创建事件 |
| 7 | `deviceos` | `string` |  | 操作系统 |
| 8 | `deviceid` | `string` |  | 设备Id |
| 9 | `groupkey` | `string` |  | 类型 |
| 10 | `logtime` | `string` |  | 日志采集时间 |
| 11 | `logtype` | `string` |  | 日志类型 |
| 12 | `ext` | `string` |  | 额外信息json形式 |
| 13 | `dt` | `string` |  |  |

---

## ods_log_access_shortlink_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_access_shortlink_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2.6M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `shortlinkid` | `string` |  | 短链id |
| 2 | `ip` | `string` |  | 用户IP |
| 3 | `time` | `bigint` |  | 访问时间 |
| 4 | `originalurl` | `string` |  | 原始链接url |
| 5 | `ua` | `string` |  | 浏览器UserAgent |
| 6 | `userid` | `bigint` |  | 用户ID |
| 7 | `mobile` | `string` |  | 用户手机号 |
| 8 | `dt` | `string` |  |  |

---

## ods_log_activity_signin_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_activity_signin_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 4.2M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `reward` | `double` |  |  |
| 2 | `actiontype` | `string` |  |  |
| 3 | `signtime` | `bigint` |  |  |
| 4 | `activityname` | `string` |  |  |
| 5 | `risk` | `int` |  |  |
| 6 | `userid` | `bigint` |  |  |
| 7 | `deviceid` | `string` |  |  |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_ab_test_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_ab_test_di` |
| **描述** | lofter backend adAbTest log |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 96.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 96.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `abtestid` | `bigint` |  | ab实验ID |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `logtype` | `string` |  | 日志类型 |
| 4 | `deviceos` | `string` |  | 操作系统 |
| 5 | `deviceid` | `string` |  | 设备Id |
| 6 | `itemid` | `string` |  | itemID |
| 7 | `itemtype` | `string` |  | 类型 |
| 8 | `type` | `bigint` |  | AB实验分组类型 |
| 9 | `createtime` | `bigint` |  | 创建事件 |
| 10 | `logtime` | `string` |  | 日志采集时间 |
| 11 | `ext` | `string` |  | 额外信息 |
| 12 | `dt` | `string` |  |  |

---

## ods_log_ad_attribution_register_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_attribution_register_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5.8M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceid` | `string` |  |  |
| 2 | `actionid` | `string` |  |  |
| 3 | `time` | `bigint` |  |  |
| 4 | `registeruserid` | `bigint` |  |  |
| 5 | `retain` | `int` |  |  |
| 6 | `appid` | `string` |  |  |
| 7 | `vcuserid` | `bigint` |  |  |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_attribution_retain_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_attribution_retain_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceid` | `string` |  |  |
| 2 | `actionid` | `string` |  |  |
| 3 | `time` | `bigint` |  |  |
| 4 | `registeruserid` | `bigint` |  |  |
| 5 | `retain` | `int` |  |  |
| 6 | `appid` | `string` |  |  |
| 7 | `vcuserid` | `bigint` |  |  |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_attribution_trade_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_attribution_trade_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceid` | `string` |  |  |
| 2 | `actionid` | `string` |  |  |
| 3 | `orderid` | `bigint` |  |  |
| 4 | `ordertype` | `int` |  |  |
| 5 | `paytime` | `bigint` |  |  |
| 6 | `orderamount` | `double` |  |  |
| 7 | `appid` | `string` |  |  |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_click_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_click_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 316.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 316.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 23 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  |  |
| 2 | `channel` | `string` |  |  |
| 3 | `idfa` | `string` |  |  |
| 4 | `actiontype` | `string` |  |  |
| 5 | `actionparam1` | `string` |  |  |
| 6 | `actionparam2` | `string` |  |  |
| 7 | `actionparam3` | `string` |  |  |
| 8 | `actiontime` | `bigint` |  |  |
| 9 | `actionip` | `string` |  |  |
| 10 | `callback` | `string` |  |  |
| 11 | `status` | `int` |  |  |
| 12 | `createtime` | `bigint` |  |  |
| 13 | `updatetime` | `bigint` |  |  |
| 14 | `actiondevice` | `string` |  |  |
| 15 | `expiredays` | `int` |  |  |
| 16 | `oaid` | `string` |  |  |
| 17 | `platform` | `string` |  |  |
| 18 | `newuser` | `int` |  |  |
| 19 | `uniquekey` | `string` |  |  |
| 20 | `hashdip` | `string` |  |  |
| 21 | `caid1` | `string` |  |  |
| 22 | `caid2` | `string` |  |  |
| 23 | `dt` | `string` |  |  |

---

## ods_log_ad_client_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_client_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 149.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 149.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  |  |
| 2 | `deviceos` | `string` |  |  |
| 3 | `adid` | `string` |  |  |
| 4 | `adsource` | `string` |  |  |
| 5 | `requestuuid` | `string` |  |  |
| 6 | `adposition` | `bigint` |  |  |
| 7 | `time` | `bigint` |  |  |
| 8 | `appversion` | `string` |  |  |
| 9 | `click` | `bigint` |  | 是否点击事件 0否 1是 |
| 10 | `expose` | `bigint` |  | 是否曝光事件 0否 1是 |
| 11 | `adcalltime` | `bigint` |  |  |
| 12 | `pos` | `int` |  |  |
| 13 | `adtrace` | `string` |  | adtrace跟踪串 |
| 14 | `adtracename` | `string` |  | adtrace名称: 基于adtrace上json后缀解析出来的 |
| 15 | `app` | `string` |  |  |
| 16 | `reqid` | `string` |  | 请求id |
| 17 | `dt` | `string` |  |  |

---

## ods_log_ad_deeplink_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_deeplink_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 13.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 13.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `market` | `string` |  |  |
| 2 | `dadeviceid` | `string` |  |  |
| 3 | `adclick` | `map<string, string>` |  |  |
| 4 | `campaignid` | `string` |  |  |
| 5 | `ip` | `string` |  |  |
| 6 | `mid` | `string` |  |  |
| 7 | `ua` | `string` |  |  |
| 8 | `deviceid` | `string` |  |  |
| 9 | `aid` | `string` |  |  |
| 10 | `advertiserid` | `string` |  |  |
| 11 | `cid` | `string` |  |  |
| 12 | `target` | `string` |  |  |
| 13 | `matchid` | `string` |  |  |
| 14 | `logtime` | `string` |  |  |
| 15 | `deviceinfo` | `string` |  | 设备消息 |
| 16 | `product` | `string` |  | 客户端版本 |
| 17 | `dt` | `string` |  |  |

---

## ods_log_ad_device_info_upload_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_device_info_upload_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 71.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 71.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `androidid` | `string` |  |  |
| 5 | `oaid` | `string` |  |  |
| 6 | `oaidmd5` | `string` |  |  |
| 7 | `ua` | `string` |  |  |
| 8 | `deviceid` | `string` |  |  |
| 9 | `ip` | `string` |  |  |
| 10 | `idfa` | `string` |  |  |
| 11 | `idfamd5` | `string` |  |  |
| 12 | `caidinfo` | `string` |  |  |
| 13 | `dt` | `string` |  |  |

---

## ods_log_ad_dsp_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_dsp_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 7989.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 7989.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `adid` | `string` |  |  |
| 2 | `appid` | `string` |  |  |
| 3 | `dspid` | `string` |  |  |
| 4 | `os` | `string` |  |  |
| 5 | `positionid` | `string` |  |  |
| 6 | `positionname` | `string` |  |  |
| 7 | `success` | `int` |  |  |
| 8 | `requesttime` | `bigint` |  |  |
| 9 | `responsetime` | `bigint` |  |  |
| 10 | `msg` | `string` |  |  |
| 11 | `externaladid` | `string` |  |  |
| 12 | `ip` | `string` |  |  |
| 13 | `wakeupboot` | `int` |  |  |
| 14 | `winflag` | `int` |  |  |
| 15 | `la` | `string` |  |  |
| 16 | `lo` | `string` |  |  |
| 17 | `version` | `string` |  |  |
| 18 | `uuid` | `string` |  |  |
| 19 | `banwords` | `string` |  |  |
| 20 | `reqid` | `string` |  |  |
| 21 | `industryid` | `string` |  |  |
| 22 | `slotid` | `string` |  | site 格式形如: 6ED29071:STARTUP:1 |
| 23 | `advertiser_type` | `string` |  | 传媒客户类型（直客:0 ,非直客:1 ,品牌:2） |
| 24 | `price` | `double` |  | 竞价 |
| 25 | `blogid` | `bigint` |  | 用户ID |
| 26 | `ext` | `map<string, string>` |  | dsp扩展字段 |
| 27 | `bidfactor` | `double` |  |  |
| 28 | `dt` | `string` |  |  |

---

## ods_log_ad_dsp_raw_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_dsp_raw_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 30.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 30.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 28 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `adid` | `string` |  |  |
| 2 | `appid` | `string` |  |  |
| 3 | `dspid` | `string` |  |  |
| 4 | `os` | `string` |  |  |
| 5 | `positionid` | `string` |  |  |
| 6 | `positionname` | `string` |  |  |
| 7 | `success` | `int` |  |  |
| 8 | `requesttime` | `bigint` |  |  |
| 9 | `responsetime` | `bigint` |  |  |
| 10 | `msg` | `string` |  |  |
| 11 | `externaladid` | `string` |  |  |
| 12 | `ip` | `string` |  |  |
| 13 | `wakeupboot` | `int` |  |  |
| 14 | `winflag` | `int` |  |  |
| 15 | `la` | `string` |  |  |
| 16 | `lo` | `string` |  |  |
| 17 | `version` | `string` |  |  |
| 18 | `uuid` | `string` |  |  |
| 19 | `banwords` | `string` |  |  |
| 20 | `reqid` | `string` |  |  |
| 21 | `industryid` | `string` |  |  |
| 22 | `slotid` | `string` |  | site 格式形如: 6ED29071:STARTUP:1 |
| 23 | `advertiser_type` | `string` |  | 传媒客户类型（直客:0 ,非直客:1 ,品牌:2） |
| 24 | `price` | `double` |  | 竞价 |
| 25 | `blogid` | `bigint` |  | 用户ID |
| 26 | `ext` | `map<string, string>` |  | dsp扩展字段 |
| 27 | `bidfactor` | `double` |  |  |
| 28 | `dt` | `string` |  |  |

---

## ods_log_ad_linkup_ks_stat_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_linkup_ks_stat_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 369.6K |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `task_id` | `bigint` |  |  |
| 5 | `order_id` | `bigint` |  |  |
| 6 | `consume_amount` | `bigint` |  |  |
| 7 | `consume_amount_yuan` | `double` |  |  |
| 8 | `supplement_order_id` | `bigint` |  |  |
| 9 | `star_user_id` | `bigint` |  |  |
| 10 | `star_name` | `string` |  |  |
| 11 | `account_id` | `bigint` |  |  |
| 12 | `cpa` | `int` |  |  |
| 13 | `caption` | `string` |  |  |
| 14 | `video_url` | `string` |  |  |
| 15 | `advertiser_id` | `bigint` |  |  |
| 16 | `campaign_id` | `bigint` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ods_log_ad_material_crawl_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_material_crawl_di` |
| **描述** | 广告投放素材抓取 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 14.5K |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `channel` | `string` |  |  |
| 2 | `channel_name` | `string` |  |  |
| 3 | `material_date` | `string` |  |  |
| 4 | `ip` | `string` |  |  |
| 5 | `lofter_urls` | `array<string>` |  |  |
| 6 | `channel_prefix` | `string` |  |  |
| 7 | `material_id` | `string` |  |  |
| 8 | `account_names` | `array<string>` |  |  |
| 9 | `sync_time` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ods_log_ad_new_linkup_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_new_linkup_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 66.1M |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

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
| 19 | `matchid` | `string` |  |  |
| 20 | `dt` | `string` |  | date partition field |

---

## ods_log_ad_outer_linkup_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_outer_linkup_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 937.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 937.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  |  |
| 2 | `channel` | `string` |  |  |
| 3 | `idfa` | `string` |  |  |
| 4 | `actiontype` | `string` |  |  |
| 5 | `actionparam1` | `string` |  |  |
| 6 | `actionparam2` | `string` |  |  |
| 7 | `actionparam3` | `string` |  |  |
| 8 | `actiontime` | `bigint` |  |  |
| 9 | `actionip` | `string` |  |  |
| 10 | `callback` | `string` |  |  |
| 11 | `status` | `int` |  |  |
| 12 | `createtime` | `bigint` |  |  |
| 13 | `updatetime` | `bigint` |  |  |
| 14 | `actiondevice` | `string` |  |  |
| 15 | `expiredays` | `int` |  |  |
| 16 | `oaid` | `string` |  |  |
| 17 | `platform` | `string` |  |  |
| 18 | `newuser` | `int` |  |  |
| 19 | `uniquekey` | `string` |  |  |
| 20 | `hashdip` | `string` |  |  |
| 21 | `dt` | `string` |  |  |

---

## ods_log_ad_post_detail_candy_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_post_detail_candy_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 221.8M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `createtime` | `bigint` |  |  |
| 5 | `deviceos` | `string` |  |  |
| 6 | `userid` | `bigint` |  |  |
| 7 | `deviceid` | `string` |  |  |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_post_detail_free_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_post_detail_free_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3.0G |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `createtime` | `bigint` |  |  |
| 5 | `deviceos` | `string` |  |  |
| 6 | `userid` | `bigint` |  |  |
| 7 | `deviceid` | `string` |  |  |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_post_like_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_post_like_di` |
| **描述** | 广告文赞解锁 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 619.0K |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `bucket` | `int` |  |  |
| 5 | `createtime` | `bigint` |  |  |
| 6 | `bizid` | `string` |  |  |
| 7 | `postid` | `bigint` |  |  |
| 8 | `userid` | `bigint` |  |  |
| 9 | `blogid` | `bigint` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ods_log_ad_resource_action_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_resource_action_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 624.8M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `userid` | `bigint` |  | 用户ID |
| 3 | `groupid` | `string` |  | 资源分组: A 绘旅人1 B虚拟恋人 C 是绘旅人2 |
| 4 | `deviceid` | `string` |  |  |
| 5 | `os` | `string` |  |  |
| 6 | `optime` | `bigint` |  | 操作时间 |
| 7 | `optype` | `string` |  | 操作类型:  expose 曝光 click 点击 downopen downstart downfinish installstart installfinish |
| 8 | `dt` | `string` |  |  |

---

## ods_log_ad_resource_monitor_close_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_resource_monitor_close_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 591.1K |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `groupid` | `string` |  | 资源分组: A 绘旅人 B虚拟恋人 |
| 2 | `userid` | `bigint` |  |  |
| 3 | `dt` | `string` |  |  |

---

## ods_log_ad_yidun_callback_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_yidun_callback_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 82.3M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `dataid` | `string` |  |  |
| 5 | `createtime` | `bigint` |  |  |
| 6 | `datatype` | `int` |  |  |
| 7 | `uuid` | `string` |  |  |
| 8 | `content` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_ad_yidun_request_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ad_yidun_request_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 39.6M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `dataid` | `string` |  |  |
| 5 | `createtime` | `bigint` |  |  |
| 6 | `datatype` | `int` |  |  |
| 7 | `appid` | `string` |  |  |
| 8 | `dspid` | `int` |  |  |
| 9 | `uuid` | `string` |  |  |
| 10 | `url` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_anti_addiction_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_addiction_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 67.6M |
| **是否分区表** | 是 |

### 字段详情

共 2 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 防沉迷用户Id |
| 2 | `dt` | `string` |  |  |

---

## ods_log_anti_forbid_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_forbid_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 29.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 29.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `userid` | `bigint` |  |  |
| 5 | `scene` | `string` |  |  |
| 6 | `content` | `string` |  |  |
| 7 | `antitype` | `int` |  |  |
| 8 | `risklevel` | `int` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_anti_risk_comment_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_risk_comment_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 3.2G |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `devicetype` | `string` |  |  |
| 2 | `eventid` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `ip` | `string` |  |  |
| 5 | `eventtime` | `bigint` |  |  |
| 6 | `mobile` | `string` |  |  |
| 7 | `commentid` | `bigint` |  |  |
| 8 | `postid` | `bigint` |  |  |
| 9 | `userid` | `bigint` |  |  |
| 10 | `rgname` | `string` |  |  |
| 11 | `rgrisk` | `string` |  |  |
| 12 | `rgscore` | `int` |  |  |
| 13 | `rulename` | `string` |  |  |
| 14 | `rulekey` | `string` |  |  |
| 15 | `rulescore` | `int` |  |  |
| 16 | `dt` | `string` |  |  |

---

## ods_log_anti_risk_message_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_risk_message_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 10.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 10.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `publishtime` | `bigint` |  |  |
| 3 | `ip` | `string` |  |  |
| 4 | `mobile` | `string` |  |  |
| 5 | `eventname` | `string` |  |  |
| 6 | `msgid` | `bigint` |  |  |
| 7 | `publisherblogid` | `bigint` |  |  |
| 8 | `blogid` | `bigint` |  |  |
| 9 | `otherblogid` | `bigint` |  |  |
| 10 | `rgname` | `string` |  |  |
| 11 | `rgrisk` | `string` |  |  |
| 12 | `rgscore` | `int` |  |  |
| 13 | `rulename` | `string` |  |  |
| 14 | `rulekey` | `string` |  |  |
| 15 | `rulescore` | `int` |  |  |
| 16 | `content` | `string` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ods_log_anti_risk_post_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_risk_post_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 5.2G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `devicetype` | `string` |  |  |
| 2 | `eventid` | `string` |  |  |
| 3 | `postlink` | `string` |  |  |
| 4 | `eventtime` | `bigint` |  |  |
| 5 | `mobile` | `string` |  |  |
| 6 | `eventname` | `string` |  |  |
| 7 | `userip` | `string` |  |  |
| 8 | `postid` | `bigint` |  |  |
| 9 | `optype` | `int` |  |  |
| 10 | `blogid` | `bigint` |  |  |
| 11 | `userid` | `bigint` |  |  |
| 12 | `rgname` | `string` |  |  |
| 13 | `rgrisk` | `string` |  |  |
| 14 | `rgscore` | `int` |  |  |
| 15 | `rulename` | `string` |  |  |
| 16 | `rulekey` | `string` |  |  |
| 17 | `rulescore` | `int` |  |  |
| 18 | `dt` | `string` |  |  |

---

## ods_log_anti_risk_shuare_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_risk_shuare_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 260.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 260.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 25 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `name` | `string` |  | 模型名称 |
| 2 | `guid` | `string` |  | 模型ID |
| 3 | `eventid` | `string` |  | 判断事件唯一标识 |
| 4 | `eventtime` | `bigint` |  | 请求时间 |
| 5 | `userid` | `string` |  | 用户ID |
| 6 | `targetpostid` | `string` |  | 目标文章ID |
| 7 | `targetblogid` | `string` |  | 目标文章对应博客 |
| 8 | `regip` | `string` |  | 注册IP |
| 9 | `clientip` | `string` |  | 行为IP |
| 10 | `regipuv` | `bigint` |  | 注册IP下的用户数 |
| 11 | `praisepv` | `bigint` |  | 点赞行为次数 |
| 12 | `recommendpv` | `bigint` |  | 推荐行为次数 |
| 13 | `otherpv` | `bigint` |  | 非喜欢和推荐外的行为次数 |
| 14 | `actiontype` | `string` |  | 行为类型 |
| 15 | `phone` | `string` |  | 手机号 |
| 16 | `blackphone` | `string` |  | 是否为黑产手机号 |
| 17 | `channel` | `string` |  | 渠道 |
| 18 | `ua` | `string` |  | UA信息 |
| 19 | `rgname` | `string` |  | 策略名称 |
| 20 | `rgrisk` | `string` |  | 策略判定结果 |
| 21 | `rgscore` | `int` |  | 综合评分 |
| 22 | `rulename` | `string` |  | 命中规则明细的名称 |
| 23 | `rulekey` | `string` |  | 命中规则名称 |
| 24 | `rulescore` | `int` |  | 命中规则值 |
| 25 | `dt` | `string` |  |  |

---

## ods_log_anti_spam_copy_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_anti_spam_copy_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 75.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 75.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `bizinfo` | `string` |  | from deserializer |
| 2 | `blogid` | `bigint` |  | from deserializer |
| 3 | `bussinessid` | `bigint` |  | from deserializer |
| 4 | `bussinesstype` | `bigint` |  | from deserializer |
| 5 | `category` | `string` |  | from deserializer |
| 6 | `content` | `string` |  | from deserializer |
| 7 | `createtime` | `bigint` |  | from deserializer |
| 8 | `hist` | `bigint` |  | from deserializer |
| 9 | `photocheckcount` | `bigint` |  | from deserializer |
| 10 | `policyid` | `bigint` |  | from deserializer |
| 11 | `posttoken` | `string` |  | from deserializer |
| 12 | `priority` | `bigint` |  | from deserializer |
| 13 | `textcheckcount` | `bigint` |  | from deserializer |
| 14 | `uuid` | `string` |  | from deserializer |
| 15 | `version` | `bigint` |  | from deserializer |
| 16 | `videocheckcount` | `bigint` |  | from deserializer |
| 17 | `videosnapshotphotocount` | `bigint` |  | from deserializer |
| 18 | `publishuserid` | `bigint` |  |  |
| 19 | `dt` | `string` |  |  |

---

## ods_log_antispam_brush_dispose_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_antispam_brush_dispose_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 17.9M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客Id |
| 2 | `eventtime` | `bigint` |  | 事件时间 |
| 3 | `postid` | `bigint` |  | 文章ID |
| 4 | `status` | `bigint` |  | 状态 |
| 5 | `type` | `bigint` |  | 类型 |
| 6 | `dt` | `string` |  |  |

---

## ods_log_antispam_callback_article_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_antispam_callback_article_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 7.5G |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `postid` | `bigint` |  |  |
| 3 | `posttype` | `bigint` |  |  |
| 4 | `label` | `string` |  |  |
| 5 | `level` | `bigint` |  |  |
| 6 | `createtime` | `bigint` |  |  |
| 7 | `operatetype` | `bigint` |  |  |
| 8 | `operator` | `string` |  |  |
| 9 | `allowview` | `bigint` |  |  |
| 10 | `hist` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_antispam_callback_comment_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_antispam_callback_comment_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 7.7G |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `responseid` | `bigint` |  |  |
| 2 | `blogid` | `bigint` |  |  |
| 3 | `postid` | `bigint` |  |  |
| 4 | `label` | `string` |  |  |
| 5 | `level` | `bigint` |  |  |
| 6 | `createtime` | `bigint` |  |  |
| 7 | `operatetype` | `bigint` |  |  |
| 8 | `hist` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_antispam_user_report_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_antispam_user_report_di` |
| **描述** | 用户投诉上报 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 114.4M |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `datatype` | `int` |  |  |
| 3 | `reporttype` | `int` |  |  |
| 4 | `reason` | `string` |  | 投诉原因 |
| 5 | `informantid` | `string` |  | 投诉者用户id |
| 6 | `informantname` | `string` |  | 投诉者名称 |
| 7 | `defendantid` | `string` |  | 被投诉方id |
| 8 | `defendantname` | `string` |  | 被投诉内容名称 |
| 9 | `content` | `string` |  | 被投诉内容 |
| 10 | `blogid` | `bigint` |  | 博客id |
| 11 | `postversion` | `string` |  | 发布版本 |
| 12 | `deviceid` | `string` |  | 设备Id |
| 13 | `devicetype` | `string` |  | 设备类型 |
| 14 | `url` | `string` |  | 投诉内容链接 |
| 15 | `logtime` | `string` |  | 日志采集时间 |
| 16 | `ip` | `string` |  | 访问ip |
| 17 | `dt` | `string` |  |  |

---

## ods_log_antispam_webapp_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_antispam_webapp_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 554.4M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `method` | `string` |  |  |
| 2 | `level` | `string` |  |  |
| 3 | `machine` | `string` |  |  |
| 4 | `hint` | `string` |  |  |
| 5 | `callback` | `string` |  |  |
| 6 | `label` | `string` |  |  |
| 7 | `postid` | `string` |  |  |
| 8 | `tag` | `string` |  |  |
| 9 | `blogid` | `string` |  |  |
| 10 | `operator` | `string` |  |  |
| 11 | `responseid` | `string` |  |  |
| 12 | `dt` | `string` |  |  |

---

## ods_log_appregister_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_appregister_di` |
| **描述** | lofter tomcat appregister |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 22.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 22.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `source` | `string` |  | from deserializer |
| 13 | `dadeviceudid` | `string` |  | from deserializer |
| 14 | `securityinfo` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_artificial_import_tag_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_artificial_import_tag_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 9.2K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `operator` | `string` |  | 人工操作者 |
| 2 | `createtime` | `bigint` |  | 操作时间 |
| 3 | `tag` | `string` |  | 标签 |
| 4 | `optype` | `int` |  | 操作类型 |
| 5 | `dt` | `string` |  |  |

---

## ods_log_authenticate_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_authenticate_di` |
| **描述** | lofter tomcat authenticate |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.0G |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `cookie` | `string` |  | from deserializer |
| 13 | `jsessionid` | `string` |  | from deserializer |
| 14 | `source` | `string` |  | from deserializer |
| 15 | `followids` | `string` |  | from deserializer |
| 16 | `nextcilck` | `string` |  | from deserializer |
| 17 | `dt` | `string` |  |  |

---

## ods_log_behavior_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_behavior_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 17.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 17.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `actiontype` | `string` |  |  |
| 2 | `eventid` | `string` |  |  |
| 3 | `itemid` | `string` |  |  |
| 4 | `itemtype` | `string` |  |  |
| 5 | `userid` | `string` |  |  |
| 6 | `type` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_bindphone_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_bindphone_di` |
| **描述** | lofter tomcat bindphone log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 386.7M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `phonenumber` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_bookstore_coupon_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_bookstore_coupon_di` |
| **描述** | lofter backend bookstore_coupon log |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 8.3M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `logtype` | `string` |  | 日志类型 |
| 3 | `deviceos` | `string` |  | 操作系统 |
| 4 | `deviceid` | `string` |  | 设备Id |
| 5 | `itemid` | `string` |  | itemID |
| 6 | `itemtype` | `string` |  | 类型 |
| 7 | `type` | `bigint` |  | AB实验分组类型 |
| 8 | `createtime` | `bigint` |  | 创建事件 |
| 9 | `logtime` | `string` |  | 日志采集时间 |
| 10 | `dt` | `string` |  |  |

---

## ods_log_browsepage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_browsepage_di` |
| **描述** | lofter browse page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 12.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 12.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_bund_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_bund_di` |
| **描述** | lofter tomcat bund log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 731.7M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `bundaccount` | `string` |  | from deserializer |
| 11 | `accounttype` | `string` |  | from deserializer |
| 12 | `devicetype` | `string` |  | from deserializer |
| 13 | `appversion` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_log_cheat_header_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_cheat_header_info_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 7.1K |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `content-length` | `string` |  |  |
| 2 | `x-forwarded-proto` | `string` |  |  |
| 3 | `secruityinfo` | `string` |  |  |
| 4 | `ip` | `string` |  |  |
| 5 | `x-forwarded-for` | `string` |  |  |
| 6 | `deviceid` | `string` |  |  |
| 7 | `uri` | `string` |  |  |
| 8 | `userid` | `bigint` |  |  |
| 9 | `version` | `string` |  |  |
| 10 | `x-from-ip` | `string` |  |  |
| 11 | `x-real-ip` | `string` |  |  |
| 12 | `authorization` | `string` |  |  |
| 13 | `market` | `string` |  |  |
| 14 | `actiontype` | `string` |  |  |
| 15 | `signature-url` | `string` |  |  |
| 16 | `dadeviceid` | `string` |  |  |
| 17 | `host` | `string` |  |  |
| 18 | `content-type` | `string` |  |  |
| 19 | `accept-encoding` | `string` |  |  |
| 20 | `user-agent` | `string` |  |  |
| 21 | `dt` | `string` |  |  |

---

## ods_log_check_public_post_md5_consumer_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_check_public_post_md5_consumer_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 50.4K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志采集时间 |
| 3 | `loghost` | `string` |  | 日志采集主机 |
| 4 | `result` | `boolean` |  | 结果 |
| 5 | `postid` | `bigint` |  | 文章ID |
| 6 | `blogid` | `bigint` |  | 博客ID |
| 7 | `dt` | `string` |  |  |

---

## ods_log_commend_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_commend_di` |
| **描述** | lofter tomcat commend log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 123.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 123.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `commendid` | `string` |  | from deserializer |
| 11 | `postid` | `string` |  | from deserializer |
| 12 | `uidid` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `posttype` | `string` |  | from deserializer |
| 16 | `logintype` | `string` |  |  |
| 17 | `replyl1commentid` | `bigint` |  |  |
| 18 | `dt` | `string` |  |  |

---

## ods_log_common_abtest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_common_abtest_di` |
| **描述** | lofter backend commonAbTest log |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 250.4M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `abtestid` | `bigint` |  | ab实验ID |
| 2 | `createtime` | `bigint` |  | 创建事件 |
| 3 | `logtime` | `string` |  | 日志采集时间 |
| 4 | `logtype` | `string` |  | 日志类型 |
| 5 | `userid` | `bigint` |  | 用户ID |
| 6 | `deviceos` | `string` |  |  |
| 7 | `type` | `bigint` |  | 类型 |
| 8 | `dt` | `string` |  |  |

---

## ods_log_content_acw_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_content_acw_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 6.3G |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `actpwd` | `string` |  | 活动口令 |
| 2 | `channel` | `string` |  | 渠道 |
| 3 | `type` | `bigint` |  | 口令类型，0-搜索口令，1-剪切口令, 2-搜索置顶口令 |
| 4 | `customid` | `string` |  | 服务端customid |
| 5 | `deviceuuid` | `string` |  | sdk 设备ID |
| 6 | `isnew` | `boolean` |  | 是否新增设备 |
| 7 | `userid` | `bigint` |  | 用户ID |
| 8 | `time` | `bigint` |  | 操作时间 |
| 9 | `starttime` | `bigint` |  | 口令开始时间 |
| 10 | `endtime` | `bigint` |  | 口令结束时间 |
| 11 | `link` | `string` |  | 口令链接 |
| 12 | `contenttype` | `bigint` |  | 口令内容类型,0:不填写;1:图片;2:文本;3:视频 |
| 13 | `tag` | `string` |  | 口令内容附加的标签信息 |
| 14 | `miniid` | `int` |  | 小程序id |
| 15 | `settlementtype` | `string` |  |  |
| 16 | `dt` | `string` |  |  |

---

## ods_log_content_acw_kafka_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_content_acw_kafka_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 736.1M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `actpwd` | `string` |  | 活动口令 |
| 2 | `channel` | `string` |  | 渠道 |
| 3 | `type` | `bigint` |  | 口令类型，0-搜索口令，1-剪切口令 |
| 4 | `customid` | `string` |  | 服务端customid |
| 5 | `deviceuuid` | `string` |  | sdk 设备ID |
| 6 | `isnew` | `boolean` |  | 是否新增设备 |
| 7 | `userid` | `bigint` |  | 用户ID |
| 8 | `time` | `bigint` |  | 操作时间 |
| 9 | `starttime` | `bigint` |  | 口令开始时间 |
| 10 | `endtime` | `bigint` |  | 口令结束时间 |
| 11 | `link` | `string` |  | 口令链接 |
| 12 | `contenttype` | `bigint` |  | 口令内容类型,0:不填写;1:图片;2:文本;3:视频 |
| 13 | `tag` | `string` |  | 口令内容附加的标签信息 |
| 14 | `miniid` | `int` |  | 小程序id |
| 15 | `settlementtype` | `string` |  |  |
| 16 | `bookstoreparent` | `boolean` |  | 是否书城母链 |
| 17 | `bookparentlink` | `string` |  | 书城母链permalink |
| 18 | `dt` | `string` |  |  |

---

## ods_log_content_incantation_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_content_incantation_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 802.8M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `contentid` | `bigint` |  | 内容Id |
| 2 | `contentlink` | `string` |  | 内容链接 |
| 3 | `contenttype` | `bigint` |  | 内容类型,1:推集,2:粮单,3:合集,4:文章,5:拉新,6:最强学者 |
| 4 | `contentuserid` | `bigint` |  | 内容拥有者ID |
| 5 | `customid` | `string` |  | 后端设备Id |
| 6 | `deviceuuid` | `string` |  | 设备ID |
| 7 | `incantation` | `string` |  | 口令链接 |
| 8 | `isnew` | `boolean` |  | 是否新增设备 |
| 9 | `opentime` | `bigint` |  | 口令打开时间 |
| 10 | `openuserid` | `bigint` |  | 打开口令的用户Id |
| 11 | `shareuserid` | `bigint` |  | 分享者ID |
| 12 | `dt` | `string` |  |  |

---

## ods_log_coupon_trade_acquisition_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_coupon_trade_acquisition_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 2.6M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `createtime` | `bigint` |  |  |
| 5 | `coupon_flg` | `int` |  |  |
| 6 | `actpwd` | `string` |  |  |
| 7 | `userid` | `bigint` |  |  |
| 8 | `deviceid` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_creator_stimulus_pm_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_creator_stimulus_pm_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 325.1M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 私信用户ID |
| 2 | `time` | `bigint` |  | 私信时间 |
| 3 | `stimulatetype` | `string` |  | 私信激励类型 first_interaction 首次互动 first_post 首次发文 creator_callback 45天未创作召回 traffic_aid_creator 回流创作者流量扶持通知 traffic_aid_feedback 回流创作者流量扶持反馈 callback_post 回流创作者首次发文评论 |
| 4 | `data` | `map<string, string>` |  |  |
| 5 | `dt` | `string` |  | date partition field |

---

## ods_log_darenrecpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_darenrecpage_di` |
| **描述** | lofter daren rec page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 821.5M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_domaindarenpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_domaindarenpage_di` |
| **描述** | lofter domain daren page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.4G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_domainset_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_domainset_di` |
| **描述** | lofter tomcat domainset |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `cookie` | `string` |  | from deserializer |
| 13 | `jsessionid` | `string` |  | from deserializer |
| 14 | `source` | `string` |  | from deserializer |
| 15 | `domainids` | `string` |  | from deserializer |
| 16 | `nextcilck` | `string` |  | from deserializer |
| 17 | `backid` | `string` |  | from deserializer |
| 18 | `dt` | `string` |  |  |

---

## ods_log_double11neteaseprofile_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_double11neteaseprofile_di` |
| **描述** | lofter tomcat lofter_ds_double11neteaseprofile |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `amount` | `string` |  | from deserializer |
| 13 | `welfare` | `string` |  | from deserializer |
| 14 | `buyerid` | `string` |  | from deserializer |
| 15 | `tradeid` | `string` |  | from deserializer |
| 16 | `ordernum` | `string` |  | from deserializer |
| 17 | `orderids` | `string` |  | from deserializer |
| 18 | `dt` | `string` |  |  |

---

## ods_log_dstr_flow_post_push_config_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_dstr_flow_post_push_config_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 8.0M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `flowtaskid` | `bigint` |  |  |
| 2 | `flowtasktype` | `int` |  |  |
| 3 | `itemid` | `string` |  |  |
| 4 | `itemtype` | `string` |  |  |
| 5 | `blogid` | `bigint` |  |  |
| 6 | `postid` | `bigint` |  |  |
| 7 | `msgtype` | `int` |  |  |
| 8 | `msg` | `string` |  |  |
| 9 | `exposure` | `bigint` |  |  |
| 10 | `hot` | `bigint` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_dynamicpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_dynamicpage_di` |
| **描述** | lofter dynamic page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 11.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 11.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_exposinterestman_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_exposinterestman_di` |
| **描述** | lofter tomcat exposinterestman log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `createtime` | `string` |  | from deserializer |
| 2 | `postids` | `string` |  | from deserializer |
| 3 | `blogid` | `string` |  | from deserializer |
| 4 | `domainid` | `string` |  | from deserializer |
| 5 | `accountid` | `string` |  | from deserializer |
| 6 | `dt` | `string` |  |  |

---

## ods_log_findpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_findpage_di` |
| **描述** | lofter find page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 29.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 29.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_flpayfailed_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_flpayfailed_di` |
| **描述** | lofter tomcat flpayfailed log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 76.1K |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `string` |  |  |
| 2 | `orderamounts` | `string` |  |  |
| 3 | `orderfrom` | `string` |  |  |
| 4 | `userid` | `string` |  |  |
| 5 | `action` | `string` |  |  |
| 6 | `type` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_flpaysucess_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_flpaysucess_di` |
| **描述** | lofter tomcat flpaysucess log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 102.3M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `orderid` | `string` |  |  |
| 2 | `orderamounts` | `string` |  |  |
| 3 | `orderfrom` | `string` |  |  |
| 4 | `userid` | `string` |  |  |
| 5 | `action` | `string` |  |  |
| 6 | `type` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_follow_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_follow_di` |
| **描述** | lofter tomcat follow log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 75.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 75.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `uidid` | `string` |  | from deserializer |
| 11 | `domainid` | `string` |  | from deserializer |
| 12 | `domainname` | `string` |  | from deserializer |
| 13 | `followsource` | `string` |  | from deserializer |
| 14 | `daren` | `string` |  | from deserializer |
| 15 | `devicetype` | `string` |  | from deserializer |
| 16 | `appversion` | `string` |  | from deserializer |
| 17 | `logintype` | `string` |  |  |
| 18 | `itemid` | `string` |  | 关注博客id |
| 19 | `dt` | `string` |  |  |

---

## ods_log_followcancel_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_followcancel_di` |
| **描述** | lofter tomcat followcancel log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 17.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 17.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `uidid` | `string` |  | from deserializer |
| 11 | `devicetype` | `string` |  | from deserializer |
| 12 | `appversion` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_followpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_followpage_di` |
| **描述** | lofter follow page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_forbid_phone_reg_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_forbid_phone_reg_di` |
| **描述** | 安全整改封禁手机注册信息 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 310.3K |
| **是否分区表** | 是 |

### 字段详情

共 4 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `phone` | `string` |  | 手机号 |
| 2 | `ip` | `string` |  | 注册ip |
| 3 | `time` | `bigint` |  |  |
| 4 | `dt` | `string` |  |  |

---

## ods_log_homepage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_homepage_di` |
| **描述** | lofter home page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 49.8G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 49.8G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_hongbao_data_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_hongbao_data_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志采集时间 |
| 3 | `loghost` | `string` |  | 日志采集主机 |
| 4 | `configcode` | `string` |  |  |
| 5 | `rewardcode` | `string` |  |  |
| 6 | `time` | `bigint` |  |  |
| 7 | `userid` | `bigint` |  |  |
| 8 | `deviceid` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_hongbao_token_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_hongbao_token_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志采集时间 |
| 3 | `loghost` | `string` |  | 日志采集主机 |
| 4 | `configcode` | `string` |  |  |
| 5 | `rewardcode` | `string` |  |  |
| 6 | `time` | `bigint` |  |  |
| 7 | `userid` | `bigint` |  |  |
| 8 | `deviceid` | `string` |  |  |
| 9 | `token` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ods_log_ios_coin_sms_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_ios_coin_sms_di` |
| **描述** | lofter ios CoinSms log |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 145.0M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志采集时间 |
| 3 | `userid` | `bigint` |  | 用户ID |
| 4 | `phone` | `string` |  |  |
| 5 | `createtime` | `bigint` |  | 创建时间 |
| 6 | `type` | `string` |  | 类型 |
| 7 | `dt` | `string` |  |  |

---

## ods_log_labeldarenpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_labeldarenpage_di` |
| **描述** | lofter label daren page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 8.9G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_labeldetailpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_labeldetailpage_di` |
| **描述** | lofter label detail page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2323.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2323.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 22 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `url` | `string` |  | from deserializer |
| 13 | `labelname` | `string` |  | from deserializer |
| 14 | `tab` | `string` |  | from deserializer |
| 15 | `sourceweb` | `string` |  | from deserializer |
| 16 | `sourcemobile` | `string` |  | from deserializer |
| 17 | `istopic` | `string` |  | from deserializer |
| 18 | `labeltype` | `string` |  | from deserializer |
| 19 | `referer` | `string` |  | from deserializer |
| 20 | `parameter` | `string` |  | from deserializer |
| 21 | `logintype` | `string` |  |  |
| 22 | `dt` | `string` |  |  |

---

## ods_log_labelpagepc_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_labelpagepc_di` |
| **描述** | lofter label page pc |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 17.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 17.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_labelrecommend_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_labelrecommend_di` |
| **描述** | lofter tomcat labelrecommend log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `recommendway` | `string` |  | from deserializer |
| 11 | `labelid` | `string` |  | from deserializer |
| 12 | `labelname` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_larecpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_larecpage_di` |
| **描述** | lofter la rec page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 129.3M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_launchtopic_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_launchtopic_di` |
| **描述** | lofter tomcat launchtopic log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 10.5M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `topicid` | `string` |  | from deserializer |
| 11 | `labelid` | `string` |  | from deserializer |
| 12 | `labelname` | `string` |  | from deserializer |
| 13 | `topiccontent` | `string` |  | from deserializer |
| 14 | `devicetype` | `string` |  | from deserializer |
| 15 | `appversion` | `string` |  | from deserializer |
| 16 | `dt` | `string` |  |  |

---

## ods_log_login401_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_login401_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 43.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 43.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `normalizedtoken` | `map<string, string>` |  |  |
| 5 | `applogintoken` | `map<string, string>` |  |  |
| 6 | `weblogintoken` | `map<string, string>` |  |  |
| 7 | `superlogintoken` | `map<string, string>` |  |  |
| 8 | `producttype` | `string` |  |  |
| 9 | `userip` | `string` |  |  |
| 10 | `url` | `string` |  |  |
| 11 | `type` | `string` |  |  |
| 12 | `dt` | `string` |  |  |

---

## ods_log_message_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_message_di` |
| **描述** | lofter tomcat message log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 216.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 216.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `messageid` | `string` |  | from deserializer |
| 11 | `uidid` | `string` |  | from deserializer |
| 12 | `devicetype` | `string` |  | from deserializer |
| 13 | `appversion` | `string` |  | from deserializer |
| 14 | `logintype` | `string` |  |  |
| 15 | `userid` | `string` |  |  |
| 16 | `dt` | `string` |  |  |

---

## ods_log_monitor_crab_suspect_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_monitor_crab_suspect_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2.9G |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户ID |
| 2 | `userip` | `string` |  | 用户IP |
| 3 | `offset` | `bigint` |  | 翻页值offset |
| 4 | `platform` | `string` |  | 平台 |
| 5 | `requesttime` | `bigint` |  | 记录创建时间 |
| 6 | `target` | `string` |  | 目标内容tag页:标签,用户个人主页:blogid,搜索:搜索词 |
| 7 | `uri` | `string` |  | 请求的uri |
| 8 | `dt` | `string` |  |  |

---

## ods_log_new_outerlinkup_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_new_outerlinkup_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 7.1G |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `appid` | `string` |  |  |
| 2 | `channel` | `string` |  |  |
| 3 | `idfa` | `string` |  |  |
| 4 | `actiontype` | `string` |  |  |
| 5 | `actionparam1` | `string` |  |  |
| 6 | `actionparam2` | `string` |  |  |
| 7 | `actionparam3` | `string` |  |  |
| 8 | `actiontime` | `string` |  |  |
| 9 | `actionip` | `string` |  |  |
| 10 | `callback` | `string` |  |  |
| 11 | `status` | `string` |  |  |
| 12 | `createtime` | `string` |  |  |
| 13 | `updatetime` | `string` |  |  |
| 14 | `actiondevice` | `string` |  |  |
| 15 | `expiredays` | `string` |  |  |
| 16 | `oaid` | `string` |  |  |
| 17 | `platform` | `string` |  |  |
| 18 | `dt` | `string` |  | date partition field |

---

## ods_log_nickname_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_nickname_di` |
| **描述** | lofter tomcat nickname |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 2.7G |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `cookie` | `string` |  | from deserializer |
| 13 | `jsessionid` | `string` |  | from deserializer |
| 14 | `source` | `string` |  | from deserializer |
| 15 | `nextcilck` | `string` |  | from deserializer |
| 16 | `backid` | `string` |  | from deserializer |
| 17 | `dt` | `string` |  |  |

---

## ods_log_participatetopic_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_participatetopic_di` |
| **描述** | lofter tomcat participatetopic log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 911.7M |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `topicid` | `string` |  | from deserializer |
| 11 | `postid` | `string` |  | from deserializer |
| 12 | `labelid` | `string` |  | from deserializer |
| 13 | `labelname` | `string` |  | from deserializer |
| 14 | `devicetype` | `string` |  | from deserializer |
| 15 | `appversion` | `string` |  | from deserializer |
| 16 | `dt` | `string` |  |  |

---

## ods_log_personalpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_personalpage_di` |
| **描述** | lofter tomcat  personalpage |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 72.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 72.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `url` | `string` |  | from deserializer |
| 11 | `blogid` | `string` |  | from deserializer |
| 12 | `dt` | `string` |  |  |

---

## ods_log_popupabtest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_popupabtest_di` |
| **描述** | lofter backend popUpAbTest log |
| **Owner** | bdms_wangjun02 |
| **表类型** | external |
| **表大小** | 79.9M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `abtestid` | `bigint` |  | ab实验ID |
| 2 | `createtime` | `bigint` |  | 创建事件 |
| 3 | `logtime` | `string` |  | 日志采集时间 |
| 4 | `logtype` | `string` |  | 日志类型 |
| 5 | `packageid` | `string` |  |  |
| 6 | `subjectid` | `bigint` |  | 主题id |
| 7 | `type` | `bigint` |  | 类型 |
| 8 | `userid` | `bigint` |  | 用户ID |
| 9 | `dt` | `string` |  |  |

---

## ods_log_post_audit_chain_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_post_audit_chain_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 30.4M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间，ISO-8601 带毫秒，如 2026-06-12T14:30:20.902 |
| 3 | `loghost` | `string` |  | 产生日志的主机名/容器名 |
| 4 | `table` | `string` |  | 操作的表名，如 Post |
| 5 | `op` | `string` |  | 操作类型，如 update/insert/delete |
| 6 | `postid` | `bigint` |  | 帖子ID |
| 7 | `blogid` | `bigint` |  | 博客ID |
| 8 | `scene` | `string` |  | 场景，如 editPost |
| 9 | `source` | `string` |  | 调用来源链路（类#方法:行号） |
| 10 | `detail` | `string` |  | 操作详情 |
| 11 | `thread` | `string` |  | 执行线程名 |
| 12 | `time` | `bigint` |  | 日志时间戳（毫秒） |
| 13 | `dt` | `string` |  |  |

---

## ods_log_post_change_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_post_change_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 209.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 209.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `action` | `string` |  | from deserializer |
| 2 | `blogid` | `bigint` |  | from deserializer |
| 3 | `extinfo` | `string` |  | from deserializer |
| 4 | `modifier` | `string` |  | from deserializer |
| 5 | `modifytime` | `bigint` |  | from deserializer |
| 6 | `postid` | `bigint` |  | from deserializer |
| 7 | `publishtime` | `bigint` |  | from deserializer |
| 8 | `dt` | `string` |  |  |

---

## ods_log_praise_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_praise_di` |
| **描述** | lofter tomcat praise log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 633.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 633.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `praiseid` | `string` |  | from deserializer |
| 11 | `postid` | `string` |  | from deserializer |
| 12 | `uidid` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `posttype` | `string` |  | from deserializer |
| 16 | `logintype` | `string` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ods_log_praisecancel_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_praisecancel_di` |
| **描述** | lofter tomcat praisecancel |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 86.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 86.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `praisecancelid` | `string` |  | from deserializer |
| 11 | `postid` | `string` |  | from deserializer |
| 12 | `uidid` | `string` |  | from deserializer |
| 13 | `posttype` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_log_publishpost_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_publishpost_di` |
| **描述** | lofter tomcat publishpost log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 92.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 92.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 27 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `postid` | `string` |  | from deserializer |
| 11 | `contenttype` | `string` |  | from deserializer |
| 12 | `labelid` | `string` |  | from deserializer |
| 13 | `labelname` | `string` |  | from deserializer |
| 14 | `labeltype` | `string` |  | from deserializer |
| 15 | `hivepaster` | `string` |  | from deserializer |
| 16 | `pasterid` | `string` |  | from deserializer |
| 17 | `filterid` | `string` |  | from deserializer |
| 18 | `modelid` | `string` |  | from deserializer |
| 19 | `devicetype` | `string` |  | from deserializer |
| 20 | `appversion` | `string` |  | from deserializer |
| 21 | `photoinfo` | `string` |  | from deserializer |
| 22 | `posttype` | `string` |  | from deserializer |
| 23 | `movefrom` | `string` |  | from deserializer |
| 24 | `labelnormalname` | `string` |  | from deserializer |
| 25 | `videotype` | `string` |  | from deserializer |
| 26 | `logintype` | `string` |  |  |
| 27 | `dt` | `string` |  |  |

---

## ods_log_push_after_accord_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_push_after_accord_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 22.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 22.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `result` | `string` |  |  |
| 5 | `pushchannel` | `int` |  |  |
| 6 | `stage` | `string` |  |  |
| 7 | `messageid` | `string` |  |  |
| 8 | `pushgroupid` | `string` |  |  |
| 9 | `taskid` | `bigint` |  |  |
| 10 | `token` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_push_before_accord_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_push_before_accord_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 16.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 16.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `pushchannel` | `int` |  |  |
| 5 | `stage` | `string` |  |  |
| 6 | `pushgroupid` | `string` |  |  |
| 7 | `taskid` | `bigint` |  |  |
| 8 | `token` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_push_callback_details_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_push_callback_details_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 291.2M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `callbacktime` | `bigint` |  |  |
| 5 | `messageid` | `string` |  |  |
| 6 | `channeltype` | `int` |  |  |
| 7 | `eventtype` | `string` |  |  |
| 8 | `pushgroupid` | `string` |  |  |
| 9 | `taskid` | `string` |  |  |
| 10 | `token` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_push_callback_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_push_callback_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 12.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 12.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `acktime` | `bigint` |  |  |
| 5 | `acktypedescription` | `string` |  |  |
| 6 | `channeltype` | `int` |  | 渠道类型： 0 杭研推送 1小米通知栏 2华为 3小米透传 4 oppo通知栏 6魅族通知栏 7 文漫websocket 8vivo 10小米ios推送 11华为新版本 |
| 7 | `acktype` | `string` |  |  |
| 8 | `pushgroupid` | `string` |  |  |
| 9 | `taskid` | `string` |  |  |
| 10 | `token` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_push_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_push_log_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 15.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 15.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `bigint` |  | 发送时间 |
| 2 | `pushgroupid` | `string` |  | push groupid |
| 3 | `day` | `bigint` |  | 日期 格式yyyyMMdd |
| 4 | `userid` | `bigint` |  | 用户id |
| 5 | `dt` | `string` |  | 日期 |

---

## ods_log_pushnotice_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_pushnotice_di` |
| **描述** | lofter tomcat pushnotice log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 3.4G |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `noticenumber` | `string` |  | from deserializer |
| 13 | `uidid` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_log_pve_ai_req_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_pve_ai_req_log_di` |
| **描述** | lofter backend pve AI_REQ_LOG log |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1130.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 1130.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间 |
| 3 | `loghost` | `string` |  | 日志主机 |
| 4 | `costtime` | `bigint` |  | 耗时 |
| 5 | `dialogueid` | `bigint` |  | 对话Id |
| 6 | `usercontent` | `string` |  | 用户发送的内容 |
| 7 | `userid` | `bigint` |  | 用户Id |
| 8 | `aisource` | `string` |  | ai模型 |
| 9 | `roleid` | `bigint` |  | 角色Id |
| 10 | `groupid` | `bigint` |  | 聊天室Id |
| 11 | `airequest` | `string` |  | 请求上下文 |
| 12 | `airesponse` | `string` |  | ai响应 |
| 13 | `type` | `int` |  | 类型 |
| 14 | `reqstatus` | `int` |  | 请求状态: 0成功，1失败 |
| 15 | `traceid` | `string` |  | 路径ID |
| 16 | `dt` | `string` |  |  |

---

## ods_log_pve_log_antispam_audit_trace_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_pve_log_antispam_audit_trace_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 461.6M |
| **是否分区表** | 是 |

### 字段详情

共 20 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间，如 2026-05-22T15:48:50.424 |
| 3 | `loghost` | `string` |  | 产生日志的主机名/容器名 |
| 4 | `userid` | `bigint` |  | 用户ID |
| 5 | `usercode` | `string` |  | 用户编码（字符串，避免大整数精度丢失） |
| 6 | `roleid` | `string` |  | 角色ID |
| 7 | `scene` | `string` |  | 场景编码，如 ultraRomanticOut |
| 8 | `scenedesc` | `string` |  | 场景描述，如 主聊天AI输出 |
| 9 | `businessid` | `string` |  | 业务ID |
| 10 | `content` | `string` |  | 审核文本内容 |
| 11 | `antitype` | `int` |  | 反垃圾类型 |
| 12 | `auditresult` | `int` |  | 审核结果：0=通过 等 |
| 13 | `dataid` | `string` |  | 数据唯一ID（UUID） |
| 14 | `hitnonage` | `boolean` |  | 是否命中未成年 |
| 15 | `hitsuicidetip` | `boolean` |  | 是否命中自杀提示 |
| 16 | `hitsuicideescalate` | `boolean` |  | 是否命中自杀升级 |
| 17 | `rt` | `int` |  | 审核耗时(ms) |
| 18 | `requesttime` | `string` |  | 请求时间，毫秒时间戳字符串，如 1779436130311 |
| 19 | `channel` | `string` |  | 渠道，如 lofter |
| 20 | `dt` | `string` |  |  |

---

## ods_log_pve_model_match_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_pve_model_match_log_di` |
| **描述** | lofter backend pve MODEL_MATCH_LOG log |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 122.9M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间 |
| 3 | `loghost` | `string` |  | 日志主机 |
| 4 | `strategyupdatetime` | `string` |  | 策略更新时间 |
| 5 | `modelscene` | `bigint` |  | 模型场景 |
| 6 | `roleid` | `bigint` |  | 角色Id |
| 7 | `strategychatrange` | `string` |  | 策略聊天范围 |
| 8 | `modeltype` | `bigint` |  | 模型类型 |
| 9 | `userid` | `bigint` |  | 用户Id |
| 10 | `version` | `string` |  | 模型版本 |
| 11 | `versionmd5` | `string` |  | 模型MD5 |
| 12 | `sortno` | `bigint` |  | 排序字段 |
| 13 | `modelname` | `string` |  | 模型名称 |
| 14 | `createtime` | `bigint` |  | 创建时间 |
| 15 | `strategyid` | `bigint` |  | 策略Id |
| 16 | `id` | `bigint` |  | id |
| 17 | `strategyflowrange` | `string` |  | 策略范围 |
| 18 | `bigmodel` | `boolean` |  | 大模型 |
| 19 | `usergroup` | `bigint` |  | 用户组 |
| 20 | `abgroup` | `string` |  | ab实验分组 |
| 21 | `dt` | `string` |  |  |

---

## ods_log_pve_monitor_access_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_pve_monitor_access_di` |
| **描述** | lofter backend PVE_MONITOR_ACCESS log |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 369.6M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `string` |  | 用户Id |
| 2 | `ip` | `string` |  | IP |
| 3 | `referer` | `string` |  | referer |
| 4 | `path` | `string` |  | 路径 |
| 5 | `logtime` | `string` |  | 日志时间 |
| 6 | `dt` | `string` |  |  |

---

## ods_log_radar_risk_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_radar_risk_di` |
| **描述** | 后台风控引擎日志明细表 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 40.4M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间, 格式: yyyy-MM-ddTHH:mm:ss.SSS |
| 3 | `loghost` | `string` |  | 日志来源主机名 |
| 4 | `riskreq` | `row<string,string,boolean,row<string,string,string,string,string,string,string,string,string,string,bigint,string,string,string>('eventid','product','optype','source','ua','userid','deviceid','targetpostid','xdevice','clientip','eventtime','exempt','targetblogid','androidid')>('reqId','guid','async','payload')` |  | 风控请求信息 |
| 5 | `antifraudresult` | `row<map<string,double>,map<string,string>,map<string,row<string,string,double,map<string,row<string,double,string>('key','value','desc')>>('strategygroupname','risk','score','hitdetails')>,row<int,int,int>('adaptations','activations','abstractions'),boolean,string,int,map<string,string>>('abstractions','adaptations','activations','respTimes','success','msg','code','data')` |  | 反欺诈结果信息 |
| 6 | `dt` | `string` |  | 日期分区, 格式: yyyy-MM-dd |

---

## ods_log_rec_scene_ctr_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_rec_scene_ctr_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 141.6K |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `scene` | `string` |  | 推荐场景： feed_rec tag_rec related_item |
| 2 | `time` | `bigint` |  | 10分钟滚动窗口开始时间 |
| 3 | `exp` | `bigint` |  | 点击数 |
| 4 | `clk` | `bigint` |  | 曝光数 |
| 5 | `dt` | `string` |  |  |

---

## ods_log_rec_text_identification_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_rec_text_identification_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.8G |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `source` | `string` |  | 模型任务类型 |
| 2 | `item_id` | `string` |  | 物料Id |
| 3 | `preds` | `string` |  | 预测结果 |
| 4 | `result` | `string` |  | 模型回复结果 |
| 5 | `createtime` | `bigint` |  | 日志创建时间 |
| 6 | `dt` | `string` |  |  |

---

## ods_log_rec_text_identification_test_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_rec_text_identification_test_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 121.0M |
| **是否分区表** | 是 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `source` | `string` |  | 模型任务类型 |
| 2 | `item_id` | `string` |  | 物料Id |
| 3 | `preds` | `string` |  | 预测结果 |
| 4 | `result` | `string` |  | 模型回复结果 |
| 5 | `createtime` | `bigint` |  | 日志创建时间 |
| 6 | `dt` | `string` |  |  |

---

## ods_log_recommend_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_recommend_di` |
| **描述** | lofter tomcat recommend log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 99.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 99.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `recommendid` | `string` |  | from deserializer |
| 11 | `postid` | `string` |  | from deserializer |
| 12 | `uidid` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `posttype` | `string` |  | from deserializer |
| 16 | `logintype` | `string` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ods_log_register_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_register_di` |
| **描述** | lofter tomcat register log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 7.2G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `source` | `string` |  | from deserializer |
| 11 | `channelid` | `string` |  | from deserializer |
| 12 | `devicetype` | `string` |  | from deserializer |
| 13 | `appversion` | `string` |  | from deserializer |
| 14 | `logintype` | `string` |  |  |
| 15 | `dt` | `string` |  |  |

---

## ods_log_registerback_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_registerback_di` |
| **描述** | lofter tomcat registerback |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 504.0M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `backtype` | `string` |  | from deserializer |
| 13 | `id` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_log_reproduce_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_reproduce_di` |
| **描述** | lofter tomcat lofter_ds_reproduce |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.0G |
| **是否分区表** | 是 |

### 字段详情

共 17 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `praiseid` | `string` |  | from deserializer |
| 13 | `postid` | `string` |  | from deserializer |
| 14 | `posttype` | `string` |  | from deserializer |
| 15 | `uidid` | `string` |  | from deserializer |
| 16 | `logintype` | `string` |  |  |
| 17 | `dt` | `string` |  |  |

---

## ods_log_risk_api_access_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_api_access_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 653.5M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventenum` | `string` |  |  |
| 2 | `message` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## ods_log_risk_operation_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_operation_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.9M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `eventtime` | `bigint` |  |  |
| 5 | `postid` | `bigint` |  |  |
| 6 | `blogid` | `bigint` |  |  |
| 7 | `version` | `int` |  |  |
| 8 | `operator` | `string` |  |  |
| 9 | `dt` | `string` |  |  |
| 10 | `operation` | `string` |  |  |

---

## ods_log_risk_post_digest_audit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_post_digest_audit_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 7.5G |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `digest` | `string` |  |  |
| 3 | `id` | `bigint` |  |  |
| 4 | `response` | `map<string, string>` |  |  |
| 5 | `dt` | `string` |  |  |

---

## ods_log_risk_radar_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_radar_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `preitems` | `row<string>('radar_ref_datetime')` |  |  |
| 2 | `model` | `row<string,string>('name','guid')` |  |  |
| 3 | `fields` | `row<int,string,string,bigint,string,bigint>('actionType','eventId','reffer','eventTime','ua','userId')` |  |  |
| 4 | `rgs` | `array<row<string,string,int,array<row<string,string,string>('desc','key','value')>>('group','risk','score','hitobjects')>` |  |  |
| 5 | `dt` | `string` |  | date partition field |

---

## ods_log_risk_slider_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_slider_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 190.4M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `msgtype` | `bigint` |  |  |
| 2 | `payload` | `string` |  |  |
| 3 | `dt` | `string` |  |  |

---

## ods_log_risk_slider_result_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_slider_result_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 3.7G |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `reqid` | `string` |  |  |
| 2 | `userid` | `bigint` |  |  |
| 3 | `source` | `string` |  |  |
| 4 | `ua` | `string` |  |  |
| 5 | `type` | `string` |  |  |
| 6 | `result` | `boolean` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_risk_yidun_audit_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_risk_yidun_audit_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 398.0B |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `businessid` | `string` |  |  |
| 2 | `parsedcallback` | `string` |  |  |
| 3 | `parsedcallbackclass` | `string` |  |  |
| 4 | `secretid` | `string` |  |  |
| 5 | `signature` | `string` |  |  |
| 6 | `taskid` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_role_anti_media_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_role_anti_media_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 252.9M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `roleid` | `bigint` |  |  |
| 5 | `userid` | `bigint` |  |  |
| 6 | `request` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_searchmob_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_searchmob_di` |
| **描述** | lofter tomcat searchmob log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 303.5M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `tab` | `string` |  | from deserializer |
| 13 | `words` | `string` |  | from deserializer |
| 14 | `logintype` | `string` |  |  |
| 15 | `dt` | `string` |  |  |

---

## ods_log_searchpchome_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_searchpchome_di` |
| **描述** | lofter tomcat searchpchome log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `words` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_searchpcperson_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_searchpcperson_di` |
| **描述** | lofter tomcat searchpcperson log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 5.3G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `words` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_send_sms_task_by_package_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_send_sms_task_by_package_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 11.2M |
| **是否分区表** | 是 |

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  |  |
| 2 | `packid` | `bigint` |  |  |
| 3 | `time` | `bigint` |  |  |
| 4 | `phone` | `string` |  | 手机号 |
| 5 | `dt` | `string` |  |  |

---

## ods_log_serecpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_serecpage_di` |
| **描述** | lofter se rec page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 121.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 121.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_share_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_share_di` |
| **描述** | lofter tomcat share log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 1.8K |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `shareid` | `string` |  | from deserializer |
| 11 | `postid` | `string` |  | from deserializer |
| 12 | `uidid` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_shareblog_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_shareblog_di` |
| **描述** | lofter tomcat shareblog |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `shareid` | `string` |  | from deserializer |
| 13 | `sharetype` | `string` |  | from deserializer |
| 14 | `blogid` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_sharelabel_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_sharelabel_di` |
| **描述** | lofter tomcat sharelabel |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `shareid` | `string` |  | from deserializer |
| 13 | `sharetype` | `string` |  | from deserializer |
| 14 | `labelname` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_sharepost_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_sharepost_di` |
| **描述** | lofter tomcat sharepost |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `postid` | `string` |  | from deserializer |
| 13 | `shareid` | `string` |  | from deserializer |
| 14 | `sharetype` | `string` |  | from deserializer |
| 15 | `labelname` | `string` |  | from deserializer |
| 16 | `uidid` | `string` |  | from deserializer |
| 17 | `posttype` | `string` |  | from deserializer |
| 18 | `dt` | `string` |  |  |

---

## ods_log_shieldrecommend_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_shieldrecommend_di` |
| **描述** | lofter tomcat shieldrecommend |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 568.9M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `shieldid` | `string` |  | from deserializer |
| 13 | `uidid` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_log_singlelogpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_singlelogpage_di` |
| **描述** | lofter tomcat singlelogpage |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 9.7G |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `labelname` | `string` |  | from deserializer |
| 11 | `blogid` | `string` |  | from deserializer |
| 12 | `dt` | `string` |  |  |

---

## ods_log_skyeye_faas_data_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_skyeye_faas_data_di` |
| **描述** | 音乐站外爬取数据 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.1M |
| **是否分区表** | 是 |

### 字段详情

共 39 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `sentiment` | `string` |  |  |
| 2 | `seed` | `string` |  |  |
| 3 | `keywords` | `array<string>` |  |  |
| 4 | `iplocation` | `string` |  |  |
| 5 | `origin` | `string` |  |  |
| 6 | `contentid` | `string` |  |  |
| 7 | `updateat` | `string` |  |  |
| 8 | `source` | `string` |  |  |
| 9 | `title` | `string` |  |  |
| 10 | `content` | `string` |  |  |
| 11 | `createat` | `string` |  |  |
| 12 | `sourceurl` | `string` |  |  |
| 13 | `sharecount` | `int` |  |  |
| 14 | `ispopofeedback` | `boolean` |  |  |
| 15 | `problem` | `array<string>` |  |  |
| 16 | `context` | `string` |  |  |
| 17 | `iscrawler` | `boolean` |  |  |
| 18 | `usernickname` | `string` |  |  |
| 19 | `categories` | `array<row<string,string,array<string>,string,string>('category','intention','keywords','problem','sentiment')>` |  |  |
| 20 | `isskyeye` | `boolean` |  |  |
| 21 | `collectedcount` | `int` |  |  |
| 22 | `category1` | `array<string>` |  |  |
| 23 | `category2` | `array<string>` |  |  |
| 24 | `category3` | `array<string>` |  |  |
| 25 | `category4` | `array<string>` |  |  |
| 26 | `fullcategory` | `string` |  |  |
| 27 | `crawlertime` | `string` |  |  |
| 28 | `imgids` | `array<string>` |  |  |
| 29 | `likedcount` | `int` |  |  |
| 30 | `skyeyeseedid` | `string` |  |  |
| 31 | `userid` | `string` |  |  |
| 32 | `commentcount` | `int` |  |  |
| 33 | `intention` | `array<string>` |  |  |
| 34 | `hasimageorvideo` | `boolean` |  |  |
| 35 | `createtime` | `string` |  |  |
| 36 | `weburl` | `string` |  |  |
| 37 | `documentid` | `string` |  |  |
| 38 | `category` | `array<string>` |  |  |
| 39 | `dt` | `string` |  |  |

---

## ods_log_specialdetailpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_specialdetailpage_di` |
| **描述** | lofter tomcat specialdetailpage |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 4.1G |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `specialid` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_specialpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_specialpage_di` |
| **描述** | lofter special page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 3.5G |
| **是否分区表** | 是 |

### 字段详情

共 16 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `url` | `string` |  | from deserializer |
| 13 | `specialid` | `string` |  | from deserializer |
| 14 | `specialname` | `string` |  | from deserializer |
| 15 | `parameter` | `string` |  | from deserializer |
| 16 | `dt` | `string` |  |  |

---

## ods_log_spm_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_spm_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 197.6M |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `productid` | `string` |  |  |
| 2 | `skuid` | `string` |  |  |
| 3 | `spm` | `string` |  |  |
| 4 | `tradeid` | `string` |  |  |
| 5 | `userid` | `string` |  |  |
| 6 | `createtime` | `string` |  |  |
| 7 | `paytime` | `string` |  |  |
| 8 | `dt` | `string` |  | date partition field |

---

## ods_log_stranger_message_daily_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_stranger_message_daily_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 20.9K |
| **是否分区表** | 是 |

### 字段详情

共 8 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型，如 strangerMessageDaily |
| 2 | `logtime` | `string` |  | 日志时间，如 2026-05-27T09:38:08.415 |
| 3 | `loghost` | `string` |  | 产生日志的主机名/容器名 |
| 4 | `strangercount` | `int` |  | 陌生人消息计数（日累计） |
| 5 | `type` | `string` |  | 主体类型，如 user / blog 等 |
| 6 | `userid` | `bigint` |  | 用户ID |
| 7 | `otherblogid` | `bigint` |  | 对方博客ID/创作者ID |
| 8 | `dt` | `string` |  |  |

---

## ods_log_subscribe_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_subscribe_di` |
| **描述** | lofter tomcat subscribe log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 55.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 55.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `labelid` | `string` |  | from deserializer |
| 11 | `labelname` | `string` |  | from deserializer |
| 12 | `istopic` | `string` |  | from deserializer |
| 13 | `isrecommend` | `string` |  | from deserializer |
| 14 | `subscribesource` | `string` |  | from deserializer |
| 15 | `devicetype` | `string` |  | from deserializer |
| 16 | `appversion` | `string` |  | from deserializer |
| 17 | `logintype` | `string` |  |  |
| 18 | `dt` | `string` |  |  |

---

## ods_log_subscribecancel_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_subscribecancel_di` |
| **描述** | lofter tomcat subscribecancel log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 5.9G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `labelid` | `string` |  | from deserializer |
| 11 | `labelname` | `string` |  | from deserializer |
| 12 | `istopic` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_subscribecollection_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_subscribecollection_di` |
| **描述** | lofter ds subscribe colletion |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 26.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 26.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `string` |  |  |
| 2 | `collectionid` | `string` |  |  |
| 3 | `optime` | `string` |  |  |
| 4 | `action` | `string` |  |  |
| 5 | `type` | `string` |  |  |
| 6 | `logintype` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_subscribepage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_subscribepage_di` |
| **描述** | lofter subscribe page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | N/A |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_subscribepost_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_subscribepost_di` |
| **描述** | lofter ds subscribe post |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 65.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 65.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `accountid` | `string` |  |  |
| 2 | `appversion` | `string` |  |  |
| 3 | `clienttype` | `string` |  |  |
| 4 | `uidid` | `string` |  |  |
| 5 | `logintype` | `string` |  |  |
| 6 | `subscribeid` | `string` |  |  |
| 7 | `action` | `string` |  |  |
| 8 | `time` | `string` |  |  |
| 9 | `type` | `string` |  |  |
| 10 | `postid` | `string` |  |  |
| 11 | `dt` | `string` |  |  |

---

## ods_log_superwoman_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_superwoman_di` |
| **描述** | lofter supewoman log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 11.6M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `dt` | `string` |  |  |

---

## ods_log_tag_protected_detail_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_tag_protected_detail_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2.1M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志采集时间 |
| 3 | `loghost` | `string` |  | 日志采集主机 |
| 4 | `createtime` | `bigint` |  |  |
| 5 | `tag` | `string` |  |  |
| 6 | `postid` | `bigint` |  |  |
| 7 | `source` | `string` |  |  |
| 8 | `blogid` | `bigint` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_tag_protected_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_tag_protected_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.4M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志采集时间 |
| 3 | `loghost` | `string` |  | 日志采集主机 |
| 4 | `op` | `string` |  | 操作行为 |
| 5 | `createtime` | `bigint` |  | 创建时间 |
| 6 | `tag` | `string` |  | 标签名 |
| 7 | `dt` | `string` |  |  |

---

## ods_log_tag_resource_empty_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_tag_resource_empty_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 3.5M |
| **是否分区表** | 是 |

### 字段详情

共 21 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  | 日志类型 |
| 2 | `logtime` | `string` |  | 日志时间，ISO-8601 带毫秒，如 2026-06-10T18:04:53.686 |
| 3 | `loghost` | `string` |  | 产生日志的主机名/容器名 |
| 4 | `tag` | `string` |  | 标签名 |
| 5 | `type` | `string` |  | 查询类型，如 total |
| 6 | `offset` | `int` |  | 分页偏移量 |
| 7 | `limit` | `int` |  | 分页大小 |
| 8 | `lastposttime` | `bigint` |  | 最后一条帖子时间戳（毫秒） |
| 9 | `style` | `int` |  | 展示样式 |
| 10 | `posttypes` | `string` |  | 帖子类型过滤，逗号分隔 |
| 11 | `postym` | `string` |  | 帖子年月过滤，格式 yyyyMM |
| 12 | `postymdst` | `bigint` |  | 帖子日期范围起，格式 yyyyMMdd |
| 13 | `postymdet` | `bigint` |  | 帖子日期范围止，格式 yyyyMMdd |
| 14 | `range` | `int` |  | 范围参数 |
| 15 | `recentday` | `int` |  | 最近天数 |
| 16 | `protectedflag` | `int` |  | 保护标记 |
| 17 | `returngiftcombination` | `string` |  | 回礼组合 |
| 18 | `userid` | `bigint` |  | 用户ID |
| 19 | `sourcetype` | `int` |  | 来源类型，-1 表示未知 |
| 20 | `abflag` | `string` |  | AB 实验标记，如 old/new |
| 21 | `dt` | `string` |  |  |

---

## ods_log_taxiuhomepage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_taxiuhomepage_di` |
| **描述** | lofter tomcat taxiuhomepage |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 13.4M |
| **是否分区表** | 是 |

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `dt` | `string` |  |  |

---

## ods_log_taxiupublish_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_taxiupublish_di` |
| **描述** | lofter tomcat taxiupublish |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 680.1K |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `postid` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_tomcat_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_tomcat_di` |
| **描述** | lofter tomcat log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 362.1G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 362.1G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 19 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `cookie_jsessionid` | `string` |  | from deserializer |
| 13 | `dadeviceudid` | `string` |  | from deserializer |
| 14 | `securityinfo` | `string` |  | from deserializer |
| 15 | `logintype` | `string` |  |  |
| 16 | `userid` | `string` |  |  |
| 17 | `userremoteport` | `string` |  |  |
| 18 | `uri` | `string` |  |  |
| 19 | `dt` | `string` |  |  |

---

## ods_log_topicpage_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_topicpage_di` |
| **描述** | lofter topic page |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 9.4G |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `referer` | `string` |  | from deserializer |
| 13 | `url` | `string` |  | from deserializer |
| 14 | `parameter` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_log_trace_request_meta_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_trace_request_meta_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 1.3G |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `traceid` | `string` |  | 跟踪Uuid， 应用于商品、抽赏曝光点击成交转化链路跟踪 |
| 2 | `userid` | `bigint` |  | 请求用户Id |
| 3 | `itemid` | `bigint` |  | 物品id |
| 4 | `itemtype` | `int` |  | 物品类型: 7电商 13卡牌 |
| 5 | `name` | `string` |  | 名称 |
| 6 | `scene` | `string` |  | 场景 |
| 7 | `url` | `string` |  | 推广地址 |
| 8 | `requesttime` | `bigint` |  | 请求时间 |
| 9 | `recid` | `string` |  | 推荐请求Id |
| 10 | `alginfo` | `string` |  |  |
| 11 | `dt` | `string` |  | 日期 |

---

## ods_log_traffic_sensing_push_attribution_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_traffic_sensing_push_attribution_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 9.2M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | from deserializer |
| 2 | `blogid` | `bigint` |  | from deserializer |
| 3 | `percentile` | `double` |  | from deserializer |
| 4 | `expose_uv` | `bigint` |  | from deserializer |
| 5 | `type` | `int` |  | from deserializer |
| 6 | `ip` | `string` |  | from deserializer |
| 7 | `level` | `string` |  | from deserializer |
| 8 | `time` | `bigint` |  | from deserializer |
| 9 | `share` | `bigint` |  | 分享量 |
| 10 | `recommend` | `bigint` |  | 蓝手量 |
| 11 | `hot` | `bigint` |  | 热度 |
| 12 | `response` | `bigint` |  |  |
| 13 | `collection_expose` | `bigint` |  |  |
| 14 | `recommend_expose` | `bigint` |  |  |
| 15 | `dt` | `string` |  |  |

---

## ods_log_traffic_sensing_push_potential_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_traffic_sensing_push_potential_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4.6M |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | from deserializer |
| 2 | `blogid` | `bigint` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `level` | `string` |  | from deserializer |
| 5 | `time` | `bigint` |  | from deserializer |
| 6 | `hot` | `bigint` |  | from deserializer |
| 7 | `exposure` | `bigint` |  | from deserializer |
| 8 | `share` | `bigint` |  | 分享量 |
| 9 | `recommend` | `bigint` |  | 蓝手量 |
| 10 | `dt` | `string` |  |  |

---

## ods_log_unbindphone_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_unbindphone_di` |
| **描述** | lofter tomcat unbindphone log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 25.5M |
| **是否分区表** | 是 |

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `devicetype` | `string` |  | from deserializer |
| 11 | `appversion` | `string` |  | from deserializer |
| 12 | `phonenumber` | `string` |  | from deserializer |
| 13 | `dt` | `string` |  |  |

---

## ods_log_unbund_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_unbund_di` |
| **描述** | lofter tomcat unbund log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 93.9M |
| **是否分区表** | 是 |

### 字段详情

共 14 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `bundaccount` | `string` |  | from deserializer |
| 11 | `accounttype` | `string` |  | from deserializer |
| 12 | `devicetype` | `string` |  | from deserializer |
| 13 | `appversion` | `string` |  | from deserializer |
| 14 | `dt` | `string` |  |  |

---

## ods_log_unsubscribecollection_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_unsubscribecollection_di` |
| **描述** | lofter ds subscribe colletion |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 7.9G |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `string` |  |  |
| 2 | `collectionid` | `string` |  |  |
| 3 | `optime` | `string` |  |  |
| 4 | `action` | `string` |  |  |
| 5 | `type` | `string` |  |  |
| 6 | `logintype` | `string` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_user_phone_unbind_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_user_phone_unbind_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 726.4M |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `msgtype` | `int` |  |  |
| 2 | `messagetype` | `int` |  |  |
| 3 | `phone` | `string` |  |  |
| 4 | `eventtime` | `bigint` |  |  |
| 5 | `type` | `int` |  |  |
| 6 | `userid` | `bigint` |  |  |
| 7 | `dt` | `string` |  |  |

---

## ods_log_user_push_token_info_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_user_push_token_info_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 22.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 22.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `pushchannel` | `int` |  |  |
| 5 | `pushgroupid` | `string` |  |  |
| 6 | `token` | `string` |  |  |
| 7 | `userid` | `bigint` |  |  |
| 8 | `deviceid` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_user_session_time_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_user_session_time_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 56.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 56.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 用户Id |
| 2 | `deviceos` | `string` |  | 设备操作系统 |
| 3 | `time` | `bigint` |  | 会话发生时间 |
| 4 | `sessionuuid` | `string` |  | 会话Uuid |
| 5 | `durationdelta` | `bigint` |  | 会话时长变化量 |
| 6 | `isnewsession` | `int` |  | 是否新会话 1是 0或者null 否 |
| 7 | `dt` | `string` |  |  |

---

## ods_log_user_stat_count_fix_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_user_stat_count_fix_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 7.0M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `newfavoritepostcount` | `string` |  |  |
| 5 | `newsharepostcount` | `string` |  |  |
| 6 | `oldsharepostcount` | `string` |  |  |
| 7 | `oldfavoritepostcount` | `string` |  |  |
| 8 | `userid` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_user_wgt_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_user_wgt_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 12.3M |
| **是否分区表** | 是 |

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `itemtype` | `string` |  |  |
| 5 | `event_id` | `string` |  |  |
| 6 | `posttype` | `int` |  |  |
| 7 | `configid` | `bigint` |  |  |
| 8 | `module` | `string` |  |  |
| 9 | `tag` | `string` |  |  |
| 10 | `postid` | `bigint` |  |  |
| 11 | `tagurl` | `string` |  |  |
| 12 | `userid` | `bigint` |  |  |
| 13 | `url` | `string` |  |  |
| 14 | `scene` | `string` |  |  |
| 15 | `dt` | `string` |  |  |

---

## ods_log_videopublishfaile_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_videopublishfaile_di` |
| **描述** | lofter ds video publish failed |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 198.8K |
| **是否分区表** | 是 |

### 字段详情

共 10 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `vid` | `string` |  |  |
| 2 | `devicetype` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `action` | `string` |  |  |
| 6 | `time` | `string` |  |  |
| 7 | `type` | `string` |  |  |
| 8 | `deviceudid` | `string` |  |  |
| 9 | `errorcode` | `string` |  |  |
| 10 | `dt` | `string` |  |  |

---

## ods_log_videopublishreguest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_videopublishreguest_di` |
| **描述** | lofter ds video publish reguest |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 473.4M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `vid` | `string` |  |  |
| 2 | `devicetype` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `action` | `string` |  |  |
| 6 | `time` | `string` |  |  |
| 7 | `type` | `string` |  |  |
| 8 | `deviceudid` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_videopublishsuccess_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_videopublishsuccess_di` |
| **描述** | lofter ds video publish success |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 675.5M |
| **是否分区表** | 是 |

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `vid` | `string` |  |  |
| 2 | `devicetype` | `string` |  |  |
| 3 | `appversion` | `string` |  |  |
| 4 | `deviceos` | `string` |  |  |
| 5 | `action` | `string` |  |  |
| 6 | `time` | `string` |  |  |
| 7 | `type` | `string` |  |  |
| 8 | `deviceudid` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## ods_log_viewabtest_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_viewabtest_di` |
| **描述** | lofter tomcat viewabtest log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 382.1M |
| **是否分区表** | 是 |

### 字段详情

共 11 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `page` | `string` |  | from deserializer |
| 11 | `dt` | `string` |  |  |

---

## ods_log_visitdomain_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_log_visitdomain_di` |
| **描述** | lofter tomcat visitdomain log |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 50.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 50.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 15 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `time` | `string` |  | from deserializer |
| 2 | `accountid` | `string` |  | from deserializer |
| 3 | `ip` | `string` |  | from deserializer |
| 4 | `browser` | `string` |  | from deserializer |
| 5 | `action` | `string` |  | from deserializer |
| 6 | `type` | `string` |  | from deserializer |
| 7 | `clienttype` | `string` |  | from deserializer |
| 8 | `deviceudid` | `string` |  | from deserializer |
| 9 | `appchannel` | `string` |  | from deserializer |
| 10 | `domainid` | `string` |  | from deserializer |
| 11 | `domainname` | `string` |  | from deserializer |
| 12 | `visitsource` | `string` |  | from deserializer |
| 13 | `devicetype` | `string` |  | from deserializer |
| 14 | `appversion` | `string` |  | from deserializer |
| 15 | `dt` | `string` |  |  |

---

## ods_mda_app_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_app_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 390488.9G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 390488.9G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 52 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `userid` | `bigint` |  |  |
| 4 | `usertype` | `int` |  |  |
| 5 | `username` | `string` |  |  |
| 6 | `appversion` | `string` |  |  |
| 7 | `appchannel` | `string` |  |  |
| 8 | `occurtime` | `bigint` |  |  |
| 9 | `category` | `string` |  |  |
| 10 | `label` | `string` |  |  |
| 11 | `customudid` | `string` |  |  |
| 12 | `sessionuuid` | `string` |  |  |
| 13 | `sdkversion` | `string` |  |  |
| 14 | `ip` | `string` |  |  |
| 15 | `deviceplatform` | `string` |  |  |
| 16 | `deviceos` | `string` |  |  |
| 17 | `deviceosversion` | `string` |  |  |
| 18 | `devicemodel` | `string` |  |  |
| 19 | `deviceresolution` | `string` |  |  |
| 20 | `deviceoldmacaddr` | `string` |  |  |
| 21 | `devicemacaddr` | `string` |  |  |
| 22 | `deviceadid` | `string` |  |  |
| 23 | `deviceidfv` | `string` |  |  |
| 24 | `deviceimei` | `string` |  |  |
| 25 | `devicenetwork` | `string` |  |  |
| 26 | `devicecarrier` | `string` |  |  |
| 27 | `appkey` | `string` |  |  |
| 28 | `city` | `string` |  |  |
| 29 | `timezone` | `string` |  |  |
| 30 | `localelanguage` | `string` |  |  |
| 31 | `localecountry` | `string` |  |  |
| 32 | `wifibssid` | `string` |  |  |
| 33 | `wifissid` | `string` |  |  |
| 34 | `costtime` | `bigint` |  |  |
| 35 | `source` | `string` |  |  |
| 36 | `deviceandroidid` | `string` |  |  |
| 37 | `sdktype` | `string` |  |  |
| 38 | `itemid` | `string` |  |  |
| 39 | `itemtype` | `string` |  |  |
| 40 | `recid` | `string` |  |  |
| 41 | `scene` | `string` |  |  |
| 42 | `action` | `string` |  |  |
| 43 | `tagname` | `string` |  |  |
| 44 | `layout` | `string` |  |  |
| 45 | `alginfo` | `string` |  |  |
| 46 | `params` | `map<string, string>` |  |  |
| 47 | `kafkatime` | `bigint` |  |  |
| 48 | `isbeta` | `string` |  | 是否ios testflight：1是 0否 |
| 49 | `oaid` | `string` |  | 安卓设备标识 oaid |
| 50 | `actiontype` | `string` |  | 事件对应的行为类型:cell_exposure,cell_click,subscribe,search,like etc |
| 51 | `tdid` | `string` |  |  |
| 52 | `dt` | `string` |  |  |

---

## ods_mda_app_raw_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_app_raw_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 4693.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 4693.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 52 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `eventid` | `string` |  |  |
| 2 | `deviceudid` | `string` |  |  |
| 3 | `userid` | `bigint` |  |  |
| 4 | `usertype` | `int` |  |  |
| 5 | `username` | `string` |  |  |
| 6 | `appversion` | `string` |  |  |
| 7 | `appchannel` | `string` |  |  |
| 8 | `occurtime` | `bigint` |  |  |
| 9 | `category` | `string` |  |  |
| 10 | `label` | `string` |  |  |
| 11 | `customudid` | `string` |  |  |
| 12 | `sessionuuid` | `string` |  |  |
| 13 | `sdkversion` | `string` |  |  |
| 14 | `ip` | `string` |  |  |
| 15 | `deviceplatform` | `string` |  |  |
| 16 | `deviceos` | `string` |  |  |
| 17 | `deviceosversion` | `string` |  |  |
| 18 | `devicemodel` | `string` |  |  |
| 19 | `deviceresolution` | `string` |  |  |
| 20 | `deviceoldmacaddr` | `string` |  |  |
| 21 | `devicemacaddr` | `string` |  |  |
| 22 | `deviceadid` | `string` |  |  |
| 23 | `deviceidfv` | `string` |  |  |
| 24 | `deviceimei` | `string` |  |  |
| 25 | `devicenetwork` | `string` |  |  |
| 26 | `devicecarrier` | `string` |  |  |
| 27 | `appkey` | `string` |  |  |
| 28 | `city` | `string` |  |  |
| 29 | `timezone` | `string` |  |  |
| 30 | `localelanguage` | `string` |  |  |
| 31 | `localecountry` | `string` |  |  |
| 32 | `wifibssid` | `string` |  |  |
| 33 | `wifissid` | `string` |  |  |
| 34 | `costtime` | `bigint` |  |  |
| 35 | `source` | `string` |  |  |
| 36 | `deviceandroidid` | `string` |  |  |
| 37 | `sdktype` | `string` |  |  |
| 38 | `itemid` | `string` |  |  |
| 39 | `itemtype` | `string` |  |  |
| 40 | `recid` | `string` |  |  |
| 41 | `scene` | `string` |  |  |
| 42 | `action` | `string` |  |  |
| 43 | `tagname` | `string` |  |  |
| 44 | `layout` | `string` |  |  |
| 45 | `alginfo` | `string` |  |  |
| 46 | `params` | `map<string, string>` |  |  |
| 47 | `kafkatime` | `bigint` |  |  |
| 48 | `isbeta` | `string` |  | 是否ios testflight：1是 0否 |
| 49 | `oaid` | `string` |  | 安卓设备标识 oaid |
| 50 | `tdid` | `string` |  |  |
| 51 | `actiontype` | `string` |  | 事件对应的行为类型:cell_exposure,cell_click,subscribe,search,like, other |
| 52 | `dt` | `string` |  |  |

---

## ods_mda_bookstore_miniprogram_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_bookstore_miniprogram_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 12.6M |
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

## ods_mda_miniprogram_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_miniprogram_di` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 127.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 127.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

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

## ods_mda_push_reach_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_push_reach_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 3.9G |
| **是否分区表** | 是 |

### 字段详情

共 51 个字段：

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
| 51 | `dt` | `string` |  |  |

---

## ods_mda_ruyuan_miniprogram_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_ruyuan_miniprogram_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 22.6M |
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
| **数据库** | `lofter` |
| **表名** | `ods_mda_wap_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 5019.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5019.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 47 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | from deserializer |
| 2 | `eventid` | `string` |  | from deserializer |
| 3 | `urlpath` | `string` |  | from deserializer |
| 4 | `userid` | `bigint` |  | from deserializer |
| 5 | `region` | `string` |  | from deserializer |
| 6 | `screenheight` | `int` |  | from deserializer |
| 7 | `browserversion` | `string` |  | from deserializer |
| 8 | `referrerdomain` | `string` |  | from deserializer |
| 9 | `kafkatime` | `bigint` |  | from deserializer |
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
| 20 | `occurtime` | `bigint` |  | from deserializer |
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
| 37 | `screenwidth` | `int` |  | from deserializer |
| 38 | `pagetitle` | `string` |  | from deserializer |
| 39 | `appchannel` | `string` |  | from deserializer |
| 40 | `pageopenscene` | `string` |  | from deserializer |
| 41 | `firstlevelsource` | `string` |  | from deserializer |
| 42 | `ipprovince` | `string` |  | from deserializer |
| 43 | `appkey` | `string` |  | from deserializer |
| 44 | `costtime` | `bigint` |  | from deserializer |
| 45 | `attributes` | `map<string, string>` |  |  |
| 46 | `useragent` | `string` |  | 浏览器ua |
| 47 | `dt` | `string` |  | date partition field |

---

## ods_mda_web_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_mda_web_di` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 229.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 229.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 47 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `deviceudid` | `string` |  | from deserializer |
| 2 | `eventid` | `string` |  | from deserializer |
| 3 | `urlpath` | `string` |  | from deserializer |
| 4 | `userid` | `bigint` |  | from deserializer |
| 5 | `region` | `string` |  | from deserializer |
| 6 | `screenheight` | `int` |  | from deserializer |
| 7 | `browserversion` | `string` |  | from deserializer |
| 8 | `referrerdomain` | `string` |  | from deserializer |
| 9 | `kafkatime` | `bigint` |  | from deserializer |
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
| 20 | `occurtime` | `bigint` |  | from deserializer |
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
| 37 | `screenwidth` | `int` |  | from deserializer |
| 38 | `pagetitle` | `string` |  | from deserializer |
| 39 | `appchannel` | `string` |  | from deserializer |
| 40 | `pageopenscene` | `string` |  | from deserializer |
| 41 | `firstlevelsource` | `string` |  | from deserializer |
| 42 | `ipprovince` | `string` |  | from deserializer |
| 43 | `appkey` | `string` |  | from deserializer |
| 44 | `costtime` | `bigint` |  | from deserializer |
| 45 | `attributes` | `map<string, string>` |  |  |
| 46 | `useragent` | `string` |  | 浏览器ua |
| 47 | `dt` | `string` |  | date partition field |

---

## ods_pve_log_ai_metric_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_pve_log_ai_metric_di` |
| **描述** | 无描述 |
| **Owner** | bdms_xiongyangyang01 |
| **表类型** | external |
| **表大小** | 75.9M |
| **是否分区表** | 是 |

### 字段详情

共 18 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `logtype` | `string` |  |  |
| 2 | `logtime` | `string` |  |  |
| 3 | `loghost` | `string` |  |  |
| 4 | `usercontent` | `string` |  |  |
| 5 | `model` | `string` |  |  |
| 6 | `airt` | `int` |  |  |
| 7 | `code` | `int` |  |  |
| 8 | `requestjson` | `string` |  |  |
| 9 | `requestid` | `string` |  |  |
| 10 | `responsejson` | `string` |  |  |
| 11 | `requesttime` | `string` |  |  |
| 12 | `inputtokencount` | `int` |  |  |
| 13 | `outputtokencount` | `int` |  |  |
| 14 | `totaltokencount` | `int` |  |  |
| 15 | `cachedtokencount` | `int` |  |  |
| 16 | `content` | `string` |  |  |
| 17 | `hitnonage` | `boolean` |  |  |
| 18 | `dt` | `string` |  |  |

---

## ods_rec_content_understand_log_di

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_rec_content_understand_log_di` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 1.2G |
| **是否分区表** | 是 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `itemid` | `bigint` |  | 物料id: 文章Id、评论Id等 |
| 2 | `type` | `string` |  | 识别类型: 图文类目V2,视频类目,非作品向文章类目等 |
| 3 | `level1_tag` | `string` |  | 一级标签 |
| 4 | `level2_tag` | `string` |  | 二级标签 |
| 5 | `level3_tag` | `string` |  | 三级标签 |
| 6 | `score` | `double` |  | 计算分值 |
| 7 | `dt` | `string` |  |  |

---

## ods_risk_limit

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_risk_limit` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | view |
| **表大小** | N/A |
| **是否分区表** | 否 |

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客id |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `status` | `int` |  | 状态： 0有效 |
| 4 | `type` | `int` |  | 类型： 0文章限流 1博客限流 |
| 5 | `optime` | `bigint` |  | 操作时间 |
| 6 | `operator` | `string` |  | 操作者 |
| 7 | `dt` | `string` |  |  |

---

## ods_risk_limit_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_risk_limit_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 24.4G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 24.4G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 7 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  | 博客id |
| 2 | `postid` | `bigint` |  | 文章id |
| 3 | `status` | `int` |  | 状态： 0有效 |
| 4 | `type` | `int` |  | 类型： 0文章限流 1博客限流 |
| 5 | `optime` | `bigint` |  | 操作时间 |
| 6 | `operator` | `string` |  | 操作者 |
| 7 | `dt` | `string` |  |  |

---

## ods_risk_mark_return_gift_nd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `ods_risk_mark_return_gift_nd` |
| **描述** | 无描述 |
| **Owner** | da_lofter |
| **表类型** | internal |
| **表大小** | 207.5M |
| **是否分区表** | 否 |

### 字段详情

共 6 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `blogid` | `bigint` |  |  |
| 2 | `id` | `bigint` |  |  |
| 3 | `machinetags` | `array<string>` |  |  |
| 4 | `persontags` | `array<string>` |  |  |
| 5 | `postid` | `bigint` |  |  |
| 6 | `reviewstatus` | `bigint` |  |  |

---

## rec_fea_user_long_profile_tags_v3

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `rec_fea_user_long_profile_tags_v3` |
| **描述** | 无描述 |
| **Owner** | N/A |
| **表类型** | external |
| **表大小** | 226.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 226.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 9 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `user_id` | `string` |  |  |
| 2 | `l_p_tag_pos` | `string` |  | 正向标签，180d |
| 3 | `l_p_tag_neg` | `string` |  | 弱负向标签，用打散 |
| 4 | `l_p_tag_blk` | `string` |  | 屏蔽标签，用过滤 |
| 5 | `l_p_tag_pos_60d` | `string` |  |  |
| 6 | `l_p_tag_pos_30d` | `string` |  |  |
| 7 | `l_p_tag_pos_10d` | `string` |  |  |
| 8 | `l_p_tag_pos_3d` | `string` |  |  |
| 9 | `dt` | `string` |  |  |

---

## stg_par_creator_interaction_in_130d_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_par_creator_interaction_in_130d_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 9589.7G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 9589.7G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 创作者id |
| 2 | `hduserid` | `bigint` |  | 互动用户id |
| 3 | `op_dt` | `string` |  | opTime日期 |
| 4 | `hot` | `bigint` |  | 热度 |
| 5 | `like_cnt` | `bigint` |  | 点赞量 |
| 6 | `reproduce_cnt` | `bigint` |  | 转载量 |
| 7 | `recommend_cnt` | `bigint` |  | 推荐量 |
| 8 | `collect_cnt` | `bigint` |  | 收藏量 |
| 9 | `comment_cnt` | `bigint` |  | 评论量 |
| 10 | `optime` | `bigint` |  | opTime |
| 11 | `postid` | `bigint` |  | 文章id |
| 12 | `dt` | `string` |  |  |

---

## stg_par_creator_interaction_out_130d_wd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_par_creator_interaction_out_130d_wd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 3274.6G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 3274.6G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 12 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `userid` | `bigint` |  | 创作者id |
| 2 | `hduserid` | `bigint` |  | 互动用户id |
| 3 | `op_dt` | `string` |  | opTime日期 |
| 4 | `hot` | `bigint` |  | 热度 |
| 5 | `like_cnt` | `bigint` |  | 点赞量 |
| 6 | `reproduce_cnt` | `bigint` |  | 转载量 |
| 7 | `recommend_cnt` | `bigint` |  | 推荐量 |
| 8 | `collect_cnt` | `bigint` |  | 收藏量 |
| 9 | `comment_cnt` | `bigint` |  | 评论量 |
| 10 | `optime` | `bigint` |  | opTime |
| 11 | `postid` | `bigint` |  | 文章id |
| 12 | `dt` | `string` |  |  |

---

## stg_post_content_feature_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_post_content_feature_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_hzxiaonaitong |
| **表类型** | external |
| **表大小** | 44.3G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 44.3G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 5 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `blogid` | `bigint` |  | 博客id |
| 3 | `pt_type` | `string` |  | 标签维度: video category |
| 4 | `label` | `string` |  | 内容特征 |
| 5 | `dt` | `string` |  |  |

---

## stg_post_hot_dynamic_in_130d_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_post_hot_dynamic_in_130d_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 2040.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 2040.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `postid` | `bigint` |  | 热门日志ID |
| 3 | `blogid` | `bigint` |  | 热门日志所属的轻博ID |
| 4 | `publisheruserid` | `bigint` |  | 操作者的ID |
| 5 | `frompostid` | `bigint` |  | 引用日志Id |
| 6 | `fromblogid` | `bigint` |  | 引用博客Id |
| 7 | `topostid` | `bigint` |  | 被引用日志Id |
| 8 | `toblogid` | `bigint` |  | 被引用博客Id |
| 9 | `content` | `string` |  | 转载附加的内容 |
| 10 | `optime` | `bigint` |  | 操作时间 |
| 11 | `type` | `int` |  | 类型,转载或喜欢 |
| 12 | `ip` | `string` |  | IP |
| 13 | `dt` | `string` |  |  |

---

## stg_post_hot_static_out_130d_wd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_post_hot_static_out_130d_wd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 5650.0G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 5650.0G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 13 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `id` | `bigint` |  | 主键ID |
| 2 | `postid` | `bigint` |  | 热门日志ID |
| 3 | `blogid` | `bigint` |  | 热门日志所属的轻博ID |
| 4 | `publisheruserid` | `bigint` |  | 操作者的ID |
| 5 | `frompostid` | `bigint` |  | 引用日志Id |
| 6 | `fromblogid` | `bigint` |  | 引用博客Id |
| 7 | `topostid` | `bigint` |  | 被引用日志Id |
| 8 | `toblogid` | `bigint` |  | 被引用博客Id |
| 9 | `content` | `string` |  | 转载附加的内容 |
| 10 | `optime` | `bigint` |  | 操作时间 |
| 11 | `type` | `int` |  | 类型,转载或喜欢 |
| 12 | `ip` | `string` |  | IP |
| 13 | `dt` | `string` |  |  |

---

## stg_post_interaction_in_130d_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_post_interaction_in_130d_dd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 707.2G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 707.2G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `like_cnt` | `bigint` |  | 累积喜欢数 |
| 3 | `reproduce_cnt` | `bigint` |  | 累积转载数 |
| 4 | `recommend_cnt` | `bigint` |  | 累积推荐数 |
| 5 | `collect_cnt` | `bigint` |  | 累积收藏数 |
| 6 | `hot_uv` | `bigint` |  | 用户累积热度 |
| 7 | `hot` | `bigint` |  | 累积热度 |
| 8 | `hot_7d` | `bigint` |  | 近7日热度 |
| 9 | `hot_15d` | `bigint` |  | 近15日热度 |
| 10 | `hot_30d` | `bigint` |  | 近30日热度 |
| 11 | `hot_90d` | `bigint` |  | 近90日热度 |
| 12 | `hot_365d` | `bigint` |  | 近365天热度 |
| 13 | `like_cnt_1d` | `bigint` |  | 近1日喜欢数 |
| 14 | `like_cnt_7d` | `bigint` |  | 近7日喜欢数 |
| 15 | `like_cnt_15d` | `bigint` |  | 近15日喜欢数 |
| 16 | `like_cnt_30d` | `bigint` |  | 近30日喜欢数 |
| 17 | `recommend_cnt_1d` | `bigint` |  | 近1日蓝手数 |
| 18 | `recommend_cnt_7d` | `bigint` |  | 近7日蓝手数 |
| 19 | `recommend_cnt_15d` | `bigint` |  | 近15日蓝手数 |
| 20 | `recommend_cnt_30d` | `bigint` |  | 近30日蓝手数 |
| 21 | `collect_cnt_1d` | `bigint` |  | 近1日收藏数 |
| 22 | `collect_cnt_7d` | `bigint` |  | 近7日收藏数 |
| 23 | `collect_cnt_15d` | `bigint` |  | 近15日收藏数 |
| 24 | `collect_cnt_30d` | `bigint` |  | 近30日收藏数 |
| 25 | `recommend_user_bitmap` | `varbinary(2147483647)` |  | 推荐用户位图 |
| 26 | `dt` | `string` |  |  |

---

## stg_post_interaction_out_130d_wd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `stg_post_interaction_out_130d_wd` |
| **描述** | 无描述 |
| **Owner** | bdms_zhaozijun03 |
| **表类型** | external |
| **表大小** | 914.5G |
| **是否分区表** | 是 |

> ⚠️ **【重要提示】** 该表大小为 914.5G，属于大表且为分区表。**查询该表时必须在 WHERE 条件中包含分区字段的限制条件**（如 `dt='2026-03-01'` 或 `dt>='2026-03-01' AND dt<='2026-03-07'`），否则将扫描全量数据，导致查询超时或资源浪费。

### 字段详情

共 26 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `postid` | `bigint` |  | 文章id |
| 2 | `like_cnt` | `bigint` |  | 累积喜欢数 |
| 3 | `reproduce_cnt` | `bigint` |  | 累积转载数 |
| 4 | `recommend_cnt` | `bigint` |  | 累积推荐数 |
| 5 | `collect_cnt` | `bigint` |  | 累积收藏数 |
| 6 | `hot_uv_bitmap` | `varbinary(2147483647)` |  | 用户热度位图 |
| 7 | `hot` | `bigint` |  | 累积热度 |
| 8 | `hot_7d` | `bigint` |  | 近7日热度 |
| 9 | `hot_15d` | `bigint` |  | 近15日热度 |
| 10 | `hot_30d` | `bigint` |  | 近30日热度 |
| 11 | `hot_90d` | `bigint` |  | 近90日热度 |
| 12 | `hot_365d` | `bigint` |  | 近365天热度 |
| 13 | `like_cnt_1d` | `bigint` |  | 近1日喜欢数 |
| 14 | `like_cnt_7d` | `bigint` |  | 近7日喜欢数 |
| 15 | `like_cnt_15d` | `bigint` |  | 近15日喜欢数 |
| 16 | `like_cnt_30d` | `bigint` |  | 近30日喜欢数 |
| 17 | `recommend_cnt_1d` | `bigint` |  | 近1日蓝手数 |
| 18 | `recommend_cnt_7d` | `bigint` |  | 近7日蓝手数 |
| 19 | `recommend_cnt_15d` | `bigint` |  | 近15日蓝手数 |
| 20 | `recommend_cnt_30d` | `bigint` |  | 近30日蓝手数 |
| 21 | `collect_cnt_1d` | `bigint` |  | 近1日收藏数 |
| 22 | `collect_cnt_7d` | `bigint` |  | 近7日收藏数 |
| 23 | `collect_cnt_15d` | `bigint` |  | 近15日收藏数 |
| 24 | `collect_cnt_30d` | `bigint` |  | 近30日收藏数 |
| 25 | `recommend_user_bitmap` | `varbinary(2147483647)` |  | 推荐用户位图 |
| 26 | `dt` | `string` |  |  |

---

## zd_lofter_post_human_no_pass_info_dd

### 基本信息

| 属性 | 值 |
|------|------|
| **数据库** | `lofter` |
| **表名** | `zd_lofter_post_human_no_pass_info_dd` |
| **描述** | 近俩年过安全人审但未进推荐池的内容 |
| **Owner** | bdms_zhangdian04 |
| **表类型** | internal |
| **表大小** | 736.1M |
| **是否分区表** | 是 |

### 字段详情

共 3 个字段：

| 序号 | 字段名 | 类型 | 是否主键 | 描述 |
|------|--------|------|----------|------|
| 1 | `ip` | `string` |  |  |
| 2 | `postid` | `bigint` |  |  |
| 3 | `dt` | `string` |  |  |

---

