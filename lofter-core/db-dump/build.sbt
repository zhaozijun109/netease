name := "lofter-db-dump"
organization := "com.netease.yaolu"
version := "0.0.1"

connect in generateJob := {
  case "Risk_AuditPost" | "benefit_adTrace_config" =>
    "jdbc:mysql://10.59.186.164:6000/lofter-mirror-gz"

  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" =>
    "jdbc:mysql://lofter-rds-statis-jd-34893.rds.cn-gz-p1.internal.:3306/comic_statis"

  case "conf_abtest" | "conf_multi_abtest" | "Conf_DispatchPlan" | "Conf_DispatchSource" | "Tc_Task" =>
    "jdbc:mysql://lofter-rds-common-recomment-mirror-gz-34729.rds.cn-gz-p1.internal.:3331/recomment"

  case table if table.startsWith("Ab_") =>
    "jdbc:mysql://lofter-rds-public-online-gz-34591.rds.cn-gz-p1.internal.:3331/dc_ab"

  case table if table.startsWith("MP_") || table.startsWith("AD_") || table == "DeviceCaid" =>
    "jdbc:mysql://10.59.186.122:6000/lofter-yaolu-online"

  case table if (table.startsWith("Dispatch_") && table != "Dispatch_ProjectApprovalOperator" ) || table.startsWith("Robot_") =>
    "jdbc:mysql://lofter-rds-flow-control-online-34888.rds.cn-gz-p1.internal.:3306/flow_control"

  case _ =>
    "jdbc:mysql://10.59.186.164:6000/lofter-mirror-gz"
}

connectUserName in generateJob := {
  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" => "lofter_bi"
  case _ => "lofter_bi_gy"
}

connectPassword in generateJob := {
  case "Risk_AuditPost" | "benefit_adTrace_config" => "Q8@BJ5wh_"
  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" => "qKsbCbRpM"
  case "conf_abtest" | "conf_multi_abtest" | "Conf_DispatchPlan" | "Conf_DispatchSource" | "Tc_Task" => "q4W0Kf_@I"
  case table if table.startsWith("Ab_") => "@2X1oKN_h"
  case table if table.startsWith("MP_") || table.startsWith("AD_") || table == "DeviceCaid"=> "w4W9F_A@q"
  case table if (table.startsWith("Dispatch_") && table != "Dispatch_ProjectApprovalOperator" ) || table.startsWith("Robot_") => "WjQ3@hE@1"
  case _ => "Q8@BJ5wh_"
}

connectMode in generateJob := {
  case "AppFlyer_Push" | "ad_user_action_partition" | "ad_user_action_confirm" | "conf_abtest" | "conf_multi_abtest" | "Conf_DispatchPlan" | "Conf_DispatchSource" | "Tc_Task" => "rds"
  case table if table.startsWith("Ab_") => "rds"
  case table if (table.startsWith("Dispatch_") && table != "Dispatch_ProjectApprovalOperator" ) || table.startsWith("Robot_") => "rds"
  case _ => "qs"
}

jobOutputBaseDirectory in generateJob := "/user/da_lofter/db_dump"
jobOutputDirectoryMapper in generateJob := { table =>
  if(table == "Act_LighthouseUser") {
    s"Act_LighthouseUser_v2/$${azkaban.flow.1.days.ago}"
  } else s"$table/$${azkaban.flow.1.days.ago}"
}

tableMetaPath in generateJob := { baseDirectory.value / "meta.txt" }

