# Quota & Key Delegation Rollback Plan

**Last Updated:** 2026-03-26  
**Feature:** Key Delegation and Quota Enforcement (`key-delegation-and-quota`)  
**Scope:** Complete mapping of all quota-related changes

---

## 1. ENTIRELY NEW FILES (Must Delete)

These files were created exclusively for the quota feature and have no prior history:

### 1.1 Core Quota Library
- **`src/lib/quota.ts`** [ENTIRE FILE NEW]
  - Functions: `getPeriodStart()`, `checkQuota()`, `incrementUsage()`
  - Handles quota period calculation and enforcement
  - Contains: ~92 lines of quota logic

### 1.2 Database Schema Changes
- **`prisma/schema.prisma`** [MODIFIED - See Section 3]
  - New Enum: `QuotaPeriod` (lines 27-31)
  - New Model: `KeyUsage` (lines 119-132)
  - Modified Model: `ApiKey` (added fields at lines 76-78)

### 1.3 OpenSpec Documentation
- **`openspec/changes/key-delegation-and-quota/`** [ENTIRE DIRECTORY NEW]
  - `.openspec.yaml` - Spec metadata
  - `design.md` - Design decisions and architecture
  - `proposal.md` - Feature proposal and capabilities
  - `tasks.md` - Implementation task list
  - `specs/quota-enforcement/spec.md` - Quota enforcement spec
  - `specs/key-delegation/spec.md` - Key delegation spec

---

## 2. NEW HELPER FUNCTIONS (Must Remove)

### 2.1 Admin Auth Helper
**File:** `src/lib/admin-auth.ts`  
**Function to Remove:** `getAdminRole(request?: Request)` (lines 74-77)
- Returns current logged-in user's role for permission checks
- Used in Admin API key creation to enforce `target_name` restrictions

### 2.2 Request Logger Callback Type
**File:** `src/lib/request-logger.ts`  
**Changes to Revert:**
- Remove `OnUsage` type definition (line 23)
- Remove `onUsage?: OnUsage` parameter from `logNonStreaming()` (line 54)
- Remove `onUsage` invocation in `logNonStreaming()` (lines 67-68)
- Remove `onUsage?: OnUsage` parameter from `logStreaming()` (line 87)
- Remove `onUsage` invocation in `logStreaming()` (lines 118-119)

---

## 3. MODIFIED FILES (Mixed New + Existing Code)

### 3.1 Prisma Schema
**File:** `prisma/schema.prisma`

**New Enum (Lines 27-31):**
```prisma
enum QuotaPeriod {
  day
  month
  forever
}
```
→ **Remove entirely**

**Modified ApiKey Model (Lines 76-78):**
```prisma
quotaTokens   Int?         @map("quota_tokens")
quotaRequests Int?         @map("quota_requests")
quotaPeriod   QuotaPeriod? @map("quota_period")
```
→ **Remove these three fields**

**New KeyUsage Model (Lines 119-132):**
```prisma
model KeyUsage {
  id           BigInt   @id @default(autoincrement())
  apiKeyId     BigInt   @map("api_key_id")
  periodStart  DateTime @map("period_start") @db.Date
  usedTokens   Int      @default(0) @map("used_tokens")
  usedRequests Int      @default(0) @map("used_requests")
  updatedAt    DateTime @updatedAt @map("updated_at")

  apiKey ApiKey @relation(fields: [apiKeyId], references: [id])

  @@unique([apiKeyId, periodStart], map: "uq_key_usage_key_period")
  @@index([apiKeyId], map: "idx_key_usage_api_key_id")
  @@map("ai_key_usages")
}
```
→ **Remove entirely**

**Modified ApiKey Relation (Line 86):**
- Added: `keyUsages   KeyUsage[]`
→ **Remove this line**

### 3.2 Cache Types
**File:** `src/lib/cache.ts`

**Type Extensions (Lines 13-16):**
```typescript
/** 配额设置（null 表示无限制） */
quotaTokens: number | null;
quotaRequests: number | null;
quotaPeriod: "day" | "month" | "forever" | null;
```
→ **Remove these four lines from `CachedKeyData` type**

### 3.3 Auth Key Validation
**File:** `src/lib/auth-key.ts`

**Lines 37-39: Quota Field Assignment**
```typescript
quotaTokens: keyRecord.quotaTokens,
quotaRequests: keyRecord.quotaRequests,
quotaPeriod: keyRecord.quotaPeriod as "day" | "month" | "forever" | null,
```
→ **Remove these three lines from the `data` object construction**

### 3.4 Proxy Route Handler
**File:** `src/app/v1/[...path]/route.ts`

