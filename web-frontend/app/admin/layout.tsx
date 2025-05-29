import type React from "react"
import { Sidebar } from "@/components/admin/sidebar"

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode
}) {  return (
    <div className="flex min-h-screen h-full">
      <Sidebar />
      <main className="flex-1 p-6 md:p-8 overflow-auto ml-[240px]">{children}</main>
    </div>
  )
}