val tableSet = Set(
  "Account","Account_BackUpRecord","Acquisition_Incantation","Acquisition_Record","ActiveUserInfo","ActivityCenter_Activity","ActivityCenter_ActivityList","ActivityCenter_Category",
  "ActivityTag","Ad_DeviceUserId","Ad_TagConfig","Ad_UserFeatureExt","Ad_UserFeatureTag","AdminPubData","AskPost","Ask_AnswerPost","Ask_JoinedUser","Ask_Question","Ask_QuestionStatistic",
  "Ask_QuestionTag","Ask_Question_bak","Ask_TagChatRoleRelation","AuditLog","Auth_verify_sign_log","Avatar_BoxItem","Avatar_BoxOrder","Avatar_BoxTab","Avatar_BoxUser","Backend_EmailNosImage",
  "BenefitHomePage","BenefitSaleStatistic","BenefitShelf","BenefitShelfProductRecord","Benefit_Activity","Benefit_ActivityAggregation","Benefit_ActivityOrder","Benefit_ActivityRule","Benefit_ActivitySku",
  "Benefit_Bounty","Benefit_BountyProductLimit","Benefit_BountyUser","Benefit_Card","Benefit_CardActivity","Benefit_CardActivitySaleNotice","Benefit_CardActivityStat","Benefit_CardBarrageMock",
  "Benefit_CardChanceTradeRefund","Benefit_CardConfig","Benefit_CardCostPriceConfig","Benefit_CardDaoJu","Benefit_CardDaoJuActivity","Benefit_CardDaoJuUseRecord","Benefit_CardDaoJuUser","Benefit_CardDeliverRemind",
  "Benefit_CardDeliverTimeConfig","Benefit_CardFreebie","Benefit_CardGroup","Benefit_CardGroupRewardUser","Benefit_CardPool","Benefit_CardProp","Benefit_CardPropChooseRecord","Benefit_CardPropGrant",
  "Benefit_CardPropGrantUser","Benefit_CardPropPool","Benefit_CardPropUser","Benefit_CardTrade","Benefit_CardUserDateData","Benefit_CategoryRelation","Benefit_Comment","Benefit_CommentControl",
  "Benefit_CouponUserLimit","Benefit_FeedData","Benefit_FeedRollingData","Benefit_HotSearchConfig","Benefit_ManualRefund","Benefit_ManualRefundAccount","Benefit_ManualRefundUser","Benefit_MarketProductStats",
  "Benefit_OrderProductDelivery","Benefit_OrderProductExt","Benefit_ProductCmbIpRelation","Benefit_ProductIp","Benefit_ProductPreSale","Benefit_ProductSaleNotice","Benefit_ProductSellPrice",
  "Benefit_ProductSupplyPrice","Benefit_RecommendItem","Benefit_RepurchaseCouponStrategy","Benefit_RepurchaseCouponStrategyRelation","Benefit_RepurchaseStrategyUserRelation","Benefit_ShelfRelated",
  "Benefit_SpmInfo","Benefit_SupplierUrgeHist","Benefit_ThirdBusiness","Benefit_TkProduct","Benefit_UserCard","Benefit_UserCardBag","BlacklistUser","BlogAccount","BlogAuthorityExtInfo","BlogAuthorityInfo",
  "BlogCount","BlogInfo","BlogMember","BlogMiscSetting","BlogSettings","BlogVip","Blog_FansVip","Blog_OfficialBlog","Blog_ProtectNew","Blog_RealInfo","Cdn_Domain","Cmb_BusinessIntroduction","Cmb_Category",
  "Cmb_Creator","Cmb_Ip","Cmb_MaterialReturn","Cmb_MyFollow","Cmb_ObserveTargetConfig","Cmb_Post","Cmb_Tag","Cmb_TagIp","CollectionInvitation","Collection_Incantation","Collection_JoinApply","Config_Bar",
  "Config_Bubble","Config_MyApp","Config_MyAppV2","Config_Tab","ConnectLogin","ConnectPhoneAccount","Corp_OpenTransferTrade","Corp_OpenUser","Creator_Announce","Creator_Banner","Creator_DataCenterConfig",
  "Creator_Module","Creator_TagFilter","Creator_TagRecommend","Creator_WeekReport","DeviceInfo","Device_IdfaImei","Dispatch_AutoProject","Dispatch_Notify","Dispatch_OperationLog","Dispatch_Permission",
  "Dispatch_Project","Dispatch_ProjectActivity","Dispatch_ProjectPost","Dispatch_ProjectProduct","Dispatch_ProjectStatHour","Dispatch_ProjectUser","Dispatch_ProjectUserPost","Dispatch_Question",
  "Dispatch_Role","Dispatch_RolePermission","Dispatch_SiteSupport","Dispatch_SiteSupportPost","Dispatch_SiteSupportStatHour","Dispatch_SiteSupportUser","Dispatch_User","Dispatch_UserRole","DomainAuthRule",
  "Dressing_Part","Dressing_Suit","Dressing_SuitExchange","Dressing_SuitGranted","Dressing_SuitOrder","Dressing_SuitReserve","Dressing_SuitSend","Dressing_SuitStock","Dressing_UserPart","Dressing_UserSuit",
  "ESign_AuthenticationRecord","EmoteDun","EmoteDunPai","EmoteDunPostStat","Emote_Info","Emote_Package","FavoriteTag","FilterPost","Forbid","ForbidRecord","ForbidTag","GiftPacks_ReceiveRecord","GrainCount",
  "Grain_Comment","Grain_CommentLike","Grain_CreateLog","Grain_FolderMigration","Grain_Follower","Grain_GreatPost","Grain_Incantation","Grain_Info","Grain_Officer","Grain_OfficerBanner","Grain_OfficerDeclare",
  "Grain_OfficerExchange","Grain_OfficerRecommend","Grain_Post","Grain_PublishUserLog","Grain_TagRelation","InterestDomainDaliyData","Ip_TagStat","LuckyBoy_Config","LuckyBoy_JoinRecord","LuckyBoy_Result",
  "MQ_TopicMessageRecord","Media_AccountImport","Media_OrgBindUser","Media_OrgIncomeStat","Media_OrgVideoMonthStatistic","Media_Org_Income","Media_PostBusinessRecord","Media_PostImport","Media_Similar_Post",
  "Media_VideoAdvert","Media_VideoFetch","Media_VideoUploadHist","Media_Video_Income_Stat","Media_Withdraw_Order","Media_YunMusicFetch","Mention","MessageSentFlag","Message_UserSetting","Mini_PhoneAccount",
  "Mini_Recommend","Mini_WxAccount","Money_Account","Money_LofterAuthorReward","Money_LofterDayBill","Money_LofterGiftAuthorMonthBill","Money_LofterIapBill","Money_LofterMonthBill","Money_StatConfig","MusicPost",
  "Nos_UploadHist","Nos_UploadHistBackup","Nos_UploadSceneConfig","Operation_Activity_Report","PayViewPost","Post","PostCollection","PostCollectionRecord","PostCollectionViewCount","PostCount",
  "PostResponse","Post_BlackHouse","Post_FansPost","Post_HighVideoPreview","Post_HotRankItem","Post_HotRankNavi","Post_Incantation","Post_RobotPromote","Post_VideoPreview","Post_VipView","Profile","PromoteWord",
  "Rank_ContentDetail","Rank_ContentList","RecommendAntispam","RecommendDomainTagMapping","RecommendPostQDResult","RecommendPostTraceLog","RecommendQuestionReviewLog","RecommendQuestionTraceLog","RecommendReviewCollection",
  "RecommendReviewDomain","RecommendReviewPost","RecommendReviewQuestion","RecommendReviewTag","RegisterInfo","RiskDock_UrlParamBlockRule","Risk_AntispamHistTask","Risk_AntispamImageCheck",
  "Risk_AntispamPost","Risk_AntispamPost_TMP","Risk_AntispamWhiteUser","Risk_Auditing_NoSearch","Risk_BanBindPhone","Risk_BlackPhone","Risk_BlackViolationReduceRecord",
  "Risk_DataApplication","Risk_GccPriority","Risk_LabelBlog","Risk_ModelAntispamTaskRelation","Risk_PostAuditMark","Risk_PostVerifyLog","Risk_SampleDataConfig","Robot_Intresting","Robot_Job","Robot_JobDetail","Robot_Task",
  "Search_RankItem","Search_RankItemForbid","Search_ResourceItem","Search_Sampling","ShortUrlRecord","SignAuthenticate","SiteConnectInfo","Sketch_ColorDownload","Sketch_ColorScheme","Sketch_ImageDownload",
  "Stat_LoginDaily","SubFolder_FolderFollower","SubFolder_TagFolderRelation","SubFolder_Theme","SubscribeCollection","TagChat_GoldenConfig","TagChat_HotRank","TagCount","TagJoined","TagResource","TagResourceCount",
  "Tag_ManageInfo","Tag_Manager","Tag_ManagerOperation","Timeline_Check_Task","Trade_AdRewardOrder","Trade_BlogVipOrder","Trade_BuyCoinOrder","Trade_CoinAvgOrder","Trade_CoinBalance","Trade_CoinBalanceLog",
  "Trade_CoinConsume","Trade_CoinProduct","Trade_FansVipAccount","Trade_FansVipApply","Trade_FansVipIncomeLog","Trade_FansVipOrder","Trade_FansVipProduct","Trade_FansVipSupportBlogStat","Trade_FreeGiftExchange",
  "Trade_GiftAccount","Trade_GiftAdminPresentDetail","Trade_GiftAdminPresentInfo","Trade_GiftBalance","Trade_GiftInfo","Trade_GiftMoneyBalanceLog","Trade_GiftOrder","Trade_GiftPayAccount",
  "Trade_GiftUserSetting","Trade_GiftWallet","Trade_GiftWithdrawAccountLog","Trade_GiftWithdrawAccountVerifyLog","Trade_MoneyBalanceLog","Trade_PayViewAuthor","Trade_PropsGiftPayConfig","Trade_PropsGiftProduct",
  "Trade_ReturnGiftCount","Trade_ReturnGiftFeedBack","Trade_ReturnGiftPlan","Trade_ReturnGiftType","Trade_SupportBlogCounter","Trade_SupportBlogStat","Trade_SupportItemCounter","Trade_SupportItemStat",
  "User","UserBlogAccount","UserGuide_IP","UserGuide_Interest","UserPostCollection","UserRecommendTagsNew","UserStatistic","UserSubBlogHist","User_ForbidTag","User_Certification",
  "User_CloseAccountLog","User_FavoriteIp","User_GuideData","User_Remark","VerifyPhoneAccount","VideoPost","Video_Post_Repair","Weibo_FetchContent","Weibo_FetchSetting","account_synchro","act_Activity_EffectBaseConfig",
  "act_Activity_EffectEventConfig","act_Activity_Menu","act_Activity_Operator","act_Activity_OperatorProductRela","act_Activity_OperatorRoleRela","act_Activity_Password","act_Activity_PasswordSugKeys","act_Activity_Product",
  "act_Activity_Role","act_Activity_RoleMenuRela","act_Activity_Scheme","act_Cp_UniverseCorpus","act_Creator_ScoreAccumulate","act_HPActivity_AwardMissionLog","act_HPActivity_Lottery","act_KeFu_FeedBackResponse",
  "act_KeFu_FeedBack_BingoKeyRecord","act_Luck_Activity","act_Luck_Chance","act_Luck_Prize","act_Luck_PrizeProbabilityStrategy","act_Luck_ThirdpartCode","act_MidFall_HighLevelUser","act_MidFall_ReceiverMessage",
  "act_MidFall_ReceiverWord","act_MidFall_SendRecord","act_MidFall_User","act_Mission","act_Mission_Activity","act_Mission_GlobalIdempotentId","act_Mission_PrizeMerge","act_Mission_UserState","act_PassCertificate",
  "act_Points_Idem","act_Points_PoolHistory","act_Prize_Idem","act_Prize_Item","act_Prize_Scheme","act_Prize_UserRecord","act_Prize_UserState","act_Reward_Activity","act_Reward_Bingo","act_Reward_UserPayInfo"
  ,"act_Scholar_Assist","act_Scholar_Exam","act_Scholar_HelpedAnswer","act_Scholar_IpRelatedWord","act_Scholar_Meeting","act_Scholar_Prize","act_Scholar_Question","act_Scholar_UserStat","act_backend_message_push_record",
  "act_goods","act_graduate_info","anonymity_login","authenticate_blog","benefit_adTrace_config","benefit_cart","benefit_category","benefit_category_product_relation","benefit_coupon","benefit_coupon_product",
  "benefit_coupon_user","benefit_exchange_order","benefit_new_coupon","benefit_order","benefit_order_product","benefit_product_attribute","benefit_product_domain","benefit_product_info","benefit_product_stock",
  "benefit_refund","benefit_supplier_info","benefit_trade","benefit_userscore","consign","darwin_mentee","darwin_post_lesson","device_access_info","explore_ready","explore_relation","forbidden_post",
  "forbidden_post_record","graduation_coin_detail","hot_list","hot_list_config","hot_list_tag","hot_search_key","ip_list","ip_tag","long_post","manual_recommend_blog","manual_recommend_blog_hist","phone_account",
  "phone_account_access_token","post_view_count","qixi_donate_coin_record","qixi_donate_info","recommend_auth","recommend_black","recommend_deliver_post","recommend_domain","recommend_domain_tag","recommend_favorite_deliver_post",
  "recommend_featured_content","recommend_product","recommend_product_config","recommend_tag_new","robot_blog_info","robot_reblog","shield_recom","tag_promotion","trade_avg_order","trade_copyright_order","trade_order",
  "trade_reward_author","trade_reward_order","trade_reward_post","trade_reward_user","trade_user_bind","trade_wallet","trade_withdraw_order","unauthenticate_blog","user_read_hist","user_recommend_log","user_subscribe_folder",
  "user_subscribe_post","vcloud_transfer","verify_blog","viewed_zipai_post","wechat_photo_post","wrap_tag","yin_coupon","yin_coupon_code","yin_coupon_product","yin_image_upload_hist","yin_order","yin_order_count","yin_order_product",
  "yin_product_info","zipai_post","Trade_CoinAvgOrder", "Pfb_ContentRelatedPost", "Pfb_Content","PaperMan_AutoDialogue","PaperMan_UserUnlockPlot","PaperMan_UserAutoDialogueRecord","PaperMan_UserDialogueNew","PaperMan_MainPlot",
  "trade_apple_pay_receipt", "benefit_app_receipt", "Trade_StoreVipOrder", "Trade_PostPackOrder", "Blog_StoreVip", "Blog_StoreCrowd", "SubFolder_Incantation", "User_ReadLog",
  "Partner_UserReward","Partner_WorkRecord","Partner_ApplyJob","Partner_ExamRecord","Partner_JobConfigs","Partner_JudgeComment","Partner_UserJudgedRecord","Partner_JudgeTask",
  "Money_StorePostMonthSales","Money_StoreContentMonthSales","Trade_UserCoupon","Pfb_BusinessShareConfig","Pfb_Provider","Pfb_ProviderSettleConfig","Pfb_Contract","Trade_Coupon",
  "Money_StoreVipSplit","Stat_LoginInfo","Trade_Apple_StoreContract","Creator_CreateSubject","Trade_GiftActUserReward","Trade_StoreContract",
  "Trade_ReturnGiftExchangeRecord","Store_RankList","InfringePost","Tag_RankConfig", "Risk_PreviewAudit", "Post_PersonalPassed", "RecommendReviewPostGiftMark", "PComment_UserMark",
  "conf_abtest", "Trade_MiniProgram_Order", "Post_Pool", "Act_LighthouseUser", "Trade_ReturnGiftWipeLog", "Trade_BlackHomeLog",
  "MP_Resource","MP_OperationLog","MP_Version","MP_User","MP_Configuration","MP_Application", "Cmb_CrowdPackage", "Store_Post", "Trade_UserExchangeCoupon",
  "PaidContent_Activity", "PaidContent_ActivityReward", "PaidContent_ActivityProgressTask", "PaidContent_ActivityTaskUserState", "Trade_Coupon_Order",
  "PaperMan_BackendReward","PaperMan_UserDailyDialogue","PaperMan_Reserve","PaperMan_UserEasterEggShard","PaperMan_UserReadLog","PaperMan_UserStaminaLog","PaperMan_UserStaminaGroup","Trade_PaperManStaminaOrder",
  "Grain_PlanPost", "Emote_TagResource", "Dressing_TagResource","Post_HotHighPComment", "Risk_InspectionRecord",
  "PVE_RoleInfo","PVE_ActiveCorpus","PVE_DailyCorpus","PVE_UserInfo","PVE_UserStaminaLog","PVE_UserCorpusRecord","PVE_UserUnlockEvent","PVE_DailyOptional","PVE_InviteMessageRecord", "Trade_PVEStaminaOrder",
  "Trade_CollectionGiftOrder", "RecommendPostStateLog", "conf_multi_abtest", "Emote_UserFansVip", "Emote_UserItem",
  "Mini_Config","Rank_Activity","Rank_Prize","HongBao_Item","HongBao_UserBalance","HongBao_UserBalanceLog", "Trade_GrainGiftOrder","PaperMan_EasterEggPlot","PaperMan_Character","PaperMan_UserInfo",
  "Reward_InviteRecord","Reward_Good","Reward_UserBag","Reward_UserScore","Reward_UserScoreLog","Reward_UserScoreAvail","Reward_ExchangeRecord","Reward_UserAccess",
  "Trade_Coupon_Coin_Order", "Dispatch_TaskColumn", "Risk_BlogDispose", "Birthday_Party", "Column_Info", "Column_Model", "Birthday_PartyHotPrize",
  "Trade_KolCouponPackParticipation", "Blog_ImageProtectSetting", "ImageMarket_Product","ImageMarket_LootBox","ImageMarket_ProductItem","ImageMarket_LootBoxProductItem",
  "ImageMarket_PostProduct","ImageMarket_ProductTag","ImageMarket_ProductOrder","ImageMarket_UserProduct","ImageMarket_UserProductItem","ImageMarket_LootBoxOrder",
  "Trade_ImageProduct_Account","ImageMarket_ProductLootBox","ImageMarket_UserLootBox", "UserGuide_IPV2", "Trade_UserBlackHome", "Trade_BlackHomePost", "Conf_DispatchPlan", "Conf_DispatchSource", "invalid_nos_image",
  "Money_ContractMonthSales","Money_FansVipContentMonthSales","Money_GiftContentMonthSales", "Money_ContentMonthSales", "PVE_RoleDuplicate", "PVE_UserDuplicateDialogue",
  "Corp_OutUser","PVE_UserDuplicate","PVE_RoleProps","PVE_UserProps","PVE_UserPropsLog","PVE_UgcRoleImpression","PVE_UgcRoleSweetDialogue","PVE_ImpressionLike",
  "PVE_SweetDialogueExchange","PVE_SweetDialogueExchangeStamina","PVE_MessageUser","PVE_UserInspireRecommend","PVE_UgcRoleRecord","PVE_UserDailyGift","PVE_UserGrassLog",
  "Trade_Pay_Grade","Tc_Task", "Trade_CouponCardOrder", "AD_Position", "Money_StoreVipSplitSum", "Trade_FansVipOrderIncome_Daily",
  "Growth_UserScore", "NotifyMessageContent", "EmoteDunPostStat", "Vote_Option", "Vote_UserRecord", "User_ShowcaseConfig", "Trade_ExchangeCouponSKU", "User_ProductHistory",
  "Tag_RankBlack", "Trade_GiftAccount", "Ask_ScoreItemBlog", "Ask_ScoreQuestionItem", "Ask_UserItemScore", "Deity_Recommend", "PVE_UserTimedStamina", "PVE_UserTimedStaminaLog",
  "Vote_Info", "PVE_Encounter", "PVE_RoleGroupDialogue", "PVE_RoleGroupMember", "PVE_ModelControlLog", "Trade_Pay_CreatorIdea", "Ask_WordQuestionStat", "Tag_GroupTask", "Tag_GroupTaskUser",
  "app_banner", "Money_BenefitSupplierSettlement", "Money_BenefitSupplierTaxInvoice", "Money_BenefitSupplierDaySellNew", "Money_BenefitSupplierDayPreSaleSell",
  "Ab_App", "Ab_Exp", "Ab_Exp_Group", "Ab_Exp_Metric", "Ab_Flow_Domain", "Ab_Flow_Layer", "Ab_Flow_Scene", "Ab_Metric_Business_Line", "Ab_Metric_Detail", "Ab_Metric_Dim", "Ab_Metric_Scene", "Ab_White_List",
  "AD_StatDetailDaily", "AD_Form", "Benefit_IpSeriesRelation", "Trade_FansPrivate_Account", "DeviceCaid", "Risk_AntispamFakePassPost",
  "PVE_ActivityReward", "PVE_UserActivityReward", "PVE_UserScoreNew", "PVE_UserScoreLog", "AD_DspPosition", "Pfb_ProviderRelatedBlog", "Blog_FansPrivate", "Trade_FansPrivate_Condition",
  "Post_FansPost", "Ask_FansQuestion", "PVE_CpActRecord", "PaidContent_ActivityAcquisitionRecord", "Trade_GiftActUserReward", "BlacklistUser", "RecommendReviewConfig", "RecommendUserMark",
  "PVE_RoleGroup", "PhotoPostHiddenTagRecord", "Cmb_PackDailyStatistic", "PVE_DressupExchangeLog", "PVE_ActZhmStyleExchange", "Trade_CommentatorReward", "Post_ViolationInfo",
  "Partner_IpTagConfig", "AD_DspReportDaily", "PVE_UserPropsGift", "Ask_QuestionTaskRecord", "Reward_CommonAdTask", "PVE_VcAppRegisterSignUpRecord", "PVE_SkinHungerDetermineLog",
  "Ask_ChatTagTopConfig", "PVE_UserBind", "Trade_HighNetWorthRightRecord", "Trade_FluencyReadOrder", "Blog_PayCreator_Warn", "Message_Yunxin_Msg", "Message_Group_Apply", "Message_Group_Member",
  "Post_ImageProtectSetting", "Post_ImgProtectSetting", "Message_Group_Info", "Trade_ReturnGiftExchangeRecordTemp",
  "exchange_task", "User_TagInfo", "Trade_LovebrushData", "Message_Group_NewPostIn_Info", "Message_Group_Member", "Oc_Item", "Challenge_Tag",
  "Oc_ItemStory", "Oc_ItemPhoto", "Blog_PayCreator_Warn", "Ab_Exp_Event_Group", "AD_PositionGroup", "Trade_HighNetWorthLevelTask", "User_RealInfo", "Tag_UserShipInfo", "Tag_UserShipInfoLog",
  "PaidContent_CheckInRecord", "PaidContent_ActivityTaskProcessRecord", "Trade_TransferOrder", "Trade_TransferOrderLog",
  "Rta_CreatorAdSwitch", "Rta_AdPostPool", "User_WgtConfig", "Live_GiftIncome", "Live_Record", "Live_Forbidden", "Live_UnionAnchor",
  "Post_MembershipPost", "Membership_RankDirectory", "Member_rankMatrixItem", "Trade_MemberShipOrder", "Post_Score_Competition_Score_Record",
  "Membership_IpStats", "Member_rankMatrixItem", "Membership_Entitlement", "Rta_GroupPostPkg", "Creator_GrowthNewInfo", "BrushHot_User", "PVE_GroupInviteMessageRecord",
  "Money_LofterCreatorMonthBill", "User_ReadLater", "Trade_AdRewardComplete", "AD_DspApp", "Cio_GoodBlogManualTag", "Trade_InspireMoneyLog",
  "QueuePost", "Tag_TabConfig", "Audio_Collection", "Audio_Post", "Audio_PostTone", "Audio_PostParagraph", "Audio_PostParagraphTone",
  "Rta_PostBlockAd", "Derive_DeliverOrder", "Derive_DeliverOrderDetail", "Derive_Partner", "Derive_PartnerDiscount", "Derive_PurchaseAfterSaleOrder", "Derive_PurchaseOrderItem", "Derive_PurchaseAfterSaleDetail", "Derive_Sku",
  "Trade_BlogSettlementLog", "Trade_Pay_Grade_Change_Hist", "ImageMarket_ProductDateSell", "Trade_MembershipPostRevenue", "Trade_TaxEngineData",
  "C2C_ProductInfo", "C2C_ProductAuditInfo", "C2C_ProdurcAuditLog", "C2C_ProductSaleInfo", "C2C_TradeOrder", "C2C_TradeOrderProduct",
  "C2C_PayOrder", "C2C_TradeOrderNode", "C2C_TradeOrderChangeLog", "C2C_ReverseOrder", "C2C_RefundOrder", "C2C_ProductPostMapInfo",
  "Trade_PostRevenue", "Trade_AdUnlockUserSetting", "Trade_AdUnlockUserSettingLog", "Trade_GiftBalanceLog", "C2C_Merchant", "Trade_InspireWallet",
  "Trade_AdUnlockPostSetting", "Rta_ConfigPostPackage",
  "Benefit_IpSeries", "Derive_PurchaseOrder", "Risk_EngineModel", "Risk_EngineRule", "Risk_EngineActivation", "Risk_EngineAbstraction",
  "Reward_ParticularScoreTask", "Reward_ContinueScoreTask", "Reward_UserBenefitPeriod", "Reward_UserTaskReward", "Reward_UserTask",
  "Reward_TaskStage", "Reward_TaskConfig", "CB_CreatorFollowRelation", "Dispatch_Placement", "Dispatch_ResourceSlot", "CB_CreatorConnection",
  "CB_CreatorTag", "CB_CreatorTagBinding", "CB_AiCreator", "C2C_ProductSpuInfo", "trade_candy_quota_post", "CB_AiCreatorStatus", "Ask_ChallengePost",
  "Tag_TabContent", "MH_ControlRecord", "MH_Exemption", "Partner_JudgeTask", "Post_SerialChapter", "C2C_MerchantApply", "CB_AiAppeal",
  "Trade_AdGame", "Trade_AdGamePickedImage", "Trade_AdGameUserRecord", "Trade_AdGameActionLog", "Trade_AdGameRewardComplete", "Trade_AdRewardBuffer",
  "Money_AlipayTradeBillDetail", "Money_AlipayLedgerBillDetail", "Money_WxpayDayBillDetail"
)

