import type { DefaultSession } from "next-auth";
import type { UserRole } from "@/lib/permissions";

declare module "next-auth" {
  interface User {
    role?: UserRole;
  }

  interface Session {
    user: {
      role?: UserRole;
    } & DefaultSession["user"];
  }
}

// NextAuth v5 (beta) 使用 @auth/core/jwt 而非 next-auth/jwt
declare module "@auth/core/jwt" {
  interface JWT {
    role?: UserRole;
  }
}
