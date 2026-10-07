import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { ShieldAlert, TrendingUp, DollarSign, Activity } from 'lucide-react';

interface PortfolioSummary {
  userId: string;
  totalValue: number;
  totalCost: number;
  absoluteProfitLoss: number;
  overallRoiPercentage: number;
}

const COLORS = ['#3b82f6', '#10b981', '#f59e0b'];

const data = [
  { name: 'Stocks', value: 400 },
  { name: 'Crypto', value: 300 },
  { name: 'ETFs', value: 300 },
];

function App() {
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [feeReport, setFeeReport] = useState<string>('');

  useEffect(() => {
    // Phase 5 API wiring
    axios.get('http://localhost:8080/api/v1/portfolios/summary', {
      headers: { 'X-User-Id': '123e4567-e89b-12d3-a456-426614174000' }
    }).then(res => setSummary(res.data))
      .catch(err => console.error("API not reachable yet", err));

    axios.get('http://localhost:8080/api/v1/analytics/fee-audit')
      .then(res => setFeeReport(res.data))
      .catch(err => console.error(err));
  }, []);

  return (
    <div className="min-h-screen p-8 bg-slate-50">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Financial Intelligence</h1>
            <p className="text-slate-500 mt-1">Open-Source Shared Optimization Platform</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg font-medium shadow-sm transition-all">
            Connect Broker
          </button>
        </div>

        {/* High-Fee Alert Banner */}
        {feeReport && feeReport.includes('ALERT') && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-r-lg flex items-start gap-3 shadow-sm">
            <ShieldAlert className="text-red-500 w-6 h-6 mt-0.5" />
            <div>
              <h3 className="text-red-800 font-bold">Hidden Fee Drain Detected</h3>
              <p className="text-red-700 mt-1">{feeReport}</p>
            </div>
          </div>
        )}

        {/* Top Metric Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center gap-4">
            <div className="bg-blue-100 p-4 rounded-full text-blue-600">
              <DollarSign className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Total Net Worth</p>
              <h2 className="text-2xl font-bold text-slate-900">
                ${summary?.totalValue?.toLocaleString() || '15,000.00'}
              </h2>
            </div>
          </div>
          
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center gap-4">
            <div className="bg-emerald-100 p-4 rounded-full text-emerald-600">
              <TrendingUp className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Total Profit / Loss</p>
              <h2 className="text-2xl font-bold text-emerald-600">
                +${summary?.absoluteProfitLoss?.toLocaleString() || '3,000.00'}
              </h2>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center gap-4">
            <div className="bg-purple-100 p-4 rounded-full text-purple-600">
              <Activity className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Community Health Score</p>
              <h2 className="text-2xl font-bold text-slate-900">92 / 100</h2>
            </div>
          </div>
        </div>

        {/* Main Content Area */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Asset Allocation Chart */}
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 lg:col-span-1">
            <h3 className="text-lg font-bold text-slate-900 mb-6">Asset Allocation</h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={data}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {data.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex justify-center gap-4 mt-4 text-sm font-medium text-slate-600">
              <span className="flex items-center gap-1"><div className="w-3 h-3 rounded-full bg-blue-500"></div> Stocks</span>
              <span className="flex items-center gap-1"><div className="w-3 h-3 rounded-full bg-emerald-500"></div> Crypto</span>
              <span className="flex items-center gap-1"><div className="w-3 h-3 rounded-full bg-amber-500"></div> ETFs</span>
            </div>
          </div>

          {/* Transaction Ledger Sandbox */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-100 lg:col-span-2 overflow-hidden">
            <div className="p-6 border-b border-slate-100">
              <h3 className="text-lg font-bold text-slate-900">Transaction Ledger</h3>
            </div>
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 text-slate-500 text-sm border-b border-slate-100">
                  <th className="p-4 font-medium">Asset</th>
                  <th className="p-4 font-medium">Type</th>
                  <th className="p-4 font-medium">Quantity</th>
                  <th className="p-4 font-medium text-right">Avg Price</th>
                </tr>
              </thead>
              <tbody className="text-sm">
                <tr className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="p-4 font-semibold text-slate-900">Apple Inc. (AAPL)</td>
                  <td className="p-4"><span className="bg-blue-100 text-blue-700 px-2.5 py-1 rounded-md font-medium text-xs">STOCK</span></td>
                  <td className="p-4">50.00</td>
                  <td className="p-4 text-right font-medium">$150.00</td>
                </tr>
                <tr className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="p-4 font-semibold text-slate-900">Bitcoin (BTC)</td>
                  <td className="p-4"><span className="bg-amber-100 text-amber-700 px-2.5 py-1 rounded-md font-medium text-xs">CRYPTO</span></td>
                  <td className="p-4">0.15</td>
                  <td className="p-4 text-right font-medium">$45,000.00</td>
                </tr>
                <tr className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                  <td className="p-4 font-semibold text-slate-900">Vanguard 500 (VOO)</td>
                  <td className="p-4"><span className="bg-emerald-100 text-emerald-700 px-2.5 py-1 rounded-md font-medium text-xs">ETF</span></td>
                  <td className="p-4">12.00</td>
                  <td className="p-4 text-right font-medium">$410.00</td>
                </tr>
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </div>
  );
}

export default App;
