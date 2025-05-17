import { Button } from "@/components/ui/button"
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
          <TabsTrigger value="detection">Detection</TabsTrigger>
        </TabsList>

        <TabsContent value="prediction" className="space-y-6 pt-6">
          <div className="grid gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Asthma Exacerbation Risk</CardTitle>
                <CardDescription>
                  Predict the risk of asthma exacerbation based on patient data and environmental factors.
                </CardDescription>
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
                    <Label htmlFor="timeframe">Prediction Timeframe</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="7">Next 7 days</option>
                      <option value="14">Next 14 days</option>
                      <option value="30">Next 30 days</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="factors">Include Environmental Factors</Label>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="pollen"
                        className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                      />
                      <Label htmlFor="pollen" className="text-sm font-normal">
                        Pollen Count
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="pollution"
                        className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                      />
                      <Label htmlFor="pollution" className="text-sm font-normal">
                        Air Pollution
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="weather"
                        className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                      />
                      <Label htmlFor="weather" className="text-sm font-normal">
                        Weather Changes
                      </Label>
                    </div>
                  </div>
                  <Button className="w-full">Generate Prediction</Button>
                </form>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>COPD Progression</CardTitle>
                <CardDescription>
                  Predict the progression of COPD based on patient history and current status.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="patient">Patient</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="">Select a patient</option>
                      <option value="2">Bob Smith</option>
                      <option value="4">David Brown</option>
                      <option value="8">Henry Wilson</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="timeframe">Prediction Timeframe</Label>
                    <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                      <option value="3">3 months</option>
                      <option value="6">6 months</option>
                      <option value="12">12 months</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="factors">Include Factors</Label>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="smoking"
                        className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                      />
                      <Label htmlFor="smoking" className="text-sm font-normal">
                        Smoking Status
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="medication"
                        className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                      />
                      <Label htmlFor="medication" className="text-sm font-normal">
                        Medication Adherence
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id="exercise"
                        className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                      />
                      <Label htmlFor="exercise" className="text-sm font-normal">
                        Exercise Routine
                      </Label>
                    </div>
                  </div>
                  <Button className="w-full">Generate Prediction</Button>
                </form>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Prediction Results</CardTitle>
              <CardDescription>
                No predictions generated yet. Use the forms above to generate predictions.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex h-[200px] items-center justify-center border rounded-md">
                <p className="text-muted-foreground">Prediction results will appear here</p>
              </div>
            </CardContent>
          </Card>
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
                  <Button className="w-full">Run Detection</Button>
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
                  <Button className="w-full">Run Analysis</Button>
                </form>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Detection Results</CardTitle>
              <CardDescription>
                No detections performed yet. Use the forms above to run detection algorithms.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex h-[200px] items-center justify-center border rounded-md">
                <p className="text-muted-foreground">Detection results will appear here</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
