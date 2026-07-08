# LOFTER 权益中心资源位导量效果监控报表

> 原文链接: https://docs.popo.netease.com/lingxi/80f48b26c5324df098204ade96a85c14

# 一、目的

**权益中心日均13w日活，流动性强，目前各入口依靠人工跑数据获取，监控后置且效率低**

**需要有一张总览报表，监控权益中心各个导量入口的导量效率和后效，方便运营及时做优化策略**

> 导量效率：导量入口曝光用户量，点击用户量，权益中心访问用户量（点击率，到达率）
>
> 后效：广告观看率、商品兑换率、留存率

需要解决的前置问题：监控链路是否完整，如不完整需补齐埋点，并通过个性化url上报关联用户在权益中心的所有行为

# 二、报表需求

## 1、报表

|  |  |
| --- | --- |
|  | **数据总览**  1、总访问量：权益中心总访问量  2、总广告收益：权益中心总收益  3、APP7留：APP用户7留  4、权益中心7留：权益中心用户7留  5、访问用户来源分布：  来源类型：常驻，引流，召回；支持不区分，默认勾选不区分  资源位：详见附表3.1资源位清单  来源分布查看方式：  1、按来源类型分布（默认）  2、按资源位分布  3、筛选来源类型后，按资源位分布  用户需要去重统计，按用户首次访问来源计数  问题：如统计7天内用户来源分布，是否可按首次来源统计  6、收入用户来源分布：同5  7、留存曲线：权益中心次留、7留、30留（同时展示） |
| **各资源位导量效果监控**  1、左上角：筛选项  日期、来源类型、资源位、用户类别（详见底表和附表说明）  2、下方：折线图  横轴：日期  纵轴：监控指标数值  维度筛选器：支持按照维度展示，包含来源类型、资源位、用户类别  3、右侧：监控指标  单选，详见附表3.2监控指标汇总 |
| **商品兑换来源分布**  1、筛选项：  日期、商品一级分类、二级分类、商品名称、product\_id  2、饼图（2张图表）  来源类型、资源位名称 |

## 2、底表

底表行构成：用户id+时间（日维度）+用户来源（来源类型+资源位名称）+用户类别+监控指标

仅供参考，开发自行评估，保证报表能实现即可

### **2.1、url命名规范：**

https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=&rc\_source=&rc\_product\_id=

**权益中心地址****&来源类型****&资源位名称****&商品id**

### **2.2、用户来源：****@乃同(肖乃同)****确认是否符合规范**

#### 2.2.1、来源类型

|  |  |
| --- | --- |
| **来源类型** | **键值对** |
| 常驻 | rc\_source\_type=fixed |
| 引流 | rc\_source\_type=attract |
| 召回 | rc\_source\_type=recall |

#### 2.2.2、资源位名称

|  |  |
| --- | --- |
| **资源位名称** | **键值对** |
| 每日福利 | rc\_source=mine\_entrance |
| 我的账户-乐乎米 | rc\_source=mine\_account\_loftgrain |
| 我的账户-滚动banner | rc\_source=mine\_account\_banner |
| 礼物背包-粮票页 | rc\_source=giftbag\_grain\_ticket |
| 首页吸顶 | rc\_source=home\_upperleft |
| 信息流定坑 | rc\_source=explorefeednative\_11 |
| 订阅提醒 | rc\_source=rc\_subscribe |
| 私信 | rc\_source=privatemessage |
| push | rc\_source=push |
| 吸边 | rc\_source=home\_float |

### **2.3、定投商品：****@狂小猪(黄辰军)****确认**

热门兑换区商品依据url中rc\_product\_id展示，rc\_product\_id=111,222,333

### **2.4、用户类别：**

访问用户，新用户，回流用户，权益中心新用户，权益中心回流用户

### **2.5、监控指标：**

|  |  |
| --- | --- |
| **直接指标** | 曝光uv、点击uv、权益中心访问uv、广告观看uv、广告收益、商品兑换uv |
| **间接指标** | 点击率、到达率、广告观看率、商品兑换率、权益中心次留率、权益中心7留率、APP次留率、APP7留率、权益中心30日留存率、APP30日留存率 |

## 3、附表

### 3.1、资源位清单

客户端确认所有入口埋点上报url@雪明