tableFilter in generateJob := {
  case "SharePost" | "comment_hot" | "PhotoPost" | "PostHot" | "TextPost" | "UserFollowing" | "Trade_UserFreeGift" | "RecommendPostReviewLog" | "User_CloseAccountDataBak" |
       "Risk_AntispamResponse" | "Risk_AntispamPost_TMP2" | "Risk_AntispamCallbackRecord" | "Risk_AntispamPostImage" | "Trade_SupportRecord" |
       "PVE_UserDialoguePartition" | "Trade_GiftPresentRecord" | "PVE_UserMaleVirtuePrisonLog" => false
  case tableName if tableSet(tableName) => true
  case _ => false
}

highPriorTables in generateJob := Seq(
  "Post", "Profile", "Pfb_ContentRelatedPost", "Pfb_Content", "AdminPubData", "Risk_GccPriority", "Media_PostImport",
  "Ask_AnswerPost", "Ask_Question", "BlogInfo", "BlogSettings", "authenticate_blog", "Blog_OfficialBlog",
  "anonymity_login", "robot_blog_info", "Media_AccountImport", "Ask_UserItemScore", "RecommendReviewPost",
  "RecommendReviewQuestion", "Trade_GiftAccount", "Trade_GiftPayAccount", "Trade_GiftInfo", "Trade_ReturnGiftPlan",
  "Trade_GiftUserSetting", "Cmb_BusinessIntroduction", "Cmb_BusinessIntroduction", "Trade_FansVipAccount", "Post_Pool",
  "Trade_ReturnGiftType", "PostCount", "PostCollection", "Cmb_Tag", "Cmb_Ip", "ActivityTag", "Cmb_Category", "Cmb_TagIp",
  "recommend_domain", "recommend_domain_tag", "benefit_supplier_info", "benefit_product_info", "Benefit_ProductCmbIpRelation",
  "Benefit_CardActivity", "Benefit_CardPool", "benefit_category_product_relation", "benefit_category", "Post_MembershipPost",
  "Grain_Info", "user_subscribe_folder", "Acquisition_Incantation", "Grain_Incantation", "SubFolder_Incantation", "Collection_Incantation", "Post_Incantation",
  "C2C_ProductInfo", "C2C_ProductPostMapInfo",
  "UserBlogAccount", "user_subscribe_folder", "Trade_Coupon_Coin_Order", "Trade_FansVipOrder", "Trade_StoreVipOrder", "Trade_BlogVipOrder", "Trade_MiniProgram_Order",
  "Trade_PVEStaminaOrder", "PVE_RoleInfo", "Trade_PaperManStaminaOrder", "Avatar_BoxOrder", "Dressing_SuitOrder",
  "benefit_category_product_relation", "ConnectPhoneAccount", "ConnectLogin",
  "trade_reward_author", "FavoriteTag", "benefit_order", "benefit_order_product", "verify_blog", "UserStatistic", "trade_reward_order", "PostCollectionRecord",
  "Media_VideoFetch", "Media_YunMusicFetch", "benefit_trade", "AuditLog", "Live_Record", "Live_GiftIncome", "benefit_new_coupon",
  "Forbid", "Trade_BuyCoinOrder", "Trade_CoinAvgOrder", "SubFolder_Incantation","Trade_Coupon", "Store_RankList","InfringePost","Tag_RankConfig", "Post_PersonalPassed",
  "conf_abtest", "Grain_PlanPost", "Benefit_CardActivity",
  "Dispatch_ProjectPost", "Dispatch_SiteSupportPost", "Dispatch_Project", "Dispatch_SiteSupport", "PaperMan_UserDailyDialogue",
  "VideoPost", "Corp_OutUser", "Trade_CouponCardOrder", "AD_Position", "Tag_RankBlack", "PostResponse", "PVE_UserDuplicate", "PVE_UserStaminaLog", "PVE_UserDuplicateDialogue",
  "PVE_DailyCorpus", "Trade_AdRewardOrder",  "Trade_SupportItemStat", "trade_order", "benefit_coupon_user",
  "Trade_GiftOrder", "Trade_CoinBalanceLog", "Trade_CoinConsume", "Trade_SupportBlogStat", "Trade_UserExchangeCoupon",
  "Trade_ReturnGiftExchangeRecord", "Trade_Coupon_Order", "RecommendReviewTag", "RecommendDomainTagMapping", "User_ForbidTag", "Trade_AdUnlockPostSetting",
  "AD_DspApp", "AD_DspPosition"
)

lowPriorTables in generateJob := Seq("Account", "ShortUrlRecord")

tableGroupSize in generateJob := 30
hiveSchema in generateJob := "lofter_db_dump"
hiveTableMapper in generateJob := {
  case "Act_LighthouseUser" => s"ods_db_act_lighthouse_user_v2_nd"
  case table => s"ods_db_${snakify(table)}_nd"
}

binlogTableMapper in generateJob := {
  case table => s"lofter.ods_binlog_${snakify(table)}_di"
}
