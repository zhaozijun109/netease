"use client";

import { useSearchParams, useRouter, usePathname } from "next/navigation";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface UrlSelectOption {
  value: string;
  label: string;
}

interface UrlSelectProps {
  param: string;
  options: UrlSelectOption[];
  placeholder?: string;
  className?: string;
}

/** URL 参数驱动的 Select，选中后更新 URL searchParam 并重置到第 1 页 */
export function UrlSelect({
  param,
  options,
  placeholder = "全部",
  className = "w-32 h-9",
}: UrlSelectProps) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const current = searchParams.get(param) ?? "";

  function handleChange(value: string) {
    const params = new URLSearchParams(searchParams.toString());
    if (value && value !== "__all__") {
      params.set(param, value);
    } else {
      params.delete(param);
    }
    params.delete("page");
    router.push(`${pathname}?${params.toString()}`);
  }

  return (
    <Select value={current || "__all__"} onValueChange={handleChange}>
      <SelectTrigger className={className}>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="__all__">{placeholder}</SelectItem>
        {options.map((opt) => (
          <SelectItem key={opt.value} value={opt.value}>
            {opt.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