|  |  |  |  |  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **序号** | **资源位名称** | **位置** | **来源类型** | **url** | **数据监控** | **跳转url** | **曝光埋点****@Forsta(盛雪明)** | **点击埋点****@Forsta(盛雪明)** | **曝光&导量量级** | **后效关联** |
| 1 | 每日福利 |  | 常驻 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=fixed&rc\_source=mine\_entrance | 长期追踪 | 可以单独设置，后台配置@庞超 | 有 | 有 | 1.7w | - [ ] |
| 2 | 我的账户-乐乎米 |  | 常驻 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=fixed&rc\_source=mine\_account\_loftgrain | 长期追踪 | 可以单独设置，改动代码@庞超 | 有 | 有 | 2.3w | - [ ] |
| 3 | 我的账户-滚动banner |  | 引流 | 粮票：https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=attract&rc\_source=mine\_account\_banner&rc\_product\_id=8001  糖果券：https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=attract&rc\_source=mine\_account\_banner&rc\_product\_id=60001,193801,194801 | 长期追踪 | 可以单独设置，  apollo配置@庞超 | 有 | 有 | 1.4w | - [ ] |
| 4 | 礼物背包-粮票页 |  | 常驻 | **需要前端替换：**https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=fixed&rc\_source=giftbag\_grain\_ticket&rc\_product\_id=8001 | 长期追踪 | 前端写死，需替换左边url@狂小猪(黄辰军) | @狂小猪(黄辰军)需要确认，如无埋点需增加 | @狂小猪(黄辰军)需要确认，如无埋点需增加 |  | - [ ] |
| 5 | 首页吸顶 |  | 引流 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=attract&rc\_source=home\_upperleft&rc\_product\_id=60001,193801,194801 | 长期追踪 | 运营配置 | 有 | 有 | 0.6w | - [ ] |
| 6 | 信息流定坑-11号位 |  | 常驻/引流 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=attract&rc\_source=explorefeednative\_11&rc\_product\_id=60001,193801,194801 | 长期追踪 | 运营配置, | 有 | 有 | 2.2w | - [ ] |
| 7 | 订阅提醒 |  | 引流/召回 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=recall&rc\_source=rc\_subscribe&rc\_product\_id=60001,193801,194801 | 长期追踪 | 运营配置 | 有 | 有 |  | - [ ] |
| 8 | 私信 |  | 引流/召回 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=recall&rc\_source=privatemessage&rc\_product\_id=60001,193801,194801 | 投放期 | 运营配置 | 有 | 有 |  | - [ ] |
| 9 | push |  | 引流/召回 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=recall&rc\_source=push&rc\_product\_id=60001,193801,194801 | 投放期 | 运营配置 | 有 | 有 |  | - [ ] |
| 10 | 吸边 |  | 引流 | https://www.lofter.com/front/homesite/reward-center?njb\_navigator=false&rc\_source\_type=attract&rc\_source=home\_float | 投放期 | 运营配置 | 有 | 有 | 0.4w | - [ ] |

### 3.2、监控指标汇总

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| 序号 | 指标名称 | 计算口径 | 去重规则 | 备注 |
| 1 | 曝光UV | 资源位展示给用户的去重人数 | 按自然天去重 |  |
| 2 | 点击UV | 点击资源位的去重人数 | 按自然天去重 |  |
| 3 | 点击率 | 点击uv /曝光uv × 100% |  |  |
| 4 | 权益中心访问UV | 通过该资源位实际到达权益中心页面的去重人数 | 按自然天去重 | 点击≠进入，需页面加载成功 |
| 5 | 到达率 | 进入权益中心UV / 点击UV × 100% | - |  |
| 6 | 广告观看UV | 在权益中心内完整观看任意一条广告的去重人数 | 按自然天去重 | 「完整观看」=看完一条广告 |
| 7 | 广告观看率 | 广告观看UV / 进入权益中心UV × 100% | - |  |
| 8 | 广告收益 | 导量用户在权益中心产生的广告收入（元） | - | ⚠️ 数据来源待开发确认 |
| 9 | 商品兑换UV | 使用乐乎米提交兑换商品的去重人数 | 按自然天去重 | 提交兑换即算 |
| 10 | 商品兑换率 | 商品兑换UV / 进入权益中心UV × 100% | - |  |
| 11 | 权益中心次留率 | 当天导量用户中，次日再次访问权益中心的用户比例 | - |  |
| 12 | 权益中心7留率 | 当天导量用户中，第7天再次访问权益中心的用户比例 | - |  |
| 13 | 权益中心30日留存率 | 当天导量用户中，第30天再次访问权益中心的用户比例 | - |  |
| 14 | APP次留率 | 当天导量用户中，次日打开APP的用户比例 | - |  |
| 15 | APP7留率 | 当天导量用户中，第7天打开APP的用户比例 | - |  |
| 16 | APP30日留存率 | 当天导量用户中，第30天打开APP的用户比例 | - |  |

# 三、待办

- [x] 1、数开+前端确认url 命名规范@乃同(肖乃同)@狂小猪(黄辰军)
- [ ] 2、前端+后端+运营按照规范配置url@狂小猪(黄辰军)@庞超@Yumeko(李梦涵)- [ ] 3、前端@狂小猪(黄辰军)补充礼物背包曝光&点击埋点，和url上报
- [ ] 4、客户端补充入口对应埋点信息@Forsta(盛雪明)，曝光+点击+url上报- [ ] 5、数开根据url关联用户权益中心行为，开发报表@乃同(肖乃同)
- [ ] 6、后续权益中心所有url配置统一收口到@Yumeko(李梦涵)