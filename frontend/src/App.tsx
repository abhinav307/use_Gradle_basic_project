import { useEffect, useState } from 'react';
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

interface Transaction {
  id: string;
  assetSymbol: string;
  assetName: string;
  assetCategory: string;
  quantity: number;
  purchasePrice: number;
  feePaid: number;
}

const COLORS = ['#3b82f6', '#10b981', '#f59e0b'];

function App() {
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [feeReport, setFeeReport] = useState<string>('');
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const userId = '123e4567-e89b-12d3-a456-426614174000';

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = () => {
    const headers = { 'X-User-Id': userId };
    
    axios.get('http://localhost:8080/api/v1/portfolios/summary', { headers })
      .then(res => setSummary(res.data))
      .catch(err => console.error(err));

    axios.get('http://localhost:8080/api/v1/analytics/fee-audit', { headers })
      .then(res => setFeeReport(res.data))
      .catch(err => console.error(err));
      
    axios.get('http://localhost:8080/api/v1/portfolios/transactions', { headers })
      .then(res => setTransactions(res.data))
      .catch(err => console.error(err));
  };

  const generateChartData = () => {
    const allocation: Record<string, number> = {};
    transactions.forEach(tx => {
      const val = (tx.quantity * tx.purchasePrice);
      allocation[tx.assetCategory] = (allocation[tx.assetCategory] || 0) + val;
    });
    return Object.keys(allocation).map(key => ({ name: key, value: allocation[key] }));
  };

  const chartData = generateChartData();

  return (
    <div className="min-h-screen p-8 bg-slate-50">
      <div className="max-w-7xl mx-auto space-y-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Financial Intelligence</h1>
            <p className="text-slate-500 mt-1">Open-Source Shared Optimization Platform</p>
          </div>
          <button className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg font-medium shadow-sm transition-all" onClick={fetchDashboard}>
            Refresh Data
          </button>
        </div>

        {feeReport && feeReport.includes('ALERT') && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-r-lg flex items-start gap-3 shadow-sm">
            <ShieldAlert className="text-red-500 w-6 h-6 mt-0.5" />
            <div>
              <h3 className="text-red-800 font-bold">Hidden Fee Drain Detected</h3>
              <p className="text-red-700 mt-1">{feeReport}</p>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center gap-4">
            <div className="bg-blue-100 p-4 rounded-full text-blue-600">
              <DollarSign className="w-6 h-6" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Total Net Worth</p>
              <h2 className="text-2xl font-bold text-slate-900">
                ${summary?.totalValue?.toLocaleString(undefined, {minimumFractionDigits: 2}) || '0.00'}
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
                ${summary?.absoluteProfitLoss?.toLocaleString(undefined, {minimumFractionDigits: 2}) || '0.00'}
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

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 lg:col-span-1">
            <h3 className="text-lg font-bold text-slate-900 mb-6">Asset Allocation</h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={chartData} cx="50%" cy="50%" innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value">
                    {chartData.map((_entry, index) => (
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
                {transactions.length === 0 ? (
                  <tr><td colSpan={4} className="p-4 text-center text-slate-500">No transactions found</td></tr>
                ) : transactions.map(tx => (
                  <tr key={tx.id} className="border-b border-slate-50 hover:bg-slate-50 transition-colors">
                    <td className="p-4 font-semibold text-slate-900">{tx.assetName || tx.assetSymbol}</td>
                    <td className="p-4"><span className="bg-blue-100 text-blue-700 px-2.5 py-1 rounded-md font-medium text-xs">{tx.assetCategory}</span></td>
                    <td className="p-4">{tx.quantity}</td>
                    <td className="p-4 text-right font-medium">${tx.purchasePrice}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
