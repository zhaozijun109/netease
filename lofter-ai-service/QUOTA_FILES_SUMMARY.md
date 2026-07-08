# Quota Feature - Files & Changes Summary

**Generated:** 2026-03-26  
**Feature:** Key Delegation and Quota (`key-delegation-and-quota`)

---

## QUICK REFERENCE TABLE

| File Path | Status | Changes | Key References | Lines | Priority |
|-----------|--------|---------|-----------------|-------|----------|
| `src/lib/quota.ts` | ✅ NEW | Entire file | `checkQuota`, `incrementUsage` | ~92 | **CRITICAL** |
| `prisma/schema.prisma` | 🔄 MODIFIED | +Enum, +Model, +3 fields | `QuotaPeriod`, `KeyUsage` | 27-31, 76-78, 86, 119-132 | **CRITICAL** |
| `src/lib/cache.ts` | 🔄 MODIFIED | +4 type fields | `quotaTokens`, `quotaRequests`, `quotaPeriod` | 13-16 | HIGH |
| `src/lib/auth-key.ts` | 🔄 MODIFIED | +3 field assignments | `quotaTokens`, `quotaRequests`, `quotaPeriod` | 37-39 | HIGH |
| `src/lib/admin-auth.ts` | 🔄 MODIFIED | +1 new function | `getAdminRole` | 74-77 | MEDIUM |
| `src/lib/request-logger.ts` | 🔄 MODIFIED | +1 type, +2 params, +2 callbacks | `OnUsage`, `onUsage` | 23, 54, 67-68, 87, 118-119 | HIGH |
| `src/app/v1/[...path]/route.ts` | 🔄 MODIFIED | +import, +Step 2.5, +callback | `checkQuota`, `maybeIncrementUsage` | 9, 80-87, 155-162, 167, 182 | **CRITICAL** |
| `src/app/api/admin/keys/route.ts` | 🔄 MODIFIED | +imports, +validation, +logic | `target_name`, `quota_*`, `getAdminRole` | 3, 5, 44-46, 73-76, 87-90, 107-116, 119, 121-127, 129-157, 178-181, 194-197, 218-224, 242-245 | **CRITICAL** |
| `src/app/(admin)/keys/page.tsx` | 🔄 MODIFIED | +helper, +column, +logic | `formatQuota`, `quotaTokens`, `isAdmin` | 20, 22-33, 74-76, 91, 109, 148, 176-178 | HIGH |
| `src/app/(admin)/keys/create-key-dialog.tsx` | 🔄 MODIFIED | +prop, +form fields, +UI sections | `isAdmin`, `forOther`, `target_name`, `quota_*` | 19-24, 26-32, 42-48, 59-64, 76-84, 105-114, 204-242, 255-317 | HIGH |
| `src/generated/prisma/index.d.ts` | 🔄 AUTO-GEN | +TypeScript definitions | `KeyUsage`, `QuotaPeriod` | N/A | LOW |
| `openspec/changes/key-delegation-and-quota/` | ✅ NEW | Entire directory | Design docs, specs | 6 files | LOW |
| `docs/AIGW-产品手册.md` | 🔄 MODIFIED | Doc mentions | References quota concept | TBD | LOW |

**Legend:**
- ✅ = Entirely new file (DELETE)
- 🔄 = Modified file (EDIT/REVERT)
- **CRITICAL** = Affects core request flow, must be exact
- **HIGH** = Core feature logic, must remove cleanly
- **MEDIUM** = Helper functions, safe to remove
- **LOW** = Documentation only

---

## FILE-BY-FILE ROLLBACK GUIDE

### 1. DELETE ENTIRELY
```bash
rm -f src/lib/quota.ts
rm -rf openspec/changes/key-delegation-and-quota/
```

### 2. REVERT FROM GIT (All modified files)
```bash
git checkout HEAD -- \
  prisma/schema.prisma \
  src/lib/cache.ts \
  src/lib/auth-key.ts \
  src/lib/admin-auth.ts \
  src/lib/request-logger.ts \
  src/app/v1/[...path]/route.ts \
  src/app/api/admin/keys/route.ts \
  src/app/\(admin\)/keys/page.tsx \
  src/app/\(admin\)/keys/create-key-dialog.tsx
```