**Lines 9: Import Statement**
```typescript
import { checkQuota, incrementUsage } from "@/lib/quota";
```
→ **Remove this import**

**Lines 80-87: Quota Check Step**
```typescript
// ── Step 2.5: 配额检查 ────────────────────────────────────────────
const quotaExceeded = await checkQuota(keyData);
if (quotaExceeded) {
  return Response.json(
    { error: quotaExceeded },
    { status: 429, headers: corsHeaders }
  );
}
```
→ **Remove entire Step 2.5 block**

**Lines 155-162: Quota Usage Callback**
```typescript
// 配额更新辅助（仅在有 quotaPeriod 时才执行）
const maybeIncrementUsage = (tokens: number) => {
  if (keyData.quotaPeriod) {
    incrementUsage(keyData.apiKeyId, keyData.quotaPeriod, tokens).catch(
      (err) => logger.warn({ err }, "Failed to increment quota usage")
    );
  }
};
```
→ **Remove entirely**

**Lines 167, 182: Callback Usage in Log Functions**
```typescript
// In logStreaming call:
logStreaming(logCtx, upstream.status, logStream, maybeIncrementUsage);

// In logNonStreaming call:
logNonStreaming(logCtx, upstream.status, parsedBody, maybeIncrementUsage);
```
→ **Change calls to remove the callback parameter:**
```typescript
logStreaming(logCtx, upstream.status, logStream);
logNonStreaming(logCtx, upstream.status, parsedBody);
```

### 3.5 Admin Keys API
**File:** `src/app/api/admin/keys/route.ts`

**Line 5: Import Statement**
```typescript
import type { ApiKeyType, QuotaPeriod } from "@/generated/prisma";
```
→ **Change to:**
```typescript
import type { ApiKeyType } from "@/generated/prisma";
```

**Lines 3: Additional Import**
```typescript
import { getAdminEmail, getAdminRole, requirePermission } from "@/lib/admin-auth";
```
→ **Remove `getAdminRole` from import:**
```typescript
import { getAdminEmail, requirePermission } from "@/lib/admin-auth";
```

**GET Handler - Lines 44-46: Select Fields**
```typescript
quotaTokens: true,
quotaRequests: true,
quotaPeriod: true,
```
→ **Remove these three lines from the select object**

**POST Handler - Lines 73-76: API Documentation**
```typescript
*   target_name?   string  -- admin only；为其他用户创建时的标识（邮箱/花名/工号等任意字符串）
*   quota_tokens?  number
*   quota_requests? number
*   quota_period?  "day" | "month" | "forever"
```
→ **Remove these documentation lines**

**POST Handler - Lines 87-90: Body Type Definition**
```typescript
target_name?: string;
quota_tokens?: number;
quota_requests?: number;
quota_period?: string;
```
→ **Remove these four properties from body type**

**POST Handler - Lines 107-116: Period Validation**
```typescript
// 验证 quota_period 枚举值
if (
  body.quota_period !== undefined &&
  !["day", "month", "forever"].includes(body.quota_period)
) {
  return json(
    { error: 'quota_period must be "day", "month", or "forever"' },
    { status: 400 }
  );
}
```
→ **Remove entire validation block**

**POST Handler - Lines 119: Role Check**
```typescript
const adminRole = await getAdminRole(req);
```
→ **Remove this line**

**POST Handler - Lines 121-127: Admin-only target_name Check**
```typescript
// target_name：只有 admin 可以为他人创建
if (body.target_name !== undefined && adminRole !== "admin") {
  return json(
    { error: "只有管理员可以为其他用户创建 Key" },
    { status: 403 }
  );
}
```
→ **Remove entire block**

**POST Handler - Lines 129-157: target_name Logic**
```typescript
// 确定 key 归属的用户标识（personal key）
// - 未传 target_name → 为自己创建，使用当前用户邮箱
// - 传了 target_name → 为他人创建，使用 target_name
let name: string;
if (body.type === "personal") {
  if (body.target_name !== undefined) {
    if (!body.target_name.trim()) {
      return json({ error: "target_name 不能为空" }, { status: 400 });
    }
    name = body.target_name.trim();
  } else {
    if (!adminEmail) {
      return json(
        { error: "无法获取当前用户邮箱，请重新登录" },
        { status: 400 }
      );
    }
    name = adminEmail;
  }
} else {
  // service key：name 为业务系统名称，必填
  if (!body.name?.trim()) {
    return json(
      { error: "service key 需填写业务系统名称" },
      { status: 400 }
    );
  }
  name = body.name.trim();
}
```
→ **Replace with original behavior (name always from adminEmail for personal keys):**
```typescript
let name: string;
if (body.type === "service") {
  // service key：name 为业务系统名称，必填
  if (!body.name?.trim()) {
    return json(
      { error: "service key 需填写业务系统名称" },
      { status: 400 }
    );
  }
  name = body.name.trim();
} else {
  // personal key：name = current user email
  if (!adminEmail) {
    return json(
      { error: "无法获取当前用户邮箱，请重新登录" },
      { status: 400 }
    );
  }
  name = adminEmail;
}
```

