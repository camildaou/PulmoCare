import { Button } from "@/components/ui/button"
import Link from "next/link"

export default function TermsAndPoliciesPage() {
  return (
    <div className="container max-w-4xl py-12">
      <div className="mb-8 text-center">
        <h1 className="text-3xl font-bold tracking-tight text-primary">PulmoCare Terms and Policies</h1>
        <p className="mt-2 text-muted-foreground">Last updated: April 1, 2025</p>
      </div>

      <div className="space-y-8">
        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">1. Introduction</h2>
          <p>
            Welcome to PulmoCare, a comprehensive pulmonologist clinic management system. These Terms and Policies
            govern your use of the PulmoCare platform and services. By accessing or using PulmoCare, you agree to be
            bound by these terms.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">2. User Accounts</h2>
          <p>
            2.1. You must register for an account to use PulmoCare. You agree to provide accurate, current, and complete
            information during registration.
          </p>
          <p>
            2.2. You are responsible for maintaining the confidentiality of your account credentials and for all
            activities that occur under your account.
          </p>
          <p>
            2.3. You must immediately notify PulmoCare of any unauthorized use of your account or any other breach of
            security.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">3. Privacy and Data Protection</h2>
          <p>
            3.1. PulmoCare collects and processes personal and medical data in accordance with applicable privacy laws
            and regulations.
          </p>
          <p>
            3.2. All medical data is encrypted and stored securely. Access to patient data is strictly limited to
            authorized healthcare providers.
          </p>
          <p>
            3.3. PulmoCare implements appropriate technical and organizational measures to protect personal data against
            unauthorized access, alteration, disclosure, or destruction.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">4. Medical Professional Responsibilities</h2>
          <p>4.1. Healthcare providers using PulmoCare must maintain valid medical licenses and credentials.</p>
          <p>4.2. Medical professionals are responsible for the accuracy of all information entered into the system.</p>
          <p>
            4.3. PulmoCare is a tool to assist healthcare providers but does not replace professional medical judgment.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">5. System Usage</h2>
          <p>
            5.1. Users agree not to use PulmoCare for any unlawful purpose or in any way that could damage, disable, or
            impair the system.
          </p>
          <p>
            5.2. Users shall not attempt to gain unauthorized access to any part of the system or any server, computer,
            or database connected to PulmoCare.
          </p>
          <p>5.3. PulmoCare reserves the right to monitor system usage to ensure compliance with these terms.</p>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">6. Limitation of Liability</h2>
          <p>6.1. PulmoCare is provided "as is" without any warranties, expressed or implied.</p>
          <p>
            6.2. PulmoCare shall not be liable for any direct, indirect, incidental, special, consequential, or punitive
            damages resulting from the use or inability to use the system.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-semibold">7. Changes to Terms</h2>
          <p>
            7.1. PulmoCare reserves the right to modify these Terms and Policies at any time. Users will be notified of
            significant changes.
          </p>
          <p>
            7.2. Continued use of PulmoCare after changes to the Terms constitutes acceptance of the modified Terms.
          </p>
        </section>
      </div>

      <div className="mt-12 flex justify-center">
        <Button asChild>
          <Link href="/">Return to Sign In</Link>
        </Button>
      </div>
    </div>
  )
}