### 3. DATABASE MIGRATION
```bash
npx prisma migrate dev --name rollback_quota_enforcement
npx prisma generate
```

### 4. VERIFY
```bash
npx tsc --noEmit
npm run lint  # if configured
npm run test  # if tests exist
```

---

## QUOTA REFERENCE LOCATIONS

### Import Statements to Remove
```typescript
// src/app/v1/[...path]/route.ts - Line 9
import { checkQuota, incrementUsage } from "@/lib/quota";

// src/app/api/admin/keys/route.ts - Line 5
import type { ApiKeyType, QuotaPeriod } from "@/generated/prisma";

// src/app/api/admin/keys/route.ts - Line 3
import { getAdminEmail, getAdminRole, requirePermission } from "@/lib/admin-auth";
```

### Type Definitions to Remove
```typescript
// src/lib/request-logger.ts - Line 23
type OnUsage = (tokens: number) => void;

// src/lib/cache.ts - Lines 14-16
quotaTokens: number | null;
quotaRequests: number | null;
quotaPeriod: "day" | "month" | "forever" | null;
```

### Function Calls to Clean Up
```typescript
// src/lib/quota.ts
checkQuota(keyData)        // Remove all calls
incrementUsage(...)        // Remove all calls
getPeriodStart(period)     // Remove all calls

// src/app/v1/[...path]/route.ts
maybeIncrementUsage(tokens) // Remove callback definition
logStreaming(..., maybeIncrementUsage)    // Remove param
logNonStreaming(..., maybeIncrementUsage) // Remove param
```

### UI Components to Clean
```typescript
// src/app/(admin)/keys/create-key-dialog.tsx
PERIOD_LABELS constant           // Remove
isAdmin prop                     // Remove
forOther / target_name form      // Remove ~40 lines
quota_period / quota_* form      // Remove ~60 lines

// src/app/(admin)/keys/page.tsx
formatQuota() function           // Remove
"限额" table column             // Remove
isAdmin variable                // Remove
CreateKeyDialog isAdmin prop     // Remove param
```

---

## CONFIRMATION CHECKLIST

Run these commands to verify complete rollback:

```bash
# 1. Check no quota.ts exists
ls -la src/lib/quota.ts  # Should fail with "No such file"

# 2. Check no quotaTokens in schema
grep -n "quotaTokens\|QuotaPeriod\|KeyUsage" prisma/schema.prisma  # Should return 0 matches

# 3. Check no quota imports
grep -rn "from '@/lib/quota'" src/  # Should return 0 matches

# 4. Check no OnUsage type
grep -n "OnUsage" src/lib/request-logger.ts  # Should return 0 matches

# 5. Check no target_name in API
grep -n "target_name" src/app/api/admin/keys/route.ts  # Should return 0 matches

# 6. TypeScript check
npx tsc --noEmit  # Should pass with no errors

# 7. Database check (after migration)
npx prisma generate  # Should succeed
```

---

## RELATED DOCUMENTATION

- **Design**: `openspec/changes/key-delegation-and-quota/design.md`
- **Proposal**: `openspec/changes/key-delegation-and-quota/proposal.md`
- **Tasks**: `openspec/changes/key-delegation-and-quota/tasks.md`
- **Spec**: `openspec/changes/key-delegation-and-quota/specs/quota-enforcement/spec.md`

---

## KEY DECISIONS AFFECTED BY ROLLBACK

1. **Soft Quota Enforcement**: Configuration checks removed, no more 429 responses
2. **Usage Tracking**: `ai_key_usages` table will be dropped
3. **Admin Delegation**: `target_name` parameter will no longer be supported
4. **Cache Structure**: Quota fields removed from `CachedKeyData` type
5. **Request Logger**: No more usage callback hooks in log functions

---

**Total Files Changed:** 10 (1 new, 9 modified)  
**Estimated Rollback Time:** 30-45 minutes (including testing)  
**Risk Level:** LOW (early-stage feature, no production data dependency)