**POST Handler - Lines 178-181: Quota Fields**
```typescript
// quota 字段
const quotaTokens = body.quota_tokens ?? null;
const quotaRequests = body.quota_requests ?? null;
const quotaPeriod = (body.quota_period as QuotaPeriod) ?? null;
```
→ **Remove entirely**

**POST Handler - Lines 194-197: Select Fields**
```typescript
quotaTokens: true,
quotaRequests: true,
quotaPeriod: true,
```
→ **Remove these three lines from keySelect object**

**POST Handler - Lines 218-224: UPSERT Data (update)**
```typescript
description: body.description ?? null,
quotaTokens,
quotaRequests,
quotaPeriod,
status: 1,
```
→ **Remove quota fields:**
```typescript
description: body.description ?? null,
status: 1,
```

**POST Handler - Lines 242-245: CREATE Data**
```typescript
description: body.description ?? null,
quotaTokens,
quotaRequests,
quotaPeriod,
createdBy: adminEmail ?? undefined,
```
→ **Remove quota fields:**
```typescript
description: body.description ?? null,
createdBy: adminEmail ?? undefined,
```

### 3.6 Admin Keys Page (UI)
**File:** `src/app/(admin)/keys/pane 20: Period Label Map**
```typescript
const PERIOD_LABEL: Record<string, string> = { day: "天", month: "月", forever: "永久" };
```
→ **Remove this line**

**Lines 22-33: formatQuota Helper**
```typescript
function formatQuota(
  tokens: number | null,
  requests: number | null,
  period: string | null
): string {
  if (!period) return "—";
  const parts: string[] = [];
  if (requests != null) parts.push(`${requests.toLocaleString()} 次`);
  if (tokens != null) parts.push(`${tokens.toLocaleString()} tokens`);
  if (parts.length === 0) return "—";
  return `${parts.join(" / ")} / ${PERIOD_LABEL[period] ?? period}`;
}
```
→ **Remove entire function**

**Lines 74-76: Quota Select Fields**
```typescript
quotaTokens: true,
quotaRequests: true,
quotaPeriod: true,
```
→ **Remove these three lines from select object**

**Line 91: isAdmin Calculation**
```typescript
const isAdmin = role === "admin";
```
→ **Remove this line**

**Line 109: isAdmin Prop to Dialog**
```typescript
{canManage && <CreateKeyDialog apps={apps} isAdmin={isAdmin} />}
```
→ **Change to:**
```typescript
{canManage && <CreateKeyDialog apps={apps} />}
```

**Line 148: "限额" Column Header**
```typescript
<TableHead>限额</TableHead>
```
→ **Remove this entire line**

**Lines 176-178: Quota Display Cell**
```typescript
<TableCell className="text-sm text-muted-foreground whitespace-nowrap">
  {formatQuota(key.quotaTokens, key.quotaRequests, key.quotaPeriod)}
