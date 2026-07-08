"use client";

import { useSearchParams, useRouter, usePathname } from "next/navigation";
import { useCallback, useRef } from "react";
import { Input } from "@/components/ui/input";
import { Search, X } from "lucide-react";

export function SearchInput({ placeholder = "搜索…" }: { placeholder?: string }) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const updateSearch = useCallback(
    (value: string) => {
      const params = new URLSearchParams(searchParams.toString());
      if (value) {
        params.set("q", value);
      } else {
        params.delete("q");
      }
      params.delete("page");
      router.push(`${pathname}?${params.toString()}`);
    },
    [searchParams, router, pathname]
  );

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => updateSearch(e.target.value), 300);
  }

  function handleClear() {
    updateSearch("");
    const input = document.getElementById("admin-search") as HTMLInputElement | null;
    if (input) input.value = "";
  }

  const currentQ = searchParams.get("q") ?? "";

  return (
    <div className="relative w-64">
      <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground pointer-events-none" />
      <Input
        id="admin-search"
        placeholder={placeholder}
        defaultValue={currentQ}
        onChange={handleChange}
        className="pl-8 pr-8 h-9"
      />
      {currentQ && (
        <button
          onClick={handleClear}
          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
        >
          <X className="h-3.5 w-3.5" />
        </button>
      )}
    </div>
  );
}
