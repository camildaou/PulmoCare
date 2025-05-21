import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Label } from "@/components/ui/label"

export default function DoctorDataAnalysisPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Data Analysis</h1>
        <p className="text-muted-foreground">Prediction and detection tools for pulmonary conditions.</p>
      </div>

      <Tabs defaultValue="prediction">
        <TabsList>
          <TabsTrigger value="prediction">Prediction</TabsTrigger>

        </TabsList>

        <TabsContent value="prediction" className="space-y-6 pt-6">
          <div className="grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Asthma Predictions</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  <li className="flex items-center gap-2">
                    <span>📅</span>
                    <span>Date: 2026-01-31, Prediction: 719.69</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span>📅</span>
                    <span>Date: 2026-02-28, Prediction: 823.98</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span>📅</span>
                    <span>Date: 2026-03-31, Prediction: 761.05</span>
                  </li>
                </ul>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>COPD Predictions</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  <li className="flex items-center gap-2">
                    <span>📅</span>
                    <span>Date: 2026-01-31, Prediction: 498.22</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span>📅</span>
                    <span>Date: 2026-02-28, Prediction: 544.23</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <span>📅</span>
                    <span>Date: 2026-03-31, Prediction: 468.48</span>
                  </li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="detection" className="space-y-6 pt-6">
          <div className="grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Lung Abnormality Detection</CardTitle>
                <CardDescription>Detect abnormalities in lung X-rays and CT scans using AI.</CardDescription>
              </CardHeader>
              <CardContent>
                <form className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="patient">Patient</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="">Select a patient</option>
                      <option value="1">Alice Johnson</option>
                      <option value="2">Bob Smith</option>
                      <option value="3">Carol Williams</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="scan">Select Scan</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="">Select a scan</option>
                      <option value="xray1">Chest X-Ray (Mar 12, 2025)</option>
                      <option value="ct1">CT Scan - Lungs (Feb 15, 2025)</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="detection">Detection Type</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="nodules">Lung Nodules</option>
                      <option value="pneumonia">Pneumonia</option>
                      <option value="fibrosis">Pulmonary Fibrosis</option>
                      <option value="all">All Abnormalities</option>
                    </select>
                  </div>
                </form>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Pulmonary Function Analysis</CardTitle>
                <CardDescription>Analyze pulmonary function test results to detect abnormalities.</CardDescription>
              </CardHeader>
              <CardContent>
                <form className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="patient">Patient</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="">Select a patient</option>
                      <option value="1">Alice Johnson</option>
                      <option value="2">Bob Smith</option>
                      <option value="3">Carol Williams</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="test">Select Test</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="">Select a test</option>
                      <option value="pft1">Pulmonary Function Test (Mar 15, 2025)</option>
                      <option value="pft2">Pulmonary Function Test (Jan 10, 2025)</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="analysis">Analysis Type</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="obstruction">Airway Obstruction</option>
                      <option value="restriction">Lung Restriction</option>
                      <option value="diffusion">Diffusion Capacity</option>
                      <option value="all">Comprehensive Analysis</option>
                    </select>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