</TableCell>
```
→ **Remove entire cell**

### 3.7 Create Key Dialog (UI Component)
**File:** `src/app/(admin)/keys/create-key-dialog.tsx`

**Line 19-24: Period Labels Constant**
```typescript
const PERIOD_LABELS: Record<string, string> = {
  "": "无限制",
  day: "每天",
  month: "每月",
  forever: "永久",
};
```
→ **Remove entire constant**

**Lines 26-32: isAdmin Prop**
```typescript
export function CreateKeyDialog({
  apps,
  isAdmin = false,
}: {
  apps: App[];
  isAdmin?: boolean;
}) {
```
→ **Change to:**
```typescript
export function CreateKeyDialog({
  apps,
}: {
  apps: App[];
}) {
```

**Lines 42-48: Form State - Delegation & Quota Fields**
```typescript
// delegation
forOther: false,
target_name: "",
// quota
quota_period: "" as "" | "day" | "month" | "forever",
quota_requests: "",
quota_tokens: "",
```
→ **Remove these seven lines**

**Lines 59-64: Payload Type - Delegation & Quota Fields**
```typescript
description?: string;
name?: string;
target_name?: string;
quota_period?: string;
quota_requests?: number;
quota_tokens?: number;
```
→ **Change to:**
```typescript
description?: string;
name?: string;
```

**Lines 76-84: Form Submission - Delegation & Quota Handling**
```typescript
if (form.type === "personal" && form.forOther && form.target_name.trim()) {
  payload.target_name = form.target_name.trim();
}

if (form.quota_period) {
  payload.quota_period = form.quota_period;
  if (form.quota_requests) payload.quota_requests = parseInt(form.quota_requests, 10);
  if (form.quota_tokens) payload.quota_tokens = parseInt(form.quota_tokens, 10);
}
```
→ **Remove entirely**

**Lines 105-114: Form Reset - Delegation & Quota Fields**
```typescript
forOther: false,
target_name: "",
quota_period: "",
quota_requests: "",
quota_tokens: "",
```
→ **Remove these five lines from reset object**

**Lines 204-242: Personal Key Admin Delegation UI**
```typescript
{/* Personal key：admin 可选择为他人创建 */}
{form.type === "personal" && isAdmin && (
  <div className="space-y-2">
    <Label>创建对象</Label>
    <div className="flex gap-3">
      {[false, true].map((isOther) => (
        <label
          key={String(isOther)}
          className="flex items-center gap-1.5 cursor-pointer text-sm"
        >
          <input
            type="radio"
            name="forOther"
            checked={form.forOther === isOther}
            onChange={() =>
              setForm({ ...form, forOther: isOther, target_name: "" })
            }
            className="accent-primary"
          />
          {isOther ? "为其他用户创建" : "为自己创建"}
        </label>
      ))}
    </div>
  </div>
)}

{/* 为他人创建：用户标识 */}
{form.type === "personal" && form.forOther && (
  <div className="space-y-2">
    <Label htmlFor="target_name">用户标识</Label>
    <Input
      id="target_name"
      placeholder="邮箱 / 花名 / 工号"
      value={form.target_name}
      onChange={(e) => setForm({ ...form, target_name: e.target.value })}
      required
    />
  </div>
)}
```
→ **Remove entire sections (39 lines)**

**Lines 255-317: Quota Configuration UI**
```typescript
{/* 配额设置 */}
<div className="space-y-3 rounded-md border border-dashed p-3">
  <Label className="text-xs text-muted-foreground uppercase tracking-wide">
    配额设置（可选）
  </Label>
  <div className="space-y-2">
    <Label htmlFor="quota_period" className="text-sm">周期</Label>
    <select
      id="quota_period"
      className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm"
      value={form.quota_period}
      onChange={(e) =>
        setForm({
          ...form,
          quota_period: e.target.value as typeof form.quota_period,
          quota_requests: "",
          quota_tokens: "",
        })
      }
    >
      {Object.entries(PERIOD_LABELS).map(([v, label]) => (
        <option key={v} value={v}>
          {label}
        </option>
      ))}
    </select>
  </div>

  {form.quota_period && (
    <div className="grid grid-cols-2 gap-3">
      <div className="space-y-2">
        <Label htmlFor="quota_requests" className="text-sm">
          最大请求数
        </Label>
        <Input
          id="quota_requests"
          type="number"
          min={1}
          placeholder="不限"
          value={form.quota_requests}
          onChange={(e) =>
            setForm({ ...form, quota_requests: e.target.value })
          }
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="quota_tokens" className="text-sm">
          最大 Tokens
        </Label>
        <Input
          id="quota_tokens"
          type="number"
          min={1}
          placeholder="不限"
          value={form.quota_tokens}
          onChange={(e) =>
            setForm({ ...form, quota_tokens: e.target.value })
          }
        />
      </div>
    </div>
  )}
</div>
```
→ **Remove entire quota settings section (63 lines)**

### 3.8 Generated Prisma Types
**File:** `src/generated/prisma/index.d.ts`

This file is auto-generated. It will be regenerated after schema changes are reverted, so no manual edits needed. However, note that it currently contains:
- `KeyUsage` model definition
- `QuotaPeriod` enum definition
- `ApiKey` model with quota fields

These will all be removed when `npx prisma db push` is run after reverting schema.prisma.

---

## 4. DATABASE MIGRATION STEPS

After rolling back code changes:

```bash
# 1. Revert schema.prisma to pre-quota version
# (Remove: QuotaPeriod enum, KeyUsage model, ApiKey quota fields)

# 2. Create Prisma migration to drop new tables and columns
npx prisma migrate dev --name rollback_quota

# 3. This will:
#    - Drop ai_key_usages table (KeyUsage model)
#    - Drop quota_tokens, quota_requests, quota_period columns from ai_api_keys
#    - Remove QuotaPeriod enum from database

# 4. Regenerate Prisma client types
npx prisma generate
```

**Database changes summary:**
- **Table to drop:** `ai_key_usages`
- **Columns to drop from `ai_api_keys`:**
  - `quota_tokens`
  - `quota_requests`
  - `quota_period`
- **Enum to drop:** `QuotaPeriod` (if supported by MySQL dialect)

---

## 5. VERIFICATION CHECKLIST

After rollback completion, verify:

- [ ] `src/lib/quota.ts` is deleted
- [ ] All imports of `checkQuota` and `incrementUsage` are removed
- [ ] No references to `quotaTokens`, `quotaRequests`, `quotaPeriod` in code
- [ ] `CachedKeyData` type no longer includes quota fields
- [ ] `src/app/v1/[...path]/route.ts` Step 2.5 is removed
- [ ] `maybeIncrementUsage` callback is removed from route handler
- [ ] Admin API `/api/admin/keys` POST accepts only non-quota parameters
- [ ] Admin UI keys page table doesn't show "限额" column
- [ ] Create key dialog doesn't show quota or delegation UI
- [ ] `src/lib/admin-auth.ts` no longer exports `getAdminRole`
- [ ] `src/lib/request-logger.ts` doesn't have `OnUsage` parameter
- [ ] `prisma/schema.prisma` no longer has `QuotaPeriod` enum or `KeyUsage` model
- [ ] `prisma/schema.prisma` `ApiKey` model doesn't have quota fields
- [ ] Database migration applied: `ai_key_usages` table dropped
- [ ] Database migration applied: quota columns removed from `ai_api_keys`
- [ ] `npx tsc --noEmit` passes with no errors
- [ ] `npx prisma generate` completes successfully

---

## 6. ROLLBACK SCRIPT (Optional)

If needed, create a rollback script:

```bash
#!/bin/bash
# rollback-quota.sh

echo "Rolling back quota feature..."

# 1. Revert schema.prisma (manually or with git)
echo "Step 1: Reverting prisma/schema.prisma..."
git checkout HEAD -- prisma/schema.prisma

# 2. Revert source files
echo "Step 2: Reverting source files..."
git checkout HEAD -- \
  src/lib/quota.ts \
  src/lib/cache.ts \
  src/lib/auth-key.ts \
  src/lib/admin-auth.ts \
  src/lib/request-logger.ts \
  src/app/v1/[...path]/route.ts \
  src/app/api/admin/keys/route.ts \
  src/app/\(admin\)/keys/page.tsx \
  src/app/\(admin\)/keys/create-key-dialog.tsx

# 3. Revert documentation
echo "Step 3: Removing openspec documentation..."
rm -rf openspec/changes/key-delegation-and-quota/

# 4. Database migration
echo "Step 4: Creating database rollback migration..."
npx prisma migrate dev --name rollback_quota_feature

# 5. Regenerate Prisma client
echo "Step 5: Regenerating Prisma client..."
npx prisma generate

# 6. Type check
echo "Step 6: Running type check..."
npx tsc --noEmit

echo "✅ Rollback complete!"
```

---

## 7. REFERENCE: Feature Implementation Summary

**Affected Code:** 9 files modified, 1 file created  
**Lines Added:** ~800 (excluding tests and docs)  
**Key Concepts:**
- Quota periods: `day` (UTC midnight), `month` (UTC 1st), `forever` (epoch: 1970-01-01)
- Quota check: Pre-request validation against `ai_key_usages` table
- Usage increment: Post-response async update via `OnUsage` callback
- Key delegation: Admin can create personal keys for other users via `target_name`
- LRU Cache: 5-minute TTL for cached key data (includes quota fields)

**Design Document:** `openspec/changes/key-delegation-and-quota/design.md`  
**Task Checklist:** `openspec/changes/key-delegation-and-quota/tasks.md`

---

## 8. RISK MITIGATION

**Risks during rollback:**
1. **Data loss:** Old cached quota data in Redis/memory will be cleared (OK, ephemeral)
2. **In-flight requests:** Any request in Step 2.5 (quota check) will be served by old code version
   - Mitigation: Perform rollback during low-traffic window
3. **Database schema:** Dropping columns is permanent
   - Mitigation: Ensure backup exists before migration
4. **TypeScript errors:** Missing imports may cause build failure
   - Mitigation: Run `npx tsc --noEmit` after each edit
5. **API compatibility:** Any clients relying on new quota fields will break
   - Mitigation: Communicate rollback plan to external API users

---

**Prepared by:** Quota Rollback Analysis  
**Feature Branch:** (based on git history)  
**Rollback Priority:** High (quota feature is new, early-stage)
